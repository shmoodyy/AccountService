package account.presentation;

import account.business.services.SecurityEventsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/security/events", "/api/security/events/"})
public class SecurityEventsController {

    @Autowired
    private final SecurityEventsService securityEventsService;

    @GetMapping
    public ResponseEntity<Object> listEvents() {
        return ResponseEntity.ok(securityEventsService.listEvents());
    }
}

