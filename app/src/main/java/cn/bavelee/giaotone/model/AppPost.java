package cn.bavelee.giaotone.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AppPost {

    /**
     * notice : {"content":"这是通知内容","id":"notice_10001"}
     * qqgroup : {"content":"官方QQ交流 1031371576","qqkey":"LpulhX8wT0Ji0ItrSbcYvxLket_FhMta"}
     */

    private Notice notice;
    private QQGroup qqgroup;


    public Notice getNotice() {
        return notice;
    }

    public void setNotice(Notice notice) {
        this.notice = notice;
    }

    public QQGroup getQqgroup() {
        return qqgroup;
    }

    public void setQqgroup(QQGroup qqgroup) {
        this.qqgroup = qqgroup;
    }

    public static class Notice {
        /**
         * content : 这是通知内容
         * id : notice_10001
         */

        private String content;
        private String id;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class QQGroup {
        /**
         * content : 官方QQ交流 1031371576
         * qqkey : LpulhX8wT0Ji0ItrSbcYvxLket_FhMta
         */

        private String content;
        private String qqkey;

        public static List<QQGroup> arrayQQGroupFromData(String str) {

            Type listType = new TypeToken<ArrayList<QQGroup>>() {
            }.getType();

            return new Gson().fromJson(str, listType);
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getQqkey() {
            return qqkey;
        }

        public void setQqkey(String qqkey) {
            this.qqkey = qqkey;
        }
    }
}
