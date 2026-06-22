package com.mycompany.database;

import java.util.List;
import com.mycompany.models.LogStatus;

import com.mycompany.models.LogStatus;
import java.util.List;

/**
 * Утилитарный класс для построения динамических SQL запросов.
 * 
 * @author admin_
 * @version 1.0
 */
public class QueryBuilder {
    
    /**
     * Строит SQL запрос для выборки логов по статусам.
     * 
     * @param statuses список статусов для фильтрации
     * @return готовый SQL запрос с плейсхолдерами
     */
    public static String buildSelectLogsByStatuses(List<LogStatus> statuses) {
    if (statuses == null || statuses.isEmpty()) {
        return CollectionSQLliteRequest.SELECT_ALL_LOGS_BY_PROCESS_ID;
    }
    
    // Создаем StringBuilder для построения строки
    StringBuilder placeholders = new StringBuilder();
    
    for (int i = 0; i < statuses.size(); i++) {
        placeholders.append("?");
        if (i < statuses.size() - 1) {
            placeholders.append(", ");
        }
    }
    
    return String.format(
        CollectionSQLliteRequest.SELECT_LOGS_CONTAINS_STATUSES,
        placeholders.toString()
    );
}
}
