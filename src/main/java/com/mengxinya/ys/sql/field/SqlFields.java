package com.mengxinya.ys.sql.field;

import com.mengxinya.ys.sql.SqlUtils;
import com.mengxinya.ys.sql.repository.DataRepository;

public class SqlFields {
    public static SqlField of(Class<?> tClass, String fieldStr) {
        return () -> tClass.getSimpleName() + "." + SqlUtils.toUnderlineCase(fieldStr);
    }

    public static SqlField named(SqlField field, String asName) {
        return () -> "(" + field.toSql() + ")" + " as " + asName;
    }

    public static SqlField count(SqlField field) {
        return () -> "count(" + field.toSql() + ")";
    }

    public static SqlField count() {
        return () -> "count(1)";
    }

    public static SqlField of(DataRepository<?> repository, String field) {
        return () -> repository.getName() + "." + field;
    }
}
