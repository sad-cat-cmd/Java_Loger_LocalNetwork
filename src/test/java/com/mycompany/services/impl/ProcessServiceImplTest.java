package com.mycompany.services.impl;

import com.mycompany.services.ProcessService;
import com.mycompany.models.Process;
import com.mycompany.database.ExceptionDB;
import com.mycompany.database.DatabaseManager;
import com.mycompany.repositories.ProcessRepository;
import com.mycompany.repositories.ExceptionAccess;
import com.mycompany.repositories.ExceptionFound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для {@link ProcessServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class ProcessServiceImplTest {

    @Mock
    private ProcessRepository repository;

    @Mock
    private DatabaseManager manager;

    private ProcessService service;

    // Константы для тестов
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_OWNER_LENGTH = 100;

    @BeforeEach
    void setUp() throws Exception {
        // Создаём сервис через рефлексию для внедрения мока репозитория
        service = new ProcessServiceImpl(manager);
        
        // Внедряем мок репозитория через рефлексию
        java.lang.reflect.Field repositoryField = 
            ProcessServiceImpl.class.getDeclaredField("repository");
        repositoryField.setAccessible(true);
        repositoryField.set(service, repository);
    }

    // ==================== ТЕСТЫ ДЛЯ add() ====================

    @Test
    void add_ShouldSaveProcessAndReturnSaved() throws Exception {
        // Arrange
        Process inputProcess = new Process("billing", "admin", MAX_NAME_LENGTH, MAX_OWNER_LENGTH);
        Process expectedProcess = new Process(
            "proc_123",
            "billing",
            "admin",
            Process.STATUS_ACTIVE,
            "unique_456",
            LocalDateTime.now(),
            null,
            0
        );

        when(repository.save(inputProcess)).thenReturn(expectedProcess);

        // Act
        Process actualProcess = service.add(inputProcess);

        // Assert
        assertNotNull(actualProcess);
        assertEquals("proc_123", actualProcess.getID());
        assertEquals("billing", actualProcess.getName());
        assertEquals("admin", actualProcess.getOwner());
        assertEquals(Process.STATUS_ACTIVE, actualProcess.getStatus());

        verify(repository, times(1)).save(inputProcess);
    }

    @Test
    void add_ShouldThrowExceptionDB_WhenRepositoryThrows() throws Exception {
        // Arrange
        Process inputProcess = new Process("billing", "admin", MAX_NAME_LENGTH, MAX_OWNER_LENGTH);
        ExceptionDB expectedException = new ExceptionDB(
            "DB save error",
            "ProcessRepositoryImpl.save()",
            "Failed to save process",
            500
        );
        when(repository.save(inputProcess)).thenThrow(expectedException);

        // Act & Assert
        ExceptionDB thrown = assertThrows(ExceptionDB.class, () -> service.add(inputProcess));
        assertEquals("Failed to save process", thrown.getClientMsg());
        assertEquals(500, thrown.getClientCode());
        verify(repository, times(1)).save(inputProcess);
    }

    // ==================== ТЕСТЫ ДЛЯ getAll() ====================

    @Test
    void getAll_ShouldReturnAllProcesses() throws Exception {
        // Arrange
        List<Process> expectedProcesses = Arrays.asList(
            new Process(
                "proc_001", "billing", "admin", Process.STATUS_ACTIVE,
                "code_001", LocalDateTime.now().minusHours(2), null, 100
            ),
            new Process(
                "proc_002", "logging", "user1", Process.STATUS_FINISHED,
                "code_002", LocalDateTime.now().minusDays(1), 
                LocalDateTime.now().minusHours(1), 250
            )
        );
        when(repository.findAll()).thenReturn(expectedProcesses);

        // Act
        List<Process> actualProcesses = service.getAll();

        // Assert
        assertNotNull(actualProcesses);
        assertEquals(2, actualProcesses.size());
        assertEquals("proc_001", actualProcesses.get(0).getID());
        assertEquals("billing", actualProcesses.get(0).getName());
        assertEquals("proc_002", actualProcesses.get(1).getID());
        assertEquals("logging", actualProcesses.get(1).getName());
        verify(repository, times(1)).findAll();
    }

    @Test
    void getAll_ShouldThrowExceptionDB_WhenRepositoryThrows() throws Exception {
        // Arrange
        ExceptionDB expectedException = new ExceptionDB(
            "DB read error",
            "ProcessRepositoryImpl.findAll()",
            "Failed to read processes",
            500
        );
        when(repository.findAll()).thenThrow(expectedException);

        // Act & Assert
        ExceptionDB thrown = assertThrows(ExceptionDB.class, () -> service.getAll());
        assertEquals("Failed to read processes", thrown.getClientMsg());
        verify(repository, times(1)).findAll();
    }

    // ==================== ТЕСТЫ ДЛЯ getAllActive() ====================

    @Test
    void getAllActive_ShouldReturnActiveProcesses() throws Exception {
        // Arrange
        List<Process> expectedProcesses = Arrays.asList(
            new Process(
                "proc_001", "billing", "admin", Process.STATUS_ACTIVE,
                "code_001", LocalDateTime.now().minusHours(2), null, 100
            ),
            new Process(
                "proc_003", "notification", "user2", Process.STATUS_ACTIVE,
                "code_003", LocalDateTime.now().minusMinutes(30), null, 50
            )
        );
        when(repository.findAllActive()).thenReturn(expectedProcesses);

        // Act
        List<Process> actualProcesses = service.getAllActive();

        // Assert
        assertNotNull(actualProcesses);
        assertEquals(2, actualProcesses.size());
        assertTrue(actualProcesses.get(0).isActive());
        assertTrue(actualProcesses.get(1).isActive());
        verify(repository, times(1)).findAllActive();
    }

    @Test
    void getAllActive_ShouldThrowExceptionDB_WhenRepositoryThrows() throws Exception {
        // Arrange
        ExceptionDB expectedException = new ExceptionDB(
            "DB read error",
            "ProcessRepositoryImpl.findAllActive()",
            "Failed to read active processes",
            500
        );
        when(repository.findAllActive()).thenThrow(expectedException);

        // Act & Assert
        ExceptionDB thrown = assertThrows(ExceptionDB.class, () -> service.getAllActive());
        assertEquals("Failed to read active processes", thrown.getClientMsg());
        verify(repository, times(1)).findAllActive();
    }

    // ==================== ТЕСТЫ ДЛЯ search() ====================

    @Test
    void search_ShouldReturnProcess_WhenFound() throws Exception {
        // Arrange
        String processId = "proc_123";
        Process expectedProcess = new Process(
            processId,
            "billing",
            "admin",
            Process.STATUS_ACTIVE,
            "unique_456",
            LocalDateTime.now(),
            null,
            0
        );
        when(repository.findById(processId)).thenReturn(expectedProcess);

        // Act
        Process actualProcess = service.search(processId);

        // Assert
        assertNotNull(actualProcess);
        assertEquals(processId, actualProcess.getID());
        assertEquals("billing", actualProcess.getName());
        verify(repository, times(1)).findById(processId);
    }

    @Test
    void search_ShouldThrowExceptionFound_WhenNotFound() throws Exception {
        // Arrange
        String processId = "non_existent";
        when(repository.findById(processId)).thenThrow(new ExceptionFound());

        // Act & Assert
        assertThrows(ExceptionFound.class, () -> service.search(processId));
        verify(repository, times(1)).findById(processId);
    }

    @Test
    void search_ShouldThrowExceptionDB_WhenRepositoryThrows() throws Exception {
        // Arrange
        String processId = "proc_123";
        ExceptionDB expectedException = new ExceptionDB(
            "DB read error",
            "ProcessRepositoryImpl.findById()",
            "Failed to find process",
            500
        );
        when(repository.findById(processId)).thenThrow(expectedException);

        // Act & Assert
        ExceptionDB thrown = assertThrows(ExceptionDB.class, () -> service.search(processId));
        assertEquals("Failed to find process", thrown.getClientMsg());
        verify(repository, times(1)).findById(processId);
    }

    // ==================== ТЕСТЫ ДЛЯ finish() ====================

    @Test
    void finish_ShouldFinishProcess_WhenValid() throws Exception {
        // Arrange
        String processId = "proc_123";
        String secureString = "secret";
        boolean flagSystemCall = true;

        Process expectedProcess = new Process(
            processId,
            "billing",
            "admin",
            Process.STATUS_FINISHED,
            "unique_456",
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now(),
            100
        );

        when(repository.finishProcess(processId, secureString, flagSystemCall))
                .thenReturn(expectedProcess);

        // Act
        Process actualProcess = service.finish(processId, secureString, flagSystemCall);

        // Assert
        assertNotNull(actualProcess);
        assertEquals(processId, actualProcess.getID());
        assertEquals(Process.STATUS_FINISHED, actualProcess.getStatus());
        assertTrue(actualProcess.isFinished());
        assertNotNull(actualProcess.getTimeEndWork());
        verify(repository, times(1)).finishProcess(processId, secureString, flagSystemCall);
    }

    @Test
    void finish_ShouldThrowExceptionFound_WhenProcessNotFound() throws Exception {
        // Arrange
        String processId = "non_existent";
        String secureString = "secret";
        when(repository.finishProcess(processId, secureString, true))
                .thenThrow(new ExceptionFound());

        // Act & Assert
        assertThrows(ExceptionFound.class, () -> service.finish(processId, secureString, true));
        verify(repository, times(1)).finishProcess(processId, secureString, true);
    }

    @Test
    void finish_ShouldThrowExceptionAccess_WhenNoPermission() throws Exception {
        // Arrange
        String processId = "proc_123";
        String secureString = "wrong_secret";
        when(repository.finishProcess(processId, secureString, true))
                .thenThrow(new ExceptionAccess());

        // Act & Assert
        assertThrows(ExceptionAccess.class, () -> service.finish(processId, secureString, true));
        verify(repository, times(1)).finishProcess(processId, secureString, true);
    }

    @Test
    void finish_ShouldThrowExceptionDB_WhenRepositoryThrows() throws Exception {
        // Arrange
        String processId = "proc_123";
        String secureString = "secret";
        ExceptionDB expectedException = new ExceptionDB(
            "DB update error",
            "ProcessRepositoryImpl.finishProcess()",
            "Failed to finish process",
            500
        );
        when(repository.finishProcess(processId, secureString, true))
                .thenThrow(expectedException);

        // Act & Assert
        ExceptionDB thrown = assertThrows(ExceptionDB.class, 
            () -> service.finish(processId, secureString, true));
        assertEquals("Failed to finish process", thrown.getClientMsg());
        verify(repository, times(1)).finishProcess(processId, secureString, true);
    }
}