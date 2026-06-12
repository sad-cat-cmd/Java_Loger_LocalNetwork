package com.mycompany.models;

import java.time.LocalDateTime;

/**
 * Кастомное исключение для ошибок валидации процесса.
 * 
 * <p>Содержит подробную информацию об ошибке валидации полей процесса:
 * <ul>
 *   <li>Временную метку возникновения ошибки</li>
 *   <li>Стек вызовов для отслеживания пути ошибки</li>
 *   <li>Конкретные сообщения об ошибках для имени и владельца</li>
 *   <li>HTTP код и сообщение для ответа клиенту</li>
 * </ul>
 * </p>
 * 
 * <h2>Структура сообщения об ошибке:</h2>
 * <pre>
 * Failed to validation:
 * 	time: 2024-01-15T10:30:45.123
 * 	stack trace: Process.Process()->Process.verificationInputMsgStrings()
 * 	Error name: length name > 100
 * 	Error owner: owner is null
 * </pre>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try {
 *     Process process = new Process(name, owner, maxName, maxOwner);
 * } catch (ExceptionProcess e) {
 *     e.addProgramUnitInTheStackTrace("MyClass.myMethod()");
 *     logger.error(e.getCombinedLogMsg());
 *     response.setStatus(e.getClientCode());
 *     response.getWriter().write(e.getClientMsg());
 * }
 * </pre>
 * 
 * <h2>HTTP коды ошибок:</h2>
 * <ul>
 *   <li>400 - Bad Request (некорректные данные)</li>
 *   <li>409 - Conflict (конфликт данных)</li>
 *   <li>500 - Internal Server Error (внутренняя ошибка сервера)</li>
 * </ul>
 * 
 * @author admin_
 * @version 1.0
 * @see Process
 */
public class ExceptionProcess extends Exception {
    
    /** Время возникновения ошибки */
    private final LocalDateTime timeErr;
    
    /** Сообщение об ошибке, связанной с именем процесса */
    private final String errorMsgName;
    
    /** Сообщение об ошибке, связанной с владельцем процесса */
    private final String errorMsgOwner;
    
    /** Пользовательская трассировка стека вызовов */
    private String stackTrace;
    
    /** HTTP код для ответа клиенту */
    private final int clientCode;
    
    /** Текст сообщения для клиента */
    private final String clientMsg;
    
    /**
     * Добавляет единицу в трассировку стека.
     * 
     * <p>Позволяет отслеживать путь прохождения ошибки через различные
     * уровни приложения. Каждый вызов добавляет новый элемент в начало цепочки.</p>
     * 
     * <p>Пример цепочки: {@code "Service.save()->Process.validate()->ExceptionProcess"}</p>
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
     * <p>Включает всю доступную информацию об ошибке в структурированном виде:</p>
     * <ul>
     *   <li>Временную метку</li>
     *   <li>Трассировку стека (если добавлена)</li>
     *   <li>Сообщения об ошибках для имени и владельца</li>
     * </ul>
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
        
        if (!errorMsgName.isEmpty()) {
            resultStr.append("\n\tError name: ").append(errorMsgName);
        } else {
            resultStr.append("\n\tError name: non-error");
        }
        
        if (!errorMsgOwner.isEmpty()) {
            resultStr.append("\n\tError owner: ").append(errorMsgOwner);
        } else {
            resultStr.append("\n\tError owner: non-error");
        }
        
        return resultStr.toString();
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
     * Возвращает сообщение для отправки клиенту.
     * 
     * @return пользовательское сообщение об ошибке (обычно на русском/английском)
     */
    public String getClientMsg() {
        return clientMsg;
    }

    /**
     * Создает новое исключение с детальной информацией об ошибке.
     * 
     * <p>Конструктор автоматически фиксирует время возникновения ошибки.</p>
     * 
     * @param errName      сообщение об ошибке имени процесса
     * @param errOwner     сообщение об ошибке владельца процесса
     * @param stackTrace   начальная единица трассировки стека
     * @param clientCode   HTTP код для ответа клиенту
     * @param clientMsg    текст сообщения для клиента
     */
    public ExceptionProcess(String errName,
                            String errOwner,
                            String stackTrace,
                            int clientCode,
                            String clientMsg) {
        super(String.format("Validation error - Name: %s, Owner: %s", errName, errOwner));
        this.errorMsgName = errName != null ? errName : "";
        this.errorMsgOwner = errOwner != null ? errOwner : "";
        this.stackTrace = stackTrace;
        this.clientCode = clientCode;
        this.clientMsg = clientMsg;
        this.timeErr = LocalDateTime.now();
    }
}