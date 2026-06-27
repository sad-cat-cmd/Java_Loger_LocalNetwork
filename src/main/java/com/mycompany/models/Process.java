package com.mycompany.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель процесса логирования.
 * 
 * <p>Представляет клиента (приложение, сервис, микросервис), который отправляет логи в систему.
 * Каждый процесс имеет уникальный идентификатор, имя, владельца и собственный счетчик логов.</p>
 * 
 * <h2>Жизненный цикл процесса:</h2>
 * <ul>
 *   <li>{@code Active} - процесс активен и может отправлять логи</li>
 *   <li>{@code Finished} - процесс завершен, логи больше не принимаются</li>
 * </ul>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * // Создание нового процесса
 * try {
 *     Process process = new Process("billing-service", "admin@company.com", 100, 50);
 *     
 *     // увеличение счетчика
 *     process.incrementLogCount();
 *     
 *     // Завершение процесса
 *     process.setStatusFinished();
 *     
 *     // Запись в БД
 *     System.out.println(process.getTimeExecution()); // "5 minutes 30 seconds"
 * } catch (ExceptionProcess e) {
 *     e.addProgramUnitInTheStackTrace("NameMethod()")
 *     System.err.println(e.getCombinedLogMsg());
 * }
 * 
 * // Загрузка существующего процесса из БД
 * Process existingProcess = new Process(
 *     "proc_123", "billing-service", "admin@company.com", "Active",
 *     "unique_code_456", LocalDateTime.now().minusDays(1), null, 1500
 * );
 * </pre>
 * 
 * @author admin_
 * @version 1.0
 * @see ExceptionProcess
 */
public class Process {
    
    /** Статус: процесс активен */
    public static final String STATUS_ACTIVE = "Active";
    
    /** Статус: процесс завершен */
    public static final String STATUS_FINISHED = "Finished";

    private final int maxLengthName;
    private final int maxLengthOwner;
    private final String ID;
    private final String name;
    private final String owner;
    private final LocalDateTime timeCreate;
    private final String uniqueCode;
    private String status;
    private LocalDateTime timeEndWork;
    private long logCount;

    /**
     * Проверяет корректность входных данных.
     * 
     * <p>Выполняет валидацию:
     * <ul>
     *   <li>Имя не должно быть null</li>
     *   <li>Имя не должно быть empty</li>
     *   <li>Длина имени не должна превышать maxLengthName</li>
     *   <li>Владелец не должен быть null</li>
     *   <li>Владелец не должен быть empty</li>
     *   <li>Длина владельца не должна превышать maxLengthOwner</li>
     * </ul>
     * </p>
     * 
     * @throws ExceptionProcess если валидация не пройдена
     */
    private void verificationInputMsgStrings() throws ExceptionProcess {
        String errName = "";
        String errOwner = "";

        if (name == null) {
            errName = "name is null";
        } else if (name.isEmpty()){
            errName = "name is empty";
        } else if (name.length() > maxLengthName) {
            errName = "length name > " + maxLengthName;
        }
        if (owner == null) {
            errOwner = "owner is null";
        } else if (owner.isEmpty()) {
            errOwner = "owner is empty";
        } else if (owner.length() > maxLengthOwner) {
            errOwner = "length owner > " + maxLengthOwner;
        }
        if (!errName.isEmpty() || !errOwner.isEmpty()) {
            throw new ExceptionProcess(errName, errOwner,
                "Process.verificationInputMsgStrings()", 400, "Invalid request data");
        }
    }

    /**
     * Генерирует уникальный идентификатор.
     * 
     * @return случайный UUID
     */
    private String generateId() {
        return UUID.randomUUID().toString();
    }

    // ========== Геттеры ==========
    
    /**
     * Возвращает уникальный идентификатор процесса.
     * 
     * @return ID процесса
     */
    public String getID() {
        return ID;
    }
    
    /**
     * Возвращает имя процесса.
     * 
     * @return имя процесса
     */
    public String getName() {
        return name;
    }
    
    /**
     * Возвращает владельца процесса.
     * 
     * @return владелец процесса
     */
    public String getOwner() {
        return owner;
    }
    
    /**
     * Возвращает текущий статус процесса.
     * 
     * @return статус (Active или Finished)
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Возвращает уникальный код процесса.
     * 
     * @return уникальный код
     */
    public String getUniqueCode() {
        return uniqueCode;
    }
    
    /**
     * Возвращает время создания процесса в виде строки.
     * 
     * @return строка с датой и временем создания
     */
    public String getTimeCreate() {
        return timeCreate.toString();
    }
    
    /**
     * Возвращает время окончания работы процесса.
     * 
     * @return строка с датой и временем окончания, или null если процесс активен
     */
    public String getTimeEndWork() {
        if (timeEndWork != null) {
            return timeEndWork.toString();
        } else {
            return null;
        }
    }
    
    /**
     * Возвращает время выполнения процесса в удобочитаемом формате.
     * 
     * <p>Автоматически выбирает единицы измерения:
     * секунды, минуты, часы, дни, месяцы, годы.</p>
     * 
     * <p>Примеры:</p>
     * <ul>
     *   <li>5 seconds</li>
     *   <li>2 minutes 30 seconds</li>
     *   <li>3 hours 15 minutes</li>
     *   <li>1 day</li>
     *   <li>2 months 10 days</li>
     *   <li>1 year</li>
     * </ul>
     * 
     * @return отформатированная строка времени выполнения
     */
    public String getTimeExecution() {
        if (timeEndWork != null) {
            return formatDuration(Duration.between(timeCreate, timeEndWork));
        }
        return formatDuration(Duration.between(timeCreate, LocalDateTime.now()));
    }
    
    /**
     * Форматирует длительность в удобочитаемый вид.
     * 
     * @param duration длительность для форматирования
     * @return строка с форматированной длительностью
     */
    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        
        if (seconds < 0) {
            return "0 seconds";
        }
        
        if (seconds < 60) {
            return seconds + " " + (seconds == 1 ? "second" : "seconds");
        }
        
        long minutes = seconds / 60;
        if (minutes < 60) {
            long remainingSeconds = seconds % 60;
            if (remainingSeconds == 0) {
                return minutes + " " + (minutes == 1 ? "minute" : "minutes");
            } else {
                return minutes + " " + (minutes == 1 ? "minute" : "minutes") + " " + 
                    remainingSeconds + " " + (remainingSeconds == 1 ? "second" : "seconds");
            }
        }
        
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        if (hours < 24) {
            if (remainingMinutes == 0) {
                return hours + " " + (hours == 1 ? "hour" : "hours");
            } else {
                return hours + " " + (hours == 1 ? "hour" : "hours") + " " + 
                    remainingMinutes + " " + (remainingMinutes == 1 ? "minute" : "minutes");
            }
        }
        
        long days = hours / 24;
        long remainingHours = hours % 24;
        if (days < 30) {
            if (remainingHours == 0) {
                return days + " " + (days == 1 ? "day" : "days");
            } else {
                return days + " " + (days == 1 ? "day" : "days") + " " + 
                    remainingHours + " " + (remainingHours == 1 ? "hour" : "hours");
            }
        }
        
        long months = days / 30;
        long remainingDays = days % 30;
        if (months < 12) {
            if (remainingDays == 0) {
                return months + " " + (months == 1 ? "month" : "months");
            } else {
                return months + " " + (months == 1 ? "month" : "months") + " " + 
                    remainingDays + " " + (remainingDays == 1 ? "day" : "days");
            }
        }
        
        long years = months / 12;
        long remainingMonths = months % 12;
        if (remainingMonths == 0) {
            return years + " " + (years == 1 ? "year" : "years");
        } else {
            return years + " " + (years == 1 ? "year" : "years") + " " + 
                remainingMonths + " " + (remainingMonths == 1 ? "month" : "months");
        }
    }

    /**
     * Возвращает количество логов, отправленных процессом.
     * 
     * @return количество логов
     */
    public long getLogCount() {
        return logCount;
    }

    // ========== Методы изменения состояния ==========
    
    /**
     * Завершает процесс.
     * 
     * <p>Устанавливает статус {@code Finished} и фиксирует время окончания.</p>
     */
    public void setStatusFinished() {
        status = STATUS_FINISHED;
        timeEndWork = LocalDateTime.now();
    }
    
    /**
     * Увеличивает счетчик логов на 1.
     * 
     * <p>Должен вызываться каждый раз при успешной записи лога.</p>
     */
    public void incrementLogCount() {
        logCount++;
    }

    // ========== Конструкторы ==========
    
    /**
     * Конструктор для создания НОВОГО процесса.
     * 
     * <p>Выполняет валидацию входных данных. При успешном создании
     * процесс получает статус {@code Active} и автоматическую временную метку.</p>
     * 
     * @param name            имя процесса (не может быть null)
     * @param owner           владелец процесса (не может быть null)
     * @param maxLengthName   максимальная длина имени
     * @param maxLengthOwner  максимальная длина владельца
     * @throws ExceptionProcess если валидация не пройдена
     */
    public Process(String name, String owner, int maxLengthName, int maxLengthOwner) 
            throws ExceptionProcess {
        this.ID = generateId();
        this.uniqueCode = generateId();
        this.name = name;
        this.owner = owner;
        this.status = STATUS_ACTIVE;
        this.timeCreate = LocalDateTime.now();
        this.timeEndWork = null;
        this.logCount = 0;
        this.maxLengthName = maxLengthName;
        this.maxLengthOwner = maxLengthOwner;

        try {
            verificationInputMsgStrings();
        } catch (ExceptionProcess exc) {
            exc.addProgramUnitInTheStackTrace("Process.Process()");
            throw exc;
        }
    }
    
    /**
     * Конструктор для ЗАГРУЗКИ процесса ИЗ БАЗЫ ДАННЫХ.
     * 
     * <p>Не выполняет валидацию, так как данные уже были проверены при создании.
     * Поля maxLengthName и maxLengthOwner устанавливаются в 0 (не используются).</p>
     * 
     * @param ID           идентификатор процесса из БД
     * @param name         имя процесса
     * @param owner        владелец процесса
     * @param status       статус процесса (Active/Finished)
     * @param uniqueCode   уникальный код процесса
     * @param createTime   время создания процесса
     * @param timeEndWork  время окончания работы (может быть null)
     * @param countLog     количество логов
     */
    public Process(String ID, String name, String owner, String status,
                   String uniqueCode, LocalDateTime createTime,
                   LocalDateTime timeEndWork, long countLog) {
        this.ID = ID;
        this.name = name;
        this.owner = owner;
        this.status = status;
        this.timeEndWork = timeEndWork;
        this.uniqueCode = uniqueCode;
        this.timeCreate = createTime;
        this.logCount = countLog;
        this.maxLengthName = 0;
        this.maxLengthOwner = 0;
    }
    
    /**
     * Проверяет, активен ли процесс.
     * 
     * @return true если статус Active
     */
    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }
    
    /**
     * Проверяет, завершен ли процесс.
     * 
     * @return true если статус Finished
     */
    public boolean isFinished() {
        return STATUS_FINISHED.equals(status);
    }
}