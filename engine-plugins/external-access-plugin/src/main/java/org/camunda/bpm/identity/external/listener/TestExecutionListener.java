package org.camunda.bpm.identity.external.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.identity.external.ExternalAccessPluginLogger;
import org.camunda.bpm.identity.external.apiManager.ApiService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class TestExecutionListener implements ExecutionListener {

    @Autowired
    private ApiService apiService;

    private static final ExternalAccessPluginLogger LOG = ExternalAccessPluginLogger.LOGGER;

    @Override
    public void notify(DelegateExecution delegateExecution) {
//        LOG.writeLog("TestExecutionListener executed");
        final String processInstanceId = delegateExecution.getProcessInstanceId();
        final String processDefinitionId = delegateExecution.getProcessDefinitionId();
        // 当前正在执行的活动（如任务、网关等）的 ID
        final String currentActivityId = delegateExecution.getCurrentActivityId();
        // 获取流程发起人 ID (内置流程变量)
        String initiator = (String) delegateExecution.getVariable("initiator");

        LOG.writeLog("TestExecutionListener executed,starterId:" + initiator + ",processInstanceId:" + processInstanceId
        + ",processDefinitionId:" + processDefinitionId + ",currentActivityId:" + currentActivityId);
        // 在这里编写获取审批人列表的逻辑
        // 比如从数据库查询、调用外部服务等
        List<String> approverList = new ArrayList<>();
        approverList.add("689d3a34526d531b9a43dc3d");
        approverList.add("68999676dafeebc572d70dda");

        // 将列表作为流程变量存储
        delegateExecution.setVariable("approverList", approverList);
    }
}
