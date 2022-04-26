package com.mengxinya.ys.sql;

import java.sql.SQLException;

public interface ResultItem {
    boolean nextRow() throws SQLException;
    boolean previousRow() throws SQLException;
    String currentColumnName() throws SQLException;
    Object currentColumnValue(Class<?> propertyType) throws SQLException;
    void nextColumn();
}
