package com.optiflow.crm.client.mapper;

import com.optiflow.crm.client.dto.ClientDto;
import com.optiflow.crm.client.model.Client;
import org.springframework.stereotype.Component;

// Mapper personalizado para cambios entidad - dto
@Component
public class ClientMapper {

    // Mapper de cliente a Dto
    public ClientDto toDto(Client client) {

        ClientDto dto = new ClientDto();

        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setSurname(client.getSurname());
        dto.setLastSurname(client.getLastSurname());
        dto.setDni(client.getDni());
        dto.setPhoneNumber(client.getPhoneNumber());
        dto.setEmail(client.getEmail());
        dto.setBirthDate(client.getBirthDate());
        dto.setAddress(client.getAddress());
        dto.setCity(client.getCity());
        dto.setPostalCode(client.getPostalCode());
        dto.setNotes(client.getNotes());
        dto.setActive(client.getActive());
        dto.setCreatedAt(client.getCreatedAt());
        dto.setUpdatedAt(client.getUpdatedAt());

        return dto;
    }

    // Mapper de Dto a nuevo Cliente
    public Client toEntity(ClientDto dto) {

        Client client = new Client();

        client.setName(dto.getName());
        client.setSurname(dto.getSurname());
        client.setLastSurname(dto.getLastSurname());
        client.setDni(dto.getDni());
        client.setPhoneNumber(dto.getPhoneNumber());
        client.setEmail(dto.getEmail());
        client.setBirthDate(dto.getBirthDate());
        client.setAddress(dto.getAddress());
        client.setCity(dto.getCity());
        client.setPostalCode(dto.getPostalCode());
        client.setNotes(dto.getNotes());

        return client;
    }

    // Mapper para actualizar de dto a cliente existente
    public void updateEntity(ClientDto dto, Client client) {

        client.setName(dto.getName());
        client.setSurname(dto.getSurname());
        client.setLastSurname(dto.getLastSurname());
        client.setDni(dto.getDni());
        client.setPhoneNumber(dto.getPhoneNumber());
        client.setEmail(dto.getEmail());
        client.setBirthDate(dto.getBirthDate());
        client.setAddress(dto.getAddress());
        client.setCity(dto.getCity());
        client.setPostalCode(dto.getPostalCode());
        client.setNotes(dto.getNotes());
    }
}