package com.optiflow.crm.order;

import com.optiflow.crm.order.dto.OrderDto;
import com.optiflow.crm.order.dto.OrderSearchDto;
import com.optiflow.crm.order.mapper.OrderMapper;
import com.optiflow.crm.order.model.Order;
import com.optiflow.crm.order.model.OrderService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller de pedidos
@RestController
@RequestMapping("/orders")
public class OrderController {

    // Inyeccion de service y mapper
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    // Listado paginado de pedidos
    @GetMapping
    public ResponseEntity<Page<OrderDto>> findAll(Pageable pageable) {

        Page<OrderDto> result = orderService.findAll(pageable)
                .map(orderMapper::toDto);

        return ResponseEntity.ok(result);
    }

    // Búsqueda avanzada
    @PostMapping("/search")
    public ResponseEntity<Page<OrderDto>> search(@Valid @RequestBody OrderSearchDto searchDto) {

        Page<OrderDto> result = orderService.search(searchDto)
                .map(orderMapper::toDto);

        return ResponseEntity.ok(result);
    }

    // Buscar pedido por ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> findById(@PathVariable Long id) {

        Order order = orderService.findById(id);

        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    // Pedidos de un cliente
    @GetMapping("/client/{clientId}")
    public ResponseEntity<Page<OrderDto>> findByClient(@PathVariable Long clientId, Pageable pageable) {

        Page<OrderDto> result = orderService.findByClientId(clientId, pageable)
                .map(orderMapper::toDto);

        return ResponseEntity.ok(result);
    }

    // Crear pedido directamente
    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody OrderDto orderDto) {

        Order savedOrder = orderService.create(orderDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderMapper.toDto(savedOrder));
    }

    // Convertir presupuesto aceptado en pedido
    @PostMapping("/from-quote/{quoteId}")
    public ResponseEntity<OrderDto> createFromQuote(@PathVariable Long quoteId) {

        Order savedOrder = orderService.createFromQuote(quoteId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderMapper.toDto(savedOrder));
    }

    // Actualizar pedido
    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> update(@PathVariable Long id,
            @Valid @RequestBody OrderDto orderDto) {

        Order updatedOrder = orderService.update(id, orderDto);

        return ResponseEntity.ok(orderMapper.toDto(updatedOrder));
    }

    // Confirmar pedido
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<OrderDto> confirm(@PathVariable Long id) {

        Order order = orderService.confirm(id);

        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    // Cancelar pedido
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancel(@PathVariable Long id) {

        Order order = orderService.cancel(id);

        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    // Baja del pedido
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        orderService.delete(id);

        return ResponseEntity.noContent().build();
    }
}