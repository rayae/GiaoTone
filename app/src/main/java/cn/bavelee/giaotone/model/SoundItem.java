package cn.bavelee.giaotone.model;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import cn.bavelee.giaotone.Consts;

public class SoundItem extends LitePalSupport {
    private int id;
    private String name;
    private String url;
    private boolean isResId;
    @Column(ignore = true)
    private boolean isOnline;
    @Column(ignore = true)
    private boolean isPlaying;
    @Column(ignore = true)
    private String category;

    public SoundItem(int id, String name, String url, boolean isResId) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.isResId = isResId;
    }

    public SoundItem(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public SoundItem(String name, String url, boolean isResId, boolean isOnline) {
        this.name = name;
        if (isOnline) {
            this.url = url.replaceFirst(".*/master", Consts.SERVER);
        } else this.url = url;
        this.isResId = isResId;
        this.isOnline = isOnline;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isResId() {
        return isResId;
    }

    public void setResId(boolean resId) {
        isResId = resId;
    }
}
