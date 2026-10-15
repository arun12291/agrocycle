package com.agrocycle.agrocycle.controller;

import com.agrocycle.agrocycle.dto.*;
import com.agrocycle.agrocycle.entity.Listing;
import com.agrocycle.agrocycle.service.ListingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping("/public/search")
    public ResponseEntity<Page<ListingDto>> search(
            @RequestParam(required = false) Listing.ListingType type,
            @RequestParam(required = false) Listing.Category category,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minQty,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        return ResponseEntity.ok(listingService.searchListings(type, category, state, district, minPrice, maxPrice, minQty, keyword, page, size, sort));
    }

    @GetMapping("/public/featured")
    public ResponseEntity<List<ListingDto>> featured(@RequestParam(defaultValue = "6") int count) {
        return ResponseEntity.ok(listingService.getFeaturedListings(count));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ListingDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(listingService.getListing(id));
    }

    @GetMapping("/public/categories")
    public ResponseEntity<Listing.Category[]> categories() {
        return ResponseEntity.ok(Listing.Category.values());
    }

    @PostMapping
    public ResponseEntity<ListingDto> create(@Valid @RequestBody ListingRequest req, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(201).body(listingService.createListing(req, user.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ListingDto> update(@PathVariable Long id, @Valid @RequestBody ListingRequest req, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(listingService.updateListing(id, req, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        listingService.deleteListing(id, user.getUsername());
        return ResponseEntity.ok(Map.of("message", "Listing deleted"));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ListingDto> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(listingService.addImage(id, file, user.getUsername()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ListingDto>> myListings(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(listingService.getMyListings(user.getUsername()));
    }
}
