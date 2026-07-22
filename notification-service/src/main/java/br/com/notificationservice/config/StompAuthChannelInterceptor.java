package br.com.notificationservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Rejects STOMP CONNECT frames unless the caller presents a valid JWT with the ADMIN or CLIENT
 * realm role. The gateway can't do this check itself: it only sees the initial HTTP upgrade
 * request, while the token travels as a STOMP-level header sent after the WebSocket connection
 * is already open.
 *
 * CLIENT connections are additionally bound to a Principal named after the JWT's clientId claim,
 * so WebSocketNotificationService can target a single client's session via convertAndSendToUser
 * instead of broadcasting.
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String CLIENT_ROLE = "CLIENT";

    private final JwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            Jwt jwt = jwtDecoder.decode(extractBearerToken(accessor));
            List<String> roles = realmRoles(jwt);

            if (roles.contains(CLIENT_ROLE)) {
                accessor.setUser(clientPrincipal(jwt));
            } else if (!roles.contains(ADMIN_ROLE)) {
                throw new AccessDeniedException("Only ADMIN or CLIENT users can connect to notifications");
            }
        }

        return message;
    }

    private String extractBearerToken(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders == null || authHeaders.isEmpty() || !authHeaders.get(0).startsWith("Bearer ")) {
            throw new AccessDeniedException("Missing Authorization header");
        }
        return authHeaders.get(0).substring("Bearer ".length());
    }

    @SuppressWarnings("unchecked")
    private List<String> realmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || realmAccess.get("roles") == null) {
            return List.of();
        }
        return (List<String>) realmAccess.get("roles");
    }

    private Principal clientPrincipal(Jwt jwt) {
        String clientId = jwt.getClaimAsString("clientId");
        if (clientId == null) {
            throw new AccessDeniedException("CLIENT token is missing the clientId claim");
        }
        return () -> clientId;
    }
}
