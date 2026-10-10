package com.optiflow.crm.client.dto;

import com.optiflow.crm.client.model.ClientCommercialStatus;

import java.time.LocalDateTime;

// Sumario de datos de cliente
public class ClientSummaryDto {

    private ClientDto client;

    private ClientStatsDto stats;

    private LocalDateTime lastActivity;

    private ClientCommercialStatus commercialStatus;
}