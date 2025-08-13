/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.identity.external;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ApiService
 * Demonstrates how to test REST client functionality
 */
@RunWith(MockitoJUnitRunner.class)
public class ExternalApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApiProperties apiProperties;

    private ApiService apiService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        apiService = new ApiService();
        // Use reflection to set private fields
        try {
            java.lang.reflect.Field restTemplateField = ApiService.class.getDeclaredField("restTemplate");
            restTemplateField.setAccessible(true);
            restTemplateField.set(apiService, restTemplate);
            
            java.lang.reflect.Field propertiesField = ApiService.class.getDeclaredField("apiProperties");
            propertiesField.setAccessible(true);
            propertiesField.set(apiService, apiProperties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Setup mock properties
        when(apiProperties.getBaseUrl()).thenReturn("https://api.example.com");
        when(apiProperties.getUserQueryPath()).thenReturn("/api/users");
        when(apiProperties.getGroupQueryPath()).thenReturn("/api/groups");
        when(apiProperties.getAuthPath()).thenReturn("/api/auth");
        when(apiProperties.getAuthToken()).thenReturn("test-token");
        when(apiProperties.getConnectTimeout()).thenReturn(5000);
        when(apiProperties.getReadTimeout()).thenReturn(10000);
    }

    @Test
    public void testFindUser_Success() {
        // Given
        String userId = "john.doe";
        Map<String, Object> expectedUser = new HashMap<>();
        expectedUser.put("id", userId);
        expectedUser.put("name", "John Doe");
        expectedUser.put("email", "john.doe@example.com");

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenReturn(expectedUser);

        // When
        Map<String, Object> result = apiService.findUser(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.get("id"));
        assertEquals("John Doe", result.get("name"));
        verify(restTemplate).getForObject(
            "https://api.example.com/users/john.doe", Map.class);
    }

    @Test
    public void testFindUser_NotFound() {
        // Given
        String userId = "nonexistent";
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
            .thenThrow(new RestClientException("404 Not Found"));

        // When
        Map<String, Object> result = apiService.findUser(userId);

        // Then
        assertNull(result);
    }

    @Test
    public void testAuthenticateUser_Success() {
        // Given
        String userId = "john.doe";
        String password = "password123";
        Map<String, Object> authRequest = new HashMap<>();
        authRequest.put("userId", userId);
        authRequest.put("password", password);

        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("valid", true);

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(authResponse);

        // When
        boolean result = apiService.authenticateUser(userId, password);

        // Then
        assertTrue(result);
        verify(restTemplate).postForObject(
            eq("https://api.example.com/auth/validate"), 
            eq(authRequest), 
            eq(Map.class));
    }

    @Test
    public void testAuthenticateUser_InvalidCredentials() {
        // Given
        String userId = "john.doe";
        String password = "wrongpassword";
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put("valid", false);

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(authResponse);

        // When
        boolean result = apiService.authenticateUser(userId, password);

        // Then
        assertFalse(result);
    }

    @Test
    public void testCallGetApi_Success() {
        // Given
        String endpoint = "/custom/endpoint";
        String expectedResponse = "{\"status\": \"success\"}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenReturn(expectedResponse);

        // When
        Object result = apiService.callGetApi(endpoint, null);

        // Then
        assertEquals(expectedResponse, result);
        verify(restTemplate).getForObject(
            "https://api.example.com/custom/endpoint", String.class);
    }

    @Test
    public void testCallPostApi_Success() {
        // Given
        String endpoint = "/custom/endpoint";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "test");
        String expectedResponse = "{\"result\": \"ok\"}";

        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
            .thenReturn(expectedResponse);

        // When
        Object result = apiService.callPostApi(endpoint, requestBody);

        // Then
        assertEquals(expectedResponse, result);
        verify(restTemplate).postForObject(
            eq("https://api.example.com/custom/endpoint"), 
            eq(requestBody), 
            eq(String.class));
    }
}