package com.micropay.payment.filter;

import com.micropay.payment.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Authentication Filter Basic Tests")
class JwtAuthenticationFilterBasicTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate user with valid JWT token")
    void doFilterInternal_ValidToken() throws ServletException, IOException {
        // Given
        String token = "valid-jwt-token";
        String authHeader = "Bearer " + token;
        String username = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn(authHeader);

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(username);
        when(claims.get("authorities")).thenReturn(List.of("ROLE_USER"));

        when(jwtUtil.getAllClaimsFromToken(token)).thenReturn(claims);
        when(jwtUtil.isInvalid(token)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).getAllClaimsFromToken(token);
        verify(jwtUtil).isInvalid(token);

        UsernamePasswordAuthenticationToken authentication = 
            (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        
        assertNotNull(authentication);
        assertEquals(username, authentication.getPrincipal());
        assertEquals(1, authentication.getAuthorities().size());
        assertEquals("ROLE_USER", authentication.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @DisplayName("Should not authenticate with invalid JWT token")
    void doFilterInternal_InvalidToken() throws ServletException, IOException {
        // Given
        String token = "invalid-jwt-token";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.isInvalid(token)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).isInvalid(token);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should not authenticate without Authorization header")
    void doFilterInternal_NoAuthHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).getAllClaimsFromToken(any());
        verify(jwtUtil, never()).isInvalid(any());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should not authenticate with malformed Authorization header")
    void doFilterInternal_MalformedAuthHeader() throws ServletException, IOException {
        // Given
        String authHeader = "InvalidToken";
        when(request.getHeader("Authorization")).thenReturn(authHeader);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).getAllClaimsFromToken(any());
        verify(jwtUtil, never()).isInvalid(any());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should handle JWT processing exception gracefully")
    void doFilterInternal_JwtProcessingException() throws ServletException, IOException {
        // Given
        String token = "malformed-jwt-token";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.getAllClaimsFromToken(token)).thenThrow(new RuntimeException("JWT processing failed"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).getAllClaimsFromToken(token);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
