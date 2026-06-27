package com.mycompany.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mycompany.models.ExceptionLogStatus;
import com.mycompany.models.LogStatus;

/**
 * Тестовый класс для CollectionStatement.
 * 
 * @author admin_
 * @version 1.0
 */
class CollectionPreparedStatementTest {
    
    private Connection connection;
    private CollectionPreparedStatement statements;
    
    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        
        // Создаем таблицы ДО инициализации CollectionStatement
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CollectionSQLliteRequest.CREATE_TABLE_PROCESSES);
            stmt.execute(CollectionSQLliteRequest.CREATE_TABLE_LOGS);
            
            stmt.execute(CollectionSQLliteRequest.CREATE_INDEX_LOGS_NUMBER);
            stmt.execute(CollectionSQLliteRequest.CREATE_INDEX_LOGS_PROCESS_ID);
            stmt.execute(CollectionSQLliteRequest.CREATE_INDEX_LOGS_STATUS);
            stmt.execute(CollectionSQLliteRequest.CREATE_INDEX_PROCESSES_NAME);
            stmt.execute(CollectionSQLliteRequest.CREATE_INDEX_PROCESSES_STATUS);
        }
        
        statements = new CollectionPreparedStatement(connection);
    }
    
    @AfterEach
    void tearDown() throws SQLException {
        if (statements != null) {
            statements.close();
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    // ==================== Вспомогательные методы ====================
    
    private List<LogStatus> createStatusList(String... statusNames) throws ExceptionLogStatus {
        List<LogStatus> statuses = new ArrayList<>();
        for (String name : statusNames) {
            statuses.add(new LogStatus(name));
        }
        return statuses;
    }
    
    // ==================== Тесты инициализации ====================
    
    @Test
    @DisplayName("Should initialize all PreparedStatement correctly")
    void testPreparedStatementsInitialization() {
        assertNotNull(statements.getInsertProcess());
        assertNotNull(statements.getSelectAllProcesses());
        assertNotNull(statements.getSelectProcessById());
        assertNotNull(statements.getSelectActiveProcesses());
        assertNotNull(statements.getUpdateProcess());
        assertNotNull(statements.getInsertLog());
        assertNotNull(statements.getSelectAllLogs());
        assertNotNull(statements.getSelectLogBySubstring());
    }
    
    // ==================== Тесты динамических запросов ====================
    
    @Test
    @DisplayName("Should create dynamic PreparedStatement for statuses")
    void testGetSelectLogByStatuses() throws ExceptionLogStatus, SQLException {
        List<LogStatus> statuses = createStatusList("FATAL", "WARN");
        
        PreparedStatement stmt = statements.getSelectLogByStatuses(statuses);
        
        assertNotNull(stmt);
        String sql = stmt.toString();
        assertTrue(sql.contains("status IN (?, ?)"));
    }
    
    @Test
    @DisplayName("Should create PreparedStatement with single status")
    void testGetSelectLogByStatusesSingle() throws ExceptionLogStatus, SQLException {
        List<LogStatus> statuses = createStatusList("FATAL");
        
        PreparedStatement stmt = statements.getSelectLogByStatuses(statuses);
        
        assertNotNull(stmt);
        String sql = stmt.toString();
        assertTrue(sql.contains("status IN (?)"));
    }
    
    @Test
    @DisplayName("Should create PreparedStatement for null statuses")
    void testGetSelectLogByStatusesNull() throws SQLException {
        PreparedStatement stmt = statements.getSelectLogByStatuses(null);
        
        assertNotNull(stmt);
        String sql = stmt.toString();
        assertFalse(sql.contains("status IN"));
    }
    
    @Test
    @DisplayName("Should create PreparedStatement for empty statuses")
    void testGetSelectLogByStatusesEmpty() throws SQLException {
        List<LogStatus> statuses = new ArrayList<>();
        
        PreparedStatement stmt = statements.getSelectLogByStatuses(statuses);
        
        assertNotNull(stmt);
        String sql = stmt.toString();
        assertFalse(sql.contains("status IN"));
    }
    
    @Test
    @DisplayName("Should create PreparedStatement for all statuses")
    void testGetSelectLogByStatusesAll() throws ExceptionLogStatus, SQLException {
        List<LogStatus> statuses = createStatusList("FATAL", "INFO", "WARN", "DEBUG", "TRACE");
        
        PreparedStatement stmt = statements.getSelectLogByStatuses(statuses);
        
        assertNotNull(stmt);
        String sql = stmt.toString();
        assertTrue(sql.contains("status IN (?, ?, ?, ?, ?)"));
    }
    
    // ==================== Тесты операций с БД ====================
    
    @Test
    @DisplayName("Should insert and select process")
    void testInsertAndSelectProcess() throws SQLException {
        PreparedStatement insertStmt = statements.getInsertProcess();
        insertStmt.setString(1, "proc_123");
        insertStmt.setString(2, "test-service");
        insertStmt.setString(3, "Active");
        insertStmt.setString(4, "test@company.com");
        insertStmt.setString(5, "code_123");
        insertStmt.setLong(6, 0);
        insertStmt.setString(7, "2024-01-01 10:00:00");
        insertStmt.setString(8, null);
        
        int affected = insertStmt.executeUpdate();
        assertEquals(1, affected);
        
        PreparedStatement selectStmt = statements.getSelectProcessById();
        selectStmt.setString(1, "proc_123");
        ResultSet rs = selectStmt.executeQuery();
        
        assertTrue(rs.next());
        assertEquals("proc_123", rs.getString("id"));
        assertEquals("test-service", rs.getString("name"));
        assertEquals("Active", rs.getString("status"));
        assertEquals(0, rs.getLong("logCount"));
    }
    
    @Test
    @DisplayName("Should insert and select log")
    void testInsertAndSelectLog() throws SQLException {
        PreparedStatement insertProcess = statements.getInsertProcess();
        insertProcess.setString(1, "proc_123");
        insertProcess.setString(2, "test-service");
        insertProcess.setString(3, "Active");
        insertProcess.setString(4, "test@company.com");
        insertProcess.setString(5, "code_123");
        insertProcess.setLong(6, 0);
        insertProcess.setString(7, "2024-01-01 10:00:00");
        insertProcess.setString(8, null);
        insertProcess.executeUpdate();
        
        PreparedStatement insertLog = statements.getInsertLog();
        insertLog.setString(1, "proc_123");
        insertLog.setString(2, "INFO");
        insertLog.setString(3, "Test log message");
        insertLog.setLong(4, 1);
        insertLog.setString(5, "2024-01-01 10:01:00");
        
        int affected = insertLog.executeUpdate();
        assertEquals(1, affected);
        
        PreparedStatement selectLogs = statements.getSelectAllLogs();
        selectLogs.setString(1, "proc_123");
        ResultSet rs = selectLogs.executeQuery();
        
        assertTrue(rs.next());
        assertEquals("proc_123", rs.getString("id_parent_process"));
        assertEquals("INFO", rs.getString("status"));
        assertEquals("Test log message", rs.getString("log_msg"));
    }
    
    @Test
    @DisplayName("Should update process")
    void testUpdateProcess() throws SQLException {
        PreparedStatement insertStmt = statements.getInsertProcess();
        insertStmt.setString(1, "proc_123");
        insertStmt.setString(2, "test-service");
        insertStmt.setString(3, "Active");
        insertStmt.setString(4, "test@company.com");
        insertStmt.setString(5, "code_123");
        insertStmt.setLong(6, 5);
        insertStmt.setString(7, "2024-01-01 10:00:00");
        insertStmt.setString(8, null);
        insertStmt.executeUpdate();
        
        PreparedStatement updateStmt = statements.getUpdateProcess();
        updateStmt.setString(1, "Finished");
        updateStmt.setLong(2, 10);
        updateStmt.setString(3, "2024-01-01 12:00:00");
        updateStmt.setString(4, "proc_123");
        
        int affected = updateStmt.executeUpdate();
        assertEquals(1, affected);
        
        PreparedStatement selectStmt = statements.getSelectProcessById();
        selectStmt.setString(1, "proc_123");
        ResultSet rs = selectStmt.executeQuery();
        
        assertTrue(rs.next());
        assertEquals("Finished", rs.getString("status"));
        assertEquals(10, rs.getLong("logCount"));
    }
    
    @Test
    @DisplayName("Should select all processes")
    void testSelectAllProcesses() throws SQLException {
        PreparedStatement insertStmt = statements.getInsertProcess();
        
        insertStmt.setString(1, "proc_1");
        insertStmt.setString(2, "service-1");
        insertStmt.setString(3, "Active");
        insertStmt.setString(4, "owner1");
        insertStmt.setString(5, "code_1");
        insertStmt.setLong(6, 0);
        insertStmt.setString(7, "2024-01-01 10:00:00");
        insertStmt.setString(8, null);
        insertStmt.executeUpdate();
        
        insertStmt.setString(1, "proc_2");
        insertStmt.setString(2, "service-2");
        insertStmt.setString(3, "Active");
        insertStmt.setString(4, "owner2");
        insertStmt.setString(5, "code_2");
        insertStmt.setLong(6, 0);
        insertStmt.setString(7, "2024-01-01 11:00:00");
        insertStmt.setString(8, null);
        insertStmt.executeUpdate();
        
        PreparedStatement selectStmt = statements.getSelectAllProcesses();
        ResultSet rs = selectStmt.executeQuery();
        
        int count = 0;
        while (rs.next()) {
            count++;
        }
        assertEquals(2, count);
    }
    
    @Test
    @DisplayName("Should select active processes only")
    void testSelectActiveProcesses() throws SQLException {
        PreparedStatement insertStmt = statements.getInsertProcess();
        
        insertStmt.setString(1, "proc_1");
        insertStmt.setString(2, "active-service");
        insertStmt.setString(3, "Active");
        insertStmt.setString(4, "owner1");
        insertStmt.setString(5, "code_1");
        insertStmt.setLong(6, 0);
        insertStmt.setString(7, "2024-01-01 10:00:00");
        insertStmt.setString(8, null);
        insertStmt.executeUpdate();
        
        insertStmt.setString(1, "proc_2");
        insertStmt.setString(2, "finished-service");
        insertStmt.setString(3, "Finished");
        insertStmt.setString(4, "owner2");
        insertStmt.setString(5, "code_2");
        insertStmt.setLong(6, 0);
        insertStmt.setString(7, "2024-01-01 11:00:00");
        insertStmt.setString(8, "2024-01-01 12:00:00");
        insertStmt.executeUpdate();
        
        PreparedStatement selectStmt = statements.getSelectActiveProcesses();
        ResultSet rs = selectStmt.executeQuery();
        
        int count = 0;
        while (rs.next()) {
            count++;
            assertEquals("Active", rs.getString("status"));
        }
        assertEquals(1, count);
    }
    
    // ==================== Тесты управления ресурсами ====================
    
    @Test
    @DisplayName("Should close all resources")
    void testCloseAll() throws SQLException {
        PreparedStatement stmt1 = statements.getSelectProcessById();
        PreparedStatement stmt2 = statements.getSelectAllLogs();
        
        assertFalse(stmt1.isClosed());
        assertFalse(stmt2.isClosed());
        
        statements.closeAll();
        
        assertTrue(stmt1.isClosed());
        assertTrue(stmt2.isClosed());
    }
    
    @Test
    @DisplayName("Should support try-with-resources")
    void testTryWithResources() throws SQLException {
        Connection testConnection = DriverManager.getConnection("jdbc:sqlite::memory:");
        
        // Создаем таблицы
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute(CollectionSQLliteRequest.CREATE_TABLE_PROCESSES);
            stmt.execute(CollectionSQLliteRequest.CREATE_TABLE_LOGS);
            stmt.execute(CollectionSQLliteRequest.CREATE_INDEX_LOGS_NUMBER);
            stmt.execute(CollectionSQLliteRequest.CREATE_INDEX_LOGS_PROCESS_ID);
            stmt.execute(CollectionSQLliteRequest.CREATE_INDEX_LOGS_STATUS);
            stmt.execute(CollectionSQLliteRequest.CREATE_INDEX_PROCESSES_NAME);
            stmt.execute(CollectionSQLliteRequest.CREATE_INDEX_PROCESSES_STATUS);
        }
        
        try (CollectionPreparedStatement stmts = new CollectionPreparedStatement(testConnection)) {
            assertNotNull(stmts.getInsertProcess());
            assertNotNull(stmts.getSelectAllProcesses());
        }
        // CollectionStatement закрыт, connection не закрыт
        assertFalse(testConnection.isClosed());
        testConnection.close();
    }
    
    @Test
    @DisplayName("Should handle connection error gracefully")
    void testConnectionError() {
        try {
            connection.close();
        } catch (SQLException e) {
            // Игнорируем
        }
        assertThrows(RuntimeException.class, () -> {
            new CollectionPreparedStatement(connection);
        });
    }
}