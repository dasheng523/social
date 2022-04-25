package com.mengxinya.ys.sql;

public interface ResultItem {
    boolean nextRow();
    boolean previousRow();
    String currentColumnName();
    String currentColumnValue();
    boolean nextColumn();
}
