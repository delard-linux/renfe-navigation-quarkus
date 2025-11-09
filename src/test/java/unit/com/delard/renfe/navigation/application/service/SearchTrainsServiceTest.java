package com.delard.renfe.navigation.application.service;

import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.domain.model.TrainsResponse;
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
import static org.mockito.Mockito.lenient;

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
    private static final String REAL_DATE_OUT = "2026-01-16";  // 16/01/2026
    private static final String REAL_DATE_RETURN = "2026-01-18";  // 18/01/2026

    @Mock
    private TrainScraperPort trainScraperPort;

    @InjectMocks
    private SearchTrainsService service;

    @BeforeEach
    void setUp() {
        service = new SearchTrainsService();
        // Use reflection to inject the mock
        try {
            java.lang.reflect.Field field = SearchTrainsService.class.getDeclaredField("trainScraperPort");
            field.setAccessible(true);
            field.set(service, trainScraperPort);
        } catch (Exception e) {
            fail("Failed to inject mock: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should return complete response with outbound and return trains when both are available")
    void shouldReturnCompleteResponseWithOutboundAndReturnTrains() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = REAL_DATE_RETURN;
        int adults = 1;

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        Train trainOut2 = createTrain("T456", "ALVIA", "12:00", "14:00", "2h", 30.0);
        Train trainRet1 = createTrain("T789", "AVE", "16:00", "18:00", "2h", 25.0);

        List<Train> trainsOut = Arrays.asList(trainOut1, trainOut2);
        List<Train> trainsReturn = Arrays.asList(trainRet1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut, trainsReturn);

        when(trainScraperPort.scrapeTrains(origin, destination, dateOut, dateReturn, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals(origin, result.getOrigin());
        assertEquals(destination, result.getDestination());
        assertEquals(dateOut, result.getDateOut());
        assertEquals(dateReturn, result.getDateReturn());
        assertEquals(adults, result.getAdults());
        assertNotNull(result.getTrainsOut());
        assertEquals(2, result.getTrainsOut().size());
        assertNotNull(result.getTrainsReturn());
        assertEquals(1, result.getTrainsReturn().size());

        verify(trainScraperPort, times(1)).scrapeTrains(origin, destination, dateOut, dateReturn, adults);
    }

    @Test
    @DisplayName("Should return response with only outbound trains when return date is null")
    void shouldReturnResponseWithOnlyOutboundTrainsWhenReturnDateIsNull() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        int adults = 1;

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        when(trainScraperPort.scrapeTrains(origin, destination, dateOut, dateReturn, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals(origin, result.getOrigin());
        assertEquals(destination, result.getDestination());
        assertEquals(dateOut, result.getDateOut());
        assertEquals(dateReturn, result.getDateReturn());
        assertEquals(adults, result.getAdults());
        assertNotNull(result.getTrainsOut());
        assertEquals(1, result.getTrainsOut().size());
        assertNull(result.getTrainsReturn());

        verify(trainScraperPort, times(1)).scrapeTrains(origin, destination, dateOut, dateReturn, adults);
    }

    @Test
    @DisplayName("Should return empty return trains list when scraper returns empty list for return trains")
    void shouldReturnEmptyReturnTrainsListWhenScraperReturnsEmptyList() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = REAL_DATE_RETURN;
        int adults = 1;

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<Train> trainsReturn = new ArrayList<>();
        List<List<Train>> scraperResult = Arrays.asList(trainsOut, trainsReturn);

        when(trainScraperPort.scrapeTrains(origin, destination, dateOut, dateReturn, adults))
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
        int adults = 1;

        List<Train> trainsOut = new ArrayList<>();
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        when(trainScraperPort.scrapeTrains(origin, destination, dateOut, dateReturn, adults))
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
        int adults = 1;
        String errorMessage = "Scraping failed";

        when(trainScraperPort.scrapeTrains(origin, destination, dateOut, dateReturn, adults))
                .thenThrow(new RuntimeException(errorMessage));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.searchTrains(origin, destination, dateOut, dateReturn, adults);
        });

        assertTrue(exception.getMessage().contains("Error searching trains"));
        assertTrue(exception.getMessage().contains(errorMessage));
        assertNotNull(exception.getCause());

        verify(trainScraperPort, times(1)).scrapeTrains(origin, destination, dateOut, dateReturn, adults);
    }

    @Test
    @DisplayName("Should handle different number of adults correctly (1, 3, 5 adults)")
    void shouldHandleDifferentNumberOfAdultsCorrectly() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        when(trainScraperPort.scrapeTrains(eq(origin), eq(destination), eq(dateOut), eq(dateReturn), anyInt()))
                .thenReturn(scraperResult);

        TrainsResponse result1 = service.searchTrains(origin, destination, dateOut, dateReturn, 1);
        TrainsResponse result2 = service.searchTrains(origin, destination, dateOut, dateReturn, 3);
        TrainsResponse result3 = service.searchTrains(origin, destination, dateOut, dateReturn, 5);

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(1, result1.getAdults());
        assertEquals(3, result2.getAdults());
        assertEquals(5, result3.getAdults());

        verify(trainScraperPort, times(3)).scrapeTrains(eq(origin), eq(destination), eq(dateOut), eq(dateReturn), anyInt());
    }

    @Test
    @DisplayName("Should handle null input values gracefully")
    void shouldHandleNullInputValuesGracefully() {
        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        lenient().when(trainScraperPort.scrapeTrains(any(), any(), any(), any(), anyInt()))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(null, null, null, null, 0);

        assertNotNull(result);
        assertNull(result.getOrigin());
        assertNull(result.getDestination());
        assertNull(result.getDateOut());
        assertNull(result.getDateReturn());
        assertEquals(0, result.getAdults());
    }

    @Test
    @DisplayName("Should return null for return trains when scraper returns single list (only outbound)")
    void shouldReturnNullForReturnTrainsWhenScraperReturnsSingleList() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        int adults = 1;

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        // Result with only one element (no return trains)
        List<List<Train>> scraperResult = Arrays.asList(trainsOut);

        when(trainScraperPort.scrapeTrains(origin, destination, dateOut, dateReturn, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertEquals(1, result.getTrainsOut().size());
        // When result.size() == 1, trainsReturn should be null
        assertNull(result.getTrainsReturn());
    }

    @Test
    @DisplayName("Should handle null trainsOut list correctly")
    void shouldHandleNullTrainsOutListCorrectly() {
        String origin = REAL_ORIGIN;
        String destination = REAL_DESTINATION;
        String dateOut = REAL_DATE_OUT;
        String dateReturn = null;
        int adults = 1;

        // Simulate scraper returning a list with null first element
        List<List<Train>> scraperResult = new ArrayList<>();
        scraperResult.add(null);

        when(trainScraperPort.scrapeTrains(origin, destination, dateOut, dateReturn, adults))
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
        int adults = 1;

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<List<Train>> scraperResult = Arrays.asList(trainsOut, null);

        when(trainScraperPort.scrapeTrains(origin, destination, dateOut, dateReturn, adults))
                .thenReturn(scraperResult);

        TrainsResponse result = service.searchTrains(origin, destination, dateOut, dateReturn, adults);

        assertNotNull(result);
        assertNotNull(result.getTrainsOut());
        assertEquals(1, result.getTrainsOut().size());
        assertNull(result.getTrainsReturn());
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

