package com.mycompany.services;

import java.util.List;

import com.mycompany.models.DataLog;
import com.mycompany.models.LogStatus;
import com.mycompany.models.Process;

/**
 * Сервис для работы с логами.
 * 
 * <p>Предоставляет бизнес-логику для операций с записями логов:
 * <ul>
 *   <li>Добавление новых логов в систему</li>
 *   <li>Получение всех логов процесса</li>
 *   <li>Получение диапазона логов процесса</li>
 *   <li>Получение всех логов с определенными статусами
 *   <li>Получение всех логов, содержащие определенную подсторку
 * </ul>
 * </p>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try {
 *     List <DataLog> allLogs;
 *     List <DataLog> allLogsRange;
 *     List <DataLog> allLogsWithSubstr;
 *     List <DataLog> allLogsWithStatuses;
 * 
 *     DataLogService logService = new DataLogServiceImpl(repository, maxLogLength);
 *     
 *     // Добавление лога
 *     DataLog newLog = new DataLog("User logged in", "INFO", 200);
 *     DataLog savedLog = logService.Add(newLog, "proc_123", "secure_code_456");
 *     
 *    
 *     Process process = processService.search("proc_123");
 *     // Получение всех логов процесса
 *     allLogs = logService.getALl(process.getID)
 * 
 *     // Получение диапазона логов
 *     allLogsRange = logService.getRange(0, 10);
 *     
 *     // Получение логов с определенными статусами
 *     allLogsWithStatuses = logService.getAllLogContainsSubstring();
 * 
 *     // Получение логов с вхождением определенной подстроки
 *     allLogsWithSubstr = logService.getAll();
 *      
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
     * @param processID  уникальный идентификатор процесса
     * @param SecureCode секретный код для проверки прав доступа
     * @return сохраненный объект лога (с присвоенным ID)
     * @throws ExceptionService если произошла ошибка при добавлении лога
     */
    DataLog add(DataLog log,
                String processID,
                String SecureCode) throws ExceptionService;
    
    /**
     * Возвращает все логи указанного процесса.
     * 
     * <p>Логи возвращаются в хронологическом порядке (от старых к новым).</p>
     * 
     * @param processID id процесса, для которого запрашиваются логи
     * @return список всех логов процесса
     * @throws ExceptionService если процесс не найден или произошла ошибка БД
     */
    List<DataLog> getAll(Process processID) throws ExceptionService;
    
    /**
     * Возвращает диапазон логов указанного процесса.
     * 
     * <p>Используется для пагинации и получения последних записей.</p>
     * 
     * @param processID id процесса, к которому запрашиваются логи
     * @return список логов процесса в указанном диапазоне
     * @throws ExceptionService если процесс не найден или произошла ошибка БД
     */
    List<DataLog> getRange(String processID,
                           int indexStart,
                           int indexEnd) throws ExceptionService;

    /**
     * Возвращает все логи процесса, имеющие заданные статусы.
     * 
     * <p>Используется для пагинации и получения последних записей.</p>
     * 
     * @param processID id процесса, к которому запрашиваются логи
     * @return список логов процесса в указанном диапазоне
     * @throws ExceptionService если процесс не найден или произошла ошибка БД
     */
    List<DataLog> getAllLogContainsStatuses(String processID,
                                            List<LogStatus> statuses) throws ExceptionService;
    
    /**
     * Возвращает все логи процесса, содержащие подтроку.
     * 
     * <p>Используется для пагинации и получения последних записей.</p>
     * 
     * @param processID id процесса, к которому запрашиваются логи
     * @return список логов процесса в указанном диапазоне
     * @throws ExceptionService если процесс не найден или произошла ошибка БД
     */
    List<DataLog> getAllLogContainsSubstring(String processID,
                                             String subString) throws ExceptionService;
    
}