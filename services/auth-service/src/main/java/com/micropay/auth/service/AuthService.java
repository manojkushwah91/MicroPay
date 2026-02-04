package com.micropay.auth.service;

import com.micropay.auth.dto.AuthResponse;
import com.micropay.auth.dto.LoginRequest;
import com.micropay.auth.dto.RegisterRequest;
import com.micropay.events.dto.UserCreatedEvent;
import com.micropay.auth.model.Role;
import com.micropay.auth.model.User;
import com.micropay.auth.model.UserStatus;
import com.micropay.auth.repository.RoleRepository;
import com.micropay.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy; // Import for @Lazy
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    private static final String USER_ROLE_NAME = "ROLE_USER";
    private static final String USER_CREATED_TOPIC = "user.created";

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            @Lazy PasswordEncoder passwordEncoder, // <--- Already Fixed
            JwtService jwtService,
            @Lazy AuthenticationManager authenticationManager, // <--- ADD @LAZY HERE (Param 4)
            KafkaTemplate<String, UserCreatedEvent> kafkaTemplate) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.kafkaTemplate = kafkaTemplate;
    }

    // ... (The rest of the file remains exactly the same) ...

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setStatus(UserStatus.ACTIVE);

        Role userRole = roleRepository.findByName(USER_ROLE_NAME)
                .orElseGet(() -> roleRepository.save(new Role(USER_ROLE_NAME, "Default user role")));
        user.addRole(userRole);

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", savedUser.getId());

        publishUserCreatedEvent(savedUser);

        UserDetails userDetails = loadUserByUsername(savedUser.getEmail());
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName()
        );
    }

    public AuthResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("User account is not active");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        logger.info("User logged in successfully: {}", user.getEmail());

        UserDetails userDetails = loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(Role::getName)
                        .toArray(String[]::new))
                .accountExpired(false)
                .accountLocked(user.getStatus() == UserStatus.SUSPENDED)
                .credentialsExpired(false)
                .disabled(user.getStatus() != UserStatus.ACTIVE)
                .build();
    }

    private void publishUserCreatedEvent(User user) {
        try {
            UserCreatedEvent event = new UserCreatedEvent(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getCreatedAt()
            );

            kafkaTemplate.send(USER_CREATED_TOPIC, user.getId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            logger.info("Published user.created event for user: {}", user.getId());
                        } else {
                            logger.error("Failed to publish user.created event for user: {}", user.getId(), ex);
                        }
                    });
        } catch (Exception e) {
            logger.error("Error publishing user.created event for user: {}", user.getId(), e);
        }
    }
}