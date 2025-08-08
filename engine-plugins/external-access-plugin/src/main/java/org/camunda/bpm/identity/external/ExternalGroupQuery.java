package org.camunda.bpm.identity.external;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.impl.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 外部组查询实现
 */
public class ExternalGroupQuery implements GroupQuery {

    private final List<Group> groups;
    private String groupId;
    private String groupName;
    private String groupType;
    private String userId;

    public ExternalGroupQuery(List<Group> groups) {
        this.groups = groups;
    }

    @Override
    public GroupQuery groupId(String id) {
        this.groupId = id;
        return this;
    }

    @Override
    public GroupQuery groupName(String name) {
        this.groupName = name;
        return this;
    }

    @Override
    public GroupQuery groupNameLike(String nameLike) {
        this.groupName = nameLike;
        return this;
    }

    @Override
    public GroupQuery groupType(String type) {
        this.groupType = type;
        return this;
    }

    @Override
    public GroupQuery groupMember(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public GroupQuery groupIdIn(String... ids) {
        return this;
    }

    @Override
    public GroupQuery potentialStarter(String procDefId) {
        return this;
    }

    @Override
    public GroupQuery memberOfTenant(String tenantId) {
        return this;
    }

    @Override
    public GroupQuery orderByGroupId() {
        return this;
    }

    @Override
    public GroupQuery orderByGroupName() {
        return this;
    }

    @Override
    public GroupQuery orderByGroupType() {
        return this;
    }

    @Override
    public GroupQuery asc() {
        return this;
    }

    @Override
    public GroupQuery desc() {
        return this;
    }

    @Override
    public long count() {
        return list().size();
    }

    @Override
    public Group singleResult() {
        List<Group> results = list();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Group> list() {
        return groups.stream()
            .filter(group -> groupId == null || group.getId().equals(groupId))
            .filter(group -> groupName == null || group.getName().contains(groupName))
            .filter(group -> groupType == null || group.getType().equals(groupType))
            .collect(Collectors.toList());
    }

    @Override
    public List<Group> listPage(int firstResult, int maxResults) {
        List<Group> allResults = list();
        int fromIndex = Math.min(firstResult, allResults.size());
        int toIndex = Math.min(firstResult + maxResults, allResults.size());
        return allResults.subList(fromIndex, toIndex);
    }

    @Override
    public List<Group> unlimitedList() {
        return list();
    }
}