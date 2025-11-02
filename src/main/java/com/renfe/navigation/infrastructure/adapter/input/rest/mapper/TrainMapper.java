package com.renfe.navigation.infrastructure.adapter.input.rest.mapper;

import com.renfe.navigation.domain.model.FareOption;
import com.renfe.navigation.domain.model.Train;
import com.renfe.navigation.domain.model.TrainsResponse;
import com.renfe.navigation.infrastructure.adapter.input.rest.dto.FareOptionDTO;
import com.renfe.navigation.infrastructure.adapter.input.rest.dto.TrainDTO;
import com.renfe.navigation.infrastructure.adapter.input.rest.dto.TrainsResponseDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between domain models and DTOs
 */
public final class TrainMapper {

    private TrainMapper() {
        // Private constructor to prevent instantiation
    }

    public static TrainsResponseDTO toDTO(TrainsResponse domain) {
        if (domain == null) {
            return null;
        }

        TrainsResponseDTO dto = new TrainsResponseDTO();
        dto.setOrigin(domain.getOrigin());
        dto.setDestination(domain.getDestination());
        dto.setDateOut(domain.getDateOut());
        dto.setDateReturn(domain.getDateReturn());
        dto.setAdults(domain.getAdults());
        dto.setTrainsOut(toTrainDTOList(domain.getTrainsOut()));
        dto.setTrainsReturn(toTrainDTOList(domain.getTrainsReturn()));

        return dto;
    }

    private static List<TrainDTO> toTrainDTOList(List<Train> trains) {
        if (trains == null) {
            return null;
        }
        return trains.stream()
                .map(TrainMapper::toTrainDTO)
                .collect(Collectors.toList());
    }

    private static TrainDTO toTrainDTO(Train train) {
        if (train == null) {
            return null;
        }

        TrainDTO dto = new TrainDTO();
        dto.setTrainId(train.getTrainId());
        dto.setServiceType(train.getServiceType());
        dto.setDepartureTime(train.getDepartureTime());
        dto.setArrivalTime(train.getArrivalTime());
        dto.setDuration(train.getDuration());
        dto.setPriceFrom(train.getPriceFrom());
        dto.setCurrency(train.getCurrency());
        dto.setFares(toFareOptionDTOList(train.getFares()));
        dto.setBadges(new ArrayList<>(train.getBadges()));
        dto.setAccessible(train.isAccessible());
        dto.setEcoFriendly(train.isEcoFriendly());

        return dto;
    }

    private static List<FareOptionDTO> toFareOptionDTOList(List<FareOption> fares) {
        if (fares == null) {
            return new ArrayList<>();
        }
        return fares.stream()
                .map(TrainMapper::toFareOptionDTO)
                .collect(Collectors.toList());
    }

    private static FareOptionDTO toFareOptionDTO(FareOption fare) {
        if (fare == null) {
            return null;
        }

        FareOptionDTO dto = new FareOptionDTO();
        dto.setName(fare.getName());
        dto.setPrice(fare.getPrice());
        dto.setCurrency(fare.getCurrency());
        dto.setCode(fare.getCode());
        dto.setTpEnlace(fare.getTpEnlace());
        dto.setFeatures(fare.getFeatures());

        return dto;
    }
}

