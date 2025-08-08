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
        this.userId = id;
        return this;
    }

    @Override
    public UserQuery userFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    @Override
    public UserQuery userFirstNameLike(String firstNameLike) {
        this.firstName = firstNameLike;
        return this;
    }

    @Override
    public UserQuery userLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    @Override
    public UserQuery userLastNameLike(String lastNameLike) {
        this.lastName = lastNameLike;
        return this;
    }

    @Override
    public UserQuery userEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public UserQuery userEmailLike(String emailLike) {
        this.email = emailLike;
        return this;
    }

    @Override
    public UserQuery memberOfGroup(String groupId) {
        this.groupId = groupId;
        return this;
    }

    @Override
    public UserQuery userIdIn(String... ids) {
        return this;
    }

    @Override
    public UserQuery potentialStarter(String procDefId) {
        return this;
    }

    @Override
    public UserQuery memberOfTenant(String tenantId) {
        return this;
    }

    @Override
    public UserQuery orderByUserId() {
        return this;
    }

    @Override
    public UserQuery orderByUserFirstName() {
        return this;
    }

    @Override
    public UserQuery orderByUserLastName() {
        return this;
    }

    @Override
    public UserQuery orderByUserEmail() {
        return this;
    }

    @Override
    public UserQuery asc() {
        return this;
    }

    @Override
    public UserQuery desc() {
        return this;
    }

    @Override
    public long count() {
        return list().size();
    }

    @Override
    public User singleResult() {
        List<User> results = list();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<User> list() {
        return users.stream()
            .filter(user -> userId == null || user.getId().equals(userId))
            .filter(user -> firstName == null || user.getFirstName().contains(firstName))
            .filter(user -> lastName == null || user.getLastName().contains(lastName))
            .filter(user -> email == null || user.getEmail().contains(email))
            .collect(Collectors.toList());
    }

    @Override
    public List<User> listPage(int firstResult, int maxResults) {
        List<User> allResults = list();
        int fromIndex = Math.min(firstResult, allResults.size());
        int toIndex = Math.min(firstResult + maxResults, allResults.size());
        return allResults.subList(fromIndex, toIndex);
    }

    @Override
    public List<User> unlimitedList() {
        return list();
    }
}