package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.domain.model.FareOption;
import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.infrastructure.config.PlaywrightConfig;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class PlaywrightSearchTrainsService {

    private static final Logger LOG = Logger.getLogger(PlaywrightSearchTrainsService.class);

    @Inject
    PlaywrightConfig config;

    @Inject
    TrainHtmlParser trainHtmlParser;

    @Inject
    ResponseStorageService responseStorageService;

    @Inject
    PlaywrightFactory playwrightFactory;

    public SearchTrainsResult searchTrains(String origin, String destination,
                                           String originDesgEstacion, String destinationDesgEstacion,
                                           String originClave, String destinationClave,
                                           String dateOut, String dateReturn, String adults) {
        LOG.debugf("Starting Chromium browser");

        LOG.debugf("Origin: %s (desgEstacion: %s, clave: %s)", origin, originDesgEstacion, originClave);
        LOG.debugf("Destination: %s (desgEstacion: %s, clave: %s)", destination, destinationDesgEstacion, destinationClave);

        // Dates are already formatted in application layer (dd/MM/yyyy format)
        String dateReturnFormatted = (dateReturn != null && !dateReturn.isEmpty()) ? dateReturn : "";

        Map<String, String> formData = buildFormData(
                originDesgEstacion, destinationDesgEstacion, originClave, destinationClave,
                dateOut, dateReturnFormatted, adults
        );

        LOG.debugf("Search parameters: %s -> %s",
                dateOut,
                dateReturnFormatted.isEmpty() ? "One way only" : dateReturnFormatted
        );

        // Log equivalent curl command for debugging
        String curlCommand = buildCurlCommand(formData);
        LOG.debugf("Equivalent curl command: %s", curlCommand);

        try (Playwright playwright = playwrightFactory.create()) {
            Browser browser = createBrowser(playwright);
            try {
                BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                        .setLocale(config.getLocale())
                        .setViewportSize(config.getViewportWidth(), config.getViewportHeight())
                );

                Page page = context.newPage();
                try {
                    // Build URL with query string parameters
                    String urlWithQueryString = buildUrlWithQueryString(formData);
                    LOG.debugf("Navigating to %s", urlWithQueryString);
                    page.navigate(urlWithQueryString, new Page.NavigateOptions()
                            .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                            .setTimeout(config.getNavigationTimeoutMs())
                    );

                    LOG.debug("Waiting for train results to appear...");
                    // Wait directly for train results instead of NETWORKIDLE (which may timeout on sites with continuous polling)
                    page.waitForSelector("div.selectedTren[role='listitem']", new Page.WaitForSelectorOptions()
                            .setTimeout(config.getTimeoutMs())
                            .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    );
                    
                    LOG.debug("Train results appeared, waiting for content to stabilize...");
                    // Give the page a moment to fully render all dynamic content
                    page.waitForTimeout(1000);

                    // Accept cookies if the banner appears
                    try {
                        Locator acceptCookiesButton = page.locator("#onetrust-accept-btn-handler");
                        // Wait for the button to appear with a short timeout
                        acceptCookiesButton.waitFor(new Locator.WaitForOptions()
                                .setTimeout(config.getShortTimeoutMs())
                                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE));
                        
                        if (acceptCookiesButton.isVisible()) {
                            LOG.debug("Cookie banner detected, clicking 'Accept all cookies' button");
                            acceptCookiesButton.click();
                            // Wait a moment for the cookie banner to close
                            page.waitForTimeout(500);
                        }
                    } catch (Exception e) {
                        // Cookie banner may not appear, continue normally
                        LOG.debugf("Cookie banner not found or already dismissed: %s", e.getMessage());
                    }

                    String responseContent = page.content();
                    responseStorageService.saveResponse(responseContent, 200);

                    LOG.debug("Extracting outbound results");
                    List<Train> trainsOut = extractResults(page);

                    List<Train> trainsRet = null;
                    if (dateReturn != null && !dateReturn.isEmpty() && !trainsOut.isEmpty()) {
                        try {
                            LOG.debug("Finding return results");
                            Locator vueltaTab = page.locator("[id*='vuelta'], [class*='vuelta'], a:has-text('Vuelta')");
                            if (vueltaTab.count() > 0) {
                                vueltaTab.first().click();
                                page.waitForTimeout(config.getShortTimeoutMs());

                                LOG.debug("Extracting return results");
                                trainsRet = extractResults(page);
                            }
                        } catch (Exception e) {
                            LOG.warnf(e, "Could not extract return trains: %s", e.getMessage());
                            trainsRet = null;
                        }
                    }

                    LOG.debug("Closing browser");
                    return new SearchTrainsResult(trainsOut, trainsRet);

                } finally {
                    page.close();
                    context.close();
                }
            } finally {
                browser.close();
            }
        } catch (Exception e) {
            LOG.errorf(e, "Error during scraping");
            throw new RuntimeException("Error scraping trains: " + e.getMessage(), e);
        }
    }

    private Browser createBrowser(Playwright playwright) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(config.isHeadless())
                .setSlowMo(config.getSlowMo())
        );
    }

    private Map<String, String> buildFormData(String originDesgEstacion,
                                              String destinationDesgEstacion,
                                              String originClave,
                                              String destinationClave,
                                              String dateOutFormatted,
                                              String dateReturnFormatted,
                                              String adults) {
        Map<String, String> formData = new LinkedHashMap<>();
        formData.put("tipoBusqueda", "autocomplete");
        formData.put("currenLocation", "menuBusqueda");
        formData.put("vengoderenfecom", "SI");
        formData.put("desOrigen", originDesgEstacion != null ? originDesgEstacion : "");
        formData.put("desDestino", destinationDesgEstacion != null ? destinationDesgEstacion : "");
        formData.put("cdgoOrigen", originClave != null ? originClave : "");
        formData.put("cdgoDestino", destinationClave != null ? destinationClave : "");
        formData.put("idiomaBusqueda", "ES");
        formData.put("FechaIdaSel", dateOutFormatted);
        formData.put("FechaVueltaSel", dateReturnFormatted);
        formData.put("_fechaIdaVisual", dateOutFormatted);
        formData.put("_fechaVueltaVisual", dateReturnFormatted);
        formData.put("minPriceDeparture", "10");
        formData.put("minPriceReturn", "false");
        formData.put("adultos_", adults != null ? adults.trim() : "1");
        formData.put("ninos_", "0");
        formData.put("ninosMenores", "0");
        formData.put("codPromocional", "");
        formData.put("plazaH", "false");
        formData.put("sinEnlace", "false");
        formData.put("conMascota", "false");
        formData.put("conBicicleta", "false");
        formData.put("asistencia", "false");
        formData.put("franjaHoraI", "00:00");
        formData.put("franjaHoraV", "00:00");
        formData.put("Idioma", "es");
        formData.put("Pais", "ES");
        return formData;
    }

    /**
     * Builds a URL with query string parameters from form data.
     *
     * @param formData The form data to convert to query string
     * @return The complete URL with query string parameters
     */
    private String buildUrlWithQueryString(Map<String, String> formData) {
        StringBuilder url = new StringBuilder(config.getRenfeSearchUrl());
        
        // Check if URL already has query parameters
        boolean hasQueryParams = url.indexOf("?") != -1;
        String separator = hasQueryParams ? "&" : "?";
        
        boolean first = true;
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (first) {
                url.append(separator);
                first = false;
            } else {
                url.append("&");
            }
            url.append(escapeUrlParameter(entry.getKey()))
               .append("=")
               .append(escapeUrlParameter(entry.getValue()));
        }
        
        return url.toString();
    }

    /**
     * Builds an equivalent curl command for the query string parameters.
     * This is useful for debugging and understanding what request is being made.
     *
     * @param formData The form data to convert to curl command with query string
     * @return A curl command string equivalent to the GET request with query string
     */
    private String buildCurlCommand(Map<String, String> formData) {
        String urlWithQueryString = buildUrlWithQueryString(formData);
        return "curl '" + urlWithQueryString + "'";
    }

    /**
     * Escapes a string for use in URL parameters.
     *
     * @param str The string to escape
     * @return The escaped string
     */
    private String escapeUrlParameter(String str) {
        if (str == null) {
            return "";
        }
        try {
            // Use URLEncoder to properly encode URL parameters
            return java.net.URLEncoder.encode(str, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            // Fallback to manual escaping if UTF-8 is not supported (should never happen)
            return str.replace(" ", "%20")
                    .replace("&", "%26")
                    .replace("=", "%3D")
                    .replace("'", "%27")
                    .replace("\"", "%22");
        }
    }

    private List<Train> extractResults(Page page) throws InterruptedException {
        LOG.debug("Waiting for train results to be visible...");
        
        // Wait for train results to be visible (skip NETWORKIDLE to avoid timeout on pages with continuous polling)
        page.waitForSelector("div.selectedTren[role='listitem']", new Page.WaitForSelectorOptions()
                .setTimeout(config.getTimeoutMs())
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
        );
        
        // Wait a moment for content to stabilize
        page.waitForTimeout(1000);
        
        String html = page.content();
        List<Train> trains = trainHtmlParser.parseTrainList(html);
        LOG.debugf("[PARSER] Extracted %d trains", trains.size());
        return trains;
    }

    public static class SearchTrainsResult {
        public final List<Train> outboundTrains;
        public final List<Train> returnTrains;

        public SearchTrainsResult(List<Train> outboundTrains, List<Train> returnTrains) {
            this.outboundTrains = outboundTrains;
            this.returnTrains = returnTrains;
        }

        @Override
        public String toString() {
            return "SearchTrainsResult{" +
                "outboundTrains=" + summarize(outboundTrains) +
                ", returnTrains=" + summarize(returnTrains) +
                '}';
        }

        private String summarize(List<Train> trains) {
            if (trains == null || trains.isEmpty()) {
                return "[]";
            }
            return trains.stream()
                .map(this::describeTrain)
                .collect(Collectors.joining(", ", "[", "]"));
        }

        private String describeTrain(Train train) {
            if (train == null) {
                return "null";
            }
            
            String serviceType = valueOrDefault(train.getServiceType(), "(no-type)");
            String timeRange = String.format("%s-%s",
                valueOrDefault(train.getDepartureTime(), "--"),
                valueOrDefault(train.getArrivalTime(), "--"));
            
            String priceRange = getPriceRangeFromFares(train);
            
            return String.format("%s %s %s", serviceType, timeRange, priceRange);
        }
        
        private String getPriceRangeFromFares(Train train) {
            List<FareOption> fares = train.getFares();
            if (fares == null || fares.isEmpty()) {
                // Fallback to priceFrom if no fares available
                return String.format("%.2f€", train.getPriceFrom());
            }
            
            double minPrice = fares.stream()
                .mapToDouble(FareOption::getPrice)
                .min()
                .orElse(train.getPriceFrom());
            
            double maxPrice = fares.stream()
                .mapToDouble(FareOption::getPrice)
                .max()
                .orElse(train.getPriceFrom());
            
            if (minPrice == maxPrice) {
                return String.format("%.2f€", minPrice);
            } else {
                return String.format("%.2f€-%.2f€", minPrice, maxPrice);
            }
        }

        private String valueOrDefault(String value, String fallback) {
            return (value == null || value.isBlank()) ? fallback : value;
        }
    }
}

