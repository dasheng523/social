package com.mengxinya.ys.sql.condition;

import java.util.Map;

public interface ParamsCondition extends SqlCondition{
    Map<String, Object> getParams();
}
