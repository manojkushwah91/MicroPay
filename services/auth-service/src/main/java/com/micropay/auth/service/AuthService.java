package com.micropay.auth.service;

import com.micropay.events.dto.PasswordResetEvent;
import com.micropay.events.dto.UserCreatedEvent;
import com.micropay.auth.dto.AuthResponse;
import com.micropay.auth.dto.LoginRequest;
import com.micropay.auth.dto.RegisterRequest;
import com.micropay.auth.model.BlockedToken;
import com.micropay.auth.model.PasswordResetToken;
import com.micropay.auth.model.Role;
import com.micropay.auth.model.User;
import com.micropay.auth.model.UserStatus;
import com.micropay.auth.repository.BlockedTokenRepository;
import com.micropay.auth.repository.PasswordResetTokenRepository;
import com.micropay.auth.repository.RoleRepository;
import com.micropay.auth.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
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
import java.util.UUID;

@Service
public class AuthService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;
    private final KafkaTemplate<String, PasswordResetEvent> passwordResetKafkaTemplate;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final BlockedTokenRepository blockedTokenRepository;

    private static final String USER_ROLE_NAME = "ROLE_USER";
    private static final String USER_CREATED_TOPIC = "user.created";

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            @Lazy PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Lazy AuthenticationManager authenticationManager,
            KafkaTemplate<String, UserCreatedEvent> kafkaTemplate,
            KafkaTemplate<String, PasswordResetEvent> passwordResetKafkaTemplate,
            PasswordResetTokenRepository passwordResetTokenRepository,
            BlockedTokenRepository blockedTokenRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.kafkaTemplate = kafkaTemplate;
        this.passwordResetKafkaTemplate = passwordResetKafkaTemplate;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.blockedTokenRepository = blockedTokenRepository;
    }

    // ----------------------- Logout -----------------------
    public void logout(String token) {
        Claims claims = jwtService.extractAllClaims(token);
        LocalDateTime expiryDate = claims.getExpiration()
                .toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime();
        BlockedToken blockedToken = new BlockedToken(token, expiryDate);
        blockedTokenRepository.save(blockedToken);
    }

    // ----------------------- Password Reset -----------------------
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if (passwordResetToken == null || passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired password reset token");
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(passwordResetToken);
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(passwordResetToken);

        passwordResetKafkaTemplate.send(
                "password.reset",
                new PasswordResetEvent(user.getId(), user.getEmail(), token)
        );
    }

    // ----------------------- Registration -----------------------
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

    // ----------------------- Login -----------------------
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

    // ----------------------- Load User -----------------------
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

    // ----------------------- Event Publishing -----------------------
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