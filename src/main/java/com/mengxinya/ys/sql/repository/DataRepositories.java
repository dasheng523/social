package com.mengxinya.ys.sql.repository;

import com.mengxinya.ys.sql.DataPair;
import com.mengxinya.ys.sql.RowStuffer;
import com.mengxinya.ys.sql.condition.CheckerCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataRepositories {
    public static <O, M> DataSqlRepository<DataPair<O, List<M>>> hasMany(DataSqlRepository<O> one, DataSqlRepository<M> many, CheckerCondition<O, M> condition) {
        return new DataSqlRepository<>() {

            @Override
            public Map<String, Object> getParams() {
                Map<String, Object> map = new HashMap<>(one.getParams());
                map.putAll(many.getParams());
                return map;
            }

            @Override
            public RowStuffer<DataPair<O, List<M>>> getRowStuffer() {
                return rs -> {
                    O oneObj = one.getRowStuffer().fillRow(rs);
                    int index = rs.getColumnIndex();

                    List<M> mList = new ArrayList<>();
                    while (true) {
                        rs.setColumnIndex(index);
                        M manyObj = many.getRowStuffer().fillRow(rs);

                        if (!condition.check(oneObj, manyObj)) {
                            rs.skipTheNext();
                            break;
                        }

                        mList.add(manyObj);

                        if (!rs.nextRow()) {
                            break;
                        }
                    }
                    return new DataPair<>(oneObj, mList);
                };
            }

            @Override
            public String getName() {
                return one.getName() + "_HasMany_" + many.getName();
            }

            @Override
            public Statement getSelectStatement() {
                return () -> {
                    String oneSelect = one.getFieldNames().stream().map(name -> one.getName() + "." + name + " as '" + one.getName() + "." + name + "'").collect(Collectors.joining(", "));
                    String manySelect = many.getFieldNames().stream().map(name -> many.getName() + "." + name + " as '" + many.getName() + "." + name + "'").collect(Collectors.joining(", "));
                    return oneSelect + ", " + manySelect;
                };
            }

            @Override
            public Statement getFromStatement() {
                return () -> "(" + one.toSql() + ") as " + one.getName();
            }

            @Override
            public Statement getJoinStatement() {
                return () -> "left join (" + many.toSql() + ") as " + many.getName() + " on " + condition.toSql();
            }

            @Override
            public List<String> getFieldNames() {
                List<String> oneList = new ArrayList<>(one.getFieldNames().stream().map(name -> one.getName() + "." + name).toList());
                List<String> manyList = many.getFieldNames().stream().map(name -> many.getName() + "." + name).toList();
                oneList.addAll(manyList);
                return oneList;
            }

        };
    }
}
