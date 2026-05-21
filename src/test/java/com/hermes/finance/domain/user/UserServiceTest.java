package com.hermes.finance.domain.user;

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
    void shouldCreateUserFromExternalProviderWhenNotExists() {
        UserSyncData data = new UserSyncData("user_new", "new@hermes.com", "Novo Usuario");
        when(userRepository.findByExternalAuthId("user_new")).thenReturn(Optional.empty());

        userService.createFromExternalProvider(data);

        verify(userRepository).save(any(User.class));
        verify(appLogger).info(eq(LoggingConstants.USER_REGISTERED), eq(Map.of()));
    }

    @Test
    void shouldSkipCreateWhenExternalUserAlreadyExists() {
        UserSyncData data = new UserSyncData("user_existing", "exists@hermes.com", "Existente");
        when(userRepository.findByExternalAuthId("user_existing")).thenReturn(Optional.of(new User()));

        userService.createFromExternalProvider(data);

        verify(userRepository, never()).save(any(User.class));
        verify(appLogger, never()).info(any(), any());
    }

    @Test
    void shouldUpdateUserFromExternalProviderWhenExists() {
        UserSyncData data = new UserSyncData("user_upd", "new@hermes.com", "Nome Novo");
        when(userRepository.findByExternalAuthId("user_upd")).thenReturn(Optional.of(new User()));

        userService.updateFromExternalProvider(data);

        verify(userRepository).updateProfile("user_upd", "new@hermes.com", "Nome Novo");
        verify(appLogger).info(eq(LoggingConstants.USER_UPDATED), eq(Map.of()));
    }

    @Test
    void shouldSkipUpdateWhenUserNotYetSynced() {
        UserSyncData data = new UserSyncData("user_missing", "x@hermes.com", "X");
        when(userRepository.findByExternalAuthId("user_missing")).thenReturn(Optional.empty());

        userService.updateFromExternalProvider(data);

        verify(userRepository, never()).updateProfile(any(), any(), any());
    }

    @Test
    void shouldAnonymizeUserAndLogDeletion() {
        userService.anonymize("user_deleted");

        verify(userRepository).anonymize("user_deleted");
        verify(appLogger).info(eq(LoggingConstants.USER_DELETED), eq(Map.of()));
    }
}
