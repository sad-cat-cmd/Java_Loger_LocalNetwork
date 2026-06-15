package com.mycompany.services;

import java.util.List;

import com.mycompany.models.DataLog;
import com.mycompany.models.Process;

/**
 * Сервис для работы с логами.
 * 
 * <p>Предоставляет бизнес-логику для операций с записями логов:
 * <ul>
 *   <li>Добавление новых логов в систему</li>
 *   <li>Получение всех логов процесса</li>
 *   <li>Получение диапазона логов процесса</li>
 * </ul>
 * </p>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try {
 *     DataLogService logService = new DataLogServiceImpl(repository, maxLogLength);
 *     
 *     // Добавление лога
 *     DataLog newLog = new DataLog("User logged in", "INFO", 200);
 *     DataLog savedLog = logService.Add(newLog, "proc_123", "secure_code_456");
 *     
 *     // Получение всех логов процесса
 *     Process process = processService.Search("proc_123");
 *     List&lt;DataLog&gt; allLogs = logService.GetAll(process);
 *     
 *     // Получение последних логов
 *     List&lt;DataLog&gt; recentLogs = logService.GetRange(process);
 * } catch (ExceptionService e) {
 *     e.addProgramUnitInTheStackTrace("MyClass.myMethod()");
 *     System.err.println(e.getCombinedLogMsg());
 * }
 * </pre>
 * 
 * @author admin_
 * @version 1.0
 * @see DataLog
 * @see ExceptionService
 * @see Process
 */
public interface DataLogService {
    
    /**
     * Добавляет новую запись лога в систему.
     * 
     * <p>Выполняет следующие проверки:</p>
     * <ul>
     *   <li>Валидация процесса по ID и SecureCode</li>
     *   <li>Валидация данных лога (статус, длина сообщения)</li>
     *   <li>Автоматическое увеличение счетчика логов процесса</li>
     * </ul>
     * 
     * @param log        объект лога для добавления
     * @param ProcessId  уникальный идентификатор процесса
     * @param SecureCode секретный код для проверки прав доступа
     * @return сохраненный объект лога (с присвоенным ID)
     * @throws ExceptionService если произошла ошибка при добавлении лога
     */
    DataLog Add(DataLog log, String ProcessId, String SecureCode) throws ExceptionService;
    
    /**
     * Возвращает все логи указанного процесса.
     * 
     * <p>Логи возвращаются в хронологическом порядке (от старых к новым).</p>
     * 
     * @param IdProcess объект процесса, для которого запрашиваются логи
     * @return список всех логов процесса
     * @throws ExceptionService если процесс не найден или произошла ошибка БД
     */
    List<DataLog> GetAll(Process IdProcess) throws ExceptionService;
    
    /**
     * Возвращает диапазон логов указанного процесса.
     * 
     * <p>Используется для пагинации и получения последних записей.</p>
     * 
     * @param IdProcess объект процесса, для которого запрашиваются логи
     * @return список логов процесса в указанном диапазоне
     * @throws ExceptionService если процесс не найден или произошла ошибка БД
     */
    List<DataLog> GetRange(Process IdProcess) throws ExceptionService;
}