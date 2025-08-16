package org.camunda.bpm.identity.external.apiVO;

public class UsersByParamsQueryVO {
    private String paramString;
    // 发起人员id
    private String startPeopleId;
    // 流程定义id
    private String processDefinitionId;
    // 流程实例id
    private String processInstanceId;
    // 流程实例任务节点id
    private String taskId;
    // tenantId
    private String tenantId;

    @Override
    public String toString() {
        return "UsersByParamsQueryVO{" +
                "paramString='" + paramString + '\'' +
                ", startPeopleId='" + startPeopleId + '\'' +
                ", processDefinitionId='" + processDefinitionId + '\'' +
                ", processInstanceId='" + processInstanceId + '\'' +
                ", taskId='" + taskId + '\'' +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }

    public String getParamString() {
        return paramString;
    }

    public void setParamString(String paramString) {
        this.paramString = paramString;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getStartPeopleId() {
        return startPeopleId;
    }

    public void setStartPeopleId(String startPeopleId) {
        this.startPeopleId = startPeopleId;
    }


}
