package com.mengxinya.ys.task;

import com.alibaba.excel.EasyExcel;
import com.mengxinya.mould.Clay;
import com.mengxinya.mould.ClayConverter;
import com.mengxinya.mould.Mould;
import com.mengxinya.mould.SourceDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ExtractSpecTask {
    public void doTask() {
        List<SpecItem> sourceData = readFromExcel("e:/需要拆规格的械字号.xlsx");

        List<SpecItem> result = new ArrayList<>();

        List<Function<String, SourceDetail>> rules = new ArrayList<>();
        rules.add(exportRule1);
        rules.add(exportRule2);
        rules.add(exportRule3);

        for (int i = 1; i <= rules.size(); i++) {
            sourceData = doExport(i, result, sourceData, rules.get(i - 1));
        }

        saveExcel(result, "e:/data/data.xlsx");
        saveExcel(sourceData, "e:/data/left.xlsx");
    }

    static String joinKey = "join";
    static Mould.MouldContext context = Mould.makeContext();
    public static Mould main = Mould.join(
            context,
            joinKey,
            Mould.composeJoining(
                    Mould.repeat(Mould.theMould(" "), 0),
                    Mould.maybe(
                            Mould.theMould("、"),
                            Mould.maybe(Mould.theMould("，"), Mould.theMould(",")),
                            Mould.maybe(Mould.theMould("；"), Mould.theMould(";"))
                    ),
                    Mould.repeat(Mould.theMould(" "), 0)
            ),
            Mould.convert(
                    Mould.repeat(Mould.maybe(
                            Mould.Letter,
                            Mould.Digit,
                            Mould.Han,
                            Mould.theMould("㎜"), Mould.theMould("*"), Mould.theMould("∮"), Mould.theMould("±"),
                            Mould.theMould("-"), Mould.theMould("+"), Mould.theMould("."), Mould.theMould(" "),
                            Mould.theMould("φ"), Mould.theMould("#"), Mould.theMould("×"), Mould.theMould("/"),
                            Mould.theMould("("), Mould.theMould(")"),
                            Mould.theMould("（"), Mould.theMould("）")
                    )),
                    ClayConverter.joining("")
            )
    );

    private final Function<String, SourceDetail> exportRule1 = source -> {
        source = source.replaceAll(",", "，").replaceAll(";", "；");

        Mould mould = Mould.convert(
                Mould.compose(
                        main,
                        Mould.zeroOrOne(Mould.maybe(
                                Mould.theMould("。"),
                                context.backRef(joinKey)
                        )),
                        Mould.EOF),
                ClayConverter.deconstruct(0)
        );
        SourceDetail detail = mould.fill(source);
        if (detail.isFinish() && Clay.getValues(detail.getClay(), Object.class).size() > 1) {
            return detail;
        }
        return SourceDetail.notMatch(source);
    };

    private final Function<String, SourceDetail> exportRule2 = source -> {
        source = source.replaceAll(",", "，")
                .replaceAll(";", "；")
                .replaceAll("型号", "\n型号")
                .replaceAll("规格", "\n规格")
                .trim();

        Mould gap = Mould.not(Mould.maybe(Mould.Digit, Mould.Letter, Mould.Han));
        Mould prefix = Mould.compose(
                Mould.maybe(Mould.theMould("型号"), Mould.theMould("规格")),
                Mould.repeat(gap, 0)
        );

        Mould mould = Mould.compose(prefix, main, Mould.repeat(gap, 1), prefix, main);

        Mould convertMould = Mould.convert(mould, clay -> {
            List<Clay> clayList = Clay.deconstruct(clay);
            List<Object> mainList1 = Clay.getValues(clayList.get(1), Object.class);
            List<Object> mainList2 = Clay.getValues(clayList.get(4), Object.class);
            List<List<Object>> result = new ArrayList<>();
            result.add(mainList1);
            result.add(mainList2);
            return Clay.make(result);
        });
        return convertMould.fill(source);
    };

    private final Function<String, SourceDetail> exportRule3 = source -> {
        source = source.trim();
        if (source.contains("规格：") || source.contains("型号：")) {
            return SourceDetail.notMatch(source);
        }
        Mould.MouldContext context = Mould.makeContext();
        String joinKey = "key";
        Mould sep = Mould.maybe(
                Mould.theMould("、"),
                Mould.maybe(Mould.theMould("，"), Mould.theMould(",")),
                Mould.maybe(Mould.theMould("；"), Mould.theMould(";"))
        );
        Mould sepSub = Mould.maybe(
                Mould.theMould("×"), Mould.theMould("*"),
                Mould.theMould("/"), Mould.theMould("-")
        );
        Mould itemSub = Mould.convert(
                Mould.repeat(Mould.not(Mould.maybe(
                        Mould.theMould("("), Mould.theMould("（"),
                        Mould.theMould(":"), Mould.theMould("："),
                        Mould.theMould("规格"), Mould.theMould("型号"),
                        Mould.theMould("注"), Mould.theMould("。"),
                        sep, sepSub
                ))),
                ClayConverter.joining("")
        );

        Mould item = Mould.convert(Mould.cons(itemSub, Mould.repeat(Mould.composeJoining(sepSub, itemSub))), ClayConverter.joining(""));
        Mould mould = Mould.join(context, joinKey, sep, item);

        SourceDetail detail = mould.fill(source);

        // 排除只有一项的情况
        if (detail.isFinish() && Clay.getValues(detail.getClay(), Object.class).size() == 1) {
            return SourceDetail.notMatch(source);
        }

        // 如果尾部还存在字符串的话，不能出现规律模式。
        Mould gap = Mould.repeat(Mould.not(item));
        Mould lawMould = Mould.compose(gap, item);
        if (detail.isFinish() && detail.getLeftSource().length() > 0) {
            SourceDetail lawDetail = lawMould.fill(detail.getLeftSource());
            if (lawDetail.isFinish()) {
                return SourceDetail.notMatch(source);
            }
        }

        return detail;
    };

    private List<SpecItem> doExport(int i, List<SpecItem> result, List<SpecItem> sourceData, Function<String, SourceDetail> export) {
        List<SpecItem> leftData = new ArrayList<>();
        List<SpecItem> data = new ArrayList<>();
        for (SpecItem item : sourceData) {
            String source = item.getSpec();
            if (source == null || source.equals("")) {
                leftData.add(item);
                continue;
            }

            SourceDetail itemResult = export.apply(source);
            if (!itemResult.isFinish()) {
                leftData.add(item);
                continue;
            }

            SpecItem info = new SpecItem();
            info.setId(item.getId());
            info.setSpec(itemResult.getClay().toJsonString());
            info.setApprovalCode(item.getApprovalCode());
            data.add(info);
        }
        result.addAll(data);
        saveExcel(data, "e:/data/data" + i + ".xlsx");
        saveExcel(leftData, "e:/data/left" + i + ".xlsx");
        return leftData;
    }

    public void saveExcel(List<SpecItem> result, String filePath) {
        EasyExcel.write(filePath, SpecItem.class)
                .sheet("sheet")
                .doWrite(() -> result);
    }

    public List<SpecItem> readFromExcel(String filePath) {
        return EasyExcel.read(filePath).head(SpecItem.class).sheet().doReadSync();
    }


}
