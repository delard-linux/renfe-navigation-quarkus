package com.renfe.navigation.domain.port.output;

/**
 * Output port for flow scraping operations
 */
public interface FlowScraperPort {

    /**
     * Execute complete flow from homepage to train search
     *
     * @param origin Station origin
     * @param destination Station destination
     * @param dateOut Outbound date
     * @param dateReturn Optional return date
     * @param adults Number of adults
     * @return Filepath of the result
     */
    String executeFlow(String origin, String destination, String dateOut,
                       String dateReturn, int adults);
}

