package account.business.models;

import account.business.configs.security.SecurityEvents;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SECURITY_EVENTS")
public class SecurityEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDateTime date = LocalDateTime.now();

    @Column
    @Enumerated(EnumType.STRING)
    private SecurityEvents action;

    @Column
    private String subject;

    @Column
    private String object;

    @Column
    private String path;
}
