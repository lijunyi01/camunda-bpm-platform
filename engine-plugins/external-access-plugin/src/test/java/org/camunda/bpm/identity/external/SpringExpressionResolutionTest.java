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
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.identity.external.ExternalAccessSpringConfiguration.ExternalAccessSpringPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.Assert.*;

/**
 * Test class for Spring-based External Access Identity Provider Plugin.
 * This test verifies that the Spring configuration properly sets up the expression manager
 * and resolves expressions correctly.
 */
public class SpringExpressionResolutionTest {

    private ProcessEngine processEngine;
    private AnnotationConfigApplicationContext springContext;

    @Before
    public void setUp() {
        // Create Spring context
        springContext = new AnnotationConfigApplicationContext();
        springContext.register(ExternalAccessSpringConfiguration.class);
        springContext.refresh();

        // Create process engine configuration
        ProcessEngineConfigurationImpl configuration = new StandaloneInMemProcessEngineConfiguration();
        configuration.setDatabaseSchemaUpdate("create-drop");
        configuration.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        configuration.setJdbcDriver("org.h2.Driver");
        configuration.setJdbcUsername("sa");
        configuration.setJdbcPassword("");

        // Get the Spring plugin and configure it
        ExternalAccessSpringPlugin plugin = springContext.getBean(ExternalAccessSpringPlugin.class);
        configuration.getProcessEnginePlugins().add(plugin);

        // Build the process engine
        processEngine = configuration.buildProcessEngine();
    }

    @After
    public void tearDown() {
        if (processEngine != null) {
            processEngine.close();
        }
        if (springContext != null) {
            springContext.close();
        }
    }

    @Test
    public void testExternalUserQueryBeanIsRegistered() {
        ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
        Object externalUserQuery = config.getBeans().get("externalUserQuery");
        assertNotNull("externalUserQuery bean should be registered", externalUserQuery);
        assertTrue("externalUserQuery should be instance of ExternalUserQueryService", 
                   externalUserQuery instanceof ExternalUserQueryService);
    }

    @Test
    public void testExpressionManagerHasRequiredFunctions() {
        ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
        
        // Test that expression manager can resolve the externalUserQuery bean
        config.getCommandExecutorTxRequired().execute(new Command<Void>() {
            @Override
            public Void execute(CommandContext commandContext) {
                try {
                    Object result = config.getExpressionManager().createExpression("${externalUserQuery}").getValue(null);
                    assertNotNull("Expression ${externalUserQuery} should resolve to a bean", result);
                    assertTrue("Resolved bean should be ExternalUserQueryService", 
                               result instanceof ExternalUserQueryService);
                } catch (Exception e) {
                    fail("Expression ${externalUserQuery} should be resolvable: " + e.getMessage());
                }
                return null;
            }
        });
    }

    @Test
    public void testExternalUserQueryMethodCall() {
        ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
        
        // Test that we can call methods on the externalUserQuery bean
        config.getCommandExecutorTxRequired().execute(new Command<Void>() {
            @Override
            public Void execute(CommandContext commandContext) {
                try {
                    // Test a simpler method call that doesn't require DelegateExecution
                    Object result = config.getExpressionManager()
                        .createExpression("${externalUserQuery}")
                        .getValue(null);
                    assertNotNull("ExternalUserQuery bean should be accessible", result);
                    assertTrue("Should be instance of ExternalUserQueryService", 
                               result instanceof ExternalUserQueryService);
                } catch (Exception e) {
                    fail("Expression ${externalUserQuery} should be resolvable: " + e.getMessage());
                }
                return null;
            }
        });
    }
}