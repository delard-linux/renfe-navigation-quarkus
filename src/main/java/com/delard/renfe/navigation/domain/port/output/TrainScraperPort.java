package com.delard.renfe.navigation.domain.port.output;

import com.delard.renfe.navigation.domain.model.Train;
import java.util.List;

/**
 * Output port for train scraping operations
 */
public interface TrainScraperPort {

    /**
     * Scrape train information from Renfe website
     *
     * @param origin Station origin
     * @param destination Station destination
     * @param dateOut Outbound date
     * @param dateReturn Optional return date
     * @param adults Number of adults
     * @return List with [trainsOut, trainsReturn]
     */
    List<List<Train>> scrapeTrains(String origin, String destination, String dateOut,
                                    String dateReturn, int adults);
}

