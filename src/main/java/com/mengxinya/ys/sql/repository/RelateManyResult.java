package com.mengxinya.ys.sql.repository;

import java.util.List;
import java.util.Map;

public record RelateManyResult<Main>(Main main, Map<String, List<Object>> dataMap) {
}
