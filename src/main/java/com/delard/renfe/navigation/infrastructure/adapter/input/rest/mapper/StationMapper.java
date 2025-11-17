package com.delard.renfe.navigation.infrastructure.adapter.input.rest.mapper;

import com.delard.renfe.navigation.domain.model.Station;
import com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto.StationDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Station domain models and DTOs
 */
public final class StationMapper {

    private StationMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Converts a Station domain model to StationDTO
     *
     * @param station Station domain model
     * @return StationDTO or null if input is null
     */
    public static StationDTO toDTO(Station station) {
        if (station == null) {
            return null;
        }

        return new StationDTO(
            station.getStationCode(),
            station.getAdministrationCode(),
            station.getPriority(),
            station.getDescription(),
            station.getStationName(),
            station.getUicCode(),
            station.getKey(),
            station.getStationNamePlano()
        );
    }

    /**
     * Converts a list of Station domain models to a list of StationDTOs
     *
     * @param stations List of Station domain models
     * @return List of StationDTOs
     */
    public static List<StationDTO> toDTOList(List<Station> stations) {
        if (stations == null) {
            return List.of();
        }
        return stations.stream()
                .map(StationMapper::toDTO)
                .collect(Collectors.toList());
    }
}

