package com.carddemo.backend.auth;

import com.carddemo.backend.user.CreateUserRequest;
import com.carddemo.backend.user.UpdateUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSecurityIntegrationTest {
    private static final String CSRF_HEADER = "X-XSRF-TOKEN";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void authenticatedAdminCanUseIssuedCsrfTokenForCreateUpdateDeleteAndLogout() throws Exception {
        BrowserSession admin = login("ADMIN001", "AdminPassword!21");
        assertThat(admin.csrf().getName()).isEqualTo("XSRF-TOKEN");
        assertThat(admin.csrf().getValue()).isNotBlank();

        MvcResult created = mvc.perform(post("/api/users").session(admin.session()).cookie(admin.csrf())
                        .header(CSRF_HEADER, admin.csrf().getValue()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CreateUserRequest("newuser", "New", "User", "secret", "REGULAR"))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.userId").value("NEWUSER"))
                .andExpect(jsonPath("$.version").value(0)).andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        mvc.perform(put("/api/users/NEWUSER").session(admin.session()).cookie(admin.csrf())
                        .header(CSRF_HEADER, admin.csrf().getValue()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new UpdateUserRequest("Updated", "User", null, "REGULAR", 0L))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.version").value(1));

        mvc.perform(delete("/api/users/NEWUSER").session(admin.session()).cookie(admin.csrf())
                        .header(CSRF_HEADER, admin.csrf().getValue()).param("version", "1"))
                .andExpect(status().isNoContent());

        mvc.perform(delete("/api/session").session(admin.session()).cookie(admin.csrf())
                        .header(CSRF_HEADER, admin.csrf().getValue()))
                .andExpect(status().isNoContent());
    }

    @Test
    void csrfRemainsRequiredForAuthenticatedMutations() throws Exception {
        BrowserSession admin = login("ADMIN001", "AdminPassword!21");
        mvc.perform(post("/api/users").session(admin.session()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CreateUserRequest("blocked", "Blocked", "User", "secret", "REGULAR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUserCannotAccessAdminResourcesEvenWithACsrfToken() throws Exception {
        BrowserSession regular = login("REGULAR1", "RegularPassword!21");
        mvc.perform(get("/api/users/ADMIN001").session(regular.session()))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mvc.perform(post("/api/users").session(regular.session()).cookie(regular.csrf())
                        .header(CSRF_HEADER, regular.csrf().getValue()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CreateUserRequest("blocked", "Blocked", "User", "secret", "REGULAR"))))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void sessionRetrievalIssuesACsrfCookieAndApiDocsArePublic() throws Exception {
        BrowserSession admin = login("ADMIN001", "AdminPassword!21");
        mvc.perform(get("/api/session").session(admin.session()))
                .andExpect(status().isOk()).andExpect(cookie().exists("XSRF-TOKEN"));
        mvc.perform(get("/api-docs"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.components.securitySchemes.SESSION.in").value("cookie"))
                .andExpect(jsonPath("$.components.schemas.ApiError").exists());
    }

    @Test
    void successNotFoundAndConflictResponsesFollowTheContract() throws Exception {
        BrowserSession admin = login("ADMIN001", "AdminPassword!21");
        mvc.perform(get("/api/users/UNKNOWN").session(admin.session()))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
        mvc.perform(put("/api/users/ADMIN001").session(admin.session()).cookie(admin.csrf())
                        .header(CSRF_HEADER, admin.csrf().getValue()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new UpdateUserRequest("Admin", "User", null, "ADMIN", 0L))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("NO_CHANGES"));
        mvc.perform(delete("/api/users/ADMIN001").session(admin.session()).cookie(admin.csrf())
                        .header(CSRF_HEADER, admin.csrf().getValue()).param("version", "9"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("VERSION_CONFLICT"));
    }

    @Test
    void authenticationValidationAndWrongCredentialsReturnContractedErrors() throws Exception {
        mvc.perform(post("/api/session").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new SessionRequest(null, null))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field").value("userId"));
        mvc.perform(post("/api/session").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new SessionRequest("ADMIN001", "wrong"))))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        mvc.perform(post("/api/session").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new SessionRequest("invalid/id", "wrong"))))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    private BrowserSession login(String userId, String password) throws Exception {
        MvcResult login = mvc.perform(post("/api/session").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new SessionRequest(userId, password))))
                .andExpect(status().isOk()).andExpect(cookie().exists("XSRF-TOKEN")).andReturn();
        return new BrowserSession((MockHttpSession) login.getRequest().getSession(false),
                new Cookie("XSRF-TOKEN", login.getResponse().getCookie("XSRF-TOKEN").getValue()));
    }

    private record BrowserSession(MockHttpSession session, Cookie csrf) { }
}
