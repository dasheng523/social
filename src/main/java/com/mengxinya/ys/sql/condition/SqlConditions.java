package com.mengxinya.ys.sql.condition;

import com.mengxinya.ys.sql.SqlUtils;
import com.mengxinya.ys.sql.field.SqlField;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class SqlConditions {
    public static ParamsCondition eq(SqlField field, Object val) {
        String key = SqlUtils.shortUuid();
        return new ParamsCondition() {
            @Override
            public Map<String, Object> getParams() {
                return new HashMap<>(){{
                    put(key, val);
                }};
            }

            @Override
            public String toSql() {
                return field.toSql() + " = :" + key;
            }
        };
    }

    // TODO func能否去掉？因为都相等了，何必多此一举？
    public static <O, M> CheckerCondition<O, M> eq(SqlField field1, SqlField field2, BiFunction<O, M, Boolean> func) {
        return new CheckerCondition<>() {
            @Override
            public boolean check(O o, M m) {
                return func.apply(o, m);
            }

            @Override
            public String toSql() {
                return field1.toSql() + "=" + field2.toSql();
            }
        };
    }
}
