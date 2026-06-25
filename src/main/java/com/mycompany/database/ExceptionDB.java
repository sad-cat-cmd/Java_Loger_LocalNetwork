package com.mycompany.database;

import java.time.LocalDateTime;

/**
 * Кастомное исключение для ошибок связанных с взаимодействием с БД.
 * 
 * <p>Предназначено для информирования системы об ошибках при:
 * <ul>
 *   <li>Получении данных из БД</li>
 *   <li>Загрузке данных в БД</li>
 *   <li>Подключения к БД</li>
 *   <li>Инициализации БД</li>
 * </ul>
 * </p>
 * 
 * <p>Особенности:
 * <ul>
 *   <li>Автоматически фиксирует время возникновения ошибки</li>
 *   <li>Поддерживает трассировку пути ошибки через стек вызовов</li>
 *   <li>Предоставляет форматированное сообщение для логирования</li>
 * </ul>
 * </p>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try {
 *     // операции в DBManager
 * } catch (IOException e) {
 *     throw new ExceptionConfig("Failed to read config", "DatabaseManager.DatabaseManager()", "Internal error", 500);
 * }
 * </pre>
 * 
 * @author admin_
 * @version 1.0
 * @see DatabaseManager
 */
public class ExceptionDB extends Exception{
    /** Время возникновения ошибки */
    private final LocalDateTime timeErr;
    
    /** Клиентское сообщение об ошибки */
    private final String clientMsg;

    /** Клиентский код ошибки */
    private final int clientCode;

    /** Текст ошибки */
    private final String msgErr;
    
    /** Трассировка стека вызовов (пользовательская) */
    private String stackTrace;

    public ExceptionDB(String msgErr,
                       String beginProgramUnit,
                       String clientMsg,
                       int clientCode){
        this.msgErr = msgErr;
        this.stackTrace = beginProgramUnit;
        this.clientMsg = clientMsg;
        this.clientCode = clientCode;
        timeErr = LocalDateTime.now();
    }

    /**
     * Добавляет единицу в трассировку стека.
     * 
     * <p>Позволяет отслеживать путь прохождения ошибки через различные
     * уровни приложения.</p>
     * 
     * @param programUnit название метода/класса, добавляемого в трассировку
     */
    public void addProgramUnitInTheStackTrace(String programUnit) {
        if (stackTrace == null) {
            stackTrace = programUnit;
        } else {
            stackTrace = programUnit + "->" + stackTrace;
        }
    }

    /**
     * Формирует комбинированное сообщение об ошибке для логирования.
     * 
     * <p>Включает временную метку, трассировку стека и текст ошибки.</p>
     * 
     * @return форматированное многострочное сообщение об ошибке
     */

    public String getCombinedLogMsg() {
        StringBuilder resultStr = new StringBuilder();
        
        resultStr.append("Configuration error occurred:");
        resultStr.append("\n\ttime: ").append(timeErr.toString());

        if (stackTrace != null && !stackTrace.isEmpty()) {
            resultStr.append("\n\tstack trace: ").append(stackTrace);   
        }
        
        if (msgErr != null && !msgErr.isEmpty()) {
            resultStr.append("\n\terror message: ").append(msgErr);
        } else {
            resultStr.append("\n\terror message: none");
        }
        
        return resultStr.toString();
    }

    /**
     * Возвращает сообщение для отправки клиенту.
     * 
     * @return сообщение об ошибке для клиента(обычно на русском/английском)
     */
    public String getClientMsg() {
        return clientMsg;
    }

    /**
     * Возвращает HTTP код ошибки.
     * 
     * @return HTTP статус код (например, 400 для Bad Request)
     */
    public int getClientCode() {
        return clientCode;
    }
}
