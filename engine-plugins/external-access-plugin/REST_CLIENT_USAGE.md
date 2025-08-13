# External Access Plugin - REST Client Usage Guide

## Overview

This plugin now includes a complete REST client framework for calling external systems. The framework provides:

- Configurable REST client with timeout and authentication support
- Service layer for common identity operations
- Spring Boot auto-configuration
- Comprehensive error handling and logging

## Quick Start

### 1. Add Dependencies

The following dependencies are already included in the plugin:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>3.4.4</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>6.2.4</version>
    <scope>provided</scope>
</dependency>
```

### 2. Configuration

Copy `application-example.yml` to your application's resources directory and configure:

```yaml
camunda:
  bpm:
    external-access:
      enabled: true
      api:
        base-url: "https://your-api.example.com"
        auth-token: "your-api-token"
        connect-timeout: 5000
        read-timeout: 10000
        endpoints:
          auth: "/auth/validate"
          user: "/users/{userId}"
          users: "/users/search"
          group: "/groups/{groupId}"
          groups: "/groups/search"
```

### 3. Using the REST Client

#### In Your Spring Components

```java
@Service
public class MyService {
    
    @Autowired
    private ExternalApiService externalApiService;
    
    public void someMethod() {
        // Find a user
        Map<String, Object> user = externalApiService.findUser("john.doe");
        
        // Authenticate user
        boolean isValid = externalApiService.authenticateUser("john.doe", "password");
        
        // Custom API call
        String response = externalApiService.callGetApi("/custom/endpoint");
    }
}
```

#### In Process Engine Expressions

The `ExternalApiService` is automatically registered as a bean and can be used in expressions:

```xml
<!-- In BPMN process definition -->
<bpmn:serviceTask id="validateUser" camunda:expression="${externalApiService.authenticateUser(userId, password)}" />
```

## Architecture

### Core Components

1. **ExternalApiProperties** - Configuration properties class
2. **ExternalApiClientConfig** - RestTemplate configuration with authentication
3. **ExternalApiService** - Main service for API interactions
4. **ExternalAccessSpringConfiguration** - Spring configuration

### Key Features

- **Automatic Authentication**: All requests include configured auth token
- **Timeout Configuration**: Configurable connection and read timeouts
- **Error Handling**: Comprehensive error logging and exception handling
- **Spring Integration**: Full Spring Boot auto-configuration support
- **Flexible Endpoints**: Configurable API endpoints for different operations

## API Methods

### ExternalApiService Methods

| Method | Description | Parameters | Returns |
|--------|-------------|------------|----------|
| `findUser(String userId)` | Find user by ID | userId | Map<String, Object> |
| `findUsers(String query)` | Search users | search query | List<Map<String, Object>> |
| `findGroup(String groupId)` | Find group by ID | groupId | Map<String, Object> |
| `findGroups(String query)` | Search groups | search query | List<Map<String, Object>> |
| `authenticateUser(String userId, String password)` | Authenticate user | userId, password | boolean |
| `callGetApi(String endpoint)` | Generic GET call | endpoint path | String |
| `callPostApi(String endpoint, Object body)` | Generic POST call | endpoint, request body | String |

## Error Handling

The framework includes comprehensive error handling:

- Connection timeouts
- HTTP error responses
- JSON parsing errors
- Authentication failures

All errors are logged using the Camunda logging framework.

## Customization

### Adding Custom Endpoints

1. Add endpoint configuration in `application.yml`:

```yaml
camunda:
  bpm:
    external-access:
      api:
        endpoints:
          custom-operation: "/api/custom/{id}"
```

2. Add method to `ExternalApiService`:

```java
public Map<String, Object> customOperation(String id) {
    String endpoint = properties.getEndpoints().get("custom-operation");
    String url = endpoint.replace("{id}", id);
    return callGetApi(url);
}
```

### Custom Authentication

Modify `AuthTokenInterceptor` in `ExternalApiClientConfig` to implement different authentication schemes.

## Troubleshooting

### Enable Debug Logging

```yaml
logging:
  level:
    org.camunda.bpm.identity.external: DEBUG
    org.springframework.web.client.RestTemplate: DEBUG
```

### Common Issues

1. **Connection Timeout**: Increase `connect-timeout` value
2. **Read Timeout**: Increase `read-timeout` value
3. **Authentication Errors**: Verify `auth-token` configuration
4. **Endpoint Not Found**: Check `base-url` and endpoint paths

## Examples

See the test directory for complete usage examples and integration tests.