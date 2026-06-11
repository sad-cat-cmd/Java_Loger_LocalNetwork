/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.models;

import java.time.LocalDateTime;

/**
 * Кастомное исключение для ошибок валидации данных лога.
 * 
 * <p>Содержит подробную информацию об ошибке, включая:
 * <ul>
 *   <li>Стек вызовов для отслеживания пути ошибки</li>
 *   <li>HTTP код и сообщение для клиента</li>
 *   <li>Временную метку возникновения ошибки</li>
 *   <li>Конкретные сообщения об ошибках для статуса и информации</li>
 * </ul>
 * </p>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try {
 *     // какая-то операция
 * } catch (ExceptionDataLog e) {
 *     e.addProgramUnitInTheStackTrace("MyClass.myMethod()");
 *     sendResponse(e.getClientCode(), e.getClientMsg());
 *     log.error(e.getCombindLogMsg());
 * }
 * </pre>
 * 
 * @author admin_
 * @version 1.0
 * @see DataLog
 */
public class ExceptionDataLog extends Exception {
    
    /** Сообщение об ошибке, связанной со статусом */
    private final String errorMsgStatus;
    
    /** Сообщение об ошибке, связанной с информационной частью */
    private final String errorMsgInfo;
    
    /** Трассировка стека вызовов (пользовательская) */
    private String stackTrace;
    
    /** Сообщение для отправки клиенту */
    private final String clientMsg;
    
    /** HTTP код ответа */
    private final int clientCode;
    
    /** Время возникновения исключения */
    private final LocalDateTime timeErr;
    
    /**
     * Добавляет单元 в трассировку стека.
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
     * <p>Включает временную метку, трассировку стека и детали ошибок.</p>
     * 
     * @return многострочное форматированное сообщение об ошибке
     */
    public String getCombinedLogMsg() {
        StringBuilder resultStr = new StringBuilder();
        
        resultStr.append("Failed to validation:");
        resultStr.append("\n\ttime: ").append(timeErr.toString());
        
        if (stackTrace != null && !stackTrace.isEmpty()) {
            resultStr.append("\n\tstack trace: ").append(stackTrace);
        }
        
        if (!errorMsgStatus.isEmpty()) {
            resultStr.append("\n\terror status: ").append(errorMsgStatus);
        } else {
            resultStr.append("\n\terror status: non-error");
        }
        
        if (!errorMsgInfo.isEmpty()) {
            resultStr.append("\n\tError information: ").append(errorMsgInfo);
        } else {
            resultStr.append("\n\tError information: non-error");
        }
        
        return resultStr.toString();
    }
    
    /**
     * Возвращает сообщение для отправки клиенту.
     * 
     * @return пользовательское сообщение об ошибке
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
    
    /**
     * Возвращает временную метку возникновения ошибки.
     * 
     * @return объект {@link LocalDateTime} времени ошибки
     */
    public LocalDateTime getTimeErr() {
        return timeErr;
    }
    
    /**
     * Создает новое исключение с детальной информацией об ошибке.
     * 
     * @param errInfo           сообщение об ошибке информационного поля
     * @param errStatus         сообщение об ошибке поля статуса
     * @param beginProgramUnit  начальная единица трассировки стека
     * @param HttpCode         HTTP код для ответа клиенту
     * @param HttpBody         текст сообщения для клиента
     */
    public ExceptionDataLog(String errInfo,
                            String errStatus,
                            String beginProgramUnit,
                            int HttpCode,
                            String HttpBody) {
        super(String.format("Validation error - Status: %s, Info: %s", errStatus, errInfo));
        this.errorMsgStatus = errStatus;
        this.errorMsgInfo = errInfo;
        this.stackTrace = beginProgramUnit;
        this.clientCode = HttpCode;
        this.clientMsg = HttpBody;
        this.timeErr = LocalDateTime.now();
    }
}