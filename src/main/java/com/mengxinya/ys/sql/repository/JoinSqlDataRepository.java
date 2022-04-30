package com.mengxinya.ys.sql.repository;

import com.mengxinya.ys.sql.condition.CheckerCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class JoinSqlDataRepository<T, O, M> implements DataSqlRepository<T> {
    private final DataSqlRepository<O> main;

    private final List<RepositoryRelate<O, ?>> relates;

    public JoinSqlDataRepository(DataSqlRepository<O> main, DataSqlRepository<M> other, CheckerCondition<O, M> condition) {
        this(main, List.of(new RepositoryRelate<>(other, condition)));
    }

    public JoinSqlDataRepository(DataSqlRepository<O> main, List<RepositoryRelate<O, ?>> relates) {
        this.main = main;
        this.relates = relates;
    }


    @Override
    public Map<String, Object> getParams() {
        Map<String, Object> map = new HashMap<>(main.getParams());
        relates.forEach(relate -> map.putAll(relate.other().getParams()));
        return map;
    }

    @Override
    public SqlQueryBuilder getSqlQueryBuilder() {
        Statement fromStatement = () -> "(" + main.toSql() + ") as " + main.getName();
        Statement selectStatement = () -> {
            String mainFields = main.getFieldNames()
                    .stream()
                    .map(name -> main.getName() + "." + name + " as '" + main.getName() + "." + name + "'")
                    .collect(Collectors.joining(", "));
            String otherFields = relates.stream()
                    .map(
                            relate ->
                                    relate.other()
                                            .getFieldNames()
                                            .stream()
                                            .map(name ->
                                                    relate.other().getName() + "." + name + " as '" + relate.other().getName() + "." + name + "'")
                                            .collect(Collectors.joining(", "))
                    )
                    .collect(Collectors.joining(", "));

            return mainFields + ", " + otherFields;
        };
        Statement joinStatement = () -> relates.stream().map(relate -> "left join (" + relate.other().toSql() + ") as " + relate.other().getName() + " on " + relate.condition().toSql()).collect(Collectors.joining(" "));

        SqlQueryBuilder builder = SqlQueryBuilder.from(fromStatement);
        builder.setSelectStatement(selectStatement);
        builder.setJoinStatement(joinStatement);
        return builder;
    }


    @Override
    public List<String> getFieldNames() {
        List<String> oneList = new ArrayList<>(main.getFieldNames().stream().map(name -> main.getName() + "." + name).toList());
        for (var relate : relates) {
            var other = relate.other();
            List<String> manyList = other.getFieldNames().stream().map(name -> other.getName() + "." + name).toList();
            oneList.addAll(manyList);
        }
        return oneList;
    }

}
