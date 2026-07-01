package com.mycompany.repositories.impl;

import java.util.Collections;
import java.util.List;

import com.mycompany.database.DatabaseManager;
import com.mycompany.database.ExceptionDB;
import com.mycompany.models.DataLog;
import com.mycompany.models.LogStatus;
import com.mycompany.models.Process;
import com.mycompany.repositories.DataLogRepository;
import com.mycompany.repositories.ExceptionAccess;
import com.mycompany.repositories.ExceptionFound;

/**
 * Реализация репозитория для работы с логами в базе данных.
 * 
 * <p>Предоставляет CRUD операции и дополнительные методы поиска для сущности {@link DataLog}.
 * Все методы репозитория работают на уровне доступа к данным и не содержат бизнес-логики.</p>
 * 
 * <h2>Ответственность:</h2>
 * <ul>
 *   <li>Сохранение логов в БД с проверкой прав доступа</li>
 *   <li>Поиск логов по идентификатору процесса</li>
 *   <li>Фильтрация логов по статусам</li>
 *   <li>Поиск логов по подстроке в сообщении</li>
 *   <li>Пагинация при получении логов (от указанного индекса до конца)</li>
 * </ul>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * // Инициализация
 * DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite:logs.db");
 * DataLogRepository repository = new DataLogRepositoryImpl(dbManager);
 * 
 * try {
 *     // Сохранение лога
 *     DataLog log = new DataLog("User logged in", "INFO", 200);
 *     DataLog saved = repository.save(log, "proc_123", "secure_code");
 * 
 *     // Получение всех логов процесса
 *     List&lt;DataLog&gt; allLogs = repository.findAllLogProcess("proc_123");
 * 
 *     // Получение логов с 10-го номера до конца
 *     List&lt;DataLog&gt; logsFromIndex = repository.findIdByIndexFrom("proc_123", 10);
 * 
 *     // Получение логов по статусам
 *     List&lt;LogStatus&gt; errors = Arrays.asList(LogStatus.FATAL);
 *     List&lt;DataLog&gt; errorLogs = repository.findIdAndByStatuses("proc_123", errors);
 * 
 *     // Поиск логов по подстроке
 *     List&lt;DataLog&gt; searchResults = repository.findIdAndBySubstring("proc_123", "payment");
 *     
 * } catch (ExceptionFound e) {
 *     System.err.println("Process not found");
 * } catch (ExceptionAccess e) {
 *     System.err.println("Invalid secure code or process finished");
 * } catch (ExceptionDB e) {
 *     System.err.println("Database error: " + e.getMessage());
 * }
 * </pre>
 * 
 * <h2>Валидация в методах:</h2>
 * <ul>
 *   <li>Все методы проверяют существование процесса</li>
 *   <li>{@link #save(DataLog, String, String)} проверяет secureCode и статус процесса</li>
 *   <li>{@link #findIdAndByStatuses(String, List)} возвращает все логи, если статусы не указаны</li>
 *   <li>{@link #findIdAndBySubstring(String, String)} возвращает все логи, если подстрока пустая</li>
 *   <li>Валидация startIndex выполняется в {@link DatabaseManager#selectLogsByRange(String, int, int)}</li>
 * </ul>
 * 
 * <h2>Исключения:</h2>
 * <ul>
 *   <li>{@link ExceptionDB} - ошибки на уровне базы данных (SQL, соединение)</li>
 *   <li>{@link ExceptionFound} - запрашиваемый процесс не найден (HTTP 404)</li>
 *   <li>{@link ExceptionAccess} - неверный код доступа или процесс завершен (HTTP 401)</li>
 * </ul>
 * 
 * @author admin_
 * @version 1.0
 * @see DataLogRepository
 * @see DataLog
 * @see DatabaseManager
 * @see ExceptionDB
 * @see ExceptionFound
 * @see ExceptionAccess
 */
public class DataLogRepositoryImpl implements DataLogRepository {
    
    /** Менеджер базы данных для выполнения операций */
    private final DatabaseManager manager;
    
    /**
     * Создает новый экземпляр репозитория логов.
     * 
     * @param manager менеджер базы данных
     */
    public DataLogRepositoryImpl(DatabaseManager manager) {
        this.manager = manager;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Выполняет следующие проверки перед сохранением:</p>
     * <ol>
     *   <li>Процесс с указанным ID должен существовать</li>
     *   <li>Процесс должен быть активен (статус Active)</li>
     *   <li>SecureCode должен соответствовать уникальному коду процесса</li>
     * </ol>
     * 
     * <p>После успешного сохранения автоматически обновляется счетчик логов процесса.</p>
     * 
     * @param log объект лога для сохранения
     * @param processID идентификатор процесса
     * @param secureCode код доступа
     * @return сохраненный объект лога
     * @throws ExceptionAccess если:
     *         <ul>
     *           <li>Процесс уже завершен (статус Finished)</li>
     *           <li>Неверный secureCode</li>
     *         </ul>
     * @throws ExceptionFound если процесс с указанным ID не найден
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     */
    @Override
    public DataLog save(DataLog log,
                        String processID,
                        String secureCode) throws ExceptionAccess, ExceptionDB, ExceptionFound {
        try {
            Process foundProcess = manager.selectProcessById(processID);
            if (foundProcess == null) {
                throw new ExceptionFound();
            }
            if (foundProcess.isFinished()) {
                throw new ExceptionAccess();
            }
            if (!secureCode.equals(foundProcess.getUniqueCode())) {
                throw new ExceptionAccess();
            }
            manager.insertLog(processID, log);
            foundProcess.incrementLogCount();
            manager.updateLogCount(processID, foundProcess.getLogCount());
            
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("DataLogRepositoryImpl.save()");
            throw excDB;
        }
        return log;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Возвращает все логи процесса в порядке от новых к старым.</p>
     * 
     * @param processID идентификатор процесса
     * @return список всех логов процесса (может быть пустым)
     * @throws ExceptionFound если процесс с указанным ID не найден
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     */
    @Override
    public List<DataLog> findAllLogProcess(String processID) throws ExceptionDB, ExceptionFound {
        List<DataLog> logs;
        try {
            Process foundProcess = manager.selectProcessById(processID);
            if (foundProcess == null) {
                throw new ExceptionFound();
            }
            logs = manager.selectAllLogs(processID);
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("DataLogRepositoryImpl.findAllLogProcess()");
            throw excDB;
        }
        return logs;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Фильтрует логи процесса по указанным статусам.</p>
     * 
     * <p>Если список статусов пуст или null, возвращаются все логи процесса.</p>
     * 
     * @param processID идентификатор процесса
     * @param statuses список статусов для фильтрации
     * @return список отфильтрованных логов (может быть пустым)
     * @throws ExceptionFound если процесс с указанным ID не найден
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     */
    @Override
    public List<DataLog> findIdAndByStatuses(String processID, List<LogStatus> statuses) 
            throws ExceptionDB, ExceptionFound {
        List<DataLog> logs;
        try {
            Process foundProcess = manager.selectProcessById(processID);
            if (foundProcess == null) {
                throw new ExceptionFound();
            }
            
            if (statuses == null || statuses.isEmpty()) {
                logs = manager.selectAllLogs(processID);
            } else {
                logs = manager.selectLogsByStatuses(processID, statuses);
            }
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("DataLogRepositoryImpl.findIdAndByStatuses()");
            throw excDB;
        }
        return logs;
    }

    @Override
    public List<DataLog> findIdAndBySubstring(String processID, String substring) 
            throws ExceptionDB, ExceptionFound {
        List<DataLog> logs;
        try {
            Process foundProcess = manager.selectProcessById(processID);
            if (foundProcess == null) {
                throw new ExceptionFound();
            }
            
            if (substring == null || substring.trim().isEmpty()) {
                logs = manager.selectAllLogs(processID);
            } else {
                logs = manager.selectLogsBySubstring(processID, substring);
            }
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("DataLogRepositoryImpl.findIdAndBySubstring()");
            throw excDB;
        }
        return logs;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Возвращает логи процесса, начиная с указанного индекса и до конца.</p>
     * 
     * <p>Нумерация логов начинается с 1, поэтому для получения логов с
     * определенной позиции используется преобразование: startIndex + 1.</p>
     * 
     * <p>Примеры:</p>
     * <pre>
     * // Получить все логи (с 0-го индекса)
     * repository.findIdByIndexFrom("proc_123", 0);
     * // → SELECT ... WHERE number_log >= 1
     * 
     * // Получить логи с 10-го номера до конца
     * repository.findIdByIndexFrom("proc_123", 10);
     * // → SELECT ... WHERE number_log >= 11
     * </pre>
     * 
     * <p>Валидация startIndex выполняется в {@link DatabaseManager#selectLogsByRange(String, int, int)}.</p>
     * 
     * @param processID идентификатор процесса
     * @param startIndex начальный индекс (0-based)
     * @return список логов с указанного индекса до конца (может быть пустым)
     * @throws ExceptionFound если процесс с указанным ID не найден
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     */
    @Override
    public List<DataLog> findIdByIndexFrom(String processID,
                                           int startIndex) throws ExceptionDB, ExceptionFound {
        List<DataLog> logs;
        try {
            Process foundProcess = manager.selectProcessById(processID);
            if (foundProcess == null) {
                throw new ExceptionFound();
            }
            long logCount = foundProcess.getLogCount();
            if (startIndex >= logCount) {
                return Collections.emptyList();
            }
            logs = manager.selectLogsByRange(processID,
                                             startIndex + 1,
                                             (int) foundProcess.getLogCount());
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("DataLogRepositoryImpl.findIdByIndexFrom()");
            throw excDB;
        }
        

        return logs;
    }
}