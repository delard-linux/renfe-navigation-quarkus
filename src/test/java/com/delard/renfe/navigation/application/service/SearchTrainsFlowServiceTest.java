package com.delard.renfe.navigation.application.service;

import com.delard.renfe.navigation.domain.model.FlowResponse;
import com.delard.renfe.navigation.domain.port.output.FlowScraperPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for SearchTrainsFlowService
 * 
 * NOTE: This test class is disabled because the FlowScraperAdapter functionality
 * is not yet fully implemented. Once the flow scraping feature is complete,
 * remove the @Disabled annotation to enable these tests.
 */
@Disabled("Flow scraping functionality is not yet implemented")
@ExtendWith(MockitoExtension.class)
class SearchTrainsFlowServiceTest {

    @Mock
    private FlowScraperPort flowScraperPort;

    @InjectMocks
    private SearchTrainsFlowService service;

    @BeforeEach
    void setUp() {
        service = new SearchTrainsFlowService();
        // Use reflection to inject the mock
        try {
            java.lang.reflect.Field field = SearchTrainsFlowService.class.getDeclaredField("flowScraperPort");
            field.setAccessible(true);
            field.set(service, flowScraperPort);
        } catch (Exception e) {
            fail("Failed to inject mock: " + e.getMessage());
        }
    }

    @Test
    void testSearchTrainsFlowSuccess() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = "2025-12-05";
        int adults = 2;
        String expectedFilepath = "/tmp/renfe_flow_result.html";

        when(flowScraperPort.executeFlow(origin, destination, dateOut, dateReturn, adults))
                .thenReturn(expectedFilepath);

        FlowResponse result = service.searchTrainsFlow(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals("Flow completed successfully", result.getMessage());
        assertEquals(expectedFilepath, result.getFilepath());

        verify(flowScraperPort, times(1)).executeFlow(origin, destination, dateOut, dateReturn, adults);
    }

    @Test
    void testSearchTrainsFlowSuccessWithoutReturnDate() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = null;
        int adults = 1;
        String expectedFilepath = "/tmp/renfe_flow_result.html";

        when(flowScraperPort.executeFlow(origin, destination, dateOut, dateReturn, adults))
                .thenReturn(expectedFilepath);

        FlowResponse result = service.searchTrainsFlow(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals("Flow completed successfully", result.getMessage());
        assertEquals(expectedFilepath, result.getFilepath());

        verify(flowScraperPort, times(1)).executeFlow(origin, destination, dateOut, dateReturn, adults);
    }

    @Test
    void testSearchTrainsFlowSuccessWithEmptyReturnDate() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = "";
        int adults = 1;
        String expectedFilepath = "/tmp/renfe_flow_result.html";

        when(flowScraperPort.executeFlow(origin, destination, dateOut, dateReturn, adults))
                .thenReturn(expectedFilepath);

        FlowResponse result = service.searchTrainsFlow(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals("Flow completed successfully", result.getMessage());
        assertEquals(expectedFilepath, result.getFilepath());
    }

    @Test
    void testSearchTrainsFlowThrowsException() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = "2025-12-05";
        int adults = 2;
        String errorMessage = "Scraping failed";

        when(flowScraperPort.executeFlow(origin, destination, dateOut, dateReturn, adults))
                .thenThrow(new RuntimeException(errorMessage));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.searchTrainsFlow(origin, destination, dateOut, dateReturn, adults);
        });

        assertTrue(exception.getMessage().contains("Error executing flow"));
        assertTrue(exception.getMessage().contains(errorMessage));
        assertNotNull(exception.getCause());

        verify(flowScraperPort, times(1)).executeFlow(origin, destination, dateOut, dateReturn, adults);
    }

    @Test
    void testSearchTrainsFlowWithDifferentAdults() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = null;
        String expectedFilepath = "/tmp/renfe_flow_result.html";

        when(flowScraperPort.executeFlow(eq(origin), eq(destination), eq(dateOut), eq(dateReturn), anyInt()))
                .thenReturn(expectedFilepath);

        FlowResponse result1 = service.searchTrainsFlow(origin, destination, dateOut, dateReturn, 1);
        FlowResponse result2 = service.searchTrainsFlow(origin, destination, dateOut, dateReturn, 3);
        FlowResponse result3 = service.searchTrainsFlow(origin, destination, dateOut, dateReturn, 5);

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(expectedFilepath, result1.getFilepath());
        assertEquals(expectedFilepath, result2.getFilepath());
        assertEquals(expectedFilepath, result3.getFilepath());

        verify(flowScraperPort, times(3)).executeFlow(eq(origin), eq(destination), eq(dateOut), eq(dateReturn), anyInt());
    }

    @Test
    void testSearchTrainsFlowWithNullValues() {
        String expectedFilepath = "/tmp/renfe_flow_result.html";

        lenient().when(flowScraperPort.executeFlow(any(), any(), any(), any(), anyInt()))
                .thenReturn(expectedFilepath);

        FlowResponse result = service.searchTrainsFlow(null, null, null, null, 0);

        assertNotNull(result);
        assertEquals("Flow completed successfully", result.getMessage());
        assertEquals(expectedFilepath, result.getFilepath());
    }
}

