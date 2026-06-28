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
 * </ul>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * ProcessRepository repository = new ProcessRepositoryImpl(dbManager);
 * 
 * // Создание и сохранение процесса
 * Process process = new Process("billing-service", "admin@company.com", 100, 50);
 * Process saved = repository.save(process);
 * 
 * // Поиск по ID
 * Process found = repository.findById("proc_123");
 * 
 * // Получение всех процессов
 * List&lt;Process&gt; allProcesses = repository.findAll();
 * 
 * // Получение только активных процессов
 * List&lt;Process&gt; activeProcesses = repository.findAllActive();
 * 
 * // Обновление процесса
 * found.setStatusFinished();
 * Process updated = repository.update(found);
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
 *     time_create DATETIME NOT NULL,
 *     time_end_work DATETIME,
 *     log_count INTEGER DEFAULT 0,
 *     CHECK (status IN ('Active', 'Finished'))
 * );
 * </pre>
 * 
 * @author admin_
 * @version 1.0
 * @see Process
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
     * @throws если:
     *         <ul>
     *           <li>Ошибка при выполнении SQL запроса</li>
     *           <li>process = null</li>
     *         </ul>
     */
    Process save(Process process) throws ExceptionDB;
    
    /**
     * Находит процесс по уникальному идентификатору.
     * 
     * <p>Использует первичный ключ для быстрого поиска записи.</p>
     * 
     * @param id уникальный идентификатор процесса (не может быть null)
     * @return найденный процесс или null, если процесс не найден
     * @throws ExceptionDB если:
     *         <ul>
     *           <li>Ошибка при выполнении SQL запроса</li>
     *           <li>id = null</li>
     *         </ul>
     */
    Process findById(String processId) throws ExceptionDB;
    
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
     */
    List<Process> findAll() throws ExceptionDB;
    
    /**
     * Обновляет информацию о процессе.
     * 
     * <p>Позволяет изменять следующие поля процесса:</p>
     * <ul>
     *   <li>Статус (Active ↔ Finished)</li>
     *   <li>Время окончания работы</li>
     *   <li>Количество логов (автоматически обновляется)</li>
     * </ul>
     * 
     * <p>Поиск процесса для обновления выполняется по ID.</p>
     * 
     * @param process объект процесса с обновленными данными (не может быть null)
     * @return обновленный объект процесса
     * @throws ExceptionDB если:
     *         <ul>
     *           <li>Процесс с указанным ID не найден</li>
     *           <li>Ошибка при выполнении SQL запроса</li>
     *           <li>process = null</li>
     *         </ul>
     */
    Process update(Process process) throws ExceptionDB;
    
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
     */
    List<Process> findAllActive() throws ExceptionDB;
    
    /**
     * Завершает процесс
     * <p>Задает статус "Finished" для процесса</p>
     * <p>Задает время завершения
     * @return завершенный процесс
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     */
    Process finishProcess(String processId) throws ExceptionDB;
}