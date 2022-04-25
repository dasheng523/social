package com.mengxinya.ys.sql.repository;

import com.mengxinya.ys.sql.RowStuffer;

import java.util.Map;

public interface DataRepository<T> {
    String toSql();
    Map<String, Object> getParams();
    RowStuffer<T> getRowStuffer();
}
