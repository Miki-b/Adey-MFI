package et.adey.mfi.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS = which browser origins may call this API. Only matters for browser-based clients
 * (e.g. Flutter web during development); native mobile apps ignore CORS.
 * Active only when app.cors.allow-any-origin=true, which only the local profile sets.
 */
@Configuration
@ConditionalOnProperty(name = "app.cors.allow-any-origin", havingValue = "true")
public class LocalCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
