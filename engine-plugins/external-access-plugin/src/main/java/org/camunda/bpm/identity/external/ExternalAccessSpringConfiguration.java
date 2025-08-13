/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.identity.external;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Duration;

/**
 * Spring configuration for External Access Identity Provider Plugin.
 * This configuration automatically registers the ExternalUserQueryService bean
 * and configures the expression manager with necessary functions.
 */
@Configuration
public class ExternalAccessSpringConfiguration {

    @Bean(name = "apiRestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofSeconds(30))
            .setReadTimeout(Duration.ofSeconds(30));
    }

    @Bean
    public ExternalUserQueryService externalUserQuery() {
        return new ExternalUserQueryService();
    }

    @Bean
    public ExternalAccessIdentityProviderSession externalAccessIdentityProviderSession() {
        return new ExternalAccessIdentityProviderSession();
    }

    @Bean
    public ExternalAccessIdentityProviderSessionFactory externalAccessIdentityProviderSessionFactory() {
        return new ExternalAccessIdentityProviderSessionFactory(apiService());
    }

    @Bean
    public ExternalAccessPlugin externalAccessPlugin() {
        return new ExternalAccessPlugin();
    }

    @Bean
    public ApiProperties apiProperties() {
        return new ApiProperties();
    }

    @Bean
    public ApiService apiService() {
        return new ApiService();
    }


}