package com.mycompany.services;

import java.util.List;

import com.mycompany.database.ExceptionDB;
import com.mycompany.models.Process;
import com.mycompany.repositories.ExceptionAccess;
import com.mycompany.repositories.ExceptionFound;

/**
 * Бизнес-логика управления процессами.
 */
public interface ProcessService {

    /**
     * Регистрирует новый процесс.
     *
     * @param process процесс для регистрации
     * @return сохранённый процесс с присвоенным ID
     * @throws ExceptionDB ошибка БД
     */
    Process add(Process process) throws ExceptionDB;

    /**
     * Возвращает все процессы (включая завершённые).
     *
     * @return список всех процессов
     * @throws ExceptionDB ошибка БД
     */
    List<Process> getAll() throws ExceptionDB;

    /**
     * Возвращает только активные процессы (статус Active).
     *
     * @return список активных процессов
     * @throws ExceptionDB ошибка БД
     */
    List<Process> getAllActive() throws ExceptionDB;

    /**
     * Поиск процесса по ID.
     *
     * @param id уникальный идентификатор процесса
     * @return найденный процесс
     * @throws ExceptionDB     ошибка БД
     * @throws ExceptionFound  процесс не найден
     */
    Process search(String id) throws ExceptionDB, ExceptionFound;

    /**
     * Завершает процесс по ID с проверкой secureCode и флагом системного вызова.
     *
     * @param id            идентификатор процесса
     * @param secureCode    код подтверждения
     * @param flagSystemCall флаг системного вызова
     * @return завершённый процесс
     * @throws ExceptionDB     ошибка БД
     * @throws ExceptionFound  процесс не найден
     * @throws ExceptionAccess недостаточно прав
     */
    Process finish(String id,
                   String secureCode,
                   boolean flagSystemCall) throws ExceptionDB, ExceptionFound, ExceptionAccess;
}