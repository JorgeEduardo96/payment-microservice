package br.com.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Integration tests exercise the controller layer directly via MockMvc without a Keycloak/OAuth2
 * setup, so no real JWTs are available.
 * <p>
 * {@link SecurityConfig} (the real JWT resource-server config) is disabled for the "integration"
 * profile ({@code @Profile("!integration")}), but simply having no {@link SecurityFilterChain}
 * bean at all is NOT equivalent to "no security": with spring-boot-starter-security on the
 * classpath, Spring Boot's default autoconfiguration steps in and requires authentication for
 * every request, which blocks these tests with 401. This permit-all chain replaces that default
 * so MockMvc requests reach the controllers unauthenticated, matching pre-JWT-migration behavior.
 */
@Configuration
@EnableWebSecurity
@Profile("integration")
public class IntegrationSecurityConfig {

    @Bean
    public SecurityFilterChain integrationSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
