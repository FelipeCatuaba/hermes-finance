package com.hermes.finance.domain.user;

import com.hermes.finance.dto.request.ClerkWebhookEvent;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AppLogger appLogger;

    public UserService(UserRepository userRepository, AppLogger appLogger) {
        this.userRepository = userRepository;
        this.appLogger = appLogger;
    }

    /**
     * Cria um usuário local a partir do evento user.created do Clerk.
     * Idempotente: se o clerk_id já existe, não faz nada.
     */
    public void createFromClerk(ClerkWebhookEvent.Data data) {
        String clerkId = data.getId();
        if (userRepository.findByClerkId(clerkId).isPresent()) {
            return; // já sincronizado — idempotente
        }

        User user = new User();
        user.setClerkId(clerkId);
        user.setEmail(data.getPrimaryEmail());
        user.setName(data.getFullName());
        userRepository.save(user);

        appLogger.info(LoggingConstants.USER_REGISTERED, Map.of());
    }

    /**
     * Atualiza e-mail e nome locais a partir do evento user.updated do Clerk.
     * Ignora se o usuário ainda não foi sincronizado (aguarda user.created).
     */
    public void updateFromClerk(ClerkWebhookEvent.Data data) {
        String clerkId = data.getId();
        if (userRepository.findByClerkId(clerkId).isEmpty()) {
            return;
        }
        userRepository.updateProfile(clerkId, data.getPrimaryEmail(), data.getFullName());
        appLogger.info(LoggingConstants.USER_UPDATED, Map.of());
    }

    /**
     * Anonimiza os dados do usuário quando o Clerk emite user.deleted.
     * Mantém o registro por integridade referencial com gastos e receitas.
     */
    public void anonymize(String clerkId) {
        userRepository.anonymize(clerkId);
        appLogger.info(LoggingConstants.USER_DELETED, Map.of());
    }
}
