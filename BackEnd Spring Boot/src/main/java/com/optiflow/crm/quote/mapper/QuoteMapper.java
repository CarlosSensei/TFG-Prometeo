package com.optiflow.crm.quote.mapper;

import com.optiflow.crm.quote.dto.QuoteDto;
import com.optiflow.crm.quote.model.Quote;

import org.springframework.stereotype.Component;

@Component
public class QuoteMapper {

    // Metodos de mapeo de Quote y QuoteDto
    public QuoteDto toDto(Quote quote) {

        QuoteDto dto = new QuoteDto();

        dto.setId(quote.getId());
        dto.setQuoteNumber(quote.getQuoteNumber());

        if (quote.getClient() != null) {
            dto.setClientId(quote.getClient().getId());
        }

        dto.setStatus(quote.getStatus());
        dto.setQuoteDate(quote.getQuoteDate());
        dto.setExpirationDate(quote.getExpirationDate());

        dto.setSubtotal(quote.getSubtotal());
        dto.setDiscount(quote.getDiscount());
        dto.setTaxRate(quote.getTaxRate());
        dto.setTaxAmount(quote.getTaxAmount());
        dto.setTotal(quote.getTotal());

        dto.setNotes(quote.getNotes());
        dto.setActive(quote.getActive());

        dto.setCreatedAt(quote.getCreatedAt());
        dto.setUpdatedAt(quote.getUpdatedAt());

        return dto;
    }

    public Quote toEntity(QuoteDto dto) {

        Quote quote = new Quote();

        quote.setStatus(dto.getStatus());
        quote.setQuoteDate(dto.getQuoteDate());
        quote.setExpirationDate(dto.getExpirationDate());

        quote.setSubtotal(dto.getSubtotal());
        quote.setDiscount(dto.getDiscount());
        quote.setTaxRate(dto.getTaxRate());

        quote.setNotes(dto.getNotes());

        return quote;
    }

    public void updateEntity(QuoteDto dto, Quote quote) {

        quote.setStatus(dto.getStatus());
        quote.setQuoteDate(dto.getQuoteDate());
        quote.setExpirationDate(dto.getExpirationDate());

        quote.setSubtotal(dto.getSubtotal());
        quote.setDiscount(dto.getDiscount());
        quote.setTaxRate(dto.getTaxRate());

        quote.setNotes(dto.getNotes());
    }
}