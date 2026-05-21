package com.hermes.finance.domain.user;

import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    private final UserRepositoryPort userRepository;
    private final AppLogger appLogger;

    public UserService(UserRepositoryPort userRepository, AppLogger appLogger) {
        this.userRepository = userRepository;
        this.appLogger = appLogger;
    }

    /**
     * Cria um usuario local a partir do evento user.created.
     * Idempotente: se o externalAuthId ja existe, nao faz nada.
     */
    public void createFromExternalProvider(UserSyncData data) {
        String externalAuthId = data.externalAuthId();
        if (userRepository.findByExternalAuthId(externalAuthId).isPresent()) {
            return;
        }

        User user = new User();
        user.setExternalAuthId(externalAuthId);
        user.setEmail(data.email());
        user.setName(data.fullName());
        userRepository.save(user);

        appLogger.info(LoggingConstants.USER_REGISTERED, Map.of());
    }

    /**
     * Atualiza email e nome locais a partir do evento user.updated.
     */
    public void updateFromExternalProvider(UserSyncData data) {
        String externalAuthId = data.externalAuthId();
        if (userRepository.findByExternalAuthId(externalAuthId).isEmpty()) {
            return;
        }
        userRepository.updateProfile(externalAuthId, data.email(), data.fullName());
        appLogger.info(LoggingConstants.USER_UPDATED, Map.of());
    }

    /**
     * Anonimiza dados do usuario no evento user.deleted.
     */
    public void anonymize(String externalAuthId) {
        userRepository.anonymize(externalAuthId);
        appLogger.info(LoggingConstants.USER_DELETED, Map.of());
    }
}
