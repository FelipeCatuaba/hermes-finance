package com.hermes.finance.domain.user;

import com.hermes.finance.domain.identity.IdentityEventHandler;
import com.hermes.finance.domain.identity.IdentityEventType;
import com.hermes.finance.domain.identity.IdentityUserEvent;
import org.springframework.stereotype.Component;

@Component
public class UserIdentityEventHandler implements IdentityEventHandler {

    private final UserService userService;

    public UserIdentityEventHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(IdentityUserEvent event) {
        if (event == null || event.type() == null) {
            return;
        }

        switch (event.type()) {
            case USER_CREATED -> userService.createFromExternalProvider(event.data());
            case USER_UPDATED -> userService.updateFromExternalProvider(event.data());
            case USER_DELETED -> {
                if (event.data() != null) {
                    userService.anonymize(event.data().externalAuthId());
                }
            }
            case UNSUPPORTED -> {
                // no-op
            }
        }
    }
}

