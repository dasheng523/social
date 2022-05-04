package com.mengxinya.ys.sql.repository;

import com.mengxinya.ys.sql.SqlUtils;
import com.mengxinya.ys.sql.condition.ParamsCondition;
import com.mengxinya.ys.sql.condition.SqlCondition;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class SqlQueryBuilder implements SqlQuery{
    private Statement selectStatement;
    private Statement fromStatement;
    private Statement whereStatement;
    private Statement groupByStatement;
    private Statement havingStatement;
    private Statement orderByStatement;
    private Statement limitStatement;
    private Statement joinStatement;

    public static SqlQueryBuilder from(Class<?> mappedClass) {
        SqlQueryBuilder builder = new SqlQueryBuilder();
        builder.setFromStatement(() -> SqlUtils.toUnderlineCase(StringUtils.uncapitalize(mappedClass.getSimpleName())));
        return builder;
    }

    public static SqlQueryBuilder from(Statement fromStatement) {
        SqlQueryBuilder builder = new SqlQueryBuilder();
        builder.setFromStatement(fromStatement);
        return builder;
    }

    public static SqlQueryBuilder from(SqlQuery query, String asName) {
        SqlQueryBuilder builder = new SqlQueryBuilder();
        builder.setFromStatement(() -> {
            String sql = query.toSql();
            return "(" + sql + ") as " + asName;
        });
        return builder;
    }

    public SqlQuery build() {
        return this;
    }

    public SqlQueryBuilder addWhere(SqlCondition condition) {
        if (this.whereStatement == null) {
            this.whereStatement = condition::toSql;
        }
        else {
            String sql = whereStatement.toSql();
            this.whereStatement = () -> (sql + " and " + condition.toSql());
        }
        return this;
    }

    public SqlQueryBuilder addWhere(ParamsCondition condition) {
        return addWhere((SqlCondition)condition);
    }
}
