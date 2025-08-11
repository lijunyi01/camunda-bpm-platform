package org.camunda.bpm.identity.external;

import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * 外部访问身份提供者插件
 * 提供基于HTTP API的外部人员访问功能
 */
public class ExternalAccessIdentityProviderPlugin extends AbstractProcessEnginePlugin {

    private static final ExternalAccessPluginLogger LOG = ExternalAccessPluginLogger.LOGGER;
    
    // 外部API客户端实例
    private ExternalApiClient apiClient;

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        LOG.writeLog("ExternalAccessIdentityProviderPlugin preInit");
        
        // 初始化API客户端
        this.apiClient = new ExternalApiClient();
        
        // 注册自定义身份提供者会话工厂
        processEngineConfiguration.setIdentityProviderSessionFactory(new ExternalAccessIdentityProviderSessionFactory(apiClient));
    }

    /**
     * 身份提供者会话工厂
     */
    public static class ExternalAccessIdentityProviderSessionFactory implements SessionFactory {
        
        private final ExternalApiClient apiClient;
        
        public ExternalAccessIdentityProviderSessionFactory(ExternalApiClient apiClient) {
            this.apiClient = apiClient;
        }

        @Override
        public Class<?> getSessionType() {
            return ReadOnlyIdentityProvider.class;
        }

        @Override
        public Session openSession() {
            LOG.writeLog("ExternalAccessIdentityProviderSessionFactory openSession");
            return new ExternalAccessIdentityProviderSession(apiClient);
        }
    }
}