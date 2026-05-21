package com.hermes.finance.domain.user;

import com.hermes.finance.domain.identity.IdentityEventType;
import com.hermes.finance.domain.identity.IdentityUserEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserIdentityEventHandlerTest {

    @Mock
    private UserService userService;

    @Test
    void shouldIgnoreNullEventOrNullType() {
        UserIdentityEventHandler handler = new UserIdentityEventHandler(userService);

        handler.handle(null);
        handler.handle(new IdentityUserEvent(null, null));

        verifyNoInteractions(userService);
    }

    @Test
    void shouldDelegateCreateUpdateDeleteAndIgnoreUnsupported() {
        UserIdentityEventHandler handler = new UserIdentityEventHandler(userService);
        UserSyncData data = new UserSyncData("ext-1", "a@b.com", "A B");

        handler.handle(new IdentityUserEvent(IdentityEventType.USER_CREATED, data));
        handler.handle(new IdentityUserEvent(IdentityEventType.USER_UPDATED, data));
        handler.handle(new IdentityUserEvent(IdentityEventType.USER_DELETED, data));
        handler.handle(new IdentityUserEvent(IdentityEventType.USER_DELETED, null));
        handler.handle(new IdentityUserEvent(IdentityEventType.UNSUPPORTED, data));

        verify(userService).createFromExternalProvider(data);
        verify(userService).updateFromExternalProvider(data);
        verify(userService).anonymize("ext-1");
        verifyNoMoreInteractions(userService);
    }
}
