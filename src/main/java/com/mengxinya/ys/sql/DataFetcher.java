package com.mengxinya.ys.sql;

import com.mengxinya.ys.sql.repository.DataRepository;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.util.StringUtils;
import org.sqlite.SQLiteDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataFetcher {

    private static final NamedParameterJdbcTemplate jdbcTemplate;

    static {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite::resource:db.sqlite");

        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }


    public static <T> List<T> getList(DataRepository<T> repository) {
        SqlRowSet rs = jdbcTemplate.queryForRowSet(repository.toSql(), repository.getParams());
        List<T> tList = new ArrayList<>();
        while (true) {
            boolean hasNext;
            try {
                hasNext = rs.next();
            } catch (InvalidResultSetAccessException e) {
                hasNext = false;
            }

            if (!hasNext) {
                break;
            }

            T tObj = repository.getRowStuffer().fillRow(
                    new ResultItem() {
                        private int current = 1;

                        @Override
                        public boolean nextRow() {
                            current = 1;
                            return rs.next();
                        }

                        @Override
                        public void prevRow() {
                            current = 1;
                            rs.previous();
                        }

                        @Override
                        public String currentColumnName() {
                            String field = rs.getMetaData().getColumnLabel(current);
                            if (!StringUtils.hasLength(field)) {
                                field = rs.getMetaData().getColumnName(current);
                            }
                            String[] pies = field.split("\\.");
                            return pies[pies.length - 1];
                        }

                        @Override
                        public Object currentColumnValue(Class<?> propertyType) {
                            if (rs instanceof ResultSetWrappingSqlRowSet rowSet) {
                                try {
                                    return JdbcUtils.getResultSetValue(rowSet.getResultSet(), current, propertyType);
                                } catch (SQLException e) {
                                    throw new DataRepositoryException("读取字段值失败", e);
                                }
                            }
                            else {
                                return rs.getObject(current);
                            }
                        }

                        @Override
                        public void nextColumn() {
                            current++;
                        }

                        @Override
                        public int getColumnIndex() {
                            return current;
                        }

                        @Override
                        public void setColumnIndex(int index) {
                            this.current = index;
                        }
                    });

            tList.add(tObj);

        }
        return tList;
    }
}
