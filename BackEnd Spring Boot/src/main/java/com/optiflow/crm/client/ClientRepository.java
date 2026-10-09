package com.optiflow.crm.client;

import com.optiflow.crm.client.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClientRepository  extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    Page<Client> findByActiveTrue(Pageable pageable);
}
