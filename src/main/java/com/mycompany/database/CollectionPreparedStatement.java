package com.mycompany.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;
import com.mycompany.models.LogStatus;
import java.util.List;
import java.util.ArrayList;

/**
 * Класс для управления подготовленными SQL запросами к базе данных.
 * 
 * <p>Предоставляет централизованный доступ к предварительно скомпилированным
 * запросам (PreparedStatement) для всех операций с базой данных.
 * Это улучшает производительность и защищает от SQL-инъекций.</p>
 * 
 * <h2>Особенности:</h2>
 * <ul>
 *   <li>Все PreparedStatement создаются при инициализации класса</li>
 *   <li>Обеспечивает единое место для управления запросами</li>
 *   <li>Поддерживает как статические, так и динамические запросы</li>
 *   <li>Автоматическое закрытие всех ресурсов</li>
 * </ul>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try (Connection conn = DriverManager.getConnection("jdbc:sqlite:logs.db");
 *      CollectionStatement stmts = new CollectionStatement(conn)) {
 *     
 *     // Использование PreparedStatement для вставки процесса
 *     PreparedStatement insertProcess = stmts.getInsertProcess();
 *     insertProcess.setString(1, "proc_123");
 *     insertProcess.setString(2, "billing-service");
 *     insertProcess.executeUpdate();
 *     
 *     // Динамический запрос для фильтрации по статусам
 *     List&lt;LogStatus&gt; statuses = Arrays.asList(LogStatus.FATAL, LogStatus.ERROR);
 *     PreparedStatement selectByStatus = stmts.getSelectLogByStatuses(statuses);
 *     // ... выполнение запроса
 *     
 * } catch (SQLException e) {
 *     System.err.println("Database error: " + e.getMessage());
 * }
 * </pre>
 * 
 * @author admin_
 * @version 1.0
 * @see CollectionSQLliteRequest
 * @see QueryBuilder
 */
public class CollectionPreparedStatement implements AutoCloseable {
    
    // ==================== Prepared Statements ====================
    
    /** PreparedStatement для вставки нового процесса */
    private final PreparedStatement insertProcessStmt;
    
    /** PreparedStatement для выборки логов по индексам */
    private final PreparedStatement selectLogsByRange;

    /** PreparedStatement для получения всех процессов */
    private final PreparedStatement selectAllProcessesStmt;
    
    /** PreparedStatement для поиска процесса по ID */
    private final PreparedStatement selectProcessByIdStmt;
    
    /** PreparedStatement для получения активных процессов */
    private final PreparedStatement selectActiveProcessesStmt;
    
    /** PreparedStatement для обновления информации о процессе */
    private final PreparedStatement updateProcessStmt;

    /** PreparedStatement для вставки нового лога */
    private final PreparedStatement insertLogStmt;
    
    /** PreparedStatement для получения всех логов процесса */
    private final PreparedStatement selectAllLogsStmt;
    
    /** PreparedStatement для поиска логов по подстроке */
    private final PreparedStatement selectLogBySubstringStmt;

    /** PreparedStatement для обновления статуса */
    private final PreparedStatement updateProcessStatus;

    /** PreparedStatemnt для обновления количества логов */
    private final PreparedStatement updateProcessLogCount;

    /** PreparedStatent для обновления временени завершения */
    private final PreparedStatement updateProcessLogFinished;

    /** PreparedStatent для получения количества логов процесса */
    private final PreparedStatement selectCountLogsInProcess;
    
    /** Список всех ресурсов для закрытия */
    private final List<AutoCloseable> resources = new ArrayList<>();
    
    /** Подключение к базе данных */
    private final Connection connection;

    /**
     * Создает новый экземпляр CollectionStatement.
     * Инициализирует все подготовленные запросы и statements.
     * 
     * @param connection активное подключение к базе данных
     * @throws RuntimeException если не удалось инициализировать запросы
     */
    public CollectionPreparedStatement(Connection connection) throws RuntimeException{
        this.connection = connection;
        
        try {
            insertProcessStmt = createAndRegisterPreparedStatement(
                CollectionSQLliteRequest.INSERT_NEW_PROCESS
            );
            selectLogsByRange = createAndRegisterPreparedStatement(
                CollectionSQLliteRequest.SELECT_LOGS_BY_RANGE_NUMBER
            );
            selectAllProcessesStmt = createAndRegisterPreparedStatement(
                CollectionSQLliteRequest.SELECT_ALL_PROCESSES
            );
            selectProcessByIdStmt = createAndRegisterPreparedStatement(
                CollectionSQLliteRequest.SELECT_PROCESS_BY_ID
            );
            selectActiveProcessesStmt = createAndRegisterPreparedStatement(
                CollectionSQLliteRequest.SELECT_ALL_PROCESSES_BY_STATUS_ACTIVE
            );
            selectCountLogsInProcess = createAndRegisterPreparedStatement(
                CollectionSQLliteRequest.SELECT_COUNT_LOG_IN_PROCESS
            );
            updateProcessStmt = createAndRegisterPreparedStatement(
                CollectionSQLliteRequest.UPDATE_PROCESS_INFO
            );

            insertLogStmt = createAndRegisterPreparedStatement(
                CollectionSQLliteRequest.INSERT_NEW_LOG
            );
            selectAllLogsStmt = createAndRegisterPreparedStatement(
                CollectionSQLliteRequest.SELECT_ALL_LOGS_BY_PROCESS_ID
            );
            selectLogBySubstringStmt = createAndRegisterPreparedStatement(
                CollectionSQLliteRequest.SELECT_LOGS_CONTAINS_SUBSTRING
            );
            updateProcessStatus = createAndRegisterPreparedStatement(
                CollectionSQLliteRequest.UPDATE_PROCESS_STATUS
            );
            updateProcessLogCount = createAndRegisterPreparedStatement(
                CollectionSQLliteRequest.UPDATE_PROCESS_LOG_COUNT
            );
            updateProcessLogFinished = createAndRegisterPreparedStatement(
                CollectionSQLliteRequest.UPDATE_PROCESS_LOG_FINISHED_BY
            );
            
        } catch (SQLException exc) {
            try {
                closeAll();
            } catch (SQLException closeExc) {
            }
            throw new RuntimeException("Failed to initialize database statements", exc);
        }
    }
    
    /**
     * Создает PreparedStatement и добавляет его в список ресурсов.
     * 
     * @param sql SQL запрос
     * @return новый PreparedStatement
     * @throws SQLException если ошибка при создании
     */
    private PreparedStatement createAndRegisterPreparedStatement(String sql) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        resources.add(stmt);
        return stmt;
    }

    // ==================== Геттеры ====================
    
    /**
     * Возвращает PreparedStatement для вставки нового процесса.
     * 
     * @return PreparedStatement для INSERT в таблицу processes
     */
    public PreparedStatement getInsertProcess() {
        return insertProcessStmt;
    }
    
    /**
     * Возвращает PreparedStatement для получения всех процессов.
     * 
     * @return PreparedStatement для SELECT * FROM processes
     */
    public PreparedStatement getSelectAllProcesses() {
        return selectAllProcessesStmt;
    }
    
    public PreparedStatement getSelectLogsByRange() {
        return selectLogsByRange;
    }
    /**
     * Возвращает PreparedStatement для получения количества логов у процесса.
     * 
     * @return PreparedStatement для SELECT * FROM processes
     */
    public PreparedStatement getSelectCountLogProcess() {
        return selectCountLogsInProcess;
    }

    /**
     * Возвращает PreparedStatement для поиска процесса по ID.
     * 
     * @return PreparedStatement для SELECT * FROM processes WHERE id = ?
     */
    public PreparedStatement getSelectProcessById() {
        return selectProcessByIdStmt;
    }
    
    /**
     * Возвращает PreparedStatement для получения активных процессов.
     * 
     * @return PreparedStatement для SELECT * FROM processes WHERE status = 'Active'
     */
    public PreparedStatement getSelectActiveProcesses() {
        return selectActiveProcessesStmt;
    }
    
    /**
     * Возвращает PreparedStatement для обновления процесса.
     * 
     * @return PreparedStatement для UPDATE processes SET ...
     */
    public PreparedStatement getUpdateProcess() {
        return updateProcessStmt;
    }
    
    /**
     * Возвращает PreparedStatement для обновления процесса.
     * 
     * @return PreparedStatement для UPDATE processes SET status = ?
     */
    public PreparedStatement getUpdateProcessStatus(){
        return updateProcessStatus;
    }
    
    /**
     * Возвращает PreparedStatement для обновления процесса.
     * 
     * @return PreparedStatement для UPDATE processes SET status = ?
     */
    public PreparedStatement getUpdateProcessLogsCount(){
        return updateProcessLogCount;
    }

    public PreparedStatement getUpdateProcessFinishedBy(){
        return updateProcessLogFinished;
    }

    /**
     * Возвращает PreparedStatement для вставки нового лога.
     * 
     * @return PreparedStatement для INSERT INTO logs
     */
    public PreparedStatement getInsertLog() {
        return insertLogStmt;
    }
    
    /**
     * Возвращает PreparedStatement для получения всех логов процесса.
     * 
     * @return PreparedStatement для SELECT * FROM logs WHERE id_parent_process = ?
     */
    public PreparedStatement getSelectAllLogs() {
        return selectAllLogsStmt;
    }
    
    /**
     * Возвращает PreparedStatement для поиска логов по подстроке.
     * 
     * @return PreparedStatement для SELECT * FROM logs WHERE log_msg LIKE ?
     */
    public PreparedStatement getSelectLogBySubstring() {
        return selectLogBySubstringStmt;
    }
    
    /**
     * Создает PreparedStatement для выборки логов по статусам.
     * 
     * <p>Запрос строится динамически в зависимости от количества статусов.
     * Если список статусов пуст или null, возвращается запрос для получения всех логов.</p>
     * 
     * <p>Примеры:</p>
     * <ul>
     *   <li>1 статус: WHERE status IN (?)</li>
     *   <li>3 статуса: WHERE status IN (?, ?, ?)</li>
     *   <li>null или пустой: без фильтрации по статусам</li>
     * </ul>
     * 
     * @param statuses список статусов для фильтрации
     * @return PreparedStatement с готовым динамическим запросом
     * @throws SQLException если ошибка при создании PreparedStatement
     */
    public PreparedStatement getSelectLogByStatuses(List<LogStatus> statuses) throws SQLException {
        String sql = QueryBuilder.buildSelectLogsByStatuses(statuses);
        PreparedStatement stmt = connection.prepareStatement(sql);
        resources.add(stmt);
        return stmt;
    }

    // ==================== Закрытие ресурсов ====================
    
    /**
     * Закрывает все подготовленные запросы и statements.
     * 
     * <p>Реализует интерфейс {@link AutoCloseable}, что позволяет
     * использовать класс в try-with-resources.</p>
     * 
     * @throws SQLException если ошибка при закрытии ресурсов
     */
    @Override
    public void close() throws SQLException {
        closeAll();
    }
    
    /**
     * Закрывает все подготовленные запросы и statements.
     * 
     * <p>Использует список ресурсов для надежного закрытия всех объектов.
     * При возникновении ошибки продолжает закрывать остальные ресурсы
     * и собирает все исключения в одно.</p>
     * 
     * @throws SQLException если ошибка при закрытии ресурсов
     */
    public void closeAll() throws SQLException {
        List<SQLException> exceptions = new ArrayList<>();
        
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (SQLException e) {
                    exceptions.add(e);
                } catch (Exception e) {
                    exceptions.add(new SQLException("Error closing resource: " + e.getMessage(), e));
                }
            }
        }
        resources.clear();
        
        if (!exceptions.isEmpty()) {
            SQLException first = exceptions.get(0);
            for (int i = 1; i < exceptions.size(); i++) {
                first.addSuppressed(exceptions.get(i));
            }
            throw first;
        }
    }
}