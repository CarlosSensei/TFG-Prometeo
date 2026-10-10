package com.optiflow.crm.quote;

import com.optiflow.crm.quote.model.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long>, JpaSpecificationExecutor<Quote> {

    Page<Quote> findByActiveTrue(Pageable pageable);

    Page<Quote> findByClientIdAndActiveTrue(Long clientId, Pageable pageable);

    Optional<Quote> findTopByQuoteNumberStartingWithOrderByQuoteNumberDesc(String prefix);

    long countByClientIdAndActiveTrue(Long clientId);
}
