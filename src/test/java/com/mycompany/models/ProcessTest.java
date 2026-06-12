package com.mycompany.models;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тестовый класс для Process.
 * 
 * <p>Проверяет:
 * <ul>
 *   <li>Создание процессов (новых и из БД)</li>
 *   <li>Валидацию имени и владельца</li>
 *   <li>Изменение состояния (завершение, инкремент логов)</li>
 *   <li>Форматирование времени выполнения</li>
 *   <li>Краевые случаи и ошибки</li>
 * </ul>
 * </p>
 * 
 * @author admin_
 * @version 1.0
 */
class ProcessTest {
    
    private static final String VALID_NAME = "billing-service";
    private static final String VALID_OWNER = "admin@company.com";
    private static final int DEFAULT_MAX_NAME = 100;
    private static final int DEFAULT_MAX_OWNER = 50;
    
    @BeforeEach
    void setUp() {
        System.out.println("Running Process test...");
    }
    
    // ==================== Успешное создание ====================
    
    @Test
    @DisplayName("Should create new process with valid parameters")
    void testCreateNewProcess() throws ExceptionProcess {
        Process process = new Process(VALID_NAME, VALID_OWNER, DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        
        assertNotNull(process);
        assertNotNull(process.getID());
        assertNotNull(process.getUniqueCode());
        assertEquals(VALID_NAME, process.getName());
        assertEquals(VALID_OWNER, process.getOwner());
        assertEquals(Process.STATUS_ACTIVE, process.getStatus());
        assertNotNull(process.getTimeCreate());
        assertEquals(0, process.getLogCount());
        assertTrue(process.isActive());
        assertFalse(process.isFinished());
    }
    
    @Test
    @DisplayName("Should generate unique ID for each process")
    void testGenerateUniqueIds() throws ExceptionProcess {
        Process process1 = new Process("service-1", "owner1", DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        Process process2 = new Process("service-2", "owner2", DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        
        assertNotEquals(process1.getID(), process2.getID());
        assertNotEquals(process1.getUniqueCode(), process2.getUniqueCode());
    }
    
    @Test
    @DisplayName("Should generate unique IDs for many processes")
    void testManyUniqueIds() throws ExceptionProcess {
        Set<String> ids = new HashSet<>();
        Set<String> codes = new HashSet<>();
        
        for (int i = 0; i < 100; i++) {
            Process process = new Process("service-" + i, "owner-" + i, 
                                          DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
            ids.add(process.getID());
            codes.add(process.getUniqueCode());
        }
        
        assertEquals(100, ids.size());
        assertEquals(100, codes.size());
    }
    
    // ==================== Валидация имени ====================
    
    @Test
    @DisplayName("Should accept valid name within max length")
    void testValidName() throws ExceptionProcess {
        String validName = "A".repeat(50);
        Process process = new Process(validName, VALID_OWNER, 100, 50);
        
        assertEquals(validName, process.getName());
    }
    
    @Test
    @DisplayName("Should accept name exactly at max length")
    void testNameExactlyAtMaxLength() throws ExceptionProcess {
        String exactName = "A".repeat(100);
        Process process = new Process(exactName, VALID_OWNER, 100, 50);
        
        assertEquals(exactName, process.getName());
    }
    
    @Test
    @DisplayName("Should reject name exceeding max length")
    void testNameExceedingMaxLength() {
        String longName = "A".repeat(101);
        
        ExceptionProcess exception = assertThrows(ExceptionProcess.class, () -> {
            new Process(longName, VALID_OWNER, 100, 50);
        });
        
        assertTrue(exception.getCombinedLogMsg().contains("length name > 100"));
    }
    
    @Test
    @DisplayName("Should reject null name")
    void testNullName() {
        assertThrows(ExceptionProcess.class, () -> {
            new Process(null, VALID_OWNER, 100, 50);
        });
    }
    
    @Test
    @DisplayName("Should reject empty name")
    void testEmptyName() {
        assertThrows(ExceptionProcess.class, () -> {
            new Process("", VALID_OWNER, DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        });
    }
    
    // ==================== Валидация владельца ====================
    
    @Test
    @DisplayName("Should accept valid owner within max length")
    void testValidOwner() throws ExceptionProcess {
        String validOwner = "user@example.com";
        Process process = new Process(VALID_NAME, validOwner, 100, 50);
        
        assertEquals(validOwner, process.getOwner());
    }
    
    @Test
    @DisplayName("Should accept owner exactly at max length")
    void testOwnerExactlyAtMaxLength() throws ExceptionProcess {
        String exactOwner = "A".repeat(50);
        Process process = new Process(VALID_NAME, exactOwner, 100, 50);
        
        assertEquals(exactOwner, process.getOwner());
    }
    
    @Test
    @DisplayName("Should reject owner exceeding max length")
    void testOwnerExceedingMaxLength() {
        String longOwner = "A".repeat(51);
        
        ExceptionProcess exception = assertThrows(ExceptionProcess.class, () -> {
            new Process(VALID_NAME, longOwner, 100, 50);
        });
        
        assertTrue(exception.getCombinedLogMsg().contains("length owner > 50"));
    }
    
    @Test
    @DisplayName("Should reject null owner")
    void testNullOwner() {
        assertThrows(ExceptionProcess.class, () -> {
            new Process(VALID_NAME, null, 100, 50);
        });
    }
    
    @Test
    @DisplayName("Should reject empty owner")
    void testEmptyOwner() {
        assertThrows(ExceptionProcess.class, () -> {
            new Process(VALID_NAME, "", DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        });
    }
    
    // ==================== Комбинированные ошибки ====================
    
    @Test
    @DisplayName("Should reject both name and owner when both invalid")
    void testBothNameAndOwnerInvalid() {
        String longName = "A".repeat(101);
        String longOwner = "B".repeat(51);
        
        ExceptionProcess exception = assertThrows(ExceptionProcess.class, () -> {
            new Process(longName, longOwner, 100, 50);
        });
        
        String errorMsg = exception.getCombinedLogMsg();
        assertTrue(errorMsg.contains("length name > 100"));
        assertTrue(errorMsg.contains("length owner > 50"));
    }
    
    // ==================== Конструктор для загрузки из БД ====================
    
    @Test
    @DisplayName("Should create process from database data")
    void testCreateFromDatabase() {
        LocalDateTime createTime = LocalDateTime.now().minusDays(5);
        LocalDateTime endTime = LocalDateTime.now().minusDays(1);
        String id = "proc_test_123";
        String uniqueCode = "unique_code_456";
        
        Process process = new Process(
            id, VALID_NAME, VALID_OWNER, Process.STATUS_FINISHED, uniqueCode,
            createTime, endTime, 1500
        );
        
        assertEquals(id, process.getID());
        assertEquals(VALID_NAME, process.getName());
        assertEquals(VALID_OWNER, process.getOwner());
        assertEquals(Process.STATUS_FINISHED, process.getStatus());
        assertEquals(uniqueCode, process.getUniqueCode());
        assertEquals(createTime.toString(), process.getTimeCreate());
        assertEquals(endTime.toString(), process.getTimeEndWork());
        assertEquals(1500, process.getLogCount());
        assertTrue(process.isFinished());
        assertFalse(process.isActive());
    }
    
    @Test
    @DisplayName("Should create process from database with null end time")
    void testCreateFromDatabaseWithNullEndTime() {
        LocalDateTime createTime = LocalDateTime.now().minusDays(5);
        String id = "proc_test_123";
        String uniqueCode = "unique_code_456";
        
        Process process = new Process(
            id, VALID_NAME, VALID_OWNER, Process.STATUS_ACTIVE, uniqueCode,
            createTime, null, 1500
        );
        
        assertEquals("process is not finished", process.getTimeEndWork());
        assertTrue(process.isActive());
        assertFalse(process.isFinished());
    }
    
    // ==================== Изменение состояния ====================
    
    @Test
    @DisplayName("Should set status to Finished and record end time")
    void testSetStatusFinished() throws ExceptionProcess {
        Process process = new Process(VALID_NAME, VALID_OWNER, DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        
        LocalDateTime before = LocalDateTime.now();
        process.setStatusFinished();
        LocalDateTime after = LocalDateTime.now();
        
        assertEquals(Process.STATUS_FINISHED, process.getStatus());
        assertTrue(process.isFinished());
        assertFalse(process.isActive());
        
        String endTimeStr = process.getTimeEndWork();
        assertNotEquals("process is not finished", endTimeStr);
        
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
        assertTrue(endTime.isAfter(before) || endTime.equals(before));
        assertTrue(endTime.isBefore(after) || endTime.equals(after));
    }
    
    @Test
    @DisplayName("Should increment log count correctly")
    void testIncrementLogCount() throws ExceptionProcess {
        Process process = new Process(VALID_NAME, VALID_OWNER, DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        
        assertEquals(0, process.getLogCount());
        
        process.incrementLogCount();
        assertEquals(1, process.getLogCount());
        
        process.incrementLogCount();
        process.incrementLogCount();
        assertEquals(3, process.getLogCount());
        
        for (int i = 0; i < 97; i++) {
            process.incrementLogCount();
        }
        assertEquals(100, process.getLogCount());
    }
    
    // ==================== Геттеры времени ====================
    
    @Test
    @DisplayName("Should return correct time creation")
    void testGetTimeCreate() throws ExceptionProcess {
        LocalDateTime before = LocalDateTime.now();
        Process process = new Process(VALID_NAME, VALID_OWNER, DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        LocalDateTime after = LocalDateTime.now();
        
        String timeCreateStr = process.getTimeCreate();
        LocalDateTime createTime = LocalDateTime.parse(timeCreateStr);
        
        assertTrue(createTime.isAfter(before) || createTime.equals(before));
        assertTrue(createTime.isBefore(after) || createTime.equals(after));
    }
    
    @Test
    @DisplayName("Should return 'process is not finished' for active process")
    void testGetTimeEndWorkForActiveProcess() throws ExceptionProcess {
        Process process = new Process(VALID_NAME, VALID_OWNER, DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        
        assertEquals("process is not finished", process.getTimeEndWork());
    }
    
    @Test
    @DisplayName("Should return correct time end work for finished process")
    void testGetTimeEndWorkForFinishedProcess() throws ExceptionProcess {
        Process process = new Process(VALID_NAME, VALID_OWNER, DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        
        LocalDateTime before = LocalDateTime.now();
        process.setStatusFinished();
        LocalDateTime after = LocalDateTime.now();
        
        String endTimeStr = process.getTimeEndWork();
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
        
        assertTrue(endTime.isAfter(before) || endTime.equals(before));
        assertTrue(endTime.isBefore(after) || endTime.equals(after));
    }
    
    // ==================== Форматирование времени выполнения ====================
    
    @Test
    @DisplayName("Should return execution time for active process")
    void testGetExecutionTimeForActiveProcess() throws ExceptionProcess, InterruptedException {
        Process process = new Process(VALID_NAME, VALID_OWNER, DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        
        Thread.sleep(2000);
        
        String executionTime = process.getTimeExecution();
        assertNotNull(executionTime);
        assertTrue(executionTime.contains("second") || executionTime.contains("seconds"));
    }
    
    @Test
    @DisplayName("Should return execution time for finished process")
    void testGetExecutionTimeForFinishedProcess() throws ExceptionProcess, InterruptedException {
        Process process = new Process(VALID_NAME, VALID_OWNER, DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        
        Thread.sleep(1000);
        process.setStatusFinished();
        
        String executionTime = process.getTimeExecution();
        assertNotNull(executionTime);
        assertTrue(executionTime.contains("second") || executionTime.contains("seconds"));
    }
    
    @Test
    @DisplayName("Should format seconds correctly")
    void testFormatSeconds() throws ExceptionProcess, InterruptedException {
        Process process = new Process(VALID_NAME, VALID_OWNER, DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        
        Thread.sleep(500);
        process.setStatusFinished();
        
        String executionTime = process.getTimeExecution();
        assertNotNull(executionTime);
    }
    
    @Test
    @DisplayName("Should handle zero execution time")
    void testZeroExecutionTime() throws ExceptionProcess {
        Process process = new Process(VALID_NAME, VALID_OWNER, DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        process.setStatusFinished();
        
        String executionTime = process.getTimeExecution();
        assertNotNull(executionTime);
    }
    
    // ==================== Пограничные случаи ====================
    
    @Test
    @DisplayName("Should handle zero max length for name")
    void testZeroMaxLengthName() {
        assertThrows(ExceptionProcess.class, () -> {
            new Process("any name", VALID_OWNER, 0, 50);
        });
    }
    
    @Test
    @DisplayName("Should handle zero max length for owner")
    void testZeroMaxLengthOwner() {
        assertThrows(ExceptionProcess.class, () -> {
            new Process(VALID_NAME, "any@owner.com", 100, 0);
        });
    }
    
    @Test
    @DisplayName("Should handle negative max length")
    void testNegativeMaxLength() {
        assertThrows(ExceptionProcess.class, () -> {
            new Process(VALID_NAME, VALID_OWNER, -10, 50);
        });
    }
    
    @Test
    @DisplayName("Should handle special characters in name and owner")
    void testSpecialCharacters() throws ExceptionProcess {
        String specialName = "service_123-test_v2.0";
        String specialOwner = "admin+test@company.co.uk";
        
        Process process = new Process(specialName, specialOwner, 200, 100);
        
        assertEquals(specialName, process.getName());
        assertEquals(specialOwner, process.getOwner());
    }
    
    @Test
    @DisplayName("Should handle unicode characters")
    void testUnicodeCharacters() throws ExceptionProcess {
        String unicodeName = "Сервис-логирования";
        String unicodeOwner = "админ@компания.рф";
        
        Process process = new Process(unicodeName, unicodeOwner, 200, 100);
        
        assertEquals(unicodeName, process.getName());
        assertEquals(unicodeOwner, process.getOwner());
    }
    
    @Test
    @DisplayName("Should handle maximum length strings")
    void testMaxLengthStrings() throws ExceptionProcess {
        String maxName = "A".repeat(100);
        String maxOwner = "B".repeat(50);
        
        Process process = new Process(maxName, maxOwner, 100, 50);
        
        assertEquals(maxName, process.getName());
        assertEquals(maxOwner, process.getOwner());
    }
    
    // ==================== Форматирование длительности (различные единицы) ====================
    
    @Test
    @DisplayName("Should format minutes correctly")
    void testFormatMinutes() throws ExceptionProcess {
        // Создаем процесс с прошедшим временем
        LocalDateTime oldTime = LocalDateTime.now().minusMinutes(5);
        
        Process process = new Process(
            "proc_id", VALID_NAME, VALID_OWNER, Process.STATUS_ACTIVE,
            "code", oldTime, null, 0
        );
        
        String executionTime = process.getTimeExecution();
        assertNotNull(executionTime);
        assertTrue(executionTime.contains("minute") || executionTime.contains("minutes"));
    }
    
    @Test
    @DisplayName("Should format hours correctly")
    void testFormatHours() throws ExceptionProcess {
        LocalDateTime oldTime = LocalDateTime.now().minusHours(3);
        
        Process process = new Process(
            "proc_id", VALID_NAME, VALID_OWNER, Process.STATUS_ACTIVE,
            "code", oldTime, null, 0
        );
        
        String executionTime = process.getTimeExecution();
        assertNotNull(executionTime);
        assertTrue(executionTime.contains("hour") || executionTime.contains("hours"));
    }
    
    @Test
    @DisplayName("Should format days correctly")
    void testFormatDays() throws ExceptionProcess {
        LocalDateTime oldTime = LocalDateTime.now().minusDays(5);
        
        Process process = new Process(
            "proc_id", VALID_NAME, VALID_OWNER, Process.STATUS_ACTIVE,
            "code", oldTime, null, 0
        );
        
        String executionTime = process.getTimeExecution();
        assertNotNull(executionTime);
        assertTrue(executionTime.contains("day") || executionTime.contains("days"));
    }
    
    // ==================== Вспомогательные тесты ====================
    
    @Test
    @DisplayName("Should increment log count after finishing")
    void testIncrementAfterFinish() throws ExceptionProcess {
        Process process = new Process(VALID_NAME, VALID_OWNER, DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        process.setStatusFinished();
        
        // Можно ли увеличивать счетчик после завершения? (по логике - да, счетчик логов не зависит от статуса)
        process.incrementLogCount();
        assertEquals(1, process.getLogCount());
    }
    
    @Test
    @DisplayName("Should handle multiple increment operations")
    void testMultipleIncrements() throws ExceptionProcess {
        Process process = new Process(VALID_NAME, VALID_OWNER, DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        
        for (int i = 1; i <= 1000; i++) {
            process.incrementLogCount();
            assertEquals(i, process.getLogCount());
        }
    }
    
    @Test
    @DisplayName("Should preserve name and owner after status change")
    void testNameAndOwnerImmutability() throws ExceptionProcess {
        Process process = new Process(VALID_NAME, VALID_OWNER, DEFAULT_MAX_NAME, DEFAULT_MAX_OWNER);
        
        String originalName = process.getName();
        String originalOwner = process.getOwner();
        
        process.setStatusFinished();
        process.incrementLogCount();
        
        assertEquals(originalName, process.getName());
        assertEquals(originalOwner, process.getOwner());
    }
}