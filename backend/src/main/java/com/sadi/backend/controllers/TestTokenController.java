package com.sadi.backend.controllers;

import com.sadi.backend.dtos.requests.TestTokenReq;
import com.sadi.backend.dtos.responses.ValueResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@Profile("test")
@Slf4j
@RequestMapping("/v1/test-tokens")
@RequiredArgsConstructor
public class TestTokenController {

    private final JwtEncoder jwtEncoder;

    @PostMapping
    public ResponseEntity<ValueResponse> createJwt(
            @Valid @RequestBody TestTokenReq req
    ) {
        log.debug("Received test token creation request: {}", req);

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", req.email());
        claims.put("email_verified", req.emailVerified());
        claims.put("name", req.name());
        claims.put("picture", req.picture());
        claims.put("scp", req.scp());

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .claims(c -> c.putAll(claims))
                .issuer("https://test-issuer.com")
                .subject(req.sub())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofDays(7)))
                .build();

        JwsHeader header =JwsHeader.with(() -> "HS256").build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(header, claimsSet))
                .getTokenValue();
        return ResponseEntity.ok(new ValueResponse(token));
    }
}
