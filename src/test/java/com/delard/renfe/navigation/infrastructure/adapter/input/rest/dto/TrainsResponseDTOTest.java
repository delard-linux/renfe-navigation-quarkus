package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TrainsResponseDTO
 */
class TrainsResponseDTOTest {

    @Test
    void testDefaultConstructor() {
        TrainsResponseDTO dto = new TrainsResponseDTO();
        assertNotNull(dto);
        assertEquals(0, dto.getAdults());
    }

    @Test
    void testGettersAndSetters() {
        TrainsResponseDTO dto = new TrainsResponseDTO();

        // Test origin
        dto.setOrigin("OURENSE");
        assertEquals("OURENSE", dto.getOrigin());

        // Test destination
        dto.setDestination("MADRID");
        assertEquals("MADRID", dto.getDestination());

        // Test dateOut
        dto.setDateOut("2025-12-01");
        assertEquals("2025-12-01", dto.getDateOut());

        // Test dateReturn
        dto.setDateReturn("2025-12-05");
        assertEquals("2025-12-05", dto.getDateReturn());

        // Test adults
        dto.setAdults(2);
        assertEquals(2, dto.getAdults());
    }

    @Test
    void testTrainsOutGetterAndSetter() {
        TrainsResponseDTO dto = new TrainsResponseDTO();
        List<TrainDTO> trainsOut = new ArrayList<>();
        TrainDTO train1 = new TrainDTO();
        train1.setTrainId("TRAIN1");
        trainsOut.add(train1);

        TrainDTO train2 = new TrainDTO();
        train2.setTrainId("TRAIN2");
        trainsOut.add(train2);

        dto.setTrainsOut(trainsOut);
        List<TrainDTO> retrievedTrains = dto.getTrainsOut();
        assertNotNull(retrievedTrains);
        assertEquals(2, retrievedTrains.size());
        assertEquals("TRAIN1", retrievedTrains.get(0).getTrainId());
        assertEquals("TRAIN2", retrievedTrains.get(1).getTrainId());

        // Test that getter returns a copy
        retrievedTrains.add(new TrainDTO());
        assertEquals(2, dto.getTrainsOut().size());
    }

    @Test
    void testTrainsOutSetterWithNull() {
        TrainsResponseDTO dto = new TrainsResponseDTO();
        dto.setTrainsOut(null);
        assertNotNull(dto.getTrainsOut());
        assertTrue(dto.getTrainsOut().isEmpty());
    }

    @Test
    void testTrainsReturnGetterAndSetter() {
        TrainsResponseDTO dto = new TrainsResponseDTO();
        List<TrainDTO> trainsReturn = new ArrayList<>();
        TrainDTO train = new TrainDTO();
        train.setTrainId("RETURN1");
        trainsReturn.add(train);

        dto.setTrainsReturn(trainsReturn);
        List<TrainDTO> retrievedTrains = dto.getTrainsReturn();
        assertNotNull(retrievedTrains);
        assertEquals(1, retrievedTrains.size());
        assertEquals("RETURN1", retrievedTrains.get(0).getTrainId());

        // Test that getter returns a copy
        retrievedTrains.add(new TrainDTO());
        assertEquals(1, dto.getTrainsReturn().size());
    }

    @Test
    void testTrainsReturnSetterWithNull() {
        TrainsResponseDTO dto = new TrainsResponseDTO();
        dto.setTrainsReturn(null);
        assertNull(dto.getTrainsReturn());
    }

    @Test
    void testAllFields() {
        TrainsResponseDTO dto = new TrainsResponseDTO();
        dto.setOrigin("OURENSE");
        dto.setDestination("MADRID");
        dto.setDateOut("2025-12-01");
        dto.setDateReturn("2025-12-05");
        dto.setAdults(2);

        List<TrainDTO> trainsOut = Arrays.asList(createTrainDTO("TRAIN1"));
        dto.setTrainsOut(trainsOut);

        List<TrainDTO> trainsReturn = Arrays.asList(createTrainDTO("RETURN1"));
        dto.setTrainsReturn(trainsReturn);

        assertEquals("OURENSE", dto.getOrigin());
        assertEquals("MADRID", dto.getDestination());
        assertEquals("2025-12-01", dto.getDateOut());
        assertEquals("2025-12-05", dto.getDateReturn());
        assertEquals(2, dto.getAdults());
        assertEquals(1, dto.getTrainsOut().size());
        assertEquals(1, dto.getTrainsReturn().size());
    }

    @Test
    void testEmptyTrainsOut() {
        TrainsResponseDTO dto = new TrainsResponseDTO();
        dto.setTrainsOut(Collections.emptyList());
        assertNotNull(dto.getTrainsOut());
        assertTrue(dto.getTrainsOut().isEmpty());
    }

    private TrainDTO createTrainDTO(String trainId) {
        TrainDTO train = new TrainDTO();
        train.setTrainId(trainId);
        return train;
    }
}

