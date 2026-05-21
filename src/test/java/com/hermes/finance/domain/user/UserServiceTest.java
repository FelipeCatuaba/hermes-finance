package com.hermes.finance.domain.user;

import com.hermes.finance.dto.request.ClerkWebhookEvent;
import com.hermes.finance.logging.AppLogger;
import com.hermes.finance.logging.LoggingConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppLogger appLogger;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserFromClerkWhenNotExists() {
        ClerkWebhookEvent.Data data = clerkData("user_new", "new@hermes.com", "Novo Usuário");
        when(userRepository.findByClerkId("user_new")).thenReturn(Optional.empty());

        userService.createFromClerk(data);

        verify(userRepository).save(any(User.class));
        verify(appLogger).info(eq(LoggingConstants.USER_REGISTERED), eq(Map.of()));
    }

    @Test
    void shouldSkipCreateWhenClerkUserAlreadyExists() {
        ClerkWebhookEvent.Data data = clerkData("user_existing", "exists@hermes.com", "Existente");
        when(userRepository.findByClerkId("user_existing")).thenReturn(Optional.of(new User()));

        userService.createFromClerk(data);

        verify(userRepository, never()).save(any(User.class));
        verify(appLogger, never()).info(any(), any());
    }

    @Test
    void shouldUpdateUserFromClerkWhenExists() {
        ClerkWebhookEvent.Data data = clerkData("user_upd", "new@hermes.com", "Nome Novo");
        when(userRepository.findByClerkId("user_upd")).thenReturn(Optional.of(new User()));

        userService.updateFromClerk(data);

        verify(userRepository).updateProfile("user_upd", "new@hermes.com", "Nome Novo");
        verify(appLogger).info(eq(LoggingConstants.USER_UPDATED), eq(Map.of()));
    }

    @Test
    void shouldSkipUpdateWhenUserNotYetSynced() {
        ClerkWebhookEvent.Data data = clerkData("user_missing", "x@hermes.com", "X");
        when(userRepository.findByClerkId("user_missing")).thenReturn(Optional.empty());

        userService.updateFromClerk(data);

        verify(userRepository, never()).updateProfile(any(), any(), any());
    }

    @Test
    void shouldAnonymizeUserAndLogDeletion() {
        userService.anonymize("user_deleted");

        verify(userRepository).anonymize("user_deleted");
        verify(appLogger).info(eq(LoggingConstants.USER_DELETED), eq(Map.of()));
    }

    private static ClerkWebhookEvent.Data clerkData(String id, String email, String fullName) {
        ClerkWebhookEvent.EmailAddress emailAddress = new ClerkWebhookEvent.EmailAddress();
        emailAddress.setEmailAddress(email);
        emailAddress.setPrimary(true);

        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();
        data.setId(id);
        data.setEmailAddresses(java.util.List.of(emailAddress));
        data.setFirstName(fullName.split(" ")[0]);
        data.setLastName(fullName.contains(" ") ? fullName.substring(fullName.indexOf(' ') + 1) : "");
        return data;
    }
}
