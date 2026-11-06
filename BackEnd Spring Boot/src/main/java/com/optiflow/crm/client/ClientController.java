package com.optiflow.crm.client;

import com.optiflow.crm.client.dto.ClientDto;
import com.optiflow.crm.client.dto.ClientSearchDto;
import com.optiflow.crm.client.mapper.ClientMapper;
import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.client.dto.ClientStatsDto;
import com.optiflow.crm.client.model.ClientService;
import com.optiflow.crm.client.dto.ClientTimelineEventDto;
import java.util.List;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller de cliente
@RestController
@RequestMapping("/clients")
public class ClientController {

    // Inyección de Service y Mapper
    private final ClientService clientService;
    private final ClientMapper clientMapper;

    public ClientController(ClientService clientService, ClientMapper clientMapper) {
        this.clientService = clientService;
        this.clientMapper = clientMapper;
    }

    // Listado paginado de clientes
    @GetMapping
    public ResponseEntity<Page<ClientDto>> findAll(Pageable pageable) {

        Page<ClientDto> result = clientService.findAll(pageable).map(clientMapper::toDto);

        return ResponseEntity.ok(result);
    }

    // Búsqueda avanzada de clientes
    @PostMapping("/search")
    public ResponseEntity<Page<ClientDto>> search(@Valid @RequestBody ClientSearchDto searchDto) {

        Page<ClientDto> result = clientService.search(searchDto).map(clientMapper::toDto);

        return ResponseEntity.ok(result);
    }

    // Buscar cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> findById(@PathVariable Long id) {

        Client client = clientService.findById(id);

        return ResponseEntity.ok(clientMapper.toDto(client));
    }

    // Crear cliente
    @PostMapping
    public ResponseEntity<ClientDto> create(
            @Valid @RequestBody ClientDto clientDto
    ) {

        Client savedClient = clientService.create(clientDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clientMapper.toDto(savedClient));
    }

    // Actualizar cliente
    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> update(@PathVariable Long id, @Valid @RequestBody ClientDto clientDto) {

        Client client = clientMapper.toEntity(clientDto);

        Client updatedClient = clientService.update(id, client);

        ClientDto response = clientMapper.toDto(updatedClient);

        return ResponseEntity.ok(response);
    }

    // Baja lógica del cliente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        clientService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<ClientTimelineEventDto>> getTimeline(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(clientService.getTimeline(id));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ClientStatsDto> getStats(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(clientService.getStats(id));
    }
}