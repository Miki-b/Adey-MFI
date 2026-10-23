package et.adey.mfi.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;


/**
 * The security filter chain: every HTTP request passes through it BEFORE reaching a controller.
 *
 * 1. Bearer token: "Authorization: Bearer <jwt>" is read and verified by the JwtDecoder
 *    (signature, issuer, expiry). Valid -> the request is authenticated as an Officer.
 * 2. Rules below decide which paths need authentication. Role rules (manager-only actions)
 *    are checked in the service layer so that refused attempts also reach the audit log.
 * 3. No server session (STATELESS): every request must carry its token. No session cookie
 *    also means no CSRF risk, which is why CSRF protection is switched off.
 */
@Configuration
// Only in a web application: the seed profile runs without a web server and needs no filter chain.
@ConditionalOnWebApplication
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityProperties props,
                                                   OfficerJwtConverter converter, ObjectMapper objectMapper)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // Getting a token must work without one.
                    auth.requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll();
                    // Spring's error page (used when a request fails before reaching us).
                    auth.requestMatchers("/error").permitAll();
                    if (props.publicDocs()) {
                        // Local profile only: Swagger UI, OpenAPI JSON and health without a token.
                        auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/actuator/health/**").permitAll();
                    }
                    // Everything else needs a valid token.
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(rs -> rs
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter))
                        .authenticationEntryPoint(unauthorized(objectMapper))
                        .accessDeniedHandler(forbidden(objectMapper)))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(unauthorized(objectMapper))
                        .accessDeniedHandler(forbidden(objectMapper)));
        return http.build();
    }

    /** 401: no token, or the token is invalid/expired. Same ProblemDetail JSON as our other errors. */
    private static AuthenticationEntryPoint unauthorized(ObjectMapper objectMapper) {
        return (request, response, ex) -> {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                    "Missing, invalid or expired access token. Log in again or refresh the token.");
            problem.setTitle("Unauthorized");
            problem.setProperty("code", "UNAUTHENTICATED");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader("WWW-Authenticate", "Bearer");
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), problem);
        };
    }

    private static AccessDeniedHandler forbidden(ObjectMapper objectMapper) {
        return (request, response, ex) -> {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
                    "You are not allowed to do this.");
            problem.setTitle("Forbidden");
            problem.setProperty("code", "FORBIDDEN");
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), problem);
        };
    }
}
