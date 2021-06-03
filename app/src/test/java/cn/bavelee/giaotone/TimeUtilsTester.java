package cn.bavelee.giaotone;

import org.junit.Test;

import cn.bavelee.giaotone.util.TimeUtils;

public class TimeUtilsTester {
    @Test
    public void test() {
        t("08:00", "00:00", "07:00");
        t("08:00", "08:00", "23:59");
        t("08:00", "09:00", "23:59");
        t("18:00", "00:00", "00:00");
        t("18:00", "21:00", "23:59");
        t("18:00", "06:00", "20:00");
        t("12:00", "23:00", "08:00");
        t("18:00", "13:00", "23:59");
        t("12:00", "11:00", "22:00");
        t("21:00", "23:00", "20:00");
        t("21:00", "20:00", "08:00");
        t("10:00", "09:00", "08:00");
        t("21:00", "23:00", "23:59");
    }

    private void t(String now, String start, String end) {
//        TimeUtils.isCurrentTimeAvailable(now, start, end);
        System.out.println(start + " " + now + " " + end + " = " + TimeUtils.isCurrentTimeAvailable(now, start, end));
    }
}
