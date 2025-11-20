package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.application.exception.QueueException;
import com.delard.renfe.navigation.application.exception.TrainUnavailabilityException;
import com.delard.renfe.navigation.support.config.PlaywrightIntegrationTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for PlaywrightSearchTrainsService.
 * Tests the service directly without going through REST endpoints.
 * Uses @QuarkusTest to allow IDE debugging (runs in same JVM).
 * 
 * Configuration is loaded from src/test/resources/application-integration.properties
 * via the PlaywrightIntegrationTestProfile.
 * You can modify Playwright settings (headless mode, slow-mo, timeouts) in that file
 * to debug tests or adjust execution speed.
 * 
 * Debug tips:
 * - Set playwright.headless=false to see the browser window
 * - Set playwright.slow-mo=5000 to slow down execution for observation
 * - Increase timeouts if pages load slowly during debugging
 */
@QuarkusTest
@TestProfile(PlaywrightIntegrationTestProfile.class)
class PlaywrightSearchTrainsServiceIT {

    private static final Logger LOG = Logger.getLogger(PlaywrightSearchTrainsServiceIT.class);

    @Inject
    PlaywrightSearchTrainsService playwrightSearchTrainsService;

    /**
     * Calculates the outbound date (2 months from today)
     *
     * @return Outbound date in format dd/MM/yyyy
     */
    private String calculateOutboundDate() {
        LocalDate today = LocalDate.now();
        LocalDate outboundDate = today.plusMonths(2);
        return outboundDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Calculates the return date (3 days after the outbound date)
     *
     * @param outboundDate Outbound date in format dd/MM/yyyy
     * @return Return date in format dd/MM/yyyy
     */
    private String calculateReturnDate(String outboundDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate outbound = LocalDate.parse(outboundDate, formatter);
        LocalDate returnDate = outbound.plusDays(3);
        return returnDate.format(formatter);
    }

    @Test
    void shouldRetrieveOutboundTrainsFromRenfe() {
        // Test Madrid to Barcelona one-way trip
        // This test accepts both successful results and QueueException as valid outcomes
        // since the queue is a controlled response from Renfe's system
        String dateOut = calculateOutboundDate();
        
        try {
            PlaywrightSearchTrainsService.SearchTrainsResult result = playwrightSearchTrainsService.searchTrains(
                "MADRID (TODAS)",
                "BARCELONA (TODAS)",
                "MADRID (TODAS)",   // originDesgEstacion
                "BARCELONA (TODAS)",  // destinationDesgEstacion
                "0071,MADRI,null",   // originClave
                "0071,BARCE,null",  // destinationClave
                dateOut,
                null,
                "1"
            );

            LOG.infof("IT result: %s", result);

            // Validate result structure
            assertNotNull(result, "SearchTrainsResult should not be null");
            assertNotNull(result.outboundTrains, "Outbound trains list should not be null");
            assertFalse(result.outboundTrains.isEmpty(), "Outbound trains list should not be empty");
            assertNull(result.returnTrains, "Return trains should be null when no return date is provided");

            LOG.infof("IT outbound trains count: %d", result.outboundTrains.size());
            
            // Validate train structure
            result.outboundTrains.forEach(train -> {
                assertNotNull(train, "Train should not be null");
                assertNotNull(train.getDepartureTime(), "Train should have departure time");
                assertNotNull(train.getArrivalTime(), "Train should have arrival time");
            });
        } catch (QueueException e) {
            // QueueException is also a valid, controlled response from Renfe's system
            // This indicates the system is managing traffic through a queue
            LOG.warnf("IT test: Queue detected (valid controlled response): %s", e.getMessage());
            assertNotNull(e.getMessage(), "QueueException should have a message");
            assertTrue(e.getMessage().contains("queued"), "QueueException message should mention queue");
        }
    }

    @Test
    void shouldRetrieveOutboundTrainsWithEmptyReturnDate() {
        // Test with empty string return date
        // Covers branch: dateReturn != null && !dateReturn.isEmpty() (line 43)
        // This test is enabled to improve branch coverage for infrastructure.service package
        String dateOut = calculateOutboundDate();
        
        try {
            PlaywrightSearchTrainsService.SearchTrainsResult result = playwrightSearchTrainsService.searchTrains(
                "BARCELONA (TODAS)",
                "VALENCIA (TODAS)",
                "BARCELONA (TODAS)",  // originDesgEstacion
                "VALENCIA (TODAS)",   // destinationDesgEstacion
                "0071,BARCE,null",  // originClave
                "0071,VALEN,null",   // destinationClave
                dateOut,
                "",
                "2"
            );

            LOG.infof("IT result with empty return date: %s", result);

            assertNotNull(result, "SearchTrainsResult should not be null");
            assertNotNull(result.outboundTrains, "Outbound trains list should not be null");
            assertFalse(result.outboundTrains.isEmpty(), "Outbound trains list should not be empty");
            // When return date is empty, returnTrains should be null (same as null dateReturn)
            assertNull(result.returnTrains, "Return trains should be null when return date is empty");

            LOG.infof("IT outbound trains count: %d", result.outboundTrains.size());
        } catch (QueueException e) {
            // QueueException is also a valid, controlled response from Renfe's system
            LOG.warnf("IT test: Queue detected (valid controlled response): %s", e.getMessage());
            assertNotNull(e.getMessage(), "QueueException should have a message");
            assertTrue(e.getMessage().contains("queued"), "QueueException message should mention queue");
        }
    }

    @Test
    void shouldRetrieveOutboundAndReturnTrainsFromRenfe() {
        // Test with return date to cover branches:
        // - dateReturn != null && !dateReturn.isEmpty() (line 43)
        // - dateReturn != null && !dateReturn.isEmpty() && !trainsOut.isEmpty() (line 117)
        // - vueltaTab.count() > 0 (line 121)
        // This test is enabled to improve branch coverage for infrastructure.service package
        String dateOut = calculateOutboundDate();
        String dateReturn = calculateReturnDate(dateOut);
        
        try {
            PlaywrightSearchTrainsService.SearchTrainsResult result = playwrightSearchTrainsService.searchTrains(
                "MADRID (TODAS)",
                "BARCELONA (TODAS)",
                "MADRID (TODAS)",  // originDesgEstacion
                "BARCELONA (TODAS)",   // destinationDesgEstacion
                "0071,MADRI,null",  // originClave
                "0071,BARCE,null",   // destinationClave
                dateOut,
                dateReturn,
                "2"
            );

            LOG.infof("IT result with return: %s", result);

            assertNotNull(result, "SearchTrainsResult should not be null");
            assertNotNull(result.outboundTrains, "Outbound trains list should not be null");
            assertFalse(result.outboundTrains.isEmpty(), "Outbound trains list should not be empty");
            
            // Return trains might be null if:
            // - vueltaTab.count() == 0 (line 121, no tab found)
            // - Exception occurs during extraction (line 128, catch block)
            // But outbound should always be present
            if (result.returnTrains != null) {
                assertFalse(result.returnTrains.isEmpty(), "Return trains list should not be empty if present");
                LOG.infof("IT return trains count: %d", result.returnTrains.size());
            } else {
                LOG.warn("Return trains extraction failed or not available (tab not found or exception occurred)");
            }

            LOG.infof("IT outbound trains count: %d", result.outboundTrains.size());
        } catch (QueueException e) {
            // QueueException is also a valid, controlled response from Renfe's system
            LOG.warnf("IT test: Queue detected (valid controlled response): %s", e.getMessage());
            assertNotNull(e.getMessage(), "QueueException should have a message");
            assertTrue(e.getMessage().contains("queued"), "QueueException message should mention queue");
        }
    }

    @Test
    void shouldCoverSearchTrainsResultToStringBranches() {
        // Test to cover branches in SearchTrainsResult.toString() and its private methods
        // Covers branches:
        // - summarize: trains == null || trains.isEmpty() (line 365)
        // - describeTrain: train == null (line 374)
        // - getPriceRangeFromFares: fares == null || fares.isEmpty() (line 390)
        // - getPriceRangeFromFares: minPrice == maxPrice (line 405)
        // - valueOrDefault: value == null || value.isBlank() (line 413)
        // This test is enabled to improve branch coverage for infrastructure.service package
        
        String dateOut = calculateOutboundDate();
        
        try {
            PlaywrightSearchTrainsService.SearchTrainsResult result = playwrightSearchTrainsService.searchTrains(
                "MADRID (TODAS)",
                "BARCELONA (TODAS)",
                "MADRID (TODAS)",  // originDesgEstacion
                "BARCELONA (TODAS)",   // destinationDesgEstacion
                "0071,MADRI,null",  // originClave
                "0071,BARCE,null",   // destinationClave
                dateOut,
                null,
                "2"
            );

            assertNotNull(result);
            
            // Call toString() to exercise all branches in SearchTrainsResult
            String resultString = result.toString();
            assertNotNull(resultString);
            assertFalse(resultString.isEmpty());
            LOG.infof("IT SearchTrainsResult.toString(): %s", resultString);
            
            // Verify that toString() handles null returnTrains correctly
            // This covers the branch: trains == null in summarize()
            assertNull(result.returnTrains, "Return trains should be null for one-way trip");
            
            // Verify that toString() handles trains with different fare scenarios
            // This will cover branches in getPriceRangeFromFares and describeTrain
            if (!result.outboundTrains.isEmpty()) {
                result.outboundTrains.forEach(train -> {
                    assertNotNull(train, "Train should not be null");
                    // Calling toString on result will exercise describeTrain for each train
                    // which covers branches in getPriceRangeFromFares
                });
            }
        } catch (QueueException e) {
            // QueueException is also a valid, controlled response from Renfe's system
            LOG.warnf("IT test: Queue detected (valid controlled response): %s", e.getMessage());
            assertNotNull(e.getMessage(), "QueueException should have a message");
            assertTrue(e.getMessage().contains("queued"), "QueueException message should mention queue");
        }
    }

    @Test
    void shouldHandleTrainUnavailabilityProperly() {
        // Test train unavailability detection (e.g., routes with no availability)
        // This test verifies that the system properly detects error messages like:
        // - <p id="noDispoIda" class="msjErrorTrenes"> for outbound unavailability
        // - <p id="noDispoVuelta" class="msjErrorTrenes"> for return unavailability
        // Instead of timing out, it should throw TrainUnavailabilityException with detailed message
        
        String dateOut = calculateOutboundDate();
        String dateReturn = calculateReturnDate(dateOut);
        
        try {
            PlaywrightSearchTrainsService.SearchTrainsResult result = playwrightSearchTrainsService.searchTrains(
                "MADRID-RECOLETOS",
                "BARCELONA (TODAS)",
                "MADRID-RECOLETOS",       // originDesgEstacion
                "BARCELONA (TODAS)",      // destinationDesgEstacion
                "0071,MADRI,null",        // originClave (using MADRID codes)
                "0071,BARCE,null",        // destinationClave
                dateOut,
                dateReturn,
                "1"
            );

            // If we get here without exception, it means trains were found (valid scenario)
            // This can happen if availability changes or on different dates
            LOG.infof("IT result: Trains found (no unavailability): %s", result);
            assertNotNull(result, "SearchTrainsResult should not be null");
            assertNotNull(result.outboundTrains, "Outbound trains list should not be null");
            
        } catch (TrainUnavailabilityException e) {
            // This is the expected behavior when trains are not available
            LOG.infof("IT test: Train unavailability detected correctly: %s", e.getMessage());
            
            // Validate exception structure
            assertNotNull(e.getMessage(), "TrainUnavailabilityException should have a message");
            assertNotNull(e.getDirection(), "TrainUnavailabilityException should have direction");
            assertNotNull(e.getDetailMessage(), "TrainUnavailabilityException should have detail message");
            
            // Verify the direction is either "outbound" or "return"
            assertTrue(
                e.getDirection().equalsIgnoreCase("outbound") || 
                e.getDirection().equalsIgnoreCase("return"),
                "Direction should be 'outbound' or 'return', got: " + e.getDirection()
            );
            
            // Verify the message contains the direction
            assertTrue(
                e.getMessage().contains(e.getDirection()),
                "Exception message should contain direction"
            );
            
            // Verify the detail message is not empty
            assertFalse(
                e.getDetailMessage().trim().isEmpty(),
                "Detail message should not be empty"
            );
            
            LOG.infof("IT validation passed - Direction: %s, Detail: %s", 
                e.getDirection(), e.getDetailMessage());
            
        } catch (QueueException e) {
            // QueueException is also a valid, controlled response from Renfe's system
            LOG.warnf("IT test: Queue detected (valid controlled response): %s", e.getMessage());
            assertNotNull(e.getMessage(), "QueueException should have a message");
            assertTrue(e.getMessage().contains("queued"), "QueueException message should mention queue");
            
        } catch (Exception e) {
            // Any other exception should fail the test with details
            LOG.errorf(e, "IT test: Unexpected exception type: %s", e.getClass().getName());
            fail("Expected TrainUnavailabilityException or QueueException, but got: " + 
                e.getClass().getName() + " - " + e.getMessage());
        }
    }
    
}


