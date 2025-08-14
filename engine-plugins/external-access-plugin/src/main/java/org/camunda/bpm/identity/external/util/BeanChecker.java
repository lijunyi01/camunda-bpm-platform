package org.camunda.bpm.identity.external.util;


import org.camunda.bpm.identity.external.ExternalAccessPluginLogger;
import org.camunda.bpm.identity.external.apiManager.ApiService;
import org.camunda.bpm.identity.external.listener.TestTaskListener;
import org.springframework.context.ApplicationContext;

public class BeanChecker {

    private static final ExternalAccessPluginLogger LOG = ExternalAccessPluginLogger.LOGGER;

    private final ApplicationContext applicationContext;

    // 通过构造函数注入
    public BeanChecker(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void checkBeans() {
        String[] beanNames = applicationContext.getBeanNamesForType(TestTaskListener.class);
        for (String beanName : beanNames) {
            LOG.writeLog("type 'TestTaskListener' bean name：" + beanName);
        }



        String[] bean2Names = applicationContext.getBeanNamesForType(ApiService.class);
        for (String bean2Name : bean2Names) {
            LOG.writeLog("type 'ApiService' bean name：" + bean2Name);
        }
    }
}
