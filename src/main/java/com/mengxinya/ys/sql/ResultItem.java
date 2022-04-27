package com.mengxinya.ys.sql;

public interface ResultItem {
    boolean nextRow();
    void prevRow();
    String currentColumnName();
    Object currentColumnValue(Class<?> propertyType);
    void nextColumn();

    int getColumnIndex();

    void setColumnIndex(int index);
}
