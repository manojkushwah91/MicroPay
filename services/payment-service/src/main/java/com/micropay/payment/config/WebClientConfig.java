package com.micropay.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://wallet-service:8083")
                .filter((request, next) -> {
                    return ReactiveSecurityContextHolder.getContext()
                            .map(SecurityContext::getAuthentication)
                            .filter(auth -> auth != null && auth.getCredentials() != null)
                            .map(auth -> "Bearer " + auth.getCredentials().toString())
                            .defaultIfEmpty("")
                            .flatMap(token -> {
                                if (!token.isEmpty()) {
                                    return next.exchange(
                                        ClientRequest.from(request)
                                            .header("Authorization", token)
                                            .build()
                                    );
                                }
                                return next.exchange(request);
                            });
                })
                .build();
    }
}
