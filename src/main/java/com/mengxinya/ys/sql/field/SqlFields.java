package com.mengxinya.ys.sql.field;

import com.mengxinya.ys.sql.ClassUtils;
import com.mengxinya.ys.sql.DataRepositoryException;
import com.mengxinya.ys.sql.SqlUtils;
import com.mengxinya.ys.sql.repository.DataRepository;

public class SqlFields {
    public static <T> SqlField<T> of(Class<?> tClass, String fieldStr, Class<T> fieldType) {
        try {
            tClass.getDeclaredField(fieldStr);
            return new SqlField<>() {
                @Override
                public String toSql() {
                    return fieldStr;
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
        } catch (NoSuchFieldException e) {
            throw new DataRepositoryException("字段不存在", e);
        }
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
        String name = "count_" + SqlUtils.shortUuid();
        return new SqlField<>() {
            @Override
            public String toSql() {
                return "count(1) as " + name;
            }

            @Override
            public Class<Long> getFieldType() {
                return Long.class;
            }

            @Override
            public String getFieldName() {
                return name;
            }
        };
    }

    public static <M, T> EntitySqlField<M, T> of(DataRepository<M> repository, String field, Class<T> fieldType) {
        if (repository.getFieldNames().stream().noneMatch(item -> item.endsWith(field))) {
            throw new DataRepositoryException("字段不存在");
        }
        return new EntitySqlField<>() {
            @Override
            public T getFieldVal(M m) {
                return fieldType.cast(ClassUtils.getObjFieldVal(m, field));
            }
            @Override
            public String toSql() {
                return repository.getName() + "." + field;
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
