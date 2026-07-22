package br.com.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * The WebSocket handshake is a plain HTTP request that browsers cannot attach custom
 * headers to, so it can't carry a Bearer token. Authorization instead happens on the
 * STOMP CONNECT frame via StompAuthChannelInterceptor, once the socket is established.
 * This filter chain only exists to stop Spring's oauth2-resource-server autoconfiguration
 * from requiring a Bearer token on that HTTP handshake (and on every other endpoint).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
