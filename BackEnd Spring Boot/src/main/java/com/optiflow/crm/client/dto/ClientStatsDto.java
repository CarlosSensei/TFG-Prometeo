package com.optiflow.crm.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Estadisticas del cliente para dashboard
public class ClientStatsDto {

    // Número total de presupuestos activos asociados al cliente.
    private long quotesCount;

    // Número total de pedidos activos asociados al cliente.
    private long ordersCount;

    // Número de presupuestos que han acabado convertidos en pedido.
    private long convertedQuotesCount;

    // Importe total gastado por el cliente.
    private BigDecimal totalSpent;

    // Importe medio de los pedidos entregados.
    private BigDecimal averageTicket;

    // Porcentaje de presupuestos que terminan convertidos en pedido.
    private BigDecimal conversionRate;

    // Fecha del último pedido realizado por el cliente.
    private LocalDate lastOrderDate;

    // Fecha y hora de la última actividad comercial conocida.
    private LocalDateTime lastActivity;

    // Número de seguimientos pendientes.
    private long pendingFollowUps;

    // Número de pedidos actualmente pendientes de finalizar.
    private long pendingOrdersCount;

    // Número de presupuestos que siguen abiertos comercialmente.
    private long openQuotesCount;

    // Constructor vacio
    public ClientStatsDto() {
    }

    // Getters y Setters

    public long getQuotesCount() {
        return quotesCount;
    }

    public void setQuotesCount(long quotesCount) {
        this.quotesCount = quotesCount;
    }

    public long getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(long ordersCount) {
        this.ordersCount = ordersCount;
    }

    public long getConvertedQuotesCount() {
        return convertedQuotesCount;
    }

    public void setConvertedQuotesCount(long convertedQuotesCount) {
        this.convertedQuotesCount = convertedQuotesCount;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public BigDecimal getAverageTicket() {
        return averageTicket;
    }

    public void setAverageTicket(BigDecimal averageTicket) {
        this.averageTicket = averageTicket;
    }

    public BigDecimal getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(BigDecimal conversionRate) {
        this.conversionRate = conversionRate;
    }

    public LocalDate getLastOrderDate() {
        return lastOrderDate;
    }

    public void setLastOrderDate(LocalDate lastOrderDate) {
        this.lastOrderDate = lastOrderDate;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public long getPendingFollowUps() {
        return pendingFollowUps;
    }

    public void setPendingFollowUps(long pendingFollowUps) {
        this.pendingFollowUps = pendingFollowUps;
    }

    public long getPendingOrdersCount() {
        return pendingOrdersCount;
    }

    public void setPendingOrdersCount(long pendingOrdersCount) {
        this.pendingOrdersCount = pendingOrdersCount;
    }

    public long getOpenQuotesCount() {
        return openQuotesCount;
    }

    public void setOpenQuotesCount(long openQuotesCount) {
        this.openQuotesCount = openQuotesCount;
    }
}