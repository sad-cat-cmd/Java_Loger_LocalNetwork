package com.mycompany.services;

import java.util.List;

import com.mycompany.models.Process;

/**
 * Сервис для управления процессами логирования.
 * 
 * <p>Предоставляет бизнес-логику для операций с процессами:
 * <ul>
 *   <li>Регистрация новых процессов</li>
 *   <li>Поиск и фильтрация процессов</li>
 *   <li>Редактирование информации о процессах</li>
 * </ul>
 * </p>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try {
 *     ProcessService processService = new ProcessServiceImpl(repository, maxNameLength, maxOwnerLength);
 *     
 *     // Регистрация нового процесса
 *     Process newProcess = new Process("billing-service", "admin@company.com", 100, 50);
 *     Process savedProcess = processService.add(newProcess);
 *     
 *     // Поиск процесса по ID
 *     Process found = processService.search("proc_123");
 *     
 *     // Получение всех активных процессов
 *     List&lt;Process&gt; activeProcesses = processService.getAllActive();
 *     
 *     // Редактирование процесса
 *     savedProcess.setStatusFinished();
 *     Process updated = processService.editProcess(savedProcess);
 * } catch (ExceptionService e) {
 *     e.addProgramUnitInTheStackTrace("MyClass.myMethod()");
 *     System.err.println(e.getCombinedLogMsg());
 * }
 * </pre>
 * 
 * @author admin_
 * @version 1.0
 * @see Process
 * @see ExceptionService
 */
public interface ProcessService {
    
    /**
     * Регистрирует новый процесс в системе.
     * 
     * <p>Выполняет валидацию имени и владельца процесса.</p>
     * 
     * @param process объект процесса для регистрации
     * @return сохраненный объект процесса (с присвоенным ID)
     * @throws ExceptionService если валидация не пройдена или произошла ошибка БД
     */
    Process add(Process process) throws ExceptionService;
    
    /**
     * Возвращает список всех зарегистрированных процессов.
     * 
     * @return список всех процессов (включая завершенные)
     * @throws ExceptionService если произошла ошибка при чтении из БД
     */
    List<Process> getAll() throws ExceptionService;
    
    /**
     * Возвращает список только активных процессов.
     * 
     * <p>Активными считаются процессы со статусом {@code Active}.</p>
     * 
     * @return список активных процессов
     * @throws ExceptionService если произошла ошибка при чтении из БД
     */
    List<Process> getAllActive() throws ExceptionService;
    
    /**
     * Выполняет поиск процесса по уникальному идентификатору.
     * 
     * @param id уникальный идентификатор процесса
     * @return найденный процесс
     * @throws ExceptionService если процесс не найден или произошла ошибка БД
     */
    Process search(String id) throws ExceptionService;
    
    /**
     * Обновляет информацию о процессе.
     * 
     * <p>Позволяет изменять статус процесса и другие поля.</p>
     * 
     * @param newProcess объект процесса с обновленными данными
     * @return обновленный объект процесса
     * @throws ExceptionService если процесс не найден или произошла ошибка БД
     */
    Process editProcess(Process newProcess) throws ExceptionService;
}