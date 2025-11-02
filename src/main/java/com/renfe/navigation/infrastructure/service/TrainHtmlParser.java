package com.renfe.navigation.infrastructure.service;

import com.renfe.navigation.domain.model.FareOption;
import com.renfe.navigation.domain.model.Train;
import org.jboss.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for extracting train information from Renfe HTML
 * Translated from Python parser.py
 */
@ApplicationScoped
public class TrainHtmlParser {

    private static final Logger LOG = Logger.getLogger(TrainHtmlParser.class);

    /**
     * Parse the HTML content of a Renfe train list (outbound or return)
     *
     * @param htmlContent HTML content of the page that holds the train list
     * @return List of Train objects with full details for each train
     */
    public List<Train> parseTrainList(String htmlContent) {
        List<Train> trains = new ArrayList<>();

        try {
            Document doc = Jsoup.parse(htmlContent);

            // Find all train rows
            Elements trainRows = doc.select("div.selectedTren[role='listitem']");
            LOG.infof("[PARSER] Found %d train rows", trainRows.size());

            int trainIndex = 0;
            for (Element row : trainRows) {
                try {
                    Train train = parseTrainRow(row, trainIndex);
                    if (train != null) {
                        trains.add(train);
                    }
                    trainIndex++;
                } catch (Exception e) {
                    LOG.warnf(e, "[PARSER] Error extracting train at index %d", trainIndex);
                }
            }

        } catch (Exception e) {
            LOG.errorf(e, "[PARSER] Error parsing HTML");
        }

        return trains;
    }

    /**
     * Parse a single train row element
     */
    private Train parseTrainRow(Element row, int index) throws Exception {
        Train train = new Train();

        // Extract train_id from attribute id="tren_i_1"
        String trainIdAttr = row.id();
        String trainId = trainIdAttr != null && !trainIdAttr.isEmpty()
                ? trainIdAttr.replace("tren_", "")
                : "unknown_" + index;
        train.setTrainId(trainId);

        // Extract service type from train image alt text
        Element img = row.selectFirst("img[alt~=Tipo][alt~=de][alt~=tren]");
        if (img != null && img.hasAttr("alt")) {
            Pattern pattern = Pattern.compile("Tipo de tren\\s+(\\w+)");
            Matcher matcher = pattern.matcher(img.attr("alt"));
            if (matcher.find()) {
                train.setServiceType(matcher.group(1));
            }
        }

        // Extract times from h5 elements
        Elements h5Elements = row.select("h5[aria-hidden='true']");
        if (h5Elements.size() >= 2) {
            String departureTime = h5Elements.get(0).text().replace(" h", "").trim();
            String arrivalTime = h5Elements.get(1).text().replace(" h", "").trim();
            train.setDepartureTime(departureTime);
            train.setArrivalTime(arrivalTime);
        }

        // Extract duration
        Element durationElem = row.selectFirst("span.text-number");
        if (durationElem != null) {
            train.setDuration(durationElem.text().trim());
        }

        // Extract minimum price
        Element precioElem = row.selectFirst("span.precio-final");
        if (precioElem != null && precioElem.hasAttr("title")) {
            Pattern pattern = Pattern.compile("([\\d,]+)");
            Matcher matcher = pattern.matcher(precioElem.attr("title"));
            if (matcher.find()) {
                double price = Double.parseDouble(matcher.group(1).replace(",", "."));
                train.setPriceFrom(price);
            }
        }

        // Extract badges (special labels)
        Elements badgeElements = row.select(".badge-amarillo-junto, .badge-azul-junto");
        for (Element badge : badgeElements) {
            String badgeText = badge.text().trim();
            if (!badgeText.isEmpty()) {
                train.getBadges().add(badgeText);
            }
        }

        // Extract available fares
        Elements fareCards = row.select("div[class*='seleccion-resumen-bottom'][class*='card']");
        for (int i = 0; i < fareCards.size(); i++) {
            try {
                FareOption fare = parseFareCard(fareCards.get(i), trainId);
                if (fare != null) {
                    train.getFares().add(fare);
                }
            } catch (Exception e) {
                LOG.warnf(e, "[PARSER] Error extracting fare %d for train %s", i, trainId);
            }
        }

        // Check accessibility and eco-friendly flags
        Element infoVarios = row.selectFirst("div.info-varios");
        if (infoVarios != null) {
            String infoText = infoVarios.text();
            train.setAccessible(infoText.contains("Plaza H disponible"));
            train.setEcoFriendly(infoText.contains("Cero emisiones"));
        }

        return train;
    }

    /**
     * Parse a single fare card element
     */
    private FareOption parseFareCard(Element fareCard, String trainId) throws Exception {
        FareOption fare = new FareOption();

        // Fare name
        Element header = fareCard.selectFirst("div.card-header");
        if (header != null) {
            Element nameSpan = header.selectFirst("span[style*='padding-right']");
            if (nameSpan != null) {
                fare.setName(nameSpan.text().trim());
            } else {
                // Fallback: extract text before the price
                String headerText = header.text().trim();
                Pattern pattern = Pattern.compile("^([^\\d€]+)");
                Matcher matcher = pattern.matcher(headerText);
                if (matcher.find()) {
                    fare.setName(matcher.group(1).trim());
                } else {
                    fare.setName("Desconocida");
                }
            }
        }

        // Fare price
        if (fareCard.hasAttr("data-precio-tarifa")) {
            double price = Double.parseDouble(
                    fareCard.attr("data-precio-tarifa").replace(",", ".")
            );
            fare.setPrice(price);
        }

        // Fare code
        if (fareCard.hasAttr("data-cod-tarifa")) {
            fare.setCode(fareCard.attr("data-cod-tarifa"));
        }

        // Type of connection code
        if (fareCard.hasAttr("data-cod-tpenlacesilencio")) {
            fare.setTpEnlace(fareCard.attr("data-cod-tpenlacesilencio"));
        }

        // Features / amenities
        Elements features = fareCard.select("li");
        for (Element feature : features) {
            String featureText = feature.text().trim();
            if (!featureText.isEmpty()) {
                fare.getFeatures().add(featureText);
            }
        }

        return fare;
    }
}

