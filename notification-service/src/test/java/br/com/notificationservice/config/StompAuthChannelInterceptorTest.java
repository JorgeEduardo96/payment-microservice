package br.com.notificationservice.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StompAuthChannelInterceptorTest {

    @Mock
    private JwtDecoder jwtDecoder;

    @InjectMocks
    private StompAuthChannelInterceptor underTest;

    private final MessageChannel channel = mock(MessageChannel.class);

    @Test
    void allowsConnectWithAdminRole() {
        when(jwtDecoder.decode("valid-token")).thenReturn(jwtWithRoles("ADMIN"));
        Message<?> message = connectMessage("Bearer valid-token");

        Message<?> result = underTest.preSend(message, channel);

        assertThat(result).isSameAs(message);
    }

    @Test
    void rejectsConnectWithUserRole() {
        when(jwtDecoder.decode("valid-token")).thenReturn(jwtWithRoles("USER"));
        Message<?> message = connectMessage("Bearer valid-token");

        assertThatThrownBy(() -> underTest.preSend(message, channel))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void allowsConnectWithClientRoleAndBindsSessionToClientId() {
        when(jwtDecoder.decode("valid-token")).thenReturn(jwtWithClientRole("11111111-1111-1111-1111-111111111111"));
        Message<?> message = connectMessage("Bearer valid-token");

        Message<?> result = underTest.preSend(message, channel);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
        assertThat(accessor.getUser()).isNotNull();
        assertThat(accessor.getUser().getName()).isEqualTo("11111111-1111-1111-1111-111111111111");
    }

    @Test
    void rejectsConnectWithClientRoleMissingClientIdClaim() {
        Jwt jwtWithoutClientId = Jwt.withTokenValue("valid-token")
                .header("alg", "none")
                .claim("sub", "some-client")
                .claim("realm_access", Map.of("roles", List.of("CLIENT")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        when(jwtDecoder.decode("valid-token")).thenReturn(jwtWithoutClientId);
        Message<?> message = connectMessage("Bearer valid-token");

        assertThatThrownBy(() -> underTest.preSend(message, channel))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectsConnectWithoutAuthorizationHeader() {
        Message<?> message = connectMessage(null);

        assertThatThrownBy(() -> underTest.preSend(message, channel))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ignoresNonConnectFrames() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = underTest.preSend(message, channel);

        assertThat(result).isSameAs(message);
    }

    private Message<?> connectMessage(String authorizationHeader) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (authorizationHeader != null) {
            accessor.setNativeHeader("Authorization", authorizationHeader);
        }
        // Mirrors what Spring's real STOMP frame decoding does for CONNECT frames specifically,
        // so that security interceptors can attach a Principal via accessor.setUser(...).
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Jwt jwtWithRoles(String... roles) {
        return Jwt.withTokenValue("valid-token")
                .header("alg", "none")
                .claim("sub", "demo")
                .claim("realm_access", Map.of("roles", List.of(roles)))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }

    private Jwt jwtWithClientRole(String clientId) {
        return Jwt.withTokenValue("valid-token")
                .header("alg", "none")
                .claim("sub", "some-client")
                .claim("clientId", clientId)
                .claim("realm_access", Map.of("roles", List.of("CLIENT")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
