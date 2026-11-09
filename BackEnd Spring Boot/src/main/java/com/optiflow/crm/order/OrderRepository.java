package com.optiflow.crm.order;

import com.optiflow.crm.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// Repository de pedidos con consultas
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Page<Order> findByActiveTrue(Pageable pageable);

    Page<Order> findByClientIdAndActiveTrue(Long clientId, Pageable pageable);

    List<Order> findByClientIdAndActiveTrue(Long clientId);

    boolean existsByQuoteId(Long quoteId);

    Optional<Order> findTopByOrderNumberStartingWithOrderByOrderNumberDesc(String prefix);

    Long countByClientIdAndActiveTrue(Long clientId);

    Optional<Order> findByQuoteId(Long quoteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);


}