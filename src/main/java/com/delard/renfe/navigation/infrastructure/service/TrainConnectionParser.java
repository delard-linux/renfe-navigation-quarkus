package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.domain.model.TrainConnection;
import org.jboss.logging.Logger;
import org.jsoup.nodes.Element;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for extracting train connection information from HTML elements
 */
@ApplicationScoped
public class TrainConnectionParser {

    private static final Logger LOG = Logger.getLogger(TrainConnectionParser.class);

    /**
     * Parse train connection information from the train row
     * 
     * @param row The train row element
     * @param trainId The train ID for logging
     * @return TrainConnection if a connection is found, null otherwise
     */
    public TrainConnection parseTrainConnection(Element row, String trainId) {
        try {
            // Find the reorder-trenes-enlaces div
            Element enlacesDiv = row.selectFirst("div.reorder-trenes-enlaces");
            if (enlacesDiv == null) {
                return null;
            }

            // Check if it has aria-hidden="true" - if so, no connection
            String ariaHidden = enlacesDiv.attr("aria-hidden");
            if ("true".equals(ariaHidden)) {
                return null;
            }

            // Check if there's a connection span
            Element enlaceSpan = enlacesDiv.selectFirst("span.enlace-tren");
            if (enlaceSpan == null) {
                return null;
            }

            // Extract connection duration
            String duration = extractConnectionDuration(enlacesDiv, trainId);
            if (duration == null) {
                return null;
            }

            // Extract first train type
            String firstTrainType = extractTrainTypeFromImage(enlacesDiv, "div.principal-tren-enlace");
            if (firstTrainType == null) {
                LOG.warnf("[TRAIN_CONNECTION_PARSER] Connection found for train %s but first train type is missing", trainId);
                return null;
            }

            // Extract second train type
            String secondTrainType = extractTrainTypeFromImage(enlacesDiv, "div.principal-tren-enlace-2");
            if (secondTrainType == null) {
                LOG.warnf("[TRAIN_CONNECTION_PARSER] Connection found for train %s but second train type is missing", trainId);
                return null;
            }

            return new TrainConnection(duration, firstTrainType, secondTrainType);

        } catch (Exception e) {
            LOG.warnf(e, "[TRAIN_CONNECTION_PARSER] Error parsing connection for train %s: %s", trainId, e.getMessage());
            return null;
        }
    }

    /**
     * Extract connection duration from span.enlace-tren-min
     */
    private String extractConnectionDuration(Element enlacesDiv, String trainId) {
        Element durationSpan = enlacesDiv.selectFirst("span.enlace-tren-min");
        if (durationSpan == null) {
            LOG.warnf("[TRAIN_CONNECTION_PARSER] Connection found for train %s but duration span is missing", trainId);
            return null;
        }
        
        String duration = durationSpan.text().trim();
        if (duration.isEmpty()) {
            LOG.warnf("[TRAIN_CONNECTION_PARSER] Connection found for train %s but duration is empty", trainId);
            return null;
        }
        
        return duration;
    }

    /**
     * Extract train type from an image within a specific container div
     * 
     * @param enlacesDiv The parent div containing the train connection elements
     * @param containerSelector The selector for the container div (e.g., "div.principal-tren-enlace")
     * @return The train type if found, null otherwise
     */
    private String extractTrainTypeFromImage(Element enlacesDiv, String containerSelector) {
        Element trainDiv = enlacesDiv.selectFirst(containerSelector);
        if (trainDiv == null) {
            return null;
        }

        Element img = trainDiv.selectFirst("img[alt*='Tipo de tren']");
        if (img == null || !img.hasAttr("alt")) {
            return null;
        }

        Pattern pattern = Pattern.compile("Tipo de tren\\s+(.+)");
        Matcher matcher = pattern.matcher(img.attr("alt"));
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }
}

