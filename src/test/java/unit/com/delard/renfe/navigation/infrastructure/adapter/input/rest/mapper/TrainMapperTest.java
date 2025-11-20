/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.rest.mapper;


import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;

import com.delard.renfe.navigation.domain.model.FareOption;
import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.domain.model.TrainConnection;
import com.delard.renfe.navigation.domain.model.TrainsResponse;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.FareOptionDTO;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.TrainConnectionDTO;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.TrainDTO;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.TrainsResponseDTO;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for TrainMapper
 */
class TrainMapperTest
{

    @Test
    void testPrivateConstructor() throws Exception
    {
        Constructor<TrainMapper> constructor = TrainMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        TrainMapper instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    void testToDTOWithNullDomain()
    {
        TrainsResponseDTO result = TrainMapper.toDTO(null);
        assertNull(result);
    }

    @Test
    void testToDTOWithCompleteDomain()
    {
        TrainsResponse domain = createCompleteTrainsResponse();
        TrainsResponseDTO dto = TrainMapper.toDTO(domain);

        assertNotNull(dto);
        assertEquals("OURENSE", dto.origin());
        assertEquals("MADRID", dto.destination());
        assertEquals("2025-12-01", dto.dateOut());
        assertEquals("2025-12-05", dto.dateReturn());
        assertEquals("2", dto.adults());
        assertNotNull(dto.trainsOut());
        assertEquals(2, dto.trainsOut().size());
        assertNotNull(dto.trainsReturn());
        assertEquals(1, dto.trainsReturn().size());
    }

    @Test
    void testToDTOWithNullTrainsOut()
    {
        TrainsResponse domain = new TrainsResponse();
        domain.setOrigin("OURENSE");
        domain.setDestination("MADRID");
        domain.setDateOut("2025-12-01");
        domain.setAdults("1");
        domain.setTrainsOut(null);
        domain.setTrainsReturn(null);

        TrainsResponseDTO dto = TrainMapper.toDTO(domain);
        assertNotNull(dto);
        // trainsOut() returns empty list when null, not null itself
        assertNotNull(dto.trainsOut());
        assertTrue(dto.trainsOut().isEmpty());
        // trainsReturn() returns null when null
        assertNull(dto.trainsReturn());
    }

    @Test
    void testToDTOWithEmptyTrainsLists()
    {
        TrainsResponse domain = new TrainsResponse();
        domain.setOrigin("OURENSE");
        domain.setDestination("MADRID");
        domain.setDateOut("2025-12-01");
        domain.setAdults("1");
        domain.setTrainsOut(Collections.emptyList());
        domain.setTrainsReturn(Collections.emptyList());

        TrainsResponseDTO dto = TrainMapper.toDTO(domain);
        assertNotNull(dto);
        assertNotNull(dto.trainsOut());
        assertTrue(dto.trainsOut().isEmpty());
        assertNotNull(dto.trainsReturn());
        assertTrue(dto.trainsReturn().isEmpty());
    }

    @Test
    void testToDTOWithTrainContainingAllFields()
    {
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
        assertEquals(1, dto.trainsOut().size());

        TrainDTO trainDTO = dto.trainsOut().get(0);
        assertEquals("TRAIN123", trainDTO.trainId());
        assertEquals("AVE", trainDTO.serviceType());
        assertEquals("08:00", trainDTO.departureTime());
        assertEquals("12:30", trainDTO.arrivalTime());
        assertEquals("4h 30m", trainDTO.duration());
        assertEquals(45.50, trainDTO.priceFrom(), 0.01);
        assertEquals("EUR", trainDTO.currency());
        assertTrue(trainDTO.accessible());
        assertTrue(trainDTO.ecoFriendly());
        assertEquals(2, trainDTO.badges().size());
        assertTrue(trainDTO.badges().contains("WIFI"));
        assertTrue(trainDTO.badges().contains("POWER"));
    }

    @Test
    void testToDTOWithNullTrainInList()
    {
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
        assertEquals(2, dto.trainsOut().size());
        assertNotNull(dto.trainsOut().get(0));
        assertNull(dto.trainsOut().get(1));
    }

    @Test
    void testToDTOWithTrainWithNullFares()
    {
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
        TrainDTO trainDTO = dto.trainsOut().get(0);
        assertNotNull(trainDTO.fares());
        assertTrue(trainDTO.fares().isEmpty());
    }

    @Test
    void testToDTOWithTrainWithEmptyFares()
    {
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
        TrainDTO trainDTO = dto.trainsOut().get(0);
        assertNotNull(trainDTO.fares());
        assertTrue(trainDTO.fares().isEmpty());
    }

    @Test
    void testToDTOWithTrainWithFares()
    {
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
        TrainDTO trainDTO = dto.trainsOut().get(0);
        assertEquals(2, trainDTO.fares().size());

        FareOptionDTO fareDTO1 = trainDTO.fares().get(0);
        assertEquals("Basic", fareDTO1.name());
        assertEquals(45.50, fareDTO1.price(), 0.01);
        assertEquals("EUR", fareDTO1.currency());
        assertEquals("BASIC", fareDTO1.code());
        assertEquals("https://example.com/basic", fareDTO1.tpEnlace());
        assertEquals(1, fareDTO1.features().size());

        FareOptionDTO fareDTO2 = trainDTO.fares().get(1);
        assertEquals("Premium", fareDTO2.name());
        assertEquals(89.90, fareDTO2.price(), 0.01);
        assertEquals(2, fareDTO2.features().size());
    }

    @Test
    void testToDTOWithNullFareInList()
    {
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
        TrainDTO trainDTO = dto.trainsOut().get(0);
        assertEquals(2, trainDTO.fares().size());
        assertNotNull(trainDTO.fares().get(0));
        assertNull(trainDTO.fares().get(1));
    }

    @Test
    void testToDTOWithMinimalData()
    {
        TrainsResponse domain = new TrainsResponse();
        domain.setOrigin("A");
        domain.setDestination("B");
        domain.setDateOut("2025-01-01");
        domain.setAdults("1");

        TrainsResponseDTO dto = TrainMapper.toDTO(domain);
        assertNotNull(dto);
        assertEquals("A", dto.origin());
        assertEquals("B", dto.destination());
        assertEquals("2025-01-01", dto.dateOut());
        assertEquals("1", dto.adults());
    }

    @Test
    void testToDTOWithTrainWithConnection()
    {
        Train train = createCompleteTrain();
        TrainConnection connection = new TrainConnection();
        connection.setDuration("1 horas 10 minutos");
        connection.setFirstTrainType("REG.EXP.");
        connection.setSecondTrainType("AVE");
        train.setConnection(connection);

        TrainsResponse domain = new TrainsResponse();
        domain.setOrigin("OURENSE");
        domain.setDestination("MADRID");
        domain.setDateOut("2025-12-01");
        domain.setAdults("1");
        domain.setTrainsOut(Arrays.asList(train));
        domain.setTrainsReturn(null);

        TrainsResponseDTO dto = TrainMapper.toDTO(domain);
        assertNotNull(dto);
        TrainDTO trainDTO = dto.trainsOut().get(0);

        assertNotNull(trainDTO.connection());
        TrainConnectionDTO connectionDTO = trainDTO.connection();
        assertEquals("1 horas 10 minutos", connectionDTO.duration());
        assertEquals("REG.EXP.", connectionDTO.firstTrainType());
        assertEquals("AVE", connectionDTO.secondTrainType());
    }

    @Test
    void testToDTOWithTrainWithoutConnection()
    {
        Train train = createCompleteTrain();
        train.setConnection(null);

        TrainsResponse domain = new TrainsResponse();
        domain.setOrigin("OURENSE");
        domain.setDestination("MADRID");
        domain.setDateOut("2025-12-01");
        domain.setAdults("1");
        domain.setTrainsOut(Arrays.asList(train));
        domain.setTrainsReturn(null);

        TrainsResponseDTO dto = TrainMapper.toDTO(domain);
        assertNotNull(dto);
        TrainDTO trainDTO = dto.trainsOut().get(0);

        assertNull(trainDTO.connection());
    }

    @Test
    void testToDTOWithTrainWithConnectionDifferentTypes()
    {
        Train train = createCompleteTrain();
        TrainConnection connection = new TrainConnection();
        connection.setDuration("45 minutos");
        connection.setFirstTrainType("ALVIA");
        connection.setSecondTrainType("EUROMED");
        train.setConnection(connection);

        TrainsResponse domain = new TrainsResponse();
        domain.setOrigin("OURENSE");
        domain.setDestination("MADRID");
        domain.setDateOut("2025-12-01");
        domain.setAdults("1");
        domain.setTrainsOut(Arrays.asList(train));
        domain.setTrainsReturn(null);

        TrainsResponseDTO dto = TrainMapper.toDTO(domain);
        assertNotNull(dto);
        TrainDTO trainDTO = dto.trainsOut().get(0);

        assertNotNull(trainDTO.connection());
        TrainConnectionDTO connectionDTO = trainDTO.connection();
        assertEquals("45 minutos", connectionDTO.duration());
        assertEquals("ALVIA", connectionDTO.firstTrainType());
        assertEquals("EUROMED", connectionDTO.secondTrainType());
    }

    private TrainsResponse createCompleteTrainsResponse()
    {
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

    private Train createCompleteTrain()
    {
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
