package com.mengxinya.ys.sql;

import java.sql.SQLException;

public interface RowStuffer<T> {
    T fillRow(ResultItem rs) throws SQLException;
}
