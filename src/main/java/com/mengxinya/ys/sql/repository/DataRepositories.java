package com.mengxinya.ys.sql.repository;

import com.mengxinya.ys.sql.*;
import com.mengxinya.ys.sql.condition.CheckerCondition;
import com.mengxinya.ys.sql.field.SqlField;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataRepositories {
    public static <Main> DataSqlRepository<RelateManyResult<Main>> hasMany(DataSqlRepository<Main> main, List<RepositoryRelate<Main, ?>> relates) {
        return new JoinSqlDataRepository<>(main, relates) {

            @Override
            public RowStuffer<RelateManyResult<Main>> getRowStuffer() {
                return rs -> {
                    Main oneObj = main.getRowStuffer().fillRow(rs);
                    int index = rs.getColumnIndex();

                    Map<String, Set<Object>> dataMap = new HashMap<>();

                    OUT:
                    do {
                        rs.setColumnIndex(index);

                        for (var relate : relates) {
                            var obj = relate.other().getRowStuffer().fillRow(rs);
                            CheckerCondition condition = relate.condition();
                            if (!condition.check(oneObj, obj)) {
                                rs.prevRow();
                                break OUT;
                            }
                            Set<Object> mList = dataMap.computeIfAbsent(relate.other().getName(), k -> new HashSet<>());
                            mList.add(obj);
                        }

                    } while (rs.nextRow());

                    return new RelateManyResult<>(oneObj, dataMap);
                };
            }

            @Override
            public String getName() {
                return main.getName() + relates.stream().map(relate -> "_HasMany_" + relate.other().getName()).collect(Collectors.joining());
            }

        };
    }

    public static <O, M> DataSqlRepository<DataPair<O, Set<M>>> hasMany(DataSqlRepository<O> main, DataSqlRepository<M> other, CheckerCondition<O, M> condition) {
        List<RepositoryRelate<O, ?>> relates = List.of(new RepositoryRelate<>(other, condition));
        DataSqlRepository<RelateManyResult<O>> repository = hasMany(main, relates);
        return convertDataRepository(repository, rowStuffer -> rs -> {
            var data = rowStuffer.fillRow(rs);
            return new DataPair<>(data.main(), data.getMany(other));
        });
    }

    public static <T> DataSqlRepository<T> groupBy(DataRepository<?> repository, List<SqlField<?>> groupFields, List<SqlField<?>> selectFields, Class<T> mappingClass) {
        if (groupFields == null || groupFields.size() == 0 || selectFields == null || selectFields.size() == 0) {
            throw new DataRepositoryException("groupFields和selectFields必须有值");
        }
        return new DataSqlRepository<>() {
            @Override
            public Map<String, Object> getParams() {
                return repository.getParams();
            }

            @Override
            public RowStuffer<T> getRowStuffer() {
                return rs -> {
                    Map<String, Object> dataMap = new HashMap<>();
                    List<SqlField<?>> fieldList = new ArrayList<>(groupFields);
                    fieldList.addAll(selectFields);
                    for (SqlField<?> field : fieldList) {
                        Object val = rs.currentColumnValue(field.getFieldType());
                        String key = field.getFieldName();
                        dataMap.put(key, val);
                    }
                    return ClassUtils.initObject(mappingClass, dataMap);
                };
            }

            @Override
            public SqlQueryBuilder getSqlQueryBuilder() {
                String tableName = SqlUtils.shortUuid();
                SqlQueryBuilder builder = SqlQueryBuilder.from(repository.getSqlQueryBuilder(), tableName);
                builder.setGroupByStatement(() -> groupFields.stream().map(field -> tableName + "." + field.toSql()).collect(Collectors.joining(", ")));
                builder.setSelectStatement(() ->
                        builder.getGroupByStatement().toSql()
                                + ", "
                                + selectFields.stream().map(field -> tableName + "." + field.toSql()).collect(Collectors.joining(", ")));

                return builder;
            }

            @Override
            public String getName() {
                return repository.getName() + "Group";
            }

            @Override
            public List<String> getFieldNames() {
                return selectFields.stream().map(SqlField::getFieldName).collect(Collectors.toList());
            }
        };
    }

    public static <Source, Target> DataSqlRepository<Target> convertDataRepository(DataSqlRepository<Source> sourceDataSqlRepository, Function<RowStuffer<Source>, RowStuffer<Target>> converter) {
        return new DataSqlRepository<>() {
            @Override
            public Map<String, Object> getParams() {
                return sourceDataSqlRepository.getParams();
            }

            @Override
            public RowStuffer<Target> getRowStuffer() {
                return converter.apply(sourceDataSqlRepository.getRowStuffer());
            }

            @Override
            public SqlQueryBuilder getSqlQueryBuilder() {
                return sourceDataSqlRepository.getSqlQueryBuilder();
            }

            @Override
            public String getName() {
                return sourceDataSqlRepository.getName();
            }

            @Override
            public List<String> getFieldNames() {
                return sourceDataSqlRepository.getFieldNames();
            }
        };
    }

    public static <Main, Other> DataSqlRepository<DataPair<Main, Other>> hasOne(DataSqlRepository<Main> main, DataSqlRepository<Other> other, CheckerCondition<Main, Other> condition) {
        List<RepositoryRelate<Main, ?>> relates = List.of(new RepositoryRelate<>(other, condition));
        DataSqlRepository<RelateOneResult<Main>> repository = hasOne(main, relates);

        return convertDataRepository(repository, rowStuffer -> rs -> {
            RelateOneResult<Main> result = rowStuffer.fillRow(rs);
            Main mainObj = result.main();
            Other otherObj = result.getOne(other);
            return new DataPair<>(mainObj, otherObj);
        });
    }

    public static <Main> DataSqlRepository<RelateOneResult<Main>> hasOne(DataSqlRepository<Main> main, List<RepositoryRelate<Main, ?>> relates) {
        return new JoinSqlDataRepository<>(main, relates) {
            @Override
            public RowStuffer<RelateOneResult<Main>> getRowStuffer() {
                return rs -> {
                    Main oneObj = main.getRowStuffer().fillRow(rs);
                    Map<String, Object> dataMap = new HashMap<>();
                    relates.forEach(relate -> dataMap.put(
                            relate.other().getName(),
                            relate.other().getRowStuffer().fillRow(rs)
                    ));
                    return new RelateOneResult<>(oneObj, dataMap);
                };
            }

            @Override
            public String getName() {
                return main.getName() + relates.stream().map(relate -> "_HasOne_" + relate.other().getName()).collect(Collectors.joining());
            }

        };
    }
}
