package account.business.configs.security;

import account.business.models.users.UserEntity;
import account.business.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private UserService userService;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {
        String authorizationHeader = request.getHeader("Authorization");
        String path = request.getRequestURI();

        if (authorizationHeader != null && authorizationHeader.startsWith("Basic ")) {
            // Decoding Basic Auth credentials to obtain the email (username) using Base65 decryption
            String encodedCredentials = authorizationHeader.substring("Basic ".length());
            byte[] decodedCredentials = Base64.getDecoder().decode(encodedCredentials);
            String credentials = new String(decodedCredentials);
            String[] parts = credentials.split(":", 2);
            String email = parts[0];

            UserEntity user = userService.findUserEntityByEmailIgnoreCase(email);
            if (user == null) {
                userService.registerSecurityEvent(SecurityEvents.LOGIN_FAILED, email, path, path);
            } else {
                if (user.isActive()) {
                    userService.increaseFailedAttempts(user);
                    userService.registerSecurityEvent(SecurityEvents.LOGIN_FAILED, email, path, path);

                    if (user.getFailedAttempt() == UserService.MAX_FAILED_ATTEMPTS) {
                        userService.lock(user);

                        userService.registerSecurityEvent(SecurityEvents.BRUTE_FORCE, email, path, path);
                        userService.registerSecurityEvent(SecurityEvents.LOCK_USER, email
                                , "Lock user " + email, path);

                        userService.resetFailedAttempts(user.getEmail());

                        authException = new LockedException("Your account has been locked due to 5 failed attempts.");
                    }
                }
            }
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}