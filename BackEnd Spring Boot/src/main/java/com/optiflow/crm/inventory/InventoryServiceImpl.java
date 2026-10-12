package com.optiflow.crm.inventory;

import com.optiflow.crm.inventory.dto.InventoryItemDto;
import com.optiflow.crm.inventory.dto.InventorySearchDto;
import com.optiflow.crm.inventory.mapper.InventoryItemMapper;
import com.optiflow.crm.inventory.model.InventoryItem;
import com.optiflow.crm.inventory.model.InventoryItemType;
import com.optiflow.crm.inventory.model.InventoryService;
import com.optiflow.crm.inventory.specification.InventorySpecification;
import com.optiflow.crm.shared.exception.InsufficientStockException;
import com.optiflow.crm.shared.exception.InventoryItemAlreadyExistsException;
import com.optiflow.crm.shared.exception.InventoryItemNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// IMplementacion del Inventory Service
@Service
public class InventoryServiceImpl implements InventoryService {

    // Inyeccion de repository y mapper
    private final InventoryRepository inventoryRepository;
    private final InventoryItemMapper inventoryItemMapper;

    public InventoryServiceImpl(
            InventoryRepository inventoryRepository,
            InventoryItemMapper inventoryItemMapper) {

        this.inventoryRepository = inventoryRepository;
        this.inventoryItemMapper = inventoryItemMapper;
    }

    // Busqueda paginada de inventory items
    @Override
    public Page<InventoryItem> findAll(Pageable pageable) {

        return inventoryRepository.findByActiveTrue(pageable);
    }

    // Busqueda personalizada de inventory items
    @Override
    public Page<InventoryItem> search(InventorySearchDto searchDto) {

        Specification<InventoryItem> specification = InventorySpecification.filterBy(searchDto);

        int page = searchDto.getPage() != null ? searchDto.getPage() : 0;

        int size = searchDto.getSize() != null ? searchDto.getSize() : 10;

        String sortBy = searchDto.getSortBy() != null ? searchDto.getSortBy() : "name";

        Sort.Direction direction = "desc".equalsIgnoreCase(searchDto.getSortDirection())
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return inventoryRepository.findAll(specification, pageable);
    }

    // Buscar items por id
    @Override
    public InventoryItem findById(Long id) {

        return inventoryRepository.findById(id).orElseThrow(() ->
                new InventoryItemNotFoundException(id));
    }

    // crear inventory items
    @Override
    @Transactional
    public InventoryItem create(InventoryItemDto dto) {

        if (inventoryRepository.existsBySkuIgnoreCase(dto.getSku())) {

            throw new InventoryItemAlreadyExistsException(dto.getSku());
        }

        InventoryItem item = inventoryItemMapper.toEntity(dto);

        item.setActive(true);

        return inventoryRepository.save(item);
    }

    // Actualizar inventory items
    @Override
    @Transactional
    public InventoryItem update(Long id, InventoryItemDto dto) {

        InventoryItem item = findById(id);

        if (!item.getSku().equalsIgnoreCase(dto.getSku())) {

            if (inventoryRepository.existsBySkuIgnoreCase(dto.getSku())) {

                throw new InventoryItemAlreadyExistsException(dto.getSku());
            }
        }

        inventoryItemMapper.updateEntity(dto, item);

        return inventoryRepository.save(item);
    }

    // Borrar items desactivándolos
    @Override
    @Transactional
    public void delete(Long id) {

        InventoryItem item = findById(id);

        item.setActive(false);

        inventoryRepository.save(item);
    }

    // Bajar el stock
    @Override
    @Transactional
    public void decreaseStock(Long inventoryItemId, Integer quantity) {

        InventoryItem item = findById(inventoryItemId);

        if (item.getType() == InventoryItemType.SERVICE) {
            return;
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }

        if (item.getStock() < quantity) {
            throw new InsufficientStockException(item.getId(), item.getStock(), quantity);
        }

        item.setStock(item.getStock() - quantity);

        inventoryRepository.save(item);
    }

    // Subir al stock
    @Override
    @Transactional
    public void increaseStock(Long inventoryItemId, Integer quantity) {

        InventoryItem item = findById(inventoryItemId);

        if (item.getType() == InventoryItemType.SERVICE) {
            return;
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }

        item.setStock(item.getStock() + quantity);

        inventoryRepository.save(item);
    }


}