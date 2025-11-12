package com.delard.renfe.navigation.infrastructure.adapter.input.rest.mapper;

import com.delard.renfe.navigation.domain.model.FareOption;
import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.domain.model.TrainsResponse;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.FareOptionDTO;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.TrainDTO;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.TrainsResponseDTO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TrainMapper
 */
class TrainMapperTest {

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<TrainMapper> constructor = TrainMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        TrainMapper instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    void testToDTOWithNullDomain() {
        TrainsResponseDTO result = TrainMapper.toDTO(null);
        assertNull(result);
    }

    @Test
    void testToDTOWithCompleteDomain() {
        TrainsResponse domain = createCompleteTrainsResponse();
        TrainsResponseDTO dto = TrainMapper.toDTO(domain);

        assertNotNull(dto);
        assertEquals("OURENSE", dto.getOrigin());
        assertEquals("MADRID", dto.getDestination());
        assertEquals("2025-12-01", dto.getDateOut());
        assertEquals("2025-12-05", dto.getDateReturn());
        assertEquals("2", dto.getAdults());
        assertNotNull(dto.getTrainsOut());
        assertEquals(2, dto.getTrainsOut().size());
        assertNotNull(dto.getTrainsReturn());
        assertEquals(1, dto.getTrainsReturn().size());
    }

    @Test
    void testToDTOWithNullTrainsOut() {
        TrainsResponse domain = new TrainsResponse();
        domain.setOrigin("OURENSE");
        domain.setDestination("MADRID");
        domain.setDateOut("2025-12-01");
        domain.setAdults("1");
        domain.setTrainsOut(null);
        domain.setTrainsReturn(null);

        TrainsResponseDTO dto = TrainMapper.toDTO(domain);
        assertNotNull(dto);
        // getTrainsOut() returns empty list when null, not null itself
        assertNotNull(dto.getTrainsOut());
        assertTrue(dto.getTrainsOut().isEmpty());
        // getTrainsReturn() returns null when null
        assertNull(dto.getTrainsReturn());
    }

    @Test
    void testToDTOWithEmptyTrainsLists() {
        TrainsResponse domain = new TrainsResponse();
        domain.setOrigin("OURENSE");
        domain.setDestination("MADRID");
        domain.setDateOut("2025-12-01");
        domain.setAdults("1");
        domain.setTrainsOut(Collections.emptyList());
        domain.setTrainsReturn(Collections.emptyList());

        TrainsResponseDTO dto = TrainMapper.toDTO(domain);
        assertNotNull(dto);
        assertNotNull(dto.getTrainsOut());
        assertTrue(dto.getTrainsOut().isEmpty());
        assertNotNull(dto.getTrainsReturn());
        assertTrue(dto.getTrainsReturn().isEmpty());
    }

    @Test
    void testToDTOWithTrainContainingAllFields() {
        Train train = createCompleteTrain();
        TrainsResponse domain = new TrainsResponse();
        domain.setOrigin("OURENSE");
        domain.setDestination("MADRID");
        domain.setDateOut("2025-12-01");
        domain.setAdults("1");
        domain.setTrainsOut(Arrays.asList(train));
        domain.setTrainsReturn(null);

        TrainsResponseDTO dto = TrainMapper.toDTO(domain);
        assertNotNull(dto);
        assertEquals(1, dto.getTrainsOut().size());

        TrainDTO trainDTO = dto.getTrainsOut().get(0);
        assertEquals("TRAIN123", trainDTO.getTrainId());
        assertEquals("AVE", trainDTO.getServiceType());
        assertEquals("08:00", trainDTO.getDepartureTime());
        assertEquals("12:30", trainDTO.getArrivalTime());
        assertEquals("4h 30m", trainDTO.getDuration());
        assertEquals(45.50, trainDTO.getPriceFrom(), 0.01);
        assertEquals("EUR", trainDTO.getCurrency());
        assertTrue(trainDTO.isAccessible());
        assertTrue(trainDTO.isEcoFriendly());
        assertEquals(2, trainDTO.getBadges().size());
        assertTrue(trainDTO.getBadges().contains("WIFI"));
        assertTrue(trainDTO.getBadges().contains("POWER"));
    }

    @Test
    void testToDTOWithNullTrainInList() {
        Train train1 = createCompleteTrain();
        Train train2 = null;
        TrainsResponse domain = new TrainsResponse();
        domain.setOrigin("OURENSE");
        domain.setDestination("MADRID");
        domain.setDateOut("2025-12-01");
        domain.setAdults("1");
        domain.setTrainsOut(Arrays.asList(train1, train2));
        domain.setTrainsReturn(null);

        TrainsResponseDTO dto = TrainMapper.toDTO(domain);
        assertNotNull(dto);
        assertEquals(2, dto.getTrainsOut().size());
        assertNotNull(dto.getTrainsOut().get(0));
        assertNull(dto.getTrainsOut().get(1));
    }

    @Test
    void testToDTOWithTrainWithNullFares() {
        Train train = createCompleteTrain();
        train.setFares(null);
        TrainsResponse domain = new TrainsResponse();
        domain.setOrigin("OURENSE");
        domain.setDestination("MADRID");
        domain.setDateOut("2025-12-01");
        domain.setAdults("1");
        domain.setTrainsOut(Arrays.asList(train));
        domain.setTrainsReturn(null);

        TrainsResponseDTO dto = TrainMapper.toDTO(domain);
        assertNotNull(dto);
        TrainDTO trainDTO = dto.getTrainsOut().get(0);
        assertNotNull(trainDTO.getFares());
        assertTrue(trainDTO.getFares().isEmpty());
    }

    @Test
    void testToDTOWithTrainWithEmptyFares() {
        Train train = createCompleteTrain();
        train.setFares(Collections.emptyList());
        TrainsResponse domain = new TrainsResponse();
        domain.setOrigin("OURENSE");
        domain.setDestination("MADRID");
        domain.setDateOut("2025-12-01");
        domain.setAdults("1");
        domain.setTrainsOut(Arrays.asList(train));
        domain.setTrainsReturn(null);

        TrainsResponseDTO dto = TrainMapper.toDTO(domain);
        assertNotNull(dto);
        TrainDTO trainDTO = dto.getTrainsOut().get(0);
        assertNotNull(trainDTO.getFares());
        assertTrue(trainDTO.getFares().isEmpty());
    }

    @Test
    void testToDTOWithTrainWithFares() {
        Train train = createCompleteTrain();
        FareOption fare1 = new FareOption();
        fare1.setName("Basic");
        fare1.setPrice(45.50);
        fare1.setCurrency("EUR");
        fare1.setCode("BASIC");
        fare1.setTpEnlace("https://example.com/basic");
        fare1.setFeatures(Arrays.asList("WIFI"));

        FareOption fare2 = new FareOption();
        fare2.setName("Premium");
        fare2.setPrice(89.90);
        fare2.setCurrency("EUR");
        fare2.setCode("PREMIUM");
        fare2.setTpEnlace("https://example.com/premium");
        fare2.setFeatures(Arrays.asList("WIFI", "MEAL"));

        train.setFares(Arrays.asList(fare1, fare2));
        TrainsResponse domain = new TrainsResponse();
        domain.setOrigin("OURENSE");
        domain.setDestination("MADRID");
        domain.setDateOut("2025-12-01");
        domain.setAdults("1");
        domain.setTrainsOut(Arrays.asList(train));
        domain.setTrainsReturn(null);

        TrainsResponseDTO dto = TrainMapper.toDTO(domain);
        assertNotNull(dto);
        TrainDTO trainDTO = dto.getTrainsOut().get(0);
        assertEquals(2, trainDTO.getFares().size());

        FareOptionDTO fareDTO1 = trainDTO.getFares().get(0);
        assertEquals("Basic", fareDTO1.getName());
        assertEquals(45.50, fareDTO1.getPrice(), 0.01);
        assertEquals("EUR", fareDTO1.getCurrency());
        assertEquals("BASIC", fareDTO1.getCode());
        assertEquals("https://example.com/basic", fareDTO1.getTpEnlace());
        assertEquals(1, fareDTO1.getFeatures().size());

        FareOptionDTO fareDTO2 = trainDTO.getFares().get(1);
        assertEquals("Premium", fareDTO2.getName());
        assertEquals(89.90, fareDTO2.getPrice(), 0.01);
        assertEquals(2, fareDTO2.getFeatures().size());
    }

    @Test
    void testToDTOWithNullFareInList() {
        Train train = createCompleteTrain();
        FareOption fare1 = new FareOption();
        fare1.setName("Basic");
        fare1.setPrice(45.50);
        FareOption fare2 = null;
        train.setFares(Arrays.asList(fare1, fare2));

        TrainsResponse domain = new TrainsResponse();
        domain.setOrigin("OURENSE");
        domain.setDestination("MADRID");
        domain.setDateOut("2025-12-01");
        domain.setAdults("1");
        domain.setTrainsOut(Arrays.asList(train));
        domain.setTrainsReturn(null);

        TrainsResponseDTO dto = TrainMapper.toDTO(domain);
        assertNotNull(dto);
        TrainDTO trainDTO = dto.getTrainsOut().get(0);
        assertEquals(2, trainDTO.getFares().size());
        assertNotNull(trainDTO.getFares().get(0));
        assertNull(trainDTO.getFares().get(1));
    }

    @Test
    void testToDTOWithMinimalData() {
        TrainsResponse domain = new TrainsResponse();
        domain.setOrigin("A");
        domain.setDestination("B");
        domain.setDateOut("2025-01-01");
        domain.setAdults("1");

        TrainsResponseDTO dto = TrainMapper.toDTO(domain);
        assertNotNull(dto);
        assertEquals("A", dto.getOrigin());
        assertEquals("B", dto.getDestination());
        assertEquals("2025-01-01", dto.getDateOut());
        assertEquals("1", dto.getAdults());
    }

    private TrainsResponse createCompleteTrainsResponse() {
        Train train1 = createCompleteTrain();
        Train train2 = new Train();
        train2.setTrainId("TRAIN456");
        train2.setServiceType("ALVIA");
        train2.setDepartureTime("10:00");
        train2.setArrivalTime("15:30");
        train2.setDuration("5h 30m");
        train2.setPriceFrom(67.80);

        Train returnTrain = new Train();
        returnTrain.setTrainId("RETURN123");
        returnTrain.setServiceType("AVE");
        returnTrain.setDepartureTime("16:00");
        returnTrain.setArrivalTime("20:30");
        returnTrain.setDuration("4h 30m");
        returnTrain.setPriceFrom(50.00);

        TrainsResponse response = new TrainsResponse();
        response.setOrigin("OURENSE");
        response.setDestination("MADRID");
        response.setDateOut("2025-12-01");
        response.setDateReturn("2025-12-05");
        response.setAdults("2");
        response.setTrainsOut(Arrays.asList(train1, train2));
        response.setTrainsReturn(Arrays.asList(returnTrain));

        return response;
    }

    private Train createCompleteTrain() {
        Train train = new Train();
        train.setTrainId("TRAIN123");
        train.setServiceType("AVE");
        train.setDepartureTime("08:00");
        train.setArrivalTime("12:30");
        train.setDuration("4h 30m");
        train.setPriceFrom(45.50);
        train.setCurrency("EUR");
        train.setAccessible(true);
        train.setEcoFriendly(true);
        train.setBadges(Arrays.asList("WIFI", "POWER"));
        return train;
    }
}

