package com.mengxinya.ys.task;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class SpecItem {
    @ExcelProperty(index = 0)
    private String id;

    @ExcelProperty(index = 1)
    private String approvalCode;

    @ExcelProperty(index = 2)
    private String spec;
}
