package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.domain.model.Train;
import org.jboss.logging.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for extracting basic train information from a train row element
 */
@ApplicationScoped
public class TrainRowParser {

    private static final Logger LOG = Logger.getLogger(TrainRowParser.class);

    /**
     * Parse a single train row element and extract basic train information
     * 
     * @param row The train row element
     * @param index The index of the train row (for fallback trainId)
     * @return Train object with basic information populated
     */
    public Train parseTrainRow(Element row, int index) {
        Train train = new Train();

        // Extract train_id from attribute id="tren_i_1"
        String trainId = extractTrainId(row, index);
        train.setTrainId(trainId);

        // Extract service type from train image alt text
        String serviceType = extractServiceType(row);
        if (serviceType != null) {
            train.setServiceType(serviceType);
        }

        // Extract times from h5 elements
        extractTimes(row, train);

        // Extract duration
        extractDuration(row, train);

        // Extract minimum price
        extractPrice(row, train);

        // Extract badges (special labels)
        extractBadges(row, train);

        // Check accessibility and eco-friendly flags
        extractAccessibilityFlags(row, train);

        return train;
    }

    /**
     * Extract train ID from the row element
     */
    private String extractTrainId(Element row, int index) {
        String trainIdAttr = row.id();
        return trainIdAttr != null && !trainIdAttr.isEmpty()
                ? trainIdAttr.replace("tren_", "")
                : "unknown_" + index;
    }

    /**
     * Extract service type from train image alt text
     * HTML format: alt="Imagen de Tren. Tipo de tren AVE"
     */
    private String extractServiceType(Element row) {
        Element img = row.selectFirst("img[alt*='Tipo de tren']");
        if (img != null && img.hasAttr("alt")) {
            Pattern pattern = Pattern.compile("Tipo de tren\\s+(\\w+)");
            Matcher matcher = pattern.matcher(img.attr("alt"));
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    /**
     * Extract departure and arrival times from h5 elements
     */
    private void extractTimes(Element row, Train train) {
        Elements h5Elements = row.select("h5[aria-hidden='true']");
        if (h5Elements.size() >= 2) {
            String departureTime = h5Elements.get(0).text().replace(" h", "").trim();
            String arrivalTime = h5Elements.get(1).text().replace(" h", "").trim();
            train.setDepartureTime(departureTime);
            train.setArrivalTime(arrivalTime);
        }
    }

    /**
     * Extract duration from span.text-number
     */
    private void extractDuration(Element row, Train train) {
        Element durationElem = row.selectFirst("span.text-number");
        if (durationElem != null) {
            train.setDuration(durationElem.text().trim());
        }
    }

    /**
     * Extract minimum price from span.precio-final
     * HTML format: title="Precio desde 63,10"
     */
    private void extractPrice(Element row, Train train) {
        Element precioElem = row.selectFirst("span.precio-final");
        if (precioElem != null && precioElem.hasAttr("title")) {
            Pattern pattern = Pattern.compile("Precio desde\\s+([\\d,]+)|([\\d,]+)");
            Matcher matcher = pattern.matcher(precioElem.attr("title"));
            if (matcher.find()) {
                String priceStr = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                try {
                    double price = Double.parseDouble(priceStr.replace(",", "."));
                    train.setPriceFrom(price);
                } catch (NumberFormatException e) {
                    LOG.warnf("[TRAIN_ROW_PARSER] Invalid price format: %s", priceStr);
                }
            }
        }
    }

    /**
     * Extract badges (special labels) from badge elements
     */
    private void extractBadges(Element row, Train train) {
        Elements badgeElements = row.select(".badge-amarillo-junto, .badge-azul-junto");
        List<String> badges = new ArrayList<>(train.getBadges());
        for (Element badge : badgeElements) {
            String badgeText = badge.text().trim();
            if (!badgeText.isEmpty()) {
                badges.add(badgeText);
            }
        }
        train.setBadges(badges);
    }

    /**
     * Extract accessibility and eco-friendly flags from div.info-varios
     */
    private void extractAccessibilityFlags(Element row, Train train) {
        Element infoVarios = row.selectFirst("div.info-varios");
        if (infoVarios != null) {
            String infoText = infoVarios.text();
            train.setAccessible(infoText.contains("Plaza H disponible"));
            train.setEcoFriendly(infoText.contains("Cero emisiones"));
        }
    }
}

