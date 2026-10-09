package com.optiflow.crm.order.model;

import com.optiflow.crm.order.dto.OrderDto;
import com.optiflow.crm.order.dto.OrderSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    Page<Order> findAll(Pageable pageable);

    Page<Order> search(OrderSearchDto searchDto);

    Page<Order> findByClientId(Long clientId, Pageable pageable);

    Order findById(Long id);

    Order create(OrderDto orderDto);

    Order createFromQuote(Long quoteId);

    Order update(Long id, OrderDto orderDto);

    void delete(Long id);
}