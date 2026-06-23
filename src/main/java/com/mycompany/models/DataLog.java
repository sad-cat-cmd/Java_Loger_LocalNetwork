/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.models;

import java.time.LocalDateTime;

/**
 * Модель данных для хранения записи лога.
 * 
 * <p>Представляет собой неизменяемый объект лога с валидацией полей.
 * Автоматически устанавливает временную метку при создании.</p>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try {
 *     DataLog log = new DataLog("User logged in", "INFO", 300);
 *     System.out.println(log.getLogInfo());
 * } catch (ExceptionDataLog e) {
 *     e.addProgramUnitInTheStackTrace("NameMethod()")
 *     System.err.println("Validation failed: " + e.getCombindLogMsg());
 * }
 * </pre>
 * 
 * <h2>Правила валидации:</h2>
 * <ul>
 *   <li>Длина сообщения не должна превышать параметра maxLengthLog, котороые вы передаете в конструктор</li>
 *   <li>Статус должен быть одним из: FATAL, INFO, WARN, DEBUG, TRACE</li>
 *   <li>
 * </ul>
 * 
 * @author admin_
 * @version 1.0
 * @see ExceptionDataLog
 */
public class DataLog {
    /** Допустимые статусы логирования */
    private static final String[] CORRECTNESS_STATUSES = {"FATAL", "INFO", "WARN", "DEBUG", "TRACE"};
    
    /** Максимальная длина сообщения лога */
    private final int maxLengthLog;
    /** Текст сообщения лога */
    private final String strLogInfo;
    
    /** Статус лога (FATAL, INFO, WARN, DEBUG, TRACE) */
    private final String strStatus;
    
    /** Порядковый номер лога */ 
    private final int numberLog;

    /** Временная метка создания лога */
    private final LocalDateTime timeLog;
    
    /**
     * Проверяет корректность полей лога.
     * 
     * <p>Выполняет следующие проверки:</p>
     * <ul>
     *   <li>Сообщение не является null</li>
     *   <li>Сообщение не является empty</li>
     *   <li>Длина сообщения не превышает параметра maxLengthLog, задающего максимальное количество символов</li>
     *   <li>Статус не является null</li>
     *   <li>Статус не является empty</li>
     *   <li>Статус присутствует в списке допустимых значений</li>
     * </ul>
     * 
     * @throws ExceptionDataLog если валидация не пройдена
     */
    private void verificationInputMsgStrings() throws ExceptionDataLog {
        String errStatus = "";
        String errInfo = "";
        boolean flagFindStatus = false;
        
        if (strLogInfo == null) {
            errInfo = "log info is null";
        } else if (strLogInfo.isEmpty()){
            errInfo = "log info is empty";
        } else if (strLogInfo.length() > maxLengthLog) {
            errInfo = "length info > " + maxLengthLog;
        }
        
        if (strStatus == null || strStatus.isEmpty()) {
            if (strStatus == null){
                errStatus = "strStatus is null";
            }
            else {
                errStatus = "strStaus is empty";
            }
        } else {
            for (String validStatus : CORRECTNESS_STATUSES) {
                if (strStatus.equals(validStatus)) {
                    flagFindStatus = true;
                    break;
                }
            }
        
            if (!flagFindStatus) {
                errStatus = "Status has been not correctness";
            }
        }
        
        if (!errStatus.isEmpty() || !errInfo.isEmpty()) {
            throw new ExceptionDataLog(errInfo,
                                       errStatus,
                                       "DataLog.verificationInputMsgStrings()",
                                       400,
                                       "Invalid request data");
        }
    }
    
    // ========== Геттеры ==========

    /**
     * Возвращает текст сообщения лога.
     * 
     * @return текст сообщения
     */
    public String getLogInfo() {
        return strLogInfo;
    }
    
    /**
     * Возвращает номер лога.
     * 
     * @return текст сообщения
     */
    public int getNumberLog() {
        return numberLog;
    }
    /**
     * Возвращает статус лога.
     * 
     * @return статус (FATAL, INFO, WARN, DEBUG, TRACE)
     */
    public String getStatus() {
        return strStatus;
    }
    
    /**
     * Возвращает временную метку в виде строки.
     * 
     * @return строка с датой и временем в формате ISO-8601
     */
    public String getTimeLog() {
        return timeLog.toString();
    }
    
    /**
     * Возвращает временную метку как объект LocalDateTime.
     * 
     * @return объект {@link LocalDateTime} времени создания лога
     */
    public LocalDateTime getTimeLogAsObject() {
        return timeLog;
    }
    
    // ========== Конструкторы ==========

    /**
     * Конструктор для создания НОВОГО объекта лога с автоматической установкой времени.
     * <p>Выполняет валидацию входных данных. При успешном создании лог получает временную метку</p>
     * @param info  текст сообщения лога (не должен превышать параметра maxLengthLog, задающего максимальное количество символов в логе)
     * @param status статус лога (должен быть из списка допустимых)
     * @param maxLengthLog максимальная длина сообщения лога
     * @throws ExceptionDataLog если валидация не пройдена
     */
    public DataLog(String info,
                   String status,
                   int maxLengthLog) throws ExceptionDataLog {
        this.numberLog = 0;
        this.strLogInfo = info;
        this.strStatus = status;
        this.timeLog = LocalDateTime.now();
        this.maxLengthLog = maxLengthLog;
        try {
            verificationInputMsgStrings();
        } catch (ExceptionDataLog exc) {
            exc.addProgramUnitInTheStackTrace("DataLog.DataLog()");
            throw exc;
        }
    }
    /**
     * Конструктор для получения объекта лога из базы данных
     * @param info  текст сообщения лога (не должен превышать параметра maxLengthLog, задающего максимальное количество символов в логе)
     * @param status статус лога (должен быть из списка допустимых)
     * @param maxLengthLog максимальная длина сообщения лога
     * @param numberLog порядкоый номер данного лога
     * @throws ExceptionDataLog если валидация не пройдена
     */
    public DataLog(String info,
                    String status,
                    int numberLog,
                    LocalDateTime timeLog) {
        this.numberLog = numberLog;
        this.strLogInfo = info;
        this.strStatus = status;
        this.timeLog = timeLog;
        this.maxLengthLog = 0;
    }
}