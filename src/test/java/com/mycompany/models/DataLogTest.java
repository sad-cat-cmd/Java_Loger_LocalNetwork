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
 * <p>Версия 3.0: Добавлена поддержка порядкового номера лога (numberLog)
 * и новый конструктор для загрузки из БД с номером.</p>
 * 
 * @author admin_
 * @version 3.0
 */
class DataLogTest {
    
    private static final String VALID_MESSAGE = "Test log message";
    private static final String VALID_STATUS = "INFO";
    private static final int DEFAULT_MAX_LENGTH = 200;
    private static final int CUSTOM_MAX_LENGTH = 100;
    private static final int TEST_NUMBER_LOG = 42;
    
    @BeforeEach
    void setUp() {
        System.out.println("Running DataLog test...");
    }
    
    // ==================== Успешные сценарии ====================
    
    @Test
    @DisplayName("Should create DataLog with valid parameters")
    void testCreateDataLogWithValidParameters() {
        assertDoesNotThrow(() -> {
            DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, DEFAULT_MAX_LENGTH);
            assertNotNull(log);
            assertEquals(0, log.getNumberLog()); // Новый лог имеет номер 0
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
        assertEquals(0, log.getNumberLog());
    }
    
    @Test
    @DisplayName("Should create DataLog from database data without number")
    void testCreateDataLogFromDatabaseWithoutNumber() {
        LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, 0, fixedTime);
        
        assertNotNull(log);
        assertEquals(VALID_MESSAGE, log.getLogInfo());
        assertEquals(VALID_STATUS, log.getStatus());
        assertEquals(fixedTime.toString(), log.getTimeLog());
        assertEquals(fixedTime, log.getTimeLogAsObject());
        assertEquals(0, log.getNumberLog());
    }
    
    @Test
    @DisplayName("Should create DataLog from database data with number")
    void testCreateDataLogFromDatabaseWithNumber() {
        LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, TEST_NUMBER_LOG, fixedTime);
        
        assertNotNull(log);
        assertEquals(VALID_MESSAGE, log.getLogInfo());
        assertEquals(VALID_STATUS, log.getStatus());
        assertEquals(fixedTime.toString(), log.getTimeLog());
        assertEquals(fixedTime, log.getTimeLogAsObject());
        assertEquals(TEST_NUMBER_LOG, log.getNumberLog());
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
    @DisplayName("Should return correct number log")
    void testGetNumberLog() {
        LocalDateTime fixedTime = LocalDateTime.now();
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, TEST_NUMBER_LOG, fixedTime);
        assertEquals(TEST_NUMBER_LOG, log.getNumberLog());
    }
    
    @Test
    @DisplayName("Should set timestamp automatically for new log")
    void testTimeLogIsSetForNewLog() throws ExceptionDataLog {
        LocalDateTime before = LocalDateTime.now();
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, DEFAULT_MAX_LENGTH);
        LocalDateTime after = LocalDateTime.now();
        
        LocalDateTime logTime = log.getTimeLogAsObject();
        
        assertNotNull(logTime);
        assertTrue(logTime.isAfter(before) || logTime.equals(before));
        assertTrue(logTime.isBefore(after) || logTime.equals(after));
    }
    
    @Test
    @DisplayName("Should preserve timestamp from database constructor")
    void testTimestampPreservationFromDatabase() {
        LocalDateTime fixedTime = LocalDateTime.now().minusDays(5);
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, 0, fixedTime);
        
        assertEquals(fixedTime, log.getTimeLogAsObject());
        assertEquals(fixedTime.toString(), log.getTimeLog());
    }
    
    @Test
    @DisplayName("Should preserve number and timestamp from database constructor")
    void testNumberAndTimestampPreservationFromDatabase() {
        LocalDateTime fixedTime = LocalDateTime.now().minusDays(5);
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, TEST_NUMBER_LOG, fixedTime);
        
        assertEquals(TEST_NUMBER_LOG, log.getNumberLog());
        assertEquals(fixedTime, log.getTimeLogAsObject());
        assertEquals(fixedTime.toString(), log.getTimeLog());
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
        String[] invalidStatuses = {"INVALID", "ERROR", "LOG"};
        
        for (String status : invalidStatuses) {
            ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
                new DataLog(VALID_MESSAGE, status, DEFAULT_MAX_LENGTH);
            });
            assertTrue(exception.getCombinedLogMsg().contains("Status has been not correctness"));
        }
    }
    
    @Test
    @DisplayName("Should reject empty status")
    void testRejectEmptyStatus() {
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog(VALID_MESSAGE, "", DEFAULT_MAX_LENGTH);
        });
        
        String errorMsg = exception.getCombinedLogMsg();
        assertTrue(errorMsg.contains("strStaus is empty"));
    }
    
    @Test
    @DisplayName("Should reject null status")
    void testRejectNullStatus() {
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog(VALID_MESSAGE, null, DEFAULT_MAX_LENGTH);
        });
        
        String errorMsg = exception.getCombinedLogMsg();
        assertTrue(errorMsg.contains("strStatus is null"));
    }
    
    // ==================== Валидация сообщения ====================
    
    @Test
    @DisplayName("Should accept message of exactly max length")
    void testExactMaxLength() throws ExceptionDataLog {
        String message200 = "A".repeat(200);
        assertDoesNotThrow(() -> {
            DataLog log = new DataLog(message200, VALID_STATUS, DEFAULT_MAX_LENGTH);
            assertEquals(message200, log.getLogInfo());
        });
    }
    
    @Test
    @DisplayName("Should accept message of exactly custom max length")
    void testExactCustomMaxLength() throws ExceptionDataLog {
        String message100 = "A".repeat(100);
        assertDoesNotThrow(() -> {
            DataLog log = new DataLog(message100, VALID_STATUS, CUSTOM_MAX_LENGTH);
            assertEquals(message100, log.getLogInfo());
        });
    }
    
    @Test
    @DisplayName("Should reject message longer than default max length")
    void testRejectLongMessageDefault() {
        String longMessage = "A".repeat(201);
        
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog(longMessage, VALID_STATUS, DEFAULT_MAX_LENGTH);
        });
        
        String errorMsg = exception.getCombinedLogMsg();
        assertTrue(errorMsg.contains("length info > 200"));
    }
    
    @Test
    @DisplayName("Should reject message longer than custom max length")
    void testRejectLongMessageCustom() {
        String longMessage = "A".repeat(101);
        
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog(longMessage, VALID_STATUS, CUSTOM_MAX_LENGTH);
        });
        
        String errorMsg = exception.getCombinedLogMsg();
        assertTrue(errorMsg.contains("length info > 100"));
    }
    
    @Test
    @DisplayName("Should reject empty message")
    void testRejectEmptyMessage() {
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog("", VALID_STATUS, DEFAULT_MAX_LENGTH);
        });
        
        String errorMsg = exception.getCombinedLogMsg();
        assertTrue(errorMsg.contains("log info is empty"));
    }
    
    @Test
    @DisplayName("Should reject null message")
    void testRejectNullMessage() {
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog(null, VALID_STATUS, DEFAULT_MAX_LENGTH);
        });
        
        String errorMsg = exception.getCombinedLogMsg();
        assertTrue(errorMsg.contains("log info is null"));
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
    
    @Test
    @DisplayName("Should report empty message and invalid status")
    void testEmptyMessageAndInvalidStatus() {
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog("", "INVALID", DEFAULT_MAX_LENGTH);
        });
        
        String errorMsg = exception.getCombinedLogMsg();
        assertTrue(errorMsg.contains("log info is empty"));
        assertTrue(errorMsg.contains("Status has been not correctness"));
    }
    
    @Test
    @DisplayName("Should report null message and null status")
    void testNullMessageAndNullStatus() {
        ExceptionDataLog exception = assertThrows(ExceptionDataLog.class, () -> {
            new DataLog(null, null, DEFAULT_MAX_LENGTH);
        });
        
        String errorMsg = exception.getCombinedLogMsg();
        assertTrue(errorMsg.contains("log info is null"));
        assertTrue(errorMsg.contains("strStatus is null"));
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
        assertEquals("Invalid request data", exception.getClientMsg());
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
    @DisplayName("Should handle message with spaces")
    void testMessageWithSpaces() throws ExceptionDataLog {
        String messageWithSpaces = "   Message with leading and trailing spaces   ";
        DataLog log = new DataLog(messageWithSpaces, VALID_STATUS, DEFAULT_MAX_LENGTH);
        
        assertEquals(messageWithSpaces, log.getLogInfo());
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
    void testNegativeMaxLength() {
        assertThrows(ExceptionDataLog.class, () -> {
            new DataLog("Test", VALID_STATUS, -1);
        });
    }
    
    @Test
    @DisplayName("Should handle very long valid message at max length boundary")
    void testVeryLongValidMessage() throws ExceptionDataLog {
        String longValidMessage = "A".repeat(200);
        DataLog log = new DataLog(longValidMessage, VALID_STATUS, 200);
        
        assertEquals(longValidMessage, log.getLogInfo());
    }
    
    // ==================== Тесты для конструктора из БД ====================
    
    @Test
    @DisplayName("Database constructor should work with any status without validation")
    void testDatabaseConstructorWithInvalidStatus() {
        LocalDateTime time = LocalDateTime.now();
        // Конструктор: DataLog(info, status, numberLog, timeLog)
        DataLog log = new DataLog(VALID_MESSAGE, "INVALID_STATUS", 0, time);
        
        assertEquals(VALID_MESSAGE, log.getLogInfo());
        assertEquals("INVALID_STATUS", log.getStatus());
        assertEquals(time, log.getTimeLogAsObject());
        assertEquals(0, log.getNumberLog());
    }
    
    @Test
    @DisplayName("Database constructor should work with null values")
    void testDatabaseConstructorWithNulls() {
        LocalDateTime time = LocalDateTime.now();
        DataLog log = new DataLog(null, null, 0, time);
        
        assertEquals(null, log.getLogInfo());
        assertEquals(null, log.getStatus());
        assertEquals(time, log.getTimeLogAsObject());
        assertEquals(0, log.getNumberLog());
    }
    
    @Test
    @DisplayName("Database constructor should work with empty strings")
    void testDatabaseConstructorWithEmptyStrings() {
        LocalDateTime time = LocalDateTime.now();
        DataLog log = new DataLog("", "", 0, time);
        
        assertEquals("", log.getLogInfo());
        assertEquals("", log.getStatus());
        assertEquals(time, log.getTimeLogAsObject());
        assertEquals(0, log.getNumberLog());
    }
    
    @Test
    @DisplayName("Database constructor should preserve exact timestamp")
    void testDatabaseConstructorPreservesExactTimestamp() {
        LocalDateTime exactTime = LocalDateTime.of(2023, 12, 25, 10, 30, 45, 123456789);
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, 0, exactTime);
        
        assertEquals(exactTime, log.getTimeLogAsObject());
        assertEquals(exactTime.toString(), log.getTimeLog());
        assertEquals(0, log.getNumberLog());
    }
    
    @Test
    @DisplayName("Database constructor with number should preserve all fields")
    void testDatabaseConstructorWithNumberPreservesAllFields() {
        LocalDateTime exactTime = LocalDateTime.of(2023, 12, 25, 10, 30, 45, 123456789);
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, TEST_NUMBER_LOG, exactTime);
        
        assertEquals(VALID_MESSAGE, log.getLogInfo());
        assertEquals(VALID_STATUS, log.getStatus());
        assertEquals(exactTime, log.getTimeLogAsObject());
        assertEquals(exactTime.toString(), log.getTimeLog());
        assertEquals(TEST_NUMBER_LOG, log.getNumberLog());
    }
    
    // ==================== Сравнение конструкторов ====================
    
    @Test
    @DisplayName("New constructor should validate, DB constructor should not")
    void testConstructorValidationDifference() {
        // Новый конструктор - валидация есть
        assertThrows(ExceptionDataLog.class, () -> {
            new DataLog("", "", DEFAULT_MAX_LENGTH);
        });
        
        // DB конструктор - валидации нет (без number)
        assertDoesNotThrow(() -> {
            new DataLog("", "", 0, LocalDateTime.now());
        });
        
        // DB конструктор - валидации нет (с number)
        assertDoesNotThrow(() -> {
            new DataLog("", "", TEST_NUMBER_LOG, LocalDateTime.now());
        });
    }
    
    @Test
    @DisplayName("New constructor should set numberLog to 0")
    void testNewConstructorSetsNumberLogToZero() throws ExceptionDataLog {
        DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, DEFAULT_MAX_LENGTH);
        assertEquals(0, log.getNumberLog());
    }
    
    @Test
    @DisplayName("Database constructor should allow any number")
    void testDatabaseConstructorAllowsAnyNumber() {
        LocalDateTime time = LocalDateTime.now();
        int[] testNumbers = {0, 1, 100, 999, 1000, -1};
        
        for (int number : testNumbers) {
            DataLog log = new DataLog(VALID_MESSAGE, VALID_STATUS, number, time);
            assertEquals(number, log.getNumberLog());
        }
    }
}