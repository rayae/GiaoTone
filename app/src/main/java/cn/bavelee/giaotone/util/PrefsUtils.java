package cn.bavelee.giaotone.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefsUtils {
    private static SharedPreferences sSharedPreferences;

    public static void init(Context context) {
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences get() {
        return sSharedPreferences;
    }

    public static SharedPreferences.Editor edit() {
        return sSharedPreferences.edit();
    }

    public static String getString(String key, String def) {
        return sSharedPreferences.getString(key, def);
    }

    public static boolean getBoolean(String key, boolean def) {
        return sSharedPreferences.getBoolean(key, def);
    }

    public static int getInt(String key, int def) {
        return sSharedPreferences.getInt(key, def);
    }
}
