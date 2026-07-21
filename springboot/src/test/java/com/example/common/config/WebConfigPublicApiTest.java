package com.example.common.config;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebConfigPublicApiTest {

    @Test
    void userRestLoginAndRegisterArePublic() {
        JWTInterceptor interceptor = mock(JWTInterceptor.class);
        InterceptorRegistry registry = mock(InterceptorRegistry.class);
        InterceptorRegistration registration = mock(InterceptorRegistration.class);
        when(registry.addInterceptor(interceptor)).thenReturn(registration);
        when(registration.addPathPatterns("/**")).thenReturn(registration);
        when(registration.excludePathPatterns(any(String[].class))).thenReturn(registration);
        WebConfig config = new WebConfig();
        org.springframework.test.util.ReflectionTestUtils.setField(config, "jwtInterceptor", interceptor);

        config.addInterceptors(registry);

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        org.mockito.Mockito.verify(registration, org.mockito.Mockito.atLeastOnce()).excludePathPatterns(captor.capture());
        List<String> excludedPaths = captor.getAllValues().stream()
                .flatMap(paths -> Arrays.stream(paths))
                .toList();
        assertThat(excludedPaths).contains("/api/user/login", "/api/user/register");
    }
}
