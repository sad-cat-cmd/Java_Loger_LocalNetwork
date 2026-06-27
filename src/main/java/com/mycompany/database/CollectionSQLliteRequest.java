package com.mycompany.database;

/**
 * Класс-контейнер для SQL запросов к базе данных SQLite.
 * 
 * <p>Содержит все SQL выражения, используемые в репозиториях.
 * Централизованное хранение запросов упрощает их поддержку и изменение.</p>
 * 
 * @author admin_
 * @version 1.0
 */
public class CollectionSQLliteRequest {
    
    // ==================== Создание таблиц ====================
    
    /**
     * SQL запрос для создания таблицы процессов.
     */
    public static final String CREATE_TABLE_PROCESSES = 
        "CREATE TABLE IF NOT EXISTS processes(" +
        "    id TEXT PRIMARY KEY," +
        "    name TEXT NOT NULL," +
        "    status TEXT NOT NULL DEFAULT 'Active'," +
        "    owner TEXT NOT NULL," +
        "    unique_code TEXT NOT NULL," +
        "    log_count INTEGER DEFAULT 0," + 
        "    created_by DATETIME NOT NULL," +
        "    finished_by DATETIME," +
        "    CHECK (status IN ('Active', 'Finished'))" +
        ");";
    
    /**
     * SQL запрос для создания таблицы логов.
     */
    public static final String CREATE_TABLE_LOGS = 
        "CREATE TABLE IF NOT EXISTS logs(" +
        "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "    id_parent_process TEXT NOT NULL," +
        "    status TEXT NOT NULL," +
        "    log_msg TEXT NOT NULL," +
        "    number_log INTEGER DEFAULT 0," +
        "    created_by DATETIME NOT NULL," +
        "    FOREIGN KEY (id_parent_process) REFERENCES processes(id) ON DELETE CASCADE" +
        ");";
    
    // ==================== Индексы ====================
    
    /**
     * SQL запрос для создания индекса по ID процесса в таблице логов.
     */
    public static final String CREATE_INDEX_LOGS_PROCESS_ID = 
        "CREATE INDEX IF NOT EXISTS idx_logs_process_id ON logs(id_parent_process);";
    
    /**
     * SQL запрос для создания индекса по статусу процесса.
     */
    public static final String CREATE_INDEX_PROCESSES_STATUS = 
        "CREATE INDEX IF NOT EXISTS idx_processes_status ON processes(status);";
    
    /**
     * SQL запрос для создания индекса по статусу в таблице логов.
     */
    public static final String CREATE_INDEX_LOGS_STATUS = 
        "CREATE INDEX IF NOT EXISTS idx_logs_status ON logs(status);";
    
    /**
     * SQL запрос для создания индекса по имени процесса.
     */
    public static final String CREATE_INDEX_PROCESSES_NAME = 
        "CREATE INDEX IF NOT EXISTS idx_processes_name ON processes(name);";
    
    /**
     * SQL запрос для создания индекса по номеру лога.
     */
    public static final String CREATE_INDEX_LOGS_NUMBER = 
        "CREATE INDEX IF NOT EXISTS idx_logs_number ON logs(number_log);";
    
    // ==================== Запросы для процессов (INSERT) ====================
    
    /**
     * SQL запрос для вставки нового процесса.
     */
    public static final String INSERT_NEW_PROCESS = 
        "INSERT INTO processes(id, name, status, owner, unique_code, log_count, created_by, finished_by) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
    
    // ==================== Запросы для процессов (UPDATE) ====================
    
    /**
     * SQL запрос для обновления информации о процессе.
     */
    public static final String UPDATE_PROCESS_INFO = 
        "UPDATE processes " +
        "SET status = ?, log_count = ?, finished_by = ? " +
        "WHERE id = ?;";
    
    /**
     * SQL запрос для обновления статуса процесса.
     */
    public static final String UPDATE_PROCESS_STATUS =
        "UPDATE processes " +
        "SET status = ? " + 
        "WHERE id = ?;";
    
    /**
     * SQL запрос для обновления количества логов процесса.
     */
    public static final String UPDATE_PROCESS_LOG_COUNT =
        "UPDATE processes " +                           
        "SET log_count = ? " +                           
        "WHERE id = ?;";
    
    /**
     * SQL запрос для обновления даты завершения процесса
     */    
    public static final String UPDATE_PROCESS_LOG_FINISHED_BY = 
        "UPDATE processes " + 
        "SET finished_by = ? " +
        "WHERE id = ?;";

    /**
     * SQL запрос для получения всех процессов.
     */
    public static final String SELECT_ALL_PROCESSES = 
        "SELECT * FROM processes ORDER BY created_by DESC;";
    
    /**
     * SQL запрос для получения всех активных процессов.
     */
    public static final String SELECT_ALL_PROCESSES_BY_STATUS_ACTIVE = 
        "SELECT * FROM processes WHERE status = 'Active' ORDER BY created_by DESC;";
    
    /**
     * SQL запрос для поиска процесса по ID.
     */
    public static final String SELECT_PROCESS_BY_ID = 
        "SELECT * FROM processes WHERE id = ?;";
    
    /**
     * SQL запрос для поиска процесса по имени.
     */
    public static final String SELECT_PROCESS_BY_NAME = 
        "SELECT * FROM processes WHERE name = ?;";
    
    /**
     * SQL запрос для проверки существования процесса по имени.
     */
    public static final String EXISTS_PROCESS_BY_NAME = 
        "SELECT 1 FROM processes WHERE name = ?;";
    
    /**
     * SQL запрос для удаления процесса по ID.
     */
    public static final String DELETE_PROCESS_BY_ID = 
        "DELETE FROM processes WHERE id = ?;";
    
    // ==================== Запросы для логов ====================
    
    /**
     * SQL запрос для вставки нового лога.
     */
    public static final String INSERT_NEW_LOG = 
        "INSERT INTO logs(id_parent_process, status, log_msg, number_log, created_by) " +
        "VALUES (?, ?, ?, ?, ?);";
    
    /**
     * SQL запрос для получения всех логов процесса по ID.
     */
    public static final String SELECT_ALL_LOGS_BY_PROCESS_ID = 
        "SELECT * FROM logs WHERE id_parent_process = ? ORDER BY number_log DESC;";
    
    /**
     * SQL запрос для получения логов процесса в диапазоне по номеру.
     */
    public static final String SELECT_LOGS_BY_RANGE_NUMBER = 
        "SELECT * FROM logs WHERE id_parent_process = ? AND number_log >= ? AND number_log <= ? " +
        "ORDER BY number_log DESC;";
    
    /**
     * SQL запрос для получения логов процесса по статусам.
     * Использовать с QueryBuilder для подстановки плейсхолдеров.
     */
    public static final String SELECT_LOGS_CONTAINS_STATUSES = 
        "SELECT * FROM logs WHERE id_parent_process = ? AND status IN (%s) ORDER BY number_log DESC;";
    
    /**
     * SQL запрос для поиска логов по подстроке.
     */
    public static final String SELECT_LOGS_CONTAINS_SUBSTRING = 
        "SELECT * FROM logs WHERE id_parent_process = ? AND log_msg LIKE ? ORDER BY number_log DESC;";
    
    /**
     * SQL запрос для подсчета количества логов по ID процесса
     */
    public static final String SELECT_COUNT_LOG_IN_PROCESS =
        "SELECT COUNT(*) FROM logs " +
        "WHERE id_parent_process = ?;"; 
}