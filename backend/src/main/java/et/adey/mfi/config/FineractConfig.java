package et.adey.mfi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

/**
 * Builds the one RestClient used to call Fineract.
 * Every request automatically gets the base URL, Basic auth and the tenant header,
 * so FineractClient only has to say "GET /offices".
 */
@Slf4j
@Configuration
public class FineractConfig {

    public static final String TENANT_HEADER = "Fineract-Platform-TenantId";

    @Bean
    public RestClient fineractRestClient(RestClient.Builder builder, FineractProperties props) {
        // RestClient.Builder comes from Spring Boot, pre-configured with Boot's Jackson ObjectMapper.
        builder.requestFactory(requestFactory(props));
        return applyDefaults(builder, props).build();
    }

    /**
     * Base URL + headers sent on every Fineract call. Separate from the request factory
     * so tests can reuse it with a mock server.
     */
    public static RestClient.Builder applyDefaults(RestClient.Builder builder, FineractProperties props) {
        return builder
                .baseUrl(props.baseUrl())
                .defaultHeader(TENANT_HEADER, props.tenant())
                .defaultHeaders(h -> h.setBasicAuth(props.username(), props.password()))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    }

    private SimpleClientHttpRequestFactory requestFactory(FineractProperties props) {
        SimpleClientHttpRequestFactory factory = props.trustSelfSignedCertificate()
                ? trustAllFactory()
                : new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.connectTimeout());
        factory.setReadTimeout(props.readTimeout());
        return factory;
    }

    /**
     * LOCAL DEVELOPMENT ONLY: accepts any certificate and any hostname.
     * Enabled only by fineract.trust-self-signed-certificate=true (set in application-local.yml).
     * This must never be enabled in production.
     */
    private SimpleClientHttpRequestFactory trustAllFactory() {
        log.warn("Fineract TLS certificate checks are DISABLED (local profile). Never use this in production.");
        SSLSocketFactory socketFactory = trustAllSocketFactory();
        return new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                // Applied per connection, so the rest of the JVM keeps normal TLS checks.
                if (connection instanceof HttpsURLConnection https) {
                    https.setSSLSocketFactory(socketFactory);
                    https.setHostnameVerifier((hostname, session) -> true);
                }
                super.prepareConnection(connection, httpMethod);
            }
        };
    }

    private static SSLSocketFactory trustAllSocketFactory() {
        TrustManager trustAll = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) { }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) { }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{trustAll}, null);
            return context.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not create trust-all SSL context", e);
        }
    }
}
