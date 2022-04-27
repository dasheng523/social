package com.mengxinya.ys.sql.field;

public interface SqlField<T> {
    String toSql();

    Class<T> getFieldType();

    String getFieldName();
}
