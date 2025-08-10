package org.camunda.bpm.identity.external;

import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 外部用户查询实现
 */
public class ExternalUserQuery implements UserQuery {

    private static final ExternalAccessPluginLogger LOG = ExternalAccessPluginLogger.LOGGER;

    private final List<User> users;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String groupId;

    public ExternalUserQuery(List<User> users) {
        this.users = users;
    }

    @Override
    public UserQuery userId(String id) {
        LOG.writeLog("ExternalUserQuery userId:" + id);
        this.userId = id;
        return this;
    }

    @Override
    public UserQuery userFirstName(String firstName) {
        LOG.writeLog("ExternalUserQuery userFirstName:" + firstName);
        this.firstName = firstName;
        return this;
    }

    @Override
    public UserQuery userFirstNameLike(String firstNameLike) {
        LOG.writeLog("ExternalUserQuery userFirstNameLike:" + firstNameLike);
        this.firstName = firstNameLike;
        return this;
    }

    @Override
    public UserQuery userLastName(String lastName) {
        LOG.writeLog("ExternalUserQuery userLastName:" + lastName);
        this.lastName = lastName;
        return this;
    }

    @Override
    public UserQuery userLastNameLike(String lastNameLike) {
        LOG.writeLog("ExternalUserQuery userLastNameLike:" + lastNameLike);
        this.lastName = lastNameLike;
        return this;
    }

    @Override
    public UserQuery userEmail(String email) {
        LOG.writeLog("ExternalUserQuery userEmail:" + email);
        this.email = email;
        return this;
    }

    @Override
    public UserQuery userEmailLike(String emailLike) {
        LOG.writeLog("ExternalUserQuery userEmailLike:" + emailLike);
        this.email = emailLike;
        return this;
    }

    @Override
    public UserQuery memberOfGroup(String groupId) {
        LOG.writeLog("ExternalUserQuery memberOfGroup:" + groupId);
        this.groupId = groupId;
        return this;
    }

    @Override
    public UserQuery userIdIn(String... ids) {
        LOG.writeLog("ExternalUserQuery userIdIn:" + String.join(",", ids));
        return this;
    }

    @Override
    public UserQuery potentialStarter(String procDefId) {
        LOG.writeLog("ExternalUserQuery potentialStarter:" + procDefId);
        return this;
    }

    @Override
    public UserQuery memberOfTenant(String tenantId) {
        LOG.writeLog("ExternalUserQuery memberOfTenant:" + tenantId);
        return this;
    }

    @Override
    public UserQuery orderByUserId() {
        LOG.writeLog("ExternalUserQuery orderByUserId");
        return this;
    }

    @Override
    public UserQuery orderByUserFirstName() {
        LOG.writeLog("ExternalUserQuery orderByUserFirstName");
        return this;
    }

    @Override
    public UserQuery orderByUserLastName() {
        LOG.writeLog("ExternalUserQuery orderByUserLastName");
        return this;
    }

    @Override
    public UserQuery orderByUserEmail() {
        LOG.writeLog("ExternalUserQuery orderByUserEmail");
        return this;
    }

    @Override
    public UserQuery asc() {
        LOG.writeLog("ExternalUserQuery asc");
        return this;
    }

    @Override
    public UserQuery desc() {
        LOG.writeLog("ExternalUserQuery desc");
        return this;
    }

    @Override
    public long count() {
        LOG.writeLog("ExternalUserQuery count");
        return list().size();
    }

    @Override
    public User singleResult() {
        LOG.writeLog("ExternalUserQuery singleResult");
        List<User> results = list();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<User> list() {
        LOG.writeLog("ExternalUserQuery list");
        return users.stream()
            .filter(user -> userId == null || user.getId().equals(userId))
            .filter(user -> firstName == null || user.getFirstName().contains(firstName))
            .filter(user -> lastName == null || user.getLastName().contains(lastName))
            .filter(user -> email == null || user.getEmail().contains(email))
            .collect(Collectors.toList());
    }

    @Override
    public List<User> listPage(int firstResult, int maxResults) {
        LOG.writeLog("ExternalUserQuery listPage");
        List<User> allResults = list();
        int fromIndex = Math.min(firstResult, allResults.size());
        int toIndex = Math.min(firstResult + maxResults, allResults.size());
        return allResults.subList(fromIndex, toIndex);
    }

    @Override
    public List<User> unlimitedList() {
        LOG.writeLog("ExternalUserQuery unlimitedList");
        return list();
    }
}