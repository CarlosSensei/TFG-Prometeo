package com.optiflow.crm.followup.model;

import com.optiflow.crm.followup.dto.CompleteFollowUpDto;
import com.optiflow.crm.followup.dto.FollowUpDto;
import com.optiflow.crm.followup.dto.FollowUpSearchDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Interface del servicio de seguimiento
public interface FollowUpService {

    // Busqueda de seguimientos
    Page<FollowUp> findAll(Pageable pageable);

    // Busqueda avanzada de seguimiento
    Page<FollowUp> search(FollowUpSearchDto searchDto);

    // Busqueda por id de presupuesto
    Page<FollowUp> findByQuoteId(Long quoteId, Pageable pageable);

    // Busqueda de seguimiento por id
    FollowUp findById(Long id);

    // Crear seguimiento
    FollowUp create(Long quoteId, FollowUpDto followUpDto);

    // Actualizar presupuesto
    FollowUp update(Long id, FollowUpDto followUpDto);

    // Completar presupuesto
    FollowUp complete(Long id, CompleteFollowUpDto dto);

    // Borrar presupuesto
    void delete(Long id);
}