package com.optiflow.crm.quote.model;

import com.optiflow.crm.quote.dto.QuoteDto;
import com.optiflow.crm.quote.dto.QuoteSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuoteService {

    Page<Quote> findAll(Pageable pageable);

    Page<Quote> search(QuoteSearchDto searchDto);

    Quote findById(Long id);

    Page<Quote> findByClientId(Long clientId, Pageable pageable);

    Quote create(QuoteDto quoteDto);

    Quote update(Long id, QuoteDto quoteDto);

    void delete(Long id);
}