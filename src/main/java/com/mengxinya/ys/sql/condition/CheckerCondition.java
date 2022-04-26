package com.mengxinya.ys.sql.condition;

public interface CheckerCondition<O,M> extends SqlCondition {
    boolean check(O o, M m);
}
