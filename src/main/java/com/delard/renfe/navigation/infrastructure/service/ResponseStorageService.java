/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.service;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.delard.renfe.navigation.infrastructure.config.PlaywrightConfig;

import org.jboss.logging.Logger;


/**
 * Service for saving HTML and JSON responses from Renfe scraping
 * Translated from Python renfe_common.py
 */
@ApplicationScoped
public class ResponseStorageService
{

    private static final Logger LOG = Logger.getLogger(ResponseStorageService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyMMdd_HHmmss");

    @Inject
    PlaywrightConfig config;

    /**
     * Create responses directory if it doesn't exist
     */
    private void ensureResponsesDir()
    {
        try {
            File dir = new File(config.getResponsesDir());
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (created) {
                    LOG.debugf("[SCRAPER] Created responses directory: %s", config.getResponsesDir());
                } else {
                    LOG.warnf("[SCRAPER] Could not create responses directory: %s", config.getResponsesDir());
                }
            }
        } catch (Exception e) {
            LOG.warnf(e, "[SCRAPER] Error ensuring responses directory");
        }
    }

    /**
     * Save HTML response to file
     * Format: [YYMMDD_HHMMSS]_[StatusCode]_[FileName_Suffix]
     *
     * @param content HTML content
     * @param statusCode HTTP status code
     * @param filenameSuffix Suffix for filename (default: "buscarTren.do.log")
     * @return Path to saved file, or null if failed
     */
    public String saveResponse(String content, int statusCode, String filenameSuffix)
    {
        ensureResponsesDir();

        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String filename = String.format("%s_%d_%s", timestamp, statusCode, filenameSuffix);
            String filepath = Paths.get(config.getResponsesDir(), filename).toString();

            Files.write(
                    Paths.get(filepath),
                    content.getBytes(StandardCharsets.UTF_8));

            LOG.debugf("[SCRAPER] Response saved as: %s", filename);
            return filepath;

        } catch (IOException e) {
            LOG.errorf(e, "[SCRAPER] Error saving response");
            return null;
        }
    }

    /**
     * Overload without status code
     */
    public String saveResponse(String content, String filenameSuffix)
    {
        return saveResponse(content, 200, filenameSuffix);
    }

    /**
     * Overload with default filename
     */
    public String saveResponse(String content, int statusCode)
    {
        return saveResponse(content, statusCode, "buscarTren.do.log");
    }

    /**
     * Overload with just content
     */
    public String saveResponse(String content)
    {
        return saveResponse(content, 200, "buscarTren.do.log");
    }
}
