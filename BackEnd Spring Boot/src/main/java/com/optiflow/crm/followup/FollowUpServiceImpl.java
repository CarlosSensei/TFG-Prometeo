package com.optiflow.crm.followup;

import com.optiflow.crm.followup.dto.CompleteFollowUpDto;
import com.optiflow.crm.followup.dto.FollowUpDto;
import com.optiflow.crm.followup.dto.FollowUpSearchDto;
import com.optiflow.crm.followup.mapper.FollowUpMapper;
import com.optiflow.crm.followup.model.FollowUp;
import com.optiflow.crm.followup.model.FollowUpService;
import com.optiflow.crm.followup.model.FollowUpStatus;
import com.optiflow.crm.followup.specification.FollowUpSpecification;

import com.optiflow.crm.quote.QuoteRepository;
import com.optiflow.crm.quote.model.Quote;

import com.optiflow.crm.shared.exception.FollowUpNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// Implementacion del Service para seguimientos
@Service
public class FollowUpServiceImpl implements FollowUpService {

    // Inyeccion de los repositories y el mapper
    private final FollowUpRepository followUpRepository;
    private final QuoteRepository quoteRepository;
    private final FollowUpMapper followUpMapper;

    public FollowUpServiceImpl(
            FollowUpRepository followUpRepository,
            QuoteRepository quoteRepository,
            FollowUpMapper followUpMapper
    ) {
        this.followUpRepository = followUpRepository;
        this.quoteRepository = quoteRepository;
        this.followUpMapper = followUpMapper;
    }

    // Listado paginado de seguimientos
    @Override
    public Page<FollowUp> findAll(Pageable pageable) {

        return followUpRepository.findByActiveTrue(pageable);
    }

    // Busqueda avanzada de seguimientos
    @Override
    public Page<FollowUp> search(FollowUpSearchDto searchDto) {

        Specification<FollowUp> specification = FollowUpSpecification.filterBy(searchDto);

        int page = searchDto.getPage() != null ? searchDto.getPage() : 0;

        int size = searchDto.getSize() != null ? searchDto.getSize() : 10;

        String sortBy = searchDto.getSortBy() != null
                ? searchDto.getSortBy() : "scheduledAt";

        Sort.Direction direction = "desc".equalsIgnoreCase(searchDto.getSortDirection())
                        ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return followUpRepository.findAll(specification, pageable);
    }

    // Busqueda por id de presupuesto
    @Override
    public Page<FollowUp> findByQuoteId(Long quoteId, Pageable pageable) {

        return followUpRepository.findByQuoteIdAndActiveTrue(quoteId, pageable);
    }

    // Busqueda por id de seguimiento
    @Override
    public FollowUp findById(Long id) {

        return followUpRepository.findById(id).orElseThrow(() ->
                        new FollowUpNotFoundException(id));
    }

    // Metodo de creacion de seguimiento
    @Override
    @Transactional
    public FollowUp create(Long quoteId, FollowUpDto followUpDto) {

        Quote quote = quoteRepository.findById(quoteId).orElseThrow(() ->
                        new FollowUpNotFoundException(quoteId));

        FollowUp followUp = followUpMapper.toEntity(followUpDto);

        followUp.setQuote(quote);

        if (followUp.getStatus() == null) {

            followUp.setStatus(FollowUpStatus.PENDING);
        }

        if (followUp.getAutomated() == null) {

            followUp.setAutomated(false);
        }

        followUp.setActive(true);

        return followUpRepository.save(followUp);
    }

    // Actualizacion de seguimientos
    @Override
    @Transactional
    public FollowUp update(Long id, FollowUpDto followUpDto) {

        FollowUp existingFollowUp = findById(id);

        followUpMapper.updateEntity(followUpDto, existingFollowUp);

        return followUpRepository.save(existingFollowUp);
    }

    // Metodo para completar un seguimiento
    @Override
    @Transactional
    public FollowUp complete(Long id, CompleteFollowUpDto dto) {

        FollowUp followUp = findById(id);

        followUp.setStatus(FollowUpStatus.COMPLETED);

        followUp.setCompletedAt(LocalDateTime.now());

        followUp.setResult(dto.getResult());

        if (dto.getNotes() != null) {

            followUp.setNotes(dto.getNotes());
        }

        return followUpRepository.save(followUp);
    }

    // Borrar un seguimiento solo desactivandolo
    @Override
    @Transactional
    public void delete(Long id) {

        FollowUp followUp = findById(id);

        followUp.setActive(false);

        followUpRepository.save(followUp);
    }
}