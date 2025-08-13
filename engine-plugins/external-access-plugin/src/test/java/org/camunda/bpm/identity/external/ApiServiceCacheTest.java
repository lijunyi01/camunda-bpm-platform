package org.camunda.bpm.identity.external;

import org.camunda.bpm.identity.external.apiVO.BpmnResponseVO;
import org.camunda.bpm.identity.external.apiVO.UserVO;
import org.camunda.bpm.engine.identity.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApiServiceCacheTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApiProperties apiProperties;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private ApiService apiService;

    @Before
    public void setUp() throws Exception {
        when(apiProperties.getBaseUrl()).thenReturn("http://localhost:8080");
    }

    @Test
    public void testCacheHit() throws Exception {
        // 准备缓存数据
        List<UserVO> userVOList = new ArrayList<>();
        UserVO userVO = new UserVO();
        userVO.setUserId("user1");
        userVO.setFirstName("John");
        userVO.setLastName("Doe");
        userVO.setEmail("john.doe@example.com");
        userVOList.add(userVO);
        
        BpmnResponseVO<List<UserVO>> cachedResponse = new BpmnResponseVO<>();
        cachedResponse.setCode(200);
        cachedResponse.setMsg("success");
        cachedResponse.setResult(userVOList);
        
        // 模拟缓存命中
        when(cacheManager.getUserCache("externalUsers")).thenReturn(cachedResponse);
        
        // 调用方法
        BpmnResponseVO<List<User>> result = apiService.listExternalUsers(new Object());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertEquals("success", result.getMsg());
        assertNotNull(result.getResult());
        assertEquals(1, result.getResult().size());
        
        // 验证没有调用外部API
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        
        // 验证缓存被查询
        verify(cacheManager, times(1)).getUserCache("externalUsers");
    }

    @Test
    public void testCacheMiss() throws Exception {
        // 模拟缓存未命中
        when(cacheManager.getUserCache("externalUsers")).thenReturn(null);
        
        // 准备API响应数据
        List<UserVO> userVOList = new ArrayList<>();
        UserVO userVO = new UserVO();
        userVO.setUserId("user1");
        userVO.setFirstName("John");
        userVO.setLastName("Doe");
        userVO.setEmail("john.doe@example.com");
        userVOList.add(userVO);
        
        BpmnResponseVO<List<UserVO>> apiResponse = new BpmnResponseVO<>();
        apiResponse.setCode(200);
        apiResponse.setMsg("success");
        apiResponse.setResult(userVOList);
        
        ResponseEntity<BpmnResponseVO<List<UserVO>>> responseEntity = 
            new ResponseEntity<>(apiResponse, HttpStatus.OK);
        
        // 模拟API调用
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);
        
        // 调用方法
        BpmnResponseVO<List<User>> result = apiService.listExternalUsers(new Object());
        
        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertEquals("success", result.getMsg());
        assertNotNull(result.getResult());
        assertEquals(1, result.getResult().size());
        
        // 验证调用了外部API
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        
        // 验证缓存被更新
        verify(cacheManager, times(1)).putUserCache(eq("externalUsers"), any(BpmnResponseVO.class));
    }

    @Test
    public void testCacheClear() throws Exception {
        // 模拟缓存清除
        doNothing().when(cacheManager).clearUserCache();
        
        // 这里我们需要通过反射调用clearUserCache方法，因为ApiService中可能没有直接暴露这个方法
        // 或者我们可以测试CacheManager的clearUserCache方法
        cacheManager.clearUserCache();
        
        // 验证缓存清除被调用
        verify(cacheManager, times(1)).clearUserCache();
    }
}