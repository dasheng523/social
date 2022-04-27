package com.mengxinya.ys.sql.repository;

import com.mengxinya.ys.sql.ResultItem;
import com.mengxinya.ys.sql.RowStuffer;
import com.mengxinya.ys.sql.SqlUtils;
import com.mengxinya.ys.sql.condition.ParamsCondition;
import com.mengxinya.ys.sql.condition.SqlCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.*;
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

    public DefaultSqlDataRepository(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        this.fromStatement = () -> StringUtils.uncapitalize(mappedClass.getSimpleName()) + " as " + mappedClass.getSimpleName();
    }




    @Override
    public Map<String, Object> getParams() {
        return this.params;
    }

    @Override
    public RowStuffer<T> getRowStuffer() {
        return new RowStuffer<>() {
            private final Map<String, PropertyDescriptor> mappedFields;

            {
                this.mappedFields = new HashMap<>();

                for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(mappedClass)) {
                    if (pd.getWriteMethod() != null) {
                        String lowerCaseName = lowerCaseName(pd.getName());
                        this.mappedFields.put(lowerCaseName, pd);
                        String underscoreName = underscoreName(pd.getName());
                        if (!lowerCaseName.equals(underscoreName)) {
                            this.mappedFields.put(underscoreName, pd);
                        }
                    }
                }
            }

            private String lowerCaseName(String name) {
                return name.toLowerCase(Locale.US);
            }
            private String underscoreName(String name) {
                if (!StringUtils.hasLength(name)) {
                    return "";
                }

                StringBuilder result = new StringBuilder();
                result.append(Character.toLowerCase(name.charAt(0)));
                for (int i = 1; i < name.length(); i++) {
                    char c = name.charAt(i);
                    if (Character.isUpperCase(c)) {
                        result.append('_').append(Character.toLowerCase(c));
                    }
                    else {
                        result.append(c);
                    }
                }
                return result.toString();
            }


            @Override
            public T fillRow(ResultItem rs) {
                BeanWrapperImpl bw = new BeanWrapperImpl();
                ConversionService cs = DefaultConversionService.getSharedInstance();
                bw.setConversionService(cs);

                T mappedObject = BeanUtils.instantiateClass(mappedClass);
                bw.setBeanInstance(mappedObject);

                int columnCount = mappedClass.getDeclaredFields().length;

                for (int index = 1; index <= columnCount; index++) {
                    String field = rs.currentColumnName();
                    PropertyDescriptor pd = this.mappedFields.get(field);
                    if (pd != null) {
                        try {
                            Object value = rs.currentColumnValue(pd.getPropertyType());
                            bw.setPropertyValue(pd.getName(), value);
                        }
                        catch (NotWritablePropertyException ex) {
                            throw new DataRetrievalFailureException(
                                    "Unable to map column '" + field + "' to property '" + pd.getName() + "'", ex);
                        }
                    }
                    rs.nextColumn();
                }
                return mappedObject;
            }
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
        return Arrays.stream(mappedClass.getDeclaredFields()).map(field -> SqlUtils.toUnderlineCase(field.getName())).collect(Collectors.toList());
    }
}
