package com.delard.renfe.navigation.infrastructure.adapter.input.rest.mapper;

import com.delard.renfe.navigation.domain.model.FareOption;
import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.domain.model.TrainConnection;
import com.delard.renfe.navigation.domain.model.TrainsResponse;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.FareOptionDTO;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.TrainConnectionDTO;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.TrainDTO;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.TrainsResponseDTO;

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

        return new TrainsResponseDTO(
            domain.getOrigin(),
            domain.getDestination(),
            domain.getDateOut(),
            domain.getDateReturn(),
            domain.getAdults(),
            toTrainDTOList(domain.getTrainsOut()),
            toTrainDTOList(domain.getTrainsReturn())
        );
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

        return new TrainDTO(
            train.getTrainId(),
            train.getServiceType(),
            train.getDepartureTime(),
            train.getArrivalTime(),
            train.getDuration(),
            train.getPriceFrom(),
            train.getCurrency(),
            toFareOptionDTOList(train.getFares()),
            new ArrayList<>(train.getBadges()),
            train.isAccessible(),
            train.isEcoFriendly(),
            toTrainConnectionDTO(train.getConnection())
        );
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

        return new FareOptionDTO(
            fare.getName(),
            fare.getPrice(),
            fare.getCurrency(),
            fare.getCode(),
            fare.getTpEnlace(),
            fare.getPlan(),
            fare.getFeatures()
        );
    }

    private static TrainConnectionDTO toTrainConnectionDTO(TrainConnection connection) {
        if (connection == null) {
            return null;
        }
        return new TrainConnectionDTO(
            connection.getDuration(),
            connection.getFirstTrainType(),
            connection.getSecondTrainType()
        );
    }
}

