package com.optiflow.crm.client.mapper;

import com.optiflow.crm.client.dto.ClientDto;
import com.optiflow.crm.client.model.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public ClientDto toDto(Client client) {

        ClientDto dto = new ClientDto();

        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setFirstSurname(client.getFirstSurname());
        dto.setFirstSurname(client.getFirstSurname());
        dto.setLastSurname(client.getLastSurname());
        dto.setDNI(client.getDNI());
        dto.setPhoneNumber(client.getPhoneNumber());
        dto.setEmail(client.getEmail());
        dto.setBirthDate(client.getBirthDate());
        dto.setAddress(client.getAddress());
        dto.setCity(client.getCity());
        dto.setPostalCode(client.getPostalCode());
        dto.setActive(client.getActive());
        dto.setCreatedAt(client.getCreatedAt());
        dto.setUpdatedAt(client.getUpdatedAt());

        return dto;
    }

    public Client toEntity(ClientDto dto) {

        Client client = new Client();

        client.setName(dto.getName());
        client.setFirstSurname(dto.getFirstSurname());
        client.setFirstSurname(dto.getFirstSurname());
        client.setLastSurname(dto.getLastSurname());
        client.setDNI(dto.getDNI());
        client.setPhoneNumber(dto.getPhoneNumber());
        client.setEmail(dto.getEmail());
        client.setBirthDate(dto.getBirthDate());
        client.setAddress(dto.getAddress());
        client.setCity(dto.getCity());
        client.setPostalCode(dto.getPostalCode());

        return client;
    }
}