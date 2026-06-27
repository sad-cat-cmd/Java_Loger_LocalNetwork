package com.mycompany.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mycompany.models.DataLog;
import com.mycompany.models.LogStatus;
import com.mycompany.models.Process;

/**
 * Менеджер базы данных для управления подключением и операциями с SQLite.
 * 
 * <p>Предоставляет низкоуровневые атомарные операции с базой данных:
 * <ul>
 *   <li>Инициализация подключения к БД</li>
 *   <li>Создание таблиц и индексов</li>
 *   <li>CRUD операции для процессов и логов</li>
 *   <li>Управление ресурсами (Connection, Statement)</li>
 * </ul>
 * </p>
 * 
 * <h2>Архитектурные принципы:</h2>
 * <ul>
 *   <li>Один метод = одна SQL операция (атомарность)</li>
 *   <li>Все методы выбрасывают {@link ExceptionDB}</li>
 *   <li>Использует {@link CollectionPreparedStatement} для PreparedStatement</li>
 *   <li>try-with-resources для автоматического закрытия ресурсов</li>
 * </ul>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try {
 *     DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite:logs.db");
 *     
 *     // Сохранение процесса
 *     dbManager.insertProcess(process);
 *     
 *     // Получение всех процессов
 *     List&lt;Process&gt; processes = dbManager.selectAllProcesses();
 *     
 *     // Получение логов процесса
 *     List&lt;DataLog&gt; logs = dbManager.selectAllLogs("proc_123");
 *     
 * } catch (ExceptionDB e) {
 *     System.err.println(e.getCombinedLogMsg());
 * }
 * </pre>
 * 
 * @author admin_
 * @version 1.0
 * @see CollectionPreparedStatement
 * @see ExceptionDB
 * @see CollectionSQLliteRequest
 */
public class DatabaseManager {
    /** Подключение к базе данных */
    private Connection connection;
    
    /** Коллекция подготовленных запросов */
    private CollectionPreparedStatement collectionStatement;
    
    /** URL для подключения к базе данных */
    private final String URLDataBase;
    
     /**
     * Создает таблицы и индексы.
     * Выполняется ДО создания CollectionStatement.
     */
    private void initTableAndIndex(Connection connection) throws ExceptionDB{
        try {
            Statement statement = connection.createStatement();
            statement.execute(CollectionSQLliteRequest.CREATE_TABLE_PROCESSES);
            statement.execute(CollectionSQLliteRequest.CREATE_TABLE_LOGS);
            
            statement.execute(CollectionSQLliteRequest.CREATE_INDEX_LOGS_NUMBER);
            statement.execute(CollectionSQLliteRequest.CREATE_INDEX_LOGS_STATUS);
            statement.execute(CollectionSQLliteRequest.CREATE_INDEX_PROCESSES_NAME);
            statement.execute(CollectionSQLliteRequest.CREATE_INDEX_PROCESSES_STATUS);
            statement.execute(CollectionSQLliteRequest.CREATE_INDEX_LOGS_PROCESS_ID);

        } catch (SQLException excSQL) {
            throw new ExceptionDB("SQLlite error:" + excSQL.getMessage() + "\nSQLite state:" + excSQL.getSQLState(),
                                  "DatabaseManager.initTableAndIndex()",
                                  "",
                                  0);
        }

    } 

    /**
     * Создает новый экземпляр DatabaseManager.
     * Инициализирует подключение к БД, создает таблицы и индексы.
     * 
     * @param URLDataBase URL подключения к БД (например, "jdbc:sqlite:logs.db")
     * @throws ExceptionDB если ошибка подключения или инициализации
     */
    public DatabaseManager(String URLDataBase) throws ExceptionDB {
        this.URLDataBase = URLDataBase;
        try {
            connection = DriverManager.getConnection(this.URLDataBase);

            initTableAndIndex(connection);

            collectionStatement = new CollectionPreparedStatement(connection);
            
        } catch (SQLException excSQL) {
            throw new ExceptionDB(
                "SQLlite error: " + excSQL.getMessage() + "\nSQLite state: " + excSQL.getSQLState(),
                "DatabaseManager.DatabaseManager()",
                "",
                0
            );
        } catch (RuntimeException excRE) {
            throw new ExceptionDB(
                "Runtime Error: " + excRE.getMessage(),
                "DatabaseManager.DatabaseManager()",
                "",
                0
            );
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("DatabaseManager.DatabaseManager()");
            throw excDB;
        }
    }
    
    // ==================== ПРОЦЕССЫ (CRUD) ====================
    
    /**
     * Вставляет новый процесс в базу данных.
     * 
     * @param process объект процесса для вставки
     * @throws ExceptionDB если ошибка при вставке
     */
    public void insertProcess(Process process) throws ExceptionDB {
        PreparedStatement pstateInsertProcess = collectionStatement.getInsertProcess();
        try {
            pstateInsertProcess.setString(1, process.getID());
            pstateInsertProcess.setString(2, process.getName());
            pstateInsertProcess.setString(3, process.getStatus());
            pstateInsertProcess.setString(4, process.getOwner());
            pstateInsertProcess.setString(5, process.getUniqueCode());
            pstateInsertProcess.setLong(6, process.getLogCount());
            pstateInsertProcess.setString(7, process.getTimeCreate());
            
            // Обработка NULL для finished_by
            if (process.getTimeEndWork() != null) {
                pstateInsertProcess.setString(8, process.getTimeEndWork().toString());
            } else {
                pstateInsertProcess.setString(8, null);
            }
            
            pstateInsertProcess.executeUpdate();
        } catch (SQLException excSQL) {
            throw new ExceptionDB(
                "SQL Error: " + excSQL.getMessage() + "\nSQLite state: " + excSQL.getSQLState(),
                "DatabaseManager.insertProcess()",
                "Failed to save process",
                500
            );
        } finally {
            try {
                if (pstateInsertProcess != null) {
                    pstateInsertProcess.clearParameters();
                }
            } catch (SQLException e) {
                // ignore
            }
        }
    }
    
    /**
     * Получает все процессы из базы данных.
     * 
     * @return список всех процессов
     * @throws ExceptionDB если ошибка при чтении
     */
    public List<Process> selectAllProcesses() throws ExceptionDB {
        List<Process> processes = new ArrayList<>();
        PreparedStatement pstate = collectionStatement.getSelectAllProcesses();
        try {
            ResultSet rs = pstate.executeQuery();
            while (rs.next()) {
                processes.add(mapRowToProcess(rs));
            }
            rs.close();
            return processes;
        } catch (SQLException excSQL) {
            throw new ExceptionDB(
                "SQL Error: " + excSQL.getMessage() + "\nSQLite state: " + excSQL.getSQLState(),
                "DatabaseManager.selectAllProcesses()",
                "Failed to get processes",
                500
            );
        } finally {
            try {
                if (pstate != null) {
                    pstate.clearParameters();
                }
            } catch (SQLException e) {
                // ignore
            }
        }
    }
    
    /**
     * Получает только активные процессы.
     * 
     * @return список активных процессов (status = 'Active')
     * @throws ExceptionDB если ошибка при чтении
     */
    public List<Process> selectActiveProcesses() throws ExceptionDB {
        List<Process> processes = new ArrayList<>();
        PreparedStatement pstate = collectionStatement.getSelectActiveProcesses();
        try {
            ResultSet rs = pstate.executeQuery();
            while (rs.next()) {
                processes.add(mapRowToProcess(rs));
            }
            rs.close();
            return processes;
        } catch (SQLException excSQL) {
            throw new ExceptionDB(
                "SQL Error: " + excSQL.getMessage() + "\nSQLite state: " + excSQL.getSQLState(),
                "DatabaseManager.selectActiveProcesses()",
                "Failed to get active processes",
                500
            );
        } finally {
            try {
                if (pstate != null) {
                    pstate.clearParameters();
                }
            } catch (SQLException e) {
                // ignore
            }
        }
    }
    
    /**
     * Получает процесс по уникальному идентификатору.
     * 
     * @param processID идентификатор процесса
     * @return объект Process или null, если не найден
     * @throws ExceptionDB если ошибка при чтении
     */
    public Process selectProcessById(String processID) throws ExceptionDB {
        PreparedStatement pstate = collectionStatement.getSelectProcessById();
        try {
            pstate.setString(1, processID);
            ResultSet rs = pstate.executeQuery();
            if (rs.next()) {
                Process process = mapRowToProcess(rs);
                rs.close();
                return process;
            }
            rs.close();
            return null;
        } catch (SQLException excSQL) {
            throw new ExceptionDB(
                "SQL Error: " + excSQL.getMessage() + "\nSQLite state: " + excSQL.getSQLState(),
                "DatabaseManager.selectProcessById()",
                "Failed to get process by ID",
                500
            );
        } finally {
            try {
                if (pstate != null) {
                    pstate.clearParameters();
                }
            } catch (SQLException e) {
                // ignore
            }
        }
    }
    
    /**
     * Обновляет статус процесса.
     * 
     * @param processID идентификатор процесса
     * @param status новый статус ('Active' или 'Finished')
     * @throws ExceptionDB если ошибка при обновлении
     */
    public void updateProcessStatus(String processID, String status) throws ExceptionDB {
        PreparedStatement pstate = collectionStatement.getUpdateProcessStatus();
        try {
            pstate.setString(1, status);
            pstate.setString(2, processID);
            pstate.executeUpdate();
        } catch (SQLException excSQL) {
            throw new ExceptionDB(
                "SQL Error: " + excSQL.getMessage() + "\nSQLite state: " + excSQL.getSQLState(),
                "DatabaseManager.updateProcessStatus()",
                "Failed to update process status",
                500
            );
        } finally {
            try {
                if (pstate != null) {
                    pstate.clearParameters();
                }
            } catch (SQLException e) {
                // ignore
            }
        }
    }
    
    /**
     * Обновляет время завершения процесса.
     * 
     * @param processID идентификатор процесса
     * @param finishedBy время завершения (может быть null)
     * @throws ExceptionDB если ошибка при обновлении
     */
    public void updateProcessFinishedBy(String processID, LocalDateTime finishedBy) throws ExceptionDB {
        PreparedStatement pstate = collectionStatement.getUpdateProcessFinishedBy();
        try {
            if (finishedBy != null) {
                pstate.setString(1, finishedBy.toString());
            } else {
                pstate.setString(1, null);
            }
            pstate.setString(2, processID);
            pstate.executeUpdate();
        } catch (SQLException excSQL) {
            throw new ExceptionDB(
                "SQL Error: " + excSQL.getMessage() + "\nSQLite state: " + excSQL.getSQLState(),
                "DatabaseManager.updateProcessFinishedBy()",
                "Failed to update finished time",
                500
            );
        } finally {
            try {
                if (pstate != null) {
                    pstate.clearParameters();
                }
            } catch (SQLException e) {
                // ignore
            }
        }
    }
    
    /**
     * Обновляет счетчик логов процесса.
     * 
     * @param processID идентификатор процесса
     * @param count новое значение счетчика
     * @throws ExceptionDB если ошибка при обновлении
     */
    public void updateLogCount(String processID, long count) throws ExceptionDB {
        PreparedStatement pstate = collectionStatement.getUpdateProcessLogsCount();
        try {
            pstate.setLong(1, count);
            pstate.setString(2, processID);
            pstate.executeUpdate();
        } catch (SQLException excSQL) {
            throw new ExceptionDB(
                "SQL Error: " + excSQL.getMessage() + "\nSQLite state: " + excSQL.getSQLState(),
                "DatabaseManager.updateLogCount()",
                "Failed to update log count",
                500
            );
        } finally {
            try {
                if (pstate != null) {
                    pstate.clearParameters();
                }
            } catch (SQLException e) {
                // ignore
            }
        }
    }
    
    // ==================== ЛОГИ (CRUD) ====================
    
    /**
     * Вставляет новый лог в базу данных.
     * 
     * <p>Примечание: количество логов процесса должно обновляться отдельно
     * через {@link #updateLogCount(String, long)}.</p>
     * 
     * @param processID идентификатор процесса-владельца
     * @param log объект лога для вставки
     * @throws ExceptionDB если ошибка при вставке
     */
    public void insertLog(String processID, DataLog log) throws ExceptionDB {
        PreparedStatement pstate = collectionStatement.getInsertLog();
        try {
            pstate.setString(1, processID);
            pstate.setString(2, log.getStatus());
            pstate.setString(3, log.getLogInfo());
            pstate.setInt(4, log.getNumberLog());
            pstate.setString(5, log.getTimeLog());
            pstate.executeUpdate();
        } catch (SQLException excSQL) {
            throw new ExceptionDB(
                "SQL Error: " + excSQL.getMessage() + "\nSQLite state: " + excSQL.getSQLState(),
                "DatabaseManager.insertLog()",
                "Failed to save log",
                500
            );
        } finally {
            try {
                if (pstate != null) {
                    pstate.clearParameters();
                }
            } catch (SQLException e) {
                // ignore
            }
        }
    }
    
    /**
     * Получает все логи процесса.
     * 
     * @param processID идентификатор процесса
     * @return список логов процесса
     * @throws ExceptionDB если ошибка при чтении
     */
    public List<DataLog> selectAllLogs(String processID) throws ExceptionDB {
        List<DataLog> logs = new ArrayList<>();
        PreparedStatement pstate = collectionStatement.getSelectAllLogs();
        try {
            pstate.setString(1, processID);
            ResultSet rs = pstate.executeQuery();
            while (rs.next()) {
                logs.add(mapRowToDataLog(rs));
            }
            rs.close();
            return logs;
        } catch (SQLException excSQL) {
            throw new ExceptionDB(
                "SQL Error: " + excSQL.getMessage() + "\nSQLite state: " + excSQL.getSQLState(),
                "DatabaseManager.selectAllLogs()",
                "Failed to get logs",
                500
            );
        } finally {
            try {
                if (pstate != null) {
                    pstate.clearParameters();
                }
            } catch (SQLException e) {
                // ignore
            }
        }
    }
    
    // /**
    //  * Получает логи процесса в диапазоне номеров.
    //  * 
    //  * @param processID идентификатор процесса
    //  * @param indexStart начальный номер (включительно)
    //  * @param indexEnd конечный номер (включительно)
    //  * @return список логов в указанном диапазоне
    //  * @throws ExceptionDB если ошибка при чтении
    //  */
    // public List<DataLog> selectLogsByRange(String processID, int indexStart, int indexEnd) throws ExceptionDB {
    //     List<DataLog> logs = new ArrayList<>();
    //     PreparedStatement pstate = collectionStatement.getSele;
    //     try {
    //         pstate.setString(1, processID);
    //         pstate.setInt(2, indexStart);
    //         pstate.setInt(3, indexEnd);
    //         ResultSet rs = pstate.executeQuery();
    //         while (rs.next()) {
    //             logs.add(mapRowToDataLog(rs));
    //         }
    //         rs.close();
    //         return logs;
    //     } catch (SQLException excSQL) {
    //         throw new ExceptionDB(
    //             "SQL Error: " + excSQL.getMessage() + "\nSQLite state: " + excSQL.getSQLState(),
    //             "DatabaseManager.selectLogsByRange()",
    //             "Failed to get logs by range",
    //             500
    //         );
    //     } finally {
    //         try {
    //             if (pstate != null) pstate.close();
    //         } catch (SQLException e) {
    //             // ignore
    //         }
    //     }
    // }
    
    /**
     * Получает логи по списку статусов.
     * 
     * @param processID идентификатор процесса
     * @param statuses список статусов для фильтрации
     * @return список отфильтрованных логов
     * @throws ExceptionDB если ошибка при чтении
     */
    public List<DataLog> selectLogsByStatuses(String processID, List<LogStatus> statuses) throws ExceptionDB {
        List<DataLog> logs = new ArrayList<>();
        PreparedStatement pstate = null;
        try {
            pstate = collectionStatement.getSelectLogByStatuses(statuses);
            pstate.setString(1, processID);
            for (int i = 0; i < statuses.size(); i++) {
                pstate.setString(i + 2, statuses.get(i).getStatus());
            }
            ResultSet rs = pstate.executeQuery();
            while (rs.next()) {
                logs.add(mapRowToDataLog(rs));
            }
            rs.close();
            pstate.close();
            return logs;
        } catch (SQLException excSQL) {
            throw new ExceptionDB(
                "SQL Error: " + excSQL.getMessage() + "\nSQLite state: " + excSQL.getSQLState(),
                "DatabaseManager.selectLogsByStatuses()",
                "Failed to get logs by statuses",
                500
            );
        }
        finally {
            if (pstate != null){
                try {
                    pstate.clearParameters();

                } 
                catch (SQLException exc) {
                    //ignore
                }
            }
        }
    }
    
    /**
     * Выполняет поиск логов по подстроке в сообщении.
     * 
     * @param processID идентификатор процесса
     * @param substring подстрока для поиска
     * @return список найденных логов
     * @throws ExceptionDB если ошибка при чтении
     */
    public List<DataLog> selectLogsBySubstring(String processID, String substring) throws ExceptionDB {
        List<DataLog> logs = new ArrayList<>();
        PreparedStatement pstate = collectionStatement.getSelectLogBySubstring();
        try {
            pstate.setString(1, processID);
            pstate.setString(2, "%" + substring + "%");
            ResultSet rs = pstate.executeQuery();
            while (rs.next()) {
                logs.add(mapRowToDataLog(rs));
            }
            rs.close();
            return logs;
        } catch (SQLException excSQL) {
            throw new ExceptionDB(
                "SQL Error: " + excSQL.getMessage() + "\nSQLite state: " + excSQL.getSQLState(),
                "DatabaseManager.selectLogsBySubstring()",
                "Failed to search logs",
                500
            );
        } finally {
            try {
                if (pstate != null) {
                    pstate.clearParameters();
                }
            } catch (SQLException e) {
                // ignore
            }
        }
    }
    
    /**
     * Подсчитывает количество логов процесса.
     * 
     * @param processID идентификатор процесса
     * @return количество логов
     * @throws ExceptionDB если ошибка при подсчете
     */
    public long countLogs(String processID) throws ExceptionDB {
        PreparedStatement pstate = collectionStatement.getSelectCountLogProcess();
        try {
            pstate.setString(1, processID);
            ResultSet rs = pstate.executeQuery();
            if (rs.next()) {
                long count = rs.getLong(1);
                rs.close();
                return count;
            }
            rs.close();
            return 0;
        } catch (SQLException excSQL) {
            throw new ExceptionDB(
                "SQL Error: " + excSQL.getMessage() + "\nSQLite state: " + excSQL.getSQLState(),
                "DatabaseManager.countLogs()",
                "Failed to count logs",
                500
            );
        } finally {
            try {
                if (pstate != null) {
                    pstate.clearParameters();
                }
            } catch (SQLException e) {
                // ignore
            }
        }
    }
    
    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================
    
    /**
     * Преобразует строку ResultSet в объект Process.
     * 
     * @param rs ResultSet с данными процесса
     * @return объект Process
     * @throws SQLException если ошибка при чтении данных
     */
    private Process mapRowToProcess(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        String owner = rs.getString("owner");
        String status = rs.getString("status");
        String uniqueCode = rs.getString("unique_code");
        long logCount = rs.getLong("log_count");
        
        // Чтение created_by (NOT NULL)
        LocalDateTime createdBy = LocalDateTime.parse(rs.getString("created_by"));
        
        // Чтение finished_by (может быть NULL)
        String finishedByStr = rs.getString("finished_by");
        LocalDateTime finishedBy;
        if (finishedByStr != null) {
            finishedBy = LocalDateTime.parse(finishedByStr);
        }
        else {
            finishedBy = null;
        }
        
        return new Process(
                        id,
                        name,
                        owner, 
                        status,
                        uniqueCode,
                        createdBy,
                        finishedBy,
                        logCount);
    }
    
    /**
     * Преобразует строку ResultSet в объект DataLog.
     * 
     * @param rs ResultSet с данными лога
     * @return объект DataLog
     * @throws SQLException если ошибка при чтении данных
     */
    private DataLog mapRowToDataLog(ResultSet rs) throws SQLException {
        String logMsg = rs.getString("log_msg");
        String status = rs.getString("status");
        int numberLog = rs.getInt("number_log");
        LocalDateTime createdBy = LocalDateTime.parse(rs.getString("created_by"));
        
        return new DataLog(logMsg,
                           status,
                           numberLog,
                           createdBy);
    }
    
    /**
     * Закрывает все ресурсы базы данных.
     * 
     * <p>Должен вызываться при завершении работы приложения.</p>
     */
    public void close() {
        try {
            if (collectionStatement != null) {
                collectionStatement.close();
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database resources: " + e.getMessage());
        }
    }
    
    /**
     * Возвращает подключение к базе данных.
     * 
     * @return объект Connection
     */
    public Connection getConnection() {
        return connection;
    }
    
    /**
     * Возвращает коллекцию подготовленных запросов.
     * 
     * @return объект CollectionStatement
     */
    public CollectionPreparedStatement getCollectionStatement() {
        return collectionStatement;
    }
}