package cn.bavelee.giaotone.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import cn.bavelee.giaotone.model.SoundItem;

public class DBUtils {

    private static MyDBHelper mHelper;
    private static final String TABLE_NAME = "list";

    public static void init(Context context) {
        if (mHelper == null)
            mHelper = new MyDBHelper(context);
    }

    public static int getCount() {
        if (mHelper == null) return 0;
        int c = 0;
        Cursor cursor = mHelper.getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor != null) {
            c = cursor.getCount();
            cursor.close();
        }
        return c;
    }

    public static int deleteById(int id) {
        if (mHelper == null) return -1;
        return mHelper.getWritableDatabase().delete(TABLE_NAME, "id=" + id, null);
    }

    public static List<SoundItem> readAll() {
        List<SoundItem> items = new ArrayList<>();
        if (mHelper == null) return items;
        Cursor cursor = mHelper.getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String uri = cursor.getString(cursor.getColumnIndex("uri"));
                int is_res_id = cursor.getInt(cursor.getColumnIndex("is_res_id"));
                items.add(new SoundItem(id, name, uri, is_res_id == 1));
            }
            cursor.close();
        }
        return items;
    }

    private static class MyDBHelper extends SQLiteOpenHelper {

        public MyDBHelper(Context context) {
            super(context, "sound_list", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table list(id INTEGER PRIMARY KEY AUTOINCREMENT, name text, uri text, is_res_id int(2))");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
