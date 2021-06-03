package cn.bavelee.giaotone.ui;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bavelee.donatedialog.DonateToMe;
import cn.bavelee.easy.appupdate.AppUpdate;
import cn.bavelee.easy.appupdate.UpdateCallback;
import cn.bavelee.easy.appupdate.UpdateData;
import cn.bavelee.giaotone.App;
import cn.bavelee.giaotone.Consts;
import cn.bavelee.giaotone.R;
import cn.bavelee.giaotone.adapter.binder.ClickableTextViewBinder;
import cn.bavelee.giaotone.adapter.binder.SimpleTextViewBinder;
import cn.bavelee.giaotone.adapter.binder.ToneControlViewBinder;
import cn.bavelee.giaotone.adapter.entity.ClickableTextEntity;
import cn.bavelee.giaotone.adapter.entity.SimpleTextEntity;
import cn.bavelee.giaotone.adapter.entity.ToneControlEntity;
import cn.bavelee.giaotone.model.SoundItem;
import cn.bavelee.giaotone.service.DaemonService;
import cn.bavelee.giaotone.service.SoundService;
import cn.bavelee.giaotone.ui.dialog.ErrorDialog;
import cn.bavelee.giaotone.ui.dialog.MessageDialog;
import cn.bavelee.giaotone.ui.searcher.AudioSearchActivity;
import cn.bavelee.giaotone.ui.setting.SettingsActivity;
import cn.bavelee.giaotone.util.DBUtils;
import cn.bavelee.giaotone.util.HttpUtils;
import cn.bavelee.giaotone.util.IOUtils;
import cn.bavelee.giaotone.util.IntentUtils;
import cn.bavelee.giaotone.util.Logcat;
import cn.bavelee.giaotone.util.PrefsUtils;
import me.drakeet.multitype.MultiTypeAdapter;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(intent);
    }

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private MultiTypeAdapter mAdapter;
    private List<Object> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initDefaultData();
        setTitle(getString(R.string.app_name));
        checkAppPermission();
        checkLostSoundAndShowData();
        checkService();
    }

    private void checkService() {
        if (!IntentUtils.isServiceRunning(this, DaemonService.class.getCanonicalName()) || !IntentUtils.isServiceRunning(this, SoundService.class.getCanonicalName()))
            App.initService();
    }

    private void checkLostSoundAndShowData() {
        //检查音频文件是否被清理掉了
        int count = 0;
        StringBuilder sb = new StringBuilder();
        for (SoundItem item : LitePal.findAll(SoundItem.class)) {
            if (!item.isResId()) {
                File file = new File(item.getUrl());
                if (!file.canRead()) {
                    sb.append(item.getName()).append("\n");
                    count++;
                    item.delete();
                }
            }
        }
        if (count > 0) {
            for (ToneControlEntity entity : LitePal.findAll(ToneControlEntity.class)) {
                if (LitePal.where("id = ?", String.valueOf(entity.getSoundId())).count(SoundItem.class) == 0) {
                    entity.setSoundId(Consts.DEFAULT_SOUND_ID);
                    entity.save();
                }
            }
            Logcat.d("有%d个音频文件丢失", count);
            MessageDialog.newInstance(null, getString(R.string.lost_audio_files, count, sb.toString())).show(getSupportFragmentManager(), null);
        }
        showData();
    }


    private void checkAppPermission() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.request_permission), Consts.REQUEST_CODE_WRITE_PERMISSION, perms);
        }
    }

    //版本升级，数据迁移
    private void upgradeLocalDataV1() {
        Logcat.d("迁移数据到新版本");
        //prefs转数据库
        SharedPreferences sp = PrefsUtils.get();
        boolean isTimedEnable = sp.getBoolean(Consts.KEY_CUSTOM_TIME_ENABLED, false);
        String startTime = sp.getString(Consts.KEY_CUSTOM_TIME_START, Consts.DEFAULT_AVAILABLE_TIME);
        String endTime = sp.getString(Consts.KEY_CUSTOM_TIME_END, Consts.DEFAULT_AVAILABLE_TIME);
        new ToneControlEntity(
                sp.getBoolean("key_plugin_enabled", true),
                getString(R.string.plug_in_sound),
                PrefsUtils.getInt("key_plugin_id", Consts.DEFAULT_SOUND_ID),
                Intent.ACTION_POWER_CONNECTED,
                0,
                isTimedEnable,
                startTime,
                endTime,
                getResources().getColor(R.color.colorPlugIn)
        ).save();
        new ToneControlEntity(
                sp.getBoolean("key_plugout_enabled", true),
                getString(R.string.plug_out_sound),
                PrefsUtils.getInt("key_plugout_id", Consts.DEFAULT_SOUND_ID),
                Intent.ACTION_POWER_DISCONNECTED,
                0,
                isTimedEnable,
                startTime,
                endTime,
                getResources().getColor(R.color.colorPlugOut)
        ).save();
        new ToneControlEntity(
                false,
                getString(R.string.screen_on),
                Consts.DEFAULT_SOUND_ID,
                Intent.ACTION_SCREEN_ON,
                0,
                false,
                Consts.DEFAULT_AVAILABLE_TIME,
                Consts.DEFAULT_AVAILABLE_TIME,
                getResources().getColor(R.color.colorScreen)
        ).save();
        new ToneControlEntity(
                false,
                getString(R.string.screen_off),
                Consts.DEFAULT_SOUND_ID,
                Intent.ACTION_SCREEN_OFF,
                0,
                false,
                Consts.DEFAULT_AVAILABLE_TIME,
                Consts.DEFAULT_AVAILABLE_TIME,
                getResources().getColor(R.color.colorScreen)
        ).save();
        new ToneControlEntity(
                sp.getBoolean("key_chargefull_enabled", true),
                getString(R.string.charge_full),
                PrefsUtils.getInt("key_chargefull_id", Consts.DEFAULT_SOUND_ID),
                Intent.ACTION_BATTERY_CHANGED,
                100,
                isTimedEnable,
                startTime,
                endTime,
                getResources().getColor(R.color.colorBatteryLevel)
        ).save();
        new ToneControlEntity(
                sp.getBoolean("key_batterylow_enabled", true),
                getString(R.string.battery_low),
                PrefsUtils.getInt("key_batterylow_id", Consts.DEFAULT_SOUND_ID),
                Intent.ACTION_BATTERY_LOW,
                0,
                isTimedEnable,
                startTime,
                endTime,
                getResources().getColor(R.color.colorBatteryLevel)
        ).save();
        for (int i = 1; i < Consts.BATTERY_LEVEL_COUNT; i++)
            new ToneControlEntity(
                    sp.getBoolean("key_battery_level_enabled_" + i, false),
                    getString(R.string.battery_level),
                    PrefsUtils.getInt("key_battery_level_id_" + i, Consts.DEFAULT_SOUND_ID),
                    Intent.ACTION_BATTERY_CHANGED,
                    sp.getInt("key_battery_level_" + i, 0),
                    isTimedEnable,
                    startTime,
                    endTime,
                    getResources().getColor(R.color.colorBatteryLevel),
                    true
            ).save();
        // 迁移数据库到 Litepal
        for (SoundItem item : DBUtils.readAll()) {
            item.save();
            DBUtils.deleteById(item.getId());
        }
        Logcat.d("迁移数据完成");
    }

    private void initDefaultData() {
        if (DBUtils.getCount() > 0 && LitePal.count(ToneControlEntity.class) == 0) {
            upgradeLocalDataV1();
        }
        if (LitePal.count(SoundItem.class) == 0) {
            //初始化音频库音频
            Logcat.d("初始化音频库音频");
            String[] names = getResources().getStringArray(R.array.raw_sounds);
            for (int i = 0; i < names.length; i++) {
                new SoundItem(i + 1, names[i], IOUtils.convertResIdToUri(this, Consts.INTERNAL_RAW_SOUNDS[i]).toString(), true).save();
            }
            new ToneControlEntity(
                    true,
                    getString(R.string.plug_in_sound),
                    Consts.DEFAULT_SOUND_ID,
                    Intent.ACTION_POWER_CONNECTED,
                    0,
                    false,
                    Consts.DEFAULT_AVAILABLE_TIME,
                    Consts.DEFAULT_AVAILABLE_TIME,
                    getResources().getColor(R.color.colorPlugIn)
            ).save();
            new ToneControlEntity(
                    true,
                    getString(R.string.plug_out_sound),
                    Consts.DEFAULT_SOUND_ID,
                    Intent.ACTION_POWER_DISCONNECTED,
                    0,
                    false,
                    Consts.DEFAULT_AVAILABLE_TIME,
                    Consts.DEFAULT_AVAILABLE_TIME,
                    getResources().getColor(R.color.colorPlugOut)
            ).save();
            new ToneControlEntity(
                    false,
                    getString(R.string.screen_on),
                    Consts.DEFAULT_SOUND_ID,
                    Intent.ACTION_USER_PRESENT,
                    0,
                    false,
                    Consts.DEFAULT_AVAILABLE_TIME,
                    Consts.DEFAULT_AVAILABLE_TIME,
                    getResources().getColor(R.color.colorScreen)
            ).save();
            new ToneControlEntity(
                    false,
                    getString(R.string.screen_off),
                    Consts.DEFAULT_SOUND_ID,
                    Intent.ACTION_SCREEN_OFF,
                    0,
                    false,
                    Consts.DEFAULT_AVAILABLE_TIME,
                    Consts.DEFAULT_AVAILABLE_TIME,
                    getResources().getColor(R.color.colorScreen)
            ).save();
            new ToneControlEntity(
                    false,
                    getString(R.string.charge_full),
                    Consts.DEFAULT_SOUND_ID,
                    Intent.ACTION_BATTERY_CHANGED,
                    100,
                    false,
                    Consts.DEFAULT_AVAILABLE_TIME,
                    Consts.DEFAULT_AVAILABLE_TIME,
                    getResources().getColor(R.color.colorBatteryLevel)
            ).save();
            new ToneControlEntity(
                    false,
                    getString(R.string.battery_low),
                    Consts.DEFAULT_SOUND_ID,
                    Intent.ACTION_BATTERY_LOW,
                    0,
                    false,
                    Consts.DEFAULT_AVAILABLE_TIME,
                    Consts.DEFAULT_AVAILABLE_TIME,
                    getResources().getColor(R.color.colorBatteryLevel)
            ).save();
            for (int i = 1; i < Consts.BATTERY_LEVEL_COUNT; i++)
                new ToneControlEntity(
                        false,
                        getString(R.string.battery_level),
                        Consts.DEFAULT_SOUND_ID,
                        Intent.ACTION_BATTERY_CHANGED,
                        0,
                        false,
                        Consts.DEFAULT_AVAILABLE_TIME,
                        Consts.DEFAULT_AVAILABLE_TIME,
                        getResources().getColor(R.color.colorBatteryLevel),
                        true
                ).save();
        }
        //screen_on换成user_present
        if (LitePal.where("filterAction = ?", Intent.ACTION_SCREEN_ON).findFirst(ToneControlEntity.class) != null) {
            ContentValues cv = new ContentValues();
            cv.put("filterAction", Intent.ACTION_USER_PRESENT);
            LitePal.updateAll(ToneControlEntity.class, cv, "filterAction = ?", Intent.ACTION_SCREEN_ON);
        }
        if (!PrefsUtils.getBoolean(Consts.KEY_IS_SHOWED_TIPS, false))
            startActivity(new Intent(this, HelpActivity.class));
    }

    private void showData() {
        if (mAdapter == null) {
            mAdapter = new MultiTypeAdapter();
            mAdapter.register(ToneControlEntity.class, new ToneControlViewBinder());
            mAdapter.register(ClickableTextEntity.class, new ClickableTextViewBinder());
            mAdapter.register(SimpleTextEntity.class, new SimpleTextViewBinder());
        }
        items.add(new SimpleTextEntity(getString(R.string.header_popular)));
        items.add(new ClickableTextEntity(getString(R.string.click_me_for_online_sound_library), v -> startActivity(new Intent(v.getContext(), OnlineActivity.class))));
        items.add(new ClickableTextEntity(getString(R.string.click_me_for_import_sound), v -> importSoundFile()));
        items.add(new ClickableTextEntity(getString(R.string.click_me_for_help), v -> startActivity(new Intent(MainActivity.this, HelpActivity.class))));
        items.add(new ClickableTextEntity(getString(R.string.settings), v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class))));
        items.add(new ClickableTextEntity("纯净版本"));
        items.add(new ClickableTextEntity(getString(R.string.click_me_for_donate_developer), v -> DonateToMe.show(v.getContext())));
        items.addAll(LitePal.findAll(ToneControlEntity.class));

        items.add(new SimpleTextEntity(getString(R.string.thanks), true, new View.OnLongClickListener() {
            private int count = 0;

            @Override
            public boolean onLongClick(View v) {
                if (count < 3) {
                    count++;
                    return false;
                } else {
                    count = 0;
                    PrefsUtils.edit().putBoolean(Consts.APP_PACKAGE_NAME, !PrefsUtils.getBoolean(Consts.APP_PACKAGE_NAME, false)).apply();
                    return true;
                }
            }
        }));
        mAdapter.setItems(items);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                Object item = items.get(position);
                if (item instanceof ClickableTextEntity) {
                    return 1;
                } else return 3;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    private void importSoundFile() {
        try {
            String picker = PrefsUtils.getString(Consts.KEY_SOUND_PICKER, Consts.DEFAULT_SOUND_PICKER);
            if (Consts.SOUND_PICKER_INTERNAL_SEARCHER.equals(picker)) {
                startActivity(new Intent(MainActivity.this, AudioSearchActivity.class));
                return;
            }
            boolean isFileManagerIntent = Consts.SOUND_PICKER_FILE_MANAGER.equals(picker);
            Intent intentFileManager = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            Intent intentMusicApp = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            Intent intent, anotherIntent;
            if (isFileManagerIntent) {
                intent = intentFileManager;
                anotherIntent = intentMusicApp;
            } else {
                intent = intentMusicApp;
                anotherIntent = intentFileManager;
            }
            List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent
                    , PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfos.size() != 0) {
                startActivityForResult(intent, Consts.REQUEST_CODE_PICK_EXTERNAL_SOUND);
            } else {
                startActivityForResult(anotherIntent, Consts.REQUEST_CODE_PICK_EXTERNAL_SOUND);
            }

        } catch (Exception e) {
            ErrorDialog.newInstance(getString(R.string.no_picker_installed) + "\n\n\n" + IOUtils.getExceptionStackInfo(e)).show(getSupportFragmentManager(), null);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        } catch (Exception ignore) {

        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.settings)
            startActivity(new Intent(this, SettingsActivity.class));
        return super.onOptionsItemSelected(item);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Consts.REQUEST_CODE_PICK_EXTERNAL_SOUND) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri == null) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Intent getData() is null\n");
                        for (String key : bundle.keySet()) {
                            sb.append(key).append("\n");
                        }
                        ErrorDialog.newInstance(sb.toString()).show(getSupportFragmentManager(), null);
                    }
                    Toast.makeText(this, R.string.select_file_failure, Toast.LENGTH_SHORT).show();
                } else showEditDialog(MainActivity.this, uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == Consts.REQUEST_CODE_WRITE_PERMISSION) {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            finishAndRemoveTask();
        }
    }

    public static void showEditDialog(AppCompatActivity activity, final Uri uri) {
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_custom_name, null);
        final AppCompatEditText editText = dialogView.findViewById(R.id.etName);
        String name = IOUtils.getUriRealFileName(activity, uri);
        final String defName;
        if (name != null) {
            int i = name.lastIndexOf('.');
            if (i < 0) i = name.length();
            defName = name.substring(0, i);
        } else {
            defName = activity.getString(R.string.untitled, System.currentTimeMillis());
        }
        editText.setText(defName);
        if (!activity.isFinishing())
            new AlertDialog.Builder(activity)
                    .setView(dialogView)
                    .setTitle(R.string.add_sound)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = editText.getText().toString();
                            if (TextUtils.isEmpty(name)) {
                                name = defName;
                            }
                            dialog.cancel();
                            showImportProgressDialog(activity, name, uri);
                        }
                    })
                    .setCancelable(false)
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
    }

    public static void showImportProgressDialog(AppCompatActivity activity, String fileName, Uri uri) {
        File savedFile = IOUtils.getCacheFile(activity, fileName + ".xmp3", Consts.AUDIO_SUB_DIR);
        Logcat.d("【导入音频】: 保存音频文件到 uri=" + uri.toString() + " path=" + savedFile.getPath());
        InputStream is = null;
        try {
            try {
                is = activity.getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                is = new FileInputStream(uri.getPath());
            }
            if (is != null && IOUtils.writeInputStreamToCache(is, savedFile)) {
                if (new SoundItem(fileName, savedFile.getAbsolutePath(), false, false).save()) {
                    Toast.makeText(activity, R.string.add_sound_success, Toast.LENGTH_SHORT).show();
                } else {
                    ErrorDialog.newInstance(activity.getString(R.string.insert_into_database_failure, fileName, uri.toString())).show(activity.getSupportFragmentManager(), null);
                    Toast.makeText(activity, R.string.add_sound_failure, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        } catch (Exception e) {
            Logcat.d("【导入音频】失败了", e);
            ErrorDialog.newInstance(activity.getString(R.string.cache_sound_file_failure, fileName, uri.toString(), IOUtils.getExceptionStackInfo(e))).show(activity.getSupportFragmentManager(), null);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {

                }
            }
        }
        Toast.makeText(activity, R.string.add_sound_failure, Toast.LENGTH_SHORT).show();
    }
}
