package com.mengxinya.ys.sql.repository;

import java.util.Map;

public record RelateOneResult<Main>(Main main, Map<String, Object> dataMap) {
}
