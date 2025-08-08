# External Access Plugin 配置指南

## 插件自动加载

**不会自动加载**。Camunda引擎不会自动发现和加载插件，需要手动配置。

## 配置方式

### 1. Spring Boot 应用配置

#### 方式一：application.yml 配置
```yaml
camunda:
  bpm:
    process-engine-plugins:
      - plugin-class: org.camunda.bpm.identity.external.ExternalAccessIdentityProviderPlugin
```

#### 方式二：Java 配置类
```java
@Configuration
public class CamundaConfig {
    
    @Bean
    public ProcessEnginePlugin externalAccessPlugin() {
        return new ExternalAccessIdentityProviderPlugin();
    }
}
```

### 2. 传统部署配置

#### processes.xml 配置
```xml
<?xml version="1.0" encoding="UTF-8"?>
<process-application
    xmlns="http://www.camunda.org/schema/1.0/ProcessApplication"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

  <process-engine name="default">
    <plugins>
      <plugin>
        <class>org.camunda.bpm.identity.external.ExternalAccessIdentityProviderPlugin</class>
      </plugin>
    </plugins>
  </process-engine>

</process-application>
```

#### 编程方式配置
```java
ProcessEngineConfiguration configuration = ProcessEngineConfiguration
    .createProcessEngineConfigurationFromResource("camunda.cfg.xml");

// 添加插件
configuration.getProcessEnginePlugins().add(new ExternalAccessIdentityProviderPlugin());

ProcessEngine processEngine = configuration.buildProcessEngine();
```

### 3. camunda.cfg.xml 配置
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd">

  <bean id="processEngineConfiguration" 
        class="org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration">
    
    <!-- 数据库配置 -->
    <property name="jdbcUrl" value="jdbc:h2:mem:camunda;DB_CLOSE_DELAY=-1" />
    <property name="jdbcDriver" value="org.h2.Driver" />
    <property name="jdbcUsername" value="sa" />
    <property name="jdbcPassword" value="" />
    
    <!-- 插件配置 -->
    <property name="processEnginePlugins">
      <list>
        <bean class="org.camunda.bpm.identity.external.ExternalAccessIdentityProviderPlugin" />
      </list>
    </property>
    
  </bean>

</beans>
```

## 部署步骤

### 1. 添加JAR依赖

将生成的 `external-access-plugin-1.0.0.jar` 添加到应用的classpath中：

- **Spring Boot**: 放入 `src/main/resources/lib/` 或添加Maven依赖
- **Tomcat**: 放入 `WEB-INF/lib/` 目录
- **应用服务器**: 放入应用的lib目录

### 2. Maven依赖配置

如果插件已发布到Maven仓库：
```xml
<dependency>
    <groupId>org.camunda.bpm.identity</groupId>
    <artifactId>external-access-plugin</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 3. 验证插件加载

启动应用后，检查日志中是否有插件加载信息：
```
INFO  [org.camunda.bpm.identity.external.ExternalAccessIdentityProviderPlugin] 
      External Access Identity Provider Plugin initialized
```

## 测试用户

插件预定义了以下测试用户：

| 用户名 | 密码 | 所属组 |
|--------|------|--------|
| admin | admin123 | administrators |
| user1 | password1 | users |
| user2 | password2 | users |

## 自定义配置

### 修改用户数据

编辑 `ExternalAccessIdentityProviderSession.java` 中的静态数据：

```java
private static final List<ExternalUser> EXTERNAL_USERS = Arrays.asList(
    new ExternalUser("your_user", "your_password", "Your Name", "your@email.com"),
    // 添加更多用户...
);

private static final List<ExternalGroup> EXTERNAL_GROUPS = Arrays.asList(
    new ExternalGroup("your_group", "Your Group", "GROUP"),
    // 添加更多组...
);
```

### 集成外部系统

替换静态数据为实际的外部系统调用：

```java
@Override
public UserQuery createUserQuery() {
    // 调用外部API获取用户数据
    List<ExternalUser> users = externalUserService.getAllUsers();
    return new ExternalUserQuery(users);
}
```

## 注意事项

1. **只读模式**: 此插件只支持读取用户信息，不支持创建、更新、删除操作
2. **性能考虑**: 大量用户时建议实现缓存机制
3. **安全性**: 生产环境中请使用加密的密码存储
4. **兼容性**: 适用于Camunda BPM 7.x版本

## 故障排除

### 插件未加载
- 检查JAR文件是否在classpath中
- 验证配置文件语法是否正确
- 查看启动日志中的错误信息

### 用户认证失败
- 确认用户名和密码是否正确
- 检查插件中的用户数据定义
- 验证身份提供者是否正确配置