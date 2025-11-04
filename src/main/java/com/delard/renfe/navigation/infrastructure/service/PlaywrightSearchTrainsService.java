package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.infrastructure.config.PlaywrightConfig;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
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
    RenfeCommonService renfeCommonService;

    @Inject
    TrainHtmlParser trainHtmlParser;

    @Inject
    ResponseStorageService responseStorageService;

    @Inject
    PlaywrightFactory playwrightFactory;

    public SearchTrainsResult searchTrains(String origin, String destination, String dateOut,
                                           String dateReturn, int adults) {
        LOG.infof("Starting Chromium browser");

        // TODO: que pasa en caso de que haya mas de una estación con la misma cadena?
        Map<String, String> originStation = renfeCommonService.findStation(origin);
        Map<String, String> destStation = renfeCommonService.findStation(destination);

        LOG.infof("Origin: %s - Key: %s",
                originStation.getOrDefault("desgEstacion", origin),
                originStation.getOrDefault("clave", ""));
        LOG.infof("Destination: %s - Key: %s",
                destStation.getOrDefault("desgEstacion", destination),
                destStation.getOrDefault("clave", ""));

        String dateOutFormatted = renfeCommonService.formatDate(dateOut);
        String dateReturnFormatted = "";
        if (dateReturn != null && !dateReturn.isEmpty()) {
            dateReturnFormatted = renfeCommonService.formatDate(dateReturn);
        }

        Map<String, String> formData = buildFormData(
                originStation, destStation, dateOutFormatted, dateReturnFormatted, adults
        );

        LOG.infof("Search parameters: %s -> %s",
                dateOutFormatted,
                dateReturnFormatted.isEmpty() ? "One way only" : dateReturnFormatted
        );

        try (Playwright playwright = playwrightFactory.create()) {
            Browser browser = createBrowser(playwright);
            try {
                BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                        .setLocale(config.getLocale())
                        .setViewportSize(config.getViewportWidth(), config.getViewportHeight())
                );

                Page page = context.newPage();
                try {
                    LOG.infof("Navigating to %s", config.getRenfeSearchUrl());
                    page.navigate(config.getRenfeSearchUrl(), new Page.NavigateOptions()
                            .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                            .setTimeout(config.getNavigationTimeoutMs())
                    );

                    String jsFormSubmit = buildFormSubmitScript(formData);
                    page.evaluate(jsFormSubmit);

                    LOG.info("Waiting for server response (network idle)...");
                    page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions()
                            .setTimeout(config.getNetworkIdleTimeoutMs())
                    );

                    String responseContent = page.content();
                    responseStorageService.saveResponse(responseContent, 200);

                    LOG.info("Extracting outbound results");
                    List<Train> trainsOut = extractResults(page);

                    List<Train> trainsRet = null;
                    if (!dateReturnFormatted.isEmpty() && !trainsOut.isEmpty()) {
                        try {
                            LOG.info("Finding return results");
                            Locator vueltaTab = page.locator("[id*='vuelta'], [class*='vuelta'], a:has-text('Vuelta')");
                            if (vueltaTab.count() > 0) {
                                vueltaTab.first().click();
                                page.waitForTimeout(config.getShortTimeoutMs());

                                LOG.info("Extracting return results");
                                trainsRet = extractResults(page);
                            }
                        } catch (Exception e) {
                            LOG.warnf(e, "Could not extract return trains: %s", e.getMessage());
                            trainsRet = null;
                        }
                    }

                    LOG.info("Closing browser");
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

    private List<Train> extractResults(Page page) throws InterruptedException {
        LOG.info("Waiting for results to load...");
        page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(config.getNetworkIdleTimeoutMs()));
        String html = page.content();
        List<Train> trains = trainHtmlParser.parseTrainList(html);
        LOG.infof("[PARSER] Extracted %d trains", trains.size());
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
            return String.format("%s %s-%s %.2f€",
                valueOrDefault(train.getTrainId(), "(sin-id)"),
                valueOrDefault(train.getDepartureTime(), "--"),
                valueOrDefault(train.getArrivalTime(), "--"),
                train.getPriceFrom());
        }

        private String valueOrDefault(String value, String fallback) {
            return (value == null || value.isBlank()) ? fallback : value;
        }
    }
}

