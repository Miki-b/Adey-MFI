package et.adey.mfi.portfolio;

import et.adey.mfi.TestJson;
import et.adey.mfi.config.FineractConfig;
import et.adey.mfi.config.FineractProperties;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.security.AccessPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static et.adey.mfi.TestOfficers.MERON;
import static et.adey.mfi.TestOfficers.SELAMAWIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Which Fineract calls the portfolio needs. The point: 1 client-list call + 1 loan-list call +
 * 1 schedule call per ACTIVE loan in scope, and nothing for loans outside the caller's scope.
 */
class PortfolioReaderTest {

    private static final String BASE = "https://fineract.test/fineract-provider/api/v1";
    private static final String CLIENTS = BASE + "/clients?offset=0&limit=200&orderBy=displayName&sortOrder=ASC";
    private static final String LOANS = BASE + "/loans?offset=0&limit=200&orderBy=id&sortOrder=ASC";

    private MockRestServiceServer server;
    private PortfolioReader reader;

    @BeforeEach
    void setUp() {
        FineractProperties props = new FineractProperties(BASE, "default", "mifos", "password",
                Duration.ofSeconds(5), Duration.ofSeconds(30), false);
        RestClient.Builder builder = FineractConfig.applyDefaults(RestClient.builder(), props);
        // Schedules are fetched in parallel, so their order is not fixed.
        server = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        reader = new PortfolioReader(new FineractClient(builder.build(), TestJson.mapper()), new AccessPolicy());
    }

    @Test
    void managerLoadsOfficeClientsAllLoansAndOnlyActiveSchedules() {
        server.expect(ExpectedCount.once(), requestTo(CLIENTS + "&officeId=3"))
                .andRespond(json("fineract/portfolio/clients-adama.json"));
        server.expect(ExpectedCount.once(), requestTo(LOANS))
                .andRespond(json("fineract/portfolio/loans-all.json"));
        for (long id : new long[]{6, 7, 9, 11}) { // Adama's ACTIVE loans; nothing for Bishoftu's
            server.expect(ExpectedCount.once(), requestTo(BASE + "/loans/" + id + "?associations=repaymentSchedule"))
                    .andRespond(json("fineract/portfolio/loan-" + id + "-schedule.json"));
        }

        PortfolioReader.Scoped scoped = reader.load(SELAMAWIT, true);

        server.verify();
        assertThat(scoped.clients()).hasSize(7);
        assertThat(scoped.loans()).extracting(v -> v.loan().id()).containsExactlyInAnyOrder(6L, 7L, 8L, 9L, 10L, 11L);
        assertThat(scoped.loans()).filteredOn(LoanView::isActive).allSatisfy(v -> assertThat(v.schedule()).isNotNull());
    }

    @Test
    void officerScopeUsesStaffIdAndSkipsSchedulesWhenNotNeeded() {
        server.expect(ExpectedCount.once(), requestTo(CLIENTS + "&officeId=3&staffId=4"))
                .andRespond(withSuccess("""
                        {"totalFilteredRecords": 1, "pageItems": [{"id": 8, "officeId": 3, "staffId": 4, "displayName": "Tigist Haile"}]}
                        """, MediaType.APPLICATION_JSON));
        server.expect(ExpectedCount.once(), requestTo(LOANS))
                .andRespond(json("fineract/portfolio/loans-all.json"));
        // withSchedules=false (e.g. the PENDING queue): no per-loan calls at all.

        PortfolioReader.Scoped scoped = reader.load(MERON, false);

        server.verify();
        assertThat(scoped.loans()).extracting(v -> v.loan().id()).containsExactly(6L);
    }

    private static org.springframework.test.web.client.ResponseCreator json(String path) {
        return withSuccess(new ClassPathResource(path), MediaType.APPLICATION_JSON);
    }
}
