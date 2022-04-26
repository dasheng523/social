package com.mengxinya.ys.sql;

import java.sql.SQLException;

public interface ResultItem {
    boolean nextRow() throws SQLException;
    void skipTheNext();
    String currentColumnName() throws SQLException;
    Object currentColumnValue(Class<?> propertyType) throws SQLException;
    void nextColumn();

    int getColumnIndex();

    void setColumnIndex(int index);
}
