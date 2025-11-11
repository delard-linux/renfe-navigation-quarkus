package com.delard.renfe.navigation.application.service;

import com.delard.renfe.navigation.domain.model.Station;
import com.delard.renfe.navigation.domain.port.output.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GetStationsService
 */
@ExtendWith(MockitoExtension.class)
class GetStationsServiceTest {

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private GetStationsService getStationsService;

    private List<Station> mockStations;

    @BeforeEach
    void setUp() {
        Station station1 = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "MADRID (TODAS)");
        Station station2 = new Station("BARCE", "0071", 3, null,
                "BARCELONA (TODAS)", null, "0071,BARCE,null", "BARCELONA (TODAS)");
        
        mockStations = Arrays.asList(station1, station2);
    }

    @Test
    void testGetAllStationsSuccess() {
        when(stationRepository.loadAllStations()).thenReturn(mockStations);

        List<Station> result = getStationsService.getAllStations();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("MADRI", result.get(0).getStationCode());
        assertEquals("BARCE", result.get(1).getStationCode());
        
        verify(stationRepository, times(1)).loadAllStations();
    }

    @Test
    void testGetAllStationsEmptyList() {
        when(stationRepository.loadAllStations()).thenReturn(List.of());

        List<Station> result = getStationsService.getAllStations();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(stationRepository, times(1)).loadAllStations();
    }

    @Test
    void testGetAllStationsThrowsException() {
        when(stationRepository.loadAllStations()).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            getStationsService.getAllStations();
        });

        assertTrue(exception.getMessage().contains("Error loading stations"));
        assertTrue(exception.getMessage().contains("Database error"));
        
        verify(stationRepository, times(1)).loadAllStations();
    }

    @Test
    void testGetAllStationsWithNullFromRepository() {
        when(stationRepository.loadAllStations()).thenReturn(null);

        List<Station> result = getStationsService.getAllStations();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(stationRepository, times(1)).loadAllStations();
    }

    @Test
    void testSearchStationsSuccess() {
        Station station1 = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "MADRID (TODAS)");
        Station station2 = new Station("60000", "0071", 2, null,
                "MADRID-PUERTA DE ATOCHA-ALMUDENA GRANDES", "00600", "0071,60000,00600",
                "MADRID-PUERTA DE ATOCHA-ALMUDENA GRANDES");
        List<Station> matchingStations = Arrays.asList(station1, station2);

        when(stationRepository.searchStations("MADRID")).thenReturn(matchingStations);

        List<Station> result = getStationsService.searchStations("MADRID");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("MADRI", result.get(0).getStationCode());
        assertEquals("60000", result.get(1).getStationCode());
        
        verify(stationRepository, times(1)).searchStations("MADRID");
    }

    @Test
    void testSearchStationsEmptyResult() {
        when(stationRepository.searchStations("NONEXISTENT")).thenReturn(List.of());

        List<Station> result = getStationsService.searchStations("NONEXISTENT");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(stationRepository, times(1)).searchStations("NONEXISTENT");
    }

    @Test
    void testSearchStationsWithNullText() {
        List<Station> result = getStationsService.searchStations(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(stationRepository, never()).searchStations(anyString());
    }

    @Test
    void testSearchStationsWithBlankText() {
        List<Station> result = getStationsService.searchStations("   ");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(stationRepository, never()).searchStations(anyString());
    }

    @Test
    void testSearchStationsWithEmptyText() {
        List<Station> result = getStationsService.searchStations("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(stationRepository, never()).searchStations(anyString());
    }

    @Test
    void testSearchStationsThrowsException() {
        when(stationRepository.searchStations("MADRID")).thenThrow(new RuntimeException("Search error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            getStationsService.searchStations("MADRID");
        });

        assertTrue(exception.getMessage().contains("Error searching stations"));
        assertTrue(exception.getMessage().contains("Search error"));
        
        verify(stationRepository, times(1)).searchStations("MADRID");
    }

    @Test
    void testSearchStationsWithNullFromRepository() {
        when(stationRepository.searchStations("MADRID")).thenReturn(null);

        List<Station> result = getStationsService.searchStations("MADRID");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(stationRepository, times(1)).searchStations("MADRID");
    }
}

