package com.mycompany.facades;

import java.util.List;
import com.mycompany.models.DataLog;
import com.mycompany.models.Process;
import com.mycompany.models.LogStatus;
import com.mycompany.services.ExceptionService;
/**
 * Фасадный сервис для координации операций логирования.
 * 
 * <p>Предоставляет унифицированный интерфейс для работы с системой логирования.
 * Объединяет вызовы к {@code ProcessService} и {@code DataLogService},
 * управляет транзакциями и бизнес-логикой высокого уровня.</p>
 * 
 * <h2>Основные функции:</h2>
 * <ul>
 *   <li>Управление процессами (регистрация, поиск, остановка)</li>
 *   <li>Управление логами (добавление, получение, фильтрация)</li>
 *   <li>Проверка прав доступа через уникальный код</li>
 *   <li>Координация между сервисами</li>
 * </ul>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try {
 *     LoggingFacadeService facade = new LoggingFacadeServiceImpl(
 *         processService, dataLogService, config
 *     );
 *     
 *     // Регистрация нового процесса
 *     Process process = facade.AddProcess(new Process("billing", "admin", 100, 50));
 *     String processId = process.getID();
 *     String secureCode = process.getUniqueCode();
 *     
 *     // Добавление лога
 *     DataLog log = new DataLog("User logged in", LogStatus.INFO, 200);
 *     DataLog savedLog = facade.AddDataLog(processId, log, secureCode);
 *     
 *     // Получение всех логов процесса
 *     List&lt;DataLog&gt; allLogs = facade.GetAllLog(processId);
 *     
 *     // Получение только ошибок
 *     List&lt;LogStatus&gt; errorStatuses = Arrays.asList(LogStatus.FATAL);
 *     List&lt;DataLog&gt; errors = facade.GetAllLogContainsStatuses(processId, errorStatuses);
 *     
 *     // Остановка процесса
 *     Process stopped = facade.StopProcess(processId, secureCode);
 *     
 * } catch (ExceptionService e) {
 *     e.addProgramUnitInTheStackTrace("MyClass.myMethod()");
 *     System.err.println(e.getCombinedLogMsg());
 * }
 * </pre>
 * 
 * <h2>Поток выполнения:</h2>
 * <ol>
 *   <li>Handler → Facade (валидация прав, координация)</li>
 *   <li>Facade → ProcessService (работа с процессами)</li>
 *   <li>Facade → DataLogService (работа с логами)</li>
 * </ol>
 * 
 * @author admin_
 * @version 1.0
 * @see com.mycompany.services.ProcessService
 * @see com.mycompany.services.DataLogService
 * @see com.mycompany.models.Process
 * @see com.mycompany.models.DataLog
 */
public interface LoggingFacadeService {
    
    // ==================== Управление процессами ====================
    
    /**
     * Регистрирует новый процесс в системе.
     * 
     * <p>Выполняет следующие операции:</p>
     * <ul>
     *   <li>Валидация имени и владельца процесса</li>
     *   <li>Проверка уникальности имени процесса</li>
     *   <li>Создание процесса со статусом {@code Active}</li>
     *   <li>Генерация уникального кода для авторизации</li>
     *   <li>Сохранение процесса в БД</li>
     * </ul>
     * 
     * @param process объект процесса для регистрации
     * @return зарегистрированный процесс с присвоенным ID и уникальным кодом
     * @throws ExceptionService если:
     *         <ul>
     *           <li>Имя или владелец невалидны</li>
     *           <li>Процесс с таким именем уже существует</li>
     *           <li>Ошибка при сохранении в БД</li>
     *         </ul>
     */
    Process AddProcess(Process process) throws ExceptionService;
    
    /**
     * Возвращает список всех зарегистрированных процессов.
     * 
     * <p>Включает как активные, так и завершенные процессы.</p>
     * 
     * @return список всех процессов
     * @throws ExceptionService если ошибка при чтении из БД
     */
    List<Process> GetAllProcess() throws ExceptionService;
    
    /**
     * Возвращает список только активных процессов.
     * 
     * <p>Активными считаются процессы со статусом {@code Active}.</p>
     * 
     * @return список активных процессов
     * @throws ExceptionService если ошибка при чтении из БД
     */
    List<Process> GetAllActiveProcess() throws ExceptionService;
    
    /**
     * Выполняет поиск процесса по уникальному идентификатору.
     * 
     * @param processId уникальный идентификатор процесса
     * @return найденный процесс
     * @throws ExceptionService если процесс не найден
     */
    Process SearchProcess(String processId) throws ExceptionService;
    
    /**
     * Останавливает процесс.
     * 
     * <p>Выполняет следующие операции:</p>
     * <ul>
     *   <li>Проверка существования процесса</li>
     *   <li>Валидация уникального кода (авторизация)</li>
     *   <li>Изменение статуса на {@code Finished}</li>
     *   <li>Фиксация времени окончания работы</li>
     *   <li>Сохранение изменений в БД</li>
     * </ul>
     * 
     * @param processId уникальный идентификатор процесса
     * @param uniqueCode уникальный код для авторизации
     * @return остановленный процесс
     * @throws ExceptionService если:
     *         <ul>
     *           <li>Процесс не найден</li>
     *           <li>Неверный уникальный код</li>
     *           <li>Процесс уже завершен</li>
     *           <li>Ошибка при сохранении в БД</li>
     *         </ul>
     */
    Process StopProcess(String processId, String uniqueCode) throws ExceptionService;
    
    // ==================== Управление логами ====================
    
    /**
     * Добавляет новый лог в систему.
     * 
     * <p>Выполняет следующие операции:</p>
     * <ul>
     *   <li>Проверка существования процесса по ID</li>
     *   <li>Валидация уникального кода (авторизация)</li>
     *   <li>Проверка статуса процесса (должен быть активен)</li>
     *   <li>Валидация данных лога (статус, длина сообщения)</li>
     *   <li>Сохранение лога в БД</li>
     *   <li>Увеличение счетчика логов процесса</li>
     * </ul>
     * 
     * @param processId уникальный идентификатор процесса
     * @param log       объект лога для добавления
     * @param uniqueCode уникальный код для авторизации
     * @return сохраненный лог (с присвоенным ID)
     * @throws ExceptionService если:
     *         <ul>
     *           <li>Процесс не найден</li>
     *           <li>Неверный уникальный код</li>
     *           <li>Процесс не активен</li>
     *           <li>Данные лога невалидны</li>
     *           <li>Ошибка при сохранении в БД</li>
     *         </ul>
     */
    DataLog AddDataLog(String processId,
                       DataLog log,
                       String uniqueCode) throws ExceptionService;
    
    /**
     * Возвращает все логи указанного процесса.
     * 
     * <p>Логи возвращаются в хронологическом порядке (от старых к новым).</p>
     * 
     * @param processId уникальный идентификатор процесса
     * @return список всех логов процесса
     * @throws ExceptionService если:
     *         <ul>
     *           <li>Процесс не найден</li>
     *           <li>Ошибка при чтении из БД</li>
     *         </ul>
     */
    List<DataLog> GetAllLog(String processId) throws ExceptionService;
    
    /**
     * Возвращает диапазон логов указанного процесса (для пагинации).
     * 
     * <p>Позволяет получать логи постранично для эффективной загрузки
     * больших объемов данных.</p>
     * 
     * @param processId  уникальный идентификатор процесса
     * @param indexStart начальный индекс (включительно)
     * @param indexEnd   конечный индекс (исключительно)
     * @return список логов в указанном диапазоне
     * @throws ExceptionService если:
     *         <ul>
     *           <li>Процесс не найден</li>
     *           <li>Неверные индексы (start > end или отрицательные)</li>
     *           <li>Ошибка при чтении из БД</li>
     *         </ul>
     */
    List<DataLog> GetRangeLog(String processId,
                              int indexStart,
                              int indexEnd) throws ExceptionService;
    
    /**
     * Возвращает логи процесса с указанными статусами.
     * 
     * <p>Позволяет фильтровать логи по уровню важности.
     * Например, получить только ошибки (FATAL) или только информационные сообщения.</p>
     * 
     * <p>Пример использования:</p>
     * <pre>
     * List&lt;LogStatus&gt; criticalStatuses = Arrays.asList(LogStatus.FATAL);
     * List&lt;DataLog&gt; criticalLogs = facade.GetAllLogContainsStatuses(processId, criticalStatuses);
     * </pre>
     * 
     * @param processId уникальный идентификатор процесса
     * @param statuses  список статусов для фильтрации
     * @return список логов с указанными статусами
     * @throws ExceptionService если:
     *         <ul>
     *           <li>Процесс не найден</li>
     *           <li>Список статусов пуст или null</li>
     *           <li>Ошибка при чтении из БД</li>
     *         </ul>
     */
    List<DataLog> GetAllLogContainsStatuses(String processId,
                                            List<LogStatus> statuses) throws ExceptionService;
    
    /**
     * Выполняет поиск логов по подстроке в сообщении.
     * 
     * <p>Позволяет искать логи по содержимому сообщения.
     * Поиск чувствителен к регистру.</p>
     * 
     * <p>Пример использования:</p>
     * <pre>
     * List&lt;DataLog&gt; paymentLogs = facade.GetAllLogContainsSubstring(processId, "payment");
     * </pre>
     * 
     * @param processId уникальный идентификатор процесса
     * @param substring  подстрока для поиска
     * @return список логов, содержащих указанную подстроку
     * @throws ExceptionService если:
     *         <ul>
     *           <li>Процесс не найден</li>
     *           <li>Подстрока пустая или null</li>
     *           <li>Ошибка при чтении из БД</li>
     *         </ul>
     */
    List<DataLog> GetAllLogContainsSubstring(String processId,
                                             String substring) throws ExceptionService;
}