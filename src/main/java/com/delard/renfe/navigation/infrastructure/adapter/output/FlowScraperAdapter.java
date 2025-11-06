package com.delard.renfe.navigation.infrastructure.adapter.output;

import com.delard.renfe.navigation.domain.port.output.FlowScraperPort;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

/**
 * Adapter for executing the complete flow from Renfe homepage to search
 * This is a placeholder implementation - actual flow logic to be implemented
 */
@ApplicationScoped
public class FlowScraperAdapter implements FlowScraperPort {

    private static final Logger LOG = Logger.getLogger(FlowScraperAdapter.class);

    @Override
    public String executeFlow(String origin, String destination, String dateOut,
                              String dateReturn, int adults) {
        LOG.debugf("Executing flow: %s -> %s, dateOut: %s, dateReturn: %s, adults: %d",
                origin, destination, dateOut, dateReturn, adults);

        // TODO: Implement actual flow execution using Playwright or similar
        // This is a placeholder that returns a dummy filepath

        String filepath = "/tmp/renfe_flow_result.html";

        LOG.warn("FlowScraperAdapter: Implementation pending - returning placeholder filepath");

        return filepath;
    }
}

