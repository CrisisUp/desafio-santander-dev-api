package me.dio;

import me.dio.config.RateLimitInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the per-IP sliding-window write limiter. Tested directly
 * (no Spring context) so a limit breach here can't leak into other tests that
 * POST from the same mock IP.
 */
class RateLimitInterceptorTest {

    /** 3 POSTs allowed per 60s window. */
    private final RateLimitInterceptor interceptor = new RateLimitInterceptor(3, 60);
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    private MockHttpServletRequest post(String ip) {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/accounts/1/transactions");
        req.setRemoteAddr(ip);
        return req;
    }

    @Test
    void allowsRequestsUpToTheLimit() throws Exception {
        for (int i = 0; i < 3; i++) {
            assertThat(interceptor.preHandle(post("1.2.3.4"), response, new Object())).isTrue();
        }
        assertThat(response.getStatus()).isEqualTo(200); // untouched on success
    }

    @Test
    void rejectsTheRequestPastTheLimitWith429() throws Exception {
        for (int i = 0; i < 3; i++) {
            interceptor.preHandle(post("5.6.7.8"), response, new Object());
        }
        MockHttpServletResponse limited = new MockHttpServletResponse();
        boolean allowed = interceptor.preHandle(post("5.6.7.8"), limited, new Object());

        assertThat(allowed).isFalse();
        assertThat(limited.getStatus()).isEqualTo(429);
    }

    @Test
    void limitsArePerIp() throws Exception {
        // Exhaust IP-A, but IP-B is unaffected.
        for (int i = 0; i < 3; i++) {
            interceptor.preHandle(post("10.0.0.1"), response, new Object());
        }
        MockHttpServletResponse b = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(post("10.0.0.2"), b, new Object())).isTrue();
        assertThat(b.getStatus()).isEqualTo(200);
    }

    @Test
    void getRequestsAreNotLimited() throws Exception {
        MockHttpServletRequest get = new MockHttpServletRequest("GET", "/users");
        get.setRemoteAddr("10.0.0.1"); // same IP that is exhausted above
        assertThat(interceptor.preHandle(get, response, new Object())).isTrue();
    }
}
