package com.mycompany.repositories.impl;

import com.mycompany.database.DatabaseManager;
import com.mycompany.database.ExceptionDB;
import com.mycompany.models.Process;
import com.mycompany.repositories.ExceptionAccess;
import com.mycompany.repositories.ExceptionFound;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Юнит-тесты для ProcessRepositoryImpl.
 * 
 * <p>Использует Mockito для мокирования DatabaseManager.
 * Тестирует только логику репозитория, без реальной БД.</p>
 * 
 * @author admin_
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class ProcessRepositoryImplUnitTest {
    
    @Mock
    private DatabaseManager managerDB;
    
    @InjectMocks
    private ProcessRepositoryImpl repository;
    
    private Process testProcess;
    private final String TEST_ID = "proc_001";
    private final String TEST_NAME = "test-service";
    private final String TEST_OWNER = "admin@test.com";
    private final String TEST_CODE = "code_123";
    
    @BeforeEach
    void setUp() {
        testProcess = new Process(
            TEST_ID,
            TEST_NAME,
            TEST_OWNER,
            "Active",
            TEST_CODE,
            LocalDateTime.now(),
            null,
            0
        );
    }
    
    // ==================== ТЕСТЫ SAVE ====================
    
    @Test
    @DisplayName("Should save process successfully")
    void testSaveSuccess() throws ExceptionDB {
        // Arrange
        doNothing().when(managerDB).insertProcess(any(Process.class));
        
        // Act
        Process result = repository.save(testProcess);
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_ID, result.getID());
        verify(managerDB, times(1)).insertProcess(testProcess);
    }
    
    @Test
    @DisplayName("Should propagate ExceptionDB from save")
    void testSaveThrowsExceptionDB() throws ExceptionDB {
        // Arrange
        doThrow(new ExceptionDB("DB Error", "test", "error", 500))
            .when(managerDB).insertProcess(any(Process.class));
        
        // Act & Assert
        assertThrows(ExceptionDB.class, () -> {
            repository.save(testProcess);
        });
        verify(managerDB, times(1)).insertProcess(any(Process.class));
    }
    
    // ==================== ТЕСТЫ FINDBYID ====================
    
    @Test
    @DisplayName("Should find process by ID successfully")
    void testFindByIdSuccess() throws ExceptionDB, ExceptionFound {
        // Arrange
        when(managerDB.selectProcessById(TEST_ID)).thenReturn(testProcess);
        
        // Act
        Process result = repository.findById(TEST_ID);
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_ID, result.getID());
        verify(managerDB, times(1)).selectProcessById(TEST_ID);
    }
    
    @Test
    @DisplayName("Should throw ExceptionFound when process not found")
    void testFindByIdNotFound() throws ExceptionDB {
        // Arrange
        when(managerDB.selectProcessById(anyString())).thenReturn(null);
        
        // Act & Assert
        assertThrows(ExceptionFound.class, () -> {
            repository.findById("non_existent_id");
        });
        verify(managerDB, times(1)).selectProcessById("non_existent_id");
    }
    
    @Test
    @DisplayName("Should propagate ExceptionDB from findById")
    void testFindByIdThrowsExceptionDB() throws ExceptionDB {
        // Arrange
        when(managerDB.selectProcessById(anyString()))
            .thenThrow(new ExceptionDB("DB Error", "test", "error", 500));
        
        // Act & Assert
        assertThrows(ExceptionDB.class, () -> {
            repository.findById(TEST_ID);
        });
        verify(managerDB, times(1)).selectProcessById(TEST_ID);
    }
    
    // ==================== ТЕСТЫ FINDALL ====================
    
    @Test
    @DisplayName("Should find all processes successfully")
    void testFindAllSuccess() throws ExceptionDB {
        // Arrange
        Process process2 = new Process(
            "proc_002", "service-2", "owner2", "Active", "code2",
            LocalDateTime.now(), null, 0
        );
        List<Process> processList = Arrays.asList(testProcess, process2);
        when(managerDB.selectAllProcesses()).thenReturn(processList);
        
        // Act
        List<Process> result = repository.findAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(managerDB, times(1)).selectAllProcesses();
    }
    
    @Test
    @DisplayName("Should return empty list when no processes")
    void testFindAllEmpty() throws ExceptionDB {
        // Arrange
        when(managerDB.selectAllProcesses()).thenReturn(Arrays.asList());
        
        // Act
        List<Process> result = repository.findAll();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(managerDB, times(1)).selectAllProcesses();
    }
    
    @Test
    @DisplayName("Should propagate ExceptionDB from findAll")
    void testFindAllThrowsExceptionDB() throws ExceptionDB {
        // Arrange
        when(managerDB.selectAllProcesses())
            .thenThrow(new ExceptionDB("DB Error", "test", "error", 500));
        
        // Act & Assert
        assertThrows(ExceptionDB.class, () -> {
            repository.findAll();
        });
        verify(managerDB, times(1)).selectAllProcesses();
    }
    
    // ==================== ТЕСТЫ FINDALLACTIVE ====================
    
    @Test
    @DisplayName("Should find all active processes successfully")
    void testFindAllActiveSuccess() throws ExceptionDB {
        // Arrange
        List<Process> processList = Arrays.asList(testProcess);
        when(managerDB.selectActiveProcesses()).thenReturn(processList);
        
        // Act
        List<Process> result = repository.findAllActive();
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(managerDB, times(1)).selectActiveProcesses();
    }
    
    @Test
    @DisplayName("Should return empty list when no active processes")
    void testFindAllActiveEmpty() throws ExceptionDB {
        // Arrange
        when(managerDB.selectActiveProcesses()).thenReturn(Arrays.asList());
        
        // Act
        List<Process> result = repository.findAllActive();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(managerDB, times(1)).selectActiveProcesses();
    }
    
    @Test
    @DisplayName("Should propagate ExceptionDB from findAllActive")
    void testFindAllActiveThrowsExceptionDB() throws ExceptionDB {
        // Arrange
        when(managerDB.selectActiveProcesses())
            .thenThrow(new ExceptionDB("DB Error", "test", "error", 500));
        
        // Act & Assert
        assertThrows(ExceptionDB.class, () -> {
            repository.findAllActive();
        });
        verify(managerDB, times(1)).selectActiveProcesses();
    }
    
    // ==================== ТЕСТЫ FINISHPROCESS ====================
    
    @Test
    @DisplayName("Should finish process with system call")
    void testFinishProcessSystemCall() throws ExceptionDB, ExceptionFound, ExceptionAccess {
        // Arrange
        when(managerDB.selectProcessById(TEST_ID)).thenReturn(testProcess);
        doNothing().when(managerDB).updateProcessStatus(anyString(), anyString());
        doNothing().when(managerDB).updateProcessFinishedBy(anyString(), any());
        
        // Act
        Process result = repository.finishProcess(TEST_ID, null, true);
        
        // Assert
        assertNotNull(result);
        assertEquals("Finished", result.getStatus());
        verify(managerDB, times(1)).selectProcessById(TEST_ID);
        verify(managerDB, times(1)).updateProcessStatus(TEST_ID, "Finished");
        verify(managerDB, times(1)).updateProcessFinishedBy(eq(TEST_ID), any());
    }
    
    @Test
    @DisplayName("Should finish process with valid user call")
    void testFinishProcessUserCall() throws ExceptionDB, ExceptionFound, ExceptionAccess {
        // Arrange
        when(managerDB.selectProcessById(TEST_ID)).thenReturn(testProcess);
        doNothing().when(managerDB).updateProcessStatus(anyString(), anyString());
        doNothing().when(managerDB).updateProcessFinishedBy(anyString(), any());
        
        // Act
        Process result = repository.finishProcess(TEST_ID, TEST_CODE, false);
        
        // Assert
        assertNotNull(result);
        assertEquals("Finished", result.getStatus());
        verify(managerDB, times(1)).selectProcessById(TEST_ID);
        verify(managerDB, times(1)).updateProcessStatus(TEST_ID, "Finished");
        verify(managerDB, times(1)).updateProcessFinishedBy(eq(TEST_ID), any());
    }
    
    @Test
    @DisplayName("Should throw ExceptionAccess with invalid secure code")
    void testFinishProcessInvalidSecureCode() throws ExceptionDB, ExceptionFound {
        // Arrange
        when(managerDB.selectProcessById(TEST_ID)).thenReturn(testProcess);
        
        // Act & Assert
        assertThrows(ExceptionAccess.class, () -> {
            repository.finishProcess(TEST_ID, "wrong_code", false);
        });
        verify(managerDB, times(1)).selectProcessById(TEST_ID);
        verify(managerDB, never()).updateProcessStatus(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should throw ExceptionFound when finishing non-existent process")
    void testFinishProcessNotFound() throws ExceptionDB {
        // Arrange
        when(managerDB.selectProcessById(anyString())).thenReturn(null);
        
        // Act & Assert
        assertThrows(ExceptionFound.class, () -> {
            repository.finishProcess("non_existent_id", null, true);
        });
        verify(managerDB, times(1)).selectProcessById("non_existent_id");
        verify(managerDB, never()).updateProcessStatus(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should propagate ExceptionDB from finishProcess")
    void testFinishProcessThrowsExceptionDB() throws ExceptionDB {
        // Arrange
        when(managerDB.selectProcessById(anyString()))
            .thenThrow(new ExceptionDB("DB Error", "test", "error", 500));
        
        // Act & Assert
        assertThrows(ExceptionDB.class, () -> {
            repository.finishProcess(TEST_ID, TEST_CODE, false);
        });
        verify(managerDB, times(1)).selectProcessById(TEST_ID);
    }
}