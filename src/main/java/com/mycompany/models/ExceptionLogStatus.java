package com.mycompany.models;

import java.time.LocalDateTime;

/**
 * Кастомное исключение для ошибок валидации статуса лога.
 * 
 * <p>Содержит подробную информацию об ошибке валидации полей процесса:
 * <ul>
 *   <li>Временную метку возникновения ошибки</li>
 *   <li>Стек вызовов для отслеживания пути ошибки</li>
 *   <li>Конкретные сообщения об ошибке валадации статуса</li>
 *   <li>HTTP код и сообщение для ответа клиенту</li>
 * </ul>
 * </p>
 * 
 * <h2>Структура сообщения об ошибке:</h2>
 * <pre>
 * Failed to validation:
 * 	time: 2024-01-15T10:30:45.123
 * 	stack trace: LogStatus.LogStatus()->LogStatus.verificationInputStatus()
 * 	Error: status is empty
 * </pre>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try {
 *     LogStatus log = new LogStatus(status);
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
public class ExceptionLogStatus extends Exception{
    /** Время возникновения ошибки */
    private final LocalDateTime timeErr;
    
    /** Текст ошибки для логирования */
    private final String msgError;
    
    /** HTTP код для ответа клиенту */
    private final int clientCode;
    
    /** Сообщение для отправки клиенту */
    private final String clientMsg;
    
    /** Пользовательская трассировка стека вызовов */
    private String stackTrace;

    /**
     * Добавляет единицу в трассировку стека.
     * 
     * <p>Позволяет отслеживать путь прохождения ошибки через различные
     * уровни приложения. Каждый вызов добавляет новый элемент в начало цепочки.</p>
     * 
     * <p>Пример цепочки: {@code "Facade.save()->Service.Add()->Repository.save()"}</p>
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
     * <p>Включает всю доступную информацию об ошибке в структурированном виде:
     * <ul>
     *   <li>Временную метку</li>
     *   <li>Трассировку стека (если добавлена)</li>
     *   <li>Текст ошибки</li>
     * </ul>
     * </p>
     * 
     * @return многострочное форматированное сообщение об ошибке
     */
    public String getCombinedLogMsg() {
        StringBuilder resultStr = new StringBuilder();
        
        resultStr.append("Failed to service work:");
        resultStr.append("\n\ttime: ").append(timeErr.toString());
        
        if (stackTrace != null && !stackTrace.isEmpty()) {
            resultStr.append("\n\tstack trace: ").append(stackTrace);
        }
        
        if (msgError != null && !msgError.isEmpty()) {
            resultStr.append("\n\tError: ").append(msgError);
        } else {
            resultStr.append("\n\tError: non-error");
        }
        
        return resultStr.toString();
    }
    
    /**
     * Возвращает HTTP код ошибки для отправки клиенту.
     * 
     * @return HTTP статус код (400, 404, 409, 500 и т.д.)
     */
    public int getClientCode() {
        return clientCode;
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
     * Создает новое исключение с детальной информацией об ошибке.
     * 
     * <p>Конструктор автоматически фиксирует время возникновения ошибки.</p>
     * 
     * @param errorMsg     сообщение, содержащие ошибку
     * @param stackTrace   начальная единица трассировки стека
     * @param clientCode   HTTP код для ответа клиенту
     * @param clientMsg    текст сообщения для клиента
     */
    public ExceptionLogStatus (String stackTrace,
                                   String errorMsg,
                                   int clientCode,
                                   String clientMsg) {
        super(errorMsg);
        this.timeErr = LocalDateTime.now();
        this.msgError = errorMsg;
        this.clientCode = clientCode;
        this.clientMsg = clientMsg;
        this.stackTrace = stackTrace;
    }
}
