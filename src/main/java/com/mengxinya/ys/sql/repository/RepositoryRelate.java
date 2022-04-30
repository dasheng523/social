package com.mengxinya.ys.sql.repository;

import com.mengxinya.ys.sql.condition.CheckerCondition;

public record RepositoryRelate<Main, Other>(DataSqlRepository<Other> other, CheckerCondition<Main, Other> condition) {
}

