package com.agrocycle.agrocycle.service;

import com.agrocycle.agrocycle.dto.*;
import com.agrocycle.agrocycle.entity.*;
import com.agrocycle.agrocycle.exception.*;
import com.agrocycle.agrocycle.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final DeliveryProfileRepository deliveryProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuditLogRepository auditLogRepository;

    public AuthService(UserRepository userRepository,
                       DeliveryProfileRepository deliveryProfileRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.deliveryProfileRepository = deliveryProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public UserDto register(RegisterRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new ApiException("Passwords do not match", 400);
        }
        if (userRepository.existsByEmail(req.getEmail().toLowerCase())) {
            throw new ApiException("Email is already registered", 409);
        }
        if (userRepository.existsByMobileNumber(req.getMobileNumber())) {
            throw new ApiException("Mobile number is already registered", 409);
        }
        User user = new User();
        user.setFullName(req.getFullName().trim());
        user.setEmail(req.getEmail().toLowerCase().trim());
        user.setMobileNumber(req.getMobileNumber().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(User.Role.FARMER_BUYER);
        user.setStatus(User.AccountStatus.ACTIVE);
        user.setVillage(req.getVillage().trim());
        user.setDistrict(req.getDistrict().trim());
        user.setState(req.getState().trim());
        user.setPostalCode(req.getPostalCode());
        user.setLatitude(req.getLatitude());
        user.setLongitude(req.getLongitude());
        user.setPreferredActivity(req.getPreferredActivity() != null ? req.getPreferredActivity() : User.PreferredActivity.BOTH);
        User saved = userRepository.save(user);
        log.info("New user registered: {} ({})", saved.getEmail(), saved.getId());
        return UserDto.from(saved);
    }

    @Transactional
    public UserDto registerDeliveryPerson(DeliveryProfileRequest req) {
        if (userRepository.existsByEmail(req.getEmail().toLowerCase())) {
            throw new ApiException("Email is already registered", 409);
        }
        if (userRepository.existsByMobileNumber(req.getMobileNumber())) {
            throw new ApiException("Mobile number is already registered", 409);
        }
        User user = new User();
        user.setFullName(req.getFullName().trim());
        user.setEmail(req.getEmail().toLowerCase().trim());
        user.setMobileNumber(req.getMobileNumber().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(User.Role.DELIVERY_PERSON);
        user.setStatus(User.AccountStatus.PENDING_VERIFICATION);
        user.setVillage(req.getVillage().trim());
        user.setDistrict(req.getDistrict().trim());
        user.setState(req.getState().trim());
        user.setPostalCode(req.getPostalCode());
        user.setLatitude(req.getLatitude());
        user.setLongitude(req.getLongitude());
        user.setPreferredActivity(User.PreferredActivity.BOTH);
        User saved = userRepository.save(user);

        DeliveryProfile profile = new DeliveryProfile();
        profile.setUser(saved);
        profile.setServiceArea(req.getServiceArea().trim());
        profile.setVehicleType(req.getVehicleType());
        profile.setCarryingCapacityKg(req.getCarryingCapacityKg());
        profile.setDocumentType(req.getDocumentType());
        profile.setDocumentNumber(req.getDocumentNumber());
        profile.setApprovalStatus(DeliveryProfile.ApprovalStatus.PENDING);
        profile.setAvailable(false);
        deliveryProfileRepository.save(profile);

        log.info("New delivery person application: {} ({})", saved.getEmail(), saved.getId());
        return UserDto.from(saved);
    }

    @Transactional
    public UserDto authenticate(LoginRequest req, jakarta.servlet.http.HttpSession session) {
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail().toLowerCase(), req.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (BadCredentialsException e) {
            throw new ApiException("Invalid email or password", 401);
        } catch (DisabledException e) {
            throw new ApiException("Account is suspended. Please contact support.", 403);
        }
        User user = userRepository.findByEmail(req.getEmail().toLowerCase())
            .orElseThrow(() -> new ApiException("User not found", 404));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("User logged in: {}", user.getEmail());
        return UserDto.from(user);
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserDto.from(user);
    }
}
