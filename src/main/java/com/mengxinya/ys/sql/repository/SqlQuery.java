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


    default String toSql() {
        return "select " + (getSelectStatement() == null ? "*" : getSelectStatement().toSql() )
                + " from " + getFromStatement().toSql()
                + (getJoinStatement() == null ? "" : " " + getJoinStatement().toSql())
                + (getWhereStatement() == null ? "" : " where " + getWhereStatement().toSql())
                + (getGroupByStatement() == null ? "" : " group by " + getGroupByStatement().toSql())
                + (getHavingStatement() == null ? "" : " having " + getHavingStatement().toSql())
                + (getOrderByStatement() == null ? "" : " order by " + getOrderByStatement().toSql())
                + (getLimitStatement() == null ? "" : " limit " + getLimitStatement().toSql());
    }
}
