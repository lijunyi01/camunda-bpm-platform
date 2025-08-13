package org.camunda.bpm.identity.external;

import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.identity.external.apiVO.BpmnResponseVO;
import org.camunda.bpm.identity.external.apiVO.AllUsersQueryVO;
import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test for ExternalAccessIdentityProviderSession dependency injection
 */
public class IdentityProviderSessionTest {

    @Mock
    private ApiService mockApiService;

    private ExternalAccessSpringConfiguration.ExternalAccessSpringPlugin.ExternalAccessIdentityProviderSessionFactory factory;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Mock the ApiService response
        BpmnResponseVO<java.util.List<org.camunda.bpm.engine.identity.User>> mockResponse = new BpmnResponseVO<>();
        mockResponse.setCode(200);
        mockResponse.setResult(new ArrayList<>());
        
        when(mockApiService.listExternalUserIds(any(AllUsersQueryVO.class))).thenReturn(mockResponse);
        
        factory = new ExternalAccessSpringConfiguration.ExternalAccessSpringPlugin.ExternalAccessIdentityProviderSessionFactory(mockApiService);
    }

    @Test
    public void testSessionFactoryCreatesSessionWithInjectedApiService() {
        // When
        Session session = factory.openSession();
        
        // Then
        assertNotNull("Session should not be null", session);
        assertTrue("Session should be instance of ExternalAccessIdentityProviderSession", 
                   session instanceof ExternalAccessIdentityProviderSession);
        
        ExternalAccessIdentityProviderSession identitySession = (ExternalAccessIdentityProviderSession) session;
        
        // Verify that apiService is properly injected by checking it's not null
        // We can't directly access the private field, but we can test that methods don't throw NPE
        try {
            identitySession.findUserById("testUser");
            // If no NPE is thrown, the apiService was properly injected
        } catch (NullPointerException e) {
            fail("ApiService was not properly injected - NullPointerException thrown: " + e.getMessage());
        } catch (Exception e) {
            // Other exceptions are fine, we just want to avoid NPE from null apiService
        }
    }

    @Test
    public void testFactoryReturnsCorrectSessionType() {
        // When
        Class<?> sessionType = factory.getSessionType();
        
        // Then
        assertEquals("Session type should be ReadOnlyIdentityProvider", 
                     org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider.class, 
                     sessionType);
    }
}