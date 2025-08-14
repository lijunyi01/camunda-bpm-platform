package org.camunda.bpm.identity.external.apiManager;

import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.stereotype.Component;

/**
 * 外部API配置属性
 */
// @Component
@ConfigurationProperties(prefix = "camunda.bpm.external-access.api")
public class ApiProperties {
    
    /**
     * 外部API基础URL
     */
    private String baseUrl = "http://bpmn-service-bpmn-service";
    
    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 5000;
    
    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 10000;
    
    /**
     * API认证token
     */
    private String authToken;
    
    /**
     * 用户查询接口路径
     */
    private String userQueryPath = "/api/users";
    
    /**
     * 组查询接口路径
     */
    private String groupQueryPath = "/api/groups";
    
    /**
     * 用户认证接口路径
     */
    private String authPath = "/api/auth";
    
    // Getters and Setters
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public int getConnectTimeout() {
        return connectTimeout;
    }
    
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    
    public int getReadTimeout() {
        return readTimeout;
    }
    
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    public String getUserQueryPath() {
        return userQueryPath;
    }
    
    public void setUserQueryPath(String userQueryPath) {
        this.userQueryPath = userQueryPath;
    }
    
    public String getGroupQueryPath() {
        return groupQueryPath;
    }
    
    public void setGroupQueryPath(String groupQueryPath) {
        this.groupQueryPath = groupQueryPath;
    }
    
    public String getAuthPath() {
        return authPath;
    }
    
    public void setAuthPath(String authPath) {
        this.authPath = authPath;
    }
}