package org.camunda.bpm.identity.external;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.identity.external.apiVO.BpmnResponseVO;
import org.camunda.bpm.identity.external.apiVO.UsersByGroupQueryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.camunda.bpm.identity.external.apiManager.ApiService;

/**
 * 外部用户查询服务
 * 通过 ExternalAccessSpringPlugin 注册到 ProcessEngine 的 beans 映射中
 * 可以在 BPMN 表达式中使用：${externalUserQuery.getCandidateUsersForGroup("guests", execution)}
 */
public class ExternalUserQueryService {

    private static final ExternalAccessPluginLogger LOG = ExternalAccessPluginLogger.LOGGER;

    @Autowired
    private ApiService apiService;

    // 这个方法将会在 BPMN 表达式中被调用；用于获取候选人列表（候选人claim后都可审批）
    // 前端表达式：${externalUserQuery.getCandidateUsersForGroup("myGroup", execution)}
    public List<String> getCandidateUsersForGroup(String groupName, DelegateExecution execution) {

        // 获取当前流程id
        String processInstanceId = execution.getProcessInstanceId();
        // 获取当前任务节点id
        String taskId = execution.getCurrentActivityId();
        // 获取当前流程模型id
        String processDefinitionId = execution.getProcessDefinitionId();
        // 提取variables里的值，放到map里
        Map<String, Object> variableMap = execution.getVariables();
        LOG.writeLog("getCandidateUsersForGroup, variables: " + variableMap);

        // 获取流程变量
        String startPeopleId = (String) execution.getVariable("startPeopleId");

        UsersByGroupQueryVO queryVO = new UsersByGroupQueryVO();
        queryVO.setGroupName(groupName);
        queryVO.setStartPeopleId(startPeopleId);
        queryVO.setProcessInstanceId(processInstanceId);
        queryVO.setProcessDefinitionId(processDefinitionId);
        queryVO.setTaskId(taskId);

        LOG.writeLog("getCandidateUsersForGroup query param:" + queryVO.toString());

        // 调用外部接口，获取groupName对应的人员
        // Object response = apiService.callPostApi("/person/getPersonByOrgId", queryVO);
        BpmnResponseVO<List<String>> response = apiService.getUsersByGroupId(queryVO);
        LOG.writeLog("bpmn-service call result:" + response.toString());

        List<String> users = new ArrayList<>();
        if (response.getCode().equals(200)) {
            users = response.getResult();
        }

        // 假数据
        // users.add("guest");
        // users.add("external1");

        LOG.writeLog("getCandidateUsersForGroup, result: " + users.toString());

        // 返回一个用户ID列表
        return users;
    }

    // 这个方法将会在 BPMN 表达式中被调用; 用于动态获取指定的审批人
    // 前端表达式：${externalUserQuery.getAssigneeForGroup("myGroup", execution)}
    public String getAssigneeForGroup(String groupName, DelegateExecution execution) {
        List<String> users = getCandidateUsersForGroup(groupName, execution);
        LOG.writeLog("getAssigneeForGroup, result: " + users.toString());
        if (users.isEmpty()) {
            return null;
        }else {
            return users.get(0);
        }
    }
}