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

@Service
public class FollowUpServiceImpl implements FollowUpService {

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

    @Override
    public Page<FollowUp> findAll(Pageable pageable) {

        return followUpRepository.findByActiveTrue(pageable);
    }

    @Override
    public Page<FollowUp> search(FollowUpSearchDto searchDto) {

        Specification<FollowUp> specification =
                FollowUpSpecification.filterBy(searchDto);

        int page = searchDto.getPage() != null
                ? searchDto.getPage() : 0;

        int size = searchDto.getSize() != null
                ? searchDto.getSize() : 10;

        String sortBy = searchDto.getSortBy() != null
                ? searchDto.getSortBy() : "scheduledAt";

        Sort.Direction direction =
                "desc".equalsIgnoreCase(searchDto.getSortDirection())
                        ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return followUpRepository.findAll(specification, pageable);
    }

    @Override
    public Page<FollowUp> findByQuoteId(Long quoteId, Pageable pageable) {

        return followUpRepository
                .findByQuoteIdAndActiveTrue(quoteId, pageable);
    }

    @Override
    public FollowUp findById(Long id) {

        return followUpRepository
                .findById(id)
                .orElseThrow(() ->
                        new FollowUpNotFoundException(id)
                );
    }

    @Override
    @Transactional
    public FollowUp create(Long quoteId, FollowUpDto followUpDto) {

        Quote quote = quoteRepository
                .findById(quoteId)
                .orElseThrow(() ->
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

    @Override
    @Transactional
    public FollowUp update(Long id, FollowUpDto followUpDto) {

        FollowUp existingFollowUp = findById(id);

        followUpMapper.updateEntity(followUpDto, existingFollowUp);

        return followUpRepository.save(existingFollowUp);
    }

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

    @Override
    @Transactional
    public void delete(Long id) {

        FollowUp followUp = findById(id);

        followUp.setActive(false);

        followUpRepository.save(followUp);
    }
}