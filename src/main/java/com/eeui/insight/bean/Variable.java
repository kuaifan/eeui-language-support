package com.eeui.insight.bean;

import java.util.List;

public class Variable
{

    /**
     * name : WXEnvironment
     * desc : 环境变量
     * root : true
     * methods : [{"name":"platform","desc":"运行平台: Android 或 iOS"},{"name":"appName","desc":"应用名称"},{"name":"appVersion","desc":"应用版本"},{"name":"osName","desc":"系统名称: Android 或 iOS"},{"name":"osVersion","desc":"系统版本"},{"name":"deviceModel","desc":"手机设备型号"},{"name":"deviceWidth","desc":"设备宽度"},{"name":"deviceHeight","desc":"设备高度"}]
     */

    private String name;
    private String desc;
    private boolean root;
    private List<MethodsBean> methods;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public List<MethodsBean> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodsBean> methods) {
        this.methods = methods;
    }

    public static class MethodsBean {
        /**
         * name : platform
         * desc : 运行平台: Android 或 iOS
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

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}
