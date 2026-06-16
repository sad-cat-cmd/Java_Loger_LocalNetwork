package com.mycompany.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестовый класс для LogStatus.
 * 
 * <p>Проверяет валидацию статусов логирования:
 * <ul>
 *   <li>Допустимые статусы: FATAL, INFO, WARN, DEBUG, TRACE</li>
 *   <li>Невалидные статусы: null, empty, другие строки</li>
 *   <li>Работу исключений и стек трейсов</li>
 * </ul>
 * </p>
 * 
 * @author admin_
 * @version 1.0
 */
class LogStatusTest {
    
    private static final String[] VALID_STATUSES = {"FATAL", "INFO", "WARN", "DEBUG", "TRACE"};
    private static final String[] INVALID_STATUSES = {"INVALID", "ERROR", "LOG", "TEST", "123", "info", "Info"};
    
    // ==================== Успешные сценарии ====================
    
    @Test
    @DisplayName("Should create LogStatus with all valid statuses")
    void testCreateWithValidStatuses() {
        for (String status : VALID_STATUSES) {
            assertDoesNotThrow(() -> {
                LogStatus logStatus = new LogStatus(status);
                assertNotNull(logStatus);
                assertEquals(status, logStatus.getStatus());
            }, "Status '" + status + "' should be valid");
        }
    }
    
    @Test
    @DisplayName("Should return correct status value")
    void testGetStatus() throws ExceptionLogStatus {
        LogStatus logStatus = new LogStatus("INFO");
        assertEquals("INFO", logStatus.getStatus());
    }
    
    // ==================== Сценарии с ошибками ====================
    
    @Test
    @DisplayName("Should reject null status")
    void testRejectNullStatus() {
        ExceptionLogStatus exception = assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus(null);
        });
        
        String combinedMsg = exception.getCombinedLogMsg();
        assertTrue(combinedMsg.contains("status is null"), 
                   "Error message should contain 'status is null'");
        assertTrue(combinedMsg.contains("time:"), 
                   "Combined message should contain timestamp");
        assertTrue(combinedMsg.contains("stack trace"), 
                   "Combined message should contain stack trace");
        assertTrue(combinedMsg.contains("LogStatus.LogStatus()"), 
                   "Stack trace should contain constructor call");
        assertEquals(400, exception.getClientCode());
        assertNotNull(exception.getClientMsg());
    }
    
    @Test
    @DisplayName("Should reject empty status")
    void testRejectEmptyStatus() {
        ExceptionLogStatus exception = assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus("");
        });
        
        String combinedMsg = exception.getCombinedLogMsg();
        assertTrue(combinedMsg.contains("status is empty"), 
                   "Error message should contain 'status is empty'");
        assertTrue(combinedMsg.contains("time:"), 
                   "Combined message should contain timestamp");
        assertTrue(combinedMsg.contains("stack trace"), 
                   "Combined message should contain stack trace");
        assertEquals(400, exception.getClientCode());
        assertNotNull(exception.getClientMsg());
    }
    
    @Test
    @DisplayName("Should reject status with only whitespace")
    void testRejectWhitespaceStatus() {
        ExceptionLogStatus exception = assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus("   ");
        });
        
        String combinedMsg = exception.getCombinedLogMsg();
        assertTrue(combinedMsg.contains("Status has been not correctness"), 
                   "Error message should contain 'Status has been not correctness'");
        assertTrue(combinedMsg.contains("time:"), 
                   "Combined message should contain timestamp");
        assertEquals(400, exception.getClientCode());
    }
    
    @Test
    @DisplayName("Should reject invalid status values")
    void testRejectInvalidStatuses() {
        for (String status : INVALID_STATUSES) {
            assertThrows(ExceptionLogStatus.class, () -> {
                new LogStatus(status);
            }, "Status '" + status + "' should be rejected");
        }
    }
    
    @Test
    @DisplayName("Should reject case-sensitive statuses")
    void testRejectCaseSensitive() {
        // Только точное совпадение с заглавными буквами
        assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus("info");
        }, "Lowercase 'info' should be rejected");
        
        assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus("Info");
        }, "Mixed case 'Info' should be rejected");
        
        assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus("iNFO");
        }, "Mixed case 'iNFO' should be rejected");
        
        // Но с заглавными должно работать
        assertDoesNotThrow(() -> {
            new LogStatus("INFO");
        }, "Uppercase 'INFO' should be valid");
    }
    
    @Test
    @DisplayName("Should reject status with leading spaces")
    void testRejectLeadingSpaces() {
        ExceptionLogStatus exception = assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus(" INFO");
        });
        
        String combinedMsg = exception.getCombinedLogMsg();
        assertTrue(combinedMsg.contains("Status has been not correctness"));
        assertTrue(combinedMsg.contains("time:"));
    }
    
    @Test
    @DisplayName("Should reject status with trailing spaces")
    void testRejectTrailingSpaces() {
        ExceptionLogStatus exception = assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus("INFO ");
        });
        
        String combinedMsg = exception.getCombinedLogMsg();
        assertTrue(combinedMsg.contains("Status has been not correctness"));
        assertTrue(combinedMsg.contains("time:"));
    }
    
    // ==================== Проверка исключения ====================
    
    @Test
    @DisplayName("Exception should contain correct HTTP code (400)")
    void testExceptionHttpCode() {
        ExceptionLogStatus exception = assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus("INVALID");
        });
        
        assertEquals(400, exception.getClientCode());
    }
    
    @Test
    @DisplayName("Exception should contain client message")
    void testExceptionClientMessage() {
        ExceptionLogStatus exception = assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus("INVALID");
        });
        
        assertNotNull(exception.getClientMsg());
        assertEquals("Invalid request data", exception.getClientMsg());
    }
    
    @Test
    @DisplayName("Exception should have stack trace with correct entries")
    void testExceptionStackTrace() {
        ExceptionLogStatus exception = assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus("INVALID");
        });
        
        String combinedMsg = exception.getCombinedLogMsg();
        assertNotNull(combinedMsg);
        assertTrue(combinedMsg.contains("stack trace"), 
                   "Stack trace should be present");
        assertTrue(combinedMsg.contains("LogStatus.LogStatus()"), 
                   "Stack trace should contain constructor call");
        assertTrue(combinedMsg.contains("LogStatus.verificationInputStatus()"), 
                   "Stack trace should contain validation method call");
    }
    
    @Test
    @DisplayName("Exception should have timestamp in combined message")
    void testExceptionTimestamp() {
        ExceptionLogStatus exception = assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus("INVALID");
        });
        
        String combinedMsg = exception.getCombinedLogMsg();
        assertNotNull(combinedMsg);
        assertTrue(combinedMsg.contains("time:"), 
                   "Combined message should contain timestamp");
    }
    
    @Test
    @DisplayName("Exception should have error message in combined message")
    void testExceptionErrorMessage() {
        ExceptionLogStatus exception = assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus("INVALID");
        });
        
        String combinedMsg = exception.getCombinedLogMsg();
        assertNotNull(combinedMsg);
        assertTrue(combinedMsg.contains("Status has been not correctness"), 
                   "Error message should contain 'Status has been not correctness'");
    }
    
    @Test
    @DisplayName("Exception should have full combined message structure")
    void testFullCombinedMessageStructure() {
        ExceptionLogStatus exception = assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus("INVALID");
        });
        
        String combinedMsg = exception.getCombinedLogMsg();
        
        // Проверяем структуру сообщения
        assertTrue(combinedMsg.contains("Failed to service work:"), 
                   "Should start with 'Failed to service work:'");
        assertTrue(combinedMsg.contains("time:"), 
                   "Should contain timestamp");
        assertTrue(combinedMsg.contains("stack trace:"), 
                   "Should contain stack trace");
        assertTrue(combinedMsg.contains("Error:"), 
                   "Should contain error message");
        assertTrue(combinedMsg.contains("Status has been not correctness"), 
                   "Should contain specific error");
    }
    
    @Test
    @DisplayName("Exception should be of correct type")
    void testExceptionType() {
        ExceptionLogStatus exception = assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus("INVALID");
        });
        
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof ExceptionLogStatus);
    }
    
    // ==================== Граничные случаи ====================
    
    @Test
    @DisplayName("Should reject status with length 0")
    void testZeroLengthStatus() {
        assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus("");
        });
    }
    
    @Test
    @DisplayName("Should reject status with invalid long string")
    void testLongInvalidStatus() {
        String longStatus = "VERY_LONG_INVALID_STATUS";
        assertThrows(ExceptionLogStatus.class, () -> {
            new LogStatus(longStatus);
        });
    }
}