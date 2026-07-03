package com.hermes.finance.domain.share;

import com.hermes.finance.dto.request.ShareCreateRequest;
import com.hermes.finance.dto.response.PublicShareResponse;
import com.hermes.finance.dto.response.ShareTokenResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class ShareTokenController {

    private final ShareTokenService service;

    public ShareTokenController(ShareTokenService service) {
        this.service = service;
    }

    @PostMapping("/api/share")
    @ResponseStatus(HttpStatus.CREATED)
    public ShareTokenResponse create(@Valid @RequestBody ShareCreateRequest request) {
        return service.create(request);
    }

    @GetMapping("/api/share")
    public List<ShareTokenResponse> list() {
        return service.list();
    }

    @DeleteMapping("/api/share/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@PathVariable UUID id) {
        service.revoke(id);
    }

    @GetMapping("/api/public/share/{tokenRaw}")
    public PublicShareResponse publicStatement(@PathVariable String tokenRaw) {
        return service.getPublicStatement(tokenRaw);
    }
}
