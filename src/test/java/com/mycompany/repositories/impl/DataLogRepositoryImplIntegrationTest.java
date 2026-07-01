package com.mycompany.repositories.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mycompany.database.DatabaseManager;
import com.mycompany.database.ExceptionDB;
import com.mycompany.models.DataLog;
import com.mycompany.models.LogStatus;
import com.mycompany.models.Process;
import com.mycompany.repositories.ExceptionAccess;
import com.mycompany.repositories.ExceptionFound;

/**
 * Интеграционные тесты для DataLogRepositoryImpl.
 * 
 * <p>Использует реальную in-memory SQLite базу данных.
 * Проверяет работу репозитория с БД.</p>
 * 
 * @author admin_
 * @version 1.0
 */
class DataLogRepositoryImplIntegrationTest {
    
    private DatabaseManager dbManager;
    private DataLogRepositoryImpl repository;
    
    private final String TEST_PROCESS_ID = "proc_001";
    private final String TEST_PROCESS_NAME = "test-service";
    private final String TEST_OWNER = "admin@test.com";
    private final String TEST_UNIQUE_CODE = "unique_code_456";
    private final String TEST_SECURE_CODE = "secure_code_123";
    private final String TEST_MESSAGE = "Test log message";
    
    @BeforeEach
    void setUp() throws ExceptionDB {
        dbManager = new DatabaseManager("jdbc:sqlite::memory:");
        repository = new DataLogRepositoryImpl(dbManager);
    }
    
    @AfterEach
    void tearDown() {
        if (dbManager != null) {
            dbManager.close();
        }
    }
    
    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================
    
    private Process createTestProcess(String status) throws ExceptionDB {
        Process process = new Process(
            TEST_PROCESS_ID,
            TEST_PROCESS_NAME,
            TEST_OWNER,
            status,
            TEST_UNIQUE_CODE,
            LocalDateTime.now(),
            status.equals("Finished") ? LocalDateTime.now() : null,
            0
        );
        dbManager.insertProcess(process);
        return process;
    }
    
    private DataLog createTestLog(int number) {
        return new DataLog(
            TEST_MESSAGE + " #" + number,
            "INFO",
            number,
            LocalDateTime.now()
        );
    }
    
    private void addTestLogs(int count) throws ExceptionDB {
        for (int i = 1; i <= count; i++) {
            dbManager.insertLog(TEST_PROCESS_ID, createTestLog(i));
        }
        dbManager.updateLogCount(TEST_PROCESS_ID, count);
    }
    
    // ==================== ТЕСТЫ SAVE ====================
    
    @Test
    @DisplayName("Should save log successfully")
    void testSaveSuccess() throws Exception {
        createTestProcess("Active");
        
        DataLog log = createTestLog(1);
        DataLog result = repository.save(log, TEST_PROCESS_ID, TEST_UNIQUE_CODE);
        
        assertNotNull(result);
        assertEquals(TEST_MESSAGE + " #1", result.getLogInfo());
        
        // Проверяем, что лог сохранен
        List<DataLog> logs = dbManager.selectAllLogs(TEST_PROCESS_ID);
        assertEquals(1, logs.size());
        assertEquals(TEST_MESSAGE + " #1", logs.get(0).getLogInfo());
        
        // Проверяем, что счетчик обновлен
        Process process = dbManager.selectProcessById(TEST_PROCESS_ID);
        assertEquals(1, process.getLogCount());
    }
    
    @Test
    @DisplayName("Should throw ExceptionFound when process not found")
    void testSaveProcessNotFound() {
        DataLog log = createTestLog(1);
        
        assertThrows(ExceptionFound.class, () -> {
            repository.save(log, "non_existent_process", TEST_SECURE_CODE);
        });
    }
    
    @Test
    @DisplayName("Should throw ExceptionAccess when process is finished")
    void testSaveProcessFinished() throws Exception {
        createTestProcess("Finished");
        DataLog log = createTestLog(1);
        
        assertThrows(ExceptionAccess.class, () -> {
            repository.save(log, TEST_PROCESS_ID, TEST_SECURE_CODE);
        });
    }
    
    @Test
    @DisplayName("Should throw ExceptionAccess when secure code is invalid")
    void testSaveInvalidSecureCode() throws Exception {
        createTestProcess("Active");
        DataLog log = createTestLog(1);
        
        assertThrows(ExceptionAccess.class, () -> {
            repository.save(log, TEST_PROCESS_ID, "wrong_code");
        });
    }
    
    // ==================== ТЕСТЫ FINDALLLOGPROCESS ====================
    
    @Test
    @DisplayName("Should find all logs for process")
    void testFindAllLogProcessSuccess() throws Exception {
        createTestProcess("Active");
        addTestLogs(5);
        
        List<DataLog> result = repository.findAllLogProcess(TEST_PROCESS_ID);
        
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals(TEST_MESSAGE + " #5", result.get(0).getLogInfo()); // DESC order
    }
    
    @Test
    @DisplayName("Should return empty list when no logs")
    void testFindAllLogProcessEmpty() throws Exception {
        createTestProcess("Active");
        
        List<DataLog> result = repository.findAllLogProcess(TEST_PROCESS_ID);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("Should throw ExceptionFound when process not found in findAllLogProcess")
    void testFindAllLogProcessNotFound() {
        assertThrows(ExceptionFound.class, () -> {
            repository.findAllLogProcess("non_existent_process");
        });
    }
    
    // ==================== ТЕСТЫ FINDIDANDBYSTATUSES ====================
    
    @Test
    @DisplayName("Should filter logs by statuses")
    void testFindIdAndByStatusesSuccess() throws Exception {
        createTestProcess("Active");
        addTestLogs(3);
        
        List<LogStatus> statuses = Arrays.asList(
            new LogStatus("INFO")
        );
        
        List<DataLog> result = repository.findIdAndByStatuses(TEST_PROCESS_ID, statuses);
        
        assertNotNull(result);
        assertEquals(3, result.size());
    }
    
    @Test
    @DisplayName("Should return all logs when statuses list is empty")
    void testFindIdAndByStatusesEmptyStatuses() throws Exception {
        createTestProcess("Active");
        addTestLogs(3);
        
        List<DataLog> result = repository.findIdAndByStatuses(TEST_PROCESS_ID, new ArrayList<>());
        
        assertNotNull(result);
        assertEquals(3, result.size());
    }
    
    @Test
    @DisplayName("Should return empty list when no logs match statuses")
    void testFindIdAndByStatusesNoMatch() throws Exception {
        createTestProcess("Active");
        addTestLogs(3);
        
        List<LogStatus> statuses = Arrays.asList(
            new LogStatus("FATAL")
        );
        
        List<DataLog> result = repository.findIdAndByStatuses(TEST_PROCESS_ID, statuses);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    // ==================== ТЕСТЫ FINDIDANDBYSUBSTRING ====================
    
    @Test
    @DisplayName("Should find logs by substring")
    void testFindIdAndBySubstringSuccess() throws Exception {
        createTestProcess("Active");
        addTestLogs(3);
        
        String substring = "Test";
        List<DataLog> result = repository.findIdAndBySubstring(TEST_PROCESS_ID, substring);
        
        assertNotNull(result);
        assertEquals(3, result.size());
    }
    
    @Test
    @DisplayName("Should return all logs when substring is empty")
    void testFindIdAndBySubstringEmptySubstring() throws Exception {
        createTestProcess("Active");
        addTestLogs(3);
        
        List<DataLog> result = repository.findIdAndBySubstring(TEST_PROCESS_ID, "");
        
        assertNotNull(result);
        assertEquals(3, result.size());
    }
    
    @Test
    @DisplayName("Should return empty list when substring not found")
    void testFindIdAndBySubstringNoMatch() throws Exception {
        createTestProcess("Active");
        addTestLogs(3);
        
        List<DataLog> result = repository.findIdAndBySubstring(TEST_PROCESS_ID, "notfound");
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    // ==================== ТЕСТЫ FINDIDBYINDEXFROM ====================
    
    @Test
    @DisplayName("Should find logs from index")
    void testFindIdByIndexFromSuccess() throws Exception {
        createTestProcess("Active");
        addTestLogs(10);
        
        // startIndex = 5 → number_log >= 6
        List<DataLog> result = repository.findIdByIndexFrom(TEST_PROCESS_ID, 5);
        
        assertNotNull(result);
        assertEquals(5, result.size());
        assertTrue(result.get(0).getNumberLog() >= 6);
    }
    
    @Test
    @DisplayName("Should return all logs when startIndex is 0")
    void testFindIdByIndexFromZero() throws Exception {
        createTestProcess("Active");
        addTestLogs(10);
        
        List<DataLog> result = repository.findIdByIndexFrom(TEST_PROCESS_ID, 0);
        
        assertNotNull(result);
        assertEquals(10, result.size());
    }
    
    @Test
    @DisplayName("Should return empty list when startIndex exceeds log count")
    void testFindIdByIndexFromExceeds() throws Exception {
        createTestProcess("Active");
        addTestLogs(5);
        
        List<DataLog> result = repository.findIdByIndexFrom(TEST_PROCESS_ID, 10);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("Should throw ExceptionFound when process not found in findIdByIndexFrom")
    void testFindIdByIndexFromNotFound() {
        assertThrows(ExceptionFound.class, () -> {
            repository.findIdByIndexFrom("non_existent_process", 0);
        });
    }
}