package com.delard.renfe.navigation.infrastructure.adapter.output;

import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.infrastructure.service.PlaywrightSearchTrainsService;
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

/**
 * Unit tests for TrainScraperAdapter
 */
@ExtendWith(MockitoExtension.class)
class TrainScraperAdapterTest {

    @Mock
    private PlaywrightSearchTrainsService playwrightSearchTrainsService;

    @InjectMocks
    private TrainScraperAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TrainScraperAdapter();
        // Use reflection to inject the mock
        try {
            java.lang.reflect.Field field = TrainScraperAdapter.class.getDeclaredField("playwrightSearchTrainsService");
            field.setAccessible(true);
            field.set(adapter, playwrightSearchTrainsService);
        } catch (Exception e) {
            fail("Failed to inject mock: " + e.getMessage());
        }
    }

    @Test
    void testScrapeTrainsWithReturnTrains() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String originDesgEstacion = "OURENSE";
        String destinationDesgEstacion = "MADRID";
        String originClave = "0071,OURENSE,null";
        String destinationClave = "0071,MADRID,null";
        String dateOut = "01/12/2025";
        String dateReturn = "05/12/2025";
        String adults = "2";

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        Train trainOut2 = createTrain("T456", "ALVIA", "12:00", "14:00", "2h", 30.0);
        Train trainRet1 = createTrain("T789", "AVE", "16:00", "18:00", "2h", 25.0);

        List<Train> trainsOut = Arrays.asList(trainOut1, trainOut2);
        List<Train> trainsReturn = Arrays.asList(trainRet1);

        PlaywrightSearchTrainsService.SearchTrainsResult result =
            new PlaywrightSearchTrainsService.SearchTrainsResult(trainsOut, trainsReturn);

        when(playwrightSearchTrainsService.searchTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, adults))
                .thenReturn(result);

        List<List<Train>> scraperResult = adapter.scrapeTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, adults);

        assertNotNull(scraperResult);
        assertEquals(2, scraperResult.size());
        assertEquals(2, scraperResult.get(0).size());
        assertEquals(1, scraperResult.get(1).size());
        assertEquals(trainsOut, scraperResult.get(0));
        assertEquals(trainsReturn, scraperResult.get(1));

        verify(playwrightSearchTrainsService, times(1)).searchTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, adults);
    }

    @Test
    void testScrapeTrainsWithoutReturnTrains() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String originDesgEstacion = "OURENSE";
        String destinationDesgEstacion = "MADRID";
        String originClave = "0071,OURENSE,null";
        String destinationClave = "0071,MADRID,null";
        String dateOut = "01/12/2025";
        String dateReturn = null;
        String adults = "1";

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);

        PlaywrightSearchTrainsService.SearchTrainsResult result =
            new PlaywrightSearchTrainsService.SearchTrainsResult(trainsOut, null);

        when(playwrightSearchTrainsService.searchTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, adults))
                .thenReturn(result);

        List<List<Train>> scraperResult = adapter.scrapeTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, adults);

        assertNotNull(scraperResult);
        assertEquals(1, scraperResult.size());
        assertEquals(1, scraperResult.get(0).size());
        assertEquals(trainsOut, scraperResult.get(0));

        verify(playwrightSearchTrainsService, times(1)).searchTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, adults);
    }

    @Test
    void testScrapeTrainsWithNullOutboundTrains() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String originDesgEstacion = "OURENSE";
        String destinationDesgEstacion = "MADRID";
        String originClave = "0071,OURENSE,null";
        String destinationClave = "0071,MADRID,null";
        String dateOut = "01/12/2025";
        String dateReturn = null;
        String adults = "1";

        PlaywrightSearchTrainsService.SearchTrainsResult result =
            new PlaywrightSearchTrainsService.SearchTrainsResult(null, null);

        when(playwrightSearchTrainsService.searchTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, adults))
                .thenReturn(result);

        List<List<Train>> scraperResult = adapter.scrapeTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, adults);

        assertNotNull(scraperResult);
        assertEquals(1, scraperResult.size());
        assertNotNull(scraperResult.get(0));
        assertTrue(scraperResult.get(0).isEmpty());
    }

    @Test
    void testScrapeTrainsWithEmptyOutboundTrains() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String originDesgEstacion = "OURENSE";
        String destinationDesgEstacion = "MADRID";
        String originClave = "0071,OURENSE,null";
        String destinationClave = "0071,MADRID,null";
        String dateOut = "01/12/2025";
        String dateReturn = null;
        String adults = "1";

        List<Train> emptyTrainsOut = new ArrayList<>();
        PlaywrightSearchTrainsService.SearchTrainsResult result =
            new PlaywrightSearchTrainsService.SearchTrainsResult(emptyTrainsOut, null);

        when(playwrightSearchTrainsService.searchTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, adults))
                .thenReturn(result);

        List<List<Train>> scraperResult = adapter.scrapeTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, adults);

        assertNotNull(scraperResult);
        assertEquals(1, scraperResult.size());
        assertTrue(scraperResult.get(0).isEmpty());
    }

    @Test
    void testScrapeTrainsWithEmptyReturnTrains() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String originDesgEstacion = "OURENSE";
        String destinationDesgEstacion = "MADRID";
        String originClave = "0071,OURENSE,null";
        String destinationClave = "0071,MADRID,null";
        String dateOut = "01/12/2025";
        String dateReturn = "05/12/2025";
        String adults = "1";

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);
        List<Train> emptyTrainsReturn = new ArrayList<>();

        PlaywrightSearchTrainsService.SearchTrainsResult result =
            new PlaywrightSearchTrainsService.SearchTrainsResult(trainsOut, emptyTrainsReturn);

        when(playwrightSearchTrainsService.searchTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, adults))
                .thenReturn(result);

        List<List<Train>> scraperResult = adapter.scrapeTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, adults);

        assertNotNull(scraperResult);
        assertEquals(2, scraperResult.size());
        assertEquals(1, scraperResult.get(0).size());
        assertEquals(0, scraperResult.get(1).size());
    }

    @Test
    void testScrapeTrainsThrowsException() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String originDesgEstacion = "OURENSE";
        String destinationDesgEstacion = "MADRID";
        String originClave = "0071,OURENSE,null";
        String destinationClave = "0071,MADRID,null";
        String dateOut = "01/12/2025";
        String dateReturn = "05/12/2025";
        String adults = "2";
        String errorMessage = "Scraping failed";

        when(playwrightSearchTrainsService.searchTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, adults))
                .thenThrow(new RuntimeException(errorMessage));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adapter.scrapeTrains(origin, destination,
                    originDesgEstacion, destinationDesgEstacion,
                    originClave, destinationClave,
                    dateOut, dateReturn, adults);
        });

        assertTrue(exception.getMessage().contains("Error scraping trains"));
        assertTrue(exception.getMessage().contains(errorMessage));
        assertNotNull(exception.getCause());

        verify(playwrightSearchTrainsService, times(1)).searchTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, adults);
    }

    @Test
    void testScrapeTrainsWithDifferentAdults() {
        String origin = "OURENSE";
        String destination = "MADRID";
        String originDesgEstacion = "OURENSE";
        String destinationDesgEstacion = "MADRID";
        String originClave = "0071,OURENSE,null";
        String destinationClave = "0071,MADRID,null";
        String dateOut = "01/12/2025";
        String dateReturn = null;

        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);

        PlaywrightSearchTrainsService.SearchTrainsResult result =
            new PlaywrightSearchTrainsService.SearchTrainsResult(trainsOut, null);

        when(playwrightSearchTrainsService.searchTrains(eq(origin), eq(destination),
                eq(originDesgEstacion), eq(destinationDesgEstacion),
                eq(originClave), eq(destinationClave),
                eq(dateOut), eq(dateReturn), anyString()))
                .thenReturn(result);

        List<List<Train>> result1 = adapter.scrapeTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, "1");
        List<List<Train>> result2 = adapter.scrapeTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, "3");
        List<List<Train>> result3 = adapter.scrapeTrains(origin, destination,
                originDesgEstacion, destinationDesgEstacion,
                originClave, destinationClave,
                dateOut, dateReturn, "5");

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
        assertEquals(1, result3.size());

        verify(playwrightSearchTrainsService, times(3)).searchTrains(eq(origin), eq(destination),
                eq(originDesgEstacion), eq(destinationDesgEstacion),
                eq(originClave), eq(destinationClave),
                eq(dateOut), eq(dateReturn), anyString());
    }

    @Test
    void testScrapeTrainsWithNullValues() {
        Train trainOut1 = createTrain("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        List<Train> trainsOut = Arrays.asList(trainOut1);

        PlaywrightSearchTrainsService.SearchTrainsResult result =
            new PlaywrightSearchTrainsService.SearchTrainsResult(trainsOut, null);

        lenient().when(playwrightSearchTrainsService.searchTrains(any(), any(), any(), any(), any(), any(), any(), any(), anyString()))
                .thenReturn(result);

        List<List<Train>> scraperResult = adapter.scrapeTrains(null, null, null, null, null, null, null, null, "0");

        assertNotNull(scraperResult);
        assertEquals(1, scraperResult.size());
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

