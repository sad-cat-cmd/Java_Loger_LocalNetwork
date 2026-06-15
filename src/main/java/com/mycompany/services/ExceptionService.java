package com.mycompany.services;

import java.time.LocalDateTime;

/**
 * Кастомное исключение для ошибок сервисного слоя.
 * 
 * <p>Содержит подробную информацию об ошибке, возникшей в сервисах:
 * <ul>
 *   <li>Временную метку возникновения ошибки</li>
 *   <li>Текст ошибки для логирования</li>
 *   <li>HTTP код и сообщение для ответа клиенту</li>
 *   <li>Пользовательскую трассировку стека вызовов</li>
 * </ul>
 * </p>
 * 
 * <h2>Структура сообщения об ошибке:</h2>
 * <pre>
 * Failed to service work:
 * 	time: 2024-01-15T10:30:45.123
 * 	stack trace: ProcessService.Add()->ProcessRepository.save()
 * 	Error: Process with id 'proc_123' not found
 * </pre>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try {
 *     Process process = processRepository.findById(id);
 *     if (process == null) {
 *         throw new ExceptionService(
 *             "ProcessService.Search()",
 *             "Process not found: " + id,
 *             404,
 *             "Requested process does not exist"
 *         );
 *     }
 * } catch (ExceptionService e) {
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
 *   <li>404 - Not Found (ресурс не найден)</li>
 *   <li>409 - Conflict (конфликт данных)</li>
 *   <li>500 - Internal Server Error (внутренняя ошибка сервера)</li>
 * </ul>
 * 
 * @author admin_
 * @version 1.0
 * @see ProcessService
 * @see DataLogService
 */
public class ExceptionService extends Exception {
    
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
     * Создает новое исключение сервисного слоя.
     * 
     * <p>Конструктор автоматически фиксирует время возникновения ошибки.</p>
     * 
     * @param stackTrace начальная единица трассировки стека
     * @param errorMsg   текст ошибки для логирования
     * @param clientCode HTTP код для ответа клиенту
     * @param clientMsg  сообщение для отправки клиенту
     */
    public ExceptionService(String stackTrace,
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