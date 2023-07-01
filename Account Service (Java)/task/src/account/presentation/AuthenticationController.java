package account.presentation;

import account.business.models.users.AuthDTO;
import account.business.models.users.NewPasswordDTO;
import account.business.models.users.UserDomain;
import account.business.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthenticationController {

    @Autowired
    private final UserService userService;

    @Autowired
    private final PasswordEncoder encoder;

    @Autowired
    private final ModelMapper controllerModelMapper;

    @PostMapping("/auth/signup")
    public ResponseEntity<Object> registerUser(@Valid @RequestBody AuthDTO authDTO) {
        String password = authDTO.getPassword();
        if (userService.existsByEmailIgnoreCase(authDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");
        }

        passwordSecurityChecks(password); // throws exception if security check fails

        authDTO.setPassword(encoder.encode(password));
        var domain = convertDTOToUserDomain(authDTO);
        List<String> roleList = new ArrayList<>(2);
        roleList.add(userService.numberOfUsers() == 0 ? "ROLE_ADMINISTRATOR" : "ROLE_USER");
        domain.setRoles(roleList);
        return ResponseEntity.ok(userService.registerUser(domain));
    }

    @PostMapping("/auth/changepass")
    public ResponseEntity<Object> changePassword(@Valid @RequestBody NewPasswordDTO newPasswordDTO
            , @AuthenticationPrincipal UserDetails userDetails) {
        String newPassword = newPasswordDTO.getNewPassword();
        if (encoder.matches(newPassword, userDetails.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
        }

        passwordSecurityChecks(newPassword); // throws exception if security check fails

        String email = userDetails.getUsername();
        return ResponseEntity.ok(userService.changePassword(email, newPassword, encoder));
    }

    @GetMapping("/security/events")

    // Controller utility methods
    public void passwordSecurityChecks(String password) {
        if (password.length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
        } if (userService.isBreachedPassword(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        }
    }

    public UserDomain convertDTOToUserDomain(AuthDTO authDTO) {
        return controllerModelMapper.map(authDTO, UserDomain.class);
    }
}