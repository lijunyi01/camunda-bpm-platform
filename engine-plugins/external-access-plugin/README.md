# External Access Plugin - HTTP API 集成版本

## 概述

本插件已经修改为支持真正的外部 HTTP 接口调用，同时在调用失败时使用 mock 数据作为降级方案。插件现在通过 `ExternalApiClient` 类进行 HTTP 调用来获取用户和组信息。

## 主要修改内容

### 1. 新增 ExternalApiClient 类

- **文件**: `src/main/java/org/camunda/bpm/identity/external/ExternalApiClient.java`
- **功能**: 负责与外部 HTTP API 进行通信
- **特性**:
  - 支持配置化的 API 基础 URL、连接超时和读取超时
  - 实现了用户、组、密码验证和用户组关系的 HTTP 调用
  - 在 HTTP 调用失败时自动降级到 mock 数据
  - 包含完整的用户-组关系映射数据

### 2. 重构 ExternalAccessIdentityProviderSession 类

- **修改**: 移除静态 mock 数据，改为使用 `ExternalApiClient`
- **新增**: `getUserGroups()` 和 `findGroupIdsByUserId()` 方法支持用户组查询
- **改进**: 所有用户和组操作都通过 HTTP API 调用实现

### 3. 增强 ExternalUserQuery 类

- **新增**: 真正的 `memberOfGroup()` 过滤功能
- **改进**: 在 `list()` 方法中正确处理组成员过滤条件
- **集成**: 与 `ExternalAccessIdentityProviderSession` 集成以获取用户组信息

### 4. 更新 ExternalAccessIdentityProviderPlugin 类

- **修改**: 在插件初始化时创建 `ExternalApiClient` 实例
- **改进**: 通过依赖注入将 API 客户端传递给会话工厂

## HTTP API 接口规范

插件期望外部 API 提供以下接口：

### 用户相关接口

```
GET /api/users                    # 获取所有用户
GET /api/users/{userId}           # 根据ID获取用户
GET /api/users/{userId}/groups    # 获取用户所属组
```

### 组相关接口

```
GET /api/groups                   # 获取所有组
GET /api/groups/{groupId}         # 根据ID获取组
```

### 认证接口

```
POST /api/auth/validate           # 验证用户密码
请求体: {"userId": "xxx", "password": "xxx"}
```

## 预期的外部HTTP API接口

插件期望外部系统提供以下HTTP接口：

### 1. 获取用户信息
```
GET /users/{userId}
Response: {
  "id": "user123",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com"
}
```

### 2. 根据用户名查找用户
```
GET /users?username={username}
Response: {
  "id": "user123",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com"
}
```

### 3. 获取组信息
```
GET /groups/{groupId}
Response: {
  "id": "admin",
  "name": "Administrators",
  "type": "SYSTEM"
}
```

### 4. 获取用户所属的组
```
GET /users/{userId}/groups
Response: [
  {
    "id": "admin",
    "name": "Administrators",
    "type": "SYSTEM"
  }
]
```

### 5. 获取组下的用户（支持流程上下文）
```
POST /groups/{groupId}/users
Request Body: {
  "groupId": "admin",
  "context": {
    "authenticatedUserId": "user123",
    "processInstanceId": "proc-inst-456",
    "processDefinitionId": "process-def-789",
    "executionId": "exec-101",
    "activityId": "UserTask_1",
    "processVariables": {
      "approvalAmount": 10000,
      "department": "Finance"
    }
  }
}

Response: [
  {
    "id": "user123",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com"
  }
]
```

**注意：** 外部系统可以根据传递的流程上下文信息（如流程变量、当前活动等）来动态决定返回哪些用户，实现基于业务规则的动态权限控制。

## 配置选项

可以通过系统属性配置 API 客户端：

```properties
# API 基础 URL（默认: http://localhost:8080/api）
external.api.baseUrl=http://your-api-server:8080/api

# 连接超时时间，毫秒（默认: 5000）
external.api.connectTimeout=5000

# 读取超时时间，毫秒（默认: 10000）
external.api.readTimeout=10000
```

## Mock 数据说明

当 HTTP 调用失败时，插件会自动使用以下 mock 数据：

### 用户数据

- `external1`, `external2` - 属于 `external-users` 组
- `guest` - 属于 `guests` 组  
- `nx1` 到 `nx8` - 属于 `nx-users` 组

### 组数据

- `external-users` - External Users
- `guests` - Guest Users
- `nx-users` - NX Users

### 密码验证

Mock 逻辑：用户密码与用户 ID 相同时验证通过

## 功能特性

### 1. 外部身份验证集成

- 支持通过HTTP接口调用外部身份验证系统
- 用户查询：根据用户ID、用户名等条件查询用户信息
- 组查询：根据组ID、组名等条件查询组信息
- 用户组关系查询：查询用户所属的组以及组下的用户
- 流程上下文传递：自动获取并传递当前流程实例信息和流程变量到外部接口
- 降级机制：当外部接口不可用时，自动降级到本地mock数据
- 灵活配置：支持配置外部API的基础URL和超时时间

### 2. HTTP 调用日志

所有 HTTP 调用都会记录详细日志，包括：
- 请求 URL 和方法
- 响应状态码
- 错误信息（如果有）

### 3. 自动降级

当外部 API 不可用时，插件会：
- 记录错误日志
- 自动使用 mock 数据
- 确保 Camunda 引擎正常运行

### 4. 流程上下文传递

插件会自动获取当前流程执行的上下文信息，并将其作为额外参数传递给外部接口。支持两种获取方式：

**方式一：通过CommandContext直接获取**
- 当调用 `createUserQuery(CommandContext commandContext)` 方法时，插件会直接从传入的 `CommandContext` 中获取认证用户信息
- 同时从当前执行上下文中获取流程实例相关信息

**方式二：从全局上下文获取**
- 当调用无参数的 `createUserQuery()` 方法时，插件会从全局上下文中获取信息

**传递的上下文信息包括：**
- `authenticatedUserId`: 当前认证用户ID
- `processInstanceId`: 流程实例ID
- `processDefinitionId`: 流程定义ID
- `executionId`: 执行ID
- `activityId`: 当前活动ID
- `processVariables`: 流程变量（Map格式）

**HTTP请求格式：**
```json
{
  "groupId": "admin",
  "context": {
    "authenticatedUserId": "user123",
    "processInstanceId": "proc-inst-456",
    "processDefinitionId": "process-def-789",
    "executionId": "exec-101",
    "activityId": "UserTask_1",
    "processVariables": {
      "approvalAmount": 10000,
      "department": "Finance"
    }
  }
}
```

### 5. 完整的用户组支持

- 支持 `UserQuery.memberOfGroup(groupId)` 查询
- 支持任务候选组过滤
- 支持 REST API 组成员查询

## 使用示例

### 1. 查询特定组的用户

```java
List<User> users = identityService.createUserQuery()
    .memberOfGroup("external-users")
    .list();
```

### 2. 验证用户密码

```java
boolean valid = identityService.checkPassword("external1", "external1");
```

### 3. 获取用户信息

```java
User user = identityService.createUserQuery()
    .userId("external1")
    .singleResult();
```

## 部署说明

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