package red.man10.shigenassist.logic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SARemarks extends SAThreader {

    public static final String PERMISSION = "ShigenAssist.assist.remarks";
    private static final List<String> column = new LinkedList<>();
    private static List<String> nowRemark;
    private static final List<String> nowRemarks = new ArrayList<>();
    private static int period = 300;

    private SARemarks() {}

    public static List<String> getColumn() {
        return column;
    }
    public static boolean addColumn(String text) {
        if (column.contains(text)) return false;
        return column.add(text);
    }
    public static boolean removeColumn(String text) {
        return column.remove(text);
    }
    public static int getColumnSize() {
        return column.size();
    }
    public static List<String> getNowRemarks() {
        return nowRemark;
    }
    public static void setPeriod(int period) {
        SARemarks.period = period;
    }
    public static int getPeriod() {
        return period;
    }

    private static int nowRemarkIndex = 0;
    private static boolean next = true;
    public static void next() {
        if (column.isEmpty()) return;
        if (next) {
            if (nowRemarkIndex >= getColumnSize()) nowRemarkIndex = 0;
            nowRemarks.addAll(List.of(column.get(nowRemarkIndex++).split("\\\\n")));
            for (int i = 0; i < nowRemarks.size(); i++) {
                var text = nowRemarks.get(i);
                if (text.length() > SAScoreboard.MAX_LENGTH) {
                    nowRemarks.set(i, text.substring(0, SAScoreboard.MAX_LENGTH));
                    nowRemarks.add(i + 1, text.substring(SAScoreboard.MAX_LENGTH));
                }
            }
        }
        var list = nowRemarks.subList(0, Math.min(4, nowRemarks.size()));
        nowRemark = List.copyOf(list);
        list.clear();
        next = nowRemarks.isEmpty();
    }
    public static void run() {
        run(() -> {
            if (1 >= getColumnSize()) cancel();
            next();
        }, 0L, period * 20L);
    }
}
