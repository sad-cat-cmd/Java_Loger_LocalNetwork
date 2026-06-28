package com.mycompany.repositories;

/**
 * Исключение для ошибок, связанных с отсутствием сущностей в репозиториях.
 * 
 * <p>Предназначено для информирования системы о ситуациях, когда:
 * <ul>
 *   <li>Запрашиваемая сущность не найдена в базе данных</li>
 *   <li>Поиск по ID или имени не дал результатов</li>
 *   <li>Операция обновления/удаления не найдена сущность</li>
 * </ul>
 * </p>
 * 
 * <p>Особенности:</p>
 * <ul>
 *   <li>Содержит HTTP код для ответа клиенту</li>
 *   <li>Содержит сообщение для отправки клиенту</li>
 *   <li>Используется в репозиториях для сигнализации об отсутствии данных</li>
 * </ul>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try {
 *     Process process = dbManager.selectProcessById(id);
 *     if (process == null) {
 *         throw new ExceptionFound(404, "Process not found: " + id);
 *     }
 *     return process;
 * } catch (ExceptionDB e) {
 *     throw new RuntimeException("Database error", e);
 * }
 * </pre>
 * 
 * <h2>HTTP коды ошибок:</h2>
 * <ul>
 *   <li>404 - Not Found (сущность не найдена)</li>
 *   <li>409 - Conflict (конфликт данных)</li>
 * </ul>
 * 
 * @author admin_
 * @version 1.0
 * @see ProcessRepository
 * @see DataLogRepository
 */
public class ExceptionFound extends Exception {
    
    /** HTTP код для ответа клиенту */
    private final int clientCode;
    
    /** Сообщение для отправки клиенту */
    private final String clientMsg;
    
    /**
     * Создает новое исключение с указанным HTTP кодом и сообщением.
     * 
     * @param clientCode HTTP код ошибки (например, 404 для Not Found)
     * @param clientMsg  сообщение для клиента (например, "Process not found")
     */
    public ExceptionFound(int clientCode, String clientMsg) {
        super(clientMsg);
        this.clientCode = clientCode;
        this.clientMsg = clientMsg;
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