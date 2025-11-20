package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.domain.model.FareOption;
import org.jboss.logging.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for extracting fare card information from HTML elements
 */
@ApplicationScoped
public class FareCardParser {

    private static final Logger LOG = Logger.getLogger(FareCardParser.class);

    /**
     * Parse a single fare card element
     * 
     * @param fareCard The fare card element
     * @param trainId The train ID for logging
     * @return FareOption object with fare information
     * @throws Exception if there's an error parsing the fare card
     */
    public FareOption parseFareCard(Element fareCard, String trainId) throws Exception {
        FareOption fare = new FareOption();

        // Extract fare name using multiple fallback strategies
        extractFareName(fareCard, fare);

        // Extract fare plan/subtitle
        extractFarePlan(fareCard, fare);

        // Extract fare price
        extractFarePrice(fareCard, fare);

        // Extract fare code
        extractFareCode(fareCard, fare);

        // Extract type of connection code
        extractTpEnlace(fareCard, fare);

        // Extract features / amenities
        extractFeatures(fareCard, fare, trainId);

        return fare;
    }

    /**
     * Extract fare name using multiple fallback strategies
     */
    private void extractFareName(Element fareCard, FareOption fare) {
        // 1. First try data-titulo-tarifa attribute (most reliable)
        if (fareCard.hasAttr("data-titulo-tarifa")) {
            String tituloTarifa = fareCard.attr("data-titulo-tarifa").trim();
            if (!tituloTarifa.isEmpty()) {
                fare.setName(tituloTarifa);
                return;
            }
        }

        // 2. If not found, try card-header with span[style*='padding-right']
        Element header = fareCard.selectFirst("div.card-header");
        if (header != null) {
            Element nameSpan = header.selectFirst("span[style*='padding-right']");
            if (nameSpan != null) {
                fare.setName(nameSpan.text().trim());
                return;
            }

            // Fallback: extract text before the price (e.g., "Prémium" or "Básico")
            String headerText = header.text().trim();
            Pattern pattern = Pattern.compile("^([^\\d€]+?)(?:\\s*\\d+[,.]?\\d*\\s*€)?");
            Matcher matcher = pattern.matcher(headerText);
            if (matcher.find()) {
                String name = matcher.group(1).trim();
                if (!name.isEmpty()) {
                    fare.setName(name);
                    return;
                }
            }

            // Last resort: use first non-empty text node
            String text = header.ownText().trim();
            if (!text.isEmpty()) {
                fare.setName(text);
                return;
            }
        }

        // 3. Final fallback: use "Unknown" if still not found
        if (fare.getName() == null || fare.getName().isEmpty()) {
            fare.setName("Unknown");
        }
    }

    /**
     * Extract fare plan/subtitle (e.g., "Con cambios y anulaciones", "La más completa")
     */
    private void extractFarePlan(Element fareCard, FareOption fare) {
        Element planElem = fareCard.selectFirst("span[class^='plan']");
        if (planElem != null) {
            String planText = planElem.text().trim();
            if (!planText.isEmpty()) {
                fare.setPlan(planText);
            }
        }
    }

    /**
     * Extract fare price from data-precio-tarifa attribute
     */
    private void extractFarePrice(Element fareCard, FareOption fare) {
        if (fareCard.hasAttr("data-precio-tarifa")) {
            try {
                double price = Double.parseDouble(
                        fareCard.attr("data-precio-tarifa").replace(",", ".")
                );
                fare.setPrice(price);
            } catch (NumberFormatException e) {
                LOG.warnf("[FARE_CARD_PARSER] Invalid price format: %s", 
                        fareCard.attr("data-precio-tarifa"));
            }
        }
    }

    /**
     * Extract fare code from data-cod-tarifa attribute
     */
    private void extractFareCode(Element fareCard, FareOption fare) {
        if (fareCard.hasAttr("data-cod-tarifa")) {
            fare.setCode(fareCard.attr("data-cod-tarifa"));
        }
    }

    /**
     * Extract type of connection code from data-cod-tpenlacesilencio attribute
     */
    private void extractTpEnlace(Element fareCard, FareOption fare) {
        if (fareCard.hasAttr("data-cod-tpenlacesilencio")) {
            fare.setTpEnlace(fareCard.attr("data-cod-tpenlacesilencio"));
        }
    }

    /**
     * Extract features / amenities from list elements
     */
    private void extractFeatures(Element fareCard, FareOption fare, String trainId) {
        Elements featureElements = fareCard.select("ul.lista-opciones li, ul.list-group li, ul.list-group-flush li");
        List<String> features = fare.getFeatures();
        for (Element feature : featureElements) {
            String featureText = feature.text().trim();
            if (!featureText.isEmpty()) {
                features.add(featureText);
            }
        }
        fare.setFeatures(features);
        
        LOG.debugf("[FARE_CARD_PARSER] Extracted %d features for fare %s of train %s", 
                fare.getFeatures().size(), fare.getName(), trainId);
    }
}

