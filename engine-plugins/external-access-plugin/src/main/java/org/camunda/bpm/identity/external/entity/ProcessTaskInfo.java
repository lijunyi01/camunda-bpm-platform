package org.camunda.bpm.identity.external.entity;

import java.util.List;

public class ProcessTaskInfo {

    private String starterId;
    private String processDefinitionId;
    private String processDefinitionName;
    private String processInstanceId;
    private String currentActivityId;
    private String currentActivityName;
    private String currentActivityType;
    private String currentActivityAssignee;
    private List<String> currentActivityCandidateUsers;

    public String getStarterId() {
        return starterId;
    }

    public void setStarterId(String starterId) {
        this.starterId = starterId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getCurrentActivityId() {
        return currentActivityId;
    }

    public void setCurrentActivityId(String currentActivityId) {
        this.currentActivityId = currentActivityId;
    }

    public String getCurrentActivityName() {
        return currentActivityName;
    }

    public void setCurrentActivityName(String currentActivityName) {
        this.currentActivityName = currentActivityName;
    }

    public String getCurrentActivityType() {
        return currentActivityType;
    }

    public void setCurrentActivityType(String currentActivityType) {
        this.currentActivityType = currentActivityType;
    }

    public String getCurrentActivityAssignee() {
        return currentActivityAssignee;
    }

    public void setCurrentActivityAssignee(String currentActivityAssignee) {
        this.currentActivityAssignee = currentActivityAssignee;
    }

    public List<String> getCurrentActivityCandidateUsers() {
        return currentActivityCandidateUsers;
    }

    public void setCurrentActivityCandidateUsers(List<String> currentActivityCandidateUsers) {
        this.currentActivityCandidateUsers = currentActivityCandidateUsers;
    }

    @Override
    public String toString() {
        return "ProcessTaskInfo{" +
                "starterId='" + starterId + '\'' +
                ", processDefinitionId='" + processDefinitionId + '\'' +
                ", processDefinitionName='" + processDefinitionName + '\'' +
                ", processInstanceId='" + processInstanceId + '\'' +
                ", currentActivityId='" + currentActivityId + '\'' +
                ", currentActivityName='" + currentActivityName + '\'' +
                ", currentActivityType='" + currentActivityType + '\'' +
                ", currentActivityAssignee='" + currentActivityAssignee + '\'' +
                ", currentActivityCandidateUsers=" + currentActivityCandidateUsers +
                '}';
    }
}
