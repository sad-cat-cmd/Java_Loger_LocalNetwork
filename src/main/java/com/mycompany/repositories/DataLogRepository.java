package com.mycompany.repositories;

import java.util.List;

import com.mycompany.database.ExceptionDB;
import com.mycompany.models.DataLog;
import com.mycompany.models.LogStatus;

/**
 * Репозиторий для работы с логами в базе данных.
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
 *   <li>Пагинация при получении логов</li>
 * </ul>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
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
 *     // Получение логов с пагинацией (первые 100 логов с номера 10)
 *     List&lt;DataLog&gt; page = repository.findIdByIndexFrom("proc_123", 10);
 * 
 *     // Поиск по статусам (только ошибки)
 *     List&lt;LogStatus&gt; errors = Arrays.asList(LogStatus.FATAL);
 *     List&lt;DataLog&gt; errorLogs = repository.findIdAndByStatuses("proc_123", errors);
 * 
 *     // Поиск по подстроке
 *     List&lt;DataLog&gt; paymentLogs = repository.findIdAndBySubstring("proc_123", "payment");
 *     
 * } catch (ExceptionFound e) {
 *     System.err.println("Process not found");
 * } catch (ExceptionAccess e) {
 *     System.err.println("Invalid secure code");
 * } catch (ExceptionDB e) {
 *     System.err.println("Database error");
 * }
 * </pre>
 * 
 * <h2>Структура таблицы logs:</h2>
 * <pre>
 * CREATE TABLE logs (
 *     id INTEGER PRIMARY KEY AUTOINCREMENT,
 *     id_parent_process TEXT NOT NULL,
 *     status TEXT NOT NULL,
 *     log_msg TEXT NOT NULL,
 *     number_log INTEGER DEFAULT 0,
 *     created_by DATETIME NOT NULL,
 *     FOREIGN KEY (id_parent_process) REFERENCES processes(id) ON DELETE CASCADE
 * );
 * </pre>
 * 
 * <h2>Индексы для оптимизации:</h2>
 * <ul>
 *   <li>idx_logs_process_id - для быстрого поиска по процессу</li>
 *   <li>idx_logs_number - для сортировки и пагинации</li>
 *   <li>idx_logs_status - для фильтрации по статусам</li>
 * </ul>
 * 
 * <h2>Исключения:</h2>
 * <ul>
 *   <li>{@link ExceptionDB} - ошибки на уровне базы данных (SQL, соединение)</li>
 *   <li>{@link ExceptionFound} - запрашиваемый процесс не найден (HTTP 404)</li>
 *   <li>{@link ExceptionAccess} - неверный код доступа (HTTP 401)</li>
 * </ul>
 * 
 * @author admin_
 * @version 1.0
 * @see DataLog
 * @see LogStatus
 * @see ExceptionDB
 * @see ExceptionFound
 * @see ExceptionAccess
 */
public interface DataLogRepository {
    
    /**
     * Сохраняет лог в базе данных.
     * 
     * <p>Выполняет вставку новой записи в таблицу logs.
     * Для успешного сохранения должны быть выполнены следующие условия:</p>
     * <ul>
     *   <li>Процесс с указанным ID должен существовать</li>
     *   <li>SecureCode должен соответствовать коду процесса</li>
     *   <li>Данные лога должны быть валидны (проверка в модели)</li>
     *   <li>Процесс должен быть активен</li>
     * </ul>
     * 
     * <p>После сохранения лога счетчик log_count в процессе автоматически увеличивается.</p>
     * 
     * @param log        объект лога для сохранения (не может быть null)
     * @param processID  уникальный идентификатор процесса (не может быть null)
     * @param secureCode секретный код для проверки прав доступа (не может быть null)
     * @return сохраненный объект лога (с присвоенным ID)
     * @throws ExceptionAccess если:
     *         <ul>
     *           <li>Неверный secureCode (не совпадает с кодом процесса)</li>
     *           <li>Процесс не активен (статус != Active)</li>
     *         </ul>
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     * @throws ExceptionFound если процесс с указанным ID не найден
     */
    DataLog save(DataLog log,
                 String processID,
                 String secureCode) throws ExceptionAccess, ExceptionDB, ExceptionFound;
    
    /**
     * Возвращает все логи указанного процесса.
     * 
     * <p>Логи возвращаются в хронологическом порядке (от новых к старым).</p>
     * 
     * <p>Используется для:</p>
     * <ul>
     *   <li>Полного просмотра истории логов процесса</li>
     *   <li>Экспорта данных</li>
     *   <li>Аудита</li>
     * </ul>
     * 
     * @param processID уникальный идентификатор процесса (не может быть null)
     * @return список всех логов процесса (может быть пустым, если логов нет)
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     * @throws ExceptionFound если процесс с указанным ID не найден
     */
    List<DataLog> findAllLogProcess(String processID) throws ExceptionDB, ExceptionFound;
    
    /**
     * Возвращает логи процесса с указанными статусами.
     * 
     * <p>Позволяет фильтровать логи по уровню важности.
     * Поддерживает множественные статусы для гибкой фильтрации.</p>
     * 
     * <p>Примеры использования:</p>
     * <pre>
     * // Получить только критические ошибки
     * List&lt;LogStatus&gt; critical = Arrays.asList(LogStatus.FATAL);
     * List&lt;DataLog&gt; criticalLogs = repository.findIdAndByStatuses(processId, critical);
     * 
     * // Получить информационные сообщения (INFO и DEBUG)
     * List&lt;LogStatus&gt; info = Arrays.asList(LogStatus.INFO, LogStatus.DEBUG);
     * List&lt;DataLog&gt; infoLogs = repository.findIdAndByStatuses(processId, info);
     * </pre>
     * 
     * @param processID уникальный идентификатор процесса (не может быть null)
     * @param statuses  список статусов для фильтрации (не может быть null или пустым)
     * @return список логов с указанными статусами (может быть пустым)
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     * @throws ExceptionFound если процесс с указанным ID не найден
     */
    List<DataLog> findIdAndByStatuses(String processID,
                                      List<LogStatus> statuses) throws ExceptionDB, ExceptionFound;
    
    /**
     * Выполняет поиск логов по подстроке в сообщении.
     * 
     * <p>Позволяет искать логи по содержимому сообщения.
     * Поиск чувствителен к регистру и находит частичные совпадения.</p>
     * 
     * <p>Примеры использования:</p>
     * <pre>
     * // Найти все логи, содержащие "payment"
     * List&lt;DataLog&gt; paymentLogs = repository.findIdAndBySubstring(processId, "payment");
     * 
     * // Найти логи с ошибками базы данных
     * List&lt;DataLog&gt; dbErrors = repository.findIdAndBySubstring(processId, "SQL");
     * </pre>
     * 
     * @param processID уникальный идентификатор процесса (не может быть null)
     * @param substring  подстрока для поиска (не может быть null или пустой)
     * @return список логов, содержащих указанную подстроку (может быть пустым)
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     * @throws ExceptionFound если процесс с указанным ID не найден
     */
    List<DataLog> findIdAndBySubstring(String processID,
                                       String substring) throws ExceptionDB, ExceptionFound;
    
    /**
     * Возвращает логи процесса, начиная с указанного индекса.
     * 
     * <p>Позволяет получать логи постранично для эффективной загрузки
     * больших объемов данных в пользовательском интерфейсе.</p>
     * 
     * <p>Сортировка: от новых к старым (по номеру лога).</p>
     * 
     * <p>Примеры использования:</p>
     * <pre>
     * // Получить логи с 10-го номера до конца
     * List&lt;DataLog&gt; logs = repository.findIdByIndexFrom("proc_123", 10);
     * 
     * // Получить логи с 0-го номера до конца (все логи)
     * List&lt;DataLog&gt; allLogs = repository.findIdByIndexFrom("proc_123", 0);
     * </pre>
     * 
     * @param processID   уникальный идентификатор процесса (не может быть null)
     * @param startIndex  начальный индекс (включительно, 0-based)
     * @return список логов с указанного индекса до конца (может быть пустым)
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     * @throws ExceptionFound если процесс с указанным ID не найден
     */
    List<DataLog> findIdByIndexFrom(String processID,
                                    int startIndex) throws ExceptionDB, ExceptionFound;
}