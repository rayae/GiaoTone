package cn.bavelee.easy.appupdate;

import android.os.Parcel;
import android.os.Parcelable;

public class UpdateData implements Parcelable {
    /**
     * verCode : 15
     * verName : 1.0.11
     * content : 【修复】崩溃问题
     * 【修复】部分用户打不开、白屏
     * 若下载失败请到【酷安】更新。
     * isForceUpdate : true
     * downloadUrl : https://imbavelee.coding.net/p/GiaoTone/d/giao-tone-server/git/raw/master/apk-release/GiaoTone-release-latest.apk
     */

    private int verCode;
    private String verName;
    private String content;
    private boolean isForceUpdate;
    private String downloadUrl;

    public UpdateData() {
    }

    protected UpdateData(Parcel in) {
        verCode = in.readInt();
        verName = in.readString();
        content = in.readString();
        isForceUpdate = in.readByte() != 0;
        downloadUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(verCode);
        dest.writeString(verName);
        dest.writeString(content);
        dest.writeByte((byte) (isForceUpdate ? 1 : 0));
        dest.writeString(downloadUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UpdateData> CREATOR = new Creator<UpdateData>() {
        @Override
        public UpdateData createFromParcel(Parcel in) {
            return new UpdateData(in);
        }

        @Override
        public UpdateData[] newArray(int size) {
            return new UpdateData[size];
        }
    };

    public int getVerCode() {
        return verCode;
    }

    public void setVerCode(int verCode) {
        this.verCode = verCode;
    }

    public String getVerName() {
        return verName;
    }

    public void setVerName(String verName) {
        this.verName = verName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isIsForceUpdate() {
        return isForceUpdate;
    }

    public void setIsForceUpdate(boolean isForceUpdate) {
        this.isForceUpdate = isForceUpdate;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String toString() {
        return "UpdateData{" +
                "verCode=" + verCode +
                ", verName='" + verName + '\'' +
                ", content='" + content + '\'' +
                ", isForceUpdate=" + isForceUpdate +
                ", downloadUrl='" + downloadUrl + '\'' +
                '}';
    }
}
