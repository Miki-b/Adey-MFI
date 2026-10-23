package et.adey.mfi.fineract;

import com.fasterxml.jackson.databind.ObjectMapper;
import et.adey.mfi.config.FineractConfig;
import et.adey.mfi.config.FineractProperties;
import et.adey.mfi.fineract.model.FineractClientData;
import et.adey.mfi.fineract.model.FineractOffice;
import et.adey.mfi.fineract.model.FineractPage;
import et.adey.mfi.fineract.model.FineractScheduleRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests FineractClient against a fake HTTP server (MockRestServiceServer):
 * we check what it sends and how it reads responses, with no real Fineract running.
 */
class FineractClientTest {

    private static final String BASE = "https://fineract.test/fineract-provider/api/v1";

    private MockRestServiceServer server;
    private FineractClient client;

    @BeforeEach
    void setUp() {
        FineractProperties props = new FineractProperties(BASE, "default", "mifos", "password",
                Duration.ofSeconds(5), Duration.ofSeconds(30), false);
        RestClient.Builder builder = FineractConfig.applyDefaults(RestClient.builder(), props);
        server = MockRestServiceServer.bindTo(builder).build();
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        client = new FineractClient(builder.build(), objectMapper);
    }

    @Test
    void sendsTenantAndBasicAuthHeadersAndReadsOffices() {
        String basic = "Basic " + Base64.getEncoder().encodeToString("mifos:password".getBytes());
        server.expect(requestTo(BASE + "/offices"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Fineract-Platform-TenantId", "default"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, basic))
                .andRespond(withSuccess("""
                        [{"id":1,"name":"Head Office","openingDate":[2009,1,1],"hierarchy":"."},
                         {"id":2,"name":"Bishoftu Branch","openingDate":[2026,10,3],"parentId":1,"extra":"ignored"}]
                        """, MediaType.APPLICATION_JSON));

        List<FineractOffice> offices = client.getOffices();

        server.verify();
        assertThat(offices).hasSize(2);
        assertThat(offices.get(1).name()).isEqualTo("Bishoftu Branch");
        assertThat(offices.get(1).parentId()).isEqualTo(1L);
        assertThat(offices.get(1).openingDate()).containsExactly(2026, 10, 3);
    }

    @Test
    void passesOnlyGivenClientFilters() {
        server.expect(requestToUriTemplate(BASE + "/clients?offset=20&limit=10&orderBy=displayName&sortOrder=ASC&staffId=1"))
                .andExpect(queryParam("staffId", "1"))
                .andRespond(withSuccess("""
                        {"totalFilteredRecords":21,"pageItems":[{"id":1,"displayName":"Yohannes Tekle","active":true}]}
                        """, MediaType.APPLICATION_JSON));

        FineractPage<FineractClientData> page = client.searchClients(null, null, 1L, 20, 10);

        server.verify();
        assertThat(page.totalFilteredRecords()).isEqualTo(21);
        assertThat(page.pageItems()).extracting(FineractClientData::displayName).containsExactly("Yohannes Tekle");
    }

    @Test
    void turnsFineractValidationErrorIntoApiException() {
        server.expect(requestTo(BASE + "/loans?command=calculateLoanSchedule"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("""
                        {"httpStatusCode":"400","defaultUserMessage":"Validation errors exist.",
                         "errors":[{"parameterName":"principal",
                                    "defaultUserMessage":"The parameter `principal` amount 90000.0 must be between 5000.00 and 50000.00 ."}]}
                        """));

        FineractScheduleRequest request = new FineractScheduleRequest(1L, 1L, new BigDecimal("90000"), 12, 2, 12, 1, 2,
                new BigDecimal("18"), 1, 0, 1, "mifos-standard-strategy", "23 October 2026", "23 October 2026",
                "individual", "dd MMMM yyyy", "en");

        assertThatThrownBy(() -> client.calculateLoanSchedule(request))
                .isInstanceOfSatisfying(FineractApiException.class, ex -> {
                    assertThat(ex.getFineractStatus()).isEqualTo(400);
                    assertThat(ex.getError().defaultUserMessage()).isEqualTo("Validation errors exist.");
                    assertThat(ex.getError().errors()).singleElement()
                            .satisfies(fe -> assertThat(fe.parameterName()).isEqualTo("principal"));
                });
    }

    @Test
    void nonJsonErrorBodyStillGivesApiException() {
        server.expect(requestTo(BASE + "/clients/1"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("<html>nope</html>"));

        assertThatThrownBy(() -> client.getClient(1))
                .isInstanceOfSatisfying(FineractApiException.class,
                        ex -> assertThat(ex.getFineractStatus()).isEqualTo(401));
    }

    @Test
    void connectionFailureBecomesUnavailableException() {
        server.expect(requestTo(BASE + "/offices"))
                .andRespond(withException(new IOException("Connection refused")));

        assertThatThrownBy(() -> client.getOffices())
                .isInstanceOf(FineractUnavailableException.class)
                .hasMessageContaining("Connection refused");
    }
}
