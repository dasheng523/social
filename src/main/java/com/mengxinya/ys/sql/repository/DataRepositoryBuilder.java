package com.mengxinya.ys.sql.repository;

import com.mengxinya.ys.sql.condition.SqlCondition;

public class DataRepositoryBuilder<T> {
    private final DefaultSqlDataRepository<T> dataRepository;

    private DataRepositoryBuilder(DefaultSqlDataRepository<T> dataRepository) {
        this.dataRepository = dataRepository;
    }

    public static <T> DataRepositoryBuilder<T> from(Class<T> mappedClass) {
        return new DataRepositoryBuilder<>(new DefaultSqlDataRepository<>(mappedClass));
    }

    public DataRepositoryBuilder<T> where(SqlCondition condition) {
        dataRepository.addWhere(condition);
        return this;
    }

    public DefaultSqlDataRepository<T> build() {
        return this.dataRepository;
    }
}
