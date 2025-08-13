package org.camunda.bpm.identity.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 外部API客户端配置
 */
@Configuration
public class ApiClientConfig {
    
    @Autowired
    private ApiProperties apiProperties;
    
    /**
     * 配置RestTemplate Bean
     */
    @Bean(name = "externalApiRestTemplate")
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(apiProperties.getConnectTimeout()))
                .setReadTimeout(Duration.ofMillis(apiProperties.getReadTimeout()))
                .build();
        
        // // 添加认证拦截器
        // List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        // if (apiProperties.getAuthToken() != null && !apiProperties.getAuthToken().isEmpty()) {
        //     interceptors.add(new AuthTokenInterceptor(apiProperties.getAuthToken()));
        // }
        // restTemplate.setInterceptors(interceptors);
        
        return restTemplate;
    }
    
    /**
     * 认证Token拦截器
     */
    // private static class AuthTokenInterceptor implements ClientHttpRequestInterceptor {
    //     private final String authToken;
        
    //     public AuthTokenInterceptor(String authToken) {
    //         this.authToken = authToken;
    //     }
        
    //     @Override
    //     public org.springframework.http.client.ClientHttpResponse intercept(
    //             org.springframework.http.HttpRequest request,
    //             byte[] body,
    //             org.springframework.http.client.ClientHttpRequestExecution execution) throws java.io.IOException {
            
    //         // 添加Authorization头
    //         request.getHeaders().add("Authorization", "Bearer " + authToken);
    //         // 添加Content-Type头
    //         request.getHeaders().add("Content-Type", "application/json");
            
    //         return execution.execute(request, body);
    //     }
    // }
}