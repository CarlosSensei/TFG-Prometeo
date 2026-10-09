package com.optiflow.crm.client;

import com.optiflow.crm.client.dto.ClientSearchDto;
import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.client.model.ClientService;
import com.optiflow.crm.client.specification.ClientSpecification;
import com.optiflow.crm.shared.exception.ClientNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Override
    public Page<Client> findAll(Pageable pageable) {
        return clientRepository.findByActiveTrue(pageable);
    }

    @Override
    public Page<Client> search(ClientSearchDto searchDto) {

        Specification<Client> specification = ClientSpecification.filterBy(searchDto);

        int page = searchDto.getPage() != null
                ? searchDto.getPage() : 0;

        int size = searchDto.getSize() != null
                ? searchDto.getSize() : 10;

        String sortBy = searchDto.getSortBy() != null
                ? searchDto.getSortBy() : "firstSurname";

        Sort.Direction direction =
                "desc".equalsIgnoreCase(searchDto.getSortDirection())
                        ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return clientRepository.findAll(specification, pageable);
    }

    @Override
    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
    }

    @Override
    public Client save(Client client) {
        return clientRepository.save(client);
    }

    @Override
    public Client update(Long id, Client client) {

        Client existingClient = findById(id);

        existingClient.setName(client.getName());
        existingClient.setFirstSurname(client.getFirstSurname());
        existingClient.setLastSurname(client.getLastSurname());
        existingClient.setDNI(client.getDNI());
        existingClient.setPhoneNumber(client.getPhoneNumber());
        existingClient.setEmail(client.getEmail());
        existingClient.setBirthDate(client.getBirthDate());
        existingClient.setAddress(client.getAddress());
        existingClient.setCity(client.getCity());
        existingClient.setPostalCode(client.getPostalCode());
        existingClient.setNotes(client.getNotes());

        return clientRepository.save(existingClient);
    }

    @Override
    public void delete(Long id) {

        Client client = findById(id);

        client.setActive(false);

        clientRepository.save(client);
    }
}
