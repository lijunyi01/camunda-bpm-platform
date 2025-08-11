# External Access Plugin

## 简介

这是一个最简的外部人员访问插件，用于Camunda BPM平台。该插件不依赖Spring Boot，仅使用Maven构建，提供基本的外部用户和组管理功能。

## 功能特性

- 简单的外部用户管理
- 基本的组管理
- 只读身份提供者实现
- 无需Spring Boot依赖
- 轻量级设计

## 预定义用户

插件预定义了以下测试用户：

| 用户ID | 姓名 | 邮箱 | 密码 |
|--------|------|------|------|
| external1 | External User1 | external1@company.com | external1 |
| external2 | External User2 | external2@company.com | external2 |
| guest | Guest User | guest@company.com | guest |

## 预定义组

- `external-users`: 外部用户组
- `guests`: 访客用户组

## 使用方法

### 1. 构建插件

```bash
mvn clean install
```

### 2. 配置Camunda引擎

在你的Camunda引擎配置中添加插件：

```java
// Java配置方式
ProcessEngineConfiguration configuration = ProcessEngineConfiguration
    .createStandaloneProcessEngineConfiguration()
    .setProcessEnginePlugins(Arrays.asList(new ExternalAccessIdentityProviderPlugin()));
```

或者在XML配置文件中：

```xml
<bean id="processEngineConfiguration" class="org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration">
    <property name="processEnginePlugins">
        <list>
            <bean class="org.camunda.bpm.identity.external.ExternalAccessIdentityProviderPlugin" />
        </list>
    </property>
</bean>
```

### 3. 验证配置

启动Camunda引擎后，可以通过以下方式验证插件是否正常工作：

```java
// 查询用户
User user = processEngine.getIdentityService().createUserQuery()
    .userId("external1")
    .singleResult();

// 验证密码
boolean isValid = processEngine.getIdentityService()
    .checkPassword("external1", "external1");
```

## 扩展说明

这是一个基础实现，你可以根据实际需求进行扩展：

1. **数据源集成**: 修改`ExternalAccessIdentityProviderSession`类，从数据库或外部API获取用户数据
2. **密码验证**: 实现更复杂的密码验证逻辑
3. **权限管理**: 添加用户组关系管理
4. **缓存机制**: 添加用户数据缓存以提高性能

## BPMN 表达式支持

### ExternalUserQueryService

插件提供了 `ExternalUserQueryService` 服务，可以在 BPMN 表达式中使用：

```xml
<!-- 在服务任务中使用 -->
<serviceTask id="getUsersTask" 
             camunda:expression="${externalUserQuery.getCandidateUsersForGroup('guests', execution)}" />

<!-- 在监听器中使用 -->
<camunda:executionListener event="start" 
                           expression="${externalUserQuery.getCandidateUsersForGroup('external-users', execution)}" />
```

### 可用方法

- `getCandidateUsersForGroup(String groupId, DelegateExecution execution)`: 获取指定组的候选用户列表

### 实现原理

插件在 `preInit()` 阶段将 `ExternalUserQueryService` 实例注册到 ProcessEngine 的 beans 映射中，使其可以在 BPMN 表达式中被识别和调用。

### 故障排除

如果遇到 "Cannot resolve identifier 'externalUserQuery'" 错误：

1. 确保插件已正确加载（检查 `META-INF/services/org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin` 文件）
2. 确保 `ExternalUserQueryService` 类没有使用 Spring 注解（如 `@Component`）
3. 检查插件的 `preInit()` 方法是否正确执行

## 注意事项

- 这是一个只读身份提供者，不支持用户和组的创建、更新、删除操作
- 密码验证采用简单的明文比较，生产环境中应使用加密验证
- 预定义的用户和组数据仅用于测试，实际使用时应连接真实的数据源
- `ExternalUserQueryService` 通过插件机制注册，不依赖 Spring 容器