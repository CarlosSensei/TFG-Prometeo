package com.optiflow.crm.client;

import com.optiflow.crm.client.dto.ClientDto;
import com.optiflow.crm.client.dto.ClientSearchDto;
import com.optiflow.crm.client.mapper.ClientMapper;
import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.order.OrderRepository;
import com.optiflow.crm.quote.QuoteRepository;
import com.optiflow.crm.shared.exception.ClientAlreadyExistsException;
import com.optiflow.crm.shared.exception.ClientNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock private ClientRepository clientRepository;
    @Mock private ClientMapper clientMapper;
    @Mock private QuoteRepository quoteRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks private ClientServiceImpl service;

    @Test
    void givenActiveClients_whenFindAll_thenReturnRepositoryPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Client client = client(1L, "12345678Z", true);
        Page<Client> expected = new PageImpl<>(List.of(client), pageable, 1);
        when(clientRepository.findByActiveTrue(pageable)).thenReturn(expected);

        Page<Client> result = service.findAll(pageable);

        assertSame(expected, result);
        assertEquals(1, result.getTotalElements());
        verify(clientRepository).findByActiveTrue(pageable);
        verify(clientRepository, never()).findAll(pageable);
    }

    @Test
    void givenNoActiveClients_whenFindAll_thenReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(clientRepository.findByActiveTrue(pageable)).thenReturn(Page.empty(pageable));

        Page<Client> result = service.findAll(pageable);

        assertTrue(result.isEmpty());
        verify(clientRepository).findByActiveTrue(pageable);
    }

    @Test
    void givenExistingClient_whenFindById_thenReturnClient() {
        Client client = client(2L, "12345678Z", true);
        when(clientRepository.findById(2L)).thenReturn(Optional.of(client));

        assertSame(client, service.findById(2L));
    }

    @Test
    void givenUnknownId_whenFindById_thenThrowNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void givenNewDni_whenCreate_thenSaveActiveClient() {
        ClientDto dto = clientDto("12345678Z");
        Client mapped = client(null, dto.getDni(), false);
        when(clientRepository.findByDniIgnoreCase(dto.getDni())).thenReturn(Optional.empty());
        when(clientMapper.toEntity(dto)).thenReturn(mapped);
        when(clientRepository.save(mapped)).thenReturn(mapped);

        Client result = service.create(dto);

        assertSame(mapped, result);
        assertTrue(result.getActive());
        verify(clientMapper).toEntity(dto);
        verify(clientRepository).save(mapped);
    }

    @Test
    void givenInactiveExistingDni_whenCreate_thenReactivateSameClient() {
        ClientDto dto = clientDto("12345678Z");
        Client inactive = client(7L, "12345678Z", false);
        when(clientRepository.findByDniIgnoreCase(dto.getDni())).thenReturn(Optional.of(inactive));
        when(clientRepository.save(inactive)).thenReturn(inactive);

        Client result = service.create(dto);

        assertSame(inactive, result);
        assertTrue(result.getActive());
        verify(clientMapper).updateEntity(dto, inactive);
        verify(clientMapper, never()).toEntity(any());
        verify(clientRepository).save(inactive);
    }

    @Test
    void givenActiveExistingDni_whenCreate_thenThrowDuplicateAndDoNotSave() {
        ClientDto dto = clientDto("12345678Z");
        when(clientRepository.findByDniIgnoreCase(dto.getDni()))
                .thenReturn(Optional.of(client(7L, dto.getDni(), true)));

        assertThrows(ClientAlreadyExistsException.class, () -> service.create(dto));

        verify(clientRepository, never()).save(any(Client.class));
        verifyNoInteractions(clientMapper);
    }

    @Test
    void givenClient_whenSave_thenDelegateToRepository() {
        Client client = client(10L, "12345678Z", true);
        when(clientRepository.save(client)).thenReturn(client);

        Client saved = service.save(client);

        assertSame(client, saved);
        verify(clientRepository).save(client);
    }

    @Test
    void givenExistingClient_whenUpdate_thenSaveModifiedEntity() {
        Client existing = client(5L, "12345678Z", true);
        Client changes = new Client();
        changes.setName("Ana");
        changes.setSurname("Soler");
        changes.setDni("12345678Z");
        changes.setEmail("ana@example.com");
        when(clientRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(clientRepository.save(existing)).thenReturn(existing);

        Client result = service.update(5L, changes);

        assertSame(existing, result);
        assertEquals("Ana", existing.getName());
        assertEquals("Soler", existing.getSurname());
        assertEquals("ana@example.com", existing.getEmail());
        verify(clientRepository).save(existing);
    }

    @Test
    void givenUnknownClient_whenUpdate_thenThrowNotFoundAndDoNotSave() {
        when(clientRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class,
                () -> service.update(404L, new Client()));

        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void givenExistingClient_whenDelete_thenSoftDeleteWithoutPhysicalRemoval() {
        Client existing = client(5L, "12345678Z", true);
        when(clientRepository.findById(5L)).thenReturn(Optional.of(existing));

        service.delete(5L);

        assertFalse(existing.getActive());
        verify(clientRepository).save(existing);
        verify(clientRepository, never()).deleteById(anyLong());
        verify(clientRepository, never()).delete(any(Client.class));
    }

    @Test
    void givenMissingClient_whenDelete_thenThrowNotFound() {
        when(clientRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> service.delete(404L));
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void givenSearchCriteria_whenSearch_thenApplyPaginationAndDescendingSort() {
        ClientSearchDto dto = new ClientSearchDto();
        dto.setPage(2);
        dto.setSize(5);
        dto.setSortBy("surname");
        dto.setSortDirection("desc");
        when(clientRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.search(dto);

        ArgumentCaptor<Pageable> pageCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(clientRepository).findAll(any(Specification.class), pageCaptor.capture());
        Pageable page = pageCaptor.getValue();
        assertEquals(2, page.getPageNumber());
        assertEquals(5, page.getPageSize());
        assertEquals(Sort.Direction.DESC, page.getSort().getOrderFor("surname").getDirection());
    }

    @Test
    @SuppressWarnings("unchecked")
    void givenNullSearchPagination_whenSearch_thenUseDefaultPageAndSize() {
        ClientSearchDto dto = new ClientSearchDto();
        dto.setPage(null);
        dto.setSize(null);
        dto.setSortBy("surname"); // El valor por defecto actual del DTO tiene una errata.
        dto.setSortDirection(null);
        when(clientRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.search(dto);

        ArgumentCaptor<Pageable> pageCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(clientRepository).findAll(any(Specification.class), pageCaptor.capture());
        assertEquals(0, pageCaptor.getValue().getPageNumber());
        assertEquals(10, pageCaptor.getValue().getPageSize());
        assertEquals(Sort.Direction.ASC,
                pageCaptor.getValue().getSort().getOrderFor("surname").getDirection());
    }

    private static Client client(Long id, String dni, boolean active) {
        Client client = new Client();
        client.setId(id);
        client.setDni(dni);
        client.setActive(active);
        return client;
    }

    private static ClientDto clientDto(String dni) {
        ClientDto dto = new ClientDto();
        dto.setDni(dni);
        dto.setName("Ana");
        dto.setSurname("Soler");
        return dto;
    }
}
