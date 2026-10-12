package com.optiflow.crm.quote.model;

import com.optiflow.crm.quote.dto.QuoteDto;
import com.optiflow.crm.quote.dto.QuoteSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Interface de servicio de presupuestos
public interface QuoteService {

    // Buscar listado paginado de presupuestos
    Page<Quote> findAll(Pageable pageable);

    // Busqueda personalizada de presupuestos
    Page<Quote> search(QuoteSearchDto searchDto);

    // Buscar pot Id
    Quote findById(Long id);

    // Buscar por cliente
    Page<Quote> findByClientId(Long clientId, Pageable pageable);

    // Crear un presupuesto
    Quote create(QuoteDto quoteDto);

    // Actualizar un presupuesto
    Quote update(Long id, QuoteDto quoteDto);

    // Borrar un presupuesto
    void delete(Long id);
}