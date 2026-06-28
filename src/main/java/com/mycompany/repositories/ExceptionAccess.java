package com.mycompany.repositories;

/**
 * Исключение для ошибок, связанных с неккоректным кодом доступа к данным
 * 
 * <p>Предназначено для информирования системы о ситуациях, когда:
 * <ul>
 *   <li></li>
 * </ul>
 * </p>
 * 
 * <p>Особенности:</p>
 * <ul>
 *   <li>Содержит HTTP код для ответа клиенту</li>
 *   <li>Содержит сообщение для отправки клиенту</li>
 *   <li>Используется в репозиториях для сигнализации об отсутсвии доступа</li>
 * </ul>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try {
 *     Process process = dbManager.selectProcessById(id);
 *      if (!foundProcess.getUniqueCode().equals(uniqueCode)){
           throw new ExceptionAccess();
        }
 *     return process;
 * } catch (ExceptionDB e) {
 *     throw new RuntimeException("Database error", e);
 * }
 * </pre>
 * 
 * @author admin_
 * @version 1.0
 * @see ProcessRepository
 * @see DataLogRepository
 */
public class ExceptionAccess extends Exception {
    /** HTTP код для ответа клиенту */
    private final int clientCode;
    
    /** Сообщение для отправки клиенту */
    private final String clientMsg;
    
    /**
     * Создает новое исключение c кодом: 401 и собщением: "Invalid secure code"
     */
    public ExceptionAccess() {
        super("");
        this.clientCode = 401;
        this.clientMsg = "Invalid secure code";
    }
    
    /**
     * Возвращает HTTP код ошибки для отправки клиенту.
     * 
     * @return HTTP статус код (например, 404)
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
}
