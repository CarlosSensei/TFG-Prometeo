package com.optiflow.crm.order.dto;

import com.optiflow.crm.order.model.OrderStatus;

import java.time.LocalDate;

public class OrderSearchDto {

    private String orderNumber;

    private Long clientId;

    private Long sellerId;

    private Long quoteId;

    private OrderStatus status;

    private LocalDate orderDateFrom;

    private LocalDate orderDateTo;

    private LocalDate expectedDeliveryDateFrom;

    private LocalDate expectedDeliveryDateTo;

    private LocalDate deliveryDateFrom;

    private LocalDate deliveryDateTo;

    private Boolean active = true;

    private Integer page = 0;

    private Integer size = 10;

    private String sortBy = "orderDate";

    private String sortDirection = "desc";

    // Getters y Setters

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
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

    public Long getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(Long quoteId) {
        this.quoteId = quoteId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDate getOrderDateFrom() {
        return orderDateFrom;
    }

    public void setOrderDateFrom(LocalDate orderDateFrom) {
        this.orderDateFrom = orderDateFrom;
    }

    public LocalDate getOrderDateTo() {
        return orderDateTo;
    }

    public void setOrderDateTo(LocalDate orderDateTo) {
        this.orderDateTo = orderDateTo;
    }

    public LocalDate getExpectedDeliveryDateFrom() {
        return expectedDeliveryDateFrom;
    }

    public void setExpectedDeliveryDateFrom(LocalDate expectedDeliveryDateFrom) {
        this.expectedDeliveryDateFrom = expectedDeliveryDateFrom;
    }

    public LocalDate getExpectedDeliveryDateTo() {
        return expectedDeliveryDateTo;
    }

    public void setExpectedDeliveryDateTo(LocalDate expectedDeliveryDateTo) {
        this.expectedDeliveryDateTo = expectedDeliveryDateTo;
    }

    public LocalDate getDeliveryDateFrom() {
        return deliveryDateFrom;
    }

    public void setDeliveryDateFrom(LocalDate deliveryDateFrom) {
        this.deliveryDateFrom = deliveryDateFrom;
    }

    public LocalDate getDeliveryDateTo() {
        return deliveryDateTo;
    }

    public void setDeliveryDateTo(LocalDate deliveryDateTo) {
        this.deliveryDateTo = deliveryDateTo;
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