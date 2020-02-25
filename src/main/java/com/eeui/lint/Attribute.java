package com.eeui.lint;

import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Attribute
{
    public String name;
    public String valuePattern;
    public List<String> valueEnum;
    public String valueType;

    public boolean match(String value) {
        if (TextUtils.isEmpty(this.valuePattern)) {
            return false;
        }
        if (this.valuePattern.toLowerCase().equals("mustache")) {
            return Pattern.compile("\\{\\{.*\\}\\}").matcher(value).matches();
        }
        if (this.valuePattern.toLowerCase().equals("number")) {
            return Pattern.compile("[0-9]+([.][0-9]+)?$").matcher(value).matches();
        }
        if (this.valuePattern.toLowerCase().equals("boolean")) {
            return Pattern.compile("(true|false)$").matcher(value).matches();
        }
        try {
            return Pattern.compile(this.valuePattern).matcher(value).matches();
        }
        catch (Exception e) {
            return false;
        }
    }
    
    public void merge(Attribute attribute) {
        if (attribute.valuePattern != null) {
            this.valuePattern = attribute.valuePattern;
        }
        if (attribute.valueEnum != null) {
            if (this.valueEnum != null) {
                Set<String> set = new HashSet<String>(this.valueEnum);
                set.addAll(attribute.valueEnum);
                this.valueEnum = new ArrayList<>(set);
            }
            else {
                this.valueEnum = attribute.valueEnum;
            }
        }
        if (attribute.valueType != null) {
            this.valueType = attribute.valueType;
        }
    }
}
