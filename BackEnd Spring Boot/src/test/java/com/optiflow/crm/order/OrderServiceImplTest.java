package com.optiflow.crm.order;

import com.optiflow.crm.auth.CurrentUserService;
import com.optiflow.crm.client.ClientRepository;
import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.inventory.InventoryRepository;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.inventory.model.InventoryService;
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
import com.optiflow.crm.shared.exception.*;

import com.optiflow.crm.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private OrderDomainService orderDomainService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderServiceImpl service;

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private OrderDto validOrderDto() {
        OrderDto dto = new OrderDto();
        dto.setClientId(7L);
        return dto;
    }

    private Order orderWithItem(OrderStatus status, Long inventoryId, int quantity) {

        Order order = new Order();
        order.setStatus(status);

        InventoryItem inventory = new InventoryItem();
        inventory.setId(inventoryId);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setInventoryItem(inventory);
        item.setQuantity(quantity);

        order.getItems().add(item);

        return order;
    }

    private void prepareCreate(OrderDto dto, Order order) {

        Client client = new Client();
        User seller = new User();

        when(clientRepository.findById(7L)).thenReturn(Optional.of(client));
        when(orderMapper.toEntity(dto)).thenReturn(order);
        when(currentUserService.getCurrentUser()).thenReturn(seller);
        when(orderRepository.save(order)).thenReturn(order);
    }

    // =====================================
    // 1. FIND ALL
    // =====================================

    @Test
    void givenActiveOrders_whenFindAll_thenReturnPage() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Order> page = new PageImpl<>(List.of(new Order()), pageable, 1);

        when(orderRepository.findByActiveTrue(pageable)).thenReturn(page);

        Page<Order> result = service.findAll(pageable);

        assertSame(page, result);

        verify(orderRepository).findByActiveTrue(pageable);
    }

    // =====================================
    // 2. SEARCH
    // =====================================

    @Test
    void givenSearch_whenSearch_thenApplyPageAndSort() {

        OrderSearchDto search = new OrderSearchDto();
        search.setPage(2);
        search.setSize(5);
        search.setSortBy("orderDate");
        search.setSortDirection("asc");

        when(orderRepository.findAll(any(Specification.class), any(Pageable.class)
        )).thenReturn(Page.empty());

        service.search(search);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        verify(orderRepository).findAll(any(Specification.class), captor.capture());

        Pageable actual = captor.getValue();

        assertEquals(2, actual.getPageNumber());
        assertEquals(5, actual.getPageSize());

        assertTrue(actual.getSort().getOrderFor("orderDate").isAscending());
    }

    // =====================================
    // 3. FIND BY ID
    // =====================================

    @Test
    void givenExistingId_whenFindById_thenReturnOrder() {

        Order order = new Order();
        order.setId(3L);

        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

        Order result = service.findById(3L);

        assertSame(order, result);
    }

    // =====================================
    // 4. CREATE
    // =====================================

    @Test
    void givenValidClient_whenCreate_thenGenerateNumberAndSave() {

        // GIVEN
        OrderDto dto = validOrderDto();

        Order order = new Order();

        Client client = new Client();
        User seller = new User();

        when(clientRepository.findById(7L)).thenReturn(Optional.of(client));
        when(orderMapper.toEntity(dto)).thenReturn(order);
        when(currentUserService.getCurrentUser()).thenReturn(seller);
        when(orderRepository.save(order)).thenReturn(order);

        // WHEN
        Order result = service.create(dto);

        // THEN
        assertSame(order, result);

        assertSame(client, order.getClient());
        assertSame(seller, order.getSeller());

        assertEquals("O-" + LocalDate.now().getYear() + "-000001",
                order.getOrderNumber());

        verify(orderDomainService).initializeOrder(order);
        verify(orderDomainService).calculateTotals(order);
        verify(orderRepository).save(order);
    }

    // =====================================
    // 5. CORRELATIVE NUMBER
    // =====================================

    @Test
    void givenPreviousOrder_whenCreate_thenIncrementNumber() {

        OrderDto dto = validOrderDto();
        Order order = new Order();

        prepareCreate(dto, order);

        String prefix = "O-" + LocalDate.now().getYear() + "-";

        Order previous = new Order();
        previous.setOrderNumber(prefix + "000047");

        when(orderRepository.findTopByOrderNumberStartingWithOrderByOrderNumberDesc(
                        prefix)).thenReturn(Optional.of(previous));

        service.create(dto);

        assertEquals(prefix + "000048", order.getOrderNumber());
    }

    // =====================================
    // 6. MISSING CLIENT
    // =====================================

    @Test
    void givenMissingClient_whenCreate_thenThrow() {

        OrderDto dto = validOrderDto();

        when(clientRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> service.create(dto));

        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(orderMapper);
    }

    // =====================================
    // 7. CREATE WITH INVENTORY
    // =====================================

    @Test
    void givenInventoryItem_whenCreate_thenBuildOrderItem() {

        OrderDto dto = validOrderDto();

        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setInventoryItemId(5L);
        itemDto.setQuantity(2);

        dto.setItems(List.of(itemDto));

        Order order = new Order();
        prepareCreate(dto, order);

        InventoryItem inventory = new InventoryItem();
        inventory.setId(5L);
        inventory.setName("Montura");
        inventory.setActive(true);
        inventory.setSalePrice(new BigDecimal("80.00"));
        inventory.setTaxRate(new BigDecimal("21.00"));

        when(inventoryRepository.findById(5L)).thenReturn(Optional.of(inventory));

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

    // =====================================
    // 8. CREATE FROM QUOTE
    // =====================================

    @Test
    void givenValidQuote_whenCreateFromQuote_thenSaveAndMarkConverted() {

        Long quoteId = 5L;

        Quote quote = spy(new Quote());
        Order order = new Order();

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(orderRepository.existsByQuoteId(quoteId)).thenReturn(false);
        when(orderDomainService.createFromQuote(quote)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);

        Order result = service.createFromQuote(quoteId);

        assertSame(order, result);
        assertEquals("O-" + LocalDate.now().getYear() + "-000001",
                order.getOrderNumber());

        verify(orderDomainService).validateQuoteConversion(quote);
        verify(orderDomainService).calculateTotals(order);
        verify(quote).markAsConverted();
        verify(quoteRepository).save(quote);
    }

    // =====================================
    // 9. QUOTE ALREADY CONVERTED
    // =====================================

    @Test
    void givenConvertedQuote_whenCreateFromQuote_thenThrow() {

        Long quoteId = 5L;
        Quote quote = new Quote();

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(orderRepository.existsByQuoteId(quoteId)).thenReturn(true);

        assertThrows(QuoteAlreadyConvertedException.class, () -> service.createFromQuote(quoteId));

        verify(orderRepository, never()).save(any(Order.class));
        verify(quoteRepository, never()).save(any(Quote.class));
    }

    // =====================================
    // 10. UPDATE
    // =====================================

    @Test
    void givenExistingOrder_whenUpdate_thenSaveAndRecalculate() {

        OrderDto dto = new OrderDto();
        Order existing = new Order();

        when(orderRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(existing)).thenReturn(existing);

        Order result = service.update(3L, dto);

        assertSame(existing, result);

        verify(orderMapper).updateEntity(dto, existing);
        verify(orderDomainService).initializeOrder(existing);
        verify(orderDomainService).calculateTotals(existing);
        verify(orderRepository).save(existing);
    }

    // =====================================
    // 11. SOFT DELETE
    // =====================================

    @Test
    void givenExistingOrder_whenDelete_thenDeactivate() {

        Order order = new Order();

        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

        service.delete(3L);

        assertFalse(order.getActive());

        verify(orderRepository).save(order);
        verify(orderRepository, never()).delete(any(Order.class));
    }

    // =====================================
    // 12. CONFIRM ORDER
    // =====================================

    @Test
    void givenPendingOrder_whenConfirm_thenDecreaseStock() {

        Order order = orderWithItem(OrderStatus.PENDING, 5L, 2);

        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = service.confirm(3L);

        assertSame(order, result);
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());

        verify(orderDomainService).validateCanConfirm(order);
        verify(inventoryService).decreaseStock(5L, 2);
        verify(orderRepository).save(order);
    }

    // =====================================
    // 13. CANCEL CONFIRMED ORDER
    // =====================================

    @Test
    void givenConfirmedOrder_whenCancel_thenRestoreStock() {

        Order order = orderWithItem(OrderStatus.CONFIRMED, 5L, 2);

        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = service.cancel(3L);

        assertSame(order, result);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        verify(orderDomainService).validateCanCancel(order);
        verify(inventoryService).increaseStock(5L, 2);
        verify(orderRepository).save(order);
    }

    // =====================================
    // 14. CANCEL PENDING ORDER
    // =====================================

    @Test
    void givenPendingOrder_whenCancel_thenDoNotRestoreStock() {

        Order order = orderWithItem(OrderStatus.PENDING, 5L, 2);

        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        service.cancel(3L);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        verifyNoInteractions(inventoryService);

        verify(orderRepository).save(order);
    }
}
