package com.tq.exchangehub.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tq.exchangehub.config.SecurityConfig;
import com.tq.exchangehub.exception.AdminReportException;
import com.tq.exchangehub.security.JwtAuthenticationFilter;
import com.tq.exchangehub.security.RateLimitingFilter;
import com.tq.exchangehub.service.AdminReportService;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = AdminReportController.class)
@Import(SecurityConfig.class)
class AdminReportControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;

    @MockBean private AdminReportService adminReportService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        doAnswer(invocation -> {
                    Object[] args = invocation.getArguments();
                    ((FilterChain) args[2]).doFilter(args[0], args[1]);
                    return null;
                })
                .when(jwtAuthenticationFilter)
                .doFilter(any(), any(), any());

        doAnswer(invocation -> {
                    Object[] args = invocation.getArguments();
                    ((FilterChain) args[2]).doFilter(args[0], args[1]);
                    return null;
                })
                .when(rateLimitingFilter)
                .doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void downloadReportReturnsPdfWithHeader() throws Exception {
        byte[] pdfBytes = "%PDF-1.7 test".getBytes(StandardCharsets.US_ASCII);
        when(adminReportService.generateMonthlyReport("2024-05")).thenReturn(pdfBytes);

        MvcResult result = mockMvc
                .perform(get("/api/admin/reports").param("period", "2024-05"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();

        byte[] responseBody = result.getResponse().getContentAsByteArray();
        assertThat(new String(responseBody, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void downloadReportReturnsBadRequestOnGenerationError() throws Exception {
        when(adminReportService.generateMonthlyReport("2024-05"))
                .thenThrow(new AdminReportException(HttpStatus.BAD_REQUEST, "PDF inválido"));

        mockMvc.perform(get("/api/admin/reports").param("period", "2024-05"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("PDF inválido"));
    }
}
