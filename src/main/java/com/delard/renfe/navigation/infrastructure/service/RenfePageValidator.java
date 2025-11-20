/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.service;


import jakarta.enterprise.context.ApplicationScoped;

import com.delard.renfe.navigation.application.exception.QueueException;
import com.delard.renfe.navigation.application.exception.TrainUnavailabilityException;

import org.jboss.logging.Logger;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;


@ApplicationScoped
public class RenfePageValidator
{

    private static final Logger LOG = Logger.getLogger(RenfePageValidator.class);

    /**
     * Checks if the current page is a queue management page and throws QueueException if detected.
     * This prevents timeouts when the system redirects to a queue page instead of showing train results.
     *
     * @param page The Playwright page to check
     * @throws QueueException if a queue page is detected
     */
    public void checkForQueuePage(Page page)
    {
        try {
            // Wait a short moment for the page to render
            page.waitForTimeout(2000);

            // Get page content for text analysis
            String pageText = "";
            try {
                String bodyText = page.locator("body").textContent();
                if (bodyText != null) {
                    pageText = bodyText.toLowerCase();
                }
            } catch (Exception e) {
                // If we can't get page content, continue with empty strings
                LOG.debugf("Could not get page content for queue check: %s", e.getMessage());
            }

            // Check for queue-related text in Spanish
            boolean hasQueueText = pageText.contains("estás en la cola") ||
                    pageText.contains("estas en la cola") ||
                    pageText.contains("cola para comprar") ||
                    pageText.contains("cuando sea tu turno") ||
                    pageText.contains("te redirigiremos");

            // Check for Queue.it elements using locators
            boolean hasQueueItLocators = false;
            try {
                hasQueueItLocators =
                        page.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']")
                                .count() > 0;
            } catch (Exception e) {
                // Ignore locator errors
            }

            // Check for specific queue page text elements
            boolean hasQueuePageText = false;
            try {
                hasQueuePageText = page.locator("text=/cola/i").count() > 0 ||
                        page.locator("text=/turno/i").count() > 0;
            } catch (Exception e) {
                // Ignore locator errors
            }

            if (hasQueueText || hasQueueItLocators || hasQueuePageText) {
                LOG.warn("Queue page detected - ticket purchase is queued");
                throw new QueueException(
                        "Ticket purchase is queued. The system redirected to a queue management page. Please try again later.");
            }
        } catch (QueueException e) {
            // Re-throw queue exceptions
            throw e;
        } catch (Exception e) {
            // If checking for queue page fails, log and continue (don't block normal flow)
            LOG.debugf("Error checking for queue page (continuing normally): %s", e.getMessage());
        }
    }

    /**
     * Checks if the page contains train unavailability error messages and throws TrainUnavailabilityException if detected.
     * This prevents timeouts when there are no trains available for the requested route/date.
     *
     * @param page The Playwright page to check
     * @param direction The direction being checked ("outbound" or "return")
     * @throws TrainUnavailabilityException if train unavailability is detected
     */
    public void checkForTrainUnavailability(Page page, String direction)
    {
        try {
            // Wait a short moment for error messages to appear
            page.waitForTimeout(1000);

            String errorSelector;
            String tabSelector;

            if ("outbound".equalsIgnoreCase(direction)) {
                // Check for outbound train unavailability
                errorSelector = "#noDispoIda.msjErrorTrenes";
                tabSelector = "#stv-ida";
            } else if ("return".equalsIgnoreCase(direction)) {
                // Check for return train unavailability
                errorSelector = "#noDispoVuelta.msjErrorTrenes";
                tabSelector = "#stv-vuelta";
            } else {
                // Unknown direction, skip check
                return;
            }

            // Check specific error
            checkSpecificError(page, errorSelector, direction);

            // Check generic errors
            checkGenericErrors(page, tabSelector, direction);

        } catch (TrainUnavailabilityException e) {
            // Re-throw train unavailability exceptions
            throw e;
        } catch (Exception e) {
            // If checking for unavailability fails, log and continue (don't block normal flow)
            LOG.debugf("Error checking for train unavailability (continuing normally): %s", e.getMessage());
        }
    }

    private void checkSpecificError(Page page, String errorSelector, String direction)
    {
        // Check if the specific error message exists
        Locator errorElement = page.locator(errorSelector);
        if (errorElement.count() > 0 && errorElement.isVisible()) {
            String errorMessage = errorElement.textContent();
            if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                LOG.warnf("Train unavailability detected for %s: %s", direction, errorMessage.trim());
                throw new TrainUnavailabilityException(direction, errorMessage.trim());
            }
        }
    }

    private void checkGenericErrors(Page page, String tabSelector, String direction)
    {
        // Check for any other error messages with class msjErrorTrenes in the appropriate tab
        Locator tabElement = page.locator(tabSelector);
        if (tabElement.count() > 0) {
            Locator otherErrors = tabElement.locator("p.msjErrorTrenes");
            if (otherErrors.count() > 0) {
                for (int i = 0; i < otherErrors.count(); i++) {
                    Locator errorElem = otherErrors.nth(i);
                    if (errorElem.isVisible()) {
                        String errorMessage = errorElem.textContent();
                        String errorId = errorElem.getAttribute("id");
                        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                            LOG.warnf("Train error detected for %s (id: %s): %s", direction, errorId,
                                    errorMessage.trim());
                            throw new TrainUnavailabilityException(direction, errorMessage.trim());
                        }
                    }
                }
            }
        }
    }
}
