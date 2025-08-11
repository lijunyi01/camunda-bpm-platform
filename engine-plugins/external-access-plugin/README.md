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

1. 编译插件：`mvn clean compile`
2. 打包插件：`mvn package`
3. 将生成的 JAR 文件放入 Camunda 的 `lib` 目录
4. 在 `bpm-platform.xml` 中配置插件
5. 配置外部 API 的系统属性
6. 启动 Camunda 引擎

## 注意事项

1. **网络连接**: 确保 Camunda 服务器能够访问外部 API
2. **超时设置**: 根据网络环境调整连接和读取超时时间
3. **错误处理**: 监控日志以确保 HTTP 调用正常工作
4. **性能考虑**: 外部 API 的响应时间会影响 Camunda 的性能
5. **安全性**: 确保外部 API 的安全性和认证机制

## 故障排除

### 常见问题

1. **连接超时**: 检查网络连接和 API 服务器状态
2. **认证失败**: 确认外部 API 的认证机制
3. **数据格式**: 确保外部 API 返回正确的 JSON 格式
4. **日志查看**: 检查 Camunda 日志中的 HTTP 调用记录

### 调试建议

1. 启用详细日志记录
2. 使用网络抓包工具检查 HTTP 请求
3. 测试外部 API 的可用性
4. 验证系统属性配置

---

**版本**: 1.0.0  
**最后更新**: 2025-08-10  
**兼容性**: Camunda BPM 7.x