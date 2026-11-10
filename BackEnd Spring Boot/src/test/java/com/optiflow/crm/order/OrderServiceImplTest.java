package com.optiflow.crm.order;

import com.optiflow.crm.auth.CurrentUserService;
import com.optiflow.crm.client.ClientRepository;
import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.inventory.InventoryRepository;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.inventory.model.InventoryService;
import com.optiflow.crm.inventory.movement.StockMovementService;
import com.optiflow.crm.inventory.movement.model.StockMovementType;
import com.optiflow.crm.order.OrderRepository;
import com.optiflow.crm.order.OrderServiceImpl;
import com.optiflow.crm.order.domain.OrderDomainService;
import com.optiflow.crm.order.dto.OrderDto;
import com.optiflow.crm.order.dto.OrderItemDto;
import com.optiflow.crm.order.dto.OrderSearchDto;
import com.optiflow.crm.order.mapper.OrderMapper;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderItem;
import com.optiflow.crm.order.model.OrderStatus;
import com.optiflow.crm.quote.QuoteRepository;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.shared.exception.ClientNotFoundException;
import com.optiflow.crm.shared.exception.InsufficientStockException;
import com.optiflow.crm.shared.exception.InvalidOrderStatusException;
import com.optiflow.crm.shared.exception.QuoteAlreadyConvertedException;
import com.optiflow.crm.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de OrderServiceImpl.
 * El servicio de dominio y los repositorios son mocks: estos tests verifican
 * coordinación, llamadas y efectos sobre la entidad. Los bloqueos reales,
 * commits/rollbacks y la concurrencia requieren tests de integración.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final long ORDER_ID = 3L;
    private static final long CLIENT_ID = 7L;
    private static final long INVENTORY_ID = 5L;

    @Mock private OrderRepository orderRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private QuoteRepository quoteRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private CurrentUserService currentUserService;
    @Mock private OrderDomainService orderDomainService;
    @Mock private InventoryService inventoryService;
    @Mock private StockMovementService stockMovementService;

    @InjectMocks private OrderServiceImpl service;

    // ---------------------------------------------------------------------
    // Datos de prueba
    // ---------------------------------------------------------------------

    private OrderDto validOrderDto() {
        OrderDto dto = new OrderDto();
        dto.setClientId(CLIENT_ID);
        return dto;
    }

    private Order orderWithItem(OrderStatus status, long inventoryId, int quantity) {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setStatus(status);
        order.setActive(true);

        InventoryItem inventory = new InventoryItem();
        inventory.setId(inventoryId);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setInventoryItem(inventory);
        item.setQuantity(quantity);
        order.getItems().add(item);
        return order;
    }

    private Order pendingOrderForUpdate() {
        Order order = orderWithItem(OrderStatus.PENDING, INVENTORY_ID, 2);
        Client client = new Client();
        client.setId(CLIENT_ID);
        order.setClient(client);
        order.setOrderDate(LocalDate.of(2026, 11, 9));
        order.setExpectedDeliveryDate(LocalDate.of(2026, 11, 20));
        order.setNotes("Antes");
        order.setTotal(new BigDecimal("121.00"));
        return order;
    }

    private void stubCreate(OrderDto dto, Order order) {
        when(clientRepository.findById(CLIENT_ID))
                .thenReturn(Optional.of(new Client()));
        when(currentUserService.getCurrentUser()).thenReturn(new User());
        when(orderMapper.toEntity(dto)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
    }

    private void stubLockedOrder(Order order) {
        when(orderRepository.findByIdForUpdate(ORDER_ID))
                .thenReturn(Optional.of(order));
    }

    // ---------------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------------

    @Test
    void givenActiveOrders_whenFindAll_thenReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(List.of(new Order()), pageable, 1);
        when(orderRepository.findByActiveTrue(pageable)).thenReturn(page);

        assertSame(page, service.findAll(pageable));
        verify(orderRepository).findByActiveTrue(pageable);
    }

    @Test
    void givenSearch_whenSearch_thenApplyPageAndSort() {
        OrderSearchDto search = new OrderSearchDto();
        search.setPage(2);
        search.setSize(5);
        search.setSortBy("orderDate");
        search.setSortDirection("asc");

        when(orderRepository.findAll(
                any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.search(search);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderRepository).findAll(any(Specification.class), captor.capture());
        Pageable actual = captor.getValue();
        assertEquals(2, actual.getPageNumber());
        assertEquals(5, actual.getPageSize());
        assertEquals(Sort.Direction.ASC,
                actual.getSort().getOrderFor("orderDate").getDirection());
    }

    @Test
    void givenExistingId_whenFindById_thenReturnOrder() {
        Order order = new Order();
        order.setId(ORDER_ID);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertSame(order, service.findById(ORDER_ID));
    }

    // ---------------------------------------------------------------------
    // Creación directa
    // ---------------------------------------------------------------------

    @Test
    void givenValidClient_whenCreate_thenGenerateNumberAndSave() {
        OrderDto dto = validOrderDto();
        Order order = new Order();
        Client client = new Client();
        User seller = new User();

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(currentUserService.getCurrentUser()).thenReturn(seller);
        when(orderMapper.toEntity(dto)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);

        assertSame(order, service.create(dto));
        assertSame(client, order.getClient());
        assertSame(seller, order.getSeller());
        assertEquals("O-" + LocalDate.now().getYear() + "-000001",
                order.getOrderNumber());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        verify(orderDomainService).initializeOrder(order);
        verify(orderDomainService).calculateTotals(order);
        verify(orderRepository).save(order);
    }

    @Test
    void givenPreviousOrder_whenCreate_thenIncrementNumber() {
        OrderDto dto = validOrderDto();
        Order order = new Order();
        stubCreate(dto, order);

        String prefix = "O-" + LocalDate.now().getYear() + "-";
        Order previous = new Order();
        previous.setOrderNumber(prefix + "000047");
        when(orderRepository.findTopByOrderNumberStartingWithOrderByOrderNumberDesc(prefix))
                .thenReturn(Optional.of(previous));

        service.create(dto);
        assertEquals(prefix + "000048", order.getOrderNumber());
    }

    @Test
    void givenMissingClient_whenCreate_thenThrow() {
        OrderDto dto = validOrderDto();
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> service.create(dto));
        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(orderMapper);
    }

    @Test
    void givenInventoryItem_whenCreate_thenBuildOrderItem() {
        OrderDto dto = validOrderDto();
        OrderItemDto lineDto = new OrderItemDto();
        lineDto.setInventoryItemId(INVENTORY_ID);
        lineDto.setQuantity(2);
        dto.setItems(List.of(lineDto));

        Order order = new Order();
        stubCreate(dto, order);

        InventoryItem inventory = new InventoryItem();
        inventory.setId(INVENTORY_ID);
        inventory.setName("Montura");
        inventory.setActive(true);
        inventory.setSalePrice(new BigDecimal("80.00"));
        inventory.setTaxRate(new BigDecimal("21.00"));
        when(inventoryRepository.findById(INVENTORY_ID))
                .thenReturn(Optional.of(inventory));

        service.create(dto);

        assertEquals(1, order.getItems().size());
        OrderItem item = order.getItems().getFirst();
        assertSame(order, item.getOrder());
        assertSame(inventory, item.getInventoryItem());
        assertEquals("Montura", item.getDescription());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("80.00"), item.getUnitPrice());
        assertEquals(BigDecimal.ZERO, item.getDiscount());
        verify(orderDomainService).calculateItem(item);
        verify(orderDomainService).calculateTotals(order);
    }

    // ---------------------------------------------------------------------
    // Conversión de presupuesto
    // ---------------------------------------------------------------------

    @Test
    void givenValidQuote_whenCreateFromQuote_thenSaveAndMarkConverted() {
        long quoteId = 5L;
        Quote quote = spy(new Quote());
        Order order = new Order();
        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(orderDomainService.createFromQuote(quote)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);

        assertSame(order, service.createFromQuote(quoteId));
        assertEquals("O-" + LocalDate.now().getYear() + "-000001",
                order.getOrderNumber());
        verify(orderDomainService).validateQuoteConversion(quote);
        verify(orderDomainService).calculateTotals(order);
        verify(quote).markAsConverted();
        verify(quoteRepository).save(quote);
    }

    @Test
    void givenAlreadyConvertedQuote_whenCreateFromQuote_thenThrow() {
        long quoteId = 5L;
        Quote quote = new Quote();
        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(orderRepository.existsByQuoteId(quoteId)).thenReturn(true);

        assertThrows(QuoteAlreadyConvertedException.class,
                () -> service.createFromQuote(quoteId));
        verify(orderRepository, never()).save(any(Order.class));
        verify(quoteRepository, never()).save(any(Quote.class));
    }

    // ---------------------------------------------------------------------
    // Edición administrativa y baja lógica
    // ---------------------------------------------------------------------

    @Test
    void givenPendingOrder_whenUpdate_thenChangeOnlyAdministrativeFields() {
        Order existing = pendingOrderForUpdate();
        OrderDto dto = validOrderDto();
        dto.setExpectedDeliveryDate(LocalDate.of(2026, 11, 24));
        dto.setNotes("Después");
        when(orderRepository.findByIdForUpdate(ORDER_ID))
                .thenReturn(Optional.of(existing));
        when(orderRepository.save(existing)).thenReturn(existing);

        Order result = service.update(ORDER_ID, dto);

        assertSame(existing, result);
        assertEquals(LocalDate.of(2026, 11, 24), existing.getExpectedDeliveryDate());
        assertEquals("Después", existing.getNotes());
        assertEquals(OrderStatus.PENDING, existing.getStatus());
        assertEquals(new BigDecimal("121.00"), existing.getTotal());
        assertEquals(1, existing.getItems().size());
        verify(orderDomainService).validateCanEdit(existing);
        verify(orderMapper, never()).updateEntity(any(), any());
        verify(orderDomainService, never()).calculateTotals(any());
        verify(orderRepository).save(existing);
    }

    @Test
    void givenDifferentClient_whenUpdate_thenReject() {
        Order existing = pendingOrderForUpdate();
        OrderDto dto = validOrderDto();
        dto.setClientId(99L);
        stubLockedOrder(existing);

        assertThrows(InvalidOrderStatusException.class,
                () -> service.update(ORDER_ID, dto));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void givenLinesInPayload_whenUpdate_thenReject() {
        Order existing = pendingOrderForUpdate();
        OrderDto dto = validOrderDto();
        dto.setItems(List.of(new OrderItemDto()));
        stubLockedOrder(existing);

        assertThrows(InvalidOrderStatusException.class,
                () -> service.update(ORDER_ID, dto));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void givenConfirmedOrder_whenUpdate_thenReject() {
        Order existing = pendingOrderForUpdate();
        existing.setStatus(OrderStatus.CONFIRMED);
        OrderDto dto = validOrderDto();
        stubLockedOrder(existing);
        doThrow(new InvalidOrderStatusException("Solo pendientes"))
                .when(orderDomainService).validateCanEdit(existing);

        assertThrows(InvalidOrderStatusException.class,
                () -> service.update(ORDER_ID, dto));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void givenPendingOrder_whenDelete_thenDeactivate() {
        Order order = pendingOrderForUpdate();
        stubLockedOrder(order);

        service.delete(ORDER_ID);

        assertFalse(order.getActive());
        verify(orderDomainService).validateCanDelete(order);
        verify(orderRepository).save(order);
        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    void givenConfirmedOrder_whenDelete_thenReject() {
        Order order = pendingOrderForUpdate();
        order.setStatus(OrderStatus.CONFIRMED);
        stubLockedOrder(order);
        doThrow(new InvalidOrderStatusException("No se puede desactivar"))
                .when(orderDomainService).validateCanDelete(order);

        assertThrows(InvalidOrderStatusException.class,
                () -> service.delete(ORDER_ID));
        verify(orderRepository, never()).save(any());
    }

    // ---------------------------------------------------------------------
    // Confirmación: bloqueo, reservas y stock
    // ---------------------------------------------------------------------

    @Test
    void givenPendingOrder_whenConfirm_thenLockAndRegisterStockExit() {
        Order order = orderWithItem(OrderStatus.PENDING, INVENTORY_ID, 2);
        stubLockedOrder(order);
        when(orderRepository.save(order)).thenReturn(order);

        assertSame(order, service.confirm(ORDER_ID));
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(orderRepository).findByIdForUpdate(ORDER_ID);
        verify(orderDomainService).validateCanConfirm(order);
        verify(stockMovementService).applyOrderMovement(
                INVENTORY_ID, ORDER_ID, 2, StockMovementType.EXIT);
        verifyNoInteractions(inventoryService);
        verify(orderRepository).save(order);
    }

    @Test
    void givenRepeatedInventoryItem_whenConfirm_thenAggregateQuantity() {
        Order order = orderWithItem(OrderStatus.PENDING, INVENTORY_ID, 2);
        InventoryItem inventory = new InventoryItem();
        inventory.setId(INVENTORY_ID);
        OrderItem second = new OrderItem();
        second.setOrder(order);
        second.setInventoryItem(inventory);
        second.setQuantity(3);
        order.getItems().add(second);
        stubLockedOrder(order);
        when(orderRepository.save(order)).thenReturn(order);

        service.confirm(ORDER_ID);
        verify(stockMovementService, times(1)).applyOrderMovement(
                INVENTORY_ID, ORDER_ID, 5, StockMovementType.EXIT);
        verifyNoInteractions(inventoryService);
    }

    @Test
    void givenInsufficientStock_whenConfirm_thenDoNotChangeStatus() {
        Order order = orderWithItem(OrderStatus.PENDING, INVENTORY_ID, 10);
        stubLockedOrder(order);
        doThrow(new InsufficientStockException(INVENTORY_ID, 2, 10))
                .when(stockMovementService).applyOrderMovement(
                        INVENTORY_ID, ORDER_ID, 10, StockMovementType.EXIT);

        assertThrows(InsufficientStockException.class,
                () -> service.confirm(ORDER_ID));
        assertEquals(OrderStatus.PENDING, order.getStatus());
        verify(orderRepository, never()).save(any());
        verifyNoInteractions(inventoryService);
    }

    @Test
    void givenAlreadyConfirmedOrder_whenConfirm_thenRejectWithoutStockChange() {
        Order order = orderWithItem(OrderStatus.CONFIRMED, INVENTORY_ID, 2);
        stubLockedOrder(order);
        doThrow(new InvalidOrderStatusException("Ya confirmado"))
                .when(orderDomainService).validateCanConfirm(order);

        assertThrows(InvalidOrderStatusException.class,
                () -> service.confirm(ORDER_ID));
        verifyNoInteractions(stockMovementService, inventoryService);
        verify(orderRepository, never()).save(any());
    }

    // ---------------------------------------------------------------------
    // Cancelación
    // ---------------------------------------------------------------------

    @Test
    void givenConfirmedOrder_whenCancel_thenRegisterStockEntry() {
        Order order = orderWithItem(OrderStatus.CONFIRMED, INVENTORY_ID, 2);
        stubLockedOrder(order);
        when(orderRepository.save(order)).thenReturn(order);

        assertSame(order, service.cancel(ORDER_ID));
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderDomainService).validateCanCancel(order);
        verify(stockMovementService).applyOrderMovement(
                INVENTORY_ID, ORDER_ID, 2, StockMovementType.ENTRY);
        verifyNoInteractions(inventoryService);
        verify(orderRepository).save(order);
    }


    @Test
    void givenInProgressOrder_whenCancel_thenRestoreStockWithAuditedEntry() {
        Order order = orderWithItem(OrderStatus.IN_PROGRESS, INVENTORY_ID, 4);
        stubLockedOrder(order);
        when(orderRepository.save(order)).thenReturn(order);

        assertSame(order, service.cancel(ORDER_ID));
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(stockMovementService).applyOrderMovement(
                INVENTORY_ID, ORDER_ID, 4, StockMovementType.ENTRY);
        verifyNoInteractions(inventoryService);
    }

    @Test
    void givenPendingOrder_whenCancel_thenDoNotRegisterStockMovement() {
        Order order = orderWithItem(OrderStatus.PENDING, INVENTORY_ID, 2);
        stubLockedOrder(order);
        when(orderRepository.save(order)).thenReturn(order);

        service.cancel(ORDER_ID);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verifyNoInteractions(stockMovementService, inventoryService);
        verify(orderRepository).save(order);
    }

    @Test
    void givenCancelledOrder_whenCancelAgain_thenRejectWithoutRestoringStock() {

        // GIVEN: pedido previamente cancelado
        Order order = orderWithItem(
                OrderStatus.CANCELLED,
                INVENTORY_ID,
                2
        );

        stubLockedOrder(order);

        doThrow(new InvalidOrderStatusException("Ya cancelado"))
                .when(orderDomainService)
                .validateCanCancel(order);

        // WHEN + THEN: no permite cancelar dos veces
        assertThrows(
                InvalidOrderStatusException.class,
                () -> service.cancel(ORDER_ID)
        );

        // No debe restaurar el stock nuevamente
        verifyNoInteractions(stockMovementService, inventoryService);

        // No debe guardar ningún cambio
        verify(orderRepository, never()).save(any());
    }

}