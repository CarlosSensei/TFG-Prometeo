package com.optiflow.crm.order.model;

import com.optiflow.crm.order.dto.OrderDto;
import com.optiflow.crm.order.dto.OrderSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Interfaz de servicio de pedidos
public interface OrderService {

    // Busqueda paginada de pedidos
    Page<Order> findAll(Pageable pageable);

    // Busqueda avanzada
    Page<Order> search(OrderSearchDto searchDto);

    // Buscar por Id de cliente
    Page<Order> findByClientId(Long clientId, Pageable pageable);

    // Buscar por Id de pedido
    Order findById(Long id);

    OrderDto findDetailById(Long id);

    // Crear un nuevo pedido
    Order create(OrderDto orderDto);

    // Crear un pedido desde un presupuesto
    Order createFromQuote(Long quoteId);

    // Actualizar un pedido
    Order update(Long id, OrderDto orderDto);

    // Confirmar un pedido
    Order confirm(Long id);

    OrderDto confirmDetail(Long id);

    // Preparar un pedido
    OrderDto startPreparationDetail(Long id);

    OrderDto markReadyDetail(Long id);

    OrderDto deliverDetail(Long id);

    // Cancelar un pedido
    Order cancel(Long id);

    OrderDto cancelDetail(Long id);

    // Borrar un pedido
    void delete(Long id);



}