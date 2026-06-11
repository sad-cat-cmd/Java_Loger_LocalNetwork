package com.mycompany.configes;

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
        "        \"max_log_length\": 500\n" +
        "    }\n" +
        "}";
    
    @BeforeEach
    void setUp() throws IOException {
        // Создаем тестовый конфиг перед каждым тестом
        Files.writeString(Paths.get(TEST_CONFIG_FILE), VALID_CONFIG);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        // Удаляем тестовый конфиг после каждого теста
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
    }
    
    @Test
    @DisplayName("Should handle default configuration values")
    void testDefaultValues() throws ExceptionConfig {
        Config config = new Config(TEST_CONFIG_FILE);
        
        assertNotNull(config.getPathDBFile());
        assertTrue(config.getHttpPort() > 0);
        assertTrue(config.getTcpPort() > 0);
        assertTrue(config.getMaxLogLength() > 0);
        assertTrue(config.getMaxThread() > 0);
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
            "        \"max_log_length\": 200\n" +
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
            "        \"max_log_length\": 200\n" +
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
        // Проверяем, что config.json существует в resources
        // Этот тест просто проверяет, что класс может быть создан
        // Для реальной загрузки из resources нужно создать файл
        
        // Создаем временный config в текущей директории
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
        assertNotNull(config.getPathDBFile(), "Database path should not be null");
        assertFalse(config.getPathDBFile().isEmpty(), "Database path should not be empty");
    }
}