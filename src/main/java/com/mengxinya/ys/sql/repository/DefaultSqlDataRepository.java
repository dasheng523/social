package com.mengxinya.ys.sql.repository;

import com.mengxinya.ys.sql.ClassUtils;
import com.mengxinya.ys.sql.DataRepositoryException;
import com.mengxinya.ys.sql.RowStuffer;
import com.mengxinya.ys.sql.SqlUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class DefaultSqlDataRepository<T> implements DataSqlRepository<T> {
    private final Class<T> mappedClass;

    private Map<String, Object> params = new HashMap<>();


    private SqlQueryBuilder queryBuilder;

    private final String name = SqlUtils.shortUuid();
    private final List<String> fieldNames;

    public DefaultSqlDataRepository(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        this.fieldNames = Arrays.stream(mappedClass.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
        this.queryBuilder = SqlQueryBuilder.from(mappedClass);
        this.queryBuilder.setSelectStatement(() -> this.fieldNames.stream().map(name -> SqlUtils.toUnderlineCase(name) + " as " + name).collect(Collectors.joining(", ")));
    }


    @Override
    public Map<String, Object> getParams() {
        return this.params;
    }

    @Override
    public RowStuffer<T> getRowStuffer() {
        return rs -> {
            int columnCount = mappedClass.getDeclaredFields().length;

            Map<String, Object> dataMap = new HashMap<>();
            for (int index = 1; index <= columnCount; index++) {
                try {
                    String field = rs.currentColumnName();
                    dataMap.put(field, rs.currentColumnValue(mappedClass.getDeclaredField(field).getType()));
                    rs.nextColumn();
                } catch (NoSuchFieldException e) {
                    throw new DataRepositoryException("字段不存在", e);
                }
            }

            return ClassUtils.initObject(mappedClass, dataMap);
        };
    }

    @Override
    public SqlQueryBuilder getSqlQueryBuilder() {
        return this.queryBuilder;
    }

    @Override
    public String getName() {
        return name;
    }


    @Override
    public List<String> getFieldNames() {
        return this.fieldNames;
    }
}
