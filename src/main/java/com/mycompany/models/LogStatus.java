package com.mycompany.models;

/**
 * Обертка для статуса лога с валидацией.
 * 
 * <p>Обеспечивает безопасное создание статусов лога,
 * проверяя, что статус входит в допустимый набор значений.</p>
 * 
 * <h2>Допустимые статусы:</h2>
 * <ul>
 *   <li>{@code FATAL} - фатальная ошибка, требующая немедленного внимания</li>
 *   <li>{@code INFO} - информационное сообщение о нормальной работе</li>
 *   <li>{@code WARN} - предупреждение о потенциальной проблеме</li>
 *   <li>{@code DEBUG} - отладочное сообщение для разработчиков</li>
 *   <li>{@code TRACE} - детальная трассировка выполнения</li>
 * </ul>
 * 
 * <h2>Правила валидации:</h2>
 * <ul>
 *   <li>Статус не может быть {@code null}</li>
 *   <li>Статус не может быть пустой строкой</li>
 *   <li>Статус чувствителен к регистру (только заглавные буквы)</li>
 *   <li>Статус должен входить в список допустимых значений</li>
 * </ul>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try {
 *     // Создание с валидацией
 *     LogStatus status = new LogStatus("INFO");
 *     System.out.println(status.getStatus()); // INFO
 * } catch (ExceptionLogStatus e) {
 *     System.err.println(e.getCombinedLogMsg());
 * }
 * </pre>
 * 
 * @author admin_
 * @version 1.0
 * @see ExceptionLogStatus
 */
public class LogStatus {
    
    /** Допустимые статусы логирования */
    private static final String[] CORRECTNESS_STATUSES = {"FATAL", "INFO", "WARN", "DEBUG", "TRACE"};
    
    /** Значение статуса */
    private final String status;

    /**
     * Возвращает строковое представление статуса.
     * 
     * @return значение статуса (FATAL, INFO, WARN, DEBUG, TRACE)
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Проверяет корректность статуса.
     * 
     * <p>Выполняет следующие проверки:</p>
     * <ul>
     *   <li>Статус не {@code null}</li>
     *   <li>Статус не пустая строка</li>
     *   <li>Статус входит в список допустимых значений</li>
     * </ul>
     * 
     * @throws ExceptionLogStatus если статус не прошел валидацию
     */
    private void verificationInputStatus() throws ExceptionLogStatus {
        String errMsg = "";
        boolean flagFoundStatus = false;
        
        // Проверка на null
        if (status == null) {
            errMsg = "status is null";
        } 
        // Проверка на empty
        else if (status.isEmpty()) {
            errMsg = "status is empty";
        }
        
        // Если есть ошибка - бросаем исключение
        if (!errMsg.isEmpty()) {
            throw new ExceptionLogStatus(
                "LogStatus.verificationInputStatus()",
                errMsg,
                400,  // 400 Bad Request
                "Invalid request data"
            );
        }
        
        // Проверка на допустимые значения
        for (String validStatus : CORRECTNESS_STATUSES) {
            if (status.equals(validStatus)) {
                flagFoundStatus = true;
                break;
            }
        }
        
        if (!flagFoundStatus) {
            errMsg = "Status has been not correctness";
            throw new ExceptionLogStatus(
                "LogStatus.verificationInputStatus()",
                errMsg,
                400,  // 400 Bad Request
                "Invalid request data"
            );
        }
    }
    
    /**
     * Создает новый объект статуса с валидацией.
     * 
     * @param status статус лога (должен быть из списка допустимых)
     * @throws ExceptionLogStatus если статус невалидный
     */
    public LogStatus(String status) throws ExceptionLogStatus {
        this.status = status;
        try {
            verificationInputStatus();
        } catch (ExceptionLogStatus exc) {
            exc.addProgramUnitInTheStackTrace("LogStatus.LogStatus()");
            throw exc;
        }
    }
}