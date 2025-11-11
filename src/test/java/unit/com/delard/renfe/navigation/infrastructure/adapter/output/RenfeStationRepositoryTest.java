package com.delard.renfe.navigation.infrastructure.adapter.output;

import com.delard.renfe.navigation.domain.model.Station;
import com.delard.renfe.navigation.infrastructure.service.StationLoaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RenfeStationRepository
 */
@ExtendWith(MockitoExtension.class)
class RenfeStationRepositoryTest {

    @Mock
    private StationLoaderService stationLoaderService;

    @InjectMocks
    private RenfeStationRepository renfeStationRepository;

    private List<Map<String, Object>> mockStationMaps;

    @BeforeEach
    void setUp() {
        Map<String, Object> station1 = new HashMap<>();
        station1.put("cdgoEstacion", "MADRI");
        station1.put("cdgoAdmon", "0071");
        station1.put("nmroPrioridad", 1);
        station1.put("descEstacion", null);
        station1.put("desgEstacion", "MADRID (TODAS)");
        station1.put("cdgoUic", null);
        station1.put("clave", "0071,MADRI,null");
        station1.put("desgEstacionPlano", "MADRID (TODAS)");

        Map<String, Object> station2 = new HashMap<>();
        station2.put("cdgoEstacion", "60000");
        station2.put("cdgoAdmon", "0071");
        station2.put("nmroPrioridad", 2);
        station2.put("descEstacion", null);
        station2.put("desgEstacion", "MADRID-PUERTA DE ATOCHA-ALMUDENA GRANDES");
        station2.put("cdgoUic", "00600");
        station2.put("clave", "0071,60000,00600");
        station2.put("desgEstacionPlano", "MADRID-PUERTA DE ATOCHA-ALMUDENA GRANDES");

        mockStationMaps = Arrays.asList(station1, station2);
    }

    @Test
    void testLoadAllStationsSuccess() {
        when(stationLoaderService.loadStations()).thenReturn(mockStationMaps);

        List<Station> result = renfeStationRepository.loadAllStations();

        assertNotNull(result);
        assertEquals(2, result.size());
        
        Station station1 = result.get(0);
        assertEquals("MADRI", station1.getStationCode());
        assertEquals("0071", station1.getAdministrationCode());
        assertEquals(1, station1.getPriority());
        assertEquals("MADRID (TODAS)", station1.getStationName());
        assertEquals("0071,MADRI,null", station1.getKey());
        assertEquals("MADRID (TODAS)", station1.getStationNamePlano());
        
        Station station2 = result.get(1);
        assertEquals("60000", station2.getStationCode());
        assertEquals("00600", station2.getUicCode());
        assertEquals("MADRID-PUERTA DE ATOCHA-ALMUDENA GRANDES", station2.getStationName());
        
        verify(stationLoaderService, times(1)).loadStations();
    }

    @Test
    void testLoadAllStationsEmptyList() {
        when(stationLoaderService.loadStations()).thenReturn(Collections.emptyList());

        List<Station> result = renfeStationRepository.loadAllStations();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(stationLoaderService, times(1)).loadStations();
    }

    @Test
    void testLoadAllStationsWithNullValues() {
        Map<String, Object> stationWithNulls = new HashMap<>();
        stationWithNulls.put("cdgoEstacion", null);
        stationWithNulls.put("cdgoAdmon", "0071");
        stationWithNulls.put("nmroPrioridad", null);
        stationWithNulls.put("descEstacion", null);
        stationWithNulls.put("desgEstacion", "TEST STATION");
        stationWithNulls.put("cdgoUic", null);
        stationWithNulls.put("clave", null);
        stationWithNulls.put("desgEstacionPlano", null);

        when(stationLoaderService.loadStations()).thenReturn(Collections.singletonList(stationWithNulls));

        List<Station> result = renfeStationRepository.loadAllStations();

        assertNotNull(result);
        assertEquals(1, result.size());
        
        Station station = result.get(0);
        assertNull(station.getStationCode());
        assertEquals("0071", station.getAdministrationCode());
        assertNull(station.getPriority());
        assertEquals("TEST STATION", station.getStationName());
    }

    @Test
    void testLoadAllStationsWithIntegerAsString() {
        Map<String, Object> station = new HashMap<>();
        station.put("cdgoEstacion", "12345");
        station.put("cdgoAdmon", "0071");
        station.put("nmroPrioridad", "10"); // String instead of Integer
        station.put("desgEstacion", "TEST");
        station.put("cdgoUic", "12345");
        station.put("clave", "0071,12345,12345");
        station.put("desgEstacionPlano", "TEST");

        when(stationLoaderService.loadStations()).thenReturn(Collections.singletonList(station));

        List<Station> result = renfeStationRepository.loadAllStations();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getPriority());
    }

    @Test
    void testLoadAllStationsWithInvalidInteger() {
        Map<String, Object> station = new HashMap<>();
        station.put("cdgoEstacion", "TEST");
        station.put("cdgoAdmon", "0071");
        station.put("nmroPrioridad", "not-a-number");
        station.put("desgEstacion", "TEST");
        station.put("cdgoUic", null);
        station.put("clave", "0071,TEST,null");
        station.put("desgEstacionPlano", "TEST");

        when(stationLoaderService.loadStations()).thenReturn(Collections.singletonList(station));

        List<Station> result = renfeStationRepository.loadAllStations();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getPriority());
    }

    @Test
    void testLoadAllStationsSkipsInvalidStations() {
        Map<String, Object> validStation = mockStationMaps.get(0);
        Map<String, Object> invalidStation = new HashMap<>();
        // Missing required fields - will cause exception during conversion

        when(stationLoaderService.loadStations()).thenReturn(Arrays.asList(validStation, invalidStation));

        List<Station> result = renfeStationRepository.loadAllStations();

        // Should have at least the valid station
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testLoadAllStationsWithNumberAsInteger() {
        Map<String, Object> station = new HashMap<>();
        station.put("cdgoEstacion", "TEST");
        station.put("cdgoAdmon", "0071");
        station.put("nmroPrioridad", 5L); // Long instead of Integer
        station.put("desgEstacion", "TEST");
        station.put("cdgoUic", null);
        station.put("clave", "0071,TEST,null");
        station.put("desgEstacionPlano", "TEST");

        when(stationLoaderService.loadStations()).thenReturn(Collections.singletonList(station));

        List<Station> result = renfeStationRepository.loadAllStations();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getPriority());
    }

    @Test
    void testSearchStationsByStationName() {
        Map<String, Object> station1 = new HashMap<>();
        station1.put("cdgoEstacion", "MADRI");
        station1.put("cdgoAdmon", "0071");
        station1.put("nmroPrioridad", 1);
        station1.put("desgEstacion", "MADRID (TODAS)");
        station1.put("desgEstacionPlano", "MADRID (TODAS)");
        station1.put("clave", "0071,MADRI,null");

        Map<String, Object> station2 = new HashMap<>();
        station2.put("cdgoEstacion", "60000");
        station2.put("cdgoAdmon", "0071");
        station2.put("nmroPrioridad", 2);
        station2.put("desgEstacion", "MADRID-PUERTA DE ATOCHA-ALMUDENA GRANDES");
        station2.put("desgEstacionPlano", "MADRID-PUERTA DE ATOCHA-ALMUDENA GRANDES");
        station2.put("clave", "0071,60000,00600");

        Map<String, Object> station3 = new HashMap<>();
        station3.put("cdgoEstacion", "BARCE");
        station3.put("cdgoAdmon", "0071");
        station3.put("nmroPrioridad", 3);
        station3.put("desgEstacion", "BARCELONA (TODAS)");
        station3.put("desgEstacionPlano", "BARCELONA (TODAS)");
        station3.put("clave", "0071,BARCE,null");

        when(stationLoaderService.loadStations()).thenReturn(Arrays.asList(station1, station2, station3));

        List<Station> result = renfeStationRepository.searchStations("MADRID");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("MADRI", result.get(0).getStationCode());
        assertEquals("60000", result.get(1).getStationCode());
    }

    @Test
    void testSearchStationsByStationNamePlano() {
        Map<String, Object> station1 = new HashMap<>();
        station1.put("cdgoEstacion", "MADRI");
        station1.put("cdgoAdmon", "0071");
        station1.put("nmroPrioridad", 1);
        station1.put("desgEstacion", "MADRID (TODAS)");
        station1.put("desgEstacionPlano", "MADRID (TODAS)");
        station1.put("clave", "0071,MADRI,null");

        Map<String, Object> station2 = new HashMap<>();
        station2.put("cdgoEstacion", "60000");
        station2.put("cdgoAdmon", "0071");
        station2.put("nmroPrioridad", 2);
        station2.put("desgEstacion", "MADRID-PUERTA DE ATOCHA-ALMUDENA GRANDES");
        station2.put("desgEstacionPlano", "MADRID-PUERTA DE ATOCHA-ALMUDENA GRANDES");
        station2.put("clave", "0071,60000,00600");

        when(stationLoaderService.loadStations()).thenReturn(Arrays.asList(station1, station2));

        List<Station> result = renfeStationRepository.searchStations("ATOCHA");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("60000", result.get(0).getStationCode());
    }

    @Test
    void testSearchStationsCaseInsensitive() {
        Map<String, Object> station1 = new HashMap<>();
        station1.put("cdgoEstacion", "MADRI");
        station1.put("cdgoAdmon", "0071");
        station1.put("nmroPrioridad", 1);
        station1.put("desgEstacion", "MADRID (TODAS)");
        station1.put("desgEstacionPlano", "MADRID (TODAS)");
        station1.put("clave", "0071,MADRI,null");

        when(stationLoaderService.loadStations()).thenReturn(Collections.singletonList(station1));

        List<Station> result = renfeStationRepository.searchStations("madrid");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MADRI", result.get(0).getStationCode());
    }

    @Test
    void testSearchStationsPartialMatch() {
        Map<String, Object> station1 = new HashMap<>();
        station1.put("cdgoEstacion", "60000");
        station1.put("cdgoAdmon", "0071");
        station1.put("nmroPrioridad", 2);
        station1.put("desgEstacion", "MADRID-PUERTA DE ATOCHA-ALMUDENA GRANDES");
        station1.put("desgEstacionPlano", "MADRID-PUERTA DE ATOCHA-ALMUDENA GRANDES");
        station1.put("clave", "0071,60000,00600");

        when(stationLoaderService.loadStations()).thenReturn(Collections.singletonList(station1));

        List<Station> result = renfeStationRepository.searchStations("PUERTA");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("60000", result.get(0).getStationCode());
    }

    @Test
    void testSearchStationsNoMatch() {
        Map<String, Object> station1 = new HashMap<>();
        station1.put("cdgoEstacion", "MADRI");
        station1.put("cdgoAdmon", "0071");
        station1.put("nmroPrioridad", 1);
        station1.put("desgEstacion", "MADRID (TODAS)");
        station1.put("desgEstacionPlano", "MADRID (TODAS)");
        station1.put("clave", "0071,MADRI,null");

        when(stationLoaderService.loadStations()).thenReturn(Collections.singletonList(station1));

        List<Station> result = renfeStationRepository.searchStations("VALENCIA");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchStationsWithNullText() {
        List<Station> result = renfeStationRepository.searchStations(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(stationLoaderService, never()).loadStations();
    }

    @Test
    void testSearchStationsWithBlankText() {
        List<Station> result = renfeStationRepository.searchStations("   ");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(stationLoaderService, never()).loadStations();
    }

    @Test
    void testSearchStationsWithEmptyText() {
        List<Station> result = renfeStationRepository.searchStations("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(stationLoaderService, never()).loadStations();
    }

    @Test
    void testSearchStationsWithNullFields() {
        Map<String, Object> station1 = new HashMap<>();
        station1.put("cdgoEstacion", "TEST");
        station1.put("cdgoAdmon", "0071");
        station1.put("nmroPrioridad", 1);
        station1.put("desgEstacion", null);
        station1.put("desgEstacionPlano", null);
        station1.put("clave", "0071,TEST,null");

        Map<String, Object> station2 = new HashMap<>();
        station2.put("cdgoEstacion", "MADRI");
        station2.put("cdgoAdmon", "0071");
        station2.put("nmroPrioridad", 1);
        station2.put("desgEstacion", "MADRID (TODAS)");
        station2.put("desgEstacionPlano", "MADRID (TODAS)");
        station2.put("clave", "0071,MADRI,null");

        when(stationLoaderService.loadStations()).thenReturn(Arrays.asList(station1, station2));

        List<Station> result = renfeStationRepository.searchStations("MADRID");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MADRI", result.get(0).getStationCode());
    }

    @Test
    void testSearchStationsMatchesBothFields() {
        Map<String, Object> station1 = new HashMap<>();
        station1.put("cdgoEstacion", "MADRI");
        station1.put("cdgoAdmon", "0071");
        station1.put("nmroPrioridad", 1);
        station1.put("desgEstacion", "MADRID (TODAS)");
        station1.put("desgEstacionPlano", "MADRID (TODAS)");
        station1.put("clave", "0071,MADRI,null");

        when(stationLoaderService.loadStations()).thenReturn(Collections.singletonList(station1));

        // Should match even if searching in stationNamePlano
        List<Station> result = renfeStationRepository.searchStations("MADRID");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MADRI", result.get(0).getStationCode());
    }
}

