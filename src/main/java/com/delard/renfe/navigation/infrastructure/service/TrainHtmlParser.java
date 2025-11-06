package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.domain.model.FareOption;
import com.delard.renfe.navigation.domain.model.Train;
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
     * @throws IllegalArgumentException if htmlContent is null
     * @throws RuntimeException if there's a critical error parsing the HTML document
     */
    public List<Train> parseTrainList(String htmlContent) {
        if (htmlContent == null) {
            throw new IllegalArgumentException("HTML content cannot be null");
        }

        List<Train> trains = new ArrayList<>();

        try {
            Document doc = Jsoup.parse(htmlContent);

            // Find all train rows
            Elements trainRows = doc.select("div.selectedTren[role='listitem']");
            LOG.debugf("[PARSER] Found %d train rows", trainRows.size());

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
                    // Continue processing other trains even if one fails
                }
            }

        } catch (IllegalArgumentException e) {
            // Re-throw IllegalArgumentException (e.g., null HTML)
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "[PARSER] Critical error parsing HTML document");
            throw new RuntimeException("Failed to parse HTML content: " + e.getMessage(), e);
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
        // HTML format: alt="Imagen de Tren. Tipo de tren AVE"
        Element img = row.selectFirst("img[alt*='Tipo de tren']");
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
        // HTML format: title="Precio desde 63,10"
        Element precioElem = row.selectFirst("span.precio-final");
        if (precioElem != null && precioElem.hasAttr("title")) {
            Pattern pattern = Pattern.compile("Precio desde\\s+([\\d,]+)|([\\d,]+)");
            Matcher matcher = pattern.matcher(precioElem.attr("title"));
            if (matcher.find()) {
                String priceStr = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                double price = Double.parseDouble(priceStr.replace(",", "."));
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
        // HTML format: div with class "seleccion-resumen-bottom" and "card" inside div.planes-opciones
        // The selector needs to match elements that have both classes (space-separated)
        Element planesOpciones = row.selectFirst("div.planes-opciones");
        Elements fareCards = new Elements();
        
        if (planesOpciones != null) {
            // Select divs that have both "seleccion-resumen-bottom" and "card" classes
            // Using attribute selector to match class values containing both strings
            fareCards = planesOpciones.select("div[class*='seleccion-resumen-bottom'][class*='card']");
            
            // Fallback: if still empty, try selecting by role="button" which fare cards have
            if (fareCards.isEmpty()) {
                fareCards = planesOpciones.select("div[role='button'][class*='seleccion-resumen-bottom']");
            }
        }
        
        // If still no fares found, try direct selection from row
        if (fareCards.isEmpty()) {
            fareCards = row.select("div[class*='seleccion-resumen-bottom'][class*='card']");
        }
        
        LOG.debugf("[PARSER] Found %d fare cards for train %s", fareCards.size(), trainId);
        
        for (int i = 0; i < fareCards.size(); i++) {
            try {
                FareOption fare = parseFareCard(fareCards.get(i), trainId);
                if (fare != null) {
                    // Get the fares list and add to it (getFares returns a copy, so we need to get-set)
                    List<FareOption> fares = train.getFares();
                    fares.add(fare);
                    train.setFares(fares);
                    LOG.debugf("[PARSER] Successfully parsed fare %d for train %s: %s (%.2f€)", 
                            i, trainId, fare.getName(), fare.getPrice());
                } else {
                    LOG.warnf("[PARSER] parseFareCard returned null for fare %d of train %s", i, trainId);
                }
            } catch (Exception e) {
                LOG.warnf(e, "[PARSER] Error extracting fare %d for train %s: %s", i, trainId, e.getMessage());
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

        // Fare name - try multiple sources
        // 1. First try data-titulo-tarifa attribute (most reliable)
        if (fareCard.hasAttr("data-titulo-tarifa")) {
            String tituloTarifa = fareCard.attr("data-titulo-tarifa").trim();
            if (!tituloTarifa.isEmpty()) {
                fare.setName(tituloTarifa);
            }
        }
        
        // 2. If not found, try card-header with span[style*='padding-right']
        if (fare.getName() == null || fare.getName().isEmpty()) {
            Element header = fareCard.selectFirst("div.card-header");
            if (header != null) {
                Element nameSpan = header.selectFirst("span[style*='padding-right']");
                if (nameSpan != null) {
                    fare.setName(nameSpan.text().trim());
                } else {
                    // Fallback: extract text before the price (e.g., "Prémium" or "Básico")
                    String headerText = header.text().trim();
                    // Remove price and extract fare name
                    Pattern pattern = Pattern.compile("^([^\\d€]+?)(?:\\s*\\d+[,.]?\\d*\\s*€)?");
                    Matcher matcher = pattern.matcher(headerText);
                    if (matcher.find()) {
                        String name = matcher.group(1).trim();
                        if (!name.isEmpty()) {
                            fare.setName(name);
                        }
                    }
                    
                    // Last resort: use first non-empty text node
                    if (fare.getName() == null || fare.getName().isEmpty()) {
                        String text = header.ownText().trim();
                        if (!text.isEmpty()) {
                            fare.setName(text);
                        }
                    }
                }
            }
        }
        
        // 3. Final fallback: use "Unknown" if still not found
        if (fare.getName() == null || fare.getName().isEmpty()) {
            fare.setName("Unknown");
        }
        
        // Extract fare plan/subtitle (e.g., "Con cambios y anulaciones", "La más completa")
        // These are in span.plan-elige or span.plan-premium within the fare card
        // Look for spans with class starting with "plan" (plan-elige, plan-premium, etc.)
        Element planElem = fareCard.selectFirst("span[class^='plan']");
        if (planElem != null) {
            String planText = planElem.text().trim();
            if (!planText.isEmpty()) {
                fare.setPlan(planText);
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
        // HTML format: <ul class="lista-opciones">...<li>Feature text</li>...
        // Also check for list-group-flush which is used in some cases
        Elements featureElements = fareCard.select("ul.lista-opciones li, ul.list-group li, ul.list-group-flush li");
        List<String> features = fare.getFeatures();
        for (Element feature : featureElements) {
            String featureText = feature.text().trim();
            if (!featureText.isEmpty()) {
                features.add(featureText);
            }
        }
        fare.setFeatures(features);
        
        LOG.debugf("[PARSER] Extracted %d features for fare %s of train %s", 
                fare.getFeatures().size(), fare.getName(), trainId);

        return fare;
    }
}

