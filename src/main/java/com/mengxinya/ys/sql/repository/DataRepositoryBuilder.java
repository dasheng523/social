package com.mengxinya.ys.sql.repository;

import com.mengxinya.ys.sql.condition.ParamsCondition;

public class DataRepositoryBuilder<T> {
    private final DefaultSqlDataRepository<T> dataRepository;

    private DataRepositoryBuilder(DefaultSqlDataRepository<T> dataRepository) {
        this.dataRepository = dataRepository;
    }

    public static <T> DataRepositoryBuilder<T> from(Class<T> mappedClass) {
        return new DataRepositoryBuilder<>(new DefaultSqlDataRepository<>(mappedClass));
    }

    public DataRepositoryBuilder<T> where(ParamsCondition condition) {
        dataRepository.getParams().putAll(condition.getParams());
        dataRepository.getSqlQueryBuilder().addWhere(condition);
        return this;
    }

    public DefaultSqlDataRepository<T> build() {
        return this.dataRepository;
    }
}
