package account.persistence;

import account.business.models.users.UserEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<UserEntity, Long> {
    UserEntity findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<UserEntity> findByOrderById();
    void deleteByEmailIgnoreCase(String email);

    @Query("UPDATE UserEntity u SET u.failedAttempt = ?1 WHERE u.email = ?2")
    @Modifying
    public void updateFailedAttempts(int failAttempts, String email);
}