package account.business.models.users;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "USERS")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String lastname;

    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @Column
    private List<String> roles;

    @Column
    private boolean isActive;

    @Column(name = "failed_attempt")
    private int failedAttempt;
}