package com.delard.renfe.navigation.application.service;

import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.domain.model.TrainsResponse;
import com.delard.renfe.navigation.domain.port.output.TrainScraperPort;
import org.junit.jupiter.api.BeforeEach;
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
 */
@ExtendWith(MockitoExtension.class)
class SearchTrainsServiceTest {

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
    void testSearchTrainsSuccessWithReturnTrains() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = "2025-12-05";
        int adults = 2;

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
    void testSearchTrainsSuccessWithoutReturnTrains() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
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
    void testSearchTrainsSuccessWithEmptyReturnList() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = "2025-12-05";
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
    void testSearchTrainsSuccessWithEmptyOutboundList() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
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
    void testSearchTrainsThrowsException() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
        String dateReturn = "2025-12-05";
        int adults = 2;
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
    void testSearchTrainsWithDifferentAdults() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
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
    void testSearchTrainsWithNullValues() {
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
    void testSearchTrainsWithSingleElementResult() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String dateOut = "2025-12-01";
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

