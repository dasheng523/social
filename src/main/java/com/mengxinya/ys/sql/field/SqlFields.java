package com.mengxinya.ys.sql.field;

import com.mengxinya.ys.sql.DataRepositoryException;
import com.mengxinya.ys.sql.SqlUtils;
import com.mengxinya.ys.sql.repository.DataRepository;

import java.lang.reflect.InvocationTargetException;

public class SqlFields {
    public static <T> SqlField<T> of(Class<?> tClass, String fieldStr, Class<T> fieldType) {
        return new SqlField<>() {
            @Override
            public String toSql() {
                return tClass.getSimpleName() + "." + SqlUtils.toUnderlineCase(fieldStr);
            }

            @Override
            public Class<T> getFieldType() {
                return fieldType;
            }

            @Override
            public String getFieldName() {
                return fieldStr;
            }
        };
    }

    public static <T> SqlField<T> named(SqlField<T> field, String asName) {
        return new SqlField<>() {
            @Override
            public String toSql() {
                return "(" + field.toSql() + ")" + " as " + asName;
            }

            @Override
            public Class<T> getFieldType() {
                return field.getFieldType();
            }

            @Override
            public String getFieldName() {
                return asName;
            }
        };
    }

    public static <T> SqlField<T> count(SqlField<T> field) {
        return new SqlField<>() {
            @Override
            public String toSql() {
                return "count(" + field.toSql() + ")";
            }

            @Override
            public Class<T> getFieldType() {
                return field.getFieldType();
            }

            @Override
            public String getFieldName() {
                return field.getFieldName();
            }
        };
    }

    public static SqlField<Long> count() {
        return new SqlField<>() {
            @Override
            public String toSql() {
                return "count(1)";
            }

            @Override
            public Class<Long> getFieldType() {
                return Long.class;
            }

            @Override
            public String getFieldName() {
                return "count" + SqlUtils.shortUuid();
            }
        };
    }

    public static <M, T> EntitySqlField<M, T> of(DataRepository<M> repository, String field, Class<T> fieldType) {
        return new EntitySqlField<>() {
            @Override
            public T getFieldVal(M m) {
                try {
                    Object val = m.getClass().getDeclaredMethod("get" + SqlUtils.captureName(field)).invoke(m);
                    return fieldType.cast(val);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new DataRepositoryException("读取字段出问题", e);
                }
            }

            @Override
            public String toSql() {
                return repository.getName() + "." + SqlUtils.toUnderlineCase(field);
            }

            @Override
            public Class<T> getFieldType() {
                return fieldType;
            }

            @Override
            public String getFieldName() {
                return field;
            }
        };
    }
}
