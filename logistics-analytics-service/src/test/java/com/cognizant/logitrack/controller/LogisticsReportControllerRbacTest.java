package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.config.CorsConfig;
import com.cognizant.logitrack.config.SecurityConfig;
import com.cognizant.logitrack.dto.LogisticsReportDTO;
import com.cognizant.logitrack.security.JwtFilter;
import com.cognizant.logitrack.security.JwtUtil;
import com.cognizant.logitrack.service.LogisticsReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pins the authorization matrix for the analytics module.
 *
 * The POST cases are a regression guard: the rule used to match only
 * HttpMethod.GET, so any authenticated user — a DRIVER, a SHIPPER — could
 * trigger a cross-service aggregation and persist a report they were not even
 * allowed to read back. The authorization matrix is the least self-evident and
 * most security-critical part of the service, which makes it exactly the part
 * that deserves tests rather than manual checking.
 */
@WebMvcTest(LogisticsReportController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtFilter.class, JwtUtil.class})
@TestPropertySource(properties = {
        "jwt.secret=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437",
        "jwt.expiration=1800000"
})
class LogisticsReportControllerRbacTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogisticsReportService reportService;

    // ---------- reading reports ----------

    @Test
    @DisplayName("GET reports is allowed for ANALYST")
    @WithMockUser(roles = "ANALYST")
    void analystCanListReports() throws Exception {
        when(reportService.getAllReports()).thenReturn(List.of());

        mockMvc.perform(get("/api/logistics-reports"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET reports is allowed for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void adminCanListReports() throws Exception {
        when(reportService.getAllReports()).thenReturn(List.of());

        mockMvc.perform(get("/api/logistics-reports"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET reports is forbidden for a SHIPPER")
    @WithMockUser(roles = "SHIPPER")
    void shipperCannotListReports() throws Exception {
        mockMvc.perform(get("/api/logistics-reports"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reportService);
    }

    @Test
    @DisplayName("GET reports requires authentication")
    void anonymousCannotListReports() throws Exception {
        mockMvc.perform(get("/api/logistics-reports"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- generating reports (the regression) ----------

    @Test
    @DisplayName("POST generate is allowed for ANALYST")
    @WithMockUser(roles = "ANALYST")
    void analystCanGenerateReport() throws Exception {
        when(reportService.generateReport(any()))
                .thenReturn(LogisticsReportDTO.builder().reportId(1).scope("GLOBAL").build());

        mockMvc.perform(post("/api/logistics-reports")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scope\":\"GLOBAL\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST generate is FORBIDDEN for a DRIVER, not merely unreadable")
    @WithMockUser(roles = "DRIVER")
    void driverCannotGenerateReport() throws Exception {
        mockMvc.perform(post("/api/logistics-reports")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scope\":\"GLOBAL\"}"))
                .andExpect(status().isForbidden());

        // The aggregation must not have run at all.
        verifyNoInteractions(reportService);
    }

    @Test
    @DisplayName("POST generate is forbidden for a COORDINATOR")
    @WithMockUser(roles = "COORDINATOR")
    void coordinatorCannotGenerateReport() throws Exception {
        mockMvc.perform(post("/api/logistics-reports")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reportService);
    }

    // ---------- scorecards ----------

    @Test
    @DisplayName("carrier scorecards are readable by ANALYST")
    @WithMockUser(roles = "ANALYST")
    void analystCanReadScorecards() throws Exception {
        when(reportService.getCarrierScorecards(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/logistics-reports/carrier-scorecards"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("carrier scorecards are forbidden for a WAREHOUSEOPS user")
    @WithMockUser(roles = "WAREHOUSEOPS")
    void warehouseCannotReadScorecards() throws Exception {
        mockMvc.perform(get("/api/logistics-reports/carrier-scorecards"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("/carrier-scorecards is not swallowed by the /{id} mapping")
    @WithMockUser(roles = "ANALYST")
    void scorecardsPathIsNotParsedAsAReportId() throws Exception {
        when(reportService.getCarrierScorecards(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/logistics-reports/carrier-scorecards"))
                .andExpect(status().isOk());

        verify(reportService).getCarrierScorecards(any(), any());
        verify(reportService, never()).getReportById(any());
    }
}
