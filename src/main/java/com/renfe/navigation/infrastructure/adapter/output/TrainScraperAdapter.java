package com.renfe.navigation.infrastructure.adapter.output;

import com.renfe.navigation.domain.model.Train;
import com.renfe.navigation.domain.port.output.TrainScraperPort;
import com.renfe.navigation.infrastructure.service.PlaywrightScraperService;
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
    PlaywrightScraperService playwrightScraperService;

    @Override
    public List<List<Train>> scrapeTrains(String origin, String destination, String dateOut,
                                          String dateReturn, int adults) {
        LOG.infof("Scraping trains: %s -> %s, dateOut: %s, dateReturn: %s, adults: %d",
                origin, destination, dateOut, dateReturn, adults);

        try {
            PlaywrightScraperService.SearchTrainsResult result =
                    playwrightScraperService.searchTrains(origin, destination, dateOut, dateReturn, adults);

            List<Train> trainsOut = result.outboundTrains != null ? result.outboundTrains : new ArrayList<>();
            List<Train> trainsReturn = result.returnTrains;

            if (trainsReturn != null) {
                return Arrays.asList(trainsOut, trainsReturn);
            } else {
                return List.of(trainsOut);
            }

        } catch (Exception e) {
            LOG.errorf(e, "Error scraping trains");
            throw new RuntimeException("Error scraping trains: " + e.getMessage(), e);
        }
    }
}
