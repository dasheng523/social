package com.mengxinya.ys.sql;

public interface RowStuffer<T> {
    T fillRow(ResultItem rs);
}
