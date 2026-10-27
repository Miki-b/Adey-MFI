package et.adey.mfi;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static et.adey.mfi.TestOfficers.SAMUEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exports the OpenAPI spec to backend/openapi.json on every test run, so the file always matches
 * the code. The Flutter app generates its typed Dart client from that file. If this test changes
 * the file, commit the new version together with the API change.
 */
class OpenApiSpecTest extends ApiTestBase {

    static final Path SPEC = Path.of("openapi.json"); // Maven runs tests in backend/

    @Test
    void exportSpecAndCheckTheEssentials() throws Exception {
        String spec = mvc.perform(get("/v3/api-docs").header("Authorization", bearer(SAMUEL)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String pretty = json.writerWithDefaultPrettyPrinter().writeValueAsString(json.readTree(spec)) + "\n";

        String previous = Files.exists(SPEC) ? Files.readString(SPEC, StandardCharsets.UTF_8) : "";
        if (!previous.equals(pretty)) {
            Files.writeString(SPEC, pretty, StandardCharsets.UTF_8);
            System.out.println("[OpenApiSpecTest] backend/openapi.json was updated - commit it.");
        }

        JsonNode root = json.readTree(pretty);
        assertThat(root.path("components").path("securitySchemes").path("bearerAuth").path("scheme").asString())
                .isEqualTo("bearer");
        JsonNode paths = root.path("paths");
        assertThat(paths.has("/api/me/portfolio")).isTrue();
        assertThat(paths.path("/api/loans").has("get")).isTrue();
        assertThat(paths.path("/api/loans").has("post")).isTrue();
        assertThat(paths.path("/api/clients").has("post")).isTrue();
        assertThat(paths.path("/api/clients/{clientId}").has("put")).isTrue();
        assertThat(paths.path("/api/loans/{loanId}/repayments").path("post").path("parameters").toString())
                .contains("Idempotency-Key");
        // Typed responses (not "object"), so the Dart client gets real classes.
        assertThat(paths.path("/api/loans/{loanId}/repayments").path("post").path("responses").path("200").toString())
                .contains("LoanTransactionResponse");
        assertThat(root.path("servers").get(0).path("url").asString()).isEqualTo("http://localhost:8080");

        // Every operation: a unique, meaningful operationId (= the Dart method name) and documented errors.
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (var path : paths.properties()) {
            for (var op : path.getValue().properties()) {
                String id = op.getValue().path("operationId").asString();
                assertThat(id).as(op.getKey() + " " + path.getKey()).isNotBlank().doesNotMatch(".*_[0-9]+$|^(get|list)[0-9]+$");
                assertThat(ids.add(id)).as("duplicate operationId " + id).isTrue();
                assertThat(op.getValue().path("responses").has("4XX")).as(id + " documents 4XX").isTrue();
            }
        }
        assertThat(ids).contains("listClients", "getClientLoans", "previewSchedule", "getPortfolio", "recordRepayment");

        JsonNode schemas = root.path("components").path("schemas");
        // LoanDetails.summary is the loan totals, not the reconciliation summary (both Java records are "Summary").
        assertThat(schemas.path("LoanDetails").path("properties").path("summary").path("$ref").asString())
                .endsWith("/LoanTotals");
        assertThat(schemas.path("LoanTotals").path("properties").has("principalOutstanding")).isTrue();
        assertThat(schemas.path("ReconciliationReport").path("properties").path("summary").path("$ref").asString())
                .endsWith("/ReconciliationSummary");
        // Required = always sent and non-null; @Nullable components stay optional.
        assertThat(schemas.path("Portfolio").path("required").toString()).contains("\"scope\"", "\"par\"")
                .doesNotContain("\"officers\"");
        assertThat(schemas.path("ClientDetails").path("required").toString()).contains("\"displayName\"")
                .doesNotContain("\"mobileNo\"");
        // Requests keep their Bean Validation rules only.
        assertThat(schemas.path("RepaymentRequest").path("required").toString()).isEqualTo("[\"amount\"]");
        // Money is an exact decimal; the role is an enum.
        assertThat(schemas.path("Par").path("properties").path("par30").path("format").asString()).isEqualTo("decimal");
        assertThat(schemas.path("RepaymentRequest").path("properties").path("amount").path("format").asString())
                .isEqualTo("decimal");
        assertThat(schemas.path("Scope").path("properties").path("role").toString()).contains("MANAGER", "OFFICER");
        assertThat(schemas.path("Problem").path("properties").path("code").path("enum").toString())
                .contains("NOT_AN_OFFICER", "PHONE_METHOD_MISMATCH");
    }
}
