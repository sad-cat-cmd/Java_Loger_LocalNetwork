package com.mycompany.services.impl;

import com.mycompany.database.DatabaseManager;
import com.mycompany.database.ExceptionDB;
import com.mycompany.models.DataLog;
import com.mycompany.models.LogStatus;
import com.mycompany.repositories.DataLogRepository;
import com.mycompany.repositories.ExceptionAccess;
import com.mycompany.repositories.ExceptionFound;
import com.mycompany.repositories.impl.DataLogRepositoryImpl;
import com.mycompany.services.DataLogService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Модульные тесты для DataLogServiceImpl.
 * 
 * <p>Использует Mockito для мокирования DatabaseManager и DataLogRepository.</p>
 * 
 * @author admin_
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class DataLogServiceImplTest {
    
    @Mock
    private DatabaseManager dbManager;
    
    @Mock
    private DataLogRepository repository;
    
    private DataLogService service;
    
    private DataLog testLog;
    private final String TEST_PROCESS_ID = "proc_001";
    private final String TEST_SECURE_CODE = "secure_code_123";
    private final String TEST_MESSAGE = "Test log message";
    private final String TEST_STATUS = "INFO";
    private final int TEST_NUMBER_LOG = 1;
    
    @BeforeEach
    void setUp() throws Exception {
        // Создаем сервис через конструктор с DatabaseManager
        service = new DataLogServiceImpl(dbManager);
        
        // Внедряем мок репозитория через рефлексию
        java.lang.reflect.Field repositoryField = 
            DataLogServiceImpl.class.getDeclaredField("repository");
        repositoryField.setAccessible(true);
        repositoryField.set(service, repository);
        
        testLog = new DataLog(
            TEST_MESSAGE,
            TEST_STATUS,
            TEST_NUMBER_LOG,
            LocalDateTime.now()
        );
    }
    
    // ==================== ТЕСТЫ ADD ====================
    
    @Test
    @DisplayName("Should add log successfully")
    void testAddSuccess() throws Exception {
        // Arrange
        when(repository.save(testLog, TEST_PROCESS_ID, TEST_SECURE_CODE))
            .thenReturn(testLog);
        
        // Act
        DataLog result = service.add(testLog, TEST_PROCESS_ID, TEST_SECURE_CODE);
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_MESSAGE, result.getLogInfo());
        verify(repository, times(1)).save(testLog, TEST_PROCESS_ID, TEST_SECURE_CODE);
    }
    
    @Test
    @DisplayName("Should propagate ExceptionFound from add")
    void testAddPropagatesExceptionFound() throws Exception {
        // Arrange
        doThrow(new ExceptionFound()).when(repository)
            .save(testLog, TEST_PROCESS_ID, TEST_SECURE_CODE);
        
        // Act & Assert
        assertThrows(ExceptionFound.class, () -> {
            service.add(testLog, TEST_PROCESS_ID, TEST_SECURE_CODE);
        });
        verify(repository, times(1)).save(testLog, TEST_PROCESS_ID, TEST_SECURE_CODE);
    }
    
    @Test
    @DisplayName("Should propagate ExceptionAccess from add")
    void testAddPropagatesExceptionAccess() throws Exception {
        // Arrange
        doThrow(new ExceptionAccess()).when(repository)
            .save(testLog, TEST_PROCESS_ID, TEST_SECURE_CODE);
        
        // Act & Assert
        assertThrows(ExceptionAccess.class, () -> {
            service.add(testLog, TEST_PROCESS_ID, TEST_SECURE_CODE);
        });
        verify(repository, times(1)).save(testLog, TEST_PROCESS_ID, TEST_SECURE_CODE);
    }
    
    @Test
    @DisplayName("Should propagate ExceptionDB from add")
    void testAddPropagatesExceptionDB() throws Exception {
        // Arrange
        ExceptionDB dbException = new ExceptionDB("DB Error", "test", "error", 500);
        doThrow(dbException).when(repository)
            .save(testLog, TEST_PROCESS_ID, TEST_SECURE_CODE);
        
        // Act & Assert
        assertThrows(ExceptionDB.class, () -> {
            service.add(testLog, TEST_PROCESS_ID, TEST_SECURE_CODE);
        });
        verify(repository, times(1)).save(testLog, TEST_PROCESS_ID, TEST_SECURE_CODE);
    }
    
    // ==================== ТЕСТЫ GETALL ====================
    
    @Test
    @DisplayName("Should get all logs successfully")
    void testGetAllSuccess() throws Exception {
        // Arrange
        List<DataLog> expectedLogs = Arrays.asList(testLog);
        when(repository.findAllLogProcess(TEST_PROCESS_ID))
            .thenReturn(expectedLogs);
        
        // Act
        List<DataLog> result = service.getAll(TEST_PROCESS_ID);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_MESSAGE, result.get(0).getLogInfo());
        verify(repository, times(1)).findAllLogProcess(TEST_PROCESS_ID);
    }
    
    @Test
    @DisplayName("Should return empty list when no logs")
    void testGetAllEmpty() throws Exception {
        // Arrange
        when(repository.findAllLogProcess(TEST_PROCESS_ID))
            .thenReturn(Collections.emptyList());
        
        // Act
        List<DataLog> result = service.getAll(TEST_PROCESS_ID);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAllLogProcess(TEST_PROCESS_ID);
    }
    
    @Test
    @DisplayName("Should propagate ExceptionFound from getAll")
    void testGetAllPropagatesExceptionFound() throws Exception {
        // Arrange
        doThrow(new ExceptionFound()).when(repository)
            .findAllLogProcess(TEST_PROCESS_ID);
        
        // Act & Assert
        assertThrows(ExceptionFound.class, () -> {
            service.getAll(TEST_PROCESS_ID);
        });
        verify(repository, times(1)).findAllLogProcess(TEST_PROCESS_ID);
    }
    
    @Test
    @DisplayName("Should propagate ExceptionDB from getAll")
    void testGetAllPropagatesExceptionDB() throws Exception {
        // Arrange
        ExceptionDB dbException = new ExceptionDB("DB Error", "test", "error", 500);
        doThrow(dbException).when(repository)
            .findAllLogProcess(TEST_PROCESS_ID);
        
        // Act & Assert
        assertThrows(ExceptionDB.class, () -> {
            service.getAll(TEST_PROCESS_ID);
        });
        verify(repository, times(1)).findAllLogProcess(TEST_PROCESS_ID);
    }
    
    // ==================== ТЕСТЫ GETBYINDEXFROM ====================
    
    @Test
    @DisplayName("Should get logs from index successfully")
    void testGetByIndexFromSuccess() throws Exception {
        // Arrange
        int startIndex = 5;
        List<DataLog> expectedLogs = Arrays.asList(testLog);
        when(repository.findIdByIndexFrom(TEST_PROCESS_ID, startIndex))
            .thenReturn(expectedLogs);
        
        // Act
        List<DataLog> result = service.getByIndexFrom(TEST_PROCESS_ID, startIndex);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findIdByIndexFrom(TEST_PROCESS_ID, startIndex);
    }
    
    @Test
    @DisplayName("Should return empty list when no logs from index")
    void testGetByIndexFromEmpty() throws Exception {
        // Arrange
        int startIndex = 10;
        when(repository.findIdByIndexFrom(TEST_PROCESS_ID, startIndex))
            .thenReturn(Collections.emptyList());
        
        // Act
        List<DataLog> result = service.getByIndexFrom(TEST_PROCESS_ID, startIndex);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findIdByIndexFrom(TEST_PROCESS_ID, startIndex);
    }
    
    @Test
    @DisplayName("Should propagate ExceptionFound from getByIndexFrom")
    void testGetByIndexFromPropagatesExceptionFound() throws Exception {
        // Arrange
        int startIndex = 5;
        doThrow(new ExceptionFound()).when(repository)
            .findIdByIndexFrom(TEST_PROCESS_ID, startIndex);
        
        // Act & Assert
        assertThrows(ExceptionFound.class, () -> {
            service.getByIndexFrom(TEST_PROCESS_ID, startIndex);
        });
        verify(repository, times(1)).findIdByIndexFrom(TEST_PROCESS_ID, startIndex);
    }
    
    @Test
    @DisplayName("Should propagate ExceptionDB from getByIndexFrom")
    void testGetByIndexFromPropagatesExceptionDB() throws Exception {
        // Arrange
        int startIndex = 5;
        ExceptionDB dbException = new ExceptionDB("DB Error", "test", "error", 500);
        doThrow(dbException).when(repository)
            .findIdByIndexFrom(TEST_PROCESS_ID, startIndex);
        
        // Act & Assert
        assertThrows(ExceptionDB.class, () -> {
            service.getByIndexFrom(TEST_PROCESS_ID, startIndex);
        });
        verify(repository, times(1)).findIdByIndexFrom(TEST_PROCESS_ID, startIndex);
    }
    
    // ==================== ТЕСТЫ GETALLCONTAINSSTATUSES ====================
    
    @Test
    @DisplayName("Should get logs by statuses successfully")
    void testGetAllContainsStatusesSuccess() throws Exception {
        // Arrange
        List<LogStatus> statuses = Arrays.asList(
            new LogStatus("INFO"),
            new LogStatus("WARN")
        );
        List<DataLog> expectedLogs = Arrays.asList(testLog);
        when(repository.findIdAndByStatuses(TEST_PROCESS_ID, statuses))
            .thenReturn(expectedLogs);
        
        // Act
        List<DataLog> result = service.getAllContainsStatuses(TEST_PROCESS_ID, statuses);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findIdAndByStatuses(TEST_PROCESS_ID, statuses);
    }
    
    @Test
    @DisplayName("Should return empty list when no logs match statuses")
    void testGetAllContainsStatusesEmpty() throws Exception {
        // Arrange
        List<LogStatus> statuses = Arrays.asList(new LogStatus("FATAL"));
        when(repository.findIdAndByStatuses(TEST_PROCESS_ID, statuses))
            .thenReturn(Collections.emptyList());
        
        // Act
        List<DataLog> result = service.getAllContainsStatuses(TEST_PROCESS_ID, statuses);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findIdAndByStatuses(TEST_PROCESS_ID, statuses);
    }
    
    @Test
    @DisplayName("Should propagate ExceptionFound from getAllContainsStatuses")
    void testGetAllContainsStatusesPropagatesExceptionFound() throws Exception {
        // Arrange
        List<LogStatus> statuses = Arrays.asList(new LogStatus("INFO"));
        doThrow(new ExceptionFound()).when(repository)
            .findIdAndByStatuses(TEST_PROCESS_ID, statuses);
        
        // Act & Assert
        assertThrows(ExceptionFound.class, () -> {
            service.getAllContainsStatuses(TEST_PROCESS_ID, statuses);
        });
        verify(repository, times(1)).findIdAndByStatuses(TEST_PROCESS_ID, statuses);
    }
    
    @Test
    @DisplayName("Should propagate ExceptionDB from getAllContainsStatuses")
    void testGetAllContainsStatusesPropagatesExceptionDB() throws Exception {
        // Arrange
        List<LogStatus> statuses = Arrays.asList(new LogStatus("INFO"));
        ExceptionDB dbException = new ExceptionDB("DB Error", "test", "error", 500);
        doThrow(dbException).when(repository)
            .findIdAndByStatuses(TEST_PROCESS_ID, statuses);
        
        // Act & Assert
        assertThrows(ExceptionDB.class, () -> {
            service.getAllContainsStatuses(TEST_PROCESS_ID, statuses);
        });
        verify(repository, times(1)).findIdAndByStatuses(TEST_PROCESS_ID, statuses);
    }
    
    // ==================== ТЕСТЫ GETALLCONTAINSSUBSTRING ====================
    
    @Test
    @DisplayName("Should get logs by substring successfully")
    void testGetAllContainsSubstringSuccess() throws Exception {
        // Arrange
        String substring = "payment";
        List<DataLog> expectedLogs = Arrays.asList(testLog);
        when(repository.findIdAndBySubstring(TEST_PROCESS_ID, substring))
            .thenReturn(expectedLogs);
        
        // Act
        List<DataLog> result = service.getAllContainsSubstring(TEST_PROCESS_ID, substring);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findIdAndBySubstring(TEST_PROCESS_ID, substring);
    }
    
    @Test
    @DisplayName("Should return empty list when substring not found")
    void testGetAllContainsSubstringEmpty() throws Exception {
        // Arrange
        String substring = "notfound";
        when(repository.findIdAndBySubstring(TEST_PROCESS_ID, substring))
            .thenReturn(Collections.emptyList());
        
        // Act
        List<DataLog> result = service.getAllContainsSubstring(TEST_PROCESS_ID, substring);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findIdAndBySubstring(TEST_PROCESS_ID, substring);
    }
    
    @Test
    @DisplayName("Should propagate ExceptionFound from getAllContainsSubstring")
    void testGetAllContainsSubstringPropagatesExceptionFound() throws Exception {
        // Arrange
        String substring = "test";
        doThrow(new ExceptionFound()).when(repository)
            .findIdAndBySubstring(TEST_PROCESS_ID, substring);
        
        // Act & Assert
        assertThrows(ExceptionFound.class, () -> {
            service.getAllContainsSubstring(TEST_PROCESS_ID, substring);
        });
        verify(repository, times(1)).findIdAndBySubstring(TEST_PROCESS_ID, substring);
    }
    
    @Test
    @DisplayName("Should propagate ExceptionDB from getAllContainsSubstring")
    void testGetAllContainsSubstringPropagatesExceptionDB() throws Exception {
        // Arrange
        String substring = "test";
        ExceptionDB dbException = new ExceptionDB("DB Error", "test", "error", 500);
        doThrow(dbException).when(repository)
            .findIdAndBySubstring(TEST_PROCESS_ID, substring);
        
        // Act & Assert
        assertThrows(ExceptionDB.class, () -> {
            service.getAllContainsSubstring(TEST_PROCESS_ID, substring);
        });
        verify(repository, times(1)).findIdAndBySubstring(TEST_PROCESS_ID, substring);
    }
}