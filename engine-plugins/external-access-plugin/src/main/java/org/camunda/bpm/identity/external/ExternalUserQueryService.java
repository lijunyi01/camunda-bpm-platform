package org.camunda.bpm.identity.external;

import java.util.List;
import java.util.ArrayList;
import org.camunda.bpm.engine.delegate.DelegateExecution;

/**
 * 外部用户查询服务
 * 通过 ExternalAccessSpringPlugin 注册到 ProcessEngine 的 beans 映射中
 * 可以在 BPMN 表达式中使用：${externalUserQuery.getCandidateUsersForGroup("guests", execution)}
 */
public class ExternalUserQueryService {

    private static final ExternalAccessPluginLogger LOG = ExternalAccessPluginLogger.LOGGER;

    // 假设你的 Identity Provider 插件有一个获取用户的方法
    // public List<String> findCandidateUsers(String groupName, String departmentId) { ... }

    // 这个方法将会在 BPMN 表达式中被调用
    public List<String> getCandidateUsersForGroup(String groupName, DelegateExecution execution) {
        
        LOG.writeLog("getCandidateUsersForGroup, groupName: " + groupName);

        // 如何获取当前流程id
        String processInstanceId = execution.getProcessInstanceId();
        LOG.writeLog("getCandidateUsersForGroup, processInstanceId: " + processInstanceId);

        // 如何获取当前流程模型id
        String processDefinitionId = execution.getProcessDefinitionId();
        LOG.writeLog("getCandidateUsersForGroup, processDefinitionId: " + processDefinitionId);

        // 查看所有的流程变量
        LOG.writeLog("getCandidateUsersForGroup, all variables: " + execution.getVariables());

        // 获取流程变量
        String amount = (String) execution.getVariable("amount");
        LOG.writeLog("getCandidateUsersForGroup, amount: " + amount);

        // 然后调用你插件中获取候选人的核心逻辑
        // 假设你有一个方法来完成这个任务
        List<String> users = new ArrayList<>();

        // 返回一个用户ID列表
        return users;
    }
}