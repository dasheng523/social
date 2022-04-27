package com.mengxinya.ys.sql.repository;

import com.mengxinya.ys.sql.DataPair;
import com.mengxinya.ys.sql.RowStuffer;
import com.mengxinya.ys.sql.condition.CheckerCondition;

import java.util.ArrayList;
import java.util.List;

public class DataRepositories {
    public static <O, M> DataSqlRepository<DataPair<O, List<M>>> hasMany(DataSqlRepository<O> main, DataSqlRepository<M> other, CheckerCondition<O, M> condition) {
        return new JoinSqlDataRepository<>(main, other, condition) {

            @Override
            public RowStuffer<DataPair<O, List<M>>> getRowStuffer() {
                return rs -> {
                    O oneObj = main.getRowStuffer().fillRow(rs);
                    int index = rs.getColumnIndex();

                    List<M> mList = new ArrayList<>();
                    while (true) {
                        rs.setColumnIndex(index);
                        M manyObj = other.getRowStuffer().fillRow(rs);

                        if (!condition.check(oneObj, manyObj)) {
                            rs.prevRow();
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
                return main.getName() + "_HasMany_" + other.getName();
            }

        };
    }

    public static <O, M> DataSqlRepository<DataPair<O, M>> hasOne(DataSqlRepository<O> main, DataSqlRepository<M> other, CheckerCondition<O, M> condition) {
        return new JoinSqlDataRepository<>(main, other, condition) {

            @Override
            public RowStuffer<DataPair<O, M>> getRowStuffer() {
                return rs -> {
                    O oneObj = main.getRowStuffer().fillRow(rs);
                    M otherObj = other.getRowStuffer().fillRow(rs);
                    return new DataPair<>(oneObj, otherObj);
                };
            }

            @Override
            public String getName() {
                return main.getName() + "_HasOne_" + other.getName();
            }

        };
    }
}
