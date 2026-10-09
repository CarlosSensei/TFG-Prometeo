package com.optiflow.crm.followup;

import com.optiflow.crm.followup.dto.CompleteFollowUpDto;
import com.optiflow.crm.followup.dto.FollowUpDto;
import com.optiflow.crm.followup.dto.FollowUpSearchDto;
import com.optiflow.crm.followup.mapper.FollowUpMapper;
import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.followup.model.FollowUpService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class FollowUpController {

    private final FollowUpService followUpService;
    private final FollowUpMapper followUpMapper;

    public FollowUpController(
            FollowUpService followUpService,
            FollowUpMapper followUpMapper
    ) {
        this.followUpService = followUpService;
        this.followUpMapper = followUpMapper;
    }

    // Listado paginado de seguimientos
    @GetMapping("/followups")
    public ResponseEntity<Page<FollowUpDto>> findAll(Pageable pageable) {

        Page<FollowUpDto> result = followUpService
                .findAll(pageable).map(followUpMapper::toDto);

        return ResponseEntity.ok(result);
    }

    // Búsqueda avanzada
    @PostMapping("/followups/search")
    public ResponseEntity<Page<FollowUpDto>> search(
            @Valid @RequestBody FollowUpSearchDto searchDto) {

        Page<FollowUpDto> result = followUpService
                .search(searchDto).map(followUpMapper::toDto);

        return ResponseEntity.ok(result);
    }

    // Buscar seguimiento por ID
    @GetMapping("/followups/{id}")
    public ResponseEntity<FollowUpDto> findById(@PathVariable Long id) {

        FollowUp followUp = followUpService.findById(id);

        return ResponseEntity.ok(followUpMapper.toDto(followUp));
    }

    // Seguimientos de un presupuesto
    @GetMapping("/quotes/{quoteId}/followups")
    public ResponseEntity<Page<FollowUpDto>> findByQuote(
            @PathVariable Long quoteId, Pageable pageable) {

        Page<FollowUpDto> result = followUpService
                .findByQuoteId(quoteId, pageable).map(followUpMapper::toDto);

        return ResponseEntity.ok(result);
    }

    // Crear seguimiento para un presupuesto
    @PostMapping("/quotes/{quoteId}/followups")
    public ResponseEntity<FollowUpDto> create(
            @PathVariable Long quoteId, @Valid @RequestBody FollowUpDto followUpDto) {

        FollowUp savedFollowUp = followUpService.create(quoteId, followUpDto);

        return ResponseEntity
                .status(HttpStatus.CREATED).body(followUpMapper.toDto(savedFollowUp));
    }

    // Actualizar seguimiento
    @PutMapping("/followups/{id}")
    public ResponseEntity<FollowUpDto> update(
            @PathVariable Long id, @Valid @RequestBody FollowUpDto followUpDto) {

        FollowUp updatedFollowUp = followUpService.update(id, followUpDto);

        return ResponseEntity.ok(followUpMapper.toDto(updatedFollowUp));
    }

    // Completar seguimiento
    @PatchMapping("/followups/{id}/complete")
    public ResponseEntity<FollowUpDto> complete(
            @PathVariable Long id, @Valid @RequestBody CompleteFollowUpDto dto) {

        FollowUp completedFollowUp = followUpService.complete(id, dto);

        return ResponseEntity.ok(followUpMapper.toDto(completedFollowUp));
    }

    // Baja lógica
    @DeleteMapping("/followups/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        followUpService.delete(id);

        return ResponseEntity.noContent().build();
    }
}