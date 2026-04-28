package org.acme.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Full-stack checks for {@code Accept} handling and {@link org.acme.api.config.BrowserPreferJsonForApiFilter}
 * (browser-style {@code Accept} with {@code text/html} vs API clients).
 */
@SpringBootTest
@AutoConfigureMockMvc
class AcceptContentNegotiationTest {

    private static final String BROWSER_ACCEPT =
            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dualJsonXmlEndpoints_browserAccept_prefersJson() throws Exception {
        for (String path : new String[] {"/api/v1/people", "/api/v1/people/corrected"}) {
            mockMvc.perform(get(path).header(HttpHeaders.ACCEPT, BROWSER_ACCEPT))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }
    }

    @Test
    void dualJsonXmlEndpoints_explicitXml_returnsXml() throws Exception {
        mockMvc.perform(get("/api/v1/people/corrected").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(content().string(containsString("<people>")));
    }

    @Test
    void xmlOnlyEndpoint_browserAccept_staysXml() throws Exception {
        mockMvc.perform(get("/api/v1/people/xml").header(HttpHeaders.ACCEPT, BROWSER_ACCEPT))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(content().string(containsString("<people>")));
    }

    @Test
    void xmlOnlyEndpoint_jsonAccept_notAcceptable() throws Exception {
        mockMvc.perform(get("/api/v1/people/xml").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void plainTextEndpoint_browserAccept_plainText() throws Exception {
        mockMvc.perform(get("/api/v1/people/summary").header(HttpHeaders.ACCEPT, BROWSER_ACCEPT))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(startsWith("John,")));
    }

    @Test
    void plainTextEndpoint_jsonAccept_notAcceptable() throws Exception {
        mockMvc.perform(get("/api/v1/people/summary").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void openApiDocs_browserAccept_stillJson() throws Exception {
        mockMvc.perform(get("/v3/api-docs").header(HttpHeaders.ACCEPT, BROWSER_ACCEPT))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(startsWith("{")));
    }
}
