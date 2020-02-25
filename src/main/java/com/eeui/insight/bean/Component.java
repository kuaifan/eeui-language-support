package com.eeui.insight.bean;

import org.apache.http.util.TextUtils;

import java.util.List;

public class Component
{
    /**
     * name : a
     * parent : null
     * desc : 一个用于实现页面间的跳转或关闭的组件。
     * url : https://eeui.app/component/a.html
     * attrs : [{"name":"href","desc":"待跳转的页面 URL，-1为关闭当前页面","option":[{"value":"true","desc":""},{"value":"false","desc":""}]},{"name":"statusBarColor","desc":"状态栏颜色值","option":[{"value":"0","desc":"矩形"},{"value":"1","desc":"圆形"}]},{"name":"backgroundColor","desc":"页面背景颜色","option":[]}]
     */

    private String name;
    private String parent;
    private String desc;
    private String url;
    private List<AttrsBean> attrs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getDesc() {
        return desc;
    }

    public String getDescDef(String def) {
        return !TextUtils.isEmpty(desc) ? desc : def;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<AttrsBean> getAttrs() {
        return attrs;
    }

    public AttrsBean getAttr(String attrName) {
        if (attrName == null) {
            return null;
        }
        for (AttrsBean m : getAttrs()) {
            if (attrName.equals(m.getName())) {
                return m;
            }
        }
        return null;
    }

    public void setAttrs(List<AttrsBean> attrs) {
        this.attrs = attrs;
    }

    public static class AttrsBean {
        /**
         * name : href
         * desc : 待跳转的页面 URL，-1为关闭当前页面
         * type : String
         * defval : -
         * option : [{"value":"true","desc":""},{"value":"false","desc":""}]
         */

        private String name;
        private String desc;
        private String type;
        private String defval;
        private List<OptionBean> option;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public String getDescDef(String def) {
            return !TextUtils.isEmpty(desc) ? desc : def;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDefval() {
            return defval;
        }

        public void setDefval(String defval) {
            this.defval = defval;
        }

        public List<OptionBean> getOption() {
            return option;
        }

        public String[] getOptions() {
            String[] array = new String[option.size()];
            for (int i = 0; i < option.size(); i++) {
                array[i] = option.get(i).value;
            }
            return array;
        }

        public void setOption(List<OptionBean> option) {
            this.option = option;
        }

        public static class OptionBean {
            /**
             * value : true
             * desc :
             */

            private String value;
            private String desc;

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public String getDesc() {
                return desc;
            }

            public String getDescDef(String def) {
                return !TextUtils.isEmpty(desc) ? desc : def;
            }

            public void setDesc(String desc) {
                this.desc = desc;
            }
        }
    }
}
