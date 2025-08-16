package org.camunda.bpm.identity.external.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.identity.external.ExternalAccessPluginLogger;
import org.camunda.bpm.identity.external.apiManager.ApiService;
import org.camunda.bpm.identity.external.entity.ProcessTaskInfo;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//import java.util.ArrayList;
//import java.util.List;

public class TestTaskListener implements TaskListener {

    @Autowired
    private ApiService apiService;

    private static final ExternalAccessPluginLogger LOG = ExternalAccessPluginLogger.LOGGER;

    @Override
    public void notify(DelegateTask delegateTask) {
        LOG.writeLog("TestTaskListener executed,delegateTask");
        // 在这里编写获取审批人列表的逻辑

        ProcessTaskInfo taskInfo = getTaskInfo(delegateTask);

        LOG.writeLog("taskInfo:" + taskInfo.toString());

        // 调用接口给审批人/候选人发送通知
        apiService.callPostApi("/person/notify",taskInfo);

    }

    private ProcessTaskInfo getTaskInfo(DelegateTask delegateTask) {

        ProcessTaskInfo taskInfo = new ProcessTaskInfo();

        // 获取流程发起人 ID (调用发起流程的rest接口时，业务代码填入的流程变量)
        String starterId = (String) delegateTask.getExecution().getVariable("startPeopleId");
        // 获取当前任务节点指定的审批人
        String assignee = delegateTask.getAssignee();
        // 流程实例id
        String processInstanceId = delegateTask.getProcessInstanceId();
        // currentActivityId
        String currentActivityId = delegateTask.getId();
        // currentActivityName
        String currentActivityName = delegateTask.getName();
        // currentActivityType
        BpmnModelElementInstance elementInstance = delegateTask.getExecution().getBpmnModelElementInstance();
        String currentActivityType = elementInstance.getElementType().getTypeName();
        // 获取当前任务节点候选的审批人
        Set<IdentityLink> candidates = delegateTask.getCandidates();
        List<String> candidateUsers = new ArrayList<>();
        if (candidates != null && !candidates.isEmpty()) {
            for (IdentityLink identityLink : candidates) {
                // 检查 IdentityLink 的类型是否为 CANDIDATE
                if (IdentityLinkType.CANDIDATE.equals(identityLink.getType())) {
                    if (identityLink.getUserId() != null) {
                        candidateUsers.add(identityLink.getUserId());
                    }
//                    if (identityLink.getGroupId() != null) {
//                        candidateGroups.add(identityLink.getGroupId());
//                    }
                }
            }
        }

        // 流程定义id
        String processDefinitionId = delegateTask.getProcessDefinitionId();
        // 获取流程定义的名称
        String processDefinitionName = delegateTask.getExecution().getProcessEngineServices()
                .getRepositoryService()
                .getProcessDefinition(processDefinitionId)
                .getName();

        taskInfo.setStarterId(starterId);
        taskInfo.setProcessDefinitionId(processDefinitionId);
        taskInfo.setProcessDefinitionName(processDefinitionName);
        taskInfo.setCurrentActivityId(currentActivityId);
        taskInfo.setCurrentActivityName(currentActivityName);
        taskInfo.setCurrentActivityAssignee(assignee);
        taskInfo.setCurrentActivityCandidateUsers(candidateUsers);
        taskInfo.setProcessInstanceId(processInstanceId);
        taskInfo.setCurrentActivityType(currentActivityType);

        return taskInfo;

    }
}
