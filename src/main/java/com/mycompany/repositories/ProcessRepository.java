package com.mycompany.repositories;

import java.util.List;

import com.mycompany.database.ExceptionDB;
import com.mycompany.models.Process;

/**
 * Репозиторий для работы с процессами в базе данных.
 * 
 * <p>Предоставляет CRUD операции и дополнительные методы поиска для сущности {@link Process}.
 * Все методы репозитория работают на уровне доступа к данным и не содержат бизнес-логики.</p>
 * 
 * <h2>Ответственность:</h2>
 * <ul>
 *   <li>Сохранение новых процессов в БД</li>
 *   <li>Поиск процессов по идентификатору</li>
 *   <li>Получение списков процессов (все, активные)</li>
 *   <li>Обновление информации о процессе</li>
 *   <li>Завершение процесса (установка статуса Finished)</li>
 * </ul>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * ProcessRepository repository = new ProcessRepositoryImpl(dbManager);
 * 
 * try {
 *     // Создание и сохранение процесса
 *     Process process = new Process("billing-service", "admin@company.com", 100, 50);
 *     Process saved = repository.save(process);
 *     
 *     // Поиск по ID
 *     Process found = repository.findById("proc_123");
 *     
 *     // Получение всех процессов
 *     List&lt;Process&gt; allProcesses = repository.findAll();
 *     
 *     // Получение только активных процессов
 *     List&lt;Process&gt; activeProcesses = repository.findAllActive();
 *     
 *     // Завершение процесса
 *     Process finished = repository.finishProcess("proc_123");
 *     
 * } catch (ExceptionDB e) {
 *     // Ошибка базы данных
 *     System.err.println("Database error: " + e.getMessage());
 * } catch (ExceptionFound e) {
 *     // Процесс не найден
 *     System.err.println("Process not found: " + e.getClientMsg());
 * }
 * </pre>
 * 
 * <h2>Структура таблицы processes:</h2>
 * <pre>
 * CREATE TABLE processes (
 *     id TEXT PRIMARY KEY,
 *     name TEXT NOT NULL,
 *     owner TEXT NOT NULL,
 *     status TEXT NOT NULL DEFAULT 'Active',
 *     unique_code TEXT NOT NULL,
 *     created_by DATETIME NOT NULL,
 *     finished_by DATETIME,
 *     log_count INTEGER DEFAULT 0,
 *     CHECK (status IN ('Active', 'Finished'))
 * );
 * </pre>
 * 
 * <h2>Исключения:</h2>
 * <ul>
 *   <li>{@link ExceptionDB} - ошибки на уровне базы данных (SQL, соединение)</li>
 *   <li>{@link ExceptionFound} - запрашиваемая сущность не найдена</li>
 * </ul>
 * 
 * @author admin_
 * @version 1.0
 * @see Process
 * @see ExceptionDB
 * @see ExceptionFound
 */
public interface ProcessRepository {
    
    /**
     * Сохраняет новый процесс в базе данных.
     * 
     * <p>Выполняет вставку новой записи в таблицу processes.
     * Процесс должен быть предварительно создан через конструктор.
     * ID и uniqueCode генерируются автоматически при создании процесса.</p>
     * 
     * <p>При сохранении выполняются следующие проверки:</p>
     * <ul>
     *   <li>Имя процесса не должно существовать в БД</li>
     *   <li>Все обязательные поля должны быть заполнены</li>
     * </ul>
     * 
     * @param process объект процесса для сохранения (не может быть null)
     * @return сохраненный объект процесса (с присвоенным ID)
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     */
    Process save(Process process) throws ExceptionDB;
    
    /**
     * Находит процесс по уникальному идентификатору.
     * 
     * <p>Использует первичный ключ для быстрого поиска записи.</p>
     * 
     * @param processId уникальный идентификатор процесса (не может быть null или пустым)
     * @return найденный процесс
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     * @throws ExceptionFound если процесс с указанным ID не найден (код 404)
     */
    Process findById(String processId) throws ExceptionDB, ExceptionFound;
    
    /**
     * Возвращает список всех зарегистрированных процессов.
     * 
     * <p>Включает как активные, так и завершенные процессы.
     * Сортировка: от новых к старым (по времени создания).</p>
     * 
     * <p>Используется для:</p>
     * <ul>
     *   <li>Отображения списка всех процессов в админ-панели</li>
     *   <li>Экспорта данных</li>
     *   <li>Статистики</li>
     * </ul>
     * 
     * @return список всех процессов (может быть пустым, если процессов нет)
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     * @throws ExceptionFound если процессы не найдены
     */
    List<Process> findAll() throws ExceptionDB, ExceptionFound;
    
    /**
     * Возвращает список только активных процессов.
     * 
     * <p>Активными считаются процессы со статусом {@code Active}.
     * Завершенные процессы (Finished) в этот список не входят.</p>
     * 
     * <p>Сортировка: от новых к старым (по времени создания).</p>
     * 
     * <p>Используется для:</p>
     * <ul>
     *   <li>Отображения только работающих процессов</li>
     *   <li>Фильтрации процессов, которые могут принимать логи</li>
     *   <li>Мониторинга активных клиентов</li>
     * </ul>
     * 
     * @return список активных процессов (может быть пустым, если нет активных процессов)
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     * @throws ExceptionFound если активные процессы не найдены
     */
    List<Process> findAllActive() throws ExceptionDB, ExceptionFound;
    
    /**
     * Завершает процесс.
     * 
     * <p>Выполняет следующие операции:</p>
     * <ul>
     *   <li>Устанавливает статус процесса в {@code Finished}</li>
     *   <li>Устанавливает время завершения на текущий момент</li>
     * </ul>
     * 
     * <p>Используется для остановки работающего процесса.</p>
     * 
     * @param processId уникальный идентификатор процесса
     * @param uniqueCode уникальный код для доступа к процессу
     * @param flagSystemCall флаг, который указывается для вызова метода системой логировния
     * @return завершенный процесс (с обновленным статусом и временем)
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     * @throws ExceptionFound если процесс с указанным ID не найден (код 404)
     * @throws ExceptionAccess если процесс код
     */
    Process finishProcess(String processId,
                          String uniqueCode,
                          boolean flagSystemCall) throws ExceptionDB, ExceptionFound, ExceptionAccess;
}