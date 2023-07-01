package account.business.models.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDomain {
    private Long id;
    private String name;
    private String lastname;
    private String email;

    @JsonIgnore
    private String password;
    private List<String> roles;
}