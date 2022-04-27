package com.mengxinya.ys.sql.condition;

import com.mengxinya.ys.sql.SqlUtils;
import com.mengxinya.ys.sql.field.EntitySqlField;
import com.mengxinya.ys.sql.field.SqlField;

import java.util.HashMap;
import java.util.Map;

public class SqlConditions {
    public static <T> ParamsCondition eqVal(SqlField<T> field, T val) {
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

    public static <O, M, F1, F2> CheckerCondition<O, M> eq(EntitySqlField<O, F1> field1, EntitySqlField<M, F2> field2) {
        return new CheckerCondition<>() {

            @Override
            public boolean check(O o, M m) {
                F1 oVal = field1.getFieldVal(o);
                F2 mVal = field2.getFieldVal(m);
                return oVal.equals(mVal);
            }

            @Override
            public String toSql() {
                return field1.toSql() + "=" + field2.toSql();
            }
        };
    }

}
