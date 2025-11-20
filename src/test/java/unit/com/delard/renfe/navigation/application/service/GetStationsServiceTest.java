package com.delard.renfe.navigation.application.service;

import com.delard.renfe.navigation.application.exception.ValidationException;
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
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            getStationsService.searchStations(null);
        });

        assertEquals("Search text is required", exception.getMessage());
        verify(stationRepository, never()).searchStations(anyString());
    }

    @Test
    void testSearchStationsWithBlankText() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            getStationsService.searchStations("   ");
        });

        assertEquals("Search text is required", exception.getMessage());
        verify(stationRepository, never()).searchStations(anyString());
    }

    @Test
    void testSearchStationsWithEmptyText() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            getStationsService.searchStations("");
        });

        assertEquals("Search text is required", exception.getMessage());
        verify(stationRepository, never()).searchStations(anyString());
    }

    @Test
    void testSearchStationsWithLessThanThreeCharacters() {
        ValidationException exception1 = assertThrows(ValidationException.class, () -> {
            getStationsService.searchStations("AB");
        });
        assertEquals("Search text must have at least 3 characters", exception1.getMessage());
        verify(stationRepository, never()).searchStations(anyString());

        ValidationException exception2 = assertThrows(ValidationException.class, () -> {
            getStationsService.searchStations("A");
        });
        assertEquals("Search text must have at least 3 characters", exception2.getMessage());
        verify(stationRepository, never()).searchStations(anyString());
    }

    @Test
    void testSearchStationsWithExactlyThreeCharacters() {
        Station station1 = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "MADRID (TODAS)");
        List<Station> matchingStations = Arrays.asList(station1);

        when(stationRepository.searchStations("MAD")).thenReturn(matchingStations);

        List<Station> result = getStationsService.searchStations("MAD");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stationRepository, times(1)).searchStations("MAD");
    }

    @Test
    void testSearchStationsWithWhitespaceTrimmed() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            getStationsService.searchStations("  AB  ");
        });

        assertEquals("Search text must have at least 3 characters", exception.getMessage());
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

    @Test
    void testSearchStationsWithMultipleWordsAllValid() {
        Station station1 = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "MADRID (TODAS)");
        List<Station> matchingStations = Arrays.asList(station1);

        when(stationRepository.searchStations("MADRID RAMON")).thenReturn(matchingStations);

        List<Station> result = getStationsService.searchStations("MADRID RAMON");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stationRepository, times(1)).searchStations("MADRID RAMON");
    }

    @Test
    void testSearchStationsWithWordLessThanThreeCharacters() {
        // When all words have 3 or fewer characters, validation requires all words to have at least 3 characters
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            getStationsService.searchStations("MA AB");
        });

        assertTrue(exception.getMessage().contains("Each word in the search text must have at least 3 characters"));
        // The message will mention the first invalid word found (MA)
        assertTrue(exception.getMessage().contains("MA"));
        verify(stationRepository, never()).searchStations(anyString());
    }

    @Test
    void testSearchStationsWithSingleWordLessThanThreeCharacters() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            getStationsService.searchStations("AB");
        });

        assertEquals("Search text must have at least 3 characters", exception.getMessage());
        verify(stationRepository, never()).searchStations(anyString());
    }

    @Test
    void testSearchStationsWithMultipleWordsOneInvalid() {
        // When there's at least one word with more than 3 characters, validation per word is skipped
        Station station1 = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "MADRID (TODAS)");
        List<Station> matchingStations = Arrays.asList(station1);

        when(stationRepository.searchStations("MADRID DE")).thenReturn(matchingStations);

        List<Station> result = getStationsService.searchStations("MADRID DE");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stationRepository, times(1)).searchStations("MADRID DE");
    }

    @Test
    void testSearchStationsWithMultipleSpacesBetweenWords() {
        Station station1 = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "MADRID (TODAS)");
        List<Station> matchingStations = Arrays.asList(station1);

        when(stationRepository.searchStations("MADRID   RAMON")).thenReturn(matchingStations);

        List<Station> result = getStationsService.searchStations("MADRID   RAMON");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stationRepository, times(1)).searchStations("MADRID   RAMON");
    }

    @Test
    void testSearchStationsWithLeadingAndTrailingSpaces() {
        Station station1 = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "MADRID (TODAS)");
        List<Station> matchingStations = Arrays.asList(station1);

        when(stationRepository.searchStations("  MADRID RAMON  ")).thenReturn(matchingStations);

        List<Station> result = getStationsService.searchStations("  MADRID RAMON  ");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stationRepository, times(1)).searchStations("  MADRID RAMON  ");
    }

    @Test
    void testSearchStationsWithThreeCharacterWords() {
        Station station1 = new Station("MADRI", "0071", 1, null,
                "MADRID (TODAS)", null, "0071,MADRI,null", "MADRID (TODAS)");
        List<Station> matchingStations = Arrays.asList(station1);

        when(stationRepository.searchStations("MAD RAM")).thenReturn(matchingStations);

        List<Station> result = getStationsService.searchStations("MAD RAM");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stationRepository, times(1)).searchStations("MAD RAM");
    }

    @Test
    void testSearchStationsWithShortWordsOnly() {
        // When all words have 3 or fewer characters, validation requires all words to have at least 3 characters
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            getStationsService.searchStations("DE LA");
        });

        assertTrue(exception.getMessage().contains("Each word in the search text must have at least 3 characters"));
        // The message will mention the invalid word (DE)
        assertTrue(exception.getMessage().contains("DE"));
        verify(stationRepository, never()).searchStations(anyString());
    }
}

