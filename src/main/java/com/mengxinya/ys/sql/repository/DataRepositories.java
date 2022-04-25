package com.mengxinya.ys.sql.repository;

import com.mengxinya.ys.sql.DataPair;
import com.mengxinya.ys.sql.condition.CheckerCondition;

import java.util.List;

public class DataRepositories {
    public static <O, M> DataRepository<DataPair<O, List<M>>> hasMany(DataRepository<O> one, DataRepository<M> many, CheckerCondition<O, M> condition) {
        // TODO
        return null;
    }
}
