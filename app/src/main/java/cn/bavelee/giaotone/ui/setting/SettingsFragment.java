package cn.bavelee.giaotone.ui.setting;

import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import org.litepal.LitePal;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import cn.bavelee.giaotone.App;
import cn.bavelee.giaotone.Consts;
import cn.bavelee.giaotone.R;
import cn.bavelee.giaotone.adapter.entity.ToneControlEntity;
import cn.bavelee.giaotone.receiver.ToneReceiver;
import cn.bavelee.giaotone.service.DaemonService;
import cn.bavelee.giaotone.service.SoundService;
import cn.bavelee.giaotone.ui.dialog.MessageDialog;
import cn.bavelee.giaotone.util.IntentUtils;
import cn.bavelee.giaotone.util.Logcat;
import cn.bavelee.giaotone.util.PlayUtils;
import cn.bavelee.giaotone.util.PrefsUtils;
import cn.bavelee.giaotone.util.VolumeUtils;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        addPreferencesFromResource(R.xml.settings);

        findPreference(Consts.KEY_EXCLUDE_FROM_RECENTS).setOnPreferenceChangeListener((preference, newValue) -> {
            App.enableHiddenLauncher(getActivity(), "true".equals(newValue.toString()));
            return true;
        });
        findPreference(Consts.KEY_AUTO_START).setOnPreferenceClickListener(preference -> {
            IntentUtils.launchSystemAutoStartManager(getActivity());
            return true;
        });
        findPreference(Consts.KEY_CUSTOM_TIME_START).setOnPreferenceClickListener(preference -> {
            showTimePicker(preference.getKey().equals(Consts.KEY_CUSTOM_TIME_START));
            return true;
        });
        findPreference(Consts.KEY_CUSTOM_TIME_END).setOnPreferenceClickListener(preference -> {
            showTimePicker(preference.getKey().equals(Consts.KEY_CUSTOM_TIME_START));
            return true;
        });
        findPreference(Consts.KEY_IGNORE_BATTERY_OPTIMIZATION).setOnPreferenceClickListener(preference -> {
            IntentUtils.gotoBatteryOptimization(getActivity());
            return true;
        });
        findPreference(Consts.KEY_MAIN_SERVER).setOnPreferenceChangeListener((preference, newValue) -> {
            App.updateServer(String.valueOf(newValue));
            return true;
        });
        findPreference(Consts.KEY_USE_FLOATING_SERVICE).setOnPreferenceChangeListener((preference, newValue) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity())) {
                if (!getActivity().isFinishing())
                    new AlertDialog.Builder(getActivity()).setCancelable(true)
                            .setTitle(R.string.notify)
                            .setMessage(R.string.request_floating_service_permission)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> IntentUtils.LaunchSettingCanDrawOverlays(SettingsFragment.this))
                            .show();
                return false;
            }
            App.restartApp();
            return true;
        });
        findPreference(Consts.KEY_SHOW_LOG).setOnPreferenceClickListener(preference -> {
            MessageDialog.newInstance(getString(R.string.settings_show_log), Logcat.getLogContent(), true).show(getActivity().getSupportFragmentManager(), null);
            return true;
        });
        findPreference(Consts.KEY_RAPID_CHECK_SCREEN_OFF).setOnPreferenceChangeListener((preference, newValue) -> {
            boolean checked = "true".equals(newValue.toString());
            String from = checked ? Intent.ACTION_SCREEN_OFF : ToneReceiver.ACTION_SCREEN_OFF;
            String to = !checked ? Intent.ACTION_SCREEN_OFF : ToneReceiver.ACTION_SCREEN_OFF;
            Logcat.d("from=" + from + " to=" + to);
            //screen_off换成cn.bavelee.giaotone.ACTION_SCREEN_OFF
            if (LitePal.where("filterAction = ?", from).findFirst(ToneControlEntity.class) != null) {
                ContentValues cv = new ContentValues();
                cv.put("filterAction", to);
                LitePal.updateAll(ToneControlEntity.class, cv, "filterAction = ?", from);
            }
            App.restartApp();
            return true;
        });
        findPreference(Consts.KEY_CHECK_SERVICE).setOnPreferenceClickListener(preference -> {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (ToneReceiver.ACTION_CHECK_RECEIVER_ALIVE_RESPONSE.equals(intent.getAction()))
                        isReceiverRunning = true;
                }
            };
            isReceiverRunning = false;
            textServiceStatus.delete(0, textServiceStatus.length());
            textServiceStatus.append("【声音服务】: ").append(IntentUtils.isServiceRunning(getActivity(), SoundService.class.getCanonicalName()) ? "正在运行" : "未在运行").append("\n");
            textServiceStatus.append("【守护进程】: ").append(IntentUtils.isServiceRunning(getActivity(), DaemonService.class.getCanonicalName()) ? "正在运行" : "未在运行").append("\n");
            getActivity().registerReceiver(receiver, new IntentFilter(ToneReceiver.ACTION_CHECK_RECEIVER_ALIVE_RESPONSE));
            getActivity().sendBroadcast(new Intent(ToneReceiver.ACTION_CHECK_RECEIVER_ALIVE));
            App.postDelayed(() -> {
                textServiceStatus.append("【意图监听器】: ").append(isReceiverRunning ? "正在运行" : "未在运行").append("\n");
                String usage = PrefsUtils.getString(Consts.KEY_AUDIO_USAGE, Consts.DEFAULT_AUDIO_USAGE);
                int usageChannel = PlayUtils.getUsageChannel(usage);
                int audioChannel = PlayUtils.convertUsageToAudioChannel(usageChannel);
                int originVolume = VolumeUtils.getStreamVolume(getActivity(), audioChannel);
                textServiceStatus.append("【声音通道】: ").append(usage).append("\n");
                textServiceStatus.append("【系统音量】: ").append(originVolume).append("\n");
                textServiceStatus.append("【独立音量】: ").append(PrefsUtils.getInt(Consts.KEY_SOUND_STANDALONE_VOLUME, 0)).append("%").append("\n");
                textServiceStatus.append("【音频选择器】: ").append(PrefsUtils.getString(Consts.KEY_SOUND_PICKER, Consts.DEFAULT_SOUND_PICKER)).append("\n");
                textServiceStatus.append("【隐藏系统提示音】: ").append(PrefsUtils.getBoolean(Consts.KEY_MUTE_WHILE_PLAYING, Consts.DEFAULT_MUTE_WHILE_PLAYING) ? "已启用" : "未启用").append("\n");
                textServiceStatus.append("【快速检测锁屏状态】: ").append(PrefsUtils.getBoolean(Consts.KEY_RAPID_CHECK_SCREEN_OFF, false) ? "已启用" : "未启用").append("\n");
                boolean status = Consts.DEFAULT_AVOID_TIME_NONE.equals(PrefsUtils.getString(Consts.KEY_AVOID_MISTAKE_TOUCH, Consts.DEFAULT_AVOID_TIME_NONE));
                textServiceStatus.append("【防误触模式】: ").append(!status ? PrefsUtils.getString(Consts.KEY_AVOID_MISTAKE_TOUCH, Consts.DEFAULT_AVOID_TIME_NONE) : "未启用").append("\n");
                status = Consts.DEFAULT_AVOID_TIME_NONE.equals(PrefsUtils.getString(Consts.KEY_AVOID_ACCIDENTAL_PLUG_OUT, Consts.DEFAULT_AVOID_TIME_NONE));
                textServiceStatus.append("【防断冲模式】: ").append(!status ? PrefsUtils.getString(Consts.KEY_AVOID_ACCIDENTAL_PLUG_OUT, Consts.DEFAULT_AVOID_TIME_NONE) : "未启用").append("\n");
                status = PrefsUtils.getBoolean(Consts.KEY_CUSTOM_TIME_ENABLED, Consts.DEFAULT_CUSTOM_TIME_ENABLED);
                String start = PrefsUtils.getString(Consts.KEY_CUSTOM_TIME_START, Consts.DEFAULT_AVAILABLE_TIME);
                String end = PrefsUtils.getString(Consts.KEY_CUSTOM_TIME_END, Consts.DEFAULT_AVAILABLE_TIME);
                textServiceStatus.append("【自定义生效时间】: ").append(status ? String.format(Locale.CHINESE, "已启用 %s~%s", start, end) : "未启用").append("\n");

                for (ToneControlEntity entity : LitePal.findAll(ToneControlEntity.class))
                    textServiceStatus.append(entity.toString()).append("\n");
                MessageDialog.newInstance(getString(R.string.settings_check_service), textServiceStatus.toString(), true).show(getActivity().getSupportFragmentManager(), null);
                getActivity().unregisterReceiver(receiver);
            }, 100);
            return true;
        });
        updateCustomTimeStatus();
    }

    private boolean isReceiverRunning = false;
    private StringBuilder textServiceStatus = new StringBuilder();

    @NonNull
    @Override
    public <T extends Preference> T findPreference(@NonNull CharSequence key) {
        return super.findPreference(key);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Consts.REQUEST_CODE_FLOATING_SERVICE_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity())) {
                Toast.makeText(getActivity(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
            } else {
                PrefsUtils.edit().putBoolean(Consts.KEY_USE_FLOATING_SERVICE, true).apply();
                App.restartApp();
            }
        }
    }

    private void updateCustomTimeStatus() {
        findPreference(Consts.KEY_CUSTOM_TIME_START).setSummary(PrefsUtils.getString(Consts.KEY_CUSTOM_TIME_START, Consts.DEFAULT_AVAILABLE_TIME));
        findPreference(Consts.KEY_CUSTOM_TIME_END).setSummary(PrefsUtils.getString(Consts.KEY_CUSTOM_TIME_END, Consts.DEFAULT_AVAILABLE_TIME));
    }

    private void showTimePicker(boolean isStartTime) {
        final String key = isStartTime ? Consts.KEY_CUSTOM_TIME_START : Consts.KEY_CUSTOM_TIME_END;
        String[] ss = PrefsUtils.getString(key, Consts.DEFAULT_AVAILABLE_TIME).split(":");
        int hourOfDay = Integer.parseInt(ss[0]);
        int minutes = Integer.parseInt(ss[1]);
        if (!getActivity().isFinishing())
            new TimePickerDialog(getActivity(), (view, hourOfDay1, minute) -> {
                String now = String.format(Locale.CHINESE, "%02d:%02d", hourOfDay1, minute);
                PrefsUtils.edit()
                        .putString(key, now)
                        .apply();
                updateCustomTimeStatus();
            }, hourOfDay, minutes, true).show();
    }

}
