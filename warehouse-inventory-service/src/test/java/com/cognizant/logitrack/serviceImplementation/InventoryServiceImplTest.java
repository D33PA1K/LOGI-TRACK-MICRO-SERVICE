package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.dto.InventoryDTO;
import com.cognizant.logitrack.entity.WarehouseInventory;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.repository.WarehouseInventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Stock arithmetic. The invariant under test throughout: quantities never go
 * negative, and stock is never created or destroyed by a reserve/release pair.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private WarehouseInventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private WarehouseInventory stock(int onHand, Integer reserved) {
        return WarehouseInventory.builder()
                .inventoryId(1)
                .sku("SKU-1001")
                .warehouseId(1)
                .quantityOnHand(onHand)
                .quantityReserved(reserved)
                .build();
    }

    private void given(WarehouseInventory inventory) {
        when(inventoryRepository.findById(1)).thenReturn(Optional.of(inventory));
        lenient().when(inventoryRepository.save(any(WarehouseInventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("reserving moves stock from on-hand to reserved without changing the total")
    void reserveMovesStockWithoutChangingTheTotal() {
        given(stock(100, 0));

        InventoryDTO result = inventoryService.reserveStock(1, 30);

        assertEquals(70, result.getQuantityOnHand());
        assertEquals(30, result.getQuantityReserved());
        assertEquals(100, result.getQuantityOnHand() + result.getQuantityReserved());
    }

    @Test
    @DisplayName("reserving more than is on hand is rejected, and names the shortfall")
    void cannotReserveMoreThanOnHand() {
        given(stock(10, 0));

        BadRequestException error = assertThrows(BadRequestException.class,
                () -> inventoryService.reserveStock(1, 11));

        assertTrue(error.getMessage().contains("SKU-1001"), error.getMessage());
        assertTrue(error.getMessage().contains("10"), error.getMessage());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("releasing more than is reserved is rejected — it used to invent stock")
    void cannotReleaseMoreThanReserved() {
        given(stock(0, 5));

        BadRequestException error = assertThrows(BadRequestException.class,
                () -> inventoryService.releaseStock(1, 6));

        assertTrue(error.getMessage().contains("only 5 are reserved"), error.getMessage());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("a reserve then release round-trip restores the original quantities")
    void reserveThenReleaseIsLossless() {
        WarehouseInventory inventory = stock(100, 0);
        given(inventory);

        inventoryService.reserveStock(1, 40);
        InventoryDTO result = inventoryService.releaseStock(1, 40);

        assertEquals(100, result.getQuantityOnHand());
        assertEquals(0, result.getQuantityReserved());
    }

    @Test
    @DisplayName("consuming draws from available on-hand first, leaving reservations intact")
    void consumeDrawsFromOnHandFirst() {
        given(stock(70, 30));

        InventoryDTO result = inventoryService.consumeStock(1, 30);

        assertEquals(40, result.getQuantityOnHand(), "on-hand is reduced by the consumed amount");
        assertEquals(30, result.getQuantityReserved(), "reservations are untouched while on-hand covers it");
    }

    @Test
    @DisplayName("received stock (reserved = 0) is consumable directly — the original bug")
    void consumeWorksOnFreshlyReceivedStock() {
        given(stock(50, 0));

        InventoryDTO result = inventoryService.consumeStock(1, 20);

        assertEquals(30, result.getQuantityOnHand());
        assertEquals(0, result.getQuantityReserved());
    }

    @Test
    @DisplayName("consuming falls back to reserved once on-hand is exhausted")
    void consumeFallsBackToReserved() {
        given(stock(5, 10));

        InventoryDTO result = inventoryService.consumeStock(1, 8);

        assertEquals(0, result.getQuantityOnHand());
        assertEquals(7, result.getQuantityReserved(), "the remainder beyond on-hand comes out of reserved");
    }

    @Test
    @DisplayName("consuming more than total physical stock (on-hand + reserved) is rejected")
    void cannotConsumeMoreThanTotalStock() {
        given(stock(70, 10));

        BadRequestException error = assertThrows(BadRequestException.class,
                () -> inventoryService.consumeStock(1, 81));

        assertTrue(error.getMessage().contains("80 available"), error.getMessage());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("a null reserved quantity is treated as zero rather than throwing")
    void nullReservedIsTreatedAsZero() {
        given(stock(50, null));

        InventoryDTO result = inventoryService.reserveStock(1, 10);

        assertEquals(40, result.getQuantityOnHand());
        assertEquals(10, result.getQuantityReserved());
    }

    @Test
    @DisplayName("zero and negative quantities are rejected for every stock operation")
    void nonPositiveQuantitiesAreRejected() {
        assertThrows(BadRequestException.class, () -> inventoryService.reserveStock(1, 0));
        assertThrows(BadRequestException.class, () -> inventoryService.reserveStock(1, -5));
        assertThrows(BadRequestException.class, () -> inventoryService.releaseStock(1, 0));
        assertThrows(BadRequestException.class, () -> inventoryService.consumeStock(1, -1));

        verifyNoInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("on-hand quantity cannot be set negative")
    void updateQuantityRejectsNegative() {
        assertThrows(BadRequestException.class, () -> inventoryService.updateQuantity(1, -1));
        assertThrows(BadRequestException.class, () -> inventoryService.updateQuantity(1, null));

        verifyNoInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("setting on-hand to zero is allowed — an empty bin is legitimate")
    void updateQuantityAllowsZero() {
        given(stock(10, 0));

        assertEquals(0, inventoryService.updateQuantity(1, 0).getQuantityOnHand());
    }
}
