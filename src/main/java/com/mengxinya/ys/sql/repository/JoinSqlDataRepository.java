package com.mengxinya.ys.sql.repository;

import com.mengxinya.ys.sql.condition.CheckerCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class JoinSqlDataRepository<T, O, M> implements DataSqlRepository<T> {
    private final DataSqlRepository<O> main;
    private final DataSqlRepository<M> other;
    private final CheckerCondition<O, M> condition;

    public JoinSqlDataRepository(DataSqlRepository<O> main, DataSqlRepository<M> other, CheckerCondition<O, M> condition) {
        this.main = main;
        this.other = other;
        this.condition = condition;
    }


    @Override
    public Map<String, Object> getParams() {
        Map<String, Object> map = new HashMap<>(main.getParams());
        map.putAll(other.getParams());
        return map;
    }

    @Override
    public Statement getSelectStatement() {
        return () -> {
            String oneSelect = main.getFieldNames().stream().map(name -> main.getName() + "." + name + " as '" + main.getName() + "." + name + "'").collect(Collectors.joining(", "));
            String manySelect = other.getFieldNames().stream().map(name -> other.getName() + "." + name + " as '" + other.getName() + "." + name + "'").collect(Collectors.joining(", "));
            return oneSelect + ", " + manySelect;
        };
    }

    @Override
    public Statement getFromStatement() {
        return () -> "(" + main.toSql() + ") as " + main.getName();
    }

    @Override
    public Statement getJoinStatement() {
        return () -> "left join (" + other.toSql() + ") as " + other.getName() + " on " + condition.toSql();
    }

    @Override
    public List<String> getFieldNames() {
        List<String> oneList = new ArrayList<>(main.getFieldNames().stream().map(name -> main.getName() + "." + name).toList());
        List<String> manyList = other.getFieldNames().stream().map(name -> other.getName() + "." + name).toList();
        oneList.addAll(manyList);
        return oneList;
    }

}
