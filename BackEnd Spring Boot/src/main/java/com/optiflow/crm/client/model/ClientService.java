package com.optiflow.crm.client.model;

import com.optiflow.crm.client.dto.ClientDto;
import com.optiflow.crm.client.dto.ClientSearchDto;
import com.optiflow.crm.client.dto.ClientStatsDto;
import com.optiflow.crm.client.dto.ClientTimelineEventDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

// interfaz de la capa de servicio del cliente
public interface ClientService {

    // Busqueda paginada de clientes
    Page<Client> findAll(Pageable pageable);

    // Busqueda personalizada de clientes
    Page<Client> search(ClientSearchDto searchDto);

    List<ClientTimelineEventDto> getTimeline(Long clientId);

    ClientStatsDto getStats(Long clientId);

    // Buscar por Id
    Client findById(Long id);

    Client create(ClientDto dto);

    // Guardar un cliente
    Client save(Client client);

    // Actualizar un cliente
    Client update(Long id, Client client);

    // Borrar un cliente
    void delete(Long id);
}
