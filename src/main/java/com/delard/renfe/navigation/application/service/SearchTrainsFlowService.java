package com.delard.renfe.navigation.application.service;

import com.delard.renfe.navigation.domain.model.FlowResponse;
import com.delard.renfe.navigation.domain.port.input.SearchTrainsFlowUseCase;
import com.delard.renfe.navigation.domain.port.output.FlowScraperPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;

/**
 * Application service for executing train search flow
 */
@ApplicationScoped
public class SearchTrainsFlowService implements SearchTrainsFlowUseCase {

    private static final Logger LOG = Logger.getLogger(SearchTrainsFlowService.class);

    @Inject
    FlowScraperPort flowScraperPort;

    @Override
    public FlowResponse searchTrainsFlow(String origin, String destination, String dateOut,
                                         String dateReturn, int adults) {
        Instant startTime = Instant.now();

        LOG.infof("[FLOW REQUEST] Starting flow: %s -> %s, Outbound: %s, Return: %s, Passengers: %d",
                origin, destination, dateOut, dateReturn, adults);

        try {
            String filepath = flowScraperPort.executeFlow(
                    origin, destination, dateOut, dateReturn, adults);

            Duration elapsed = Duration.between(startTime, Instant.now());
            LOG.infof("[FLOW SUCCESS] Flow completed in %.2fs - File saved: %s",
                    elapsed.toMillis() / 1000.0, filepath);

            return new FlowResponse("Flow completed successfully", filepath);

        } catch (Exception e) {
            Duration elapsed = Duration.between(startTime, Instant.now());
            LOG.errorf(e, "[FLOW ERROR] Flow failed after %.2fs: %s",
                    elapsed.toMillis() / 1000.0, e.getMessage());
            throw new RuntimeException("Error executing flow: " + e.getMessage(), e);
        }
    }
}

