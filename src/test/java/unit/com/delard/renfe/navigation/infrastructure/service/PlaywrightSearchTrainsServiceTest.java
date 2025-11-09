package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.domain.model.FareOption;
import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.infrastructure.config.PlaywrightConfig;
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

/**
 * Unit tests for PlaywrightSearchTrainsService
 */
@ExtendWith(MockitoExtension.class)
class PlaywrightSearchTrainsServiceTest {

    @Mock
    private PlaywrightConfig config;

    @Mock
    private RenfeCommonService renfeCommonService;

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
    void testSearchTrainsResultToStringWithNullLists() {
        // Arrange
        PlaywrightSearchTrainsService.SearchTrainsResult result =
                new PlaywrightSearchTrainsService.SearchTrainsResult(null, null);

        // Act
        String toString = result.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("outboundTrains="));
        assertTrue(toString.contains("returnTrains="));
        assertTrue(toString.contains("[]"));
    }

    @Test
    void testSearchTrainsResultToStringWithEmptyLists() {
        // Arrange
        PlaywrightSearchTrainsService.SearchTrainsResult result =
                new PlaywrightSearchTrainsService.SearchTrainsResult(new ArrayList<>(), new ArrayList<>());

        // Act
        String toString = result.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("outboundTrains="));
        assertTrue(toString.contains("returnTrains="));
        assertTrue(toString.contains("[]"));
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
    void testSearchTrainsResultToStringWithTrainWithoutServiceType() {
        // Arrange
        Train train = new Train();
        train.setDepartureTime("08:00");
        train.setArrivalTime("12:30");
        train.setPriceFrom(45.50);
        List<Train> trains = Arrays.asList(train);
        PlaywrightSearchTrainsService.SearchTrainsResult result =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains, null);

        // Act
        String toString = result.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("(no-type)"));
        assertTrue(toString.contains("08:00-12:30"));
    }

    @Test
    void testSearchTrainsResultToStringWithTrainWithoutTimes() {
        // Arrange
        Train train = new Train();
        train.setServiceType("AVE");
        train.setPriceFrom(45.50);
        List<Train> trains = Arrays.asList(train);
        PlaywrightSearchTrainsService.SearchTrainsResult result =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains, null);

        // Act
        String toString = result.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("AVE"));
        assertTrue(toString.contains("--"));
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
    void testSearchTrainsResultToStringWithTrainWithEmptyFares() {
        // Arrange
        Train train = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        train.setFares(new ArrayList<>());
        List<Train> trains = Arrays.asList(train);
        PlaywrightSearchTrainsService.SearchTrainsResult result =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains, null);

        // Act
        String toString = result.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("AVE"));
        assertTrue(toString.contains("45.50€")); // Should fallback to priceFrom
    }

    @Test
    void testSearchTrainsResultToStringWithTrainWithNullFares() {
        // Arrange
        Train train = new Train("TRAIN123", "AVE", "08:00", "12:30", "4h 30m", 45.50);
        train.setFares(null);
        List<Train> trains = Arrays.asList(train);
        PlaywrightSearchTrainsService.SearchTrainsResult result =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains, null);

        // Act
        String toString = result.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("AVE"));
        assertTrue(toString.contains("45.50€")); // Should fallback to priceFrom
    }

    @Test
    void testSearchTrainsResultToStringWithTrainWithBlankServiceType() {
        // Arrange
        Train train = new Train();
        train.setServiceType("   ");
        train.setDepartureTime("08:00");
        train.setArrivalTime("12:30");
        train.setPriceFrom(45.50);
        List<Train> trains = Arrays.asList(train);
        PlaywrightSearchTrainsService.SearchTrainsResult result =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains, null);

        // Act
        String toString = result.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("(no-type)"));
    }

    @Test
    void testSearchTrainsResultToStringWithTrainWithBlankTimes() {
        // Arrange
        Train train = new Train();
        train.setServiceType("AVE");
        train.setDepartureTime("   ");
        train.setArrivalTime("   ");
        train.setPriceFrom(45.50);
        List<Train> trains = Arrays.asList(train);
        PlaywrightSearchTrainsService.SearchTrainsResult result =
                new PlaywrightSearchTrainsService.SearchTrainsResult(trains, null);

        // Act
        String toString = result.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("AVE"));
        assertTrue(toString.contains("--"));
    }
}

