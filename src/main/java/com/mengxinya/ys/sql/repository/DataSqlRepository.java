package com.mengxinya.ys.sql.repository;

public interface DataSqlRepository<T> extends DataRepository<T>{
    default String toSql() {
        return getSqlQueryBuilder().toSql();
    }
}
