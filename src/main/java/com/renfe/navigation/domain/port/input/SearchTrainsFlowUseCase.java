package com.renfe.navigation.domain.port.input;

import com.renfe.navigation.domain.model.FlowResponse;

/**
 * Input port for executing the complete train search flow
 */
public interface SearchTrainsFlowUseCase {

    /**
     * Performs the complete flow from Renfe's homepage to train search
     *
     * @param origin Station origin (e.g., "OURENSE")
     * @param destination Station destination (e.g., "MADRID")
     * @param dateOut Outbound date in format YYYY-MM-DD
     * @param dateReturn Optional return date in format YYYY-MM-DD
     * @param adults Number of adult passengers (1-8)
     * @return FlowResponse with completion message and filepath
     */
    FlowResponse searchTrainsFlow(String origin, String destination, String dateOut,
                                  String dateReturn, int adults);
}

