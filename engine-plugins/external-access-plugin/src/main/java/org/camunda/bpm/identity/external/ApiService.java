package org.camunda.bpm.identity.external;

import org.camunda.bpm.identity.external.apiVO.BpmnResponseVO;
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
import org.camunda.bpm.identity.external.apiVO.UserVO;
import org.springframework.core.ParameterizedTypeReference;
import org.camunda.bpm.engine.identity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 外部API服务类
 * 提供调用外部REST接口的方法
 */
// @Service
public class ApiService {
    
    private static final ExternalAccessPluginLogger LOG = ExternalAccessPluginLogger.LOGGER;
    
    @Autowired
    @Qualifier("apiRestTemplate")
    private RestTemplate restTemplate;
    
    @Autowired
    private ApiProperties apiProperties;

    @Autowired
    private CacheManager cacheManager;
    
    /**
     * 查询全量用户id列表
     * @return 用户id列表
     */
    public BpmnResponseVO<List<User>> listExternalUsers(Object requestBody) {
        BpmnResponseVO<List<UserVO>> cachedResponse = cacheManager.getUserCache("externalUsers");
        if(cachedResponse != null) {
            LOG.writeLog("ExternalAccessIdentityProviderSession getAllUsers from cache");
            // 转换缓存的UserVO为User对象
            List<User> externalUsers = new ArrayList<>();
            for(UserVO userVO : cachedResponse.getResult()) {
                externalUsers.add(createUser(userVO.getUserId(), userVO.getFirstName(), userVO.getLastName(), userVO.getEmail()));
            }
            BpmnResponseVO<List<User>> response = new BpmnResponseVO<>();
            response.setCode(200);
            response.setMsg("success");
            response.setResult(externalUsers);
            return response;
        }
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(apiProperties.getBaseUrl())
                    .path("/person/idlist")
                    .toUriString();
            
            LOG.writeLog("Calling external API(idlist) to find users");
            
            ParameterizedTypeReference<BpmnResponseVO<List<UserVO>>> typeRef = new ParameterizedTypeReference<BpmnResponseVO<List<UserVO>>>() {};
            HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody);
            final ResponseEntity<BpmnResponseVO<List<UserVO>>> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, typeRef);
            // 以下无法正常为带范型的类反序列化
            // final ResponseEntity<BpmnResponseVO> response = restTemplate.postForEntity(url, requestBody, BpmnResponseVO.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getCode().equals(200)) {
                List<User> externalUsers = new ArrayList<>();
                for(UserVO userVO : response.getBody().getResult()) {
                    externalUsers.add(createUser(userVO.getUserId(), userVO.getFirstName(), userVO.getLastName(), userVO.getEmail()));
                }
                if(!externalUsers.isEmpty()) {
                    cacheManager.putUserCache("externalUsers", response.getBody());
                }
                BpmnResponseVO<List<User>> successResponse = new BpmnResponseVO<>();
                successResponse.setCode(200);
                successResponse.setMsg("success");
                successResponse.setResult(externalUsers);
                return successResponse;
            } else {
                LOG.writeLog("External API(idlist) returned non-success status: " + response.getStatusCode());
                BpmnResponseVO<List<User>> errorResponse = new BpmnResponseVO<>();
                errorResponse.setCode(500);
                errorResponse.setMsg("error");
                errorResponse.setResult(new ArrayList<>());
                return errorResponse;
            }
            
        } catch (RestClientException e) {
            LOG.writeLog("Error calling external API(idlist): , error: " + e.getMessage());
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
//    public boolean authenticateUser(String userId, String password) {
//        try {
//            String url = UriComponentsBuilder
//                    .fromHttpUrl(apiProperties.getBaseUrl())
//                    .path(apiProperties.getAuthPath())
//                    .toUriString();
//
//            // 构建认证请求体
//            Map<String, String> authRequest = Map.of(
//                    "userId", userId,
//                    "password", password
//            );
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Content-Type", "application/json");
//            HttpEntity<Map<String, String>> entity = new HttpEntity<>(authRequest, headers);
//
//            LOG.writeLog("Calling external API to authenticate user:" + userId);
//
//            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
//
//            return response.getStatusCode().is2xxSuccessful();
//
//        } catch (RestClientException e) {
//            LOG.writeLog("Error calling external API to authenticate user: " + userId + "error:" + e.getMessage());
//            return false;
//        }
//    }
    
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
        LOG.writeLog("post base url:" + apiProperties.getBaseUrl());
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


    /**
     * 从候选组获取对应的人员
     * @param requestBody 请求体
     * @return 响应结果
     */
    public BpmnResponseVO<List<String>> getUsersByGroupId(Object requestBody) {
        LOG.writeLog("post base url:" + apiProperties.getBaseUrl());
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(apiProperties.getBaseUrl())
                    .path("/person/getPersonByOrgId")
                    .toUriString();

            // HttpHeaders headers = new HttpHeaders();
            // headers.set("Content-Type", "application/json");
            // HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

            LOG.writeLog("Calling external API POST: " + url);

//            ResponseEntity<BpmnResponseVO<List<String>>> response = restTemplate.exchange(url, HttpMethod.POST, entity, BpmnResponseVO<List<String>>.class);
            final ResponseEntity<BpmnResponseVO> response = restTemplate.postForEntity(url, requestBody, BpmnResponseVO.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                LOG.writeLog("External API returned non-success status: " + response.getStatusCode());
                return null;
            }

        } catch (RestClientException e) {
            LOG.writeLog("Error calling external API POST: error:" + e.getMessage());
            return null;
        }
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
}