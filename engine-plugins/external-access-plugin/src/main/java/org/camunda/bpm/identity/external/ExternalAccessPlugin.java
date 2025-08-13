package org.camunda.bpm.identity.external;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.el.JuelExpressionManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring-aware version of the External Access Identity Provider Plugin.
 * This plugin configures the expression manager after the Spring context is fully initialized.
 */
public class ExternalAccessPlugin extends AbstractProcessEnginePlugin {

    @Autowired
    private ExternalUserQueryService externalUserQuery;
    
    @Autowired
    private ApiService apiService;
    
    @Autowired
    private ExternalAccessIdentityProviderSessionFactory sessionFactory;

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        // Register the ExternalUserQueryService bean in the process engine configuration
        Map<Object, Object> beans = processEngineConfiguration.getBeans();
        if (beans == null) {
            beans = new HashMap<>();
            processEngineConfiguration.setBeans(beans);
        }
        beans.put("externalUserQuery", externalUserQuery);
        
        // Set the identity provider session factory
        processEngineConfiguration.setIdentityProviderSessionFactory(sessionFactory);
    }

    @Override
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


}