package org.camunda.bpm.identity.external;

import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * 外部访问身份提供者插件
 * 提供最简单的外部人员访问功能
 */
public class ExternalAccessIdentityProviderPlugin extends AbstractProcessEnginePlugin {

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        // 注册自定义身份提供者会话工厂
        processEngineConfiguration.setIdentityProviderSessionFactory(new ExternalAccessIdentityProviderSessionFactory());
    }

    /**
     * 身份提供者会话工厂
     */
    public static class ExternalAccessIdentityProviderSessionFactory implements SessionFactory {

        @Override
        public Class<?> getSessionType() {
            return ReadOnlyIdentityProvider.class;
        }

        @Override
        public Session openSession() {
            return new ExternalAccessIdentityProviderSession();
        }
    }
}