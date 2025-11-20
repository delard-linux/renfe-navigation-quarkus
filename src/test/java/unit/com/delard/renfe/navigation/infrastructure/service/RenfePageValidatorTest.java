package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.application.exception.QueueException;
import com.delard.renfe.navigation.application.exception.TrainUnavailabilityException;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RenfePageValidatorTest {

    @InjectMocks
    private RenfePageValidator validator;

    @BeforeEach
    void setUp() {
    }

    // ========== Tests for checkForQueuePage method ==========

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when page contains 'estás en la cola'")
    void testCheckForQueuePageWithEstasEnLaCola() {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Estás en la cola de espera");

        // Act & Assert
        QueueException exception = assertThrows(QueueException.class, () -> {
            validator.checkForQueuePage(mockPage);
        });
        
        assertTrue(exception.getMessage().contains("queued"));
        verify(mockPage, times(1)).waitForTimeout(2000L);
        verify(mockPage, times(1)).locator("body");
    }

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when page contains 'cola para comprar'")
    void testCheckForQueuePageWithColaParaComprar() {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Estás en la cola para comprar billetes");

        // Act & Assert
        QueueException exception = assertThrows(QueueException.class, () -> {
            validator.checkForQueuePage(mockPage);
        });
        
        assertTrue(exception.getMessage().contains("queued"));
    }

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when page contains 'cuando sea tu turno'")
    void testCheckForQueuePageWithCuandoSeaTuTurno() {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Cuando sea tu turno te redirigiremos");

        // Act & Assert
        QueueException exception = assertThrows(QueueException.class, () -> {
            validator.checkForQueuePage(mockPage);
        });
        
        assertTrue(exception.getMessage().contains("queued"));
    }

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when page contains 'te redirigiremos'")
    void testCheckForQueuePageWithTeRedirigiremos() {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Te redirigiremos cuando sea tu turno");

        // Act & Assert
        QueueException exception = assertThrows(QueueException.class, () -> {
            validator.checkForQueuePage(mockPage);
        });
        
        assertTrue(exception.getMessage().contains("queued"));
    }

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when queue locators find queue.it elements")
    void testCheckForQueuePageWithQueueItInHtml() {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page content");
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(1); // Found queue element

        // Act & Assert
        QueueException exception = assertThrows(QueueException.class, () -> {
            validator.checkForQueuePage(mockPage);
        });
        
        assertTrue(exception.getMessage().contains("queued"));
    }

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when queue page text locators are found")
    void testCheckForQueuePageWithQueuePageTextLocators() {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page content");
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(1);

        // Act & Assert
        QueueException exception = assertThrows(QueueException.class, () -> {
            validator.checkForQueuePage(mockPage);
        });
        
        assertTrue(exception.getMessage().contains("queued"));
    }

    @Test
    @DisplayName("checkForQueuePage should throw QueueException when turno text locator is found")
    void testCheckForQueuePageWithTurnoTextLocator() {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal page content");
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(1);

        // Act & Assert
        QueueException exception = assertThrows(QueueException.class, () -> {
            validator.checkForQueuePage(mockPage);
        });
        
        assertTrue(exception.getMessage().contains("queued"));
    }

    @Test
    @DisplayName("checkForQueuePage should not throw when page is normal")
    void testCheckForQueuePageWithNormalPage() {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn("Normal train search page");
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);

        // Act & Assert - Should not throw
        assertDoesNotThrow(() -> {
            validator.checkForQueuePage(mockPage);
        });
        
        verify(mockPage, times(1)).waitForTimeout(2000L);
    }

    @Test
    @DisplayName("checkForQueuePage should handle null bodyText gracefully")
    void testCheckForQueuePageWithNullBodyText() {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockBodyLocator = mock(Locator.class);
        Locator mockQueueLocator = mock(Locator.class);
        Locator mockColaLocator = mock(Locator.class);
        Locator mockTurnoLocator = mock(Locator.class);
        
        when(mockPage.locator("body")).thenReturn(mockBodyLocator);
        when(mockBodyLocator.textContent()).thenReturn(null); // null bodyText
        when(mockPage.locator("[class*='queue'], [id*='queue'], img[alt*='queue'], img[src*='queue']"))
                .thenReturn(mockQueueLocator);
        when(mockQueueLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/cola/i")).thenReturn(mockColaLocator);
        when(mockColaLocator.count()).thenReturn(0);
        when(mockPage.locator("text=/turno/i")).thenReturn(mockTurnoLocator);
        when(mockTurnoLocator.count()).thenReturn(0);

        // Act & Assert - Should not throw
        assertDoesNotThrow(() -> {
            validator.checkForQueuePage(mockPage);
        });
    }

    @Test
    @DisplayName("checkForQueuePage should re-throw QueueException")
    void testCheckForQueuePageReThrowsQueueException() {
        // Arrange
        Page mockPage = mock(Page.class);
        
        doThrow(new QueueException("Already queued")).when(mockPage).waitForTimeout(2000L);

        // Act & Assert
        QueueException exception = assertThrows(QueueException.class, () -> {
            validator.checkForQueuePage(mockPage);
        });
        
        assertEquals("Already queued", exception.getMessage());
    }

    @Test
    @DisplayName("checkForQueuePage should handle general exception and continue")
    void testCheckForQueuePageWithGeneralException() {
        // Arrange
        Page mockPage = mock(Page.class);
        
        doThrow(new RuntimeException("General error")).when(mockPage).waitForTimeout(2000L);

        // Act & Assert - Should not throw (exception is caught and logged)
        assertDoesNotThrow(() -> {
            validator.checkForQueuePage(mockPage);
        });
    }
    
    // ========== Tests for checkForTrainUnavailability method ==========
    
    @Test
    @DisplayName("checkForTrainUnavailability should throw TrainUnavailabilityException for outbound specific error")
    void testCheckForTrainUnavailabilityOutboundSpecific() {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockErrorLocator = mock(Locator.class);
        
        when(mockPage.locator("#noDispoIda.msjErrorTrenes")).thenReturn(mockErrorLocator);
        when(mockErrorLocator.count()).thenReturn(1);
        when(mockErrorLocator.isVisible()).thenReturn(true);
        when(mockErrorLocator.textContent()).thenReturn("No hay trenes de ida");
        
        // Act & Assert
        TrainUnavailabilityException exception = assertThrows(TrainUnavailabilityException.class, () -> {
            validator.checkForTrainUnavailability(mockPage, "outbound");
        });
        
        assertEquals("outbound", exception.getDirection());
        assertEquals("No hay trenes de ida", exception.getDetailMessage());
    }
    
    @Test
    @DisplayName("checkForTrainUnavailability should throw TrainUnavailabilityException for return specific error")
    void testCheckForTrainUnavailabilityReturnSpecific() {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockErrorLocator = mock(Locator.class);
        
        when(mockPage.locator("#noDispoVuelta.msjErrorTrenes")).thenReturn(mockErrorLocator);
        when(mockErrorLocator.count()).thenReturn(1);
        when(mockErrorLocator.isVisible()).thenReturn(true);
        when(mockErrorLocator.textContent()).thenReturn("No hay trenes de vuelta");
        
        // Act & Assert
        TrainUnavailabilityException exception = assertThrows(TrainUnavailabilityException.class, () -> {
            validator.checkForTrainUnavailability(mockPage, "return");
        });
        
        assertEquals("return", exception.getDirection());
        assertEquals("No hay trenes de vuelta", exception.getDetailMessage());
    }
    
    @Test
    @DisplayName("checkForTrainUnavailability should throw TrainUnavailabilityException for generic error in outbound tab")
    void testCheckForTrainUnavailabilityOutboundGeneric() {
        // Arrange
        Page mockPage = mock(Page.class);
        Locator mockErrorLocator = mock(Locator.class);
        Locator mockTabLocator = mock(Locator.class);
        Locator mockGenericErrors = mock(Locator.class);
        Locator mockSingleError = mock(Locator.class);
        
        when(mockPage.locator("#noDispoIda.msjErrorTrenes")).thenReturn(mockErrorLocator);
        when(mockErrorLocator.count()).thenReturn(0);
        
        when(mockPage.locator("#stv-ida")).thenReturn(mockTabLocator);
        when(mockTabLocator.count()).thenReturn(1);
        when(mockTabLocator.locator("p.msjErrorTrenes")).thenReturn(mockGenericErrors);
        when(mockGenericErrors.count()).thenReturn(1);
        when(mockGenericErrors.nth(0)).thenReturn(mockSingleError);
        when(mockSingleError.isVisible()).thenReturn(true);
        when(mockSingleError.textContent()).thenReturn("Error genérico ida");
        
        // Act & Assert
        TrainUnavailabilityException exception = assertThrows(TrainUnavailabilityException.class, () -> {
            validator.checkForTrainUnavailability(mockPage, "outbound");
        });
        
        assertEquals("outbound", exception.getDirection());
        assertEquals("Error genérico ida", exception.getDetailMessage());
    }
}

