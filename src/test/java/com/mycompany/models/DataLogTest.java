package com.mycompany.models;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тестовый класс для DataLog.
 * 
 * <p>Проверяет валидацию полей, корректность создания объектов
 * и обработку исключительных ситуаций.</p>
 * 
 * <p>Версия 2.0: Обновлена для работы с параметром maxLengthLog в конструкторе.</p>
 * 
 * @author admin_
 * @version 2.0
 */
class DataLogTest {
    
    private static final String VALID_MESSAGE = "Test log message";
    private static final String VALID_STATUS = "INFO";
    private static final int DEFAULT_MAX_LENGTH = 200;
    private static final int CUSTOM_MAX_LENGTH = 100;
    
    @BeforeEach
    void setUp() {
        System.out.println("Running test...");
    }
    
    // ==================== Успешные сценарии ====================
    
    @Test
    @DisplayName("Should create DataLog with valid parameters")
    void testCreateDataLogWithValidParameters() {
        assertDoesNotThrow(() -> {
            DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, DEFAULT_MAX_LENGTH);
            assertNotNull(log);
        });
    }
    
    @Test
    @DisplayName("Should create DataLog with custom max length")
    void testCreateDataLogWithCustomMaxLength() throws ExceptionDataLog {
        String message80 = "A".repeat(80);
        DataLog log = new DataLog(message80, VALID_STATUS, CUSTOM_MAX_LENGTH);
        
        assertNotNull(log);
        assertEquals(message80, log.getLogInfo());
        assertEquals(VALID_STATUS, log.getStatus());
    }
    
    @Test
    @DisplayName("Should return correct message")
    void testGetLogInfo() throws ExceptionDataLog {
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, DEFAULT_MAX_LENGTH);
        assertEquals(VALID_MESSAGE, log.getLogInfo());
    }
    
    @Test
    @DisplayName("Should return correct status")
    void testGetStatus() throws ExceptionDataLog {
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, DEFAULT_MAX_LENGTH);
        assertEquals(VALID_STATUS, log.getStatus());
    }
    
    @Test
    @DisplayName("Should set timestamp automatically")
    void testTimeLogIsSet() throws ExceptionDataLog {
        LocalDateTime before = LocalDateTime.now();
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, DEFAULT_MAX_LENGTH);
        LocalDateTime after = LocalDateTime.now();
        
        LocalDateTime logTime = log.getTimeLogAsObject();
        
        assertNotNull(logTime);
        assertTrue(logTime.isAfter(before) || logTime.equals(before));
        assertTrue(logTime.isBefore(after) || logTime.equals(after));
    }
    
    @Test
    @DisplayName("Should return formatted string")
    void testToString() throws ExceptionDataLog {
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, DEFAULT_MAX_LENGTH);
        String toString = log.toString();
        
        assertTrue(toString.contains(VALID_STATUS));
        assertTrue(toString.contains(VALID_MESSAGE));
        assertTrue(toString.contains("["));
        assertTrue(toString.contains("]"));
    }
    
    // ==================== Валидация статусов ====================
    
    @Test
    @DisplayName("Should accept all valid statuses")
    void testAllValidStatuses() {
        String[] validStatuses = {"FATAL", "INFO", "WARN", "DEBUG", "TRACE"};
        
        for (String status : validStatuses) {
            assertDoesNotThrow(() -> {
                DataLog log = new DataLog(VALID_MESSAGE, status, DEFAULT_MAX_LENGTH);
                assertEquals(status, log.getStatus());
            });
        }
    }
    
    @Test
    @DisplayName("Should reject invalid status")
    void testRejectInvalidStatus() {
        String[] invalidStatuses = {"INVALID", "ERROR", "LOG", "", " "};
        
        for (String status : invalidStatuses) {
            assertThrows(ExceptionDataLog.class, () -> {
                new DataLog(VALID_MESSAGE, status, DEFAULT_MAX_LENGTH);
            });
        }
    }
    
    @Test
    @DisplayName("Should reject null status")
    void testRejectNullStatus() {
        assertThrows(ExceptionDataLog.class, () -> {
            new DataLog(VALID_MESSAGE, null, DEFAULT_MAX_LENGTH);
        });
    }
    
    // ==================== Валидация длины сообщения ====================
    
    @Test
    @DisplayName("Should accept message of exactly max length (200)")
    void testExactMaxLength() throws ExceptionDataLog {
        String message200 = "A".repeat(200);
        assertDoesNotThrow(() -> {
            DataLog log = new DataLog(message200, VALID_STATUS, DEFAULT_MAX_LENGTH);
            assertEquals(message200, log.getLogInfo());
        });
    }
    
    @Test
    @DisplayName("Should accept message of exactly custom max length (100)")
    void testExactCustomMaxLength() throws ExceptionDataLog {
        String message100 = "A".repeat(100);
        assertDoesNotThrow(() -> {
            DataLog log = new DataLog(message100, VALID_STATUS, CUSTOM_MAX_LENGTH);
            assertEquals(message100, log.getLogInfo());
        });
    }
    
    @Test
    @DisplayName("Should reject message longer than default max length (200)")
    void testRejectLongMessageDefault() {
        String longMessage = "A".repeat(201);
        
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog(longMessage, VALID_STATUS, DEFAULT_MAX_LENGTH);
        });
        
        String errorMsg = exception.getCombinedLogMsg();
        assertTrue(errorMsg.contains("length info > 200"));
    }
    
    @Test
    @DisplayName("Should reject message longer than custom max length (100)")
    void testRejectLongMessageCustom() {
        String longMessage = "A".repeat(101);
        
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog(longMessage, VALID_STATUS, CUSTOM_MAX_LENGTH);
        });
        
        String errorMsg = exception.getCombinedLogMsg();
        assertTrue(errorMsg.contains("length info > 100"));
    }
    
    @Test
    @DisplayName("Should accept empty message")
    void testEmptyMessage() throws ExceptionDataLog {
        assertDoesNotThrow(() -> {
            DataLog log = new DataLog("", VALID_STATUS, DEFAULT_MAX_LENGTH);
            assertEquals("", log.getLogInfo());
        });
    }
    
    @Test
    @DisplayName("Should reject null message")
    void testNullMessage() {
        assertThrows(ExceptionDataLog.class, () -> {
            new DataLog(null, VALID_STATUS, DEFAULT_MAX_LENGTH);
        });
    }
    
    // ==================== Комбинированные ошибки ====================
    
    @Test
    @DisplayName("Should report both errors when both fields are invalid")
    void testBothFieldsInvalid() {
        String longMessage = "A".repeat(201);
        String invalidStatus = "INVALID";
        
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog(longMessage, invalidStatus, DEFAULT_MAX_LENGTH);
        });
        
        String errorMsg = exception.getCombinedLogMsg();
        assertTrue(errorMsg.contains("length info > 200"));
        assertTrue(errorMsg.contains("Status has been not correctness"));
    }
    
    @Test
    @DisplayName("Should report error when message exceeds custom max length and status invalid")
    void testBothFieldsInvalidWithCustomMaxLength() {
        String longMessage = "A".repeat(101);
        String invalidStatus = "INVALID";
        
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog(longMessage, invalidStatus, CUSTOM_MAX_LENGTH);
        });
        
        String errorMsg = exception.getCombinedLogMsg();
        assertTrue(errorMsg.contains("length info > 100"));
        assertTrue(errorMsg.contains("Status has been not correctness"));
    }
    
    // ==================== Проверка исключения ====================
    
    @Test
    @DisplayName("Exception should contain correct HTTP code")
    void testExceptionHttpCode() {
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog("Test", "INVALID", DEFAULT_MAX_LENGTH);
        });
        
        assertEquals(400, exception.getClientCode());
    }
    
    @Test
    @DisplayName("Exception should contain client message")
    void testExceptionClientMessage() {
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog("Test", "INVALID", DEFAULT_MAX_LENGTH);
        });
        
        assertNotNull(exception.getClientMsg());
    }
    
    @Test
    @DisplayName("Exception should have stack trace")
    void testExceptionStackTrace() {
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog("Test", "INVALID", DEFAULT_MAX_LENGTH);
        });
        
        String combinedMsg = exception.getCombinedLogMsg();
        assertTrue(combinedMsg.contains("DataLog.DataLog()"));
        assertTrue(combinedMsg.contains("DataLog.verificationInputMsgStrings()"));
    }
    
    @Test
    @DisplayName("Exception should have timestamp")
    void testExceptionTimestamp() {
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog("Test", "INVALID", DEFAULT_MAX_LENGTH);
        });
        
        assertNotNull(exception.getTimeErr());
    }
    
    // ==================== Граничные случаи ====================
    
    @Test
    @DisplayName("Should handle special characters in message")
    void testSpecialCharacters() throws ExceptionDataLog {
        String specialMsg = "!@#$%^&*()_+{}|:<>?~`";
        DataLog log = new DataLog(specialMsg, VALID_STATUS, DEFAULT_MAX_LENGTH);
        
        assertEquals(specialMsg, log.getLogInfo());
    }
    
    @Test
    @DisplayName("Should handle unicode characters")
    void testUnicodeCharacters() throws ExceptionDataLog {
        String unicodeMsg = "Привет мир! こんにちは 🌍";
        DataLog log = new DataLog(unicodeMsg, VALID_STATUS, DEFAULT_MAX_LENGTH);
        
        assertEquals(unicodeMsg, log.getLogInfo());
    }
    
    @Test
    @DisplayName("Should preserve exact message content")
    void testMessagePreservation() throws ExceptionDataLog {
        String originalMsg = "User 'admin' logged in at 2024-01-01";
        DataLog log = new DataLog(originalMsg, VALID_STATUS, DEFAULT_MAX_LENGTH);
        
        assertEquals(originalMsg, log.getLogInfo());
    }
    
    @Test
    @DisplayName("Should handle zero max length")
    void testZeroMaxLength() {
        assertThrows(ExceptionDataLog.class, () -> {
            new DataLog("Any message", VALID_STATUS, 0);
        });
    }
    
    @Test
    @DisplayName("Should handle negative max length")
    void testNegativeMaxLength() throws ExceptionDataLog {
        // Отрицательный maxLengthLog - невалидное значение, но конструктор не проверяет
        // Любое сообщение будет длиннее отрицательного числа
        String message = "Test";
        assertThrows(ExceptionDataLog.class, () -> {
            new DataLog(message, VALID_STATUS, -1);
        });
    }
}