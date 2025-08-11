package org.camunda.bpm.identity.external;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * 外部API客户端
 * 负责与外部HTTP接口进行通信
 */
public class ExternalApiClient {
    
    private static final ExternalAccessPluginLogger LOG = ExternalAccessPluginLogger.LOGGER;
    
    // 配置项
    private final String baseUrl;
    private final int connectTimeout;
    private final int readTimeout;
    
    // 用户-组关系映射（mock数据）
    private static final Map<String, Set<String>> USER_GROUP_MEMBERSHIPS = new HashMap<>();
    
    static {
        // 初始化用户-组关系映射
        USER_GROUP_MEMBERSHIPS.put("external1", new HashSet<>(Arrays.asList("external-users")));
        USER_GROUP_MEMBERSHIPS.put("external2", new HashSet<>(Arrays.asList("external-users")));
        USER_GROUP_MEMBERSHIPS.put("guest", new HashSet<>(Arrays.asList("guests")));
        USER_GROUP_MEMBERSHIPS.put("nx1", new HashSet<>(Arrays.asList("nx-users")));
        USER_GROUP_MEMBERSHIPS.put("nx2", new HashSet<>(Arrays.asList("nx-users")));
        USER_GROUP_MEMBERSHIPS.put("nx3", new HashSet<>(Arrays.asList("nx-users")));
        USER_GROUP_MEMBERSHIPS.put("nx4", new HashSet<>(Arrays.asList("nx-users")));
        USER_GROUP_MEMBERSHIPS.put("nx5", new HashSet<>(Arrays.asList("nx-users")));
        USER_GROUP_MEMBERSHIPS.put("nx6", new HashSet<>(Arrays.asList("nx-users")));
        USER_GROUP_MEMBERSHIPS.put("nx7", new HashSet<>(Arrays.asList("nx-users")));
        USER_GROUP_MEMBERSHIPS.put("nx8", new HashSet<>(Arrays.asList("nx-users")));
    }
    
    public ExternalApiClient() {
        // 默认配置
        this.baseUrl = System.getProperty("external.api.baseUrl", "http://localhost:8080/api");
        this.connectTimeout = Integer.parseInt(System.getProperty("external.api.connectTimeout", "5000"));
        this.readTimeout = Integer.parseInt(System.getProperty("external.api.readTimeout", "10000"));
    }
    
    public ExternalApiClient(String baseUrl, int connectTimeout, int readTimeout) {
        this.baseUrl = baseUrl;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }
    
    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        LOG.writeLog("ExternalApiClient getAllUsers - calling external API");
        
        try {
            // 实际的HTTP调用
            String response = performHttpGet("/users");
            LOG.writeLog("External API response: " + response);
            
            // 暂时返回mock数据
            return getMockUsers();
            
        } catch (Exception e) {
            LOG.writeLog("Failed to call external API, using mock data: " + e.getMessage());
            return getMockUsers();
        }
    }
    
    /**
     * 根据用户ID获取用户
     */
    public User getUserById(String userId) {
        LOG.writeLog("ExternalApiClient getUserById: " + userId + " - calling external API");
        
        try {
            // 实际的HTTP调用
            String response = performHttpGet("/users/" + userId);
            LOG.writeLog("External API response: " + response);
            
            // 暂时返回mock数据
            return getMockUsers().stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElse(null);
                
        } catch (Exception e) {
            LOG.writeLog("Failed to call external API, using mock data: " + e.getMessage());
            return getMockUsers().stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElse(null);
        }
    }
    
    /**
     * 获取所有组
     */
    public List<Group> getAllGroups() {
        LOG.writeLog("ExternalApiClient getAllGroups - calling external API");
        
        try {
            // 实际的HTTP调用
            String response = performHttpGet("/groups");
            LOG.writeLog("External API response: " + response);
            
            // 暂时返回mock数据
            return getMockGroups();
            
        } catch (Exception e) {
            LOG.writeLog("Failed to call external API, using mock data: " + e.getMessage());
            return getMockGroups();
        }
    }
    
    /**
     * 根据组ID获取组
     */
    public Group getGroupById(String groupId) {
        LOG.writeLog("ExternalApiClient getGroupById: " + groupId + " - calling external API");
        
        try {
            // 实际的HTTP调用
            String response = performHttpGet("/groups/" + groupId);
            LOG.writeLog("External API response: " + response);
            
            // 暂时返回mock数据
            return getMockGroups().stream()
                .filter(group -> group.getId().equals(groupId))
                .findFirst()
                .orElse(null);
                
        } catch (Exception e) {
            LOG.writeLog("Failed to call external API, using mock data: " + e.getMessage());
            return getMockGroups().stream()
                .filter(group -> group.getId().equals(groupId))
                .findFirst()
                .orElse(null);
        }
    }
    
    /**
     * 验证用户密码
     */
    public boolean checkPassword(String userId, String password) {
        LOG.writeLog("ExternalApiClient checkPassword: " + userId + " - calling external API");
        
        try {
            // 实际的HTTP调用
            String requestBody = "{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}";
            String response = performHttpPost("/auth/validate", requestBody);
            LOG.writeLog("External API response: " + response);
            
            // 暂时返回mock逻辑：密码与用户ID相同
            return userId != null && userId.equals(password);
            
        } catch (Exception e) {
            LOG.writeLog("Failed to call external API, using mock logic: " + e.getMessage());
            return userId != null && userId.equals(password);
        }
    }
    
    /**
     * 获取用户所属的组
     */
    public Set<String> getUserGroups(String userId) {
        LOG.writeLog("ExternalApiClient getUserGroups: " + userId + " - calling external API");
        
        try {
            // 实际的HTTP调用
            String response = performHttpGet("/users/" + userId + "/groups");
            LOG.writeLog("External API response: " + response);
            
            // 暂时返回mock数据
            return USER_GROUP_MEMBERSHIPS.getOrDefault(userId, new HashSet<>());
            
        } catch (Exception e) {
            LOG.writeLog("Failed to call external API, using mock data: " + e.getMessage());
            return USER_GROUP_MEMBERSHIPS.getOrDefault(userId, new HashSet<>());
        }
    }

    /**
     * 获取组下的所有用户
     * @param groupId 组ID
     * @param contextInfo 流程上下文信息
     * @return 用户ID集合
     */
    public Set<String> getGroupUsers(String groupId, Map<String, Object> contextInfo) {
        LOG.writeLog("ExternalApiClient getGroupUsers: " + groupId + " with context: " + contextInfo + " - calling external API");
        
        try {
            // 构建包含上下文信息的请求体
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("groupId", groupId);
            if (contextInfo != null && !contextInfo.isEmpty()) {
                requestData.put("context", contextInfo);
            }
            String requestBody = buildRequestWithContext(requestData);
            
            // 实际的HTTP调用，包含上下文信息
            String response = performHttpPost("/groups/" + groupId + "/users", requestBody);
            LOG.writeLog("External API response: " + response);
            
            // 暂时返回mock数据：通过反向查找用户-组关系映射
            return getMockGroupUsers(groupId);
            
        } catch (Exception e) {
            LOG.writeLog("Failed to call external API, using mock data: " + e.getMessage());
            return getMockGroupUsers(groupId);
        }
    }
    
    /**
     * 获取组下的所有用户（兼容性方法）
     * @param groupId 组ID
     * @return 用户ID集合
     */
    public Set<String> getGroupUsers(String groupId) {
        return getGroupUsers(groupId, new HashMap<>());
    }
    
    /**
     * 获取Mock组用户数据（通过反向查找用户-组关系映射）
     */
    private Set<String> getMockGroupUsers(String groupId) {
        Set<String> userIds = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : USER_GROUP_MEMBERSHIPS.entrySet()) {
            if (entry.getValue().contains(groupId)) {
                userIds.add(entry.getKey());
            }
        }
        return userIds;
    }
    
    /**
     * 执行HTTP GET请求
     * @param endpoint API端点
     * @return 响应内容
     */
    private String performHttpGet(String endpoint) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            
            int responseCode = connection.getResponseCode();
            LOG.writeLog("HTTP GET " + endpoint + " - Response Code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readResponse(connection);
            } else {
                throw new IOException("HTTP request failed with response code: " + responseCode);
            }
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * 执行HTTP POST请求
     * @param endpoint API端点
     * @param requestBody 请求体
     * @return 响应内容
     */
    private String performHttpPost(String endpoint, String requestBody) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            
            // 写入请求体
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            LOG.writeLog("HTTP POST " + endpoint + " - Response Code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readResponse(connection);
            } else {
                throw new IOException("HTTP request failed with response code: " + responseCode);
            }
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * 构建包含上下文信息的请求体
     * @param contextInfo 上下文信息
     * @return JSON格式的请求体
     */
    private String buildRequestWithContext(Map<String, Object> contextInfo) {
        if (contextInfo == null || contextInfo.isEmpty()) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : contextInfo.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
            } else if (value instanceof Map) {
                json.append(buildRequestWithContext((Map<String, Object>) value));
            } else {
                json.append("\"").append(value != null ? value.toString() : "null").append("\"");
            }
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * 读取HTTP响应
     */
    private String readResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
    
    /**
     * 获取Mock用户数据
     */
    private List<User> getMockUsers() {
        return Arrays.asList(
            createUser("external1", "External", "User1", "external1@company.com"),
            createUser("external2", "External", "User2", "external2@company.com"),
            createUser("guest", "Guest", "User", "guest@company.com"),
            createUser("nx1", "nx1", "NXUser1", "nx1@company.com"),
            createUser("nx2", "nx2", "NXUser2", "nx2@company.com"),
            createUser("nx3", "nx3", "NXUser3", "nx3@company.com"),
            createUser("nx4", "nx4", "NXUser4", "nx4@company.com"),
            createUser("nx5", "nx5", "NXUser5", "nx5@company.com"),
            createUser("nx6", "nx6", "NXUser6", "nx6@company.com"),
            createUser("nx7", "nx7", "NXUser7", "nx7@company.com"),
            createUser("nx8", "nx8", "NXUser8", "nx8@company.com")
        );
    }
    
    /**
     * 获取Mock组数据
     */
    private List<Group> getMockGroups() {
        return Arrays.asList(
            createGroup("external-users", "External Users", "WORKFLOW"),
            createGroup("guests", "Guest Users", "WORKFLOW"),
            createGroup("nx-users", "NX Users", "WORKFLOW")
        );
    }
    
    /**
     * 创建用户实体
     */
    private User createUser(String id, String firstName, String lastName, String email) {
        User user = new UserEntity();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        return user;
    }
    
    /**
     * 创建组实体
     */
    private Group createGroup(String id, String name, String type) {
        Group group = new GroupEntity();
        group.setId(id);
        group.setName(name);
        group.setType(type);
        return group;
    }
}