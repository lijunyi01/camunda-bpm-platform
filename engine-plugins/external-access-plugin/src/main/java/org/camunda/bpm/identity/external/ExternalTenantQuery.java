package org.camunda.bpm.identity.external;

import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * 外部租户查询实现
 * 提供空的租户列表，因为此插件不使用多租户功能
 */
public class ExternalTenantQuery extends AbstractQuery<TenantQuery, Tenant> implements TenantQuery {

    private static final long serialVersionUID = 1L;

    public ExternalTenantQuery() {
        super();
    }

    public ExternalTenantQuery(CommandExecutor commandExecutor) {
        super(commandExecutor);
    }

    @Override
    public TenantQuery tenantId(String tenantId) {
        return this;
    }

    @Override
    public TenantQuery tenantIdIn(String... tenantIds) {
        return this;
    }

    @Override
    public TenantQuery tenantName(String tenantName) {
        return this;
    }

    @Override
    public TenantQuery tenantNameLike(String tenantNameLike) {
        return this;
    }

    @Override
    public TenantQuery userMember(String userId) {
        return this;
    }

    @Override
    public TenantQuery groupMember(String groupId) {
        return this;
    }

    @Override
    public TenantQuery includingGroupsOfUser(boolean includingGroupsOfUser) {
        return this;
    }

    @Override
    public TenantQuery orderByTenantId() {
        return this;
    }

    @Override
    public TenantQuery orderByTenantName() {
        return this;
    }

    @Override
    public long executeCount(CommandContext commandContext) {
        return 0;
    }

    @Override
    public List<Tenant> executeList(CommandContext commandContext, org.camunda.bpm.engine.impl.Page page) {
        // 返回空列表，因为此插件不使用多租户功能
        return new ArrayList<>();
    }
}