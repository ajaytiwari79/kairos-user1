package com.kairos.config;

import com.kairos.utils.user_context.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;

import java.io.IOException;

@Configuration
class IntegrationTestConfig {


    @Value("${spring.test.authorization}")
    private String authorization ;

    @Profile({"test"})
    @Bean
    @Primary
    public TestRestTemplate getTestRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        restTemplateBuilder = restTemplateBuilder
        .interceptors(new TestUserContextInterceptor());
        return new TestRestTemplate(restTemplateBuilder);
    }

    class TestUserContextInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(
                HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                throws IOException {

            HttpHeaders headers = request.getHeaders();
            headers.add(UserContext.AUTH_TOKEN,authorization);
            return execution.execute(request, body);
        }
    }
}
