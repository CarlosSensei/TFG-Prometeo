package com.optiflow.crm.quote;

import com.optiflow.crm.auth.CurrentUserService;
import com.optiflow.crm.client.ClientRepository;
import com.optiflow.crm.client.model.Client;
import com.optiflow.crm.quote.domain.QuoteDomainService;
import com.optiflow.crm.quote.dto.QuoteDto;
import com.optiflow.crm.quote.dto.QuoteSearchDto;
import com.optiflow.crm.quote.mapper.QuoteMapper;
import com.optiflow.crm.quote.model.Quote;
import com.optiflow.crm.quote.model.QuoteService;

import com.optiflow.crm.quote.specification.QuoteSpecification;
import com.optiflow.crm.shared.exception.ClientNotFoundException;
import com.optiflow.crm.shared.exception.QuoteNotFoundException;
import com.optiflow.crm.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final ClientRepository clientRepository;
    private final QuoteMapper quoteMapper;
    private final QuoteDomainService quoteDomainService;
    private final CurrentUserService currentUserService;

    public QuoteServiceImpl(
            QuoteRepository quoteRepository,
            ClientRepository clientRepository,
            QuoteMapper quoteMapper,
            QuoteDomainService quoteDomainService,
            CurrentUserService currentUserService
    ) {
        this.quoteRepository = quoteRepository;
        this.clientRepository = clientRepository;
        this.quoteMapper = quoteMapper;
        this.quoteDomainService = quoteDomainService;
        this.currentUserService = currentUserService;
    }

    @Override
    public Page<Quote> findAll(Pageable pageable) {

        return quoteRepository.findByActiveTrue(pageable);
    }

    @Override
    public Quote findById(Long id) {

        return quoteRepository.findById(id).orElseThrow(() ->
                        new QuoteNotFoundException(id));
    }

    @Override
    public Page<Quote> search(QuoteSearchDto searchDto) {

        Specification<Quote> specification = QuoteSpecification.filterBy(searchDto);

        int page = searchDto.getPage() != null
                ? searchDto.getPage() : 0;

        int size = searchDto.getSize() != null
                ? searchDto.getSize() : 10;

        String sortBy = searchDto.getSortBy() != null
                ? searchDto.getSortBy() : "quoteDate";

        Sort.Direction direction =
                "asc".equalsIgnoreCase(searchDto.getSortDirection())
                        ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return quoteRepository.findAll(specification, pageable);
    }

    @Override
    public Page<Quote> findByClientId(Long clientId, Pageable pageable) {

        return quoteRepository.findByClientIdAndActiveTrue(clientId, pageable);
    }

    @Override
    public Quote create(QuoteDto quoteDto) {

        Client client = clientRepository.findById(quoteDto.getClientId())
                .orElseThrow(() -> new ClientNotFoundException(quoteDto.getClientId()));

        Quote quote = quoteMapper.toEntity(quoteDto);

        User seller = currentUserService.getCurrentUser();

        quote.setSeller(seller);

        quote.setClient(client);
        quote.setQuoteNumber(generateQuoteNumber());

        quoteDomainService.initializeQuote(quote);
        quoteDomainService.calculateTotals(quote);

        return quoteRepository.save(quote);
    }

    @Override
    public Quote update(Long id, QuoteDto quoteDto) {

        Quote existingQuote = findById(id);

        quoteMapper.updateEntity(quoteDto, existingQuote);

        if (quoteDto.getClientId() != null) {

            Client client = clientRepository
                    .findById(quoteDto.getClientId())
                    .orElseThrow(() -> new ClientNotFoundException(quoteDto.getClientId()));

            existingQuote.setClient(client);
        }

        calculateTotals(existingQuote);

        return quoteRepository.save(existingQuote);
    }

    @Override
    public void delete(Long id) {

        Quote quote = findById(id);

        quote.setActive(false);

        quoteRepository.save(quote);
    }

    private void calculateTotals(Quote quote) {

        BigDecimal subtotal = quote.getSubtotal() != null
                        ? quote.getSubtotal() : BigDecimal.ZERO;

        BigDecimal discount = quote.getDiscount() != null
                        ? quote.getDiscount() : BigDecimal.ZERO;

        BigDecimal taxRate = quote.getTaxRate() != null
                        ? quote.getTaxRate() : new BigDecimal("21.00");

        BigDecimal taxableBase = subtotal.subtract(discount);

        if (taxableBase.compareTo(BigDecimal.ZERO) < 0) {
            taxableBase = BigDecimal.ZERO;
        }

        BigDecimal taxAmount = taxableBase.multiply(taxRate)
                .divide(new BigDecimal("100"),2, RoundingMode.HALF_UP);

        BigDecimal total = taxableBase.add(taxAmount);

        quote.setTaxAmount(taxAmount);
        quote.setTotal(total);
    }

    private String generateQuoteNumber() {

        int year = LocalDate.now().getYear();

        String prefix = "Q-" + year + "-";

        Optional<Quote> lastQuote = quoteRepository
                        .findTopByQuoteNumberStartingWithOrderByQuoteNumberDesc(prefix);

        int nextNumber = 1;

        if (lastQuote.isPresent()) {

            String lastQuoteNumber = lastQuote.get().getQuoteNumber();

            String sequence = lastQuoteNumber.substring(prefix.length());

            nextNumber = Integer.parseInt(sequence) + 1;
        }

        return prefix + String.format("%06d", nextNumber);
    }


}