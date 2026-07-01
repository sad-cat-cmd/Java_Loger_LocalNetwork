package com.mycompany.repositories.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycompany.database.DatabaseManager;
import com.mycompany.database.ExceptionDB;
import com.mycompany.models.DataLog;
import com.mycompany.models.LogStatus;
import com.mycompany.models.Process;
import com.mycompany.repositories.ExceptionAccess;
import com.mycompany.repositories.ExceptionFound;

/**
 * Модульные тесты для DataLogRepositoryImpl.
 * 
 * <p>Использует Mockito для мокирования DatabaseManager.
 * Тестирует только логику репозитория, без реальной БД.</p>
 * 
 * @author admin_
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class DataLogRepositoryImplUnitTest {
    
    @Mock
    private DatabaseManager manager;
    
    @InjectMocks
    private DataLogRepositoryImpl repository;
    
    private Process testProcess;
    private DataLog testLog;
    private final String TEST_PROCESS_ID = "proc_001";
    private final String TEST_SECURE_CODE = "secure_code_123";
    private final String TEST_UNIQUE_CODE = "unique_code_456";
    private final String TEST_MESSAGE = "Test log message";
    private final String TEST_STATUS = "INFO";
    private final int TEST_NUMBER_LOG = 1;
    
    @BeforeEach
    void setUp() {
        testProcess = new Process(
            TEST_PROCESS_ID,
            "test-service",
            "admin@test.com",
            "Active",
            TEST_UNIQUE_CODE,
            LocalDateTime.now(),
            null,
            0
        );
        
        testLog = new DataLog(
            TEST_MESSAGE,
            TEST_STATUS,
            TEST_NUMBER_LOG,
            LocalDateTime.now()
        );
    }
    
    // ==================== ТЕСТЫ SAVE ====================
    
    @Test
    @DisplayName("Should save log successfully")
    void testSaveSuccess() throws Exception {
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(testProcess);
        doNothing().when(manager).insertLog(TEST_PROCESS_ID, testLog);
        doNothing().when(manager).updateLogCount(TEST_PROCESS_ID, 1);
        
        DataLog result = repository.save(testLog, TEST_PROCESS_ID, TEST_UNIQUE_CODE);
        
        assertNotNull(result);
        assertEquals(TEST_MESSAGE, result.getLogInfo());
        verify(manager, times(1)).selectProcessById(TEST_PROCESS_ID);
        verify(manager, times(1)).insertLog(TEST_PROCESS_ID, testLog);
        verify(manager, times(1)).updateLogCount(TEST_PROCESS_ID, 1);
    }
    
    @Test
    @DisplayName("Should throw ExceptionFound when process not found")
    void testSaveProcessNotFound() throws Exception {
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(null);
        
        assertThrows(ExceptionFound.class, () -> {
            repository.save(testLog, TEST_PROCESS_ID, TEST_SECURE_CODE);
        });
        
        verify(manager, never()).insertLog(anyString(), any(DataLog.class));
        verify(manager, never()).updateLogCount(anyString(), anyLong());
    }
    
    @Test
    @DisplayName("Should throw ExceptionAccess when process is finished")
    void testSaveProcessFinished() throws Exception {
        testProcess = new Process(
            TEST_PROCESS_ID,
            "test-service",
            "admin@test.com",
            "Finished",  // ← Процесс завершен
            TEST_UNIQUE_CODE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            0
        );
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(testProcess);
        
        assertThrows(ExceptionAccess.class, () -> {
            repository.save(testLog, TEST_PROCESS_ID, TEST_SECURE_CODE);
        });
        
        verify(manager, never()).insertLog(anyString(), any(DataLog.class));
        verify(manager, never()).updateLogCount(anyString(), anyLong());
    }
    
    @Test
    @DisplayName("Should throw ExceptionAccess when secure code is invalid")
    void testSaveInvalidSecureCode() throws Exception {
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(testProcess);
        
        assertThrows(ExceptionAccess.class, () -> {
            repository.save(testLog, TEST_PROCESS_ID, "wrong_code");
        });
        
        verify(manager, never()).insertLog(anyString(), any(DataLog.class));
        verify(manager, never()).updateLogCount(anyString(), anyLong());
    }
    
    @Test
    @DisplayName("Should propagate ExceptionDB from save")
    void testSavePropagatesExceptionDB() throws Exception {
        when(manager.selectProcessById(TEST_PROCESS_ID))
            .thenThrow(new ExceptionDB("DB Error", "test", "error", 500));
        
        assertThrows(ExceptionDB.class, () -> {
            repository.save(testLog, TEST_PROCESS_ID, TEST_SECURE_CODE);
        });
        
        verify(manager, times(1)).selectProcessById(TEST_PROCESS_ID);
        verify(manager, never()).insertLog(anyString(), any(DataLog.class));
    }
    
    // ==================== ТЕСТЫ FINDALLLOGPROCESS ====================
    
    @Test
    @DisplayName("Should find all logs for process")
    void testFindAllLogProcessSuccess() throws Exception {
        List<DataLog> expectedLogs = Arrays.asList(testLog);
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(testProcess);
        when(manager.selectAllLogs(TEST_PROCESS_ID)).thenReturn(expectedLogs);
        
        List<DataLog> result = repository.findAllLogProcess(TEST_PROCESS_ID);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_MESSAGE, result.get(0).getLogInfo());
        verify(manager, times(1)).selectProcessById(TEST_PROCESS_ID);
        verify(manager, times(1)).selectAllLogs(TEST_PROCESS_ID);
    }
    
    @Test
    @DisplayName("Should return empty list when no logs")
    void testFindAllLogProcessEmpty() throws Exception {
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(testProcess);
        when(manager.selectAllLogs(TEST_PROCESS_ID)).thenReturn(Collections.emptyList());
        
        List<DataLog> result = repository.findAllLogProcess(TEST_PROCESS_ID);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("Should throw ExceptionFound when process not found in findAllLogProcess")
    void testFindAllLogProcessNotFound() throws Exception {
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(null);
        
        assertThrows(ExceptionFound.class, () -> {
            repository.findAllLogProcess(TEST_PROCESS_ID);
        });
        
        verify(manager, never()).selectAllLogs(anyString());
    }
    
    // ==================== ТЕСТЫ FINDIDANDBYSTATUSES ====================
    
    @Test
    @DisplayName("Should filter logs by statuses")
    void testFindIdAndByStatusesSuccess() throws Exception {
        List<LogStatus> statuses = Arrays.asList(
            new LogStatus("INFO"),
            new LogStatus("WARN")
        );
        List<DataLog> expectedLogs = Arrays.asList(testLog);
        
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(testProcess);
        when(manager.selectLogsByStatuses(TEST_PROCESS_ID, statuses)).thenReturn(expectedLogs);
        
        List<DataLog> result = repository.findIdAndByStatuses(TEST_PROCESS_ID, statuses);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(manager, times(1)).selectLogsByStatuses(TEST_PROCESS_ID, statuses);
    }
    
    @Test
    @DisplayName("Should return all logs when statuses list is empty")
    void testFindIdAndByStatusesEmptyStatuses() throws Exception {
        List<DataLog> expectedLogs = Arrays.asList(testLog);
        
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(testProcess);
        when(manager.selectAllLogs(TEST_PROCESS_ID)).thenReturn(expectedLogs);
        
        List<DataLog> result = repository.findIdAndByStatuses(TEST_PROCESS_ID, Collections.emptyList());
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(manager, times(1)).selectAllLogs(TEST_PROCESS_ID);
        verify(manager, never()).selectLogsByStatuses(anyString(), anyList());
    }
    
    @Test
    @DisplayName("Should return all logs when statuses is null")
    void testFindIdAndByStatusesNullStatuses() throws Exception {
        List<DataLog> expectedLogs = Arrays.asList(testLog);
        
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(testProcess);
        when(manager.selectAllLogs(TEST_PROCESS_ID)).thenReturn(expectedLogs);
        
        List<DataLog> result = repository.findIdAndByStatuses(TEST_PROCESS_ID, null);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(manager, times(1)).selectAllLogs(TEST_PROCESS_ID);
        verify(manager, never()).selectLogsByStatuses(anyString(), anyList());
    }
    
    // ==================== ТЕСТЫ FINDIDANDBYSUBSTRING ====================
    
    @Test
    @DisplayName("Should find logs by substring")
    void testFindIdAndBySubstringSuccess() throws Exception {
        String substring = "payment";
        List<DataLog> expectedLogs = Arrays.asList(testLog);
        
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(testProcess);
        when(manager.selectLogsBySubstring(TEST_PROCESS_ID, substring)).thenReturn(expectedLogs);
        
        List<DataLog> result = repository.findIdAndBySubstring(TEST_PROCESS_ID, substring);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(manager, times(1)).selectLogsBySubstring(TEST_PROCESS_ID, substring);
    }
    
    @Test
    @DisplayName("Should return all logs when substring is empty")
    void testFindIdAndBySubstringEmptySubstring() throws Exception {
        List<DataLog> expectedLogs = Arrays.asList(testLog);
        
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(testProcess);
        when(manager.selectAllLogs(TEST_PROCESS_ID)).thenReturn(expectedLogs);
        
        List<DataLog> result = repository.findIdAndBySubstring(TEST_PROCESS_ID, "");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(manager, times(1)).selectAllLogs(TEST_PROCESS_ID);
        verify(manager, never()).selectLogsBySubstring(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should return all logs when substring is null")
    void testFindIdAndBySubstringNullSubstring() throws Exception {
        List<DataLog> expectedLogs = Arrays.asList(testLog);
        
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(testProcess);
        when(manager.selectAllLogs(TEST_PROCESS_ID)).thenReturn(expectedLogs);
        
        List<DataLog> result = repository.findIdAndBySubstring(TEST_PROCESS_ID, null);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(manager, times(1)).selectAllLogs(TEST_PROCESS_ID);
        verify(manager, never()).selectLogsBySubstring(anyString(), anyString());
    }
    
    // ==================== ТЕСТЫ FINDIDBYINDEXFROM ====================
    
    @Test
    @DisplayName("Should find logs from index")
    void testFindIdByIndexFromSuccess() throws Exception {
        Process processWithLogs = new Process(
            TEST_PROCESS_ID,
            "test-service",
            "admin@test.com",
            "Active",
            TEST_UNIQUE_CODE,
            LocalDateTime.now(),
            null,
            10  // ← 10 логов
        );
        List<DataLog> expectedLogs = Arrays.asList(testLog);
        
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(processWithLogs);
        when(manager.selectLogsByRange(TEST_PROCESS_ID, 6, 10)).thenReturn(expectedLogs);
        
        List<DataLog> result = repository.findIdByIndexFrom(TEST_PROCESS_ID, 5);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(manager, times(1)).selectLogsByRange(TEST_PROCESS_ID, 6, 10);
    }
    
    @Test
    @DisplayName("Should return empty list when start index is 0")
    void testFindIdByIndexFromStartIndexZero() throws Exception {
        Process processWithLogs = new Process(
            TEST_PROCESS_ID,
            "test-service",
            "admin@test.com",
            "Active",
            TEST_UNIQUE_CODE,
            LocalDateTime.now(),
            null,
            10
        );
        List<DataLog> expectedLogs = Arrays.asList(testLog);
        
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(processWithLogs);
        when(manager.selectLogsByRange(TEST_PROCESS_ID, 1, 10)).thenReturn(expectedLogs);
        
        List<DataLog> result = repository.findIdByIndexFrom(TEST_PROCESS_ID, 0);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(manager, times(1)).selectLogsByRange(TEST_PROCESS_ID, 1, 10);
    }
    
    @Test
    @DisplayName("Should throw ExceptionFound when process not found in findIdByIndexFrom")
    void testFindIdByIndexFromNotFound() throws Exception {
        when(manager.selectProcessById(TEST_PROCESS_ID)).thenReturn(null);
        
        assertThrows(ExceptionFound.class, () -> {
            repository.findIdByIndexFrom(TEST_PROCESS_ID, 5);
        });
        
        verify(manager, never()).selectLogsByRange(anyString(), anyInt(), anyInt());
    }
}