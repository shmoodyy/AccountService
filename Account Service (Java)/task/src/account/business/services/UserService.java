package account.business.services;

import account.business.configs.security.SecurityEvents;
import account.business.models.SecurityEventEntity;
import account.business.models.users.UserDomain;
import account.business.models.users.UserEntity;
import account.persistence.SecurityEventsRepository;
import account.persistence.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final SecurityEventsRepository securityEventsRepository;

    @Autowired
    private final ModelMapper serviceModelMapper;

    private final List<String> BREACHED_PASSWORDS_LIST = List.of("PasswordForJanuary", "PasswordForFebruary",
            "PasswordForMarch", "PasswordForApril", "PasswordForMay", "PasswordForJune", "PasswordForJuly",
            "PasswordForAugust", "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember"
            , "PasswordForDecember");

    public static final int MAX_FAILED_ATTEMPTS = 5;

    public UserDomain registerUser(UserDomain userDomain) {
        var email = userDomain.getEmail().toLowerCase();
        userDomain.setEmail(email);
        var entity = convertDomainToEntity(userDomain);
        entity.setActive(true);
        userRepository.save(entity);

        registerSecurityEvent(SecurityEvents.CREATE_USER, "Anonymous", email, "/api/auth/signup");

        return convertEntityToDomain(entity);
    }

    public Map<String, String> changePassword(String email, String newPassword, PasswordEncoder encoder) {
        Map<String, String> statusMap = new ConcurrentHashMap<>(2);
        var entity = userRepository.findByEmailIgnoreCase(email);
        entity.setPassword(encoder.encode(newPassword));
        userRepository.save(entity);
        statusMap.put("email", email);
        statusMap.put("status", "The password has been updated successfully");
        registerSecurityEvent(SecurityEvents.CHANGE_PASSWORD, email,  email, "/api/auth/changepass");
        return statusMap;
    }

    @Transactional
    public UserDomain updateUserRole(String targetEmail, String currentUserEmail, String role, String operation) {
        String path = "/api/admin/user/role";
        var user = userRepository.findByEmailIgnoreCase(targetEmail);
        var roleList = user.getRoles();
        if (operation.equals("GRANT")) {
            roleList.add("ROLE_" + role);
            registerSecurityEvent(SecurityEvents.GRANT_ROLE, currentUserEmail
                    , "Grant role " + role + " to " + targetEmail, path);
        } else {
            roleList.remove("ROLE_" + role);
            registerSecurityEvent(SecurityEvents.REMOVE_ROLE, currentUserEmail
                    , "Remove role " + role + " from " + targetEmail, path);
        }

        user.setRoles(roleList.stream()
                .sorted()
                .toList());
        return convertEntityToDomain(user);
    }

    public List<UserDomain> listUsers() {
        return userRepository.findByOrderById().stream()
                .map(this::convertEntityToDomain)
                .toList();
    }

    @Transactional
    public Map<String, String> deleteUserByEmail(String targetEmail, String currentUserEmail) {
        userRepository.deleteByEmailIgnoreCase(targetEmail);
        Map<String, String> deleteMsg = new ConcurrentHashMap<>(2);
        deleteMsg.put("user", targetEmail);
        deleteMsg.put("status", "Deleted successfully!");
        registerSecurityEvent(SecurityEvents.DELETE_USER, currentUserEmail,  targetEmail, "/api/admin/user");
        return deleteMsg;
    }

    @Transactional
    public Map<String, String> updateUserAccess(String targetEmail, String currentUserEmail, String operation) {
        var user = userRepository.findByEmailIgnoreCase(targetEmail);
        String path = "/api/admin/user/access";
        String status = null;
        switch (operation.toUpperCase()) {
            case "LOCK"   -> {
                user.setActive(false);
                status = "locked";
                registerSecurityEvent(SecurityEvents.LOCK_USER, currentUserEmail
                        ,  "Lock user " + targetEmail, path);
            }
            case "UNLOCK" -> {
                user.setActive(true);
                status = "unlocked";
                registerSecurityEvent(SecurityEvents.UNLOCK_USER, currentUserEmail
                        ,  "Unlock user " + targetEmail, path);
            }
        }
        userRepository.save(user);
        Map<String, String> statusMap = new ConcurrentHashMap<>(1);
        statusMap.put("status", "User " + targetEmail + " " + status + "!");
        return statusMap;
    }


    public void registerSecurityEvent(SecurityEvents eventName, String subject, String object, String path) {
        var eventEntity = new SecurityEventEntity();
        eventEntity.setAction(eventName);
        eventEntity.setSubject(subject);
        eventEntity.setObject(object);
        eventEntity.setPath(path);
        securityEventsRepository.save(eventEntity);
    }

    @Transactional
    public void increaseFailedAttempts(UserEntity user) {
        int newFailAttempts = user.getFailedAttempt() + 1;
        userRepository.updateFailedAttempts(newFailAttempts, user.getEmail());
    }

    @Transactional
    public void resetFailedAttempts(String email) {
        userRepository.updateFailedAttempts(0, email);
    }

    @Transactional
    public void lock(UserEntity user) {
        user.setActive(false);
        userRepository.save(user);
    }

    public long numberOfUsers() {
        return userRepository.count();
    }

    public UserEntity findUserEntityByEmailIgnoreCase(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    public UserDomain findUserByEmailIgnoreCase(String email) {
        return convertEntityToDomain(userRepository.findByEmailIgnoreCase(email));
    }

    public boolean existsByEmailIgnoreCase(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    public boolean isBreachedPassword(String password) {
        return BREACHED_PASSWORDS_LIST.contains(password);
    }


    // Service utility methods
    public UserDomain convertEntityToDomain(UserEntity userEntity) {
        return serviceModelMapper.map(userEntity, UserDomain.class);
    }

    public UserEntity convertDomainToEntity(UserDomain userDomain) {
        return serviceModelMapper.map(userDomain, UserEntity.class);
    }
}