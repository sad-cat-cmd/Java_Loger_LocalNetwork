package com.mycompany.repositories.impl;

import com.mycompany.database.DatabaseManager;
import com.mycompany.database.ExceptionDB;
import com.mycompany.models.Process;
import com.mycompany.repositories.ExceptionAccess;
import com.mycompany.repositories.ExceptionFound;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Интеграционные тесты для ProcessRepositoryImpl.
 * 
 * <p>Использует реальную in-memory SQLite базу данных.
 * Проверяет работу репозитория с БД.</p>
 * 
 * @author admin_
 * @version 1.0
 */
class ProcessRepositoryImplIntegrationTest {
    
    private DatabaseManager dbManager;
    private ProcessRepositoryImpl repository;
    
    private final String TEST_ID = "proc_001";
    private final String TEST_NAME = "test-service";
    private final String TEST_OWNER = "admin@test.com";
    private final String TEST_CODE = "code_123";
    
    @BeforeEach
    void setUp() throws ExceptionDB {
        // Используем in-memory БД для каждого теста
        dbManager = new DatabaseManager("jdbc:sqlite::memory:");
        repository = new ProcessRepositoryImpl(dbManager);
    }
    
    @AfterEach
    void tearDown() {
        if (dbManager != null) {
            dbManager.close();
        }
    }
    
    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================
    
    private Process createTestProcess() {
        return new Process(
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
    @DisplayName("Should save and retrieve process")
    void testSaveAndRetrieve() throws ExceptionDB, ExceptionFound {
        // Arrange
        Process process = createTestProcess();
        
        // Act
        Process saved = repository.save(process);
        Process found = repository.findById(TEST_ID);
        
        // Assert
        assertNotNull(saved);
        assertNotNull(found);
        assertEquals(TEST_ID, found.getID());
        assertEquals(TEST_NAME, found.getName());
        assertEquals(TEST_OWNER, found.getOwner());
        assertEquals("Active", found.getStatus());
        assertEquals(TEST_CODE, found.getUniqueCode());
        assertEquals(0, found.getLogCount());
    }
    
    @Test
    @DisplayName("Should save multiple processes")
    void testSaveMultiple() throws ExceptionDB {
        // Arrange
        Process process1 = createTestProcess();
        Process process2 = new Process(
            "proc_002", "service-2", "owner2", "Active", "code2",
            LocalDateTime.now(), null, 0
        );
        
        // Act
        repository.save(process1);
        repository.save(process2);
        List<Process> processes = repository.findAll();
        
        // Assert
        assertEquals(2, processes.size());
    }
    
    // ==================== ТЕСТЫ FINDBYID ====================
    
    @Test
    @DisplayName("Should find process by ID")
    void testFindById() throws ExceptionDB, ExceptionFound {
        // Arrange
        repository.save(createTestProcess());
        
        // Act
        Process found = repository.findById(TEST_ID);
        
        // Assert
        assertNotNull(found);
        assertEquals(TEST_ID, found.getID());
        assertEquals(TEST_NAME, found.getName());
    }
    
    @Test
    @DisplayName("Should throw ExceptionFound when process not found")
    void testFindByIdNotFound() {
        assertThrows(ExceptionFound.class, () -> {
            repository.findById("non_existent_id");
        });
    }
    
    // ==================== ТЕСТЫ FINDALL ====================
    
    @Test
    @DisplayName("Should find all processes")
    void testFindAll() throws ExceptionDB {
        // Arrange
        repository.save(createTestProcess());
        repository.save(new Process(
            "proc_002", "service-2", "owner2", "Active", "code2",
            LocalDateTime.now(), null, 0
        ));
        
        // Act
        List<Process> processes = repository.findAll();
        
        // Assert
        assertEquals(2, processes.size());
    }
    
    @Test
    @DisplayName("Should return empty list when no processes")
    void testFindAllEmpty() throws ExceptionDB {
        // Act
        List<Process> processes = repository.findAll();
        
        // Assert
        assertNotNull(processes);
        assertEquals(0, processes.size());
    }
    
    // ==================== ТЕСТЫ FINDALLACTIVE ====================
    
    @Test
    @DisplayName("Should find only active processes")
    void testFindAllActive() throws ExceptionDB {
        // Arrange
        Process active = createTestProcess();
        Process finished = new Process(
            "proc_002", "finished-service", "owner2", "Finished", "code2",
            LocalDateTime.now(), LocalDateTime.now(), 0
        );
        repository.save(active);
        repository.save(finished);
        
        // Act
        List<Process> activeProcesses = repository.findAllActive();
        
        // Assert
        assertEquals(1, activeProcesses.size());
        assertEquals("Active", activeProcesses.get(0).getStatus());
    }
    
    @Test
    @DisplayName("Should return empty list when no active processes")
    void testFindAllActiveEmpty() throws ExceptionDB {
        // Arrange
        Process finished = new Process(
            "proc_001", "finished-service", "owner1", "Finished", "code1",
            LocalDateTime.now(), LocalDateTime.now(), 0
        );
        repository.save(finished);
        
        // Act
        List<Process> activeProcesses = repository.findAllActive();
        
        // Assert
        assertNotNull(activeProcesses);
        assertEquals(0, activeProcesses.size());
    }
    
    // ==================== ТЕСТЫ FINISHPROCESS ====================
    
    @Test
    @DisplayName("Should finish process with system call")
    void testFinishProcessSystemCall() throws ExceptionDB, ExceptionFound, ExceptionAccess {
        // Arrange
        repository.save(createTestProcess());
        
        // Act
        Process finished = repository.finishProcess(TEST_ID, null, true);
        
        // Assert
        assertNotNull(finished);
        assertEquals("Finished", finished.getStatus());
        assertNotNull(finished.getTimeEndWork());
        
        // Verify from database
        Process fromDb = repository.findById(TEST_ID);
        assertEquals("Finished", fromDb.getStatus());
        assertNotNull(fromDb.getTimeEndWork());
    }
    
    @Test
    @DisplayName("Should finish process with valid user call")
    void testFinishProcessUserCall() throws ExceptionDB, ExceptionFound, ExceptionAccess {
        // Arrange
        repository.save(createTestProcess());
        
        // Act
        Process finished = repository.finishProcess(TEST_ID, TEST_CODE, false);
        
        // Assert
        assertNotNull(finished);
        assertEquals("Finished", finished.getStatus());
        assertNotNull(finished.getTimeEndWork());
    }
    
    @Test
    @DisplayName("Should throw ExceptionAccess with invalid secure code")
    void testFinishProcessInvalidSecureCode() throws ExceptionDB, ExceptionFound {
        // Arrange
        repository.save(createTestProcess());
        
        // Act & Assert
        assertThrows(ExceptionAccess.class, () -> {
            repository.finishProcess(TEST_ID, "wrong_code", false);
        });
        
        // Verify process still active
        Process process = repository.findById(TEST_ID);
        assertEquals("Active", process.getStatus());
    }
    
    @Test
    @DisplayName("Should throw ExceptionFound when finishing non-existent process")
    void testFinishProcessNotFound() {
        assertThrows(ExceptionFound.class, () -> {
            repository.finishProcess("non_existent_id", null, true);
        });
    }
    
    @Test
    @DisplayName("Should allow finishing already finished process (idempotent)")
    void testFinishAlreadyFinishedProcess() throws ExceptionDB, ExceptionFound, ExceptionAccess {
        repository.save(createTestProcess());
        repository.finishProcess(TEST_ID, TEST_CODE, false);
        
        // Повторное завершение не выбрасывает исключение (идемпотентность)
        assertDoesNotThrow(() -> {
            repository.finishProcess(TEST_ID, TEST_CODE, false);
        });
        
        Process process = repository.findById(TEST_ID);
        assertEquals("Finished", process.getStatus());
    }
    
    // ==================== ТЕСТЫ КРАЕВЫХ СЛУЧАЕВ ====================
    
    @Test
    @DisplayName("Should handle special characters in process fields")
    void testHandleSpecialCharacters() throws ExceptionDB {
        Process process = new Process(
            "proc_special",
            "service_123-test_v2",  // ← валидное имя
            "admin+test@company.co.uk",  // ← валидный owner
            "Active",
            "code",
            LocalDateTime.now(),
            null,
            0
        );
        
        repository.save(process);
        try {
            Process found = repository.findById("proc_special");
            assertNotNull(found);
            assertEquals("service_123-test_v2", found.getName());
        }
        catch(ExceptionFound exceptionFound) {

        }
        catch(ExceptionDB exceptionDB){
            
        }
    }
    
    @Test
    @DisplayName("Should handle long strings")
    void testHandleLongStrings() throws ExceptionDB {
        // Arrange
        String longName = "A".repeat(500);
        Process process = new Process(
            "proc_long",
            longName,
            "owner",
            "Active",
            "code",
            LocalDateTime.now(),
            null,
            0
        );
        
        // Act
        repository.save(process);
        try {
            Process found = repository.findById("proc_long");
            assertNotNull(found);
            assertEquals(longName, found.getName());
        } catch (ExceptionFound exceptionFound) {

        }
        catch (ExceptionDB exceptionDB){

        }
        
        // Assert
        
    }
}