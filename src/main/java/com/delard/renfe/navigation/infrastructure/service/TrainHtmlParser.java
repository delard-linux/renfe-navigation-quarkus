package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.domain.model.FareOption;
import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.domain.model.TrainConnection;
import org.jboss.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for extracting train information from Renfe HTML
 * Orchestrates specialized parsers for different parts of the train data
 */
@ApplicationScoped
public class TrainHtmlParser {

    private static final Logger LOG = Logger.getLogger(TrainHtmlParser.class);

    @Inject
    TrainRowParser trainRowParser;

    @Inject
    FareCardParser fareCardParser;

    @Inject
    TrainConnectionParser trainConnectionParser;

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
        // Use TrainRowParser to extract basic train information
        Train train = trainRowParser.parseTrainRow(row, index);
        String trainId = train.getTrainId();

        // Extract available fares using FareCardParser
        extractFares(row, train, trainId);

        // Extract train connection if present using TrainConnectionParser
        TrainConnection connection = trainConnectionParser.parseTrainConnection(row, trainId);
        if (connection != null) {
            train.setConnection(connection);
            LOG.debugf("[PARSER] Found connection for train %s: %s -> %s (duration: %s)", 
                    trainId, connection.getFirstTrainType(), connection.getSecondTrainType(), connection.getDuration());
        }

        return train;
    }

    /**
     * Extract available fares from the train row
     */
    private void extractFares(Element row, Train train, String trainId) {
        // HTML format: div with class "seleccion-resumen-bottom" and "card" inside div.planes-opciones
        Element planesOpciones = row.selectFirst("div.planes-opciones");
        Elements fareCards = new Elements();
        
        if (planesOpciones != null) {
            // Select divs that have both "seleccion-resumen-bottom" and "card" classes
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
                FareOption fare = fareCardParser.parseFareCard(fareCards.get(i), trainId);
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
    }

}

