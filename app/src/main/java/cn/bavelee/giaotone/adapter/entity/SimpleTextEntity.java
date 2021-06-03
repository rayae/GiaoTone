package cn.bavelee.giaotone.adapter.entity;

import android.view.View;

public class SimpleTextEntity {
    private String title;
    private boolean isCenter;
    private View.OnLongClickListener listener;

    public SimpleTextEntity(String title, boolean isCenter, View.OnLongClickListener listener) {
        this.title = title;
        this.isCenter = isCenter;
        this.listener = listener;
    }

    public SimpleTextEntity(String title, boolean isCenter) {
        this.title = title;
        this.isCenter = isCenter;
    }

    public View.OnLongClickListener getListener() {
        return listener;
    }

    public void setListener(View.OnLongClickListener listener) {
        this.listener = listener;
    }

    public SimpleTextEntity(String title) {
        this(title, false);
    }

    public boolean isCenter() {
        return isCenter;
    }

    public void setCenter(boolean center) {
        isCenter = center;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
