package com.mycompany.configs;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Конфигурация сервера логирования.
 * 
 * <p>Загружает настройки из JSON файла и предоставляет доступ к параметрам:
 * <ul>
 *   <li>Путь к файлу базы данных SQLite</li>
 *   <li>Порты для HTTP и TCP серверов</li>
 *   <li>Максимальную длину сообщения лога</li>
 *   <li>Максимальное количество потоков сервера</li>
 *   <li>Максимальное имя процесса логирования</li>
 *   <li>Максимальное имя владельца процесса логирования</li>
 * </ul>
 * </p>
 * 
 * <h2>Пример конфигурационного файла (config.json):</h2>
 * <pre>
 * {
 *     "database": {
 *         "path": "data/logs.db"
 *     },
 *     "server": {
 *         "http_port": 8080,
 *         "tcp_port": 9090,
 *         "max_threads": 50
 *     },
 *     "logging": {
 *         "max_log_length": 200,
 *         "max_name_process_length" : 50,
           "max_owner_process_length" : 50
 *     }
 * }
 * </pre>
 * 
 * <h2>Пример использования:</h2>
 * <pre>
 * try {
 *     Config config = new Config("config.json");
 *     String dbPath = config.getPathDBFile();
 *     int port = config.getHttpPort();
 * } catch (ExceptionConfig e) {
 *     System.err.println(e.getCombinedLogMsg());
 * }
 * </pre>
 * 
 * @author admin_
 * @version 1.0
 * @see ExceptionConfig
 */
public class Config {
    
    /** Путь к файлу конфигурации */
    private String pathConfigFile;
    
    /** Путь к файлу базы данных */
    private String pathDBFile;
    
    /** HTTP порт сервера */
    private int httpPort;
    
    /** TCP порт сервера */
    private int tcpPort;
    
    /** Максимальная длина сообщения лога */
    private int maxLogLength;
    
    /** Максимальное количество потоков */
    private int maxThread;

    /** Максимальная длина имени процесса */
    private int maxNameProcessLength;

    /** Максимальная длиная владельца */
    private int maxOwnerProcessLength;

    /**
     * Внутренний класс для десериализации JSON.
     * Структура должна точно соответствовать файлу конфигурации.
     */
    private static class ConfigJson {
        DatabaseConfig database;
        ServerConfig server;
        LoggingConfig logging;

        static class DatabaseConfig {
            String path;
        }
        
        static class ServerConfig {
            int http_port;
            int tcp_port;
            int max_threads;
        }
        
        static class LoggingConfig {
            int max_log_length;
            int max_name_process_length;
            int max_owner_process_length;
        }
    }
    
    /**
     * Читает и парсит файл конфигурации.
     * 
     * @throws ExceptionConfig при ошибках чтения файла или парсинга JSON
     */
    private void readConfig() throws ExceptionConfig {
        Gson gson = new Gson();
        
        try {
            Reader reader = getConfigReader();
            ConfigJson configJson = gson.fromJson(reader, ConfigJson.class);

            // Валидация обязательных полей
            if (configJson.database == null || configJson.database.path == null) {
                throw new ExceptionConfig("Missing database configuration", "Config.readConfig()");
            }
            if (configJson.server == null) {
                throw new ExceptionConfig("Missing server configuration", "Config.readConfig()");
            }
            if (configJson.logging == null) {
                throw new ExceptionConfig("Missing logging configuration", "Config.readConfig()");
            }


            this.pathDBFile = configJson.database.path;
            this.httpPort = configJson.server.http_port;
            this.tcpPort = configJson.server.tcp_port;
            this.maxLogLength = configJson.logging.max_log_length;
            this.maxThread = configJson.server.max_threads;
            this.maxNameProcessLength = configJson.logging.max_name_process_length;
            this.maxOwnerProcessLength = configJson.logging.max_owner_process_length;
            if (this.maxNameProcessLength <= 0) {
                throw new ExceptionConfig("max_name_process_length must be positive", "Config.readConfig()");
            }
            if (this.maxOwnerProcessLength <= 0) {
                throw new ExceptionConfig("max_owner_process_length must be positive", "Config.readConfig()");
            }
        } catch (IOException excIO) {
            throw new ExceptionConfig("IO Error: " + excIO.getMessage(), "Config.readConfig()");
        } catch (JsonSyntaxException excJSE) {
            throw new ExceptionConfig("JSON Parse Error: " + excJSE.getMessage(), "Config.readConfig()");
        } catch (NullPointerException excNP) {
            throw new ExceptionConfig("Missing required field in config: " + excNP.getMessage(), "Config.readConfig()");
        }
    }
    
    /**
     * Получает Reader для чтения конфигурационного файла.
     * 
     * <p>Сначала пытается загрузить файл из файловой системы,
     * затем из ресурсов classpath.</p>
     * 
     * @return Reader для чтения конфигурации
     * @throws IOException если файл не найден
     */
    private Reader getConfigReader() throws IOException {
        Path ConfigPath = Paths.get(pathConfigFile);
        if (Files.exists(ConfigPath)) {
            return Files.newBufferedReader(ConfigPath);
        }
        
        InputStream inStream = getClass().getClassLoader().getResourceAsStream(pathConfigFile);
        if (inStream == null) {
            throw new IOException("Config file not found: " + pathConfigFile);
        }
        return new java.io.InputStreamReader(inStream);
    }

    /**
     * Возвращает путь к файлу базы данных.
     * 
     * @return путь к DB файлу
     */
    public String getPathDBFile() {
        return pathDBFile;
    }
    
    /**
     * Возвращает HTTP порт сервера.
     * 
     * @return номер HTTP порта
     */
    public int getHttpPort() {
        return httpPort;
    }

    /**
     * Возвращает TCP порт сервера.
     * 
     * @return номер TCP порта
     */
    public int getTcpPort() {
        return tcpPort;
    }

    /**
     * Возвращает максимальную длину сообщения лога.
     * 
     * @return максимальная длина в символах
     */
    public int getMaxLogLength() {
        return maxLogLength;
    }

    /**
     * Возвращает максимальную длину имени процесса.
     * 
     * @return максимальная длина в символах
     */
    public int getMaxNameProcessLength() {
        return maxNameProcessLength;
    }

    /**
     * Возвращает максимальную длину имени хозяина процесса.
     * 
     * @return максимальная длина в символах
     */
    public int getMaxOwnerProcessLength() {
        return maxOwnerProcessLength;
    }
    /**
     * Возвращает максимальное количество потоков сервера.
     * 
     * @return максимальное количество потоков
     */
    public int getMaxThread() {
        return maxThread;
    }
    
    /**
     * Создает новый объект конфигурации.
     * 
     * @param pathCFG путь к файлу конфигурации
     * @throws ExceptionConfig если не удалось загрузить или распарсить конфигурацию
     */
    public Config(String pathCFG) throws ExceptionConfig {
        this.pathConfigFile = pathCFG;
        try {
            readConfig();
        } catch (ExceptionConfig exc) {
            exc.addProgramUnitInTheStackTrace("Config.Config()");
            throw exc;
        }
    }
}