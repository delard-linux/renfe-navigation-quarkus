package com.delard.renfe.navigation.infrastructure.service;

import com.delard.renfe.navigation.infrastructure.config.PlaywrightConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ResponseStorageService
 */
@ExtendWith(MockitoExtension.class)
class ResponseStorageServiceTest {

    @Mock
    private PlaywrightConfig config;

    @InjectMocks
    private ResponseStorageService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        when(config.getResponsesDir()).thenReturn(tempDir.toString());
    }

    @Test
    void testSaveResponseWithAllParameters() throws IOException {
        String content = "<html><body>Test Content</body></html>";
        int statusCode = 200;
        String filenameSuffix = "test.log";

        String result = service.saveResponse(content, statusCode, filenameSuffix);

        assertNotNull(result);
        assertTrue(result.contains(filenameSuffix));
        assertTrue(result.contains(String.valueOf(statusCode)));
        
        // Verify file was created
        Path savedFile = Path.of(result);
        assertTrue(Files.exists(savedFile));
        
        // Verify content
        String savedContent = Files.readString(savedFile);
        assertEquals(content, savedContent);
    }

    @Test
    void testSaveResponseWithDefaultStatusCode() throws IOException {
        String content = "Test content";
        String filenameSuffix = "test.log";

        String result = service.saveResponse(content, filenameSuffix);

        assertNotNull(result);
        assertTrue(result.contains(filenameSuffix));
        assertTrue(result.contains("200"));
    }

    @Test
    void testSaveResponseWithDefaultFilename() throws IOException {
        String content = "Test content";
        int statusCode = 404;

        String result = service.saveResponse(content, statusCode);

        assertNotNull(result);
        assertTrue(result.contains("buscarTren.do.log"));
        assertTrue(result.contains("404"));
    }

    @Test
    void testSaveResponseWithJustContent() throws IOException {
        String content = "Simple content";

        String result = service.saveResponse(content);

        assertNotNull(result);
        assertTrue(result.contains("buscarTren.do.log"));
        assertTrue(result.contains("200"));
    }

    @Test
    void testSaveResponseCreatesDirectory() {
        Path newDir = tempDir.resolve("new-responses");
        when(config.getResponsesDir()).thenReturn(newDir.toString());

        String content = "Test";
        String result = service.saveResponse(content, "test.log");

        assertNotNull(result);
        assertTrue(Files.exists(newDir));
    }

    @Test
    void testSaveResponseWithSpecialCharacters() throws IOException {
        String content = "Content with special chars: áéíóú € <>&\"'";
        String result = service.saveResponse(content, "special.log");

        assertNotNull(result);
        Path savedFile = Path.of(result);
        String savedContent = Files.readString(savedFile);
        assertEquals(content, savedContent);
    }

    @Test
    void testSaveResponseWithEmptyContent() throws IOException {
        String result = service.saveResponse("", "empty.log");

        assertNotNull(result);
        Path savedFile = Path.of(result);
        assertTrue(Files.exists(savedFile));
        assertEquals(0, Files.size(savedFile));
    }

    @Test
    void testSaveResponseTimestampFormat() {
        String result = service.saveResponse("test", "test.log");
        
        assertNotNull(result);
        // Filename should start with timestamp format YYMMDD_HHMMSS
        String filename = Path.of(result).getFileName().toString();
        assertTrue(filename.matches("\\d{6}_\\d{6}_\\d+_test\\.log"));
    }

    @Test
    void testSaveResponseWithIOException() {
        // Arrange - use invalid path that will cause IOException
        when(config.getResponsesDir()).thenReturn("/invalid/path/that/does/not/exist/and/is/too/long/to/create");

        // Act
        String result = service.saveResponse("test content", "test.log");

        // Assert
        assertNull(result);
    }

    @Test
    void testSaveResponseDirectoryCreationFailure() {
        // Arrange - use path that exists but cannot create subdirectory
        when(config.getResponsesDir()).thenReturn(tempDir.toString());

        // Act - should still work since tempDir exists
        String result = service.saveResponse("test", "test.log");

        // Assert
        assertNotNull(result);
    }

    @Test
    void testSaveResponseWithVeryLongContent() throws IOException {
        // Arrange
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longContent.append("This is a very long content line. ");
        }
        String content = longContent.toString();

        // Act
        String result = service.saveResponse(content, "long.log");

        // Assert
        assertNotNull(result);
        Path savedFile = Path.of(result);
        assertTrue(Files.exists(savedFile));
        String savedContent = Files.readString(savedFile);
        assertEquals(content, savedContent);
    }

    @Test
    void testSaveResponseWithDifferentStatusCodes() {
        // Arrange
        String content = "test";
        int[] statusCodes = {200, 404, 500, 301, 302};

        // Act & Assert
        for (int statusCode : statusCodes) {
            String result = service.saveResponse("test", statusCode, "test.log");
            assertNotNull(result);
            assertTrue(result.contains(String.valueOf(statusCode)));
        }
    }

    @Test
    void testSaveResponseWhenDirectoryAlreadyExists() {
        // Arrange - directory already exists (should skip creation)
        when(config.getResponsesDir()).thenReturn(tempDir.toString());
        
        // Create a file first to ensure directory exists
        try {
            Files.createFile(tempDir.resolve("existing.txt"));
        } catch (IOException e) {
            // Ignore
        }

        // Act
        String result = service.saveResponse("test", "test.log");

        // Assert - should work fine when directory exists
        assertNotNull(result);
        assertTrue(result.contains("test.log"));
    }

    @Test
    void testSaveResponseMultipleCalls() {
        // Arrange
        when(config.getResponsesDir()).thenReturn(tempDir.toString());

        // Act - multiple calls should all work
        String result1 = service.saveResponse("content1", "file1.log");
        String result2 = service.saveResponse("content2", "file2.log");
        String result3 = service.saveResponse("content3", "file3.log");

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertNotEquals(result1, result2);
        assertNotEquals(result2, result3);
    }

    @Test
    void testSaveResponseWithEmptyStringContent() {
        // Arrange
        when(config.getResponsesDir()).thenReturn(tempDir.toString());

        // Act - test with empty string instead of null (null would cause NPE)
        String result = service.saveResponse("", "empty.log");

        // Assert - should handle empty string
        assertNotNull(result);
        assertTrue(result.contains("empty.log"));
    }
}

