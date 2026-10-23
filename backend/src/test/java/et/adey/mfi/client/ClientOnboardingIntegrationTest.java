package et.adey.mfi.client;

import et.adey.mfi.ApiTestBase;
import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.audit.AuditOutcome;
import et.adey.mfi.fineract.FineractApiException;
import et.adey.mfi.fineract.model.FineractCommandResult;
import et.adey.mfi.fineract.model.FineractError;
import et.adey.mfi.fineract.model.FineractStaff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;

import static et.adey.mfi.TestOfficers.HANA;
import static et.adey.mfi.TestOfficers.SAMUEL;
import static et.adey.mfi.TestOfficers.TADESSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** POST / PUT /api/clients: office and officer come from the token; our rules; audit. */
class ClientOnboardingIntegrationTest extends ApiTestBase {

    private static final String NEW_CLIENT = """
            {"firstname": "Selam", "lastname": "Worku", "mobileNo": "0911447788", "gender": "FEMALE",
             "dateOfBirth": "1994-02-11", "externalId": "ET-NID-1994-0211"%s}""";

    @BeforeEach
    void stubFineract() {
        when(fineract.getJson("/codes")).thenReturn(json.readTree("[{\"id\": 4, \"name\": \"Gender\"}]"));
        when(fineract.getJson(eq("/codes/{id}/codevalues"), any())).thenReturn(json.readTree(
                "[{\"id\": 23, \"name\": \"Male\"}, {\"id\": 24, \"name\": \"Female\"}]"));
        when(fineract.createClient(any())).thenReturn(new FineractCommandResult(2L, 16L, null, 16L, Map.of()));
        when(fineract.getClient(16L)).thenReturn(client(16, 2, 1));
    }

    @Test
    void officerCreatesAClientInTheirOfficeAssignedToThemselves() throws Exception {
        create(SAMUEL, "").andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/clients/16"))
                .andExpect(jsonPath("$.id").value(16))
                .andExpect(jsonPath("$.gender").value("Female"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> body = ArgumentCaptor.forClass(Map.class);
        verify(fineract).createClient(body.capture());
        assertThat(body.getValue())
                .containsEntry("officeId", 2L)
                .containsEntry("staffId", 1L)
                .containsEntry("genderId", 24L)
                .containsEntry("mobileNo", "0911447788")
                .containsEntry("externalId", "ET-NID-1994-0211")
                .containsEntry("dateOfBirth", "11 February 1994")
                .containsEntry("active", true)
                .containsKey("activationDate");
        assertThat(auditLog.findAll()).singleElement().satisfies(r -> {
            assertThat(r.getAction()).isEqualTo(AuditAction.CLIENT_CREATE);
            assertThat(r.getOutcome()).isEqualTo(AuditOutcome.SUCCESS);
            assertThat(r.getClientId()).isEqualTo(16L);
        });
    }

    @Test
    void officerCannotAssignTheClientToSomeoneElse() throws Exception {
        create(SAMUEL, ", \"staffId\": 2").andExpect(status().isForbidden());
        verify(fineract, never()).createClient(any());
        assertThat(auditLog.findAll()).singleElement()
                .satisfies(r -> assertThat(r.getOutcome()).isEqualTo(AuditOutcome.DENIED));
    }

    @Test
    void managerMustChooseAnActiveLoanOfficerOfTheirOwnOffice() throws Exception {
        when(fineract.getStaff(2L)).thenReturn(new FineractStaff(2L, "Girma, Hana", 2L, true, true));
        when(fineract.getStaff(3L)).thenReturn(new FineractStaff(3L, "Bekele, Tadesse", 2L, false, true));
        when(fineract.getStaff(4L)).thenReturn(new FineractStaff(4L, "Alemu, Meron", 3L, true, true));

        create(TADESSE, "").andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].field").value("staffId"));
        create(TADESSE, ", \"staffId\": 4").andExpect(status().isForbidden());   // Adama officer
        create(TADESSE, ", \"staffId\": 3").andExpect(status().isBadRequest());  // a manager, not a loan officer
        create(TADESSE, ", \"staffId\": 2").andExpect(status().isCreated());     // Hana

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> body = ArgumentCaptor.forClass(Map.class);
        verify(fineract).createClient(body.capture());
        assertThat(body.getValue()).containsEntry("staffId", 2L).containsEntry("officeId", 2L);
    }

    @Test
    void ourOwnRulesAndValidation() throws Exception {
        mvc.perform(post("/api/clients").header("Authorization", bearer(SAMUEL)).contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_CLIENT.formatted("").replace("1994-02-11", "2012-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("The client must be at least 18 years old."));
        mvc.perform(post("/api/clients").header("Authorization", bearer(SAMUEL)).contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_CLIENT.formatted("").replace("0911447788", "12345")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("mobileNo"));
    }

    @Test
    void duplicateMobileNumberGives409() throws Exception {
        when(fineract.createClient(any())).thenThrow(new FineractApiException(403, new FineractError(
                "The request caused a data integrity issue to be fired by the database.",
                "Client with mobileNo `0911447788` already exists", "error.msg.client.duplicate.mobileNo",
                List.of(new FineractError.FieldError("mobileNo", "Client with mobileNo `0911447788` already exists",
                        null, "error.msg.client.duplicate.mobileNo")))));

        create(SAMUEL, "").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_CLIENT"))
                .andExpect(jsonPath("$.detail").value("A client with this mobile number already exists."));
    }

    @Test
    void updateSendsOnlyChangedFieldsAndRespectsScope() throws Exception {
        when(fineract.getClient(1L)).thenReturn(client(1, 2, 1));
        when(fineract.updateClient(anyLong(), any())).thenReturn(new FineractCommandResult(2L, 1L, null, 1L, Map.of()));

        update(SAMUEL, 1, "{\"mobileNo\": \"0911447789\"}").andExpect(status().isOk());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> body = ArgumentCaptor.forClass(Map.class);
        verify(fineract).updateClient(eq(1L), body.capture());
        assertThat(body.getValue()).containsOnly(Map.entry("mobileNo", "0911447789"));

        update(HANA, 1, "{\"mobileNo\": \"0911447790\"}").andExpect(status().isForbidden());
        update(SAMUEL, 1, "{}").andExpect(status().isBadRequest());
        assertThat(auditLog.findAll()).extracting(r -> r.getAction() + ":" + r.getOutcome())
                .containsExactlyInAnyOrder("CLIENT_UPDATE:SUCCESS", "CLIENT_UPDATE:DENIED", "CLIENT_UPDATE:REJECTED");
    }

    private ResultActions create(et.adey.mfi.security.Officer officer, String extra) throws Exception {
        return mvc.perform(post("/api/clients").header("Authorization", bearer(officer))
                .contentType(MediaType.APPLICATION_JSON).content(NEW_CLIENT.formatted(extra)));
    }

    private ResultActions update(et.adey.mfi.security.Officer officer, long id, String body) throws Exception {
        return mvc.perform(put("/api/clients/" + id).header("Authorization", bearer(officer))
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
