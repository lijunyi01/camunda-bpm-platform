package org.camunda.bpm.identity.external.apiVO;

public class AllUsersQueryVO {
    // tenantId
    private String tenantId;

    @Override
    public String toString() {
        return "AllUsersQueryVO{" +
                "tenantId='" + tenantId + '\'' +
                '}';
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
