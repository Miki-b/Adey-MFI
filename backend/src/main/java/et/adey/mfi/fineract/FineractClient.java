package et.adey.mfi.fineract;

import et.adey.mfi.fineract.model.FineractClientAccounts;
import et.adey.mfi.fineract.model.FineractClientData;
import et.adey.mfi.fineract.model.FineractError;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractLoanProduct;
import et.adey.mfi.fineract.model.FineractLoanSchedule;
import et.adey.mfi.fineract.model.FineractOffice;
import et.adey.mfi.fineract.model.FineractPage;
import et.adey.mfi.fineract.model.FineractCommandResult;
import et.adey.mfi.fineract.model.FineractLoanRequest;
import et.adey.mfi.fineract.model.FineractLoanTransaction;
import et.adey.mfi.fineract.model.FineractPaymentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

/**
 * The ONLY class that talks to Fineract. Returns Fineract's raw shapes (fineract.model);
 * the feature services turn those into our clean API DTOs.
 * Base URL, auth and tenant header are already set on the injected RestClient (see FineractConfig).
 */
@Slf4j
@Component
public class FineractClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public FineractClient(RestClient fineractRestClient, ObjectMapper objectMapper) {
        this.restClient = fineractRestClient;
        this.objectMapper = objectMapper;
    }

    // ---- Offices -------------------------------------------------------------------------

    public List<FineractOffice> getOffices() {
        return call(() -> restClient.get()
                .uri("/offices")
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::throwApiError)
                .body(new ParameterizedTypeReference<List<FineractOffice>>() { }));
    }

    // ---- Loan products -------------------------------------------------------------------

    public List<FineractLoanProduct> getLoanProducts() {
        return call(() -> restClient.get()
                .uri("/loanproducts")
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::throwApiError)
                .body(new ParameterizedTypeReference<List<FineractLoanProduct>>() { }));
    }

    public FineractLoanProduct getLoanProduct(long productId) {
        return call(() -> restClient.get()
                .uri("/loanproducts/{id}", productId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::throwApiError)
                .body(FineractLoanProduct.class));
    }

    // ---- Clients -------------------------------------------------------------------------

    /**
     * GET /clients with optional filters. displayName is a substring match in Fineract
     * (case-sensitive on PostgreSQL). Null filters are left out of the URL.
     */
    public FineractPage<FineractClientData> searchClients(String displayName, Long officeId, Long staffId,
                                                          int offset, int limit) {
        return call(() -> restClient.get()
                .uri(b -> {
                    UriBuilder u = b.path("/clients")
                            .queryParam("offset", offset)
                            .queryParam("limit", limit)
                            .queryParam("orderBy", "displayName")
                            .queryParam("sortOrder", "ASC");
                    if (displayName != null) u.queryParam("displayName", displayName);
                    if (officeId != null) u.queryParam("officeId", officeId);
                    if (staffId != null) u.queryParam("staffId", staffId);
                    return u.build();
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::throwApiError)
                .body(new ParameterizedTypeReference<FineractPage<FineractClientData>>() { }));
    }

    public FineractClientData getClient(long clientId) {
        return call(() -> restClient.get()
                .uri("/clients/{id}", clientId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::throwApiError)
                .body(FineractClientData.class));
    }

    public FineractClientAccounts getClientAccounts(long clientId) {
        return call(() -> restClient.get()
                .uri("/clients/{id}/accounts", clientId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::throwApiError)
                .body(FineractClientAccounts.class));
    }

    // ---- Loans ---------------------------------------------------------------------------

    /** One loan including its repayment schedule (needed for installments paid / next due). */
    public FineractLoan getLoanWithSchedule(long loanId) {
        return getLoan(loanId, "repaymentSchedule");
    }

    /** One loan with schedule AND transactions (the loan details screen). */
    public FineractLoan getLoanWithScheduleAndTransactions(long loanId) {
        return getLoan(loanId, "repaymentSchedule,transactions");
    }

    private FineractLoan getLoan(long loanId, String associations) {
        return call(() -> restClient.get()
                .uri("/loans/{id}?associations={associations}", loanId, associations)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::throwApiError)
                .body(FineractLoan.class));
    }

    public FineractLoanTransaction getLoanTransaction(long loanId, long transactionId) {
        return call(() -> restClient.get()
                .uri("/loans/{loanId}/transactions/{txId}", loanId, transactionId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::throwApiError)
                .body(FineractLoanTransaction.class));
    }

    /** Submit a loan application: POST /loans. resourceId of the result is the new loan id. */
    public FineractCommandResult submitLoanApplication(FineractLoanRequest request) {
        return post("/loans", request);
    }

    /**
     * A state change on a loan: POST /loans/{id}?command=approve|undoApproval|reject|disburse.
     * The body type depends on the command (see FineractLoanCommands).
     */
    public FineractCommandResult loanCommand(long loanId, String command, Object body) {
        return post("/loans/" + loanId + "?command=" + command, body);
    }

    /** POST /loans/{id}/transactions?command=repayment. resourceId is the new transaction id. */
    public FineractCommandResult repayLoan(long loanId, Object body) {
        return post("/loans/" + loanId + "/transactions?command=repayment", body);
    }

    private FineractCommandResult post(String uri, Object body) {
        return call(() -> restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::throwApiError)
                .body(FineractCommandResult.class));
    }

    // ---- Payment types -------------------------------------------------------------------

    public List<FineractPaymentType> getPaymentTypes() {
        return call(() -> restClient.get()
                .uri("/paymenttypes")
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::throwApiError)
                .body(new ParameterizedTypeReference<List<FineractPaymentType>>() { }));
    }

    /** Preview a schedule without saving anything: POST /loans?command=calculateLoanSchedule. */
    public FineractLoanSchedule calculateLoanSchedule(FineractLoanRequest request) {
        return call(() -> restClient.post()
                .uri("/loans?command=calculateLoanSchedule")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::throwApiError)
                .body(FineractLoanSchedule.class));
    }

    // ---- Error handling ------------------------------------------------------------------

    /** Network-level failures (refused, timeout, TLS) become FineractUnavailableException. */
    private <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (ResourceAccessException e) {
            log.error("Cannot reach Fineract: {}", e.getMessage());
            throw new FineractUnavailableException("Cannot reach Fineract: " + e.getMessage(), e);
        }
    }

    /** Fineract answered with 4xx/5xx: parse its JSON error body and throw it as our exception. */
    private void throwApiError(HttpRequest request, ClientHttpResponse response)
            throws IOException {
        int status = response.getStatusCode().value();
        FineractError error = parseError(response);
        log.warn("Fineract {} {} -> {} {}", request.getMethod(), request.getURI().getPath(), status,
                error.defaultUserMessage());
        throw new FineractApiException(status, error);
    }

    private FineractError parseError(ClientHttpResponse response) {
        try {
            byte[] body = response.getBody().readAllBytes();
            if (body.length > 0) {
                FineractError parsed = objectMapper.readValue(body, FineractError.class);
                if (parsed.defaultUserMessage() != null) {
                    return parsed;
                }
            }
        } catch (IOException | JacksonException e) {
            // Not JSON (e.g. an HTML error page). Fall through to a generic message.
        }
        String reason;
        try {
            reason = response.getStatusText();
        } catch (IOException e) {
            reason = "";
        }
        return new FineractError(null, "Fineract error " + reason, null, List.of());
    }
}
