package cn.bavelee.giaotone.model;

public class UpdateBean {

    /**
     * verCode : 0
     * verName : 1.0.4
     * content : 1. 优化【不显示最近任务】体验#2. 增加【使用通知音量】开关
     3. Android 8.0 增加【应用常驻内存】开关
     4. 增加【自定义生效时间】开关
     5. 增加更多电量等级选项
     6. 增加【在线音频】
     7. 修复若干BUG
     * isForceUpdate : true
     * downloadUrl : https://gitee.com/Bave/giao-tone-server/raw/master/apk-release/GiaoTone-release-latest.apk
     */

    private int verCode;
    private String verName;
    private String content;
    private boolean isForceUpdate;
    private String downloadUrl;


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
}
