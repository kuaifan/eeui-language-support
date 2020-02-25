package com.eeui.insight.bean;

import org.apache.http.util.TextUtils;

import java.util.List;

public class Module
{
    /**
     * name : animation
     * methods : [{"name":"transition","desc":"执行动画"}]
     * component : false
     */

    private String name;
    private boolean component;
    private List<MethodsBean> methods;

    public String capitalName() {
        if (this.name == null || this.name.length() == 0) {
            return "";
        }
        char[] cs = this.name.toCharArray();
        cs[0] = Character.toUpperCase(cs[0]);
        return String.valueOf(cs);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isComponent() {
        return component;
    }

    public void setComponent(boolean component) {
        this.component = component;
    }

    public List<MethodsBean> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodsBean> methods) {
        this.methods = methods;
    }

    public static class MethodsBean {
        /**
         * name : transition
         * desc : 执行动画
         */

        private String name;
        private String desc;

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
    }
}
