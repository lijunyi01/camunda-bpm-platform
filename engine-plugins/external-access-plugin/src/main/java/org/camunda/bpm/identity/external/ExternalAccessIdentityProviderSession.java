package org.camunda.bpm.identity.external;

import org.camunda.bpm.engine.identity.*;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.impl.context.BpmnExecutionContext;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.RuntimeService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 外部访问身份提供者会话
 * 实现简单的用户和组管理
 */
public class ExternalAccessIdentityProviderSession implements ReadOnlyIdentityProvider, Session {

    private static final ExternalAccessPluginLogger LOG = ExternalAccessPluginLogger.LOGGER;

    // 模拟外部用户数据
    private static final List<User> EXTERNAL_USERS = Arrays.asList(
        createUser("external1", "External", "User1", "external1@company.com"),
        createUser("external2", "External", "User2", "external2@company.com"),
        createUser("guest", "Guest", "User", "guest@company.com"),
        createUser("nx1", "nx1", "NXUser1", "nx1@company.com"),
        createUser("nx2", "nx2", "NXUser2", "nx2@company.com"),
        createUser("nx3", "nx3", "NXUser3", "nx3@company.com"),
        createUser("nx4", "nx4", "NXUser4", "nx4@company.com"),
        createUser("nx5", "nx5", "NXUser5", "nx5@company.com"),
        createUser("nx6", "nx6", "NXUser6", "nx6@company.com"),
        createUser("nx7", "nx7", "NXUser7", "nx7@company.com"),
        createUser("nx8", "nx8", "NXUser8", "nx8@company.com")
    );

    // 模拟外部组数据
    private static final List<Group> EXTERNAL_GROUPS = Arrays.asList(
        createGroup("external-users", "External Users", "WORKFLOW"),
        createGroup("guests", "Guest Users", "WORKFLOW"),
        createGroup("nx-users", "NX Users", "WORKFLOW")
    );

    @Override
    public User findUserById(String userId) {
        LOG.writeLog("ExternalAccessIdentityProviderSession findUserById:" + userId);
        return EXTERNAL_USERS.stream()
            .filter(user -> user.getId().equals(userId))
            .findFirst()
            .orElse(null);
    }

    @Override
    public UserQuery createUserQuery() {
        LOG.writeLog("ExternalAccessIdentityProviderSession createUserQuery");
        // 返回null表示不支持原生查询
        // return null;
        // 获取全局上下文
        CommandContext commandContext = Context.getCommandContext();
        LOG.writeLog("ExternalAccessIdentityProviderSession createUserQuery:" + commandContext);

        ProcessEngine processEngine = Context.getProcessEngineConfiguration().getProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();

        // ExecutionEntity execution = Context.getExecutionContext().getExecution();
        // LOG.writeLog("execution:" + execution);
        // String processInstanceId = execution.getProcessInstanceId();
        // LOG.writeLog("processInstanceId:" + processInstanceId);
        if (commandContext != null) {
            // 获取流程上下文信息
            Map<String, Object> contextInfo = getCurrentProcessContextFromCommandContext(commandContext);
            LOG.writeLog("contextInfo:" + contextInfo);
        }
        return new ExternalUserQuery(EXTERNAL_USERS);
    }

    @Override
    public UserQuery createUserQuery(CommandContext commandContext) {
        // LOG.writeLog("ExternalAccessIdentityProviderSession createUserQuery:commandContext");
        LOG.writeLog("ExternalAccessIdentityProviderSession createUserQuery:commandContext:" + commandContext);
        return new ExternalUserQuery(EXTERNAL_USERS);
    }

    @Override
    public NativeUserQuery createNativeUserQuery() {
        LOG.writeLog("ExternalAccessIdentityProviderSession createNativeUserQuery");
        // 返回null表示不支持原生查询
        return null;
    }

    @Override
    public Group findGroupById(String groupId) {
        LOG.writeLog("ExternalAccessIdentityProviderSession findGroupById:" + groupId);
        return EXTERNAL_GROUPS.stream()
            .filter(group -> group.getId().equals(groupId))
            .findFirst()
            .orElse(null);
    }

    @Override
    public GroupQuery createGroupQuery() {
        LOG.writeLog("ExternalAccessIdentityProviderSession createGroupQuery");
        return new ExternalGroupQuery(EXTERNAL_GROUPS);
    }

    @Override
    public GroupQuery createGroupQuery(CommandContext commandContext) {
        LOG.writeLog("ExternalAccessIdentityProviderSession createGroupQuery:commandContext");
        return new ExternalGroupQuery(EXTERNAL_GROUPS);
    }

    @Override
    public boolean checkPassword(String userId, String password) {
        LOG.writeLog("ExternalAccessIdentityProviderSession checkPassword:" + userId + ":" + password);
        // 简单的密码验证：密码与用户ID相同
        return userId != null && userId.equals(password);
    }

    @Override
    public Tenant findTenantById(String tenantId) {
        LOG.writeLog("ExternalAccessIdentityProviderSession findTenantById:" + tenantId);
        // 返回null表示不支持租户
        return null;
    }

    @Override
    public TenantQuery createTenantQuery() {
        LOG.writeLog("ExternalAccessIdentityProviderSession createTenantQuery");
        return new ExternalTenantQuery();
    }

    @Override
    public TenantQuery createTenantQuery(CommandContext commandContext) {
        LOG.writeLog("ExternalAccessIdentityProviderSession createTenantQuery:commandContext");
        return new ExternalTenantQuery();
    }

    @Override
    public void flush() {
        // 只读实现，无需刷新
    }

    @Override
    public void close() {
        // 无需关闭资源
    }

    // 辅助方法：创建用户
    private static User createUser(String id, String firstName, String lastName, String email) {
        User user = new UserEntity();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        return user;
    }

    // 辅助方法：创建组
    private static Group createGroup(String id, String name, String type) {
        Group group = new GroupEntity();
        group.setId(id);
        group.setName(name);
        group.setType(type);
        return group;
    }

    /**
     * 从CommandContext中安全地获取流程上下文信息
     */
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
}