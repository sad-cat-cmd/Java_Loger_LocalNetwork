package com.mycompany.repositories;

import com.mycompany.models.DataLog;
import com.mycompany.models.LogStatus;
import java.util.List;

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
 * // Сохранение лога
 * DataLog log = new DataLog("User logged in", "INFO", 200);
 * DataLog saved = repository.save(log, "proc_123", "secure_code");
 * 
 * // Получение всех логов процесса
 * List&lt;DataLog&gt; allLogs = repository.findAll("proc_123");
 * 
 * // Получение логов с пагинацией (первые 100)
 * List&lt;DataLog&gt; page = repository.findIdAndRange("proc_123", 0, 100);
 * 
 * // Поиск по статусам (только ошибки)
 * List&lt;LogStatus&gt; errors = Arrays.asList(LogStatus.FATAL);
 * List&lt;DataLog&gt; errorLogs = repository.findIdAndByStatuses("proc_123", errors);
 * 
 * // Поиск по подстроке
 * List&lt;DataLog&gt; paymentLogs = repository.findIdAndBySubstring("proc_123", "payment");
 * </pre>
 * 
 * <h2>Структура таблицы logs:</h2>
 * <pre>
 * CREATE TABLE logs (
 *     id INTEGER PRIMARY KEY AUTOINCREMENT,
 *     process_id TEXT NOT NULL,
 *     timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
 *     status TEXT NOT NULL,
 *     log_msg TEXT NOT NULL,
 *     FOREIGN KEY (process_id) REFERENCES processes(id) ON DELETE CASCADE
 * );
 * </pre>
 * 
 * <h2>Индексы для оптимизации:</h2>
 * <ul>
 *   <li>idx_logs_process_id - для быстрого поиска по процессу</li>
 *   <li>idx_logs_timestamp - для сортировки по времени</li>
 *   <li>idx_logs_status - для фильтрации по статусам</li>
 * </ul>
 * 
 * @author admin_
 * @version 1.0
 * @see DataLog
 * @see LogStatus
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
     * </ul>
     * 
     * <p>После сохранения лога счетчик log_count в процессе автоматически увеличивается.</p>
     * 
     * @param log        объект лога для сохранения (не может быть null)
     * @param processID  уникальный идентификатор процесса (не может быть null)
     * @param secureCode секретный код для проверки прав доступа (не может быть null)
     * @return сохраненный объект лога (с присвоенным ID)
     * @throws RuntimeException если:
     *         <ul>
     *           <li>Процесс с указанным ID не найден</li>
     *           <li>Неверный secureCode (не совпадает с кодом процесса)</li>
     *           <li>Процесс не активен (статус != Active)</li>
     *           <li>Данные лога не прошли валидацию</li>
     *           <li>Ошибка при выполнении SQL запроса</li>
     *           <li>log = null или processID = null</li>
     *         </ul>
     */
    DataLog save(DataLog log,
                 String processID,
                 String secureCode);
    
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
     * @throws RuntimeException если:
     *         <ul>
     *           <li>Процесс с указанным ID не найден</li>
     *           <li>Ошибка при выполнении SQL запроса</li>
     *           <li>processID = null</li>
     *         </ul>
     */
    List<DataLog> findAll(String processID);
    
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
     * 
     * // Получить все предупреждения и ошибки
     * List&lt;LogStatus&gt; warnings = Arrays.asList(LogStatus.FATAL, LogStatus.WARN);
     * List&lt;DataLog&gt; warnLogs = repository.findIdAndByStatuses(processId, warnings);
     * </pre>
     * 
     * @param processID уникальный идентификатор процесса (не может быть null)
     * @param statuses  список статусов для фильтрации (не может быть null или пустым)
     * @return список логов с указанными статусами (может быть пустым)
     * @throws RuntimeException если:
     *         <ul>
     *           <li>Процесс с указанным ID не найден</li>
     *           <li>Список статусов пуст или null</li>
     *           <li>Ошибка при выполнении SQL запроса</li>
     *           <li>processID = null</li>
     *         </ul>
     */
    List<DataLog> findIdAndByStatuses(String processID,
                                      List<LogStatus> statuses);
    
    /**
     * Выполняет поиск логов по подстроке в сообщении.
     * 
     * <p>Позволяет искать логи по содержимому сообщения.
     * Поиск чувствителен к регистру (SQL LIKE с использованием '%') и находит частичные совпадения.</p>
     * 
     * <p>Примеры использования:</p>
     * <pre>
     * // Найти все логи, содержащие "payment"
     * List&lt;DataLog&gt; paymentLogs = repository.findIdAndBySubstring(processId, "payment");
     * 
     * // Найти логи с ошибками базы данных
     * List&lt;DataLog&gt; dbErrors = repository.findIdAndBySubstring(processId, "SQL");
     * 
     * // Найти логи по ID пользователя
     * List&lt;DataLog&gt; userLogs = repository.findIdAndBySubstring(processId, "user_123");
     * </pre>
     * 
     * @param processID уникальный идентификатор процесса (не может быть null)
     * @param substring  подстрока для поиска (не может быть null или пустой)
     * @return список логов, содержащих указанную подстроку (может быть пустым)
     * @throws RuntimeException если:
     *         <ul>
     *           <li>Процесс с указанным ID не найден</li>
     *           <li>Подстрока пустая или null</li>
     *           <li>Ошибка при выполнении SQL запроса</li>
     *           <li>processID = null</li>
     *         </ul>
     */
    List<DataLog> findIdAndBySubstring(String processID,
                                       String substring);
    
    /**
     * Возвращает диапазон логов указанного процесса (для пагинации).
     * 
     * <p>Позволяет получать логи постранично для эффективной загрузки
     * больших объемов данных в пользовательском интерфейсе.</p>
     * 
     * <p>Сортировка: от новых к старым (по времени создания).</p>
     * 
     * <p>Примеры использования:</p>
     * <pre>
     * // Первая страница (первые 100 логов)
     * List&lt;DataLog&gt; page1 = repository.findIdAndRange(processId, 0, 100);
     * 
     * // Вторая страница (логи 100-199)
     * List&lt;DataLog&gt; page2 = repository.findIdAndRange(processId, 100, 200);
     * 
     * // Последние 50 логов
     * List&lt;DataLog&gt; recent = repository.findIdAndRange(processId, 0, 50);
     * </pre>
     * 
     * @param processID   уникальный идентификатор процесса (не может быть null)
     * @param indexStart  начальный индекс (включительно, 0-based)
     * @param indexEnd    конечный индекс (исключительно)
     * @return список логов в указанном диапазоне (может быть пустым)
     * @throws RuntimeException если:
     *         <ul>
     *           <li>Процесс с указанным ID не найден</li>
     *           <li>indexStart > indexEnd</li>
     *           <li>indexStart или indexEnd отрицательные</li>
     *           <li>Ошибка при выполнении SQL запроса</li>
     *           <li>processID = null</li>
     *         </ul>
     */
    List<DataLog> findIdAndRange(String processID,
                                 int indexStart,
                                 int indexEnd);
}