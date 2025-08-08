package org.camunda.bpm.identity.external;

import org.camunda.bpm.engine.identity.*;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 外部访问身份提供者会话
 * 实现简单的用户和组管理
 */
public class ExternalAccessIdentityProviderSession implements ReadOnlyIdentityProvider, Session {

    // 模拟外部用户数据
    private static final List<User> EXTERNAL_USERS = Arrays.asList(
        createUser("external1", "External", "User1", "external1@company.com"),
        createUser("external2", "External", "User2", "external2@company.com"),
        createUser("guest", "Guest", "User", "guest@company.com")
    );

    // 模拟外部组数据
    private static final List<Group> EXTERNAL_GROUPS = Arrays.asList(
        createGroup("external-users", "External Users", "WORKFLOW"),
        createGroup("guests", "Guest Users", "WORKFLOW")
    );

    @Override
    public User findUserById(String userId) {
        return EXTERNAL_USERS.stream()
            .filter(user -> user.getId().equals(userId))
            .findFirst()
            .orElse(null);
    }

    @Override
    public UserQuery createUserQuery() {
        return new ExternalUserQuery(EXTERNAL_USERS);
    }

    @Override
    public UserQuery createUserQuery(CommandContext commandContext) {
        return new ExternalUserQuery(EXTERNAL_USERS);
    }

    @Override
    public NativeUserQuery createNativeUserQuery() {
        // 返回null表示不支持原生查询
        return null;
    }

    @Override
    public Group findGroupById(String groupId) {
        return EXTERNAL_GROUPS.stream()
            .filter(group -> group.getId().equals(groupId))
            .findFirst()
            .orElse(null);
    }

    @Override
    public GroupQuery createGroupQuery() {
        return new ExternalGroupQuery(EXTERNAL_GROUPS);
    }

    @Override
    public GroupQuery createGroupQuery(CommandContext commandContext) {
        return new ExternalGroupQuery(EXTERNAL_GROUPS);
    }

    @Override
    public boolean checkPassword(String userId, String password) {
        // 简单的密码验证：密码与用户ID相同
        return userId != null && userId.equals(password);
    }

    @Override
    public Tenant findTenantById(String tenantId) {
        // 返回null表示不支持租户
        return null;
    }

    @Override
    public TenantQuery createTenantQuery() {
        return new ExternalTenantQuery();
    }

    @Override
    public TenantQuery createTenantQuery(CommandContext commandContext) {
        return new ExternalTenantQuery();
    }

    @Override
    public void flush() {
        // 只读实现，无需刷新
    }

    @Override
    public void close() {
        // 无需关闭资源
    }

    // 辅助方法：创建用户
    private static User createUser(String id, String firstName, String lastName, String email) {
        User user = new UserEntity();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        return user;
    }

    // 辅助方法：创建组
    private static Group createGroup(String id, String name, String type) {
        Group group = new GroupEntity();
        group.setId(id);
        group.setName(name);
        group.setType(type);
        return group;
    }
}