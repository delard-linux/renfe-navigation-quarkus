package com.renfe.navigation.application.service;

import com.renfe.navigation.domain.model.Train;
import com.renfe.navigation.domain.model.TrainsResponse;
import com.renfe.navigation.domain.port.input.SearchTrainsUseCase;
import com.renfe.navigation.domain.port.output.TrainScraperPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Application service for searching trains
 */
@ApplicationScoped
public class SearchTrainsService implements SearchTrainsUseCase {

    private static final Logger LOG = Logger.getLogger(SearchTrainsService.class);

    @Inject
    TrainScraperPort trainScraperPort;

    @Override
    public TrainsResponse searchTrains(String origin, String destination, String dateOut,
                                       String dateReturn, int adults) {
        Instant startTime = Instant.now();

        LOG.infof("[REQUEST] Starting search: %s -> %s, Outbound: %s, Return: %s, Passengers: %d",
                origin, destination, dateOut, dateReturn, adults);

        try {
            List<List<Train>> result = trainScraperPort.scrapeTrains(
                    origin, destination, dateOut, dateReturn, adults);

            List<Train> trainsOut = result.get(0);
            List<Train> trainsReturn = result.size() > 1 ? result.get(1) : null;

            Duration elapsed = Duration.between(startTime, Instant.now());
            LOG.infof("[SUCCESS] Search completed in %.2fs - Outbound trains: %d, Return trains: %d",
                    elapsed.toMillis() / 1000.0,
                    trainsOut != null ? trainsOut.size() : 0,
                    trainsReturn != null ? trainsReturn.size() : 0);

            return new TrainsResponse(
                    origin, destination, dateOut, dateReturn, adults,
                    trainsOut, trainsReturn);

        } catch (Exception e) {
            Duration elapsed = Duration.between(startTime, Instant.now());
            LOG.errorf(e, "[ERROR] Search failed after %.2fs: %s",
                    elapsed.toMillis() / 1000.0, e.getMessage());
            throw new RuntimeException("Error searching trains: " + e.getMessage(), e);
        }
    }
}

