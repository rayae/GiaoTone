package cn.bavelee.giaotone.adapter.entity;

import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.Locale;

public class ToneControlEntity extends LitePalSupport {
    private int id;
    private boolean isEnabled;
    private String title;
    private int soundId;
    private String filterAction;
    private int batteryLevel;
    private boolean isTimedEnabled;
    private String startTime;
    private String endTime;
    private int color;
    private boolean isBatteryLevelMode;
    @Column(ignore = true)
    private boolean isPlaying;

    public ToneControlEntity() {
    }

    public ToneControlEntity(boolean isEnabled, String title, int soundId, String filterAction, int batteryLevel, boolean isTimedEnabled, String startTime, String endTime, int color, boolean isBatteryLevelMode) {
        this.isEnabled = isEnabled;
        this.title = title;
        this.soundId = soundId;
        this.filterAction = filterAction;
        this.batteryLevel = batteryLevel;
        this.isTimedEnabled = isTimedEnabled;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
        this.isBatteryLevelMode = isBatteryLevelMode;
    }

    public ToneControlEntity(boolean isEnabled, String title, int soundId, String filterAction, int batteryLevel, boolean isTimedEnabled, String startTime, String endTime, int color) {
        this.isEnabled = isEnabled;
        this.title = title;
        this.soundId = soundId;
        this.filterAction = filterAction;
        this.batteryLevel = batteryLevel;
        this.isTimedEnabled = isTimedEnabled;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
    }

    public void refreshFromDatabase() {
        ToneControlEntity nt = LitePal.where("id = ?", String.valueOf(id)).findFirst(ToneControlEntity.class);
        if (nt != null) {
            setEndTime(nt.getEndTime());
            setStartTime(nt.getStartTime());
            setEnabled(nt.isEnabled());
            setTimedEnabled(nt.isTimedEnabled());
            setSoundId(nt.getSoundId());
            setBatteryLevel(nt.getBatteryLevel());
            setTitle(nt.getTitle());
            setFilterAction(nt.getFilterAction());
        }
    }

    public boolean isBatteryLevelMode() {
        return isBatteryLevelMode;
    }

    public void setBatteryLevelMode(boolean batteryLevelMode) {
        isBatteryLevelMode = batteryLevelMode;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSoundId() {
        return soundId;
    }

    public void setSoundId(int soundId) {
        this.soundId = soundId;
    }

    public String getFilterAction() {
        return filterAction;
    }

    public void setFilterAction(String filterAction) {
        this.filterAction = filterAction;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public boolean isTimedEnabled() {
        return isTimedEnabled;
    }

    public void setTimedEnabled(boolean timedEnabled) {
        isTimedEnabled = timedEnabled;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }


    @Override
    public String toString() {
        if (isBatteryLevelMode())
            return String.format(Locale.CHINESE, "【%d%% %s】\n\t\t开关状态=%3s 启用时间=%3s 时间区间=%5s~%5s", batteryLevel, title, isEnabled ? "已启用" : "已禁用", isTimedEnabled ? "已启用" : "已禁用", startTime, endTime);
        return String.format(Locale.CHINESE, "【%s】\n\t\t开关状态=%3s 启用时间=%3s 时间区间=%5s~%5s", title, isEnabled ? "已启用" : "已禁用", isTimedEnabled ? "已启用" : "已禁用", startTime, endTime);
    }
}