package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.infrastructure.config.PlaywrightConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaywrightSearchTrainsServiceTest {

    @Mock
    PlaywrightConfig config;

    @Mock
    RenfeCommonService renfeCommonService;

    @Mock
    TrainHtmlParser trainHtmlParser;

    @Mock
    ResponseStorageService responseStorageService;

    @Mock
    PlaywrightFactory playwrightFactory;

    @Mock
    Playwright playwright;

    @Mock
    BrowserType browserType;

    @Mock
    Browser browser;

    @Mock
    BrowserContext browserContext;

    @Mock
    Page page;

    @InjectMocks
    PlaywrightSearchTrainsService service;

    @Test
    void searchTrainsReturnsOutboundResults() {
        Map<String, String> originStation = Map.of("desgEstacion", "Ourense", "clave", "OU");
        Map<String, String> destinationStation = Map.of("desgEstacion", "Madrid", "clave", "MD");

        when(renfeCommonService.findStation("OURENSE")).thenReturn(originStation);
        when(renfeCommonService.findStation("MADRID")).thenReturn(destinationStation);
        when(renfeCommonService.formatDate("2025-12-01")).thenReturn("2025-12-01");

        when(playwrightFactory.create()).thenReturn(playwright);
        when(playwright.chromium()).thenReturn(browserType);
        when(browserType.launch(any(BrowserType.LaunchOptions.class))).thenReturn(browser);
        when(browser.newContext(any(Browser.NewContextOptions.class))).thenReturn(browserContext);
        when(browserContext.newPage()).thenReturn(page);

        doNothing().when(playwright).close();
        doNothing().when(browser).close();
        doNothing().when(browserContext).close();
        doNothing().when(page).close();

        when(config.getLocale()).thenReturn("es-ES");
        when(config.getViewportWidth()).thenReturn(1280);
        when(config.getViewportHeight()).thenReturn(720);
        when(config.isHeadless()).thenReturn(true);
        when(config.getSlowMo()).thenReturn(0);
        when(config.getRenfeSearchUrl()).thenReturn("https://renfe.test/search");
        when(config.getNavigationTimeoutMs()).thenReturn(1000);
        when(config.getNetworkIdleTimeoutMs()).thenReturn(1000);
        when(config.getShortTimeoutMs()).thenReturn(100);

        doNothing().when(page).navigate(anyString(), any(Page.NavigateOptions.class));
        doNothing().when(page).evaluate(anyString());
        doNothing().when(page).waitForLoadState(any(LoadState.class), any(Page.WaitForLoadStateOptions.class));
        doNothing().when(page).waitForTimeout(anyInt());

        String htmlResponse = "<html>OK</html>";
        when(page.content()).thenReturn(htmlResponse);

        Train mockTrain = new Train("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        when(trainHtmlParser.parseTrainList(htmlResponse)).thenReturn(List.of(mockTrain));

        doNothing().when(responseStorageService).saveResponse(htmlResponse, 200);

        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
            "OURENSE", "MADRID", "2025-12-01", null, 1
        );

        assertEquals(List.of(mockTrain), result.outboundTrains);
        assertNull(result.returnTrains);

        Mockito.verify(responseStorageService).saveResponse(htmlResponse, 200);
        Mockito.verify(trainHtmlParser).parseTrainList(htmlResponse);
        Mockito.verify(page).waitForLoadState(LoadState.NETWORKIDLE, any(Page.WaitForLoadStateOptions.class));
    }
}


