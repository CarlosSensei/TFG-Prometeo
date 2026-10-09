package com.optiflow.crm.client.model;

import com.optiflow.crm.client.dto.ClientSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {

    Page<Client> findAll(Pageable pageable);

    Page<Client> search(ClientSearchDto searchDto);

    Client findById(Long id);

    Client save(Client client);

    Client update(Long id, Client client);

    void delete(Long id);
}
