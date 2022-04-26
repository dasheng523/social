package com.mengxinya.ys.sql;

import com.mengxinya.ys.sql.repository.DataRepository;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.sqlite.SQLiteDataSource;

import java.sql.SQLException;
import java.util.List;

public class DataFetcher {

    private static final NamedParameterJdbcTemplate jdbcTemplate;

    static {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite::resource:db.sqlite");

        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }


    public static <T> List<T> getList(DataRepository<T> repository) {
        return jdbcTemplate.query(repository.toSql(), repository.getParams(), (rs, rowNum) -> repository.getRowStuffer().fillRow(
                new ResultItem() {
                    private int current = 1;
                    private boolean isSkip = false;

                    @Override
                    public boolean nextRow() throws SQLException {
                        current = 1;
                        if (isSkip) {
                            isSkip = false;
                            return true;
                        }
                        return rs.next();
                    }

                    @Override
                    public void skipTheNext() {
                        isSkip = true;
                    }

                    @Override
                    public String currentColumnName() throws SQLException {
                        String field = JdbcUtils.lookupColumnName(rs.getMetaData(), current);
                        if (field.startsWith(repository.getName() + ".")) {
                            String[] pies = field.split("\\.");
                            return pies[pies.length - 1];
                        }
                        return field;
                    }

                    @Override
                    public Object currentColumnValue(Class<?> propertyType) throws SQLException {
                        return JdbcUtils.getResultSetValue(rs, current, propertyType);
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
                }));
    }
}
