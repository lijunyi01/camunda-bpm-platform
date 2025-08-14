package org.camunda.bpm.identity.external.taskListener;

import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.identity.external.ExternalAccessPluginLogger;

import java.util.ArrayList;
import java.util.List;

public class TestTaskListener implements TaskListener {

    private static final ExternalAccessPluginLogger LOG = ExternalAccessPluginLogger.LOGGER;

    @Override
    public void notify(DelegateTask delegateTask) {
        LOG.writeLog("TestTaskListener executed,delegateTask:" + delegateTask.toString());
        // 在这里编写获取审批人列表的逻辑
        // 比如从数据库查询、调用外部服务等
        List<String> approverList = new ArrayList<>();
        approverList.add("689d3a34526d531b9a43dc3d");
        approverList.add("68999676dafeebc572d70dda");

        // 将列表作为流程变量存储
        // 仅当前任务可见
        delegateTask.setVariable("approverList", approverList);
        // 全流程可见
        delegateTask.getExecution().setVariable("approverList", approverList);
    }
}
