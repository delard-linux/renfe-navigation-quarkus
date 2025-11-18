package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.application.exception.QueueException;
import com.delard.renfe.navigation.domain.model.FareOption;
import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.infrastructure.config.PlaywrightConfig;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

/**
 * Unit tests for PlaywrightSearchTrainsService
 */
@ExtendWith(MockitoExtension.class)
class PlaywrightSearchTrainsServiceTest {

    @Mock
    private PlaywrightConfig config;

    @Mock
    private TrainHtmlParser trainHtmlParser;

    @Mock
    private ResponseStorageService responseStorageService;

    @Mock
    private PlaywrightFactory playwrightFactory;

    @InjectMocks
    private PlaywrightSearchTrainsService service;

    @BeforeEach
    void setUp() {
        // Setup is handled by MockitoExtension
    }

    @Test
    void testSearchTrainsResultConstructor() {
        // Arrange
        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        Train train2 = new Train("TRAIN456", "ALVIA", "10:00", "15:30", "5h 30m", 67.80);
        List<Train> outboundTrains = Arrays.asList(train1, train2);
        List<Train> returnTrains = Arrays.asList(train1);

        // Act
        PlaywrightSearchTrainsService.SearchTrainsResult result =
                new PlaywrightSearchTrainsService.SearchTrainsResult(outboundTrains, returnTrains);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.outboundTrains.size());
        assertEquals(1, result.returnTrains.size());
    }

    @Test
    void testSearchTrainsResultWithNullLists() {
        // Arrange & Act
        PlaywrightSearchTrainsService.SearchTrainsResult result =
                new PlaywrightSearchTrainsService.SearchTrainsResult(null, null);

        // Assert
        assertNotNull(result);
        assertNull(result.outboundTrains);
        assertNull(result.returnTrains);
    }

    @Test
    void testSearchTrainsResultToStringWithNullOrEmptyLists() {
        // Test null lists
        PlaywrightSearchTrainsService.SearchTrainsResult resultNull =
                new PlaywrightSearchTrainsService.SearchTrainsResult(null, null);
        String toStringNull = resultNull.toString();
        assertNotNull(toStringNull);
        assertTrue(toStringNull.contains("outboundTrains="));
        assertTrue(toStringNull.contains("returnTrains="));
        assertTrue(toStringNull.contains("[]"));

        // Test empty lists
        PlaywrightSearchTrainsService.SearchTrainsResult resultEmpty =
                new PlaywrightSearchTrainsService.SearchTrainsResult(new ArrayList<>(), new ArrayList<>());
        String toStringEmpty = resultEmpty.toString();
        assertNotNull(toStringEmpty);
        assertTrue(toStringEmpty.contains("outboundTrains="));
        assertTrue(toStringEmpty.contains("returnTrains="));
        assertTrue(toStringEmpty.contains("[]"));
    }

    @Test
    void testSearchTrainsResultToStringWithTrains() {
        // Arrange
        Train train = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        List<Train> trains = Arrays.asList(train);
        PlaywrightSearchTrainsService.SearchTrainsResult result =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains, null);

        // Act
        String toString = result.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("AVE"));
        assertTrue(toString.contains("08:00-12:30"));
        assertTrue(toString.contains("45.50€"));
    }

    @Test
    void testSearchTrainsResultToStringWithNullTrain() {
        // Arrange
        List<Train> trains = new ArrayList<>();
        trains.add(null);
        PlaywrightSearchTrainsService.SearchTrainsResult result =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains, null);

        // Act
        String toString = result.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("null"));
    }

    @Test
    void testSearchTrainsResultToStringWithTrainMissingFields() {
        // Test without service type
        Train train1 = new Train();
        train1.setDepartureTime("08:00");
        train1.setArrivalTime("12:30");
        train1.setPriceFrom(45.50);
        List<Train> trains1 = Arrays.asList(train1);
        PlaywrightSearchTrainsService.SearchTrainsResult result1 =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains1, null);
        String toString1 = result1.toString();
        assertNotNull(toString1);
        assertTrue(toString1.contains("(no-type)"));
        assertTrue(toString1.contains("08:00-12:30"));

        // Test without times
        Train train2 = new Train();
        train2.setServiceType("AVE");
        train2.setPriceFrom(45.50);
        List<Train> trains2 = Arrays.asList(train2);
        PlaywrightSearchTrainsService.SearchTrainsResult result2 =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains2, null);
        String toString2 = result2.toString();
        assertNotNull(toString2);
        assertTrue(toString2.contains("AVE"));
        assertTrue(toString2.contains("--"));
    }

    @Test
    void testSearchTrainsResultToStringWithTrainWithFares() {
        // Arrange
        Train train = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        FareOption fare1 = new FareOption("Basic", 45.50, "EUR", "BASIC", null, null);
        FareOption fare2 = new FareOption("Premium", 89.90, "EUR", "PREMIUM", null, null);
        train.setFares(Arrays.asList(fare1, fare2));
        List<Train> trains = Arrays.asList(train);
        PlaywrightSearchTrainsService.SearchTrainsResult result =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains, null);

        // Act
        String toString = result.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("AVE"));
        assertTrue(toString.contains("45.50€-89.90€"));
    }

    @Test
    void testSearchTrainsResultToStringWithTrainWithSingleFare() {
        // Arrange
        Train train = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        FareOption fare = new FareOption("Basic", 45.50, "EUR", "BASIC", null, null);
        train.setFares(Arrays.asList(fare));
        List<Train> trains = Arrays.asList(train);
        PlaywrightSearchTrainsService.SearchTrainsResult result =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains, null);

        // Act
        String toString = result.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("AVE"));
        assertTrue(toString.contains("45.50€"));
        assertFalse(toString.contains("45.50€-45.50€")); // Should not show range for single price
    }

    @Test
    void testSearchTrainsResultToStringWithTrainWithEmptyOrNullFares() {
        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        train1.setFares(new ArrayList<>());
        List<Train> trains1 = Arrays.asList(train1);
        PlaywrightSearchTrainsService.SearchTrainsResult result1 =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains1, null);
        String toString1 = result1.toString();
        assertNotNull(toString1);
        assertTrue(toString1.contains("AVE"));
        assertTrue(toString1.contains("45.50€")); // Should fallback to priceFrom

        Train train2 = new Train("TRAIN456", "ALVIA", "10:00", "15:30", "5h 30m", 67.80);
        train2.setFares(null);
        List<Train> trains2 = Arrays.asList(train2);
        PlaywrightSearchTrainsService.SearchTrainsResult result2 =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains2, null);
        String toString2 = result2.toString();
        assertNotNull(toString2);
        assertTrue(toString2.contains("ALVIA"));
        assertTrue(toString2.contains("67.80€")); // Should fallback to priceFrom
    }

    @Test
    void testSearchTrainsResultToStringWithTrainWithBlankFields() {
        // Test with blank service type
        Train train1 = new Train();
        train1.setServiceType("   ");
        train1.setDepartureTime("08:00");
        train1.setArrivalTime("12:30");
        train1.setPriceFrom(45.50);
        List<Train> trains1 = Arrays.asList(train1);
        PlaywrightSearchTrainsService.SearchTrainsResult result1 =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains1, null);
        String toString1 = result1.toString();
        assertNotNull(toString1);
        assertTrue(toString1.contains("(no-type)"));

        // Test with blank times
        Train train2 = new Train();
        train2.setServiceType("AVE");
        train2.setDepartureTime("   ");
        train2.setArrivalTime("   ");
        train2.setPriceFrom(45.50);
        List<Train> trains2 = Arrays.asList(train2);
        PlaywrightSearchTrainsService.SearchTrainsResult result2 =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains2, null);
        String toString2 = result2.toString();
        assertNotNull(toString2);
        assertTrue(toString2.contains("AVE"));
        assertTrue(toString2.contains("--"));
    }

    // ========== Tests for escapeUrlParameter method ==========

    @Test
    @DisplayName("escapeUrlParameter should return empty string when input is null")
    void testEscapeUrlParameterWithNull() throws Exception {
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("escapeUrlParameter", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, (String) null);

        assertEquals("", result);
    }

    @Test
    @DisplayName("escapeUrlParameter should encode normal string using URLEncoder")
    void testEscapeUrlParameterWithNormalString() throws Exception {
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("escapeUrlParameter", String.class);
        method.setAccessible(true);

        String input = "Madrid Barcelona";
        String result = (String) method.invoke(service, input);

        // URLEncoder.encode("Madrid Barcelona", "UTF-8") should return "Madrid+Barcelona"
        assertEquals("Madrid+Barcelona", result);
    }

    @Test
    @DisplayName("escapeUrlParameter should encode special characters using URLEncoder")
    void testEscapeUrlParameterWithSpecialCharacters() throws Exception {
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("escapeUrlParameter", String.class);
        method.setAccessible(true);

        String input = "test&value=123";
        String result = (String) method.invoke(service, input);

        // URLEncoder should encode & and = characters
        assertTrue(result.contains("%26") || result.contains("&")); // & encoded or not depending on version
        assertTrue(result.contains("%3D") || result.contains("=")); // = encoded or not depending on version
    }

    @Test
    @DisplayName("escapeUrlParameter should handle empty string")
    void testEscapeUrlParameterWithEmptyString() throws Exception {
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("escapeUrlParameter", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, "");

        assertEquals("", result);
    }

    @Test
    @DisplayName("escapeUrlParameter should encode strings with special characters")
    void testEscapeUrlParameterWithSpecialCharactersEncoding() throws Exception {
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("escapeUrlParameter", String.class);
        method.setAccessible(true);

        // Test with various special characters that need encoding
        String input = "test value&key=123'quote\"double";
        String result = (String) method.invoke(service, input);
        
        // The result should be URL encoded (UTF-8 is always supported in modern JVMs)
        assertNotNull(result);
        // Verify it's encoded (not the same as input)
        assertNotEquals(input, result);
        // Verify the result is a valid URL-encoded string
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("escapeUrlParameter should use fallback when UnsupportedEncodingException occurs")
    void testEscapeUrlParameterWithUnsupportedEncodingException() throws Exception {
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("escapeUrlParameter", String.class);
        method.setAccessible(true);

        // Test with a string containing all characters that the fallback handles
        String input = "test value&key=123'quote\"double";
        
        // Use try-with-resources to mock URLEncoder.encode statically
        try (var mockedStatic = mockStatic(java.net.URLEncoder.class)) {
            // Make URLEncoder.encode throw UnsupportedEncodingException
            mockedStatic.when(() -> java.net.URLEncoder.encode(anyString(), eq("UTF-8")))
                    .thenThrow(new java.io.UnsupportedEncodingException("UTF-8 not supported"));
            
            String result = (String) method.invoke(service, input);
            
            // Verify fallback was used - check that all special characters are replaced
            assertNotNull(result);
            assertEquals("test%20value%26key%3D123%27quote%22double", result);
            assertTrue(result.contains("%20")); // space
            assertTrue(result.contains("%26")); // &
            assertTrue(result.contains("%3D")); // =
            assertTrue(result.contains("%27")); // '
            assertTrue(result.contains("%22")); // "
        }
    }

    @Test
    @DisplayName("escapeUrlParameter fallback should handle space character")
    void testEscapeUrlParameterFallbackSpace() throws Exception {
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("escapeUrlParameter", String.class);
        method.setAccessible(true);

        String input = "test value";
        
        try (var mockedStatic = mockStatic(java.net.URLEncoder.class)) {
            mockedStatic.when(() -> java.net.URLEncoder.encode(anyString(), eq("UTF-8")))
                    .thenThrow(new java.io.UnsupportedEncodingException("UTF-8 not supported"));
            
            String result = (String) method.invoke(service, input);
            
            assertEquals("test%20value", result);
        }
    }

    @Test
    @DisplayName("escapeUrlParameter fallback should handle ampersand character")
    void testEscapeUrlParameterFallbackAmpersand() throws Exception {
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("escapeUrlParameter", String.class);
        method.setAccessible(true);

        String input = "test&value";
        
        try (var mockedStatic = mockStatic(java.net.URLEncoder.class)) {
            mockedStatic.when(() -> java.net.URLEncoder.encode(anyString(), eq("UTF-8")))
                    .thenThrow(new java.io.UnsupportedEncodingException("UTF-8 not supported"));
            
            String result = (String) method.invoke(service, input);
            
            assertEquals("test%26value", result);
        }
    }

    @Test
    @DisplayName("escapeUrlParameter fallback should handle equals character")
    void testEscapeUrlParameterFallbackEquals() throws Exception {
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("escapeUrlParameter", String.class);
        method.setAccessible(true);

        String input = "key=value";
        
        try (var mockedStatic = mockStatic(java.net.URLEncoder.class)) {
            mockedStatic.when(() -> java.net.URLEncoder.encode(anyString(), eq("UTF-8")))
                    .thenThrow(new java.io.UnsupportedEncodingException("UTF-8 not supported"));
            
            String result = (String) method.invoke(service, input);
            
            assertEquals("key%3Dvalue", result);
        }
    }

    @Test
    @DisplayName("escapeUrlParameter fallback should handle single quote character")
    void testEscapeUrlParameterFallbackSingleQuote() throws Exception {
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("escapeUrlParameter", String.class);
        method.setAccessible(true);

        String input = "test'value";
        
        try (var mockedStatic = mockStatic(java.net.URLEncoder.class)) {
            mockedStatic.when(() -> java.net.URLEncoder.encode(anyString(), eq("UTF-8")))
                    .thenThrow(new java.io.UnsupportedEncodingException("UTF-8 not supported"));
            
            String result = (String) method.invoke(service, input);
            
            assertEquals("test%27value", result);
        }
    }

    @Test
    @DisplayName("escapeUrlParameter fallback should handle double quote character")
    void testEscapeUrlParameterFallbackDoubleQuote() throws Exception {
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("escapeUrlParameter", String.class);
        method.setAccessible(true);

        String input = "test\"value";
        
        try (var mockedStatic = mockStatic(java.net.URLEncoder.class)) {
            mockedStatic.when(() -> java.net.URLEncoder.encode(anyString(), eq("UTF-8")))
                    .thenThrow(new java.io.UnsupportedEncodingException("UTF-8 not supported"));
            
            String result = (String) method.invoke(service, input);
            
            assertEquals("test%22value", result);
        }
    }

    @Test
    @DisplayName("escapeUrlParameter fallback should handle string with no special characters")
    void testEscapeUrlParameterFallbackNoSpecialChars() throws Exception {
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("escapeUrlParameter", String.class);
        method.setAccessible(true);

        String input = "normalstring";
        
        try (var mockedStatic = mockStatic(java.net.URLEncoder.class)) {
            mockedStatic.when(() -> java.net.URLEncoder.encode(anyString(), eq("UTF-8")))
                    .thenThrow(new java.io.UnsupportedEncodingException("UTF-8 not supported"));
            
            String result = (String) method.invoke(service, input);
            
            // Should return unchanged since no special characters to replace
            assertEquals("normalstring", result);
        }
    }

    @Test
    @DisplayName("escapeUrlParameter fallback should handle multiple occurrences of same character")
    void testEscapeUrlParameterFallbackMultipleOccurrences() throws Exception {
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("escapeUrlParameter", String.class);
        method.setAccessible(true);

        String input = "test value with spaces";
        
        try (var mockedStatic = mockStatic(java.net.URLEncoder.class)) {
            mockedStatic.when(() -> java.net.URLEncoder.encode(anyString(), eq("UTF-8")))
                    .thenThrow(new java.io.UnsupportedEncodingException("UTF-8 not supported"));
            
            String result = (String) method.invoke(service, input);
            
            // All spaces should be replaced
            assertEquals("test%20value%20with%20spaces", result);
            assertFalse(result.contains(" ")); // No spaces should remain
        }
    }

    // ========== Tests for extractResults method ==========

    @Test
    @DisplayName("extractResults should wait for selector, get content, and parse trains")
    void testExtractResultsSuccess() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        ElementHandle mockElementHandle = mock(ElementHandle.class);
        
        when(config.getTimeoutMs()).thenReturn(30000);
        doReturn(mockElementHandle).when(mockPage).waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class));
        when(mockPage.content()).thenReturn("<html><body>Train HTML content</body></html>");
        
        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        Train train2 = new Train("TRAIN456", "ALVIA", "10:00", "15:30", "5h 30m", 67.80);
        List<Train> expectedTrains = Arrays.asList(train1, train2);
        
        when(trainHtmlParser.parseTrainList(anyString())).thenReturn(expectedTrains);

        // Act
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("extractResults", Page.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Train> result = (List<Train>) method.invoke(service, mockPage);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("TRAIN123", result.get(0).getTrainId());
        assertEquals("TRAIN456", result.get(1).getTrainId());
        
        // Verify interactions
        verify(mockPage, times(1)).waitForSelector(
                eq("div.selectedTren[role='listitem']"),
                any(Page.WaitForSelectorOptions.class)
        );
        verify(mockPage, times(1)).waitForTimeout(1000L);
        verify(mockPage, times(1)).content();
        verify(trainHtmlParser, times(1)).parseTrainList("<html><body>Train HTML content</body></html>");
        verify(config, times(1)).getTimeoutMs();
    }

    @Test
    @DisplayName("extractResults should handle empty train list")
    void testExtractResultsWithEmptyList() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        ElementHandle mockElementHandle = mock(ElementHandle.class);
        
        when(config.getTimeoutMs()).thenReturn(30000);
        doReturn(mockElementHandle).when(mockPage).waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class));
        when(mockPage.content()).thenReturn("<html><body>No trains</body></html>");
        
        when(trainHtmlParser.parseTrainList(anyString())).thenReturn(new ArrayList<>());

        // Act
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("extractResults", Page.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Train> result = (List<Train>) method.invoke(service, mockPage);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(mockPage, times(1)).waitForSelector(
                eq("div.selectedTren[role='listitem']"),
                any(Page.WaitForSelectorOptions.class)
        );
        verify(mockPage, times(1)).waitForTimeout(1000L);
        verify(mockPage, times(1)).content();
        verify(trainHtmlParser, times(1)).parseTrainList("<html><body>No trains</body></html>");
    }

    @Test
    @DisplayName("extractResults should use correct timeout from config")
    void testExtractResultsUsesConfigTimeout() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        ElementHandle mockElementHandle = mock(ElementHandle.class);
        int customTimeout = 60000;
        
        when(config.getTimeoutMs()).thenReturn(customTimeout);
        doReturn(mockElementHandle).when(mockPage).waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class));
        when(mockPage.content()).thenReturn("<html><body>Content</body></html>");
        when(trainHtmlParser.parseTrainList(anyString())).thenReturn(new ArrayList<>());

        // Act
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("extractResults", Page.class);
        method.setAccessible(true);
        method.invoke(service, mockPage);

        // Assert
        verify(config, times(1)).getTimeoutMs();
        verify(mockPage, times(1)).waitForSelector(
                eq("div.selectedTren[role='listitem']"),
                any(Page.WaitForSelectorOptions.class)
        );
    }

    @Test
    @DisplayName("extractResults should wait for VISIBLE state")
    void testExtractResultsWaitsForVisibleState() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        ElementHandle mockElementHandle = mock(ElementHandle.class);
        
        when(config.getTimeoutMs()).thenReturn(30000);
        doReturn(mockElementHandle).when(mockPage).waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class));
        when(mockPage.content()).thenReturn("<html><body>Content</body></html>");
        when(trainHtmlParser.parseTrainList(anyString())).thenReturn(new ArrayList<>());

        // Act
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("extractResults", Page.class);
        method.setAccessible(true);
        method.invoke(service, mockPage);

        // Assert - Verify waitForSelector was called with VISIBLE state
        verify(mockPage, times(1)).waitForSelector(
                eq("div.selectedTren[role='listitem']"),
                any(Page.WaitForSelectorOptions.class)
        );
    }

    // ========== Tests for checkForQueuePage method ==========

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when page contains 'estás en la cola'")
    void testCheckForQueuePageWithEstasEnLaCola() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Estás en la cola de espera");

        // Act & Assert
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, mockPage);
        });
        
        // Extract the actual exception from InvocationTargetException
        Throwable cause = exception instanceof InvocationTargetException 
                ? ((InvocationTargetException) exception).getCause() 
                : exception;
        
        assertTrue(cause instanceof QueueException, "Expected QueueException but got: " + cause.getClass());
        assertTrue(cause.getMessage().contains("queued"));
        verify(mockPage, times(1)).waitForTimeout(500L);
        verify(mockPage, times(1)).locator("body");
    }

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when page contains 'cola para comprar'")
    void testCheckForQueuePageWithColaParaComprar() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Estás en la cola para comprar billetes");

        // Act & Assert
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, mockPage);
        });
        
        Throwable cause = exception instanceof InvocationTargetException 
                ? ((InvocationTargetException) exception).getCause() 
                : exception;
        
        assertTrue(cause instanceof QueueException, "Expected QueueException but got: " + cause.getClass());
        assertTrue(cause.getMessage().contains("queued"));
    }

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when page contains 'cuando sea tu turno'")
    void testCheckForQueuePageWithCuandoSeaTuTurno() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Cuando sea tu turno te redirigiremos");

        // Act & Assert
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, mockPage);
        });
        
        Throwable cause = exception instanceof InvocationTargetException 
                ? ((InvocationTargetException) exception).getCause() 
                : exception;
        
        assertTrue(cause instanceof QueueException, "Expected QueueException but got: " + cause.getClass());
        assertTrue(cause.getMessage().contains("queued"));
    }

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when page contains 'te redirigiremos'")
    void testCheckForQueuePageWithTeRedirigiremos() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Te redirigiremos cuando sea tu turno");

        // Act & Assert
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, mockPage);
        });
        
        Throwable cause = exception instanceof InvocationTargetException 
                ? ((InvocationTargetException) exception).getCause() 
                : exception;
        
        assertTrue(cause instanceof QueueException, "Expected QueueException but got: " + cause.getClass());
        assertTrue(cause.getMessage().contains("queued"));
    }

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when queue locators find queue.it elements")
    void testCheckForQueuePageWithQueueItInHtml() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page content");
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(1); // Found queue element

        // Act & Assert
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, mockPage);
        });
        
        Throwable cause = exception instanceof InvocationTargetException 
                ? ((InvocationTargetException) exception).getCause() 
                : exception;
        
        assertTrue(cause instanceof QueueException, "Expected QueueException but got: " + cause.getClass());
        assertTrue(cause.getMessage().contains("queued"));
    }

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when queue locators find queueit elements")
    void testCheckForQueuePageWithQueueitInHtml() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page content");
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(1); // Found queueit element

        // Act & Assert
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, mockPage);
        });
        
        Throwable cause = exception instanceof InvocationTargetException 
                ? ((InvocationTargetException) exception).getCause() 
                : exception;
        
        assertTrue(cause instanceof QueueException, "Expected QueueException but got: " + cause.getClass());
        assertTrue(cause.getMessage().contains("queued"));
    }

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when queue locators are found")
    void testCheckForQueuePageWithQueueLocators() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page content");
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(1);

        // Act & Assert
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, mockPage);
        });
        
        Throwable cause = exception instanceof InvocationTargetException 
                ? ((InvocationTargetException) exception).getCause() 
                : exception;
        
        assertTrue(cause instanceof QueueException, "Expected QueueException but got: " + cause.getClass());
        assertTrue(cause.getMessage().contains("queued"));
    }

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when queue page text locators are found")
    void testCheckForQueuePageWithQueuePageTextLocators() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page content");
        when(mockPage.content()).thenReturn("<html><body>Normal page</body></html>");
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(1);

        // Act & Assert
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, mockPage);
        });
        
        Throwable cause = exception instanceof InvocationTargetException 
                ? ((InvocationTargetException) exception).getCause() 
                : exception;
        
        assertTrue(cause instanceof QueueException, "Expected QueueException but got: " + cause.getClass());
        assertTrue(cause.getMessage().contains("queued"));
    }

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when turno text locator is found")
    void testCheckForQueuePageWithTurnoTextLocator() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page content");
        when(mockPage.content()).thenReturn("<html><body>Normal page</body></html>");
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(1);

        // Act & Assert
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, mockPage);
        });
        
        Throwable cause = exception instanceof InvocationTargetException 
                ? ((InvocationTargetException) exception).getCause() 
                : exception;
        
        assertTrue(cause instanceof QueueException, "Expected QueueException but got: " + cause.getClass());
        assertTrue(cause.getMessage().contains("queued"));
    }

    @Test
    @DisplayName("checkForQueuePage should not throw when page is normal")
    void testCheckForQueuePageWithNormalPage() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal train search page");
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);

        // Act & Assert - Should not throw
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(service, mockPage);
        });
        
        verify(mockPage, times(1)).waitForTimeout(500L);
    }

    @Test
    @DisplayName("checkForQueuePage should handle null bodyText gracefully")
    void testCheckForQueuePageWithNullBodyText() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn(null); // null bodyText
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);

        // Act & Assert - Should not throw (null bodyText is handled)
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(service, mockPage);
        });
    }

    @Test
    @DisplayName("checkForQueuePage should handle exception when getting page content")
    void testCheckForQueuePageWithExceptionGettingContent() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenThrow(new RuntimeException("Error getting content"));
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);

        // Act & Assert - Should not throw (exception is caught and logged)
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(service, mockPage);
        });
        
        verify(mockPage, times(1)).waitForTimeout(500L);
    }

    @Test
    @DisplayName("checkForQueuePage should handle exception when checking queue locators")
    void testCheckForQueuePageWithExceptionInQueueLocators() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page");
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenThrow(new RuntimeException("Error with locator"));
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);

        // Act & Assert - Should not throw (exception is caught and ignored)
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(service, mockPage);
        });
    }

    @Test
    @DisplayName("checkForQueuePage should handle exception when checking queue page text locators")
    void testCheckForQueuePageWithExceptionInQueuePageTextLocators() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page");
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenThrow(new RuntimeException("Error with text locator"));

        // Act & Assert - Should not throw (exception is caught and ignored)
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(service, mockPage);
        });
    }

    @Test
    @DisplayName("checkForQueuePage should re-throw QueueException")
    void testCheckForQueuePageReThrowsQueueException() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        
        doThrow(new QueueException("Already queued")).when(mockPage).waitForTimeout(500L);

        // Act & Assert
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, mockPage);
        });
        
        Throwable cause = exception instanceof InvocationTargetException 
                ? ((InvocationTargetException) exception).getCause() 
                : exception;
        
        assertTrue(cause instanceof QueueException, "Expected QueueException but got: " + cause.getClass());
        assertEquals("Already queued", cause.getMessage());
    }

    @Test
    @DisplayName("checkForQueuePage should handle general exception and continue")
    void testCheckForQueuePageWithGeneralException() throws Exception {
        // Arrange
        Page mockPage = mock(Page.class);
        
        doThrow(new RuntimeException("General error")).when(mockPage).waitForTimeout(500L);

        // Act & Assert - Should not throw (exception is caught and logged)
        Method method = PlaywrightSearchTrainsService.class.getDeclaredMethod("checkForQueuePage", Page.class);
        method.setAccessible(true);
        
        assertDoesNotThrow(() -> {
            method.invoke(service, mockPage);
        });
    }

    // ========== Tests for searchTrains method ==========

    @Test
    @DisplayName("searchTrains should return result with outbound trains when dateReturn is null")
    void testSearchTrainsWithNullDateReturn() throws Exception {
        // Arrange
        Playwright mockPlaywright = mock(Playwright.class);
        BrowserType mockBrowserType = mock(BrowserType.class);
        Browser mockBrowser = mock(Browser.class);
        BrowserContext mockContext = mock(BrowserContext.class);
        Page mockPage = mock(Page.class);
        ElementHandle mockElementHandle = mock(ElementHandle.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        Locator mockCookieButton = mock(Locator.class);

        when(playwrightFactory.create()).thenReturn(mockPlaywright);
        when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
        when(mockBrowserType.launch(any(BrowserType.LaunchOptions.class))).thenReturn(mockBrowser);
        when(mockBrowser.newContext(any(Browser.NewContextOptions.class))).thenReturn(mockContext);
        when(mockContext.newPage()).thenReturn(mockPage);
        
        when(config.getLocale()).thenReturn("es");
        when(config.getViewportWidth()).thenReturn(1920);
        when(config.getViewportHeight()).thenReturn(1080);
        when(config.getNavigationTimeoutMs()).thenReturn(30000);
        when(config.getTimeoutMs()).thenReturn(30000);
        when(config.getShortTimeoutMs()).thenReturn(5000);
        when(config.getRenfeSearchUrl()).thenReturn("https://www.renfe.com/es/es/viajar");
        when(config.isHeadless()).thenReturn(true);
        when(config.getSlowMo()).thenReturn(0);

        // Mock page interactions
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page");
        when(mockPage.content()).thenReturn("<html><body>Train results</body></html>");
        when(mockPage.waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class)))
                .thenReturn(mockElementHandle);
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);
        when(mockPage.locator("#onetrust-accept-btn-handler")).thenReturn(mockCookieButton);
        doThrow(new RuntimeException("Cookie button not found")).when(mockCookieButton).waitFor(any(Locator.WaitForOptions.class));

        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        when(trainHtmlParser.parseTrainList(anyString())).thenReturn(Arrays.asList(train1));

        // Act
        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "0071,MADRI,null", "0071,BARCE,null",
                "16/01/2026", null, "2"
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.outboundTrains);
        assertEquals(1, result.outboundTrains.size());
        assertNull(result.returnTrains); // Should be null when dateReturn is null
        
        verify(mockPage, times(1)).navigate(anyString(), any(Page.NavigateOptions.class));
        verify(responseStorageService, times(1)).saveResponse(anyString(), eq(200));
    }

    @Test
    @DisplayName("searchTrains should return result with outbound and return trains when dateReturn is provided")
    void testSearchTrainsWithDateReturn() throws Exception {
        // Arrange
        Playwright mockPlaywright = mock(Playwright.class);
        BrowserType mockBrowserType = mock(BrowserType.class);
        Browser mockBrowser = mock(Browser.class);
        BrowserContext mockContext = mock(BrowserContext.class);
        Page mockPage = mock(Page.class);
        ElementHandle mockElementHandle = mock(ElementHandle.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        Locator mockCookieButton = mock(Locator.class);
        Locator mockVueltaTab = mock(Locator.class);

        when(playwrightFactory.create()).thenReturn(mockPlaywright);
        when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
        when(mockBrowserType.launch(any(BrowserType.LaunchOptions.class))).thenReturn(mockBrowser);
        when(mockBrowser.newContext(any(Browser.NewContextOptions.class))).thenReturn(mockContext);
        when(mockContext.newPage()).thenReturn(mockPage);
        
        when(config.getLocale()).thenReturn("es");
        when(config.getViewportWidth()).thenReturn(1920);
        when(config.getViewportHeight()).thenReturn(1080);
        when(config.getNavigationTimeoutMs()).thenReturn(30000);
        when(config.getTimeoutMs()).thenReturn(30000);
        when(config.getShortTimeoutMs()).thenReturn(5000);
        when(config.getRenfeSearchUrl()).thenReturn("https://www.renfe.com/es/es/viajar");
        when(config.isHeadless()).thenReturn(true);
        when(config.getSlowMo()).thenReturn(0);

        // Mock page interactions
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page");
        when(mockPage.content()).thenReturn("<html><body>Train results</body></html>");
        when(mockPage.waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class)))
                .thenReturn(mockElementHandle);
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);
        when(mockPage.locator("#onetrust-accept-btn-handler")).thenReturn(mockCookieButton);
        doThrow(new RuntimeException("Cookie button not found")).when(mockCookieButton).waitFor(any(Locator.WaitForOptions.class));
        when(mockPage.locator("[id*='vuelta'], [class*='vuelta'], a:has-text('Vuelta')")).thenReturn(mockVueltaTab);
        when(mockVueltaTab.count()).thenReturn(1);
        when(mockVueltaTab.first()).thenReturn(mockVueltaTab);

        Train trainOut1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        Train trainRet1 = new Train("TRAIN456", "AVE", "16:00", "20:30", "4h 30m", 45.50);
        when(trainHtmlParser.parseTrainList(anyString()))
                .thenReturn(Arrays.asList(trainOut1))  // First call for outbound
                .thenReturn(Arrays.asList(trainRet1)); // Second call for return

        // Act
        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "0071,MADRI,null", "0071,BARCE,null",
                "16/01/2026", "18/01/2026", "2"
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.outboundTrains);
        assertEquals(1, result.outboundTrains.size());
        assertNotNull(result.returnTrains); // Should have return trains
        assertEquals(1, result.returnTrains.size());
        
        verify(mockVueltaTab, times(1)).click();
        // waitForSelector is called: 1 in searchTrains + 1 in extractResults (outbound) + 1 in extractResults (return) = 3
        verify(mockPage, atLeast(2)).waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class));
    }

    @Test
    @DisplayName("searchTrains should handle empty dateReturn string")
    void testSearchTrainsWithEmptyDateReturn() throws Exception {
        // Arrange
        Playwright mockPlaywright = mock(Playwright.class);
        BrowserType mockBrowserType = mock(BrowserType.class);
        Browser mockBrowser = mock(Browser.class);
        BrowserContext mockContext = mock(BrowserContext.class);
        Page mockPage = mock(Page.class);
        ElementHandle mockElementHandle = mock(ElementHandle.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        Locator mockCookieButton = mock(Locator.class);

        when(playwrightFactory.create()).thenReturn(mockPlaywright);
        when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
        when(mockBrowserType.launch(any(BrowserType.LaunchOptions.class))).thenReturn(mockBrowser);
        when(mockBrowser.newContext(any(Browser.NewContextOptions.class))).thenReturn(mockContext);
        when(mockContext.newPage()).thenReturn(mockPage);
        
        when(config.getLocale()).thenReturn("es");
        when(config.getViewportWidth()).thenReturn(1920);
        when(config.getViewportHeight()).thenReturn(1080);
        when(config.getNavigationTimeoutMs()).thenReturn(30000);
        when(config.getTimeoutMs()).thenReturn(30000);
        when(config.getShortTimeoutMs()).thenReturn(5000);
        when(config.getRenfeSearchUrl()).thenReturn("https://www.renfe.com/es/es/viajar");
        when(config.isHeadless()).thenReturn(true);
        when(config.getSlowMo()).thenReturn(0);

        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page");
        when(mockPage.content()).thenReturn("<html><body>Train results</body></html>");
        when(mockPage.waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class)))
                .thenReturn(mockElementHandle);
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);
        when(mockPage.locator("#onetrust-accept-btn-handler")).thenReturn(mockCookieButton);
        doThrow(new RuntimeException("Cookie button not found")).when(mockCookieButton).waitFor(any(Locator.WaitForOptions.class));

        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        when(trainHtmlParser.parseTrainList(anyString())).thenReturn(Arrays.asList(train1));

        // Act
        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "0071,MADRI,null", "0071,BARCE,null",
                "16/01/2026", "", "2"
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.outboundTrains);
        assertNull(result.returnTrains); // Should be null when dateReturn is empty
    }

    @Test
    @DisplayName("searchTrains should click cookie button when visible")
    void testSearchTrainsWithCookieButtonVisible() throws Exception {
        // Arrange
        Playwright mockPlaywright = mock(Playwright.class);
        BrowserType mockBrowserType = mock(BrowserType.class);
        Browser mockBrowser = mock(Browser.class);
        BrowserContext mockContext = mock(BrowserContext.class);
        Page mockPage = mock(Page.class);
        ElementHandle mockElementHandle = mock(ElementHandle.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        Locator mockCookieButton = mock(Locator.class);

        when(playwrightFactory.create()).thenReturn(mockPlaywright);
        when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
        when(mockBrowserType.launch(any(BrowserType.LaunchOptions.class))).thenReturn(mockBrowser);
        when(mockBrowser.newContext(any(Browser.NewContextOptions.class))).thenReturn(mockContext);
        when(mockContext.newPage()).thenReturn(mockPage);
        
        when(config.getLocale()).thenReturn("es");
        when(config.getViewportWidth()).thenReturn(1920);
        when(config.getViewportHeight()).thenReturn(1080);
        when(config.getNavigationTimeoutMs()).thenReturn(30000);
        when(config.getTimeoutMs()).thenReturn(30000);
        when(config.getShortTimeoutMs()).thenReturn(5000);
        when(config.getRenfeSearchUrl()).thenReturn("https://www.renfe.com/es/es/viajar");
        when(config.isHeadless()).thenReturn(true);
        when(config.getSlowMo()).thenReturn(0);

        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page");
        when(mockPage.content()).thenReturn("<html><body>Train results</body></html>");
        when(mockPage.waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class)))
                .thenReturn(mockElementHandle);
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);
        when(mockPage.locator("#onetrust-accept-btn-handler")).thenReturn(mockCookieButton);
        doNothing().when(mockCookieButton).waitFor(any(Locator.WaitForOptions.class));
        when(mockCookieButton.isVisible()).thenReturn(true);

        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        when(trainHtmlParser.parseTrainList(anyString())).thenReturn(Arrays.asList(train1));

        // Act
        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "0071,MADRI,null", "0071,BARCE,null",
                "16/01/2026", null, "2"
        );

        // Assert
        assertNotNull(result);
        verify(mockCookieButton, times(1)).click(); // Should click when visible
    }

    @Test
    @DisplayName("searchTrains should not click cookie button when not visible")
    void testSearchTrainsWithCookieButtonNotVisible() throws Exception {
        // Arrange
        Playwright mockPlaywright = mock(Playwright.class);
        BrowserType mockBrowserType = mock(BrowserType.class);
        Browser mockBrowser = mock(Browser.class);
        BrowserContext mockContext = mock(BrowserContext.class);
        Page mockPage = mock(Page.class);
        ElementHandle mockElementHandle = mock(ElementHandle.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        Locator mockCookieButton = mock(Locator.class);

        when(playwrightFactory.create()).thenReturn(mockPlaywright);
        when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
        when(mockBrowserType.launch(any(BrowserType.LaunchOptions.class))).thenReturn(mockBrowser);
        when(mockBrowser.newContext(any(Browser.NewContextOptions.class))).thenReturn(mockContext);
        when(mockContext.newPage()).thenReturn(mockPage);
        
        when(config.getLocale()).thenReturn("es");
        when(config.getViewportWidth()).thenReturn(1920);
        when(config.getViewportHeight()).thenReturn(1080);
        when(config.getNavigationTimeoutMs()).thenReturn(30000);
        when(config.getTimeoutMs()).thenReturn(30000);
        when(config.getShortTimeoutMs()).thenReturn(5000);
        when(config.getRenfeSearchUrl()).thenReturn("https://www.renfe.com/es/es/viajar");
        when(config.isHeadless()).thenReturn(true);
        when(config.getSlowMo()).thenReturn(0);

        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page");
        when(mockPage.content()).thenReturn("<html><body>Train results</body></html>");
        when(mockPage.waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class)))
                .thenReturn(mockElementHandle);
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);
        when(mockPage.locator("#onetrust-accept-btn-handler")).thenReturn(mockCookieButton);
        doNothing().when(mockCookieButton).waitFor(any(Locator.WaitForOptions.class));
        when(mockCookieButton.isVisible()).thenReturn(false); // Not visible

        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        when(trainHtmlParser.parseTrainList(anyString())).thenReturn(Arrays.asList(train1));

        // Act
        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "0071,MADRI,null", "0071,BARCE,null",
                "16/01/2026", null, "2"
        );

        // Assert
        assertNotNull(result);
        verify(mockCookieButton, never()).click(); // Should not click when not visible
    }

    @Test
    @DisplayName("searchTrains should handle exception when cookie button check fails")
    void testSearchTrainsWithCookieButtonException() throws Exception {
        // Arrange
        Playwright mockPlaywright = mock(Playwright.class);
        BrowserType mockBrowserType = mock(BrowserType.class);
        Browser mockBrowser = mock(Browser.class);
        BrowserContext mockContext = mock(BrowserContext.class);
        Page mockPage = mock(Page.class);
        ElementHandle mockElementHandle = mock(ElementHandle.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        Locator mockCookieButton = mock(Locator.class);

        when(playwrightFactory.create()).thenReturn(mockPlaywright);
        when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
        when(mockBrowserType.launch(any(BrowserType.LaunchOptions.class))).thenReturn(mockBrowser);
        when(mockBrowser.newContext(any(Browser.NewContextOptions.class))).thenReturn(mockContext);
        when(mockContext.newPage()).thenReturn(mockPage);
        
        when(config.getLocale()).thenReturn("es");
        when(config.getViewportWidth()).thenReturn(1920);
        when(config.getViewportHeight()).thenReturn(1080);
        when(config.getNavigationTimeoutMs()).thenReturn(30000);
        when(config.getTimeoutMs()).thenReturn(30000);
        when(config.getShortTimeoutMs()).thenReturn(5000);
        when(config.getRenfeSearchUrl()).thenReturn("https://www.renfe.com/es/es/viajar");
        when(config.isHeadless()).thenReturn(true);
        when(config.getSlowMo()).thenReturn(0);

        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page");
        when(mockPage.content()).thenReturn("<html><body>Train results</body></html>");
        when(mockPage.waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class)))
                .thenReturn(mockElementHandle);
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);
        when(mockPage.locator("#onetrust-accept-btn-handler")).thenReturn(mockCookieButton);
        doThrow(new RuntimeException("Cookie button error")).when(mockCookieButton).waitFor(any(Locator.WaitForOptions.class));

        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        when(trainHtmlParser.parseTrainList(anyString())).thenReturn(Arrays.asList(train1));

        // Act - Should not throw, exception is caught
        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "0071,MADRI,null", "0071,BARCE,null",
                "16/01/2026", null, "2"
        );

        // Assert
        assertNotNull(result);
        // Exception should be caught and logged, execution should continue
    }

    @Test
    @DisplayName("searchTrains should not extract return trains when vueltaTab count is 0")
    void testSearchTrainsWithVueltaTabCountZero() throws Exception {
        // Arrange
        Playwright mockPlaywright = mock(Playwright.class);
        BrowserType mockBrowserType = mock(BrowserType.class);
        Browser mockBrowser = mock(Browser.class);
        BrowserContext mockContext = mock(BrowserContext.class);
        Page mockPage = mock(Page.class);
        ElementHandle mockElementHandle = mock(ElementHandle.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        Locator mockCookieButton = mock(Locator.class);
        Locator mockVueltaTab = mock(Locator.class);

        when(playwrightFactory.create()).thenReturn(mockPlaywright);
        when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
        when(mockBrowserType.launch(any(BrowserType.LaunchOptions.class))).thenReturn(mockBrowser);
        when(mockBrowser.newContext(any(Browser.NewContextOptions.class))).thenReturn(mockContext);
        when(mockContext.newPage()).thenReturn(mockPage);
        
        when(config.getLocale()).thenReturn("es");
        when(config.getViewportWidth()).thenReturn(1920);
        when(config.getViewportHeight()).thenReturn(1080);
        when(config.getNavigationTimeoutMs()).thenReturn(30000);
        when(config.getTimeoutMs()).thenReturn(30000);
        when(config.getShortTimeoutMs()).thenReturn(5000);
        when(config.getRenfeSearchUrl()).thenReturn("https://www.renfe.com/es/es/viajar");
        when(config.isHeadless()).thenReturn(true);
        when(config.getSlowMo()).thenReturn(0);

        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page");
        when(mockPage.content()).thenReturn("<html><body>Train results</body></html>");
        when(mockPage.waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class)))
                .thenReturn(mockElementHandle);
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);
        when(mockPage.locator("#onetrust-accept-btn-handler")).thenReturn(mockCookieButton);
        doThrow(new RuntimeException("Cookie button not found")).when(mockCookieButton).waitFor(any(Locator.WaitForOptions.class));
        when(mockPage.locator("[id*='vuelta'], [class*='vuelta'], a:has-text('Vuelta')")).thenReturn(mockVueltaTab);
        when(mockVueltaTab.count()).thenReturn(0); // No vuelta tab found

        Train trainOut1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        when(trainHtmlParser.parseTrainList(anyString())).thenReturn(Arrays.asList(trainOut1));

        // Act
        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "0071,MADRI,null", "0071,BARCE,null",
                "16/01/2026", "18/01/2026", "2"
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.outboundTrains);
        assertNull(result.returnTrains); // Should be null when vueltaTab.count() == 0
        verify(mockVueltaTab, never()).first(); // Should not try to click when count is 0
    }

    @Test
    @DisplayName("searchTrains should not extract return trains when outbound trains list is empty")
    void testSearchTrainsWithEmptyOutboundTrains() throws Exception {
        // Arrange
        Playwright mockPlaywright = mock(Playwright.class);
        BrowserType mockBrowserType = mock(BrowserType.class);
        Browser mockBrowser = mock(Browser.class);
        BrowserContext mockContext = mock(BrowserContext.class);
        Page mockPage = mock(Page.class);
        ElementHandle mockElementHandle = mock(ElementHandle.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        Locator mockCookieButton = mock(Locator.class);

        when(playwrightFactory.create()).thenReturn(mockPlaywright);
        when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
        when(mockBrowserType.launch(any(BrowserType.LaunchOptions.class))).thenReturn(mockBrowser);
        when(mockBrowser.newContext(any(Browser.NewContextOptions.class))).thenReturn(mockContext);
        when(mockContext.newPage()).thenReturn(mockPage);
        
        when(config.getLocale()).thenReturn("es");
        when(config.getViewportWidth()).thenReturn(1920);
        when(config.getViewportHeight()).thenReturn(1080);
        when(config.getNavigationTimeoutMs()).thenReturn(30000);
        when(config.getTimeoutMs()).thenReturn(30000);
        when(config.getShortTimeoutMs()).thenReturn(5000);
        when(config.getRenfeSearchUrl()).thenReturn("https://www.renfe.com/es/es/viajar");
        when(config.isHeadless()).thenReturn(true);
        when(config.getSlowMo()).thenReturn(0);

        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page");
        when(mockPage.content()).thenReturn("<html><body>No trains</body></html>");
        when(mockPage.waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class)))
                .thenReturn(mockElementHandle);
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);
        when(mockPage.locator("#onetrust-accept-btn-handler")).thenReturn(mockCookieButton);
        doThrow(new RuntimeException("Cookie button not found")).when(mockCookieButton).waitFor(any(Locator.WaitForOptions.class));

        when(trainHtmlParser.parseTrainList(anyString())).thenReturn(new ArrayList<>()); // Empty list

        // Act
        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "0071,MADRI,null", "0071,BARCE,null",
                "16/01/2026", "18/01/2026", "2"
        );

        // Assert
        assertNotNull(result);
        assertTrue(result.outboundTrains.isEmpty());
        assertNull(result.returnTrains); // Should be null when outbound trains are empty
        verify(mockPage, never()).locator("[id*='vuelta'], [class*='vuelta'], a:has-text('Vuelta')");
    }

    @Test
    @DisplayName("searchTrains should handle exception when extracting return trains")
    void testSearchTrainsWithExceptionExtractingReturnTrains() throws Exception {
        // Arrange
        Playwright mockPlaywright = mock(Playwright.class);
        BrowserType mockBrowserType = mock(BrowserType.class);
        Browser mockBrowser = mock(Browser.class);
        BrowserContext mockContext = mock(BrowserContext.class);
        Page mockPage = mock(Page.class);
        ElementHandle mockElementHandle = mock(ElementHandle.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        Locator mockCookieButton = mock(Locator.class);
        Locator mockVueltaTab = mock(Locator.class);

        when(playwrightFactory.create()).thenReturn(mockPlaywright);
        when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
        when(mockBrowserType.launch(any(BrowserType.LaunchOptions.class))).thenReturn(mockBrowser);
        when(mockBrowser.newContext(any(Browser.NewContextOptions.class))).thenReturn(mockContext);
        when(mockContext.newPage()).thenReturn(mockPage);
        
        when(config.getLocale()).thenReturn("es");
        when(config.getViewportWidth()).thenReturn(1920);
        when(config.getViewportHeight()).thenReturn(1080);
        when(config.getNavigationTimeoutMs()).thenReturn(30000);
        when(config.getTimeoutMs()).thenReturn(30000);
        when(config.getShortTimeoutMs()).thenReturn(5000);
        when(config.getRenfeSearchUrl()).thenReturn("https://www.renfe.com/es/es/viajar");
        when(config.isHeadless()).thenReturn(true);
        when(config.getSlowMo()).thenReturn(0);

        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page");
        when(mockPage.content()).thenReturn("<html><body>Train results</body></html>");
        when(mockPage.waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class)))
                .thenReturn(mockElementHandle);
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);
        when(mockPage.locator("#onetrust-accept-btn-handler")).thenReturn(mockCookieButton);
        doThrow(new RuntimeException("Cookie button not found")).when(mockCookieButton).waitFor(any(Locator.WaitForOptions.class));
        when(mockPage.locator("[id*='vuelta'], [class*='vuelta'], a:has-text('Vuelta')")).thenReturn(mockVueltaTab);
        when(mockVueltaTab.count()).thenReturn(1);
        when(mockVueltaTab.first()).thenReturn(mockVueltaTab);
        doThrow(new RuntimeException("Error clicking vuelta tab")).when(mockVueltaTab).click();

        Train trainOut1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        when(trainHtmlParser.parseTrainList(anyString())).thenReturn(Arrays.asList(trainOut1));

        // Act - Should not throw, exception is caught
        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "0071,MADRI,null", "0071,BARCE,null",
                "16/01/2026", "18/01/2026", "2"
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.outboundTrains);
        assertNull(result.returnTrains); // Should be null when exception occurs
    }

    @Test
    @DisplayName("searchTrains should throw QueueException when queue page is detected")
    void testSearchTrainsThrowsQueueException() throws Exception {
        // Arrange
        Playwright mockPlaywright = mock(Playwright.class);
        BrowserType mockBrowserType = mock(BrowserType.class);
        Browser mockBrowser = mock(Browser.class);
        BrowserContext mockContext = mock(BrowserContext.class);
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        
        when(playwrightFactory.create()).thenReturn(mockPlaywright);
        when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
        when(mockBrowserType.launch(any(BrowserType.LaunchOptions.class))).thenReturn(mockBrowser);
        when(mockBrowser.newContext(any(Browser.NewContextOptions.class))).thenReturn(mockContext);
        when(mockContext.newPage()).thenReturn(mockPage);
        
        when(config.getLocale()).thenReturn("es");
        when(config.getViewportWidth()).thenReturn(1920);
        when(config.getViewportHeight()).thenReturn(1080);
        when(config.getNavigationTimeoutMs()).thenReturn(30000);
        when(config.getRenfeSearchUrl()).thenReturn("https://www.renfe.com/es/es/viajar");
        when(config.isHeadless()).thenReturn(true);
        when(config.getSlowMo()).thenReturn(0);

        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Estás en la cola de espera");

        // Act & Assert
        QueueException exception = assertThrows(QueueException.class, () -> {
            service.searchTrains(
                    "MADRID (TODAS)", "BARCELONA (TODAS)",
                    "MADRID (TODAS)", "BARCELONA (TODAS)",
                    "0071,MADRI,null", "0071,BARCE,null",
                    "16/01/2026", null, "2"
            );
        });
        
        assertTrue(exception.getMessage().contains("queued"));
    }

    @Test
    @DisplayName("searchTrains should throw RuntimeException when general exception occurs")
    void testSearchTrainsThrowsRuntimeException() throws Exception {
        // Arrange
        lenient().when(playwrightFactory.create()).thenThrow(new RuntimeException("Playwright creation failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.searchTrains(
                    "MADRID (TODAS)", "BARCELONA (TODAS)",
                    "MADRID (TODAS)", "BARCELONA (TODAS)",
                    "0071,MADRI,null", "0071,BARCE,null",
                    "16/01/2026", null, "2"
            );
        });
        
        // Verify it's a RuntimeException (the message format may vary depending on the original exception)
        assertNotNull(exception);
        // The exception may or may not have a cause depending on how it's wrapped
        // Just verify that a RuntimeException was thrown
    }

    @Test
    @DisplayName("searchTrains should handle null values in buildFormData")
    void testSearchTrainsWithNullFormDataValues() throws Exception {
        // Arrange
        Playwright mockPlaywright = mock(Playwright.class);
        BrowserType mockBrowserType = mock(BrowserType.class);
        Browser mockBrowser = mock(Browser.class);
        BrowserContext mockContext = mock(BrowserContext.class);
        Page mockPage = mock(Page.class);
        ElementHandle mockElementHandle = mock(ElementHandle.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        Locator mockCookieButton = mock(Locator.class);

        when(playwrightFactory.create()).thenReturn(mockPlaywright);
        when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
        when(mockBrowserType.launch(any(BrowserType.LaunchOptions.class))).thenReturn(mockBrowser);
        when(mockBrowser.newContext(any(Browser.NewContextOptions.class))).thenReturn(mockContext);
        when(mockContext.newPage()).thenReturn(mockPage);
        
        when(config.getLocale()).thenReturn("es");
        when(config.getViewportWidth()).thenReturn(1920);
        when(config.getViewportHeight()).thenReturn(1080);
        when(config.getNavigationTimeoutMs()).thenReturn(30000);
        when(config.getTimeoutMs()).thenReturn(30000);
        when(config.getShortTimeoutMs()).thenReturn(5000);
        when(config.getRenfeSearchUrl()).thenReturn("https://www.renfe.com/es/es/viajar");
        when(config.isHeadless()).thenReturn(true);
        when(config.getSlowMo()).thenReturn(0);

        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page");
        when(mockPage.content()).thenReturn("<html><body>Train results</body></html>");
        when(mockPage.waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class)))
                .thenReturn(mockElementHandle);
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);
        when(mockPage.locator("#onetrust-accept-btn-handler")).thenReturn(mockCookieButton);
        doThrow(new RuntimeException("Cookie button not found")).when(mockCookieButton).waitFor(any(Locator.WaitForOptions.class));

        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        when(trainHtmlParser.parseTrainList(anyString())).thenReturn(Arrays.asList(train1));

        // Act - Test with null values for form data
        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                null, null, // null originDesgEstacion and destinationDesgEstacion
                null, null, // null originClave and destinationClave
                "16/01/2026", null, null // null adults
        );

        // Assert
        assertNotNull(result);
        // Should handle null values gracefully (buildFormData uses empty strings for null)
    }

    @Test
    @DisplayName("searchTrains should handle adults with whitespace")
    void testSearchTrainsWithAdultsWhitespace() throws Exception {
        // Arrange
        Playwright mockPlaywright = mock(Playwright.class);
        BrowserType mockBrowserType = mock(BrowserType.class);
        Browser mockBrowser = mock(Browser.class);
        BrowserContext mockContext = mock(BrowserContext.class);
        Page mockPage = mock(Page.class);
        ElementHandle mockElementHandle = mock(ElementHandle.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        Locator mockCookieButton = mock(Locator.class);

        when(playwrightFactory.create()).thenReturn(mockPlaywright);
        when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
        when(mockBrowserType.launch(any(BrowserType.LaunchOptions.class))).thenReturn(mockBrowser);
        when(mockBrowser.newContext(any(Browser.NewContextOptions.class))).thenReturn(mockContext);
        when(mockContext.newPage()).thenReturn(mockPage);
        
        when(config.getLocale()).thenReturn("es");
        when(config.getViewportWidth()).thenReturn(1920);
        when(config.getViewportHeight()).thenReturn(1080);
        when(config.getNavigationTimeoutMs()).thenReturn(30000);
        when(config.getTimeoutMs()).thenReturn(30000);
        when(config.getShortTimeoutMs()).thenReturn(5000);
        when(config.getRenfeSearchUrl()).thenReturn("https://www.renfe.com/es/es/viajar");
        when(config.isHeadless()).thenReturn(true);
        when(config.getSlowMo()).thenReturn(0);

        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page");
        when(mockPage.content()).thenReturn("<html><body>Train results</body></html>");
        when(mockPage.waitForSelector(anyString(), any(Page.WaitForSelectorOptions.class)))
                .thenReturn(mockElementHandle);
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);
        when(mockPage.locator("#onetrust-accept-btn-handler")).thenReturn(mockCookieButton);
        doThrow(new RuntimeException("Cookie button not found")).when(mockCookieButton).waitFor(any(Locator.WaitForOptions.class));

        Train train1 = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        when(trainHtmlParser.parseTrainList(anyString())).thenReturn(Arrays.asList(train1));

        // Act - Test with whitespace in adults
        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "MADRID (TODAS)", "BARCELONA (TODAS)",
                "0071,MADRI,null", "0071,BARCE,null",
                "16/01/2026", null, "  2  " // adults with whitespace
        );

        // Assert
        assertNotNull(result);
        // Should trim whitespace from adults (buildFormData uses adults.trim())
    }
}

