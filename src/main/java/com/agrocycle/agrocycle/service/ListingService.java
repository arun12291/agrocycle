package com.agrocycle.agrocycle.service;

import com.agrocycle.agrocycle.dto.*;
import com.agrocycle.agrocycle.entity.*;
import com.agrocycle.agrocycle.exception.*;
import com.agrocycle.agrocycle.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListingService {

    private static final Logger log = LoggerFactory.getLogger(ListingService.class);

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ReviewRepository reviewRepository;

    public ListingService(ListingRepository listingRepository, UserRepository userRepository,
                          FileStorageService fileStorageService, ReviewRepository reviewRepository) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public ListingDto createListing(ListingRequest req, String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Listing listing = new Listing();
        mapRequestToListing(req, listing);
        listing.setSeller(seller);
        listing.setAvailableQuantity(req.getTotalQuantity());
        listing.setReservedQuantity(BigDecimal.ZERO);
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        Listing saved = listingRepository.save(listing);
        log.info("Listing created: {} by {}", saved.getId(), seller.getEmail());
        return toDto(saved);
    }

    @Transactional
    public ListingDto addImage(Long listingId, MultipartFile file, String userEmail) {
        Listing listing = getOwnedListing(listingId, userEmail);
        if (listing.getImages().size() >= 5) throw new ApiException("Maximum 5 images per listing", 400);
        String fileName = fileStorageService.store(file);
        ListingImage img = new ListingImage();
        img.setListing(listing);
        img.setFileName(fileName);
        img.setFilePath("uploads/" + fileName);
        img.setContentType(file.getContentType());
        img.setFileSize(file.getSize());
        img.setPrimaryImage(listing.getImages().isEmpty());
        listing.getImages().add(img);
        return toDto(listingRepository.save(listing));
    }

    @Transactional
    public ListingDto updateListing(Long listingId, ListingRequest req, String userEmail) {
        Listing listing = getOwnedListing(listingId, userEmail);
        if (listing.getStatus() == Listing.ListingStatus.DELETED) throw new ApiException("Listing is deleted", 400);
        BigDecimal diff = req.getTotalQuantity().subtract(listing.getTotalQuantity());
        BigDecimal newAvailable = listing.getAvailableQuantity().add(diff);
        if (newAvailable.compareTo(BigDecimal.ZERO) < 0) throw new ApiException("Cannot reduce below reserved quantity", 400);
        mapRequestToListing(req, listing);
        listing.setAvailableQuantity(newAvailable);
        return toDto(listingRepository.save(listing));
    }

    @Transactional
    public void deleteListing(Long listingId, String userEmail) {
        Listing listing = getOwnedListing(listingId, userEmail);
        listing.setStatus(Listing.ListingStatus.DELETED);
        listingRepository.save(listing);
    }

    @Transactional(readOnly = true)
    public ListingDto getListing(Long listingId) {
        return toDto(listingRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found")));
    }

    @Transactional(readOnly = true)
    public Page<ListingDto> searchListings(Listing.ListingType type, Listing.Category category,
            String state, String district, BigDecimal minPrice, BigDecimal maxPrice,
            BigDecimal minQty, String keyword, int page, int size, String sort) {
        Sort pageSort = switch (sort != null ? sort : "newest") {
            case "price_asc" -> Sort.by("pricePerUnit").ascending();
            case "price_desc" -> Sort.by("pricePerUnit").descending();
            case "qty_desc" -> Sort.by("availableQuantity").descending();
            default -> Sort.by("createdAt").descending();
        };
        PageRequest pageable = PageRequest.of(page, Math.min(size, 50), pageSort);
        return listingRepository.searchListings(type, category, state, district,
            minPrice, maxPrice, minQty, keyword, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<ListingDto> getMyListings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return listingRepository.findBySellerIdOrderByCreatedAtDesc(user.getId())
            .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ListingDto> getFeaturedListings(int count) {
        return listingRepository.findFeaturedListings(PageRequest.of(0, count))
            .stream().map(this::toDto).collect(Collectors.toList());
    }

    private Listing getOwnedListing(Long listingId, String userEmail) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        if (!listing.getSeller().getEmail().equals(userEmail))
            throw new ApiException("Not authorized to modify this listing", 403);
        return listing;
    }

    private void mapRequestToListing(ListingRequest req, Listing listing) {
        listing.setProductName(req.getProductName().trim());
        listing.setCategory(req.getCategory());
        listing.setDescription(req.getDescription());
        listing.setCondition(req.getCondition());
        listing.setTotalQuantity(req.getTotalQuantity());
        listing.setUnit(req.getUnit().trim());
        listing.setPricePerUnit(req.getPricePerUnit());
        listing.setNegotiable(req.isNegotiable());
        listing.setVillage(req.getVillage().trim());
        listing.setDistrict(req.getDistrict().trim());
        listing.setState(req.getState().trim());
        listing.setPostalCode(req.getPostalCode());
        listing.setLatitude(req.getLatitude());
        listing.setLongitude(req.getLongitude());
        listing.setListingType(req.getListingType());
    }

    private ListingDto toDto(Listing listing) {
        ListingDto dto = ListingDto.from(listing);
        dto.setAverageRating(reviewRepository.getAverageRatingBySeller(listing.getSeller().getId()));
        dto.setRatingCount(reviewRepository.countBySeller(listing.getSeller().getId()));
        return dto;
    }
}
