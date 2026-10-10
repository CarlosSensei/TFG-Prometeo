package com.optiflow.crm.client.model;

import com.optiflow.crm.client.dto.ClientSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// interfaz de la capa de servicio del cliente
public interface ClientService {

    // Busqueda paginada de clientes
    Page<Client> findAll(Pageable pageable);

    // Busqueda personalizada de clientes
    Page<Client> search(ClientSearchDto searchDto);

    // Buscar por Id
    Client findById(Long id);

    // Guardar un cliente
    Client save(Client client);

    // Actualizar un cliente
    Client update(Long id, Client client);

    // Borrar un cliente
    void delete(Long id);
}
