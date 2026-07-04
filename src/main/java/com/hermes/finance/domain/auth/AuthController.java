package com.hermes.finance.domain.auth;

import com.hermes.finance.domain.user.User;
import com.hermes.finance.dto.request.AuthLoginRequest;
import com.hermes.finance.dto.request.AuthLogoutRequest;
import com.hermes.finance.dto.request.AuthRefreshRequest;
import com.hermes.finance.dto.request.AuthRegisterRequest;
import com.hermes.finance.dto.response.AuthResponse;
import com.hermes.finance.dto.response.AuthUserResponse;
import com.hermes.finance.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final SecurityUtils securityUtils;

    public AuthController(AuthService authService, SecurityUtils securityUtils) {
        this.authService = authService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody AuthRegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody AuthRefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody(required = false) AuthLogoutRequest request) {
        User user = securityUtils.getCurrentUser();
        authService.logout(user.getId(), request == null ? null : request.refreshToken());
    }

    @GetMapping("/me")
    public AuthUserResponse me() {
        return authService.toUserResponse(securityUtils.getCurrentUser());
    }
}
