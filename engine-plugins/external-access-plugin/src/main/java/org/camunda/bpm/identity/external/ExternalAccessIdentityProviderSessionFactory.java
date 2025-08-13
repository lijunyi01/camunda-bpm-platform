package org.camunda.bpm.identity.external;

import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Identity Provider Session Factory for External Access
 * 外部访问身份提供者会话工厂
 */
// @Component
public class ExternalAccessIdentityProviderSessionFactory implements SessionFactory {

    private final ApiService apiService;

    @Autowired
    public ExternalAccessIdentityProviderSessionFactory(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Class<?> getSessionType() {
        return ExternalAccessIdentityProviderSession.class;
    }

    @Override
    public Session openSession() {
        ExternalAccessIdentityProviderSession session = new ExternalAccessIdentityProviderSession();
        // 手动注入ApiService依赖
        session.setApiService(apiService);
        return session;
    }
}