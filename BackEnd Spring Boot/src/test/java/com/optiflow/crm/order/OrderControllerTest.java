package com.optiflow.crm.order;

import com.optiflow.crm.order.dto.OrderDto;
import com.optiflow.crm.order.dto.OrderSearchDto;
import com.optiflow.crm.order.mapper.OrderMapper;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderService;
import com.optiflow.crm.order.model.OrderStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {

    private OrderService orderService;
    private OrderMapper orderMapper;
    private MockMvc mockMvc;

    private LocalValidatorFactoryBean validator;

    // =====================================
    // CONFIGURACIÓN
    // =====================================

    @BeforeEach
    void setUp() {

        orderService = mock(OrderService.class);
        orderMapper = mock(OrderMapper.class);

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        OrderController controller =
                new OrderController(orderService, orderMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver())
                .setValidator(validator)
                .setMessageConverters(
                        new JacksonJsonHttpMessageConverter(
                                JsonMapper.builder().build()))
                .build();
    }

    // =====================================
    // MÉTODOS AUXILIARES
    // =====================================

    private Order createOrder(Long id) {

        Order order = new Order();
        order.setId(id);

        return order;
    }

    private OrderDto createOrderDto(Long id) {

        OrderDto dto = new OrderDto();

        dto.setId(id);
        dto.setClientId(7L);
        dto.setOrderNumber("O-2026-000001");
        dto.setStatus(OrderStatus.PENDING);
        dto.setNotes("Pedido de prueba");

        return dto;
    }

    private String validOrderJson() {

        return """
                {
                    "clientId": 7,
                    "notes": "Pedido de prueba"
                }
                """;
    }

    // =====================================
    // 1. FIND ALL
    // =====================================

    @Test
    void givenOrders_whenFindAll_thenReturn200AndPagedJson() throws Exception {

        // GIVEN
        Order order = createOrder(1L);
        OrderDto dto = createOrderDto(1L);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);

        when(orderService.findAll(any(Pageable.class))).thenReturn(page);
        when(orderMapper.toDto(order)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].clientId").value(7))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        // VERIFY
        verify(orderService).findAll(argThat(p ->
                        p.getPageNumber() == 0 && p.getPageSize() == 10));

        verify(orderMapper).toDto(order);
    }

    // =====================================
    // 2. SEARCH
    // =====================================

    @Test
    void givenSearchBody_whenSearch_thenReturn200() throws Exception {

        // GIVEN
        Order order = createOrder(2L);
        OrderDto dto = createOrderDto(2L);

        Page<Order> page = new PageImpl<>(List.of(order),
                PageRequest.of(0, 10), 1);

        when(orderService.search(any(OrderSearchDto.class))).thenReturn(page);
        when(orderMapper.toDto(order)).thenReturn(dto);

        String json = """
                {
                    "clientId": 7,
                    "page": 0,
                    "size": 10
                }
                """;

        // WHEN + THEN
        mockMvc.perform(post("/orders/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.totalElements").value(1));

        // VERIFY
        verify(orderService).search(argThat(search ->
                        Long.valueOf(7L).equals(search.getClientId())
                                && Integer.valueOf(0).equals(search.getPage())
                                && Integer.valueOf(10).equals(search.getSize())));
        verify(orderMapper).toDto(order);
    }

    // =====================================
    // 3. FIND BY ID
    // =====================================

    @Test
    void givenExistingId_whenFindById_thenReturn200() throws Exception {

        // GIVEN
        Order order = createOrder(5L);
        OrderDto dto = createOrderDto(5L);

        when(orderService.findById(5L)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/orders/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.clientId").value(7))
                .andExpect(jsonPath("$.orderNumber")
                        .value("O-2026-000001"));

        verify(orderService).findById(5L);
        verify(orderMapper).toDto(order);
    }

    // =====================================
    // 4. FIND BY CLIENT
    // =====================================

    @Test
    void givenClientId_whenFindByClient_thenReturnPage() throws Exception {

        // GIVEN
        Order order = createOrder(3L);
        OrderDto dto = createOrderDto(3L);

        Page<Order> page = new PageImpl<>(List.of(order),
                PageRequest.of(0, 5), 1);

        when(orderService.findByClientId(eq(7L), any(Pageable.class))).thenReturn(page);
        when(orderMapper.toDto(order)).thenReturn(dto);

        // WHEN + THEN
        mockMvc.perform(get("/orders/client/7")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(orderService).findByClientId(eq(7L), argThat(p ->
                        p.getPageNumber() == 0 && p.getPageSize() == 5));
    }

    // =====================================
    // 5. CREATE
    // =====================================

    @Test
    void givenValidOrder_whenCreate_thenReturn201() throws Exception {

        // GIVEN
        Order saved = createOrder(9L);
        OrderDto responseDto = createOrderDto(9L);

        when(orderService.create(any(OrderDto.class))).thenReturn(saved);
        when(orderMapper.toDto(saved)).thenReturn(responseDto);

        // WHEN + THEN
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOrderJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.clientId").value(7));

        // VERIFY
        verify(orderService).create(argThat(dto ->
                        Long.valueOf(7L).equals(dto.getClientId())));

        verify(orderMapper).toDto(saved);
    }

    // =====================================
    // 6. CREATE FROM QUOTE
    // =====================================

    @Test
    void givenQuoteId_whenCreateFromQuote_thenReturn201() throws Exception {

        // GIVEN
        Order saved = createOrder(9L);
        OrderDto responseDto = createOrderDto(9L);
        responseDto.setQuoteId(5L);

        when(orderService.createFromQuote(5L)).thenReturn(saved);
        when(orderMapper.toDto(saved)).thenReturn(responseDto);

        // WHEN + THEN
        mockMvc.perform(post("/orders/from-quote/5"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.quoteId").value(5));

        // VERIFY
        verify(orderService).createFromQuote(5L);
        verify(orderMapper).toDto(saved);
    }

    // =====================================
    // 7. UPDATE
    // =====================================

    @Test
    void givenValidOrder_whenUpdate_thenReturn200() throws Exception {

        // GIVEN
        Order updated = createOrder(9L);
        OrderDto responseDto = createOrderDto(9L);

        when(orderService.update(eq(9L), any(OrderDto.class))).thenReturn(updated);
        when(orderMapper.toDto(updated)).thenReturn(responseDto);

        // WHEN + THEN
        mockMvc.perform(put("/orders/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validOrderJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9));

        verify(orderService).update(eq(9L), argThat(dto ->
                        Long.valueOf(7L).equals(dto.getClientId())));

        verify(orderMapper).toDto(updated);
    }

    // =====================================
    // 8. CONFIRM
    // =====================================

    @Test
    void givenOrderId_whenConfirm_thenReturn200() throws Exception {

        // GIVEN
        Order confirmed = createOrder(9L);
        OrderDto responseDto = createOrderDto(9L);
        responseDto.setStatus(OrderStatus.CONFIRMED);

        when(orderService.confirm(9L)).thenReturn(confirmed);
        when(orderMapper.toDto(confirmed)).thenReturn(responseDto);

        // WHEN + THEN
        mockMvc.perform(patch("/orders/9/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.status")
                        .value("CONFIRMED"));

        verify(orderService).confirm(9L);
        verify(orderMapper).toDto(confirmed);
    }

    // =====================================
    // 9. CANCEL
    // =====================================

    @Test
    void givenOrderId_whenCancel_thenReturn200() throws Exception {

        // GIVEN
        Order cancelled = createOrder(9L);
        OrderDto responseDto = createOrderDto(9L);
        responseDto.setStatus(OrderStatus.CANCELLED);

        when(orderService.cancel(9L)).thenReturn(cancelled);
        when(orderMapper.toDto(cancelled)).thenReturn(responseDto);

        // WHEN + THEN
        mockMvc.perform(patch("/orders/9/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.status")
                        .value("CANCELLED"));

        verify(orderService).cancel(9L);
        verify(orderMapper).toDto(cancelled);
    }

    // =====================================
    // 10. DELETE
    // =====================================

    @Test
    void givenOrderId_whenDelete_thenReturn204() throws Exception {

        // WHEN + THEN
        mockMvc.perform(delete("/orders/9"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // VERIFY
        verify(orderService).delete(9L);
        verifyNoInteractions(orderMapper);
    }

    // =====================================
    // 11. MISSING CLIENT
    // =====================================

    @Test
    void givenMissingClientId_whenCreate_thenReturn400() throws Exception {

        String json = """
                {
                    "notes": "Pedido sin cliente"
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)).andExpect(status().isBadRequest());

        verifyNoInteractions(orderService, orderMapper);
    }

    // =====================================
    // 12. INVALID CLIENT
    // =====================================

    @Test
    void givenNegativeClientId_whenCreate_thenReturn400() throws Exception {

        String json = """
                {
                    "clientId": -5,
                    "notes": "Cliente inválido"
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)).andExpect(status().isBadRequest());

        verifyNoInteractions(orderService, orderMapper);
    }

    // =====================================
    // 13. NEGATIVE DISCOUNT
    // =====================================

    @Test
    void givenNegativeDiscount_whenUpdate_thenReturn400() throws Exception {

        String json = """
                {
                    "clientId": 7,
                    "discount": -10.00
                }
                """;

        mockMvc.perform(put("/orders/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)).andExpect(status().isBadRequest());

        verifyNoInteractions(orderService, orderMapper);
    }

    // =====================================
    // 14. NON-NUMERIC ID
    // =====================================

    @Test
    void givenNonNumericId_whenFindById_thenReturn400() throws Exception {

        mockMvc.perform(get("/orders/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService, orderMapper);
    }
}
