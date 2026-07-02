package com.mycompany.services.impl;

import com.mycompany.services.ProcessService;
import com.mycompany.models.Process;
import com.mycompany.database.ExceptionDB;
import com.mycompany.database.DatabaseManager;
import com.mycompany.repositories.impl.ProcessRepositoryImpl;
import com.mycompany.repositories.ProcessRepository;
import com.mycompany.repositories.ExceptionAccess;
import com.mycompany.repositories.ExceptionFound;

import java.util.List;

/**
 * Реализация сервиса управления процессами.
 * 
 * <p>Обеспечивает бизнес-логику для операций с процессами, делегируя
 * работу репозиторию {@link ProcessRepository}.</p>
 * 
 * <p>Все методы логируют ошибки, добавляя информацию о вызывающем методе
 * в стек исключений через {@link ExceptionDB#addProgramUnitInTheStackTrace(String)}.</p>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * DatabaseManager manager = new DatabaseManager();
 * ProcessService service = new ProcessServiceImpl(manager);
 * 
 * // Создание процесса
 * Process process = new Process("billing", "admin", 100, 50);
 * Process saved = service.add(process);
 * System.out.println("Создан процесс с ID: " + saved.getId());
 * 
 * // Поиск процесса
 * Process found = service.search(saved.getId());
 * 
 * // Завершение процесса
 * Process finished = service.finish(saved.getId(), "secret", true);
 * </pre>
 * 
 * @author admin_
 * @version 1.0
 * @see ProcessService
 * @see ProcessRepository
 * @see ExceptionDB
 */
public class ProcessServiceImpl implements ProcessService {

    private final ProcessRepository repository;

    /**
     * Создаёт экземпляр сервиса с репозиторием, инициализированным через {@link DatabaseManager}.
     *
     * @param manager менеджер подключения к базе данных
     */
    public ProcessServiceImpl(DatabaseManager manager) {
        repository = new ProcessRepositoryImpl(manager);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Сохраняет процесс через репозиторий и возвращает сохранённый объект
     * (с присвоенным идентификатором). В случае ошибки БД выбрасывает {@link ExceptionDB}
     * с добавленной информацией о контексте вызова.</p>
     *
     * @param process процесс для регистрации
     * @return сохранённый процесс с присвоенным ID
     * @throws ExceptionDB ошибка при сохранении в БД
     */
    @Override
    public Process add(Process process) throws ExceptionDB {
        Process saved = null;
        try {
            saved = repository.save(process);
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("ProcessServiceImpl.add()");
            throw excDB;
        }
        return saved;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Возвращает все процессы из репозитория (включая завершённые).</p>
     *
     * @return список всех процессов
     * @throws ExceptionDB ошибка при чтении из БД
     */
    @Override
    public List<Process> getAll() throws ExceptionDB {
        List<Process> processes = null;
        try {
            processes = repository.findAll();
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("ProcessServiceImpl.getAll()");
            throw excDB;
        }
        return processes;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Возвращает только активные процессы (со статусом {@code Active}).</p>
     *
     * @return список активных процессов
     * @throws ExceptionDB ошибка при чтении из БД
     */
    @Override
    public List<Process> getAllActive() throws ExceptionDB {
        List<Process> processes = null;
        try {
            processes = repository.findAllActive();
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("ProcessServiceImpl.getAllActive()");
            throw excDB;
        }
        return processes;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Ищет процесс по уникальному идентификатору.</p>
     *
     * @param id уникальный идентификатор процесса
     * @return найденный процесс
     * @throws ExceptionDB    ошибка при чтении из БД
     * @throws ExceptionFound процесс с указанным ID не найден
     */
    @Override
    public Process search(String id) throws ExceptionDB, ExceptionFound {
        Process process = null;
        try {
            process = repository.findById(id);
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("ProcessServiceImpl.search()");
            throw excDB;
        } catch (ExceptionFound excFound) {
            throw excFound;
        }
        return process;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Завершает процесс по ID с проверкой кода подтверждения и флага системного вызова.
     * Делегирует выполнение репозиторию.</p>
     *
     * @param id              идентификатор процесса
     * @param secureString    код подтверждения
     * @param flagSystemCall  флаг системного вызова
     * @return завершённый процесс
     * @throws ExceptionDB     ошибка при обновлении в БД
     * @throws ExceptionFound  процесс с указанным ID не найден
     * @throws ExceptionAccess недостаточно прав для завершения процесса
     */
    @Override
    public Process finish(String id,
                          String secureString,
                          boolean flagSystemCall) throws ExceptionDB, ExceptionFound, ExceptionAccess {
        Process process = null;
        try {
            process = repository.finishProcess(id,
                                               secureString,
                                               flagSystemCall);
        } catch (ExceptionDB excDB) {
            excDB.addProgramUnitInTheStackTrace("ProcessServiceImpl.finish()");
            throw excDB;
        } catch (ExceptionFound excFound) {
            throw excFound;
        } catch (ExceptionAccess excAccess) {
            throw excAccess;
        }
        return process;
    }
}