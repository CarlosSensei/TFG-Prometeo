package com.optiflow.crm.order;

import com.optiflow.crm.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Page<Order> findByActiveTrue(Pageable pageable);

    Page<Order> findByClientIdAndActiveTrue(Long clientId, Pageable pageable);

    boolean existsByQuoteId(Long quoteId);

    Optional<Order> findTopByOrderNumberStartingWithOrderByOrderNumberDesc(String prefix);

    Long countByClientIdAndActiveTrue(Long clientId);

}