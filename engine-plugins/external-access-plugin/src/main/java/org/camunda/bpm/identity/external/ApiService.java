package org.camunda.bpm.identity.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * 外部API服务类
 * 提供调用外部REST接口的方法
 */
@Service
public class ApiService {
    
    private static final ExternalAccessPluginLogger LOG = ExternalAccessPluginLogger.LOGGER;
    
    @Autowired
    @Qualifier("externalApiRestTemplate")
    private RestTemplate restTemplate;
    
    @Autowired
    private ApiProperties apiProperties;
    
    /**
     * 查询用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    public Map<String, Object> findUser(String userId) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(apiProperties.getBaseUrl())
                    .path(apiProperties.getUserQueryPath())
                    .path("/" + userId)
                    .toUriString();
            
            LOG.writeLog("Calling external API to find user: " + userId);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                LOG.writeLog("External API returned non-success status: " + response.getStatusCode());
                return null;
            }
            
        } catch (RestClientException e) {
            LOG.writeLog("Error calling external API to find user: " + userId + ", error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 查询用户列表
     * 
     * @param groupId 组ID（可选）
     * @param departmentId 部门ID（可选）
     * @return 用户列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getUsersByGroup(String groupId, Map<String, Object> variableMap) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(apiProperties.getBaseUrl())
                    .path("/api/getUsersByGroup");
            
            if (groupId != null && !groupId.isEmpty()) {
                builder.queryParam("groupId", groupId);
            }
            if (departmentId != null && !departmentId.isEmpty()) {
                builder.queryParam("departmentId", departmentId);
            }
            
            String url = builder.toUriString();
            
            LOG.writeLog("Calling external API to find users with groupId:" + groupId + " departmentId:" + departmentId);
            
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                LOG.writeLog("External API returned non-success status:" + response.getStatusCode());
                return null;
            }
            
        } catch (RestClientException e) {
            LOG.writeLog("Error calling external API to find users: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 查询组信息
     * 
     * @param groupId 组ID
     * @return 组信息
     */
    public Map<String, Object> findGroup(String groupId) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(apiProperties.getBaseUrl())
                    .path(apiProperties.getGroupQueryPath())
                    .path("/" + groupId)
                    .toUriString();
            
            LOG.writeLog("Calling external API to find group: " + groupId);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                LOG.writeLog("External API returned non-success status:" + response.getStatusCode());
                return null;
            }
            
        } catch (RestClientException e) {
            LOG.writeLog("Error calling external API to find group: " + groupId + ", error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 查询组列表
     * 
     * @param userId 用户ID（可选）
     * @return 组列表
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> findGroups(String userId) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(apiProperties.getBaseUrl())
                    .path(apiProperties.getGroupQueryPath());
            
            if (userId != null && !userId.isEmpty()) {
                builder.queryParam("userId", userId);
            }
            
            String url = builder.toUriString();
            
            LOG.writeLog("Calling external API to find groups for user: " + userId);
            
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                LOG.writeLog("External API returned non-success status: " + response.getStatusCode());
                return null;
            }
            
        } catch (RestClientException e) {
            LOG.writeLog("Error calling external API to find groups for user: " + userId + "error:" + e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证用户认证
     * 
     * @param userId 用户ID
     * @param password 密码
     * @return 认证是否成功
     */
    public boolean authenticateUser(String userId, String password) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(apiProperties.getBaseUrl())
                    .path(apiProperties.getAuthPath())
                    .toUriString();
            
            // 构建认证请求体
            Map<String, String> authRequest = Map.of(
                    "userId", userId,
                    "password", password
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(authRequest, headers);
            
            LOG.writeLog("Calling external API to authenticate user:" + userId);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (RestClientException e) {
            LOG.writeLog("Error calling external API to authenticate user: " + userId + "error:" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 通用GET请求方法
     * 
     * @param path API路径
     * @param params 查询参数
     * @return 响应结果
     */
    public Object callGetApi(String path, Map<String, String> params) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(apiProperties.getBaseUrl())
                    .path(path);
            
            if (params != null) {
                params.forEach(builder::queryParam);
            }
            
            String url = builder.toUriString();
            
            LOG.writeLog("Calling external API GET: " + url);
            
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                LOG.writeLog("External API returned non-success status: " + response.getStatusCode());
                return null;
            }
            
        } catch (RestClientException e) {
            LOG.writeLog("Error calling external API GET: " + path + "error:" + e.getMessage());
            return null;
        }
    }
    
    /**
     * 通用POST请求方法
     * 
     * @param path API路径
     * @param requestBody 请求体
     * @return 响应结果
     */
    public Object callPostApi(String path, Object requestBody) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(apiProperties.getBaseUrl())
                    .path(path)
                    .toUriString();
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
            
            LOG.writeLog("Calling external API POST: " + url);
            
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.POST, entity, Object.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                LOG.writeLog("External API returned non-success status: " + response.getStatusCode());
                return null;
            }
            
        } catch (RestClientException e) {
            LOG.writeLog("Error calling external API POST: " + path + "error:" + e.getMessage());
            return null;
        }
    }
}