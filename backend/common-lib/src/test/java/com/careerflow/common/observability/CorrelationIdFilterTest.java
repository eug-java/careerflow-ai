package com.careerflow.common.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void generatesCorrelationIdWhenHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNotBlank();
        });

        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isNotBlank();
    }

    @Test
    void reusesIncomingCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test");
        request.addHeader(CorrelationIdFilter.HEADER, "corr-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isEqualTo("corr-123");
        });

        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("corr-123");
    }
}
