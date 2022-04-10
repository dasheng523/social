package com.mengxinya.ys.task;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ExtractSpecTaskTest {

    @Test
    public void testDoTask() {
        new ExtractSpecTask().doTask();
    }

    @Test
    public void testReadExcel() {
        List<SpecItem> sourceData  = new ExtractSpecTask().readFromExcel("e:/需要拆规格的械字号.xlsx");
        Assertions.assertEquals(4758, sourceData.size());
    }

    @Test
    public void testWriteExcel() {
        List<SpecItem> sourceData  = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            SpecItem item = new SpecItem();
            item.setSpec("spec" + i);
            item.setApprovalCode("code" + i);
            item.setId(i + "");
            sourceData.add(item);
        }
        new ExtractSpecTask().saveExcel(sourceData, "e:/test.xlsx");
    }
}
