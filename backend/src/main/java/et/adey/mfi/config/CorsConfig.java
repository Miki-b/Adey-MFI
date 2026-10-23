package et.adey.mfi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS = which browser origins (scheme://host:port of a web page) may call this API.
 * Only browsers enforce it (Flutter web); the Flutter mobile app ignores CORS.
 * The list comes from app.cors.allowed-origins; patterns like http://localhost:[*] allow any port.
 * Spring Security picks this bean up via http.cors(...).
 */
@Configuration
public class CorsConfig {

    /**
     * Bound with @ConfigurationProperties, not @Value: @Value cannot read a YAML list
     * (YAML stores it as allowed-origins[0], [1], ...), which silently produced an empty list.
     */
    @ConfigurationProperties(prefix = "app.cors")
    public record CorsProperties(@DefaultValue List<String> allowedOrigins) {
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(props.allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
