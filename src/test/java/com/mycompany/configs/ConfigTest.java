package com.mycompany.configs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тестовый класс для Config.
 * 
 * <p>Проверяет загрузку конфигурации, обработку ошибок и валидацию.</p>
 * 
 * @author admin_
 * @version 2.0
 */
class ConfigTest {
    
    private static final String TEST_CONFIG_FILE = "test-config.json";
    private static final String VALID_CONFIG = 
        "{\n" +
        "    \"database\": {\n" +
        "        \"path\": \"data/test.db\"\n" +
        "    },\n" +
        "    \"server\": {\n" +
        "        \"http_port\": 9090,\n" +
        "        \"tcp_port\": 9999,\n" +
        "        \"max_threads\": 100\n" +
        "    },\n" +
        "    \"logging\": {\n" +
        "        \"max_log_length\": 500,\n" +
        "        \"max_name_process_length\": 60,\n" +
        "        \"max_owner_process_length\": 40\n" +
        "    }\n" +
        "}";
    
    @BeforeEach
    void setUp() throws IOException {
        Files.writeString(Paths.get(TEST_CONFIG_FILE), VALID_CONFIG);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_CONFIG_FILE));
    }
    
    // ==================== Успешные сценарии ====================
    
    @Test
    @DisplayName("Should load valid configuration successfully")
    void testLoadValidConfig() throws ExceptionConfig {
        Config config = new Config(TEST_CONFIG_FILE);
        
        assertNotNull(config);
        assertEquals("data/test.db", config.getPathDBFile());
        assertEquals(9090, config.getHttpPort());
        assertEquals(9999, config.getTcpPort());
        assertEquals(500, config.getMaxLogLength());
        assertEquals(100, config.getMaxThread());
        assertEquals(60, config.getMaxNameProcessLength());
        assertEquals(40, config.getMaxOwnerProcessLength());
    }
    
    @Test
    @DisplayName("Should handle configuration values correctly")
    void testConfigurationValues() throws ExceptionConfig {
        Config config = new Config(TEST_CONFIG_FILE);
        
        assertTrue(config.getHttpPort() > 0, "HTTP port should be positive");
        assertTrue(config.getTcpPort() > 0, "TCP port should be positive");
        assertTrue(config.getMaxLogLength() > 0, "Max log length should be positive");
        assertTrue(config.getMaxThread() > 0, "Max threads should be positive");
        assertTrue(config.getMaxNameProcessLength() > 0, "Max name process length should be positive");
        assertTrue(config.getMaxOwnerProcessLength() > 0, "Max owner process length should be positive");
        assertNotNull(config.getPathDBFile(), "Database path should not be null");
        assertFalse(config.getPathDBFile().isEmpty(), "Database path should not be empty");
    }
    
    // ==================== Тесты для параметров процесса ====================
    
    @Test
    @DisplayName("Should return correct max name process length")
    void testGetMaxNameProcessLength() throws ExceptionConfig {
        Config config = new Config(TEST_CONFIG_FILE);
        assertEquals(60, config.getMaxNameProcessLength());
    }
    
    @Test
    @DisplayName("Should return correct max owner process length")
    void testGetMaxOwnerProcessLength() throws ExceptionConfig {
        Config config = new Config(TEST_CONFIG_FILE);
        assertEquals(40, config.getMaxOwnerProcessLength());
    }
    
    // ==================== Сценарии с ошибками ====================
    
    @Test
    @DisplayName("Should throw exception when config file not found")
    void testConfigFileNotFound() {
        assertThrows(ExceptionConfig.class, () -> {
            new Config("non-existent-file.json");
        });
    }
    
    @Test
    @DisplayName("Should throw exception on invalid JSON syntax")
    void testInvalidJson() throws IOException {
        String invalidJson = "{ invalid json }";
        Files.writeString(Paths.get(TEST_CONFIG_FILE), invalidJson);
        
        assertThrows(ExceptionConfig.class, () -> {
            new Config(TEST_CONFIG_FILE);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when database config missing")
    void testMissingDatabaseConfig() throws IOException {
        String configWithoutDb = 
            "{\n" +
            "    \"server\": {\n" +
            "        \"http_port\": 8080,\n" +
            "        \"tcp_port\": 9090,\n" +
            "        \"max_threads\": 50\n" +
            "    },\n" +
            "    \"logging\": {\n" +
            "        \"max_log_length\": 200,\n" +
            "        \"max_name_process_length\": 50,\n" +
            "        \"max_owner_process_length\": 50\n" +
            "    }\n" +
            "}";
        Files.writeString(Paths.get(TEST_CONFIG_FILE), configWithoutDb);
        
        assertThrows(ExceptionConfig.class, () -> {
            new Config(TEST_CONFIG_FILE);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when server config missing")
    void testMissingServerConfig() throws IOException {
        String configWithoutServer = 
            "{\n" +
            "    \"database\": {\n" +
            "        \"path\": \"data/logs.db\"\n" +
            "    },\n" +
            "    \"logging\": {\n" +
            "        \"max_log_length\": 200,\n" +
            "        \"max_name_process_length\": 50,\n" +
            "        \"max_owner_process_length\": 50\n" +
            "    }\n" +
            "}";
        Files.writeString(Paths.get(TEST_CONFIG_FILE), configWithoutServer);
        
        assertThrows(ExceptionConfig.class, () -> {
            new Config(TEST_CONFIG_FILE);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when logging config missing")
    void testMissingLoggingConfig() throws IOException {
        String configWithoutLogging = 
            "{\n" +
            "    \"database\": {\n" +
            "        \"path\": \"data/logs.db\"\n" +
            "    },\n" +
            "    \"server\": {\n" +
            "        \"http_port\": 8080,\n" +
            "        \"tcp_port\": 9090,\n" +
            "        \"max_threads\": 50\n" +
            "    }\n" +
            "}";
        Files.writeString(Paths.get(TEST_CONFIG_FILE), configWithoutLogging);
        
        assertThrows(ExceptionConfig.class, () -> {
            new Config(TEST_CONFIG_FILE);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when max_name_process_length is missing")
    void testMissingMaxNameProcessLength() throws IOException {
        String configMissingNameLength = 
            "{\n" +
            "    \"database\": {\"path\": \"data/logs.db\"},\n" +
            "    \"server\": {\"http_port\": 8080, \"tcp_port\": 9090, \"max_threads\": 50},\n" +
            "    \"logging\": {\n" +
            "        \"max_log_length\": 200,\n" +
            "        \"max_owner_process_length\": 50\n" +
            "    }\n" +
            "}";
        Files.writeString(Paths.get(TEST_CONFIG_FILE), configMissingNameLength);
        
        assertThrows(ExceptionConfig.class, () -> {
            new Config(TEST_CONFIG_FILE);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when max_owner_process_length is missing")
    void testMissingMaxOwnerProcessLength() throws IOException {
        String configMissingOwnerLength = 
            "{\n" +
            "    \"database\": {\"path\": \"data/logs.db\"},\n" +
            "    \"server\": {\"http_port\": 8080, \"tcp_port\": 9090, \"max_threads\": 50},\n" +
            "    \"logging\": {\n" +
            "        \"max_log_length\": 200,\n" +
            "        \"max_name_process_length\": 50\n" +
            "    }\n" +
            "}";
        Files.writeString(Paths.get(TEST_CONFIG_FILE), configMissingOwnerLength);
        
        assertThrows(ExceptionConfig.class, () -> {
            new Config(TEST_CONFIG_FILE);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when both process length parameters are missing")
    void testMissingBothProcessLengthParams() throws IOException {
        String configMissingBoth = 
            "{\n" +
            "    \"database\": {\"path\": \"data/logs.db\"},\n" +
            "    \"server\": {\"http_port\": 8080, \"tcp_port\": 9090, \"max_threads\": 50},\n" +
            "    \"logging\": {\n" +
            "        \"max_log_length\": 200\n" +
            "    }\n" +
            "}";
        Files.writeString(Paths.get(TEST_CONFIG_FILE), configMissingBoth);
        
        assertThrows(ExceptionConfig.class, () -> {
            new Config(TEST_CONFIG_FILE);
        });
    }
    
    // ==================== Проверка исключений ====================
    
    @Test
    @DisplayName("Exception should contain stack trace")
    void testExceptionStackTrace() {
        ExceptionConfig exception = assertThrows(ExceptionConfig.class, () -> {
            new Config("non-existent.json");
        });
        
        String combinedMsg = exception.getCombinedLogMsg();
        assertNotNull(combinedMsg);
        assertTrue(combinedMsg.contains("stack trace"));
    }
    
    @Test
    @DisplayName("Exception should have timestamp")
    void testExceptionTimestamp() {
        ExceptionConfig exception = assertThrows(ExceptionConfig.class, () -> {
            new Config("non-existent.json");
        });
        
        assertNotNull(exception.getTimeErr());
    }
    
    // ==================== Краевые случаи ====================
    
    @Test
    @DisplayName("Should handle empty config file path")
    void testEmptyPath() {
        assertThrows(ExceptionConfig.class, () -> {
            new Config("");
        });
    }
    
    @Test
    @DisplayName("Should handle null config file path")
    void testNullPath() {
        assertThrows(ExceptionConfig.class, () -> {
            new Config(null);
        });
    }
    
    @Test
    @DisplayName("Should create config from resources")
    void testConfigFromResources() {
        assertDoesNotThrow(() -> {
            Config config = new Config(TEST_CONFIG_FILE);
            assertNotNull(config);
        });
    }
    
    @Test
    @DisplayName("Should validate all config fields are positive")
    void testPositiveValues() throws ExceptionConfig {
        Config config = new Config(TEST_CONFIG_FILE);
        
        assertTrue(config.getHttpPort() > 0, "HTTP port should be positive");
        assertTrue(config.getTcpPort() > 0, "TCP port should be positive");
        assertTrue(config.getMaxLogLength() > 0, "Max log length should be positive");
        assertTrue(config.getMaxThread() > 0, "Max threads should be positive");
        assertTrue(config.getMaxNameProcessLength() > 0, "Max name process length should be positive");
        assertTrue(config.getMaxOwnerProcessLength() > 0, "Max owner process length should be positive");
        assertNotNull(config.getPathDBFile(), "Database path should not be null");
        assertFalse(config.getPathDBFile().isEmpty(), "Database path should not be empty");
    }
    
    // ==================== Граничные значения ====================
    
    @Test
@DisplayName("Should throw exception when values are zero (invalid)")
void testZeroValues() throws IOException {
    String configWithZero = 
        "{\n" +
        "    \"database\": {\"path\": \"data/test.db\"},\n" +
        "    \"server\": {\"http_port\": 0, \"tcp_port\": 0, \"max_threads\": 0},\n" +
        "    \"logging\": {\n" +
        "        \"max_log_length\": 0,\n" +
        "        \"max_name_process_length\": 0,\n" +
        "        \"max_owner_process_length\": 0\n" +
        "    }\n" +
        "}";
    Files.writeString(Paths.get(TEST_CONFIG_FILE), configWithZero);
    
    assertThrows(ExceptionConfig.class, () -> {
        new Config(TEST_CONFIG_FILE);
    });
}
    
    @Test
    @DisplayName("Should handle very large values")
    void testLargeValues() throws IOException, ExceptionConfig {
        String configWithLarge = 
            "{\n" +
            "    \"database\": {\"path\": \"data/test.db\"},\n" +
            "    \"server\": {\"http_port\": 65535, \"tcp_port\": 65535, \"max_threads\": 10000},\n" +
            "    \"logging\": {\n" +
            "        \"max_log_length\": 10000,\n" +
            "        \"max_name_process_length\": 1000,\n" +
            "        \"max_owner_process_length\": 1000\n" +
            "    }\n" +
            "}";
        Files.writeString(Paths.get(TEST_CONFIG_FILE), configWithLarge);
        
        Config config = new Config(TEST_CONFIG_FILE);
        
        assertEquals(65535, config.getHttpPort());
        assertEquals(65535, config.getTcpPort());
        assertEquals(10000, config.getMaxLogLength());
        assertEquals(10000, config.getMaxThread());
        assertEquals(1000, config.getMaxNameProcessLength());
        assertEquals(1000, config.getMaxOwnerProcessLength());
    }
}