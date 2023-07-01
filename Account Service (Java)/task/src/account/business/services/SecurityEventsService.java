package account.business.services;

import account.business.models.SecurityEventEntity;
import account.business.models.users.UserDomain;
import account.persistence.SecurityEventsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SecurityEventsService {


    @Autowired
    private final SecurityEventsRepository securityEventsRepository;

    public List<SecurityEventEntity> listEvents() {
        return securityEventsRepository.findByOrderById();
    }
}
