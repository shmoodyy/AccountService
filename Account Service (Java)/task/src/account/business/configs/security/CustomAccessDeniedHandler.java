package account.business.configs.security;

import account.business.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Map;

//@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    public static final Logger LOG
            = Logger.getLogger(CustomAccessDeniedHandler.class);

    @Autowired
    private UserService userService;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        String path = request.getRequestURI();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(
                Map.of(
                        "timestamp", Calendar.getInstance().getTime(),
                        "status", HttpServletResponse.SC_FORBIDDEN,
                        "error", "Forbidden",
                        "message", "Access Denied!",
                        "path", path
                )
        );

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (authentication != null) ? authentication.getName() : null;
        userService.registerSecurityEvent(SecurityEvents.ACCESS_DENIED, email, path, path);

        response.getWriter().write(json);
    }
}
