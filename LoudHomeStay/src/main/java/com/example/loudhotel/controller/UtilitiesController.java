package com.example.loudhotel.controller;

import com.example.loudhotel.dto.request.UtilitiesRequest;
import com.example.loudhotel.dto.response.UtilitiesResponse;
import com.example.loudhotel.service.UtilitiesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/utilities")
@RequiredArgsConstructor
public class UtilitiesController {

    private final UtilitiesService utilitiesService;

    @GetMapping
    public Page<UtilitiesResponse> getAll(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) com.example.loudhotel.enums.UtilityType utilityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "utilitiesId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return utilitiesService.getAllUtilities(keyword, utilityType, pageable);
    }

    @PostMapping
    public void create(@Valid @RequestBody UtilitiesRequest request) {
        utilitiesService.create(request);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id,
                       @Valid @RequestBody UtilitiesRequest request) {
        utilitiesService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        utilitiesService.deleteById(id);
    }
}
