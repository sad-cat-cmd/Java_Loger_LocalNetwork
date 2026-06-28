package com.mycompany.repositories.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.mycompany.database.DatabaseManager;
import com.mycompany.database.ExceptionDB;
import com.mycompany.models.Process;
import com.mycompany.repositories.ExceptionAccess;
import com.mycompany.repositories.ExceptionFound;
import com.mycompany.repositories.ProcessRepository;

/**
 * Реализация репозитория для работы с процессами в базе данных.
 * 
 * <p>Предоставляет CRUD операции и дополнительные методы поиска для сущности {@link Process}.
 * Все методы репозитория работают на уровне доступа к данным и не содержат бизнес-логики.</p>
 * 
 * <h2>Ответственность:</h2>
 * <ul>
 *   <li>Сохранение новых процессов в БД</li>
 *   <li>Поиск процессов по идентификатору</li>
 *   <li>Получение списков процессов (все, активные)</li>
 *   <li>Завершение процесса (установка статуса Finished)</li>
 * </ul>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * // Инициализация
 * DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite:logs.db");
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
 *     // Завершение процесса (системный вызов)
 *     Process finished = repository.finishProcess("proc_123", null, true);
 *     
 *     // Завершение процесса (пользовательский вызов)
 *     Process finished = repository.finishProcess("proc_123", "secure_code_456", false);
 *     
 * } catch (ExceptionDB e) {
 *     System.err.println("Database error: " + e.getMessage());
 * } catch (ExceptionFound e) {
 *     System.err.println("Process not found");
 * } catch (ExceptionAccess e) {
 *     System.err.println("Invalid secure code");
 * }
 * </pre>
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
 * @see ProcessRepository
 * @see Process
 * @see DatabaseManager
 */
public class ProcessRepositoryImpl implements ProcessRepository {
    
    /** Менеджер базы данных для выполнения операций */
    private final DatabaseManager managerDB;
    
    /**
     * Создает новый экземпляр репозитория процессов.
     * 
     * @param managerDB менеджер базы данных
     */
    public ProcessRepositoryImpl(DatabaseManager managerDB) {
        this.managerDB = managerDB;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Вставляет новый процесс в базу данных.
     * Процесс должен быть предварительно создан через конструктор.</p>
     * 
     * <p>При сохранении не выполняется проверка на дубликаты — это ответственность
     * бизнес-слоя.</p>
     * 
     * @param process объект процесса для сохранения
     * @return сохраненный процесс (с присвоенным ID)
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     */
    @Override
    public Process save(Process process) throws ExceptionDB {
        try {
            managerDB.insertProcess(process);
            return process;
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("ProcessRepositoryImpl.save()");
            throw excDB;
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Выполняет поиск процесса по уникальному идентификатору.</p>
     * 
     * @param id уникальный идентификатор процесса
     * @return найденный процесс
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     * @throws ExceptionFound если процесс с указанным ID не найден
     */
    @Override
    public Process findById(String id) throws ExceptionDB, ExceptionFound {
        Process foundProcess;
        try {
            foundProcess = managerDB.selectProcessById(id);
            if (foundProcess == null) {
                throw new ExceptionFound();
            }
            return foundProcess;
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("ProcessRepositoryImpl.findById()");
            throw excDB;
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Возвращает список всех зарегистрированных процессов.
     * Включает как активные, так и завершенные процессы.</p>
     * 
     * <p>Если процессов нет, возвращается пустой список (не null).</p>
     * 
     * @return список всех процессов (может быть пустым)
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     */
    @Override
    public List<Process> findAll() throws ExceptionDB {
        List<Process> processes;
        try {
            processes = managerDB.selectAllProcesses();
            return processes;
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("ProcessRepositoryImpl.findAll()");
            throw excDB;
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Возвращает список только активных процессов со статусом {@code Active}.
     * Завершенные процессы (Finished) в этот список не входят.</p>
     * 
     * <p>Если активных процессов нет, возвращается пустой список (не null).</p>
     * 
     * @return список активных процессов (может быть пустым)
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     */
    @Override
    public List<Process> findAllActive() throws ExceptionDB {
        List<Process> processes;
        try {
            processes = managerDB.selectActiveProcesses();
            return processes;
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("ProcessRepositoryImpl.findAllActive()");
            throw excDB;
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Завершает процесс, устанавливая статус {@code Finished} и время завершения.</p>
     * 
     * <h3>Поток выполнения:</h3>
     * <ol>
     *   <li>Поиск процесса по ID</li>
     *   <li>Проверка существования (ExceptionFound если не найден)</li>
     *   <li>Проверка кода доступа (ExceptionAccess если не совпадает и не системный вызов)</li>
     *   <li>Установка статуса Finished в объекте</li>
     *   <li>Обновление статуса в БД</li>
     *   <li>Обновление времени завершения в БД</li>
     *   <li>Возврат обновленного процесса</li>
     * </ol>
     * 
     * <h3>Параметр flagSystemCall:</h3>
     * <ul>
     *   <li><b>true</b> - системный вызов (проверка uniqueCode пропускается)</li>
     *   <li><b>false</b> - пользовательский вызов (требуется валидный uniqueCode)</li>
     * </ul>
     * 
     * @param processId уникальный идентификатор процесса
     * @param uniqueCode уникальный код доступа к процессу
     * @param flagSystemCall флаг системного вызова
     * @return завершенный процесс (с обновленным статусом и временем)
     * @throws ExceptionDB если ошибка при выполнении SQL запроса
     * @throws ExceptionFound если процесс с указанным ID не найден
     * @throws ExceptionAccess если уникальный код доступа неверный
     */
    @Override
    public Process finishProcess(String processId, String uniqueCode, boolean flagSystemCall) 
            throws ExceptionDB, ExceptionFound, ExceptionAccess {
        Process foundProcess;
        try {
            // 1. Поиск процесса
            foundProcess = managerDB.selectProcessById(processId);
            if (foundProcess == null) {
                throw new ExceptionFound();
            }
            
            // 2. Проверка прав доступа (если не системный вызов)
            if (flagSystemCall == true) {
                foundProcess.setStatusFinished();
                managerDB.updateProcessStatus(processId, foundProcess.getStatus());
                managerDB.updateProcessFinishedBy(processId, 
                    LocalDateTime.parse(foundProcess.getTimeEndWork()));
                return foundProcess;
            }
            
            // 3. Проверка уникального кода (для пользовательских вызовов)
            if (!foundProcess.getUniqueCode().equals(uniqueCode)) {
                throw new ExceptionAccess();
            }
            
            // 4. Завершение процесса
            foundProcess.setStatusFinished();
            managerDB.updateProcessStatus(processId, foundProcess.getStatus());
            managerDB.updateProcessFinishedBy(processId, 
                LocalDateTime.parse(foundProcess.getTimeEndWork()));
            
            return foundProcess;
            
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("ProcessRepositoryImpl.finishProcess()");
            throw excDB;
        }
    }
}