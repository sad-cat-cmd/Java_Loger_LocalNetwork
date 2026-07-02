package com.mycompany.services.impl;

import java.util.List;

import com.mycompany.database.DatabaseManager;
import com.mycompany.database.ExceptionDB;
import com.mycompany.models.DataLog;
import com.mycompany.models.LogStatus;
import com.mycompany.repositories.DataLogRepository;
import com.mycompany.repositories.ExceptionAccess;
import com.mycompany.repositories.ExceptionFound;
import com.mycompany.repositories.impl.DataLogRepositoryImpl;
import com.mycompany.services.DataLogService;

/**
 * Реализация сервиса для работы с логами.
 * 
 * <p>Предоставляет бизнес-логику для операций с записями логов.
 * Делегирует выполнение операций репозиторию {@link DataLogRepository}.</p>
 * 
 * <h2>Ответственность:</h2>
 * <ul>
 *   <li>Добавление новых логов с проверкой прав доступа</li>
 *   <li>Получение всех логов процесса</li>
 *   <li>Пагинация логов (от указанного индекса до конца)</li>
 *   <li>Фильтрация логов по статусам</li>
 *   <li>Поиск логов по подстроке в сообщении</li>
 *   <li>Проброс и логирование исключений с добавлением стека</li>
 * </ul>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * // Инициализация
 * DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite:logs.db");
 * DataLogService logService = new DataLogServiceImpl(dbManager);
 * 
 * try {
 *     // Добавление лога
 *     DataLog log = new DataLog("User logged in", "INFO", 200);
 *     DataLog saved = logService.add(log, "proc_123", "secure_code");
 *     
 *     // Получение всех логов
 *     List&lt;DataLog&gt; allLogs = logService.getAll("proc_123");
 *     
 *     // Получение логов с 10-го индекса
 *     List&lt;DataLog&gt; logsFromIndex = logService.getByIndexFrom("proc_123", 10);
 *     
 *     // Фильтрация по статусам
 *     List&lt;LogStatus&gt; statuses = Arrays.asList(LogStatus.INFO);
 *     List&lt;DataLog&gt; filtered = logService.getAllContainsStatuses("proc_123", statuses);
 *     
 *     // Поиск по подстроке
 *     List&lt;DataLog&gt; search = logService.getAllContainsSubstring("proc_123", "payment");
 *     
 * } catch (ExceptionFound e) {
 *     System.err.println("Process not found: " + e.getClientMsg());
 * } catch (ExceptionAccess e) {
 *     System.err.println("Access denied: " + e.getClientMsg());
 * } catch (ExceptionDB e) {
 *     System.err.println("Database error: " + e.getCombinedLogMsg());
 * }
 * </pre>
 * 
 * <h2>Исключения:</h2>
 * <ul>
 *   <li>{@link ExceptionFound} - процесс не найден (HTTP 404)</li>
 *   <li>{@link ExceptionAccess} - неверный код доступа или процесс завершен (HTTP 401)</li>
 *   <li>{@link ExceptionDB} - ошибки на уровне базы данных (HTTP 500)</li>
 * </ul>
 * 
 * @author admin_
 * @version 1.0
 * @see DataLogService
 * @see DataLogRepository
 * @see DataLog
 * @see DatabaseManager
 */
public class DataLogServiceImpl implements DataLogService {
    
    /** Репозиторий для работы с логами */
    private final DataLogRepository repository;
    
    /**
     * Создает новый экземпляр сервиса логов.
     * 
     * <p>Инициализирует репозиторий {@link DataLogRepositoryImpl} для работы с БД.</p>
     * 
     * @param manager менеджер базы данных
     */
    public DataLogServiceImpl(DatabaseManager manager) {
        this.repository = new DataLogRepositoryImpl(manager);
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Делегирует сохранение лога репозиторию.
     * При возникновении ошибки базы данных добавляет информацию о стеке вызовов.</p>
     * 
     * @param log        объект лога для сохранения
     * @param processID  идентификатор процесса
     * @param secureCode код доступа
     * @return сохраненный объект лога
     * @throws ExceptionDB    если ошибка базы данных
     * @throws ExceptionFound если процесс не найден
     * @throws ExceptionAccess если неверный код доступа или процесс завершен
     */
    @Override
    public DataLog add(DataLog log,
                       String processID,
                       String secureCode) throws ExceptionDB, ExceptionFound, ExceptionAccess {
        DataLog savedLog = null;
        try {
            savedLog = repository.save(log, processID, secureCode);
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("DataLogServiceImpl.add()");
            throw excDB;
        } catch (ExceptionFound excFound) {
            throw excFound;
        } catch (ExceptionAccess excAccess) {
            throw excAccess;
        }
        return savedLog;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Делегирует получение всех логов репозиторию.
     * Логи возвращаются в порядке от новых к старым.</p>
     * 
     * @param processID идентификатор процесса
     * @return список всех логов процесса (может быть пустым)
     * @throws ExceptionDB    если ошибка базы данных
     * @throws ExceptionFound если процесс не найден
     */
    @Override
    public List<DataLog> getAll(String processID) throws ExceptionDB, ExceptionFound {
        List<DataLog> logs = null;
        try {
            logs = repository.findAllLogProcess(processID);
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("DataLogServiceImpl.getAll()");
            throw excDB;
        } catch (ExceptionFound excFound) {
            throw excFound;
        }
        return logs;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Делегирует получение логов с указанного индекса репозиторию.
     * Используется для эффективной пагинации больших объемов данных.</p>
     * 
     * @param processID  идентификатор процесса
     * @param indexStart начальный индекс (0-based)
     * @return список логов с указанного индекса до конца (может быть пустым)
     * @throws ExceptionDB    если ошибка базы данных
     * @throws ExceptionFound если процесс не найден
     */
    @Override
    public List<DataLog> getByIndexFrom(String processID,
                                        int indexStart) throws ExceptionDB, ExceptionFound {
        List<DataLog> logs = null;
        try {
            logs = repository.findIdByIndexFrom(processID, indexStart);
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("DataLogServiceImpl.getByIndexFrom()");
            throw excDB;
        } catch (ExceptionFound excFound) {
            throw excFound;
        }
        return logs;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Делегирует фильтрацию логов по статусам репозиторию.
     * Если список статусов пуст или null, возвращаются все логи процесса.</p>
     * 
     * @param processID идентификатор процесса
     * @param statuses  список статусов для фильтрации
     * @return список отфильтрованных логов (может быть пустым)
     * @throws ExceptionDB    если ошибка базы данных
     * @throws ExceptionFound если процесс не найден
     */
    @Override
    public List<DataLog> getAllContainsStatuses(String processID,
                                                List<LogStatus> statuses) throws ExceptionDB, ExceptionFound {
        List<DataLog> logs = null;
        try {
            logs = repository.findIdAndByStatuses(processID, statuses);
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("DataLogServiceImpl.getAllContainsStatuses()");
            throw excDB;
        } catch (ExceptionFound excFound) {
            throw excFound;
        }
        return logs;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Делегирует поиск логов по подстроке репозиторию.
     * Если подстрока пустая или null, возвращаются все логи процесса.</p>
     * 
     * @param processID идентификатор процесса
     * @param substring подстрока для поиска
     * @return список найденных логов (может быть пустым)
     * @throws ExceptionDB    если ошибка базы данных
     * @throws ExceptionFound если процесс не найден
     */
    @Override
    public List<DataLog> getAllContainsSubstring(String processID,
                                                 String substring) throws ExceptionDB, ExceptionFound {
        List<DataLog> logs = null;
        try {
            logs = repository.findIdAndBySubstring(processID, substring);
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("DataLogServiceImpl.getAllContainsSubstring()");
            throw excDB;
        } catch (ExceptionFound excFound) {
            throw excFound;
        }
        return logs;
    }
}