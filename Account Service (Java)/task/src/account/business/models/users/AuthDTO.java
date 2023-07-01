package account.business.models.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthDTO {
    @NotBlank
    private String name;

    @NotBlank
    private String lastname;

    @Pattern(regexp = ".+@acme.com")
    @NotEmpty
    private String email;

    @Size(min = 12)
    @NotEmpty
    private String password;
}