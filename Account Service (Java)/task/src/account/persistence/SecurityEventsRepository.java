package account.persistence;

import account.business.models.SecurityEventEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityEventsRepository extends CrudRepository<SecurityEventEntity, Long> {
    List<SecurityEventEntity> findByOrderById();
}