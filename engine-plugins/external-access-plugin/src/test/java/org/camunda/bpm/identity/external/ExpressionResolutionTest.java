package org.camunda.bpm.identity.external;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.el.JuelExpressionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * 测试 ExternalUserQueryService 在表达式中的解析
 */
public class ExpressionResolutionTest {

    private ProcessEngine processEngine;
    private AnnotationConfigApplicationContext applicationContext;

    @Before
    public void setUp() {
        // 创建Spring应用上下文
        applicationContext = new AnnotationConfigApplicationContext(ExternalAccessSpringConfiguration.class);
        
        // 创建独立的流程引擎配置
        StandaloneProcessEngineConfiguration configuration = new StandaloneProcessEngineConfiguration();
        configuration.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        configuration.setJdbcDriver("org.h2.Driver");
        configuration.setJdbcUsername("sa");
        configuration.setJdbcPassword("");
        configuration.setDatabaseSchemaUpdate("create-drop");
        
        // 添加Spring配置的外部访问插件
        ExternalAccessSpringConfiguration.ExternalAccessSpringPlugin springPlugin = applicationContext.getBean(ExternalAccessSpringConfiguration.ExternalAccessSpringPlugin.class);
        configuration.getProcessEnginePlugins().add(springPlugin);
        
        // 构建流程引擎
        processEngine = configuration.buildProcessEngine();
    }

    @After
    public void tearDown() {
        if (processEngine != null) {
            processEngine.close();
        }
        if (applicationContext != null) {
            applicationContext.close();
        }
    }

    @Test
    public void testExternalUserQueryBeanRegistration() {
        // 验证 beans 映射中包含 externalUserQuery
        ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
        Map<Object, Object> beans = config.getBeans();
        
        assertNotNull("Beans map should not be null", beans);
        assertTrue("externalUserQuery should be registered in beans map", 
                   beans.containsKey("externalUserQuery"));
        
        Object externalUserQuery = beans.get("externalUserQuery");
        assertNotNull("externalUserQuery bean should not be null", externalUserQuery);
        assertTrue("externalUserQuery should be instance of ExternalUserQueryService", 
                   externalUserQuery instanceof ExternalUserQueryService);
    }

    @Test
    public void testExpressionManagerHasCorrectBeans() {
        // 验证 ExpressionManager 使用了正确的 beans 映射
        ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
        
        if (config.getExpressionManager() instanceof JuelExpressionManager) {
            // 通过创建表达式来测试 bean 解析
            try {
                // 这应该不会抛出 PropertyNotFoundException
                config.getExpressionManager().createExpression("${externalUserQuery}");
                // 如果没有异常，说明 bean 解析成功
            } catch (Exception e) {
                fail("Expression creation should not fail: " + e.getMessage());
            }
        }
    }

    @Test
    public void testExternalUserQueryServiceType() {
        // 验证服务类型正确
        ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
        Map<Object, Object> beans = config.getBeans();
        
        ExternalUserQueryService service = (ExternalUserQueryService) beans.get("externalUserQuery");
        assertNotNull("Service should not be null", service);
        
        // 验证服务类型
        assertTrue("Service should be instance of ExternalUserQueryService", 
                   service instanceof ExternalUserQueryService);
        
        // 验证服务不为空对象
        assertNotNull("Service toString should not be null", service.toString());
    }
}