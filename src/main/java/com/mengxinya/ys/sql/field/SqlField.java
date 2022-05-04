package com.mengxinya.ys.sql.field;

public interface SqlField<T> {
    String toSql();

    String toSql(String prefix);

    Class<T> getFieldType();

    String getFieldName();
}
