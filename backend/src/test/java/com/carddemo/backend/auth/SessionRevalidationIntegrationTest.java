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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SessionRevalidationIntegrationTest {
    private static final String CSRF_HEADER = "X-XSRF-TOKEN";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void secondAdminDemotingFirstInvalidatesTheFirstSessionOnItsNextRequest() throws Exception {
        BrowserSession root = login("ADMIN001", "AdminPassword!21");
        create(root, "FIRSTADM", "ADMIN");
        create(root, "SECONDADM", "ADMIN");
        BrowserSession firstAdmin = login("FIRSTADM", "secret");
        BrowserSession secondAdmin = login("SECONDADM", "secret");

        mvc.perform(put("/api/users/FIRSTADM").session(secondAdmin.session()).cookie(secondAdmin.csrf())
                        .header(CSRF_HEADER, secondAdmin.csrf().getValue()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new UpdateUserRequest("Temp", "Admin", null, "REGULAR", 0L))))
                .andExpect(status().isOk());

        mvc.perform(get("/api/session").session(firstAdmin.session()))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void deletedUserSessionIsInvalidatedBeforeItsNextRequest() throws Exception {
        BrowserSession admin = login("ADMIN001", "AdminPassword!21");
        create(admin, "TEMPUSER", "REGULAR");
        BrowserSession deleted = login("TEMPUSER", "secret");

        mvc.perform(delete("/api/users/TEMPUSER").session(admin.session()).cookie(admin.csrf())
                        .header(CSRF_HEADER, admin.csrf().getValue()).param("version", "0"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/session").session(deleted.session()))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void malformedBodiesInvalidRolesUnsafeIdsAndMissingVersionAreStableBadRequests() throws Exception {
        BrowserSession admin = login("ADMIN001", "AdminPassword!21");
        mvc.perform(post("/api/users").session(admin.session()).cookie(admin.csrf()).header(CSRF_HEADER, admin.csrf().getValue())
                        .contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_REQUIRED"));
        mvc.perform(post("/api/users").session(admin.session()).cookie(admin.csrf()).header(CSRF_HEADER, admin.csrf().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CreateUserRequest("bad/id", "Bad", "Id", "secret", "ADMIN"))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field").value("userId"));
        mvc.perform(post("/api/users").session(admin.session()).cookie(admin.csrf()).header(CSRF_HEADER, admin.csrf().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CreateUserRequest("BADROLE", "Bad", "Role", "secret", "UNKNOWN"))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field").value("role"));
        mvc.perform(delete("/api/users/ADMIN001").session(admin.session()).cookie(admin.csrf()).header(CSRF_HEADER, admin.csrf().getValue()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field").value("version"));
    }

    @Test
    void duplicateCreateAndDeleteNotFoundFollowTheContractAndErrorsHaveTraceIds() throws Exception {
        BrowserSession admin = login("ADMIN001", "AdminPassword!21");
        create(admin, "DUPLICATE", "REGULAR");
        mvc.perform(post("/api/users").session(admin.session()).cookie(admin.csrf()).header(CSRF_HEADER, admin.csrf().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CreateUserRequest("duplicate", "Duplicate", "User", "secret", "REGULAR"))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DUPLICATE_USER"))
                .andExpect(jsonPath("$.traceId").isNotEmpty()).andExpect(header().exists("X-Trace-Id"));
        mvc.perform(delete("/api/users/MISSING").session(admin.session()).cookie(admin.csrf()).header(CSRF_HEADER, admin.csrf().getValue())
                        .param("version", "0"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
        mvc.perform(delete("/api/users/DUPLICATE").session(admin.session()).cookie(admin.csrf()).header(CSRF_HEADER, admin.csrf().getValue())
                        .param("version", "0")).andExpect(status().isNoContent());
    }

    private void create(BrowserSession admin, String userId, String role) throws Exception {
        mvc.perform(post("/api/users").session(admin.session()).cookie(admin.csrf()).header(CSRF_HEADER, admin.csrf().getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CreateUserRequest(userId, "Temp", "Admin", "secret", role))))
                .andExpect(status().isCreated());
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
