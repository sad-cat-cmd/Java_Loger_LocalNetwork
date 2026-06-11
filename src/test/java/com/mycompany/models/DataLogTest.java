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
 */
class DataLogTest {
    
    private static final String VALID_MESSAGE = "Test log message";
    private static final String VALID_STATUS = "INFO";
    private static final String LONG_MESSAGE = "A".repeat(201);
    
    @BeforeEach
    void setUp() {
        System.out.println("Running test...");
    }
    
    // ==================== Успешные сценарии ====================
    
    @Test
    @DisplayName("Should create DataLog with valid parameters")
    void testCreateDataLogWithValidParameters() {
        assertDoesNotThrow(() -> {
            DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS);
            assertNotNull(log);
        });
    }
    
    @Test
    @DisplayName("Should return correct message")
    void testGetLogInfo() throws ExceptionDataLog {
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS);
        assertEquals(VALID_MESSAGE, log.getLogInfo());
    }
    
    @Test
    @DisplayName("Should return correct status")
    void testGetStatus() throws ExceptionDataLog {
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS);
        assertEquals(VALID_STATUS, log.getStatus());
    }
    
    @Test
    @DisplayName("Should set timestamp automatically")
    void testTimeLogIsSet() throws ExceptionDataLog {
        LocalDateTime before = LocalDateTime.now();
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS);
        LocalDateTime after = LocalDateTime.now();
        
        LocalDateTime logTime = log.getTimeLogAsObject();
        
        assertNotNull(logTime);
        assertTrue(logTime.isAfter(before) || logTime.equals(before));
        assertTrue(logTime.isBefore(after) || logTime.equals(after));
    }
    
    @Test
    @DisplayName("Should return formatted string")
    void testToString() throws ExceptionDataLog {
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS);
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
                DataLog log = new DataLog(VALID_MESSAGE, status);
                assertEquals(status, log.getStatus());
            });
        }
    }
    
    @Test
    @DisplayName("Should reject invalid status")
    void testRejectInvalidStatus() {
        String[] invalidStatuses = {"INVALID", "ERROR", "LOG", "", " ", null};
        
        for (String status : invalidStatuses) {
            assertThrows(ExceptionDataLog.class, () -> {
                new DataLog(VALID_MESSAGE, status);
            });
        }
    }
    
    // ==================== Валидация длины сообщения ====================
    
    @Test
    @DisplayName("Should accept message of exactly 200 characters")
    void testExactMaxLength() throws ExceptionDataLog {
        String message200 = "A".repeat(200);
        assertDoesNotThrow(() -> {
            DataLog log = new DataLog(message200, VALID_STATUS);
            assertEquals(message200, log.getLogInfo());
        });
    }
    
    @Test
    @DisplayName("Should reject message longer than 200 characters")
    void testRejectLongMessage() {
        String longMessage = "A".repeat(201);
        
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog(longMessage, VALID_STATUS);
        });
        
        assertTrue(exception.getCombinedLogMsg().contains("length info > 200"));
    }
    
    @Test
    @DisplayName("Should accept empty message")
    void testEmptyMessage() throws ExceptionDataLog {
        assertDoesNotThrow(() -> {
            DataLog log = new DataLog("", VALID_STATUS);
            assertEquals("", log.getLogInfo());
        });
    }
    
    // ==================== Комбинированные ошибки ====================
    
    @Test
    @DisplayName("Should report both errors when both fields are invalid")
    void testBothFieldsInvalid() {
        String longMessage = "A".repeat(201);
        String invalidStatus = "INVALID";
        
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog(longMessage, invalidStatus);
        });
        
        String errorMsg = exception.getCombinedLogMsg();
        assertTrue(errorMsg.contains("length info > 200"));
        assertTrue(errorMsg.contains("Status has been not correctness"));
    }
    
    // ==================== Проверка исключения ====================
    
    @Test
    @DisplayName("Exception should contain correct HTTP code")
    void testExceptionHttpCode() {
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog("Test", "INVALID");
        });
        
        assertEquals(400, exception.getClientCode());
    }
    
    @Test
    @DisplayName("Exception should contain client message")
    void testExceptionClientMessage() {
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog("Test", "INVALID");
        });
        
        assertNotNull(exception.getClientMsg());
    }
    
    @Test
    @DisplayName("Exception should have stack trace")
    void testExceptionStackTrace() {
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog("Test", "INVALID");
        });
        
        String combinedMsg = exception.getCombinedLogMsg();
        assertTrue(combinedMsg.contains("DataLog.DataLog()"));
        assertTrue(combinedMsg.contains("DataLog.verificationInputMsgStrings()"));
    }
    
    @Test
    @DisplayName("Exception should have timestamp")
    void testExceptionTimestamp() {
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog("Test", "INVALID");
        });
        
        assertNotNull(exception.getTimeErr());
    }
    
    // ==================== Граничные случаи случаи ====================
    
    @Test
    @DisplayName("Should handle special characters in message")
    void testSpecialCharacters() throws ExceptionDataLog {
        String specialMsg = "!@#$%^&*()_+{}|:<>?~`";
        DataLog log = new DataLog(specialMsg, VALID_STATUS);
        
        assertEquals(specialMsg, log.getLogInfo());
    }
    
    @Test
    @DisplayName("Should handle unicode characters")
    void testUnicodeCharacters() throws ExceptionDataLog {
        String unicodeMsg = "Привет мир! こんにちは 🌍";
        DataLog log = new DataLog(unicodeMsg, VALID_STATUS);
        
        assertEquals(unicodeMsg, log.getLogInfo());
    }
    
    @Test
    @DisplayName("Should preserve exact message content")
    void testMessagePreservation() throws ExceptionDataLog {
        String originalMsg = "User 'admin' logged in at 2024-01-01";
        DataLog log = new DataLog(originalMsg, VALID_STATUS);
        
        assertEquals(originalMsg, log.getLogInfo());
    }
}