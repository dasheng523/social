package com.mengxinya.ys.sql.field;

public interface EntitySqlField<M, T> extends SqlField<T>{
    T getFieldVal(M m);
}
