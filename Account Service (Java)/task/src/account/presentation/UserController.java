package account.presentation;

import account.business.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/user")
public class UserController {

    @Autowired
    private final UserService userService;

    @PutMapping("/role")
    public ResponseEntity<Object> updateUserRole(@RequestBody Map<String, String> updateRequest,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        String email = updateRequest.get("user").toLowerCase();
        String roleRequest = updateRequest.get("role").toUpperCase();
        String operation = updateRequest.get("operation").toUpperCase();

        if (!userService.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }

        var domain = userService.findUserByEmailIgnoreCase(email);
        List<String> currentRoles = domain.getRoles();

        if (!roleRequest.matches("ADMINISTRATOR|ACCOUNTANT|USER|AUDITOR")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
        }

        if (!operation.matches("GRANT|REMOVE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid operation!");
        }

        if (operation.equals("REMOVE")) {
            removeRoleErrorHandling(roleRequest, currentRoles);
        }

        grantRoleErrorHandling(roleRequest, currentRoles);

        return ResponseEntity.ok().body(userService.updateUserRole(email, userDetails.getUsername()
                , roleRequest, operation));
    }

    @GetMapping({"","/"})
    public ResponseEntity<Object> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @DeleteMapping({"", "/{email}"})
    public ResponseEntity<Object> deleteUserByEmail(@PathVariable String email,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (!userService.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }

        var userRoles = userService.findUserByEmailIgnoreCase(email).getRoles();
        if (userRoles.contains("ROLE_ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        }

        return ResponseEntity.ok().body(userService.deleteUserByEmail(email.toLowerCase(), userDetails.getUsername()));
    }

    @PutMapping("/access")
    public ResponseEntity<Object> updateUserAccess(@RequestBody Map<String, String> updateRequest,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        String email = updateRequest.get("user").toLowerCase();
        String operation = updateRequest.get("operation");

        if (!userService.existsByEmailIgnoreCase(updateRequest.get("user"))) {
            return ResponseEntity.notFound().build();
        }


        var userRoles = userService.findUserByEmailIgnoreCase(email).getRoles();
        if (userRoles.contains("ROLE_ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
        }

        return ResponseEntity.ok().body(userService.updateUserAccess(email, userDetails.getUsername(), operation));
    }


    // Controller utility methods
    public void grantRoleErrorHandling(String roleRequest, List<String> currentRoles) {
        if ((currentRoles.contains("ROLE_ADMINISTRATOR") && roleRequest.matches("ACCOUNTANT|USER|AUDITOR"))
                || (currentRoles.contains("ROLE_ACCOUNTANT")
                || currentRoles.contains("ROLE_USER")
                || currentRoles.contains("ROLE_USER"))
                    && roleRequest.equals("ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST
                    , "The user cannot combine administrative and business roles!");
        }
    }

    public void removeRoleErrorHandling(String roleRequest, List<String> currentRoles) {
        if (roleRequest.equals("ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        } if (!currentRoles.contains("ROLE_" + roleRequest)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
        } if (currentRoles.size() == 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
        }
    }
}