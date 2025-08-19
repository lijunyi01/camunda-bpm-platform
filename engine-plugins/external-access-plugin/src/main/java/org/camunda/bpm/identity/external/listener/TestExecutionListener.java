package org.camunda.bpm.identity.external.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.identity.external.ExternalAccessPluginLogger;
import org.camunda.bpm.identity.external.apiManager.ApiService;
import org.camunda.bpm.identity.external.apiVO.BpmnResponseVO;
import org.camunda.bpm.identity.external.apiVO.UsersByParamsQueryVO;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class TestExecutionListener implements ExecutionListener {

    @Autowired
    private ApiService apiService;

    private static final ExternalAccessPluginLogger LOG = ExternalAccessPluginLogger.LOGGER;

    @Override
    public void notify(DelegateExecution delegateExecution) {
        final String processInstanceId = delegateExecution.getProcessInstanceId();
        final String processDefinitionId = delegateExecution.getProcessDefinitionId();
        // 当前正在执行的活动（如任务、网关等）的 ID
        final String currentActivityId = delegateExecution.getCurrentActivityId();
        // 获取流程发起人 ID
        String starterId = (String) delegateExecution.getVariable("startPeopleId");

        LOG.writeLog("TestExecutionListener executed,starterId:" + starterId + ",processInstanceId:" + processInstanceId
        + ",processDefinitionId:" + processDefinitionId + ",currentActivityId:" + currentActivityId);

        FlowNode startEvent = (FlowNode) delegateExecution.getBpmnModelElementInstance();
        List<UserTask> followingUserTasks = new ArrayList<>();
        Set<FlowNode> visitedNodes = new HashSet<>(); // 用于防止无限循环

        // 从开始事件节点开始，递归查找所有后续的用户任务
        findFollowingUserTasks(startEvent, followingUserTasks, visitedNodes);
        LOG.writeLog("Found following user tasks: " + followingUserTasks.size());
        if(followingUserTasks.isEmpty()) {
            LOG.writeLog("No user tasks found after the start event.");
            return;
        }
        for(UserTask userTask : followingUserTasks) {
            LOG.writeLog("User Task ID: " + userTask.getId() + ", Name: " + userTask.getName());
            // 获取该任务的循环特性 (LoopCharacteristics)
            final LoopCharacteristics loopCharacteristics = userTask.getLoopCharacteristics();
            // 判断是否存在循环特性且其类型为多实例 (MultiInstanceLoopCharacteristics)
            if (loopCharacteristics instanceof MultiInstanceLoopCharacteristics) {
                // 将其转换为 MultiInstanceLoopCharacteristics 类型
                MultiInstanceLoopCharacteristics multiInstance = (MultiInstanceLoopCharacteristics) loopCharacteristics;
                // 调用 isSequential() 方法进行最终判断
                if (multiInstance.isSequential()) {
                    LOG.writeLog("这是一个【串行】多实例审批节点。");
                    // 获取查询审批人相关的扩展属性
                    Map<String, String> extensionProperties = getExtensionProperties(userTask);
                    LOG.writeLog("extensionProperties:" + extensionProperties.toString());
                    // 从扩展属性中获取特定的属性（人员查询相关的）
                    String myKey = null;
                    String myValue = null;
                    for(String key: extensionProperties.keySet()) {
                        if(key.startsWith("approverList")) {
                            myKey = key;
                            break;
                        }
                    }
                    if(myKey != null) {
                        myValue = extensionProperties.get(myKey);
                    }
                    UsersByParamsQueryVO queryVO = new UsersByParamsQueryVO();
                    queryVO.setParamString(myValue);
                    queryVO.setStartPeopleId(starterId);
                    queryVO.setProcessInstanceId(processInstanceId);
                    queryVO.setProcessDefinitionId(processDefinitionId);
                    queryVO.setTaskId(currentActivityId);

                    // 在这里编写获取审批人列表的逻辑
                    BpmnResponseVO<List<String>> response = apiService.getUsersByParams(queryVO);
                    List<String> approverList = new ArrayList<>();
                    if (response.getCode().equals(200)) {
                        approverList = response.getResult();
                    }
                    LOG.writeLog("myKey: " + myKey + ", myValue: " + myValue + " => approverList: " + approverList.toString());
                    // 将列表作为流程变量存储
                    delegateExecution.setVariable(myKey, approverList);
                } else {
                    LOG.writeLog("这是一个【并行】多实例审批节点。");
                    // do nothing...
                }
            }
        }
    }

    /**
     * 递归方法，用于查找一个节点之后的所有用户任务
     * @param currentNode      当前遍历到的节点
     * @param foundUserTasks   用于存储找到的用户任务的列表
     * @param visitedNodes     用于记录已访问的节点，防止死循环
     */
    private void findFollowingUserTasks(FlowNode currentNode, List<UserTask> foundUserTasks, Set<FlowNode> visitedNodes) {
        // 如果当前节点已经访问过，则直接返回，避免无限循环
        if (visitedNodes.contains(currentNode)) {
            return;
        }
        visitedNodes.add(currentNode);

        // 获取当前节点的所有出向顺序流
        Collection<SequenceFlow> outgoingFlows = currentNode.getOutgoing();

        for (SequenceFlow flow : outgoingFlows) {
            FlowNode targetNode = flow.getTarget();

            // 如果目标节点是用户任务，则将其添加到结果列表中
            if (targetNode instanceof UserTask) {
                foundUserTasks.add((UserTask) targetNode);
            }

            // 如果目标节点不是结束事件，则继续从该节点往下递归查找
            // 这会自然地处理掉网关、服务任务等中间节点
            if (!(targetNode instanceof EndEvent)) {
                findFollowingUserTasks(targetNode, foundUserTasks, visitedNodes);
            }
        }
    }

    /**
     * 获取用户任务的扩展属性
     * @param userTask 用户任务实例
     * @return 包含扩展属性的 Map
     */
    private Map<String, String> getExtensionProperties(UserTask userTask) {
        Map<String, String> properties = new HashMap<>();
        ExtensionElements extensionElements = userTask.getExtensionElements();
        if (extensionElements != null) {
            // Query for Camunda Properties
            CamundaProperties camundaProperties = extensionElements.getElementsQuery()
                    .filterByType(CamundaProperties.class)
                    .singleResult();

            if (camundaProperties != null) {
                Collection<CamundaProperty> camundaPropertyList = camundaProperties.getCamundaProperties();
                for (CamundaProperty property : camundaPropertyList) {
                    String propertyName = property.getCamundaName();
                    String propertyValue = property.getCamundaValue();
                    properties.put(propertyName, propertyValue);
                }
            }
        }
        return properties;
    }
}