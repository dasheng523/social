package com.mengxinya.ys.sql.repository;

import java.util.Map;

public record RelateOneResult<Main>(Main main, Map<String, Object> dataMap) {
    public <T> T getOne(DataSqlRepository<T> repository) {
        // 这里的警告怎么解决呢？
        return (T)dataMap.get(repository.getName());
    }
}
