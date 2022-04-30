package com.mengxinya.ys.sql.repository;

import java.util.Map;
import java.util.Set;

public record RelateManyResult<Main>(Main main, Map<String, Set<Object>> dataMap) {
    public <T> Set<T> getMany(DataSqlRepository<T> repository) {
        // 这里的警告怎么解决呢？
        return (Set<T>)dataMap.get(repository.getName());
    }
}
