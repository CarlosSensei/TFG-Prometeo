package com.optiflow.crm.search;

import com.optiflow.crm.search.dto.SearchResultDto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@Validated
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<List<SearchResultDto>> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "10")
            @Min(1) @Max(20) int limit
    ) {

        return ResponseEntity.ok(searchService.search(q, limit));
    }
}
