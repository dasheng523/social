package com.mengxinya.ys.sql.repository;

public interface DataSqlRepository<T> extends DataRepository<T>, SqlQuery{
    default String toSql() {
        return "select " + (getSelectStatement() == null ? "*" : getSelectStatement().toSql() )
                + " from " + getFromStatement().toSql()
                + (getJoinStatement() == null ? "" : getJoinStatement().toSql())
                + (getWhereStatement() == null ? "" : " where " + getWhereStatement().toSql())
                + (getGroupByStatement() == null ? "" : " group by " + getGroupByStatement().toSql())
                + (getHavingStatement() == null ? "" : " having " + getHavingStatement().toSql())
                + (getOrderByStatement() == null ? "" : " order by " + getOrderByStatement().toSql())
                + (getLimitStatement() == null ? "" : " limit " + getLimitStatement().toSql());
    }
}
