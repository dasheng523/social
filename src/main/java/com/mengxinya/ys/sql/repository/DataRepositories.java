package com.mengxinya.ys.sql.repository;

import com.mengxinya.ys.sql.DataPair;
import com.mengxinya.ys.sql.RowStuffer;
import com.mengxinya.ys.sql.condition.CheckerCondition;

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
