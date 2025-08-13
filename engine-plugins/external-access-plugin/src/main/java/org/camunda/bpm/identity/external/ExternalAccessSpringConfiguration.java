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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.el.JuelExpressionManager;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import java.util.Map;
import java.util.HashMap;
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
    public ExternalAccessSpringPlugin externalAccessSpringPlugin() {
        return new ExternalAccessSpringPlugin();
    }

    // @Bean
    // public ApiClientConfig apiClientConfig() {
    //     return new ApiClientConfig();
    // }

    @Bean
    public ApiProperties apiProperties() {
        return new ApiProperties();
    }

    @Bean
    public ApiService apiService() {
        return new ApiService();
    }

    /**
     * Spring-aware version of the External Access Identity Provider Plugin.
     * This plugin configures the expression manager after the Spring context is fully initialized.
     */
    public static class ExternalAccessSpringPlugin extends AbstractProcessEnginePlugin {

        @Autowired
        private ExternalUserQueryService externalUserQuery;
        
        @Autowired
        private ApiService apiService;

        public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
            // Register the ExternalUserQueryService bean in the process engine configuration
            Map<Object, Object> beans = processEngineConfiguration.getBeans();
            if (beans == null) {
                beans = new HashMap<>();
                processEngineConfiguration.setBeans(beans);
            }
            beans.put("externalUserQuery", externalUserQuery);
            
            // Set the identity provider session factory
            processEngineConfiguration.setIdentityProviderSessionFactory(
                new ExternalAccessIdentityProviderSessionFactory(apiService));
        }

        public void postProcessEngineBuild(ProcessEngine processEngine) {
            // Configure expression manager after engine is fully built
            ProcessEngineConfigurationImpl configuration = 
                (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
            
            // Get the beans map from configuration
            Map<Object, Object> beans = configuration.getBeans();
            
            // Create new expression manager with beans
            JuelExpressionManager newExpressionManager = new JuelExpressionManager(beans);
            
            // Add core functions
            newExpressionManager.addFunction("currentUser", 
                org.camunda.bpm.engine.impl.util.ReflectUtil.getMethod(
                    org.camunda.bpm.engine.impl.el.CommandContextFunctions.class, "currentUser"));
            newExpressionManager.addFunction("currentUserGroups", 
                org.camunda.bpm.engine.impl.util.ReflectUtil.getMethod(
                    org.camunda.bpm.engine.impl.el.CommandContextFunctions.class, "currentUserGroups"));
            newExpressionManager.addFunction("now", 
                org.camunda.bpm.engine.impl.util.ReflectUtil.getMethod(
                    org.camunda.bpm.engine.impl.el.DateTimeFunctions.class, "now"));
            newExpressionManager.addFunction("dateTime", 
                org.camunda.bpm.engine.impl.util.ReflectUtil.getMethod(
                    org.camunda.bpm.engine.impl.el.DateTimeFunctions.class, "dateTime"));
            
            // Set the new expression manager
            configuration.setExpressionManager(newExpressionManager);
        }

        /**
         * Identity Provider Session Factory for External Access
         */
        public static class ExternalAccessIdentityProviderSessionFactory implements SessionFactory {
            
            private final ApiService apiService;
            
            public ExternalAccessIdentityProviderSessionFactory(ApiService apiService) {
                this.apiService = apiService;
            }

            @Override
            public Class<?> getSessionType() {
                return ReadOnlyIdentityProvider.class;
            }

            @Override
            public Session openSession() {
                ExternalAccessIdentityProviderSession session = new ExternalAccessIdentityProviderSession();
                // 手动注入ApiService依赖
                session.setApiService(apiService);
                return session;
            }
        }
    }
}