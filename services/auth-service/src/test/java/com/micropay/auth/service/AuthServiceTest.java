package com.micropay.auth.service;

import com.micropay.auth.dto.AuthResponse;
import com.micropay.auth.dto.LoginRequest;
import com.micropay.auth.dto.RegisterRequest;
import com.micropay.auth.model.Role;
import com.micropay.auth.model.User;
import com.micropay.auth.model.UserStatus;
import com.micropay.auth.repository.RoleRepository;
import com.micropay.auth.repository.UserRepository;
import com.micropay.events.dto.UserCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate = mock(KafkaTemplate.class);

    private final AuthService authService = new AuthService(
            userRepository,
            roleRepository,
            passwordEncoder,
            jwtService,
            authenticationManager,
            kafkaTemplate
    );

    @Test
    @DisplayName("register creates user, publishes event and returns JWT")
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("secret");
        req.setFirstName("Test");
        req.setLastName("User");

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("ENC");

        Role role = new Role("ROLE_USER", "Default user role");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));

        User saved = new User();
        saved.setId(UUID.randomUUID());
        saved.setEmail(req.getEmail());
        saved.setFirstName(req.getFirstName());
        saved.setLastName(req.getLastName());
        saved.setPassword("ENC");
        saved.setStatus(UserStatus.ACTIVE);
        saved.setCreatedAt(LocalDateTime.now());

        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse resp = authService.register(req);

        assertThat(resp.getToken()).isEqualTo("jwt-token");
        assertThat(resp.getEmail()).isEqualTo(req.getEmail());

        verify(userRepository).save(any(User.class));
        verify(kafkaTemplate).send(eq("user.created"), eq(saved.getId().toString()), any(UserCreatedEvent.class));
    }

    @Test
    @DisplayName("login authenticates and returns JWT")
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("secret");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(req.getEmail());
        user.setPassword("ENC");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse resp = authService.login(req);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).save(any(User.class));
        assertThat(resp.getToken()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("loadUserByUsername throws if user not found")
    void loadUserByUsername_notFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}


