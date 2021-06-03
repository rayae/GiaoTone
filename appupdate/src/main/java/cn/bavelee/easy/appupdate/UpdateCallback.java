package cn.bavelee.easy.appupdate;

public interface UpdateCallback {
    void onError(String msg);

    void onUpdate(UpdateData data);

    void onNoUpdate();
}
