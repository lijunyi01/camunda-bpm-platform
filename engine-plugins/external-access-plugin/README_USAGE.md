# ExternalAccessIdentityProviderSession 使用说明

## 从 CommandContext 获取流程变量等信息

### 问题背景

在通过 REST API 启动流程时，`Context.getBpmnExecutionContext()` 返回 `null`，而 `Context.getCommandContext()` 不为 `null`。这是因为：

1. REST API 调用会执行 `StartProcessInstanceCmd` 或 `StartProcessInstanceAtActivitiesCmd` 命令
2. 这些命令在 `CommandContext` 中执行，但 `BpmnExecutionContext` 仅在执行用户代码（如 `JavaDelegate`、`ExecutionListener` 等）时才会被设置

### 解决方案

我们实现了 `getCurrentProcessContextFromCommandContext` 方法，通过以下方式从 `CommandContext` 获取流程信息：

#### 核心实现逻辑

```java
private Map<String, Object> getCurrentProcessContextFromCommandContext(CommandContext commandContext) {
    Map<String, Object> contextInfo = new HashMap<String, Object>();
    
    try {
        // 获取当前认证用户ID
        String authenticatedUserId = commandContext.getAuthenticatedUserId();
        contextInfo.put("authenticatedUserId", authenticatedUserId);
        contextInfo.put("commandContextAvailable", true);

        // 尝试从CommandContext的缓存中获取ExecutionEntity
        List<ExecutionEntity> cachedExecutions = commandContext.getDbEntityManager().getCachedEntitiesByType(ExecutionEntity.class);
        ExecutionEntity execution = null;
        
        // 查找当前活跃的执行实体
        for (ExecutionEntity executionEntity : cachedExecutions) {
            if (executionEntity != null && !executionEntity.isEnded()) {
                execution = executionEntity;
                break;
            }
        }
        
        if (execution != null) {
            contextInfo.put("executionId", execution.getId());
            contextInfo.put("processInstanceId", execution.getProcessInstanceId());
            contextInfo.put("processDefinitionId", execution.getProcessDefinitionId());
            contextInfo.put("processDefinitionKey", execution.getProcessDefinitionKey());
            
            // 获取流程变量
            try {
                VariableMap processVariables = execution.getVariables();
                contextInfo.put("processVariables", processVariables);
            } catch (Exception e) {
                contextInfo.put("variableError", "Failed to get process variables: " + e.getMessage());
            }
        } else {
            contextInfo.put("executionNotFound", "No active execution found in cache");
        }
        
    } catch (Exception e) {
        // 如果获取流程上下文失败，记录错误但不影响主要功能
        contextInfo.put("error", "Failed to get process context: " + e.getMessage());
    }
    
    return contextInfo;
}
```

#### 使用方式

在需要获取流程上下文信息的地方调用此方法：

```java
@Override
public UserQuery createUserQuery(CommandContext commandContext) {
    // 获取流程上下文信息
    Map<String, Object> processContext = getCurrentProcessContextFromCommandContext(commandContext);
    
    // 使用流程上下文信息
    String authenticatedUserId = (String) processContext.get("authenticatedUserId");
    String processInstanceId = (String) processContext.get("processInstanceId");
    VariableMap processVariables = (VariableMap) processContext.get("processVariables");
    
    // 根据流程上下文信息进行相应的处理
    // ...
    
    return new ExternalAccessUserQuery();
}
```

### 方法特点

1. **缓存优先**：首先从 `CommandContext` 的缓存中查找 `ExecutionEntity`，避免数据库查询
2. **异常安全**：即使获取流程信息失败，也不会影响主要功能
3. **信息完整**：可以获取执行ID、流程实例ID、流程定义ID、流程定义Key和流程变量
4. **状态检查**：只选择未结束的执行实体

### 返回信息说明

方法返回的 `Map<String, Object>` 包含以下信息：

- `authenticatedUserId`: 当前认证用户ID
- `commandContextAvailable`: CommandContext是否可用（总是true）
- `executionId`: 执行实体ID
- `processInstanceId`: 流程实例ID
- `processDefinitionId`: 流程定义ID
- `processDefinitionKey`: 流程定义Key
- `processVariables`: 流程变量（VariableMap类型）
- `executionNotFound`: 如果没有找到活跃执行实体的提示信息
- `variableError`: 获取流程变量时的错误信息
- `error`: 整体获取过程中的错误信息

### 注意事项

1. 此方法依赖于 `CommandContext` 的缓存机制，在某些情况下可能无法找到执行实体
2. 如果有多个活跃的执行实体，方法会返回第一个找到的
3. 建议在使用返回的信息前检查是否存在错误信息
4. 流程变量的获取可能会失败，需要单独处理

### 测试

项目包含了完整的单元测试，验证了以下场景：
- 存在活跃执行实体时的正常情况
- 不存在活跃执行实体时的处理
- 异常情况的处理

运行测试：
```bash
mvn test -Dtest=ExternalAccessIdentityProviderSessionTest
```