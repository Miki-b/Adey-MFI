package et.adey.mfi.auth;

import et.adey.mfi.security.Officer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Login, token refresh, logout, and "who am I". login/refresh/logout work without an access token. */
@Tag(name = "Auth")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Log in with Fineract username + password; returns access and refresh tokens")
    @SecurityRequirements // no bearer token needed
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        // getRemoteAddr = the direct caller. Behind a reverse proxy this would be the proxy's IP;
        // then read X-Forwarded-For from a trusted proxy instead.
        return authService.login(request.username(), request.password(), http.getRemoteAddr());
    }

    @Operation(summary = "Exchange a refresh token for a new token pair (the old refresh token stops working)")
    @SecurityRequirements
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @Operation(summary = "Log out: revoke the refresh token (the access token expires on its own within 15 min)")
    @SecurityRequirements
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "The officer the access token belongs to")
    @GetMapping("/me")
    public OfficerResponse me(@AuthenticationPrincipal Officer officer) {
        return OfficerResponse.of(officer);
    }
}
