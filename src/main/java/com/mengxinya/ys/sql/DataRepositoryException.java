package com.mengxinya.ys.sql;

public class DataRepositoryException extends RuntimeException{
    public DataRepositoryException(String msg, Throwable e) {
        super(msg, e);
    }

    public DataRepositoryException(String msg) {
        super(msg);
    }
}
