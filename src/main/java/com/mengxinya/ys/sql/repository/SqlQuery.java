package com.mengxinya.ys.sql.repository;

import java.util.List;

public interface SqlQuery {
    Statement getSelectStatement();
    Statement getFromStatement();
    default Statement getWhereStatement() {
        return null;
    }
    default Statement getJoinStatement() {
        return null;
    }
    default Statement getGroupByStatement() {
        return null;
    }
    default Statement getHavingStatement() {
        return null;
    }
    default Statement getOrderByStatement() {
        return null;
    }
    default Statement getLimitStatement() {
        return null;
    }

    List<String> getFieldNames();
}
