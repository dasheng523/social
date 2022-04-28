package com.mengxinya.ys.sql.repository;

import com.mengxinya.ys.sql.ClassUtils;
import com.mengxinya.ys.sql.DataRepositoryException;
import com.mengxinya.ys.sql.RowStuffer;
import com.mengxinya.ys.sql.SqlUtils;
import com.mengxinya.ys.sql.condition.ParamsCondition;
import com.mengxinya.ys.sql.condition.SqlCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.StringUtils;

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


    private Statement selectStatement;
    private Statement fromStatement;
    private Statement whereStatement;
    private Statement groupByStatement;
    private Statement havingStatement;
    private Statement orderByStatement;
    private Statement limitStatement;
    private Statement joinStatement;

    private final String name = SqlUtils.shortUuid();
    private final List<String> fieldNames;

    public DefaultSqlDataRepository(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        this.fromStatement = () -> StringUtils.uncapitalize(mappedClass.getSimpleName()) + " as " + mappedClass.getSimpleName();
        this.fieldNames = Arrays.stream(mappedClass.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
        this.selectStatement = () -> this.fieldNames.stream().map(name -> SqlUtils.toUnderlineCase(name) + " as " + name).collect(Collectors.joining(", "));
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
    public String getName() {
        return name;
    }

    public void addWhere(SqlCondition condition) {
        if (this.whereStatement == null) {
            this.whereStatement = condition::toSql;
        }
        else {
            String sql = whereStatement.toSql();
            this.whereStatement = () -> (sql + " and " + condition.toSql());
        }
    }

    public void addWhere(ParamsCondition condition) {
        this.params.putAll(condition.getParams());
        addWhere((SqlCondition)condition);
    }

    @Override
    public List<String> getFieldNames() {
        return this.fieldNames;
    }
}
