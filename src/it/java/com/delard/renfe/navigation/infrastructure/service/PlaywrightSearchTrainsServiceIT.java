package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.support.config.PlaywrightRealProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.jboss.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@TestProfile(PlaywrightRealProfile.class)
class PlaywrightSearchTrainsServiceIT {

    private static final Logger LOG = Logger.getLogger(PlaywrightSearchTrainsServiceIT.class);

    @Inject
    PlaywrightSearchTrainsService playwrightSearchTrainsService;

    @Test
    void shouldRetrieveOutboundTrainsFromRenfe() {
        PlaywrightSearchTrainsService.SearchTrainsResult result = playwrightSearchTrainsService.searchTrains(
            "OURENSE", "MADRID", "2025-12-15", null, 1
        );

        LOG.infof("E2E outbound trains count: %d", result.outboundTrains != null ? result.outboundTrains.size() : 0);
        LOG.debugf("E2E outbound trains detail: %s", result.outboundTrains);
        LOG.infof("E2E SearchTrainsResult toString: %s", result.toString());

        assertNotNull(result);
        assertNotNull(result.outboundTrains, "Expected outbound trains list to be initialized");
        assertFalse(result.outboundTrains.isEmpty(), "Expected at least one outbound train in E2E run");
    }
}


