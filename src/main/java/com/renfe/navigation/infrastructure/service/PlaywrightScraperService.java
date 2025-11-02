package com.renfe.navigation.infrastructure.service;

import com.renfe.navigation.domain.model.Train;
import com.renfe.navigation.infrastructure.config.PlaywrightConfig;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.*;

/**
 * Service for scraping trains from Renfe website using Playwright
 * Translated from Python search_trains_service.py
 */
@ApplicationScoped
public class PlaywrightScraperService {

    private static final Logger LOG = Logger.getLogger(PlaywrightScraperService.class);

    @Inject
    PlaywrightConfig config;

    @Inject
    RenfeCommonService renfeCommonService;

    @Inject
    TrainHtmlParser trainHtmlParser;

    @Inject
    ResponseStorageService responseStorageService;

    /**
     * Perform a train search using Renfe's website with Playwright browser automation
     *
     * @param origin Origin station name
     * @param destination Destination station name
     * @param dateOut Outbound date (YYYY-MM-DD)
     * @param dateReturn Optional return date (YYYY-MM-DD)
     * @param adults Number of adult passengers
     * @return Tuple with (outbound_trains, return_trains) where return_trains can be null
     */
    public SearchTrainsResult searchTrains(String origin, String destination, String dateOut,
                                           String dateReturn, int adults) {
        LOG.infof("[SCRAPER] Starting Chromium browser");

        // Find stations in catalog
        Map<String, String> originStation = renfeCommonService.findStation(origin);
        Map<String, String> destStation = renfeCommonService.findStation(destination);

        LOG.infof("[SCRAPER] Origin: %s - Key: %s",
                originStation.getOrDefault("desgEstacion", origin),
                originStation.getOrDefault("clave", ""));
        LOG.infof("[SCRAPER] Destination: %s - Key: %s",
                destStation.getOrDefault("desgEstacion", destination),
                destStation.getOrDefault("clave", ""));

        // Convert dates from YYYY-MM-DD to DD/MM/YYYY
        String dateOutFormatted = renfeCommonService.formatDate(dateOut);
        String dateReturnFormatted = "";

        if (dateReturn != null && !dateReturn.isEmpty()) {
            dateReturnFormatted = renfeCommonService.formatDate(dateReturn);
        }

        // Build form data
        Map<String, String> formData = buildFormData(
                originStation, destStation, dateOutFormatted, dateReturnFormatted, adults
        );

        LOG.infof("[SCRAPER] Search parameters: %s -> %s%s",
                dateOutFormatted,
                dateReturnFormatted.isEmpty() ? "One way only" : dateReturnFormatted,
                "");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = createBrowser(playwright);

            try {
                BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                        .setLocale(config.getLocale())
                        .setViewportSize(config.getViewportWidth(), config.getViewportHeight())
                );

                Page page = context.newPage();

                try {
                    LOG.infof("[SCRAPER] Navigating to %s", config.getRenfeSearchUrl());
                    page.navigate(config.getRenfeSearchUrl(), new Page.NavigateOptions()
                            .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    );

                    // Send form via JavaScript
                    String jsFormSubmit = buildFormSubmitScript(formData);
                    page.evaluate(jsFormSubmit);

                    LOG.info("[SCRAPER] Waiting for server response...");
                    page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions()
                            .setTimeout(30000)
                    );

                    // Save HTML response
                    String responseContent = page.content();
                    responseStorageService.saveResponse(responseContent, 200);

                    LOG.info("[SCRAPER] Extracting outbound results");
                    List<Train> trainsOut = extractResults(page);

                    List<Train> trainsRet = null;
                    if (!dateReturnFormatted.isEmpty() && !trainsOut.isEmpty()) {
                        try {
                            LOG.info("[SCRAPER] Finding return results");
                            Locator vueltaTab = page.locator("[id*='vuelta'], [class*='vuelta'], a:has-text('Vuelta')");

                            if (vueltaTab.count() > 0) {
                                vueltaTab.first().click();
                                page.waitForTimeout(500);

                                LOG.info("[SCRAPER] Extracting return results");
                                trainsRet = extractResults(page);
                            }
                        } catch (Exception e) {
                            LOG.warnf("[SCRAPER] Could not extract return trains: %s", e.getMessage());
                            trainsRet = null;
                        }
                    }

                    LOG.info("[SCRAPER] Closing browser");
                    return new SearchTrainsResult(trainsOut, trainsRet);

                } finally {
                    page.close();
                    context.close();
                }
            } finally {
                browser.close();
            }

        } catch (Exception e) {
            LOG.errorf(e, "[SCRAPER] Error during scraping");
            throw new RuntimeException("Error scraping trains: " + e.getMessage(), e);
        }
    }

    /**
     * Create and launch Chromium browser with configured settings
     */
    private Browser createBrowser(Playwright playwright) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(config.isHeadless())
                .setSlowMo(config.getSlowMo())
        );
    }

    /**
     * Build form data map for Renfe search
     */
    private Map<String, String> buildFormData(Map<String, String> originStation,
                                              Map<String, String> destStation,
                                              String dateOutFormatted,
                                              String dateReturnFormatted,
                                              int adults) {
        Map<String, String> formData = new LinkedHashMap<>();

        formData.put("tipoBusqueda", "autocomplete");
        formData.put("currenLocation", "menuBusqueda");
        formData.put("vengoderenfecom", "SI");
        formData.put("desOrigen", originStation.getOrDefault("desgEstacion", ""));
        formData.put("desDestino", destStation.getOrDefault("desgEstacion", ""));
        formData.put("cdgoOrigen", originStation.getOrDefault("clave", ""));
        formData.put("cdgoDestino", destStation.getOrDefault("clave", ""));
        formData.put("idiomaBusqueda", "ES");
        formData.put("FechaIdaSel", dateOutFormatted);
        formData.put("FechaVueltaSel", dateReturnFormatted);
        formData.put("_fechaIdaVisual", dateOutFormatted);
        formData.put("_fechaVueltaVisual", dateReturnFormatted);
        formData.put("minPriceDeparture", "false");
        formData.put("minPriceReturn", "false");
        formData.put("adultos_", String.valueOf(adults));
        formData.put("ninos_", "0");
        formData.put("ninosMenores", "0");
        formData.put("codPromocional", "");
        formData.put("plazaH", "false");
        formData.put("sinEnlace", "false");
        formData.put("conMascota", "false");
        formData.put("conBicicleta", "false");
        formData.put("asistencia", "false");
        formData.put("franjaHoraI", "");
        formData.put("franjaHoraV", "");
        formData.put("Idioma", "es");
        formData.put("Pais", "ES");

        return formData;
    }

    /**
     * Build JavaScript for form submission
     */
    private String buildFormSubmitScript(Map<String, String> formData) {
        StringBuilder sb = new StringBuilder();
        sb.append("const form = document.createElement('form');");
        sb.append("form.method = 'POST';");
        sb.append("form.action = '").append(config.getRenfeSearchUrl()).append("';");
        sb.append("const params = ").append(mapToJsonString(formData)).append(";");
        sb.append("for (const [key, value] of Object.entries(params)) {");
        sb.append("  const input = document.createElement('input');");
        sb.append("  input.type = 'hidden';");
        sb.append("  input.name = key;");
        sb.append("  input.value = value;");
        sb.append("  form.appendChild(input);");
        sb.append("}");
        sb.append("document.body.appendChild(form);");
        sb.append("form.submit();");

        return sb.toString();
    }

    /**
     * Convert map to JSON string for JavaScript
     */
    private String mapToJsonString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("'").append(entry.getKey()).append("':'").append(escapeJsonString(entry.getValue())).append("'");
            first = false;
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Escape special characters in JSON string
     */
    private String escapeJsonString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /**
     * Extract page HTML content and parse it using the parser
     */
    private List<Train> extractResults(Page page) throws InterruptedException {
        LOG.info("[SCRAPER] Waiting for results to load...");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        String html = page.content();

        // Use parser to extract trains
        List<Train> trains = trainHtmlParser.parseTrainList(html);
        LOG.infof("[PARSER] Extracted %d trains", trains.size());

        return trains;
    }

    /**
     * Result class for train search
     */
    public static class SearchTrainsResult {
        public final List<Train> outboundTrains;
        public final List<Train> returnTrains;

        public SearchTrainsResult(List<Train> outboundTrains, List<Train> returnTrains) {
            this.outboundTrains = outboundTrains;
            this.returnTrains = returnTrains;
        }
    }
}

