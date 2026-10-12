package com.optiflow.crm.quote.dto;

import com.optiflow.crm.quote.model.QuoteStatus;

import java.time.LocalDate;

// Entidad dto de busqueda de inventario
public class QuoteSearchDto {

    private String quoteNumber;

    private Long clientId;

    private Long sellerId;

    private String sellerName;

    private QuoteStatus status;

    private LocalDate quoteDateFrom;

    private LocalDate quoteDateTo;

    private LocalDate expirationDateFrom;

    private LocalDate expirationDateTo;

    private Boolean active = true;

    private Integer page = 0;

    private Integer size = 10;

    private String sortBy = "quoteDate";

    private String sortDirection = "desc";

    // Getters y Setters

    public String getQuoteNumber() {
        return quoteNumber;
    }

    public void setQuoteNumber(String quoteNumber) {
        this.quoteNumber = quoteNumber;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public QuoteStatus getStatus() {
        return status;
    }

    public void setStatus(QuoteStatus status) {
        this.status = status;
    }

    public LocalDate getQuoteDateFrom() {
        return quoteDateFrom;
    }

    public void setQuoteDateFrom(LocalDate quoteDateFrom) {
        this.quoteDateFrom = quoteDateFrom;
    }

    public LocalDate getQuoteDateTo() {
        return quoteDateTo;
    }

    public void setQuoteDateTo(LocalDate quoteDateTo) {
        this.quoteDateTo = quoteDateTo;
    }

    public LocalDate getExpirationDateFrom() {
        return expirationDateFrom;
    }

    public void setExpirationDateFrom(LocalDate expirationDateFrom) {
        this.expirationDateFrom = expirationDateFrom;
    }

    public LocalDate getExpirationDateTo() {
        return expirationDateTo;
    }

    public void setExpirationDateTo(LocalDate expirationDateTo) {
        this.expirationDateTo = expirationDateTo;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}