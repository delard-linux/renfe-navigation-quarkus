package com.delard.renfe.navigation.infrastructure.adapter.output;

import com.delard.renfe.navigation.application.exception.QueueException;
import com.delard.renfe.navigation.application.exception.TrainUnavailabilityException;
import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.domain.port.output.TrainScraperPort;
import com.delard.renfe.navigation.infrastructure.service.PlaywrightSearchTrainsService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adapter for scraping train information from Renfe website using Playwright
 */
@ApplicationScoped
public class TrainScraperAdapter implements TrainScraperPort {

    private static final Logger LOG = Logger.getLogger(TrainScraperAdapter.class);

    @Inject
    PlaywrightSearchTrainsService playwrightSearchTrainsService;

    @Override
    public List<List<Train>> scrapeTrains(String origin, String destination,
                                          String originDesgEstacion, String destinationDesgEstacion,
                                          String originClave, String destinationClave,
                                          String dateOut, String dateReturn, String adults) {
        LOG.debugf("Scraping trains: %s -> %s, dateOut: %s, dateReturn: %s, adults: %s",
                origin, destination, dateOut, dateReturn, adults);

        try {
            PlaywrightSearchTrainsService.SearchTrainsResult result =
                playwrightSearchTrainsService.searchTrains(
                        origin, destination, originDesgEstacion, destinationDesgEstacion,
                        originClave, destinationClave, dateOut, dateReturn, adults);

            List<Train> trainsOut = result.outboundTrains != null ? result.outboundTrains : new ArrayList<>();
            List<Train> trainsReturn = result.returnTrains;

            if (trainsReturn != null) {
                return Arrays.asList(trainsOut, trainsReturn);
            } else {
                return List.of(trainsOut);
            }

        } catch (QueueException e) {
            // Re-throw queue exceptions as-is
            throw e;
        } catch (TrainUnavailabilityException e) {
            // Re-throw train unavailability exceptions as-is
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "Error scraping trains");
            throw new RuntimeException("Error scraping trains: " + e.getMessage(), e);
        }
    }
}
