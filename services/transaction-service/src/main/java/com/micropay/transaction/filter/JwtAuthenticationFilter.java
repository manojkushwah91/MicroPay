package com.micropay.transaction.filter;

import com.micropay.transaction.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                // Parse once. If expired or forged, it instantly drops into the catch blocks below.
                Claims claims = jwtUtil.getAllClaimsFromToken(jwt);
                
                @SuppressWarnings("unchecked")
                List<String> authorities = (List<String>) claims.get("authorities");
                
                if (authorities == null) {
                    authorities = List.of("ROLE_USER");
                }
                
                List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(claims.getSubject(), null, grantedAuthorities);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException ex) {
            logger.warn("Transaction Service: JWT Token is expired - " + ex.getMessage());
            SecurityContextHolder.clearContext();
        } catch (SignatureException ex) {
            logger.error("Transaction Service: Invalid JWT signature (Potential forged token) - " + ex.getMessage());
            SecurityContextHolder.clearContext();
        } catch (MalformedJwtException ex) {
            logger.error("Transaction Service: Malformed JWT token - " + ex.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception ex) {
            logger.error("Transaction Service: Unexpected error parsing JWT", ex);
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}