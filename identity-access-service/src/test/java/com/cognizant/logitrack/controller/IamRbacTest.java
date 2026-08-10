package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.config.CorsConfig;
import com.cognizant.logitrack.config.SecurityConfig;
import com.cognizant.logitrack.security.JwtFilter;
import com.cognizant.logitrack.security.JwtUtil;
import com.cognizant.logitrack.service.AuditLogService;
import com.cognizant.logitrack.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The Identity & Access Management authorization matrix.
 *
 * User administration is ADMIN-only; the audit trail is readable by ANALYST and
 * ADMIN (an analyst investigating an incident needs it, but must not be able to
 * change accounts). These rules are the security core of the whole system —
 * every other service trusts the tokens this one issues — so they are pinned
 * here rather than verified by hand.
 */
class IamRbacTest {

    private static final String JWT_PROPS_SECRET =
            "jwt.secret=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    @WebMvcTest(UserController.class)
    @Import({SecurityConfig.class, CorsConfig.class, JwtFilter.class, JwtUtil.class})
    @TestPropertySource(properties = {JWT_PROPS_SECRET, "jwt.expiration=1800000"})
    @Nested
    class UserAdministration {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private UserService userService;

        @Test
        @DisplayName("ADMIN can list users")
        @WithMockUser(roles = "ADMIN")
        void adminCanListUsers() throws Exception {
            when(userService.getAllUsers()).thenReturn(List.of());

            mockMvc.perform(get("/api/users")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("a COORDINATOR cannot list users")
        @WithMockUser(roles = "COORDINATOR")
        void coordinatorCannotListUsers() throws Exception {
            mockMvc.perform(get("/api/users")).andExpect(status().isForbidden());

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("a SHIPPER cannot read another user's record")
        @WithMockUser(roles = "SHIPPER")
        void shipperCannotReadAUser() throws Exception {
            mockMvc.perform(get("/api/users/1")).andExpect(status().isForbidden());

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("an ANALYST cannot deactivate a user")
        @WithMockUser(roles = "ANALYST")
        void analystCannotChangeUserStatus() throws Exception {
            mockMvc.perform(patch("/api/users/1").with(csrf()).param("status", "INACTIVE"))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("ADMIN can deactivate a user")
        @WithMockUser(roles = "ADMIN")
        void adminCanChangeUserStatus() throws Exception {
            mockMvc.perform(patch("/api/users/1").with(csrf()).param("status", "INACTIVE"))
                    .andExpect(status().isOk());

            verify(userService).updateUserStatus(eq(1), any());
        }

        @Test
        @DisplayName("an unauthenticated request gets 401, not 403")
        void anonymousGetsUnauthorized() throws Exception {
            // 401 vs 403 is not cosmetic: the frontend refreshes its access token
            // on 401, so answering 403 to an expired token would silently break
            // the refresh flow instead of renewing the session.
            mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
        }
    }

    @WebMvcTest(AuditLogController.class)
    @Import({SecurityConfig.class, CorsConfig.class, JwtFilter.class, JwtUtil.class})
    @TestPropertySource(properties = {JWT_PROPS_SECRET, "jwt.expiration=1800000"})
    @Nested
    class AuditTrail {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private AuditLogService auditLogService;

        private void stubEmptyPage() {
            Page<com.cognizant.logitrack.dto.AuditLogDTO> empty =
                    new PageImpl<>(List.of(), Pageable.ofSize(20), 0);
            when(auditLogService.search(any(), any(), any(), any(), any())).thenReturn(empty);
        }

        @Test
        @DisplayName("ADMIN can read the audit trail")
        @WithMockUser(roles = "ADMIN")
        void adminCanReadAuditTrail() throws Exception {
            stubEmptyPage();

            mockMvc.perform(get("/api/audit-logs")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("ANALYST can read the audit trail")
        @WithMockUser(roles = "ANALYST")
        void analystCanReadAuditTrail() throws Exception {
            stubEmptyPage();

            mockMvc.perform(get("/api/audit-logs")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("a COORDINATOR cannot read the audit trail")
        @WithMockUser(roles = "COORDINATOR")
        void coordinatorCannotReadAuditTrail() throws Exception {
            mockMvc.perform(get("/api/audit-logs")).andExpect(status().isForbidden());

            verifyNoInteractions(auditLogService);
        }

        @Test
        @DisplayName("a DRIVER cannot read another user's audit history")
        @WithMockUser(roles = "DRIVER")
        void driverCannotReadPerUserAuditHistory() throws Exception {
            mockMvc.perform(get("/api/audit-logs/user/1")).andExpect(status().isForbidden());

            verifyNoInteractions(auditLogService);
        }

        @Test
        @DisplayName("the date filter accepts plain calendar dates")
        @WithMockUser(roles = "ANALYST")
        void dateFiltersBind() throws Exception {
            stubEmptyPage();

            mockMvc.perform(get("/api/audit-logs")
                            .param("fromDate", "2026-08-01")
                            .param("toDate", "2026-08-08")
                            .param("action", "LOGIN"))
                    .andExpect(status().isOk());

            verify(auditLogService).search(any(), eq("LOGIN"), any(), any(), any());
        }

        @Test
        @DisplayName("an oversized page size is clamped rather than honoured")
        @WithMockUser(roles = "ANALYST")
        void pageSizeIsClamped() throws Exception {
            stubEmptyPage();

            mockMvc.perform(get("/api/audit-logs").param("size", "100000"))
                    .andExpect(status().isOk());

            verify(auditLogService).search(any(), any(), any(), any(),
                    argThat(pageable -> pageable.getPageSize() <= 200));
        }

        @Test
        @DisplayName("an unauthenticated audit read gets 401")
        void anonymousGetsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/audit-logs")).andExpect(status().isUnauthorized());
        }
    }
}
