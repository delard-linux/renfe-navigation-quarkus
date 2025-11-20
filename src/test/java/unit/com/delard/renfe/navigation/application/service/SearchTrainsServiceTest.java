package com.delard.renfe.navigation.application.service;

import com.delard.renfe.navigation.application.exception.QueueException;
import com.delard.renfe.navigation.application.exception.TrainUnavailabilityException;
import com.delard.renfe.navigation.application.exception.ValidationException;
import com.delard.renfe.navigation.domain.model.Station;
import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.domain.model.TrainsResponse;
import com.delard.renfe.navigation.domain.port.input.GetStationsUseCase;
import com.delard.renfe.navigation.domain.port.output.TrainScraperPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SearchTrainsService
 * 
 * Uses real search data: Madrid to Barcelona
 * Dates: 16/01/2026 (outbound) and 18/01/2026 (return)
 */
@ExtendWith(MockitoExtension.class)
class SearchTrainsServiceTest {

    // Real search parameters from resultado_search_trains.html
    private static final String REAL_ORIGIN = "MADRID";
    private static final String REAL_DESTINATION = "BARCELONA";
    private static final String REAL_DATE_OUT = "2026-01-16";  // Input format: yyyy-MM-dd
    private static final String REAL_DATE_RETURN = "2026-01-18";  // Input format: yyyy-MM-dd
    
    // Formatted dates (output format: dd/MM/yyyy) that will be used after validation
    private static final String FORMATTED_DATE_OUT = "16/01/2026";
    private static final String FORMATTED_DATE_RETURN = "18/01/2026";
    
    // Real station names (desgEstacionPlano) that will be used after validation
    private static final String REAL_ORIGIN_STATION_NAME = "MADRID (TODAS)";
    private static final String REAL_DESTINATION_STATION_NAME = "BARCELONA (TODAS)";
    
    // Station data for form submission (from Station objects)
    private static final String REAL_ORIGIN_DESG_ESTACION = "MADRID (TODAS)";
    private static final String REAL_DESTINATION_DESG_ESTACION = "BARCELONA (TODAS)";
    private static final String REAL_ORIGIN_CLAVE = "0071,MADRI,null";
    private static final String REAL_DESTINATION_CLAVE = "0071,BARCE,null";

    @Mock
    private TrainScraperPort trainScraperPort;

    @Mock
    private GetStationsUseCase getStationsUseCase;

    @InjectMocks
    private SearchTrainsService service;

    @BeforeEach
    void setUp() {
        service = new SearchTrainsService();
        // Use reflection to inject the mocks
        try {
            java.lang.reflect.Field field1 = SearchTrainsService.class.getDeclaredField("trainScraperPort");
            field1.setAccessible(true);
            field1.set(service, trainScraperPort);

            java.lang.reflect.Field field2 = SearchTrainsService.class.getDeclaredField("getStationsUseCase");
            field2.setAccessible(true);
            field2.set(service, getStationsUseCase);
        } catch (Exception e) {
            fail("Failed to inject mocks: " + e.getMessage());
        }

        // Default: return one station for origin and destination (valid case)
        // Use lenient() to avoid unnecessary stubbing errors in tests that override these
        Station originStation = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "MADRID (TODAS)");
        Station destinationStation = new Station("BARCE", "0071", 3, null,
                "BARCELONA (TODAS)", null, "0071,BARCE,null", "BARCELONA (TODAS)");

        lenient().when(getStationsUseCase.searchStations(REAL_ORIGIN)).thenReturn(List.of(originStation));
        lenient().when(getStationsUseCase.searchStations(REAL_DESTINATION)).thenReturn(List.of(destinationStation));
    }

    @Test
    @DisplayName("Should return complete response with outbound and return trains when both are available")
    void shouldReturnCompleteResponseWithOutboundAndReturnTrains() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = REAL_DATE_RETURN;
        String adults = "2";

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        Train trainOut2 = createTrain("T456", "ALVIA", "12:00", "14:00", "2h", 30.0);
        Train trainRet1 = createTrain("T789", "AVE", "16:00", "18:00", "2h", 25.0);

        List<Train> trainsOut = Arrays.asList(trainOut1, trainOut2);
        List<Train> trainsReturn = Arrays.asList(trainRet1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut, trainsReturn);

        when(trainScraperPort.scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, FORMATTED_DATE_RETURN, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals(REAL_ORIGIN_STATION_NAME, result.getOrigin());
        assertEquals(REAL_DESTINATION_STATION_NAME, result.getDestination());
        assertEquals(FORMATTED_DATE_OUT, result.getDateOut());
        assertEquals(FORMATTED_DATE_RETURN, result.getDateReturn());
        assertEquals(adults, result.getAdults());
        assertNotNull(result.getTrainsOut());
        assertEquals(2, result.getTrainsOut().size());
        assertNotNull(result.getTrainsReturn());
        assertEquals(1, result.getTrainsReturn().size());

        verify(trainScraperPort, times(1)).scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, FORMATTED_DATE_RETURN, adults);
    }

    @Test
    @DisplayName("Should return response with only outbound trains when return date is null")
    void shouldReturnResponseWithOnlyOutboundTrainsWhenReturnDateIsNull() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        when(trainScraperPort.scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, null, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals(REAL_ORIGIN_STATION_NAME, result.getOrigin());
        assertEquals(REAL_DESTINATION_STATION_NAME, result.getDestination());
        assertEquals(FORMATTED_DATE_OUT, result.getDateOut());
        assertEquals(dateReturn, result.getDateReturn());
        assertEquals(adults, result.getAdults());
        assertNotNull(result.getTrainsOut());
        assertEquals(1, result.getTrainsOut().size());
        assertNull(result.getTrainsReturn());

        verify(trainScraperPort, times(1)).scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, null, adults);
    }

    @Test
    @DisplayName("Should return empty return trains list when scraper returns empty list for return trains")
    void shouldReturnEmptyReturnTrainsListWhenScraperReturnsEmptyList() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = REAL_DATE_RETURN;
        String adults = "2";

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<Train> trainsReturn = new ArrayList<>();
        List<List<Train>> scraperResult = Arrays.asList(trainsOut, trainsReturn);

        when(trainScraperPort.scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, FORMATTED_DATE_RETURN, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals(1, result.getTrainsOut().size());
        assertNotNull(result.getTrainsReturn());
        assertEquals(0, result.getTrainsReturn().size());
    }

    @Test
    @DisplayName("Should return empty outbound trains list when scraper returns empty list")
    void shouldReturnEmptyOutboundTrainsListWhenScraperReturnsEmptyList() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        List<Train> trainsOut = new ArrayList<>();
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        when(trainScraperPort.scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, null, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertNotNull(result.getTrainsOut());
        assertEquals(0, result.getTrainsOut().size());
        assertNull(result.getTrainsReturn());
    }

    @Test
    @DisplayName("Should throw RuntimeException with error message when scraper throws exception")
    void shouldThrowRuntimeExceptionWhenScraperThrowsException() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = REAL_DATE_RETURN;
        String adults = "2";
        String errorMessage = "Scraping failed";

        when(trainScraperPort.scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, FORMATTED_DATE_RETURN, adults))
                .thenThrow(new RuntimeException(errorMessage));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.searchTrains(origin, destination, dateOut, dateReturn, adults);
        });

        assertTrue(exception.getMessage().contains("Error searching trains"));
        assertTrue(exception.getMessage().contains(errorMessage));
        assertNotNull(exception.getCause());

        verify(trainScraperPort, times(1)).scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, FORMATTED_DATE_RETURN, adults);
    }

    @Test
    @DisplayName("Should re-throw QueueException when scraper throws QueueException")
    void shouldReThrowQueueExceptionWhenScraperThrowsQueueException() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = REAL_DATE_RETURN;
        String adults = "2";
        String queueMessage = "Ticket purchase is queued. The system redirected to a queue management page. Please try again later.";

        when(trainScraperPort.scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, FORMATTED_DATE_RETURN, adults))
                .thenThrow(new QueueException(queueMessage));

        QueueException exception = assertThrows(QueueException.class, () -> {
            service.searchTrains(origin, destination, dateOut, dateReturn, adults);
        });

        assertEquals(queueMessage, exception.getMessage());
        assertNull(exception.getCause());

        verify(trainScraperPort, times(1)).scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, FORMATTED_DATE_RETURN, adults);
    }

    @Test
    @DisplayName("Should handle different number of adults correctly (1, 2, 3, 5 adults)")
    void shouldHandleDifferentNumberOfAdultsCorrectly() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        when(trainScraperPort.scrapeTrains(eq(REAL_ORIGIN_STATION_NAME), eq(REAL_DESTINATION_STATION_NAME),
                eq(REAL_ORIGIN_DESG_ESTACION), eq(REAL_DESTINATION_DESG_ESTACION),
                eq(REAL_ORIGIN_CLAVE), eq(REAL_DESTINATION_CLAVE),
                eq(FORMATTED_DATE_OUT), eq((String) null), anyString()))
                .thenReturn(scraperResult);

        TrainsResponse result1 = service.searchTrains(origin, destination, dateOut, dateReturn, "1");
        TrainsResponse result2 = service.searchTrains(origin, destination, dateOut, dateReturn, "2");
        TrainsResponse result3 = service.searchTrains(origin, destination, dateOut, dateReturn, "3");
        TrainsResponse result4 = service.searchTrains(origin, destination, dateOut, dateReturn, "5");

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertNotNull(result4);
        assertEquals("1", result1.getAdults());
        assertEquals("2", result2.getAdults());
        assertEquals("3", result3.getAdults());
        assertEquals("5", result4.getAdults());

        verify(trainScraperPort, times(4)).scrapeTrains(eq(REAL_ORIGIN_STATION_NAME), eq(REAL_DESTINATION_STATION_NAME),
                eq(REAL_ORIGIN_DESG_ESTACION), eq(REAL_DESTINATION_DESG_ESTACION),
                eq(REAL_ORIGIN_CLAVE), eq(REAL_DESTINATION_CLAVE),
                eq(FORMATTED_DATE_OUT), eq((String) null), anyString());
    }

    @Test
    @DisplayName("Should throw ValidationException when input values are null or invalid")
    void shouldHandleNullInputValuesGracefully() {
        // Test null origin
        ValidationException exception1 = assertThrows(ValidationException.class, () -> {
            service.searchTrains(null, REAL_DESTINATION, REAL_DATE_OUT, null, "2");
        });
        assertEquals("Origin is required", exception1.getMessage());

        // Test null destination
        ValidationException exception2 = assertThrows(ValidationException.class, () -> {
            service.searchTrains(REAL_ORIGIN, null, REAL_DATE_OUT, null, "2");
        });
        assertEquals("Destination is required", exception2.getMessage());

        // Test null dateOut
        ValidationException exception3 = assertThrows(ValidationException.class, () -> {
            service.searchTrains(REAL_ORIGIN, REAL_DESTINATION, null, null, "2");
        });
        assertTrue(exception3.getMessage().contains("dateOut is required") || exception3.getMessage().contains("Date out is required"));

        // Test adults = 0
        ValidationException exception4 = assertThrows(ValidationException.class, () -> {
            service.searchTrains(REAL_ORIGIN, REAL_DESTINATION, REAL_DATE_OUT, null, "0");
        });
        assertEquals("Adults must be greater than 0", exception4.getMessage());

        // Test adults < 0
        ValidationException exception5 = assertThrows(ValidationException.class, () -> {
            service.searchTrains(REAL_ORIGIN, REAL_DESTINATION, REAL_DATE_OUT, null, "-1");
        });
        assertEquals("Adults must be greater than 0", exception5.getMessage());
        
        // Test adults = null
        ValidationException exception6 = assertThrows(ValidationException.class, () -> {
            service.searchTrains(REAL_ORIGIN, REAL_DESTINATION, REAL_DATE_OUT, null, null);
        });
        assertEquals("Adults is required", exception6.getMessage());
        
        // Test adults = blank
        ValidationException exception7 = assertThrows(ValidationException.class, () -> {
            service.searchTrains(REAL_ORIGIN, REAL_DESTINATION, REAL_DATE_OUT, null, "");
        });
        assertEquals("Adults is required", exception7.getMessage());
        
        // Test adults = invalid format
        ValidationException exception8 = assertThrows(ValidationException.class, () -> {
            service.searchTrains(REAL_ORIGIN, REAL_DESTINATION, REAL_DATE_OUT, null, "abc");
        });
        assertEquals("Adults must be a valid number", exception8.getMessage());
        
        // Test adults > 8
        ValidationException exception9 = assertThrows(ValidationException.class, () -> {
            service.searchTrains(REAL_ORIGIN, REAL_DESTINATION, REAL_DATE_OUT, null, "9");
        });
        assertEquals("Adults must be at most 8", exception9.getMessage());
        
        // Test adults = 8 (boundary value - should be valid)
        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);
        
        when(trainScraperPort.scrapeTrains(eq(REAL_ORIGIN_STATION_NAME), eq(REAL_DESTINATION_STATION_NAME),
                eq(REAL_ORIGIN_DESG_ESTACION), eq(REAL_DESTINATION_DESG_ESTACION),
                eq(REAL_ORIGIN_CLAVE), eq(REAL_DESTINATION_CLAVE),
                eq(FORMATTED_DATE_OUT), eq((String) null), eq("8")))
                .thenReturn(scraperResult);
        
        TrainsResponse result = service.searchTrains(REAL_ORIGIN, REAL_DESTINATION, REAL_DATE_OUT, null, "8");
        assertNotNull(result);
        assertEquals("8", result.getAdults());
    }

    @Test
    @DisplayName("Should throw ValidationException when dateOut format is invalid")
    void shouldThrowValidationExceptionWhenDateOutFormatIsInvalid() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String invalidDateOut = "16-01-2026";  // Wrong format (should be yyyy-MM-dd)
        String dateReturn = null;
        String adults = "2";

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.searchTrains(origin, destination, invalidDateOut, dateReturn, adults);
        });

        assertTrue(exception.getMessage().contains("Invalid date format"));
        assertTrue(exception.getMessage().contains("dateOut"));
        assertTrue(exception.getMessage().contains(invalidDateOut));
        assertTrue(exception.getMessage().contains("yyyy-MM-dd"));
        verify(trainScraperPort, never()).scrapeTrains(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw ValidationException when dateReturn format is invalid")
    void shouldThrowValidationExceptionWhenDateReturnFormatIsInvalid() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String invalidDateReturn = "18-01-2026";  // Wrong format (should be yyyy-MM-dd)
        String adults = "2";

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.searchTrains(origin, destination, dateOut, invalidDateReturn, adults);
        });

        assertTrue(exception.getMessage().contains("Invalid date format"));
        assertTrue(exception.getMessage().contains("dateReturn"));
        assertTrue(exception.getMessage().contains(invalidDateReturn));
        assertTrue(exception.getMessage().contains("yyyy-MM-dd"));
        verify(trainScraperPort, never()).scrapeTrains(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should format dates correctly from yyyy-MM-dd to dd/MM/yyyy")
    void shouldFormatDatesCorrectly() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = "2026-12-25";  // Different date to verify formatting
        String dateReturn = null;
        String adults = "2";

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        // Expect formatted date: 25/12/2026
        when(trainScraperPort.scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                "25/12/2026", null, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals("25/12/2026", result.getDateOut());
        verify(trainScraperPort, times(1)).scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                "25/12/2026", null, adults);
    }

    @Test
    @DisplayName("Should return null for return trains when scraper returns single list (only outbound)")
    void shouldReturnNullForReturnTrainsWhenScraperReturnsSingleList() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        // Result with only one element (no return trains)
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        when(trainScraperPort.scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, null, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals(1, result.getTrainsOut().size());
        // When result.size() == 1, trainsReturn should be null
        assertNull(result.getTrainsReturn());
        verify(trainScraperPort, times(1)).scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, null, adults);
    }

    @Test
    @DisplayName("Should handle null trainsOut list correctly")
    void shouldHandleNullTrainsOutListCorrectly() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        // Simulate scraper returning a list with null first element
        List<List<Train>> scraperResult = new ArrayList<>();
        scraperResult.add(null);

        when(trainScraperPort.scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, null, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertNull(result.getTrainsOut());
        assertNull(result.getTrainsReturn());
    }

    @Test
    @DisplayName("Should handle null trainsReturn list correctly when result has two elements")
    void shouldHandleNullTrainsReturnListCorrectly() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = REAL_DATE_RETURN;
        String adults = "2";

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut, null);

        when(trainScraperPort.scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, FORMATTED_DATE_RETURN, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertNotNull(result.getTrainsOut());
        assertEquals(1, result.getTrainsOut().size());
        assertNull(result.getTrainsReturn());
    }

    @Test
    @DisplayName("Should throw ValidationException when origin station is not found")
    void shouldThrowValidationExceptionWhenOriginStationNotFound() {
        String origin = "NONEXISTENT";
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        when(getStationsUseCase.searchStations(origin)).thenReturn(List.of());

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.searchTrains(origin, destination, dateOut, dateReturn, adults);
        });

        assertTrue(exception.getMessage().contains("No station found matching"));
        assertTrue(exception.getMessage().contains("origin"));
        verify(getStationsUseCase, times(1)).searchStations(origin);
        verify(trainScraperPort, never()).scrapeTrains(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw ValidationException when destination station is not found")
    void shouldThrowValidationExceptionWhenDestinationStationNotFound() {
        String origin = REAL_ORIGIN;
        String destination = "NONEXISTENT";
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        when(getStationsUseCase.searchStations(destination)).thenReturn(List.of());

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.searchTrains(origin, destination, dateOut, dateReturn, adults);
        });

        assertTrue(exception.getMessage().contains("No station found matching"));
        assertTrue(exception.getMessage().contains("destination"));
        verify(getStationsUseCase, times(1)).searchStations(origin);
        verify(getStationsUseCase, times(1)).searchStations(destination);
        verify(trainScraperPort, never()).scrapeTrains(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw ValidationException when multiple stations match origin")
    void shouldThrowValidationExceptionWhenMultipleStationsMatchOrigin() {
        String origin = "MADRID";
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        Station station1 = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "MADRID (TODAS)");
        Station station2 = new Station("60000", "0071", 2, null,
                "MADRID-PUERTA DE ATOCHA-ALMUDENA GRANDES", "00600", "0071,60000,00600",
                "MADRID-PUERTA DE ATOCHA-ALMUDENA GRANDES");
        Station station3 = new Station("60001", "0071", 3, null,
                "MADRID-CHAMARTIN", "00601", "0071,60001,00601", "MADRID-CHAMARTIN");

        when(getStationsUseCase.searchStations(origin)).thenReturn(List.of(station1, station2, station3));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.searchTrains(origin, destination, dateOut, dateReturn, adults);
        });

        assertTrue(exception.getMessage().contains("Please provide a more precise station name"));
        assertTrue(exception.getMessage().contains("origin"));
        assertTrue(exception.getMessage().contains("MADRID (TODAS)"));
        assertTrue(exception.getMessage().contains("MADRID-PUERTA DE ATOCHA-ALMUDENA GRANDES"));
        assertTrue(exception.getMessage().contains("MADRID-CHAMARTIN"));
        // Note: The message uses stationNamePlano, which should be the same as stationName in this case
        verify(getStationsUseCase, times(1)).searchStations(origin);
        verify(trainScraperPort, never()).scrapeTrains(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw ValidationException when multiple stations match destination")
    void shouldThrowValidationExceptionWhenMultipleStationsMatchDestination() {
        String origin = REAL_ORIGIN;
        String destination = "BARCELONA";
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        Station station1 = new Station("BARCE", "0071", 1, null,
                "BARCELONA (TODAS)", null, "0071,BARCE,null", "BARCELONA (TODAS)");
        Station station2 = new Station("70000", "0071", 2, null,
                "BARCELONA-SANTS", "00700", "0071,70000,00700", "BARCELONA-SANTS");

        when(getStationsUseCase.searchStations(destination)).thenReturn(List.of(station1, station2));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.searchTrains(origin, destination, dateOut, dateReturn, adults);
        });

        assertTrue(exception.getMessage().contains("Please provide a more precise station name"));
        assertTrue(exception.getMessage().contains("destination"));
        assertTrue(exception.getMessage().contains("BARCELONA (TODAS)"));
        assertTrue(exception.getMessage().contains("BARCELONA-SANTS"));
        verify(getStationsUseCase, times(1)).searchStations(origin);
        verify(getStationsUseCase, times(1)).searchStations(destination);
        verify(trainScraperPort, never()).scrapeTrains(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should proceed when exactly one station matches for both origin and destination")
    void shouldProceedWhenExactlyOneStationMatches() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        Station originStation = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "MADRID (TODAS)");
        Station destinationStation = new Station("BARCE", "0071", 3, null,
                "BARCELONA (TODAS)", null, "0071,BARCE,null", "BARCELONA (TODAS)");

        when(getStationsUseCase.searchStations(origin)).thenReturn(List.of(originStation));
        when(getStationsUseCase.searchStations(destination)).thenReturn(List.of(destinationStation));

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        String realOriginName = originStation.getStationNamePlano();
        String realDestinationName = destinationStation.getStationNamePlano();
        String originDesgEstacion = originStation.getStationName();
        String destinationDesgEstacion = destinationStation.getStationName();
        String originClave = originStation.getKey();
        String destinationClave = destinationStation.getKey();
        when(trainScraperPort.scrapeTrains(realOriginName, realDestinationName,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                FORMATTED_DATE_OUT, null, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals(1, result.getTrainsOut().size());
        assertEquals(realOriginName, result.getOrigin());
        assertEquals(realDestinationName, result.getDestination());
        verify(getStationsUseCase, times(1)).searchStations(origin);
        verify(getStationsUseCase, times(1)).searchStations(destination);
        verify(trainScraperPort, times(1)).scrapeTrains(realOriginName, realDestinationName,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                FORMATTED_DATE_OUT, null, adults);
    }

    @Test
    @DisplayName("Should handle station with null stationNamePlano (fallback to stationName)")
    void shouldHandleStationWithNullStationNamePlano() {
        String origin = "MADRID";
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        // Station with null stationNamePlano but valid stationName
        Station originStation = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", null); // stationNamePlano is null
        Station destinationStation = new Station("BARCE", "0071", 3, null,
                "BARCELONA (TODAS)", null, "0071,BARCE,null", "BARCELONA (TODAS)");

        when(getStationsUseCase.searchStations(origin)).thenReturn(List.of(originStation));
        when(getStationsUseCase.searchStations(destination)).thenReturn(List.of(destinationStation));

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        // Should use stationName as fallback for stationNamePlano
        String expectedOriginName = originStation.getStationName(); // "MADRID (TODAS)"
        String expectedOriginDesgEstacion = originStation.getStationName(); // "MADRID (TODAS)"
        String expectedOriginClave = originStation.getKey(); // "0071,MADRI,null"

        when(trainScraperPort.scrapeTrains(expectedOriginName, destinationStation.getStationNamePlano(),
                expectedOriginDesgEstacion, destinationStation.getStationName(),
                expectedOriginClave, destinationStation.getKey(),
                FORMATTED_DATE_OUT, null, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals(expectedOriginName, result.getOrigin());
        verify(getStationsUseCase, times(1)).searchStations(origin);
        verify(getStationsUseCase, times(1)).searchStations(destination);
    }

    @Test
    @DisplayName("Should handle station with blank stationNamePlano (fallback to stationName)")
    void shouldHandleStationWithBlankStationNamePlano() {
        String origin = "MADRID";
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        // Station with blank stationNamePlano but valid stationName
        Station originStation = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "   "); // stationNamePlano is blank
        Station destinationStation = new Station("BARCE", "0071", 3, null,
                "BARCELONA (TODAS)", null, "0071,BARCE,null", "BARCELONA (TODAS)");

        when(getStationsUseCase.searchStations(origin)).thenReturn(List.of(originStation));
        when(getStationsUseCase.searchStations(destination)).thenReturn(List.of(destinationStation));

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        // Should use stationName as fallback for stationNamePlano
        String expectedOriginName = originStation.getStationName(); // "MADRID (TODAS)"
        String expectedOriginDesgEstacion = originStation.getStationName(); // "MADRID (TODAS)"
        String expectedOriginClave = originStation.getKey(); // "0071,MADRI,null"

        when(trainScraperPort.scrapeTrains(expectedOriginName, destinationStation.getStationNamePlano(),
                expectedOriginDesgEstacion, destinationStation.getStationName(),
                expectedOriginClave, destinationStation.getKey(),
                FORMATTED_DATE_OUT, null, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals(expectedOriginName, result.getOrigin());
        verify(getStationsUseCase, times(1)).searchStations(origin);
        verify(getStationsUseCase, times(1)).searchStations(destination);
    }

    @Test
    @DisplayName("Should handle station with null stationNamePlano and null stationName (fallback to search text)")
    void shouldHandleStationWithNullStationNamePlanoAndNullStationName() {
        String origin = "MADRID";
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        // Station with both stationNamePlano and stationName null
        Station originStation = new Station("MADRI", "0071", 1, null,
                null, null, "0071,MADRI,null", null); // Both null
        Station destinationStation = new Station("BARCE", "0071", 3, null,
                "BARCELONA (TODAS)", null, "0071,BARCE,null", "BARCELONA (TODAS)");

        when(getStationsUseCase.searchStations(origin)).thenReturn(List.of(originStation));
        when(getStationsUseCase.searchStations(destination)).thenReturn(List.of(destinationStation));

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        // Should use search text (origin) as final fallback
        String expectedOriginName = origin; // "MADRID"
        String expectedOriginDesgEstacion = origin; // "MADRID" (fallback from realStationName)
        String expectedOriginClave = originStation.getKey(); // "0071,MADRI,null"

        when(trainScraperPort.scrapeTrains(expectedOriginName, destinationStation.getStationNamePlano(),
                expectedOriginDesgEstacion, destinationStation.getStationName(),
                expectedOriginClave, destinationStation.getKey(),
                FORMATTED_DATE_OUT, null, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals(expectedOriginName, result.getOrigin());
        verify(getStationsUseCase, times(1)).searchStations(origin);
        verify(getStationsUseCase, times(1)).searchStations(destination);
    }

    @Test
    @DisplayName("Should handle station with null stationName (use realStationName as desgEstacion)")
    void shouldHandleStationWithNullStationName() {
        String origin = "MADRID";
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        // Station with null stationName but valid stationNamePlano
        Station originStation = new Station("MADRI", "0071", 1, null,
                null, null, "0071,MADRI,null", "MADRID (TODAS)"); // stationName is null, stationNamePlano is valid
        Station destinationStation = new Station("BARCE", "0071", 3, null,
                "BARCELONA (TODAS)", null, "0071,BARCE,null", "BARCELONA (TODAS)");

        when(getStationsUseCase.searchStations(origin)).thenReturn(List.of(originStation));
        when(getStationsUseCase.searchStations(destination)).thenReturn(List.of(destinationStation));

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        // Should use stationNamePlano as realStationName, and realStationName as desgEstacion (since stationName is null)
        String expectedOriginName = originStation.getStationNamePlano(); // "MADRID (TODAS)"
        String expectedOriginDesgEstacion = expectedOriginName; // Same as realStationName (fallback)
        String expectedOriginClave = originStation.getKey(); // "0071,MADRI,null"

        when(trainScraperPort.scrapeTrains(expectedOriginName, destinationStation.getStationNamePlano(),
                expectedOriginDesgEstacion, destinationStation.getStationName(),
                expectedOriginClave, destinationStation.getKey(),
                FORMATTED_DATE_OUT, null, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals(expectedOriginName, result.getOrigin());
        verify(getStationsUseCase, times(1)).searchStations(origin);
        verify(getStationsUseCase, times(1)).searchStations(destination);
    }

    @Test
    @DisplayName("Should handle station with blank stationName (use realStationName as desgEstacion)")
    void shouldHandleStationWithBlankStationName() {
        String origin = "MADRID";
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        // Station with blank stationName but valid stationNamePlano
        Station originStation = new Station("MADRI", "0071", 1, null,
                "   ", null, "0071,MADRI,null", "MADRID (TODAS)"); // stationName is blank, stationNamePlano is valid
        Station destinationStation = new Station("BARCE", "0071", 3, null,
                "BARCELONA (TODAS)", null, "0071,BARCE,null", "BARCELONA (TODAS)");

        when(getStationsUseCase.searchStations(origin)).thenReturn(List.of(originStation));
        when(getStationsUseCase.searchStations(destination)).thenReturn(List.of(destinationStation));

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        // Should use stationNamePlano as realStationName, and realStationName as desgEstacion (since stationName is blank)
        String expectedOriginName = originStation.getStationNamePlano(); // "MADRID (TODAS)"
        String expectedOriginDesgEstacion = expectedOriginName; // Same as realStationName (fallback)
        String expectedOriginClave = originStation.getKey(); // "0071,MADRI,null"

        when(trainScraperPort.scrapeTrains(expectedOriginName, destinationStation.getStationNamePlano(),
                expectedOriginDesgEstacion, destinationStation.getStationName(),
                expectedOriginClave, destinationStation.getKey(),
                FORMATTED_DATE_OUT, null, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals(expectedOriginName, result.getOrigin());
        verify(getStationsUseCase, times(1)).searchStations(origin);
        verify(getStationsUseCase, times(1)).searchStations(destination);
    }

    @Test
    @DisplayName("Should handle station with null key (use empty string)")
    void shouldHandleStationWithNullKey() {
        String origin = "MADRID";
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        // Station with null key
        Station originStation = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, null, "MADRID (TODAS)"); // key is null
        Station destinationStation = new Station("BARCE", "0071", 3, null,
                "BARCELONA (TODAS)", null, "0071,BARCE,null", "BARCELONA (TODAS)");

        when(getStationsUseCase.searchStations(origin)).thenReturn(List.of(originStation));
        when(getStationsUseCase.searchStations(destination)).thenReturn(List.of(destinationStation));

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        // Should use empty string for clave when key is null
        String expectedOriginName = originStation.getStationNamePlano(); // "MADRID (TODAS)"
        String expectedOriginDesgEstacion = originStation.getStationName(); // "MADRID (TODAS)"
        String expectedOriginClave = ""; // Empty string when key is null

        when(trainScraperPort.scrapeTrains(expectedOriginName, destinationStation.getStationNamePlano(),
                expectedOriginDesgEstacion, destinationStation.getStationName(),
                expectedOriginClave, destinationStation.getKey(),
                FORMATTED_DATE_OUT, null, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals(expectedOriginName, result.getOrigin());
        verify(getStationsUseCase, times(1)).searchStations(origin);
        verify(getStationsUseCase, times(1)).searchStations(destination);
    }

    @Test
    @DisplayName("Should handle station with blank key (use empty string)")
    void shouldHandleStationWithBlankKey() {
        String origin = "MADRID";
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        String adults = "2";

        // Station with blank key
        Station originStation = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "   ", "MADRID (TODAS)"); // key is blank
        Station destinationStation = new Station("BARCE", "0071", 3, null,
                "BARCELONA (TODAS)", null, "0071,BARCE,null", "BARCELONA (TODAS)");

        when(getStationsUseCase.searchStations(origin)).thenReturn(List.of(originStation));
        when(getStationsUseCase.searchStations(destination)).thenReturn(List.of(destinationStation));

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        // Should use empty string for clave when key is blank
        String expectedOriginName = originStation.getStationNamePlano(); // "MADRID (TODAS)"
        String expectedOriginDesgEstacion = originStation.getStationName(); // "MADRID (TODAS)"
        String expectedOriginClave = ""; // Empty string when key is blank

        when(trainScraperPort.scrapeTrains(expectedOriginName, destinationStation.getStationNamePlano(),
                expectedOriginDesgEstacion, destinationStation.getStationName(),
                expectedOriginClave, destinationStation.getKey(),
                FORMATTED_DATE_OUT, null, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals(expectedOriginName, result.getOrigin());
        verify(getStationsUseCase, times(1)).searchStations(origin);
        verify(getStationsUseCase, times(1)).searchStations(destination);
    }

    @Test
    @DisplayName("Should handle dateReturn with blank value (treat as null)")
    void shouldHandleDateReturnWithBlankValue() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = "   "; // Blank value - should be treated as null (optional)
        String adults = "2";

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        when(trainScraperPort.scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, null, adults)) // dateReturn should be null when blank
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertNull(result.getDateReturn()); // Should be null when blank
        assertEquals(1, result.getTrainsOut().size());
        verify(trainScraperPort, times(1)).scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, null, adults);
    }

    @Test
    @DisplayName("Should re-throw TrainUnavailabilityException when scraper throws TrainUnavailabilityException")
    void shouldReThrowTrainUnavailabilityExceptionWhenScraperThrowsTrainUnavailabilityException() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = REAL_DATE_RETURN;
        String adults = "2";
        String direction = "outbound";
        String detailMessage = "No hay trenes disponibles para la fecha seleccionada";

        when(trainScraperPort.scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, FORMATTED_DATE_RETURN, adults))
                .thenThrow(new TrainUnavailabilityException(direction, detailMessage));

        TrainUnavailabilityException exception = assertThrows(TrainUnavailabilityException.class, () -> {
            service.searchTrains(origin, destination, dateOut, dateReturn, adults);
        });

        assertEquals(direction, exception.getDirection());
        assertEquals(detailMessage, exception.getDetailMessage());
        assertTrue(exception.getMessage().contains(direction));
        assertTrue(exception.getMessage().contains(detailMessage));
        assertNull(exception.getCause());

        verify(trainScraperPort, times(1)).scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, FORMATTED_DATE_RETURN, adults);
    }

    @Test
    @DisplayName("Should re-throw TrainUnavailabilityException for return trains")
    void shouldReThrowTrainUnavailabilityExceptionForReturnTrains() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = REAL_DATE_RETURN;
        String adults = "1";
        String direction = "return";
        String detailMessage = "No hay billetes de vuelta disponibles";

        when(trainScraperPort.scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, FORMATTED_DATE_RETURN, adults))
                .thenThrow(new TrainUnavailabilityException(direction, detailMessage));

        TrainUnavailabilityException exception = assertThrows(TrainUnavailabilityException.class, () -> {
            service.searchTrains(origin, destination, dateOut, dateReturn, adults);
        });

        assertEquals(direction, exception.getDirection());
        assertEquals(detailMessage, exception.getDetailMessage());
        assertTrue(exception.getMessage().contains("Error searching trains for return"));
        
        verify(trainScraperPort, times(1)).scrapeTrains(REAL_ORIGIN_STATION_NAME, REAL_DESTINATION_STATION_NAME,
                REAL_ORIGIN_DESG_ESTACION, REAL_DESTINATION_DESG_ESTACION,
                REAL_ORIGIN_CLAVE, REAL_DESTINATION_CLAVE,
                FORMATTED_DATE_OUT, FORMATTED_DATE_RETURN, adults);
    }

    private Train createTrain(String trainId, String serviceType, String departureTime,
                              String arrivalTime, String duration, double priceFrom) {
        Train train = new Train();
        train.setTrainId(trainId);
        train.setServiceType(serviceType);
        train.setDepartureTime(departureTime);
        train.setArrivalTime(arrivalTime);
        train.setDuration(duration);
        train.setPriceFrom(priceFrom);
        return train;
    }
}

