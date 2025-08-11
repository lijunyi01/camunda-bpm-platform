package org.camunda.bpm.identity.external;

import org.camunda.bpm.engine.identity.*;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.BpmnExecutionContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.variable.VariableMap;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

/**
 * 外部访问身份提供者会话
 * 使用ExternalApiClient进行HTTP调用
 */
public class ExternalAccessIdentityProviderSession implements ReadOnlyIdentityProvider, Session {

    private static final ExternalAccessPluginLogger LOG = ExternalAccessPluginLogger.LOGGER;
    
    // 外部API客户端
    private final ExternalApiClient apiClient;
    
    public ExternalAccessIdentityProviderSession(ExternalApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public User findUserById(String userId) {
        LOG.writeLog("ExternalAccessIdentityProviderSession findUserById:" + userId);
        return apiClient.getUserById(userId);
    }

    @Override
    public UserQuery createUserQuery() {
        LOG.writeLog("ExternalAccessIdentityProviderSession createUserQuery");
        return new ExternalUserQuery(apiClient.getAllUsers(), this);
    }

    @Override
    public UserQuery createUserQuery(CommandContext commandContext) {
        LOG.writeLog("ExternalAccessIdentityProviderSession createUserQuery:commandContext");
        
        // 从CommandContext中获取流程上下文信息
        Map<String, Object> contextInfo = getCurrentProcessContextFromCommandContext(commandContext);
        
        // 创建带有上下文信息的UserQuery
        ExternalUserQuery userQuery = new ExternalUserQuery(apiClient.getAllUsers(), this);
        userQuery.setProcessContext(contextInfo);
        
        return userQuery;
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
        return apiClient.getGroupById(groupId);
    }

    @Override
    public GroupQuery createGroupQuery() {
        LOG.writeLog("ExternalAccessIdentityProviderSession createGroupQuery");
        return new ExternalGroupQuery(apiClient.getAllGroups());
    }

    @Override
    public GroupQuery createGroupQuery(CommandContext commandContext) {
        LOG.writeLog("ExternalAccessIdentityProviderSession createGroupQuery:commandContext");
        return new ExternalGroupQuery(apiClient.getAllGroups());
    }

    @Override
    public boolean checkPassword(String userId, String password) {
        LOG.writeLog("ExternalAccessIdentityProviderSession checkPassword:" + userId + ":" + password);
        return apiClient.checkPassword(userId, password);
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
        // 不需要实现
    }

    @Override
    public void close() {
        // 不需要实现
    }
    
    /**
   * 获取用户所属的组
   */
  public Set<String> getUserGroups(String userId) {
    return apiClient.getUserGroups(userId);
  }
  
  /**
   * 根据用户ID查找组ID列表（为UserQuery提供支持）
   */
  public Set<String> findGroupIdsByUserId(String userId) {
    return getUserGroups(userId);
  }

  /**
   * 根据组ID查找用户ID列表（为UserQuery提供支持）
   */
  public Set<String> findUserIdsByGroupId(String groupId) {
        try {
            Map<String, Object> contextInfo = getCurrentProcessContext();
            return apiClient.getGroupUsers(groupId, contextInfo);
        } catch (Exception e) {
            LOG.writeLog("Failed to get users for group: " + groupId + ", error: " + e.getMessage());
            Map<String, Object> contextInfo = getCurrentProcessContext();
            return apiClient.getGroupUsers(groupId, contextInfo); // This will return mock data
        }
    }
    
    /**
     * 根据组ID查找用户ID集合（带流程上下文信息）
     * @param groupId 组ID
     * @param processContext 流程上下文信息
     * @return 用户ID集合
     */
    public Set<String> findUserIdsByGroupId(String groupId, Map<String, Object> processContext) {
        try {
            return apiClient.getGroupUsers(groupId, processContext);
        } catch (Exception e) {
            LOG.writeLog("Failed to get users for group: " + groupId + " with context, error: " + e.getMessage());
            return apiClient.getGroupUsers(groupId, processContext); // This will return mock data
        }
    }

  /**
     * 从CommandContext中获取流程实例的上下文信息
     * @param commandContext 命令上下文
     * @return 包含流程实例ID、流程定义ID、流程变量等信息的Map
     */
    private Map<String, Object> getCurrentProcessContextFromCommandContext(CommandContext commandContext) {
        Map<String, Object> contextInfo = new HashMap<>();
        
        try {
            // 从传入的CommandContext获取认证用户ID
            if (commandContext != null) {
                String authenticatedUserId = commandContext.getAuthenticatedUserId();
                if (authenticatedUserId != null) {
                    contextInfo.put("authenticatedUserId", authenticatedUserId);
                }
            }
            
            // 获取当前BPMN执行上下文
            BpmnExecutionContext bpmnContext = Context.getBpmnExecutionContext();
            if (bpmnContext != null) {
                ExecutionEntity execution = bpmnContext.getExecution();
                if (execution != null) {
                    // 获取流程实例ID
                    String processInstanceId = execution.getProcessInstanceId();
                    if (processInstanceId != null) {
                        contextInfo.put("processInstanceId", processInstanceId);
                    }
                    
                    // 获取流程定义ID
                    String processDefinitionId = execution.getProcessDefinitionId();
                    if (processDefinitionId != null) {
                        contextInfo.put("processDefinitionId", processDefinitionId);
                    }
                    
                    // 获取执行ID
                    String executionId = execution.getId();
                    if (executionId != null) {
                        contextInfo.put("executionId", executionId);
                    }
                    
                    // 获取活动ID
                    String activityId = execution.getActivityId();
                    if (activityId != null) {
                        contextInfo.put("activityId", activityId);
                    }
                    
                    // 获取流程变量
                    try {
                        VariableMap variables = execution.getVariables();
                        if (variables != null && !variables.isEmpty()) {
                            contextInfo.put("processVariables", new HashMap<>(variables));
                        }
                    } catch (Exception e) {
                        LOG.writeLog("Could not retrieve process variables: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            LOG.writeLog("Could not retrieve process context information from CommandContext: " + e.getMessage());
        }
        
        return contextInfo;
    }
    
    /**
     * 获取当前流程实例的上下文信息
     * @return 包含流程实例ID、流程定义ID、流程变量等信息的Map
     */
    private Map<String, Object> getCurrentProcessContext() {
    Map<String, Object> contextInfo = new HashMap<>();
    
    try {
      // 获取当前认证用户ID
      CommandContext commandContext = Context.getCommandContext();
      if (commandContext != null) {
        String authenticatedUserId = commandContext.getAuthenticatedUserId();
        if (authenticatedUserId != null) {
          contextInfo.put("authenticatedUserId", authenticatedUserId);
        }
      }
      
      // 获取当前BPMN执行上下文
      BpmnExecutionContext bpmnContext = Context.getBpmnExecutionContext();
      if (bpmnContext != null) {
        ExecutionEntity execution = bpmnContext.getExecution();
        if (execution != null) {
          // 获取流程实例ID
          String processInstanceId = execution.getProcessInstanceId();
          if (processInstanceId != null) {
            contextInfo.put("processInstanceId", processInstanceId);
          }
          
          // 获取流程定义ID
          String processDefinitionId = execution.getProcessDefinitionId();
          if (processDefinitionId != null) {
            contextInfo.put("processDefinitionId", processDefinitionId);
          }
          
          // 获取执行ID
          String executionId = execution.getId();
          if (executionId != null) {
            contextInfo.put("executionId", executionId);
          }
          
          // 获取活动ID
          String activityId = execution.getActivityId();
          if (activityId != null) {
            contextInfo.put("activityId", activityId);
          }
          
          // 获取流程变量
          try {
            VariableMap variables = execution.getVariables();
            if (variables != null && !variables.isEmpty()) {
              contextInfo.put("processVariables", new HashMap<>(variables));
            }
          } catch (Exception e) {
            LOG.writeLog("Could not retrieve process variables: " + e.getMessage());
          }
        }
      }
    } catch (Exception e) {
      LOG.writeLog("Could not retrieve process context information: " + e.getMessage());
    }
    
    return contextInfo;
  }
}