package com.optiflow.crm.quote;

import com.optiflow.crm.quote.dto.QuoteDto;
import com.optiflow.crm.quote.dto.QuoteSearchDto;
import com.optiflow.crm.quote.mapper.QuoteMapper;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

// Controller de presupuestos
@RestController
@RequestMapping("/quotes")
public class QuoteController {

    // Inyeccion de service y mapper
    private final QuoteService quoteService;
    private final QuoteMapper quoteMapper;

    public QuoteController(QuoteService quoteService, QuoteMapper quoteMapper) {
        this.quoteService = quoteService;
        this.quoteMapper = quoteMapper;
    }

    // Listado paginado de presupuestos
    @GetMapping
    public ResponseEntity<Page<QuoteDto>> findAll(Pageable pageable) {

        Page<QuoteDto> result = quoteService
                .findAll(pageable)
                .map(quoteMapper::toSummaryDto);

        return ResponseEntity.ok(result);
    }

    // Búsqueda avanzada
    @PostMapping("/search")
    public ResponseEntity<Page<QuoteDto>> search(@Valid @RequestBody QuoteSearchDto searchDto) {

        Page<QuoteDto> result = quoteService
                .search(searchDto)
                .map(quoteMapper::toSummaryDto);

        return ResponseEntity.ok(result);
    }

    // Buscar presupuesto por id
    @GetMapping("/{id}")
    public ResponseEntity<QuoteDto> findById(
            @PathVariable Long id
    ) {

        QuoteDto detail = quoteService.findDetailById(id);

        return ResponseEntity.ok(detail);
    }


    // Crear presupuesto
    @PostMapping
    public ResponseEntity<QuoteDto> create(@Valid @RequestBody QuoteDto quoteDto) {

        Quote savedQuote = quoteService.create(quoteDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(quoteMapper.toDto(savedQuote));
    }

    // Actualizar presupuesto
    @PutMapping("/{id}")
    public ResponseEntity<QuoteDto> update(
            @PathVariable Long id,
            @Valid @RequestBody QuoteDto quoteDto
    ) {

        Quote updatedQuote = quoteService.update(id, quoteDto);

        return ResponseEntity.ok(quoteMapper.toDto(updatedQuote));
    }

    // Borrar un presupuesto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        quoteService.delete(id);

        return ResponseEntity.noContent().build();
    }

    // Buscar presupuestos por cliente
    @GetMapping("/client/{clientId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<QuoteDto>> findByClient(@PathVariable Long clientId, Pageable pageable) {

        Page<QuoteDto> result = quoteService.findByClientId(clientId, pageable).map(quoteMapper::toSummaryDto);

        return ResponseEntity.ok(result);
    }
}