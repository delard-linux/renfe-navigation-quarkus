package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.domain.model.Train;
import com.delard.renfe.navigation.infrastructure.config.PlaywrightConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

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

    @Mock
    Locator locator;

    @InjectMocks
    PlaywrightSearchTrainsService service;

    @Test
    void searchTrainsReturnsOutboundResults() {
        Map<String, String> originStation = Map.of("desgEstacion", "Ourense", "clave", "OU");
        Map<String, String> destinationStation = Map.of("desgEstacion", "Madrid", "clave", "MD");

        when(renfeCommonService.findStation("OURENSE")).thenReturn(originStation);
        when(renfeCommonService.findStation("MADRID")).thenReturn(destinationStation);
        when(renfeCommonService.formatDate("2025-12-01")).thenReturn("01/12/2025");

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
        lenient().when(config.getShortTimeoutMs()).thenReturn(500);
        lenient().when(page.navigate(anyString(), any(Page.NavigateOptions.class))).thenReturn(null);
        lenient().when(page.evaluate(anyString())).thenReturn(null);

        String htmlResponse = "<html>OK</html>";
        when(page.content()).thenReturn(htmlResponse);

        Train mockTrain = new Train("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        when(trainHtmlParser.parseTrainList(htmlResponse)).thenReturn(List.of(mockTrain));

        when(responseStorageService.saveResponse(htmlResponse, 200)).thenReturn("/tmp/resp.html");

        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
            "OURENSE", "MADRID", "2025-12-01", null, 1
        );

        assertEquals(List.of(mockTrain), result.outboundTrains);
        assertNull(result.returnTrains);

        Mockito.verify(responseStorageService).saveResponse(htmlResponse, 200);
        Mockito.verify(trainHtmlParser).parseTrainList(htmlResponse);
        Mockito.verify(page, times(2)).waitForLoadState(eq(LoadState.NETWORKIDLE), any(Page.WaitForLoadStateOptions.class));
    }

    @Test
    void searchTrainsWithReturnDate() {
        Map<String, String> originStation = Map.of("desgEstacion", "Ourense", "clave", "OU");
        Map<String, String> destinationStation = Map.of("desgEstacion", "Madrid", "clave", "MD");

        when(renfeCommonService.findStation("OURENSE")).thenReturn(originStation);
        when(renfeCommonService.findStation("MADRID")).thenReturn(destinationStation);
        when(renfeCommonService.formatDate("2025-12-01")).thenReturn("01/12/2025");
        when(renfeCommonService.formatDate("2025-12-05")).thenReturn("05/12/2025");

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
        when(config.getShortTimeoutMs()).thenReturn(500);
        lenient().when(page.navigate(anyString(), any(Page.NavigateOptions.class))).thenReturn(null);
        lenient().when(page.evaluate(anyString())).thenReturn(null);

        String htmlResponse = "<html>OK</html>";
        // page.content() is called multiple times
        when(page.content()).thenReturn(htmlResponse);
        // waitForLoadState is called multiple times
        lenient().doNothing().when(page).waitForLoadState(any(LoadState.class), any(Page.WaitForLoadStateOptions.class));

        Train mockTrainOut = new Train("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        Train mockTrainRet = new Train("T456", "AVE", "16:00", "18:00", "2h", 25.0);
        when(trainHtmlParser.parseTrainList(htmlResponse))
            .thenReturn(List.of(mockTrainOut))
            .thenReturn(List.of(mockTrainRet));

        when(responseStorageService.saveResponse(htmlResponse, 200)).thenReturn("/tmp/resp.html");

        when(page.locator(anyString())).thenReturn(locator);
        when(locator.count()).thenReturn(1);
        when(locator.first()).thenReturn(locator);
        doNothing().when(locator).click();
        lenient().doNothing().when(page).waitForTimeout(anyInt());

        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
            "OURENSE", "MADRID", "2025-12-01", "2025-12-05", 1
        );

        assertEquals(List.of(mockTrainOut), result.outboundTrains);
        assertNotNull(result.returnTrains);
        assertEquals(List.of(mockTrainRet), result.returnTrains);
    }

    @Test
    void searchTrainsWithReturnDateButNoReturnTab() {
        Map<String, String> originStation = Map.of("desgEstacion", "Ourense", "clave", "OU");
        Map<String, String> destinationStation = Map.of("desgEstacion", "Madrid", "clave", "MD");

        when(renfeCommonService.findStation("OURENSE")).thenReturn(originStation);
        when(renfeCommonService.findStation("MADRID")).thenReturn(destinationStation);
        when(renfeCommonService.formatDate("2025-12-01")).thenReturn("01/12/2025");
        when(renfeCommonService.formatDate("2025-12-05")).thenReturn("05/12/2025");

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
        lenient().when(page.navigate(anyString(), any(Page.NavigateOptions.class))).thenReturn(null);
        lenient().when(page.evaluate(anyString())).thenReturn(null);

        String htmlResponse = "<html>OK</html>";
        when(page.content()).thenReturn(htmlResponse);
        lenient().doNothing().when(page).waitForLoadState(any(LoadState.class), any(Page.WaitForLoadStateOptions.class));

        Train mockTrainOut = new Train("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        when(trainHtmlParser.parseTrainList(htmlResponse)).thenReturn(List.of(mockTrainOut));

        when(responseStorageService.saveResponse(htmlResponse, 200)).thenReturn("/tmp/resp.html");

        when(page.locator(anyString())).thenReturn(locator);
        when(locator.count()).thenReturn(0); // No return tab found

        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
            "OURENSE", "MADRID", "2025-12-01", "2025-12-05", 1
        );

        assertEquals(List.of(mockTrainOut), result.outboundTrains);
        assertNull(result.returnTrains);
    }

    @Test
    void searchTrainsWithEmptyReturnDate() {
        Map<String, String> originStation = Map.of("desgEstacion", "Ourense", "clave", "OU");
        Map<String, String> destinationStation = Map.of("desgEstacion", "Madrid", "clave", "MD");

        when(renfeCommonService.findStation("OURENSE")).thenReturn(originStation);
        when(renfeCommonService.findStation("MADRID")).thenReturn(destinationStation);
        when(renfeCommonService.formatDate("2025-12-01")).thenReturn("01/12/2025");

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
        lenient().when(page.navigate(anyString(), any(Page.NavigateOptions.class))).thenReturn(null);
        lenient().when(page.evaluate(anyString())).thenReturn(null);
        lenient().doNothing().when(page).waitForLoadState(eq(LoadState.NETWORKIDLE), any(Page.WaitForLoadStateOptions.class));

        String htmlResponse = "<html>OK</html>";
        when(page.content()).thenReturn(htmlResponse);

        Train mockTrain = new Train("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        when(trainHtmlParser.parseTrainList(htmlResponse)).thenReturn(List.of(mockTrain));

        when(responseStorageService.saveResponse(htmlResponse, 200)).thenReturn("/tmp/resp.html");

        PlaywrightSearchTrainsService.SearchTrainsResult result = service.searchTrains(
            "OURENSE", "MADRID", "2025-12-01", "", 1
        );

        assertEquals(List.of(mockTrain), result.outboundTrains);
        assertNull(result.returnTrains);
    }

    @Test
    void searchTrainsThrowsExceptionOnError() {
        when(renfeCommonService.findStation(anyString())).thenThrow(new RuntimeException("Test error"));

        assertThrows(RuntimeException.class, () -> {
            service.searchTrains("OURENSE", "MADRID", "2025-12-01", null, 1);
        });
    }

    @Test
    void searchTrainsResultToString() {
        Train train1 = new Train("T123", "AVE", "08:00", "10:00", "2h", 25.0);
        Train train2 = new Train("T456", "ALVIA", "12:00", "14:00", "2h", 30.0);
        List<Train> outbound = List.of(train1, train2);
        List<Train> returnTrains = List.of(train1);

        PlaywrightSearchTrainsService.SearchTrainsResult result =
            new PlaywrightSearchTrainsService.SearchTrainsResult(outbound, returnTrains);

        String toString = result.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("outboundTrains"));
        assertTrue(toString.contains("returnTrains"));
        assertTrue(toString.contains("T123"));
    }

    @Test
    void searchTrainsResultToStringWithNullLists() {
        PlaywrightSearchTrainsService.SearchTrainsResult result =
            new PlaywrightSearchTrainsService.SearchTrainsResult(null, null);

        String toString = result.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("[]"));
    }

    @Test
    void searchTrainsResultToStringWithEmptyLists() {
        PlaywrightSearchTrainsService.SearchTrainsResult result =
            new PlaywrightSearchTrainsService.SearchTrainsResult(List.of(), List.of());

        String toString = result.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("[]"));
    }
}


