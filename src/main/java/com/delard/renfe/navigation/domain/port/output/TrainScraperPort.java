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
     * @param origin Station origin name (stationNamePlano)
     * @param destination Station destination name (stationNamePlano)
     * @param originDesgEstacion Origin station description (desgEstacion) for form submission
     * @param destinationDesgEstacion Destination station description (desgEstacion) for form submission
     * @param originClave Origin station key (clave) for form submission
     * @param destinationClave Destination station key (clave) for form submission
     * @param dateOut Outbound date (formatted as dd/MM/yyyy)
     * @param dateReturn Optional return date (formatted as dd/MM/yyyy)
     * @param adults Number of adults
     * @return List with [trainsOut, trainsReturn]
     */
    List<List<Train>> scrapeTrains(String origin, String destination,
                                    String originDesgEstacion, String destinationDesgEstacion,
                                    String originClave, String destinationClave,
                                    String dateOut, String dateReturn, int adults);
}

