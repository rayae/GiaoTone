package cn.bavelee.giaotone.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Logcat {
    private static final String TAG = "充电提示音";
    private static boolean logToFile;
    private static File mLogFile;
    private static FileWriter mWriter;
    private static SimpleDateFormat sdf;

    public static void init(Context context) {
        try {
            logToFile = true;
            mLogFile = IOUtils.getCacheFile(context, "程序运行日志.txt");
            sdf = new SimpleDateFormat("MM月dd日 HH:mm:ss :  ", Locale.CHINA);
            writeToFile("日志服务启动", false);
        } catch (Exception ignore) {

        }
    }

    public static String getLogContent() {
        if (mLogFile == null) return null;
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(mLogFile));
            String line = null;
            while (null != (line = br.readLine())) {
                sb.append(line).append("\n");
            }
            br.close();
        } catch (Exception ignored) {

        }
        return sb.toString();
    }

    private static void writeToFile(String content, boolean isAppend) {
        try {
            mWriter = new FileWriter(mLogFile, isAppend);
            mWriter.write(sdf.format(new Date()));
            mWriter.write(content);
            mWriter.write("\n");
            mWriter.flush();
        } catch (Exception ignore) {
        } finally {
            try {
                mWriter.flush();
                mWriter.close();
            } catch (IOException ignore) {

            }
        }
    }

    public static void d(String str) {
        android.util.Log.d(TAG, str);
        if (logToFile)
            writeToFile(str, true);
    }

    public static void d(String str, Object... args) {
        String content = String.format(str, args);
        d(content);
    }
}
