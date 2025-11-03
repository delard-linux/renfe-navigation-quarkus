package com.delard.renfe.navigation.domain.port.input;

import com.delard.renfe.navigation.domain.model.TrainsResponse;

/**
 * Input port for searching trains
 */
public interface SearchTrainsUseCase {

    /**
     * Search trains from origin to destination
     *
     * @param origin Station origin (e.g., "OURENSE")
     * @param destination Station destination (e.g., "MADRID")
     * @param dateOut Outbound date in format YYYY-MM-DD
     * @param dateReturn Optional return date in format YYYY-MM-DD
     * @param adults Number of adult passengers (1-8)
     * @return TrainsResponse with outbound and optional return trains
     */
    TrainsResponse searchTrains(String origin, String destination, String dateOut,
                                String dateReturn, int adults);
}

