package com.mycompany.database;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mycompany.models.DataLog;
import com.mycompany.models.ExceptionLogStatus;
import com.mycompany.models.LogStatus;
import com.mycompany.models.Process;

/**
 * Тестовый класс для DatabaseManager.
 * 
 * @author admin_
 * @version 1.0
 */
class DatabaseManagerTest {
    
    private DatabaseManager dbManager;
    
    @BeforeEach
    void setUp() {
        try {
            dbManager = new DatabaseManager("jdbc:sqlite::memory:");
        } catch (ExceptionDB e) {
            //System.err.println("Failed to initialize DatabaseManager:\n" + e.getCombinedLogMsg());
            fail("DatabaseManager initialization failed:\n\t" + e.getCombinedLogMsg());
        }
    }
    
    @AfterEach
    void tearDown() {
        if (dbManager != null) {
            dbManager.close();
        }
    }
    
    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================
    
    private LogStatus createLogStatus(String status) {
        try {
            return new LogStatus(status);
        } catch (ExceptionLogStatus e) {
            fail("Failed to create LogStatus: " + e.getMessage());
            return null;
        }
    }
    
    private List<LogStatus> createStatusList(String... statusNames) {
        List<LogStatus> statuses = new ArrayList<>();
        for (String name : statusNames) {
            statuses.add(createLogStatus(name));
        }
        return statuses;
    }
    
    // ==================== БАЗОВЫЕ ТЕСТЫ ====================
    
    @Test
    @DisplayName("Should initialize DatabaseManager successfully")
    void testInitialization() {
        assertNotNull(dbManager);
        assertNotNull(dbManager.getConnection());
        assertNotNull(dbManager.getCollectionStatement());
    }
    
    @Test
    @DisplayName("Should insert and retrieve process")
    void testInsertAndSelectProcess() throws ExceptionDB {
        LocalDateTime now = LocalDateTime.now();
        Process process = new Process(
            "proc_001",
            "test-service",
            "admin@test.com",
            "Active",
            "code_123",
            now,
            null,
            0
        );
        
        dbManager.insertProcess(process);
        
        Process retrieved = dbManager.selectProcessById("proc_001");
        
        assertNotNull(retrieved);
        assertEquals(process.getID(), retrieved.getID());
        assertEquals(process.getName(), retrieved.getName());
    }
    
    @Test
    @DisplayName("Should select all processes")
    void testSelectAllProcesses() throws ExceptionDB {
        Process process1 = new Process("proc_1", "service-1", "owner1", "Active", "code1", 
                                       LocalDateTime.now(), null, 0);
        Process process2 = new Process("proc_2", "service-2", "owner2", "Active", "code2", 
                                       LocalDateTime.now(), null, 0);
        
        dbManager.insertProcess(process1);
        dbManager.insertProcess(process2);
        
        List<Process> processes = dbManager.selectAllProcesses();
        
        assertNotNull(processes);
        assertEquals(2, processes.size());
    }
    
    @Test
    @DisplayName("Should select active processes only")
    void testSelectActiveProcesses() throws ExceptionDB {
        Process active = new Process("proc_1", "active-service", "owner1", "Active", "code1",
                                     LocalDateTime.now(), null, 0);
        Process finished = new Process("proc_2", "finished-service", "owner2", "Finished", "code2",
                                       LocalDateTime.now(), LocalDateTime.now(), 0);
        
        dbManager.insertProcess(active);
        dbManager.insertProcess(finished);
        
        List<Process> activeProcesses = dbManager.selectActiveProcesses();
        
        assertNotNull(activeProcesses);
        assertEquals(1, activeProcesses.size());
        assertEquals("Active", activeProcesses.get(0).getStatus());
    }
    
    @Test
    @DisplayName("Should return null for non-existent process")
    void testSelectNonExistentProcess() throws ExceptionDB {
        Process process = dbManager.selectProcessById("non_existent_id");
        assertNull(process);
    }
    
    @Test
    @DisplayName("Should update process status")
    void testUpdateProcessStatus() throws ExceptionDB {
        Process process = new Process("proc_001", "test-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        dbManager.updateProcessStatus("proc_001", "Finished");
        
        Process updated = dbManager.selectProcessById("proc_001");
        assertEquals("Finished", updated.getStatus());
    }
    
    @Test
    @DisplayName("Should update process finished_by")
    void testUpdateProcessFinishedBy() throws ExceptionDB {
        Process process = new Process("proc_001", "test-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        LocalDateTime now = LocalDateTime.now();
        dbManager.updateProcessFinishedBy("proc_001", now);
        
        Process updated = dbManager.selectProcessById("proc_001");
        assertNotNull(updated.getTimeEndWork());
        assertEquals(now.toString(), updated.getTimeEndWork().toString());
    }
    
    @Test
    @DisplayName("Should update log count")
    void testUpdateLogCount() throws ExceptionDB {
        Process process = new Process("proc_001", "test-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        dbManager.updateLogCount("proc_001", 5);
        
        Process updated = dbManager.selectProcessById("proc_001");
        assertEquals(5, updated.getLogCount());
    }
    
    // ==================== ТЕСТЫ ЛОГОВ ====================
    
    @Test
    @DisplayName("Should insert and retrieve log")
    void testInsertAndSelectLog() throws ExceptionDB {
        Process process = new Process("proc_001", "test-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        LocalDateTime now = LocalDateTime.now();
        DataLog log = new DataLog("Test message", "INFO", 1, now);
        
        dbManager.insertLog("proc_001", log);
        
        List<DataLog> logs = dbManager.selectAllLogs("proc_001");
        
        assertNotNull(logs);
        assertEquals(1, logs.size());
        assertEquals("Test message", logs.get(0).getLogInfo());
        assertEquals("INFO", logs.get(0).getStatus());
    }
    
    @Test
    @DisplayName("Should insert multiple logs")
    void testInsertMultipleLogs() throws ExceptionDB {
        Process process = new Process("proc_001", "test-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        LocalDateTime now = LocalDateTime.now();
        dbManager.insertLog("proc_001", new DataLog("Log 1", "INFO", 1, now));
        dbManager.insertLog("proc_001", new DataLog("Log 2", "WARN", 2, now));
        dbManager.insertLog("proc_001", new DataLog("Log 3", "ERROR", 3, now));
        
        List<DataLog> logs = dbManager.selectAllLogs("proc_001");
        
        assertEquals(3, logs.size());
    }
    
    @Test
    @DisplayName("Should return empty list for process without logs")
    void testSelectLogsForProcessWithoutLogs() throws ExceptionDB {
        Process process = new Process("proc_001", "test-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        List<DataLog> logs = dbManager.selectAllLogs("proc_001");
        
        assertNotNull(logs);
        assertEquals(0, logs.size());
    }
    
    @Test
    @DisplayName("Should count logs for process")
    void testCountLogs() throws ExceptionDB {
        Process process = new Process("proc_001", "test-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        LocalDateTime now = LocalDateTime.now();
        dbManager.insertLog("proc_001", new DataLog("Log 1", "INFO", 1, now));
        dbManager.insertLog("proc_001", new DataLog("Log 2", "WARN", 2, now));
        
        long count = dbManager.countLogs("proc_001");
        assertEquals(2, count);
    }
    
    @Test
    @DisplayName("Should return 0 count for process without logs")
    void testCountLogsForProcessWithoutLogs() throws ExceptionDB {
        Process process = new Process("proc_001", "test-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        long count = dbManager.countLogs("proc_001");
        assertEquals(0, count);
    }
    
    @Test
    @DisplayName("Should search logs by substring")
    void testSearchLogsBySubstring() throws ExceptionDB {
        Process process = new Process("proc_001", "test-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        LocalDateTime now = LocalDateTime.now();
        dbManager.insertLog("proc_001", new DataLog("Payment processed", "INFO", 1, now));
        dbManager.insertLog("proc_001", new DataLog("User logged in", "INFO", 2, now));
        dbManager.insertLog("proc_001", new DataLog("Payment failed", "ERROR", 3, now));
        
        List<DataLog> results = dbManager.selectLogsBySubstring("proc_001", "Payment");
        
        assertEquals(2, results.size());
    }
    
    @Test
    @DisplayName("Should filter logs by statuses")
    void testFilterLogsByStatuses() throws ExceptionDB {
        Process process = new Process("proc_001", "test-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        LocalDateTime now = LocalDateTime.now();
        dbManager.insertLog("proc_001", new DataLog("Info message", "INFO", 1, now));
        dbManager.insertLog("proc_001", new DataLog("Warning message", "WARN", 2, now));
        dbManager.insertLog("proc_001", new DataLog("Error message", "ERROR", 3, now));
        
        List<LogStatus> statuses = createStatusList("INFO", "WARN");
        List<DataLog> results = dbManager.selectLogsByStatuses("proc_001", statuses);
        
        assertEquals(2, results.size());
    }
    
    @Test
    @DisplayName("Should filter logs by single status")
    void testFilterLogsBySingleStatus() throws ExceptionDB {
        Process process = new Process("proc_001", "test-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        LocalDateTime now = LocalDateTime.now();
        dbManager.insertLog("proc_001", new DataLog("Info message", "INFO", 1, now));
        dbManager.insertLog("proc_001", new DataLog("Warning message", "WARN", 2, now));
        dbManager.insertLog("proc_001", new DataLog("Error message", "FATAL", 3, now));
        
        List<LogStatus> statuses = createStatusList("FATAL");
        List<DataLog> results = dbManager.selectLogsByStatuses("proc_001", statuses);
        
        assertEquals(1, results.size());
        assertEquals("FATAL", results.get(0).getStatus());
    }
    
    // ==================== ТЕСТЫ ОШИБОК ====================
    
    @Test
    @DisplayName("Should throw ExceptionDB on invalid connection")
    void testInvalidConnection() {
        assertThrows(ExceptionDB.class, () -> {
            new DatabaseManager("jdbc:sqlite:/invalid/path/to/database.db");
        });
    }
    
    @Test
    @DisplayName("Should close resources without errors")
    void testClose() {
        assertDoesNotThrow(() -> {
            dbManager.close();
        });
    }
    
    @Test
    @DisplayName("Should get connection")
    void testGetConnection() {
        assertNotNull(dbManager.getConnection());
    }
    
    @Test
    @DisplayName("Should get collection statement")
    void testGetCollectionStatement() {
        assertNotNull(dbManager.getCollectionStatement());
    }
    
    // ==================== ИНТЕГРАЦИОННЫЙ ТЕСТ ====================
    
    @Test
    @DisplayName("Should perform full workflow")
    void testFullWorkflow() throws ExceptionDB {
        LocalDateTime now = LocalDateTime.now();
        Process process = new Process(
            "proc_full",
            "full-service",
            "admin@test.com",
            "Active",
            "secure_code",
            now,
            null,
            0
        );
        dbManager.insertProcess(process);
        
        dbManager.insertLog("proc_full", new DataLog("Started", "INFO", 1, now));
        dbManager.insertLog("proc_full", new DataLog("Processing", "INFO", 2, now));
        dbManager.insertLog("proc_full", new DataLog("Completed", "INFO", 3, now));
        
        long count = dbManager.countLogs("proc_full");
        assertEquals(3, count);
        
        dbManager.updateProcessStatus("proc_full", "Finished");
        dbManager.updateProcessFinishedBy("proc_full", LocalDateTime.now());
        dbManager.updateLogCount("proc_full", 3);
        
        Process updated = dbManager.selectProcessById("proc_full");
        assertEquals("Finished", updated.getStatus());
        assertNotNull(updated.getTimeEndWork());
        assertEquals(3, updated.getLogCount());
    }
    @Test
    @DisplayName("Should return logs in correct range")
    void testSelectLogsByRangeSuccess() throws ExceptionDB {
        // Arrange
        Process process = new Process("proc_range", "range-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= 10; i++) {
            dbManager.insertLog("proc_range", new DataLog("Log #" + i, "INFO", i, now));
        }
        
        // Act
        List<DataLog> logs = dbManager.selectLogsByRange("proc_range", 3, 7);
        
        // Assert
        assertNotNull(logs);
        assertEquals(5, logs.size());
        // Проверяем, что все логи в диапазоне (в DESC порядке)
        for (DataLog log : logs) {
            assertTrue(log.getNumberLog() >= 3 && log.getNumberLog() <= 7);
        }
    }
    
    @Test
    @DisplayName("Should return empty list when range has no logs")
    void testSelectLogsByRangeNoLogs() throws ExceptionDB {
        // Arrange
        Process process = new Process("proc_empty_range", "empty-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        LocalDateTime now = LocalDateTime.now();
        dbManager.insertLog("proc_empty_range", new DataLog("Log #1", "INFO", 1, now));
        dbManager.insertLog("proc_empty_range", new DataLog("Log #2", "INFO", 2, now));
        
        // Act
        List<DataLog> logs = dbManager.selectLogsByRange("proc_empty_range", 10, 20);
        
        // Assert
        assertNotNull(logs);
        assertEquals(0, logs.size());
    }
    
    @Test
    @DisplayName("Should return logs in descending order")
    void testSelectLogsByRangeDescendingOrder() throws ExceptionDB {
        // Arrange
        Process process = new Process("proc_order", "order-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= 5; i++) {
            dbManager.insertLog("proc_order", new DataLog("Log #" + i, "INFO", i, now));
        }
        
        // Act
        List<DataLog> logs = dbManager.selectLogsByRange("proc_order", 1, 5);
        
        // Assert
        assertNotNull(logs);
        assertEquals(5, logs.size());
        // Проверяем DESC порядок (от большего к меньшему)
        for (int i = 0; i < logs.size() - 1; i++) {
            assertTrue(logs.get(i).getNumberLog() > logs.get(i + 1).getNumberLog());
        }
    }
    
    @Test
    @DisplayName("Should throw ExceptionDB when processID is null")
    void testSelectLogsByRangeNullProcessId() {
        ExceptionDB exception = assertThrows(ExceptionDB.class, () -> {
            dbManager.selectLogsByRange(null, 1, 10);
        });
        
        assertTrue(exception.getCombinedLogMsg().contains("Process ID cannot be null or empty"));
    }
    
    @Test
    @DisplayName("Should throw ExceptionDB when processID is empty")
    void testSelectLogsByRangeEmptyProcessId() {
        ExceptionDB exception = assertThrows(ExceptionDB.class, () -> {
            dbManager.selectLogsByRange("", 1, 10);
        });
        
        assertTrue(exception.getCombinedLogMsg().contains("Process ID cannot be null or empty"));
    }
    
    @Test
    @DisplayName("Should throw ExceptionDB when indexStart is negative")
    void testSelectLogsByRangeNegativeStart() {
        ExceptionDB exception = assertThrows(ExceptionDB.class, () -> {
            dbManager.selectLogsByRange("proc_001", -1, 10);
        });
        
        assertTrue(exception.getCombinedLogMsg().contains("Range indices cannot be negative"));
    }
    
    @Test
    @DisplayName("Should throw ExceptionDB when indexEnd is negative")
    void testSelectLogsByRangeNegativeEnd() {
        ExceptionDB exception = assertThrows(ExceptionDB.class, () -> {
            dbManager.selectLogsByRange("proc_001", 1, -5);
        });
        
        assertTrue(exception.getCombinedLogMsg().contains("Range indices cannot be negative"));
    }
    
    @Test
    @DisplayName("Should throw ExceptionDB when start > end")
    void testSelectLogsByRangeStartGreaterThanEnd() {
        ExceptionDB exception = assertThrows(ExceptionDB.class, () -> {
            dbManager.selectLogsByRange("proc_001", 10, 1);
        });
        
        assertTrue(exception.getCombinedLogMsg().contains("Start index cannot be greater than end index"));
    }
    
    @Test
    @DisplayName("Should handle range with start = 0 and end = 0")
    void testSelectLogsByRangeZeroToZero() throws ExceptionDB {
        // Arrange
        Process process = new Process("proc_zero", "zero-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        LocalDateTime now = LocalDateTime.now();
        dbManager.insertLog("proc_zero", new DataLog("Log #1", "INFO", 1, now));
        
        // Act
        List<DataLog> logs = dbManager.selectLogsByRange("proc_zero", 0, 0);
        
        // Assert
        assertNotNull(logs);
        assertEquals(0, logs.size()); // Нет лога с номером 0
    }
    
    @Test
    @DisplayName("Should handle range that exceeds existing logs")
    void testSelectLogsByRangeExceedsLogs() throws ExceptionDB {
        // Arrange
        Process process = new Process("proc_exceed", "exceed-service", "admin@test.com", "Active", 
                                      "code_123", LocalDateTime.now(), null, 0);
        dbManager.insertProcess(process);
        
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= 5; i++) {
            dbManager.insertLog("proc_exceed", new DataLog("Log #" + i, "INFO", i, now));
        }
        
        // Act
        List<DataLog> logs = dbManager.selectLogsByRange("proc_exceed", 1, 100);
        
        // Assert
        assertNotNull(logs);
        assertEquals(5, logs.size()); // Только существующие логи
    }
}