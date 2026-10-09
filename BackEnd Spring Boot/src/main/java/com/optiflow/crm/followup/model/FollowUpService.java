package com.optiflow.crm.followup.model;

import com.optiflow.crm.followup.dto.CompleteFollowUpDto;
import com.optiflow.crm.followup.dto.FollowUpDto;
import com.optiflow.crm.followup.dto.FollowUpSearchDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FollowUpService {

    Page<FollowUp> findAll(Pageable pageable);

    Page<FollowUp> search(FollowUpSearchDto searchDto);

    Page<FollowUp> findByQuoteId(Long quoteId, Pageable pageable);

    FollowUp findById(Long id);

    FollowUp create(Long quoteId, FollowUpDto followUpDto);

    FollowUp update(Long id, FollowUpDto followUpDto);

    FollowUp complete(Long id, CompleteFollowUpDto dto);

    void delete(Long id);
}