package com.tq.exchangehub.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tq.exchangehub.config.SecurityConfig;
import com.tq.exchangehub.dto.AdminMetricsDto;
import com.tq.exchangehub.dto.TimeSeriesPointDto;
import com.tq.exchangehub.security.JwtAuthenticationFilter;
import com.tq.exchangehub.security.RateLimitingFilter;
import com.tq.exchangehub.service.AdminService;
import java.util.List;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private WebApplicationContext context;

    @MockBean private AdminService adminService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    void setUp() throws Exception {
        // Ensure mock filters do not block the request chain
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
    void metricsEndpointAccessibleForAdmins() throws Exception {
        AdminMetricsDto metrics = new AdminMetricsDto(
                10L,
                2L,
                5L,
                3L,
                List.of(new TimeSeriesPointDto("10:00", 2L)),
                List.of(new TimeSeriesPointDto("10:00", 1L)));

        when(adminService.getMetrics()).thenReturn(metrics);

        mockMvc.perform(get("/api/admin/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeUsers").value(2))
                .andExpect(jsonPath("$.tradeActivity[0].label").value("10:00"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void metricsEndpointRejectedForNonAdmins() throws Exception {
        mockMvc.perform(get("/api/admin/metrics")).andExpect(status().isForbidden());
    }

    @Test
    void metricsEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/metrics")).andExpect(status().isUnauthorized());
    }
}
