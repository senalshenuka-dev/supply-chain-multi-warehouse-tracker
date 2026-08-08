package com.supplychainmultiwarehousetracker.service;

import com.supplychainmultiwarehousetracker.domain.model.*;
import com.supplychainmultiwarehousetracker.domain.repository.*;
import com.supplychainmultiwarehousetracker.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockTransferService {

    private final StockTransferRepository transferRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final WarehouseInventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional(rollbackFor = Exception.class)
    public TransferResponseDto initiateTransfer(TransferRequestDto request, String username) {
        if (request.getSourceWarehouseId().equals(request.getTargetWarehouseId())) {
            throw new IllegalArgumentException("Source and target warehouse cannot be the same");
        }

        Warehouse sourceWarehouse = warehouseRepository.findById(request.getSourceWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Source warehouse not found ID: " + request.getSourceWarehouseId()));
        Warehouse targetWarehouse = warehouseRepository.findById(request.getTargetWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Target warehouse not found ID: " + request.getTargetWarehouseId()));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        String transferNumber = "TR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        StockTransfer transfer = StockTransfer.builder()
                .transferNumber(transferNumber)
                .sourceWarehouse(sourceWarehouse)
                .targetWarehouse(targetWarehouse)
                .status(TransferStatus.REQUESTED)
                .initiatedBy(user)
                .build();

        for (TransferItemRequestDto itemDto : request.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found ID: " + itemDto.getProductId()));

            // 1. REQUESTED State: Validate source warehouse available stock and lock row using PESSIMISTIC_WRITE
            WarehouseInventory sourceInventory = inventoryRepository
                    .findByWarehouseIdAndProductIdWithLock(sourceWarehouse.getId(), product.getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Product " + product.getSku() + " does not exist in source warehouse inventory"));

            int availableStock = sourceInventory.getQuantity() - sourceInventory.getReservedQuantity();
            if (availableStock < itemDto.getRequestedQuantity()) {
                throw new IllegalStateException(
                        String.format("Insufficient available stock for product %s in %s. Available: %d, Requested: %d",
                                product.getSku(), sourceWarehouse.getName(), availableStock, itemDto.getRequestedQuantity())
                );
            }

            // Increment reserved quantity
            sourceInventory.setReservedQuantity(sourceInventory.getReservedQuantity() + itemDto.getRequestedQuantity());
            inventoryRepository.save(sourceInventory);

            TransferItem transferItem = TransferItem.builder()
                    .product(product)
                    .requestedQuantity(itemDto.getRequestedQuantity())
                    .transferredQuantity(0)
                    .build();

            transfer.addItem(transferItem);
        }

        StockTransfer savedTransfer = transferRepository.save(transfer);

        auditLogService.log("StockTransfer", savedTransfer.getId().toString(), "INITIATE_TRANSFER", username,
                String.format("Initiated transfer %s from %s to %s with %d item(s)",
                        transferNumber, sourceWarehouse.getCode(), targetWarehouse.getCode(), request.getItems().size()));

        return mapToDto(savedTransfer);
    }

    @Transactional(rollbackFor = Exception.class)
    public TransferResponseDto updateTransferStatus(Long transferId, TransferStatus targetStatus, String username) {
        StockTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Stock transfer not found ID: " + transferId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        TransferStatus currentStatus = transfer.getStatus();

        if (currentStatus == targetStatus) {
            return mapToDto(transfer);
        }

        if (targetStatus == TransferStatus.DISPATCHED) {
            if (currentStatus != TransferStatus.REQUESTED) {
                throw new IllegalStateException("Transfer can only be DISPATCHED from REQUESTED status. Current: " + currentStatus);
            }

            // 2. DISPATCHED State: Lock source inventory. Deduct both quantity and reserved_quantity from source warehouse.
            for (TransferItem item : transfer.getItems()) {
                WarehouseInventory sourceInv = inventoryRepository
                        .findByWarehouseIdAndProductIdWithLock(transfer.getSourceWarehouse().getId(), item.getProduct().getId())
                        .orElseThrow(() -> new IllegalStateException("Source inventory row not found"));

                sourceInv.setQuantity(sourceInv.getQuantity() - item.getRequestedQuantity());
                sourceInv.setReservedQuantity(sourceInv.getReservedQuantity() - item.getRequestedQuantity());
                inventoryRepository.save(sourceInv);

                item.setTransferredQuantity(item.getRequestedQuantity());
            }

            transfer.setStatus(TransferStatus.DISPATCHED);
            transfer.setDispatchedBy(user);

            auditLogService.log("StockTransfer", transfer.getId().toString(), "DISPATCH_TRANSFER", username,
                    "Dispatched transfer " + transfer.getTransferNumber());

        } else if (targetStatus == TransferStatus.RECEIVED) {
            if (currentStatus != TransferStatus.DISPATCHED) {
                throw new IllegalStateException("Transfer can only be RECEIVED from DISPATCHED status. Current: " + currentStatus);
            }

            // 3. RECEIVED State: Lock target inventory. Increment target warehouse quantity. Set transfer status to RECEIVED.
            for (TransferItem item : transfer.getItems()) {
                WarehouseInventory targetInv = inventoryRepository
                        .findByWarehouseIdAndProductIdWithLock(transfer.getTargetWarehouse().getId(), item.getProduct().getId())
                        .orElseGet(() -> {
                            WarehouseInventoryId key = new WarehouseInventoryId(transfer.getTargetWarehouse().getId(), item.getProduct().getId());
                            return WarehouseInventory.builder()
                                    .id(key)
                                    .warehouse(transfer.getTargetWarehouse())
                                    .product(item.getProduct())
                                    .quantity(0)
                                    .reservedQuantity(0)
                                    .build();
                        });

                targetInv.setQuantity(targetInv.getQuantity() + item.getRequestedQuantity());
                inventoryRepository.save(targetInv);
            }

            transfer.setStatus(TransferStatus.RECEIVED);
            transfer.setReceivedBy(user);

            auditLogService.log("StockTransfer", transfer.getId().toString(), "RECEIVE_TRANSFER", username,
                    "Received transfer " + transfer.getTransferNumber() + " at " + transfer.getTargetWarehouse().getName());

        } else if (targetStatus == TransferStatus.CANCELLED) {
            if (currentStatus == TransferStatus.RECEIVED) {
                throw new IllegalStateException("Cannot cancel a transfer that has already been RECEIVED");
            }

            if (currentStatus == TransferStatus.REQUESTED) {
                // Release reserved stock back
                for (TransferItem item : transfer.getItems()) {
                    WarehouseInventory sourceInv = inventoryRepository
                            .findByWarehouseIdAndProductIdWithLock(transfer.getSourceWarehouse().getId(), item.getProduct().getId())
                            .orElse(null);
                    if (sourceInv != null) {
                        sourceInv.setReservedQuantity(Math.max(0, sourceInv.getReservedQuantity() - item.getRequestedQuantity()));
                        inventoryRepository.save(sourceInv);
                    }
                }
            }

            transfer.setStatus(TransferStatus.CANCELLED);
            auditLogService.log("StockTransfer", transfer.getId().toString(), "CANCEL_TRANSFER", username,
                    "Cancelled transfer " + transfer.getTransferNumber());
        } else {
            throw new IllegalArgumentException("Unsupported target transfer status: " + targetStatus);
        }

        StockTransfer updatedTransfer = transferRepository.save(transfer);
        return mapToDto(updatedTransfer);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<TransferResponseDto> getTransfersPaginated(TransferStatus status, Long warehouseId, Pageable pageable) {
        Page<StockTransfer> page = transferRepository.findTransfersFiltered(status, warehouseId, pageable);
        return PageResponseDto.fromPage(page.map(this::mapToDto));
    }

    @Transactional(readOnly = true)
    public TransferResponseDto getTransferById(Long id) {
        StockTransfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stock transfer not found ID: " + id));
        return mapToDto(transfer);
    }

    public TransferResponseDto mapToDto(StockTransfer transfer) {
        return TransferResponseDto.builder()
                .id(transfer.getId())
                .transferNumber(transfer.getTransferNumber())
                .sourceWarehouseId(transfer.getSourceWarehouse().getId())
                .sourceWarehouseCode(transfer.getSourceWarehouse().getCode())
                .sourceWarehouseName(transfer.getSourceWarehouse().getName())
                .targetWarehouseId(transfer.getTargetWarehouse().getId())
                .targetWarehouseCode(transfer.getTargetWarehouse().getCode())
                .targetWarehouseName(transfer.getTargetWarehouse().getName())
                .status(transfer.getStatus())
                .initiatedByUsername(transfer.getInitiatedBy() != null ? transfer.getInitiatedBy().getUsername() : null)
                .dispatchedByUsername(transfer.getDispatchedBy() != null ? transfer.getDispatchedBy().getUsername() : null)
                .receivedByUsername(transfer.getReceivedBy() != null ? transfer.getReceivedBy().getUsername() : null)
                .createdAt(transfer.getCreatedAt())
                .updatedAt(transfer.getUpdatedAt())
                .items(transfer.getItems().stream().map(item -> TransferItemResponseDto.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productSku(item.getProduct().getSku())
                        .productName(item.getProduct().getName())
                        .requestedQuantity(item.getRequestedQuantity())
                        .transferredQuantity(item.getTransferredQuantity())
                        .build()
                ).collect(Collectors.toList()))
                .build();
    }
}
