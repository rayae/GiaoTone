package cn.bavelee.giaotone.model;

import java.util.List;

public class OnlineSoundList {


    private List<Sound> list;

    public List<Sound> getList() {
        return list;
    }

    public void setList(List<Sound> list) {
        this.list = list;
    }

    public static class Sound {
        /**
         * name : 热门 不开心到此为止(团子)
         * url : https://imbavelee.coding.net/p/GiaoTone/d/giao-tone-server/git/raw/master/sound/000_热门/000_不开心到此为止(团子).mp3
         * category : 热门
         */

        private String name;
        private String url;
        private String category;

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

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }
}
