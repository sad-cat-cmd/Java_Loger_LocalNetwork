package com.mycompany.services;

import java.util.List;

import com.mycompany.database.ExceptionDB;
import com.mycompany.models.DataLog;
import com.mycompany.models.LogStatus;
import com.mycompany.repositories.ExceptionAccess;
import com.mycompany.repositories.ExceptionFound;

/**
 * Сервис для работы с логами.
 * 
 * <p>Обеспечивает бизнес-логику для операций с логами:
 * <ul>
 *   <li>Добавление логов с проверкой прав</li>
 *   <li>Получение всех логов процесса</li>
 *   <li>Пагинация (от индекса до конца)</li>
 *   <li>Фильтрация по статусам</li>
 *   <li>Поиск по подстроке</li>
 * </ul>
 * 
 * @author admin_
 * @version 2.0
 * @see DataLog
 * @see ExceptionDB
 * @see ExceptionFound
 * @see ExceptionAccess
 */
public interface DataLogService {
    
    /**
     * Добавляет новый лог.
     * 
     * <p>Проверяет: существование процесса, активность, secureCode.</p>
     * 
     * @param log        объект лога
     * @param processID  идентификатор процесса
     * @param secureCode код доступа
     * @return сохраненный лог
     * @throws ExceptionFound   если процесс не найден
     * @throws ExceptionAccess  если процесс завершен или неверный код
     * @throws ExceptionDB      если ошибка БД
     */
    DataLog add(DataLog log,
                String processID,
                String secureCode) throws ExceptionDB, ExceptionFound, ExceptionAccess;
    
    /**
     * Возвращает все логи процесса.
     * 
     * <p>Сортировка: от новых к старым.</p>
     * 
     * @param processID идентификатор процесса
     * @return список логов (может быть пустым)
     * @throws ExceptionFound  если процесс не найден
     * @throws ExceptionDB     если ошибка БД
     */
    List<DataLog> getAll(String processID) throws ExceptionDB, ExceptionFound;
    
    /**
     * Возвращает логи с указанного индекса до конца.
     * 
     * <p>Пример: indexStart=10 → логи с 10-го номера до конца.</p>
     * 
     * @param processID  идентификатор процесса
     * @param indexStart начальный индекс (0-based)
     * @return список логов (может быть пустым)
     * @throws ExceptionFound  если процесс не найден
     * @throws ExceptionDB     если ошибка БД
     */
    List<DataLog> getByIndexFrom(String processID,
                                 int indexStart) throws ExceptionDB, ExceptionFound;

    /**
     * Возвращает логи с указанными статусами.
     * 
     * <p>Если статусы не указаны (null или пустой список) — возвращает все логи.</p>
     * 
     * @param processID идентификатор процесса
     * @param statuses  список статусов для фильтрации
     * @return список отфильтрованных логов (может быть пустым)
     * @throws ExceptionFound  если процесс не найден
     * @throws ExceptionDB     если ошибка БД
     */
    List<DataLog> getAllContainsStatuses(String processID,
                                         List<LogStatus> statuses) throws ExceptionDB, ExceptionFound;
    
    /**
     * Возвращает логи, содержащие подстроку в сообщении.
     * 
     * <p>Если подстрока пустая или null — возвращает все логи.</p>
     * 
     * @param processID идентификатор процесса
     * @param substring подстрока для поиска
     * @return список найденных логов (может быть пустым)
     * @throws ExceptionFound  если процесс не найден
     * @throws ExceptionDB     если ошибка БД
     */
    List<DataLog> getAllContainsSubstring(String processID,
                                          String substring) throws ExceptionDB, ExceptionFound;
}