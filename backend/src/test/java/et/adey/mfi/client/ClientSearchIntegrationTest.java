package et.adey.mfi.client;

import et.adey.mfi.ApiTestBase;
import et.adey.mfi.fineract.model.FineractClientData;
import et.adey.mfi.fineract.model.FineractPage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static et.adey.mfi.TestOfficers.SAMUEL;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Name search ignores case (Fineract's own filter doesn't) and stays inside the token's scope. */
class ClientSearchIntegrationTest extends ApiTestBase {

    @Test
    void searchIsCaseInsensitiveAndPagedOverTheMatches() throws Exception {
        // samuel = office 2, staff 1. Fineract returns his clients "Client 1" ... "Client 25", by name.
        List<FineractClientData> all = IntStream.rangeClosed(1, 25).mapToObj(i -> client(i, 2, 1)).toList();
        when(fineract.searchClients(isNull(), eq(2L), eq(1L), eq(0), eq(200))).thenReturn(new FineractPage<>(25, all));

        // "client 1" (lower case) matches Client 1, 10..19 = 11 clients; page size 5.
        mvc.perform(get("/api/clients").param("search", "client 1").param("size", "5").header("Authorization", bearer(SAMUEL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(11))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.content[0].displayName").value("Client 1"));
        mvc.perform(get("/api/clients").param("search", "CLIENT 1").param("size", "5").param("page", "2").header("Authorization", bearer(SAMUEL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].displayName").value("Client 19"));

        // Fineract's case-sensitive displayName filter is never used.
        verify(fineract, never()).searchClients(eq("client 1"), eq(2L), eq(1L), eq(0), eq(200));
    }

    @Test
    void largeScopesAreReadInChunks() throws Exception {
        List<FineractClientData> first = IntStream.rangeClosed(1, 200).mapToObj(i -> client(i, 2, 1)).toList();
        List<FineractClientData> second = IntStream.rangeClosed(201, 230).mapToObj(i -> client(i, 2, 1)).toList();
        when(fineract.searchClients(isNull(), eq(2L), eq(1L), eq(0), eq(200))).thenReturn(new FineractPage<>(230, first));
        when(fineract.searchClients(isNull(), eq(2L), eq(1L), eq(200), eq(200))).thenReturn(new FineractPage<>(230, second));

        mvc.perform(get("/api/clients").param("search", "client 22").header("Authorization", bearer(SAMUEL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(11)); // 22, 220..229
    }
}
