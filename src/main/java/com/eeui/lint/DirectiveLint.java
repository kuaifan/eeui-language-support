package com.eeui.lint;

import com.eeui.util.ExtraModulesUtil;
import com.eeui.util.Logger;
import com.eeui.util.SettingsUtil;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class DirectiveLint
{
    private static EEUITag[] tags;
    private static Set<String> tagNames;
    private static String[] htmlTags;
    
    public static void dump() {
        if (DirectiveLint.tags != null) {
            List<EEUITag> list = Arrays.asList(DirectiveLint.tags);
            Logger.warn("Tags: (" + list.size() + ") " + list.toString());
        }
        else {
            Logger.warn("Tags: null");
        }
        if (DirectiveLint.tagNames != null) {
            Logger.warn("Tag names: (" + DirectiveLint.tagNames.size() + ") " + DirectiveLint.tagNames.toString());
        }
        else {
            Logger.warn("Tag names: null");
        }
        List<EEUITag> list2 = ExtraModulesUtil.getTagsFromNodeModules();
        Logger.warn("Tag from node_modules: (" + list2.size() + ") " + list2.toString());
    }
    
    public static void reset() {
        loadBuiltInRules();
        if (DirectiveLint.tagNames != null) {
            DirectiveLint.tagNames.clear();
        }
        mergeToBuiltIn(SettingsUtil.getRules());
        Logger.info("Rebuild eeui component caches, " + DirectiveLint.tags.length + " tags found.");
    }
    
    public static void prepare() {
        if (DirectiveLint.tags == null) {
            loadBuiltInRules();
        }
        mergeToBuiltIn(SettingsUtil.getRules());
    }
    
    private static void mergeToTags(List<EEUITag> from) {
        List<EEUITag> builtIn = new ArrayList<>(Arrays.asList(DirectiveLint.tags));
        if (from != null) {
            for (EEUITag tag : from) {
                if (containsTag(tag.tag)) {
                    EEUITag builtInTag = getBuiltInEEUITag(tag.tag);
                    if (builtInTag == null) {
                        continue;
                    }
                    mergeCustom(builtInTag, tag);
                }
                else {
                    builtIn.add(tag);
                }
            }
        }
        DirectiveLint.tags = builtIn.toArray(new EEUITag[builtIn.size()]);
        if (DirectiveLint.tagNames != null) {
            DirectiveLint.tagNames.clear();
        }
    }
    
    private static void loadBuiltInRules() {
        InputStream is = DirectiveLint.class.getResourceAsStream("/directives/components.json");
        Gson gson = new Gson();
        DirectiveLint.tags = gson.fromJson(new InputStreamReader(is), EEUITag[].class);
        if (DirectiveLint.tags == null) {
            DirectiveLint.tags = new EEUITag[0];
        }
        loadNodeModules();
    }
    
    private static void loadNodeModules() {
        List<EEUITag> EEUITags = ExtraModulesUtil.getTagsFromNodeModules();
        mergeToTags(EEUITags);
        Logger.debug(EEUITags.toString());
    }
    
    private static void mergeToBuiltIn(EEUITag[] customRules) {
        if (customRules != null) {
            mergeToTags(Arrays.asList(customRules));
        }
    }
    
    private static void mergeCustom(EEUITag builtIn, EEUITag custom) {
        if (custom.parents != null && custom.parents.size() > 0) {
            if (builtIn.parents != null) {
                builtIn.parents.addAll(custom.parents);
            }
            else {
                builtIn.parents = custom.parents;
            }
        }
        if (custom.childes != null && custom.childes.size() > 0) {
            if (builtIn.childes != null) {
                builtIn.childes.addAll(custom.childes);
            }
            else {
                builtIn.childes = custom.childes;
            }
        }
        if (custom.attrs != null && custom.attrs.size() > 0) {
            if (builtIn.attrs != null) {
                for (Attribute attr : builtIn.attrs) {
                    if (custom.getAttribute(attr.name) != null) {
                        attr.merge(custom.getAttribute(attr.name));
                    }
                }
            }
            for (Attribute attr2 : custom.attrs) {
                if (builtIn.getAttribute(attr2.name) == null) {
                    builtIn.attrs.add(attr2);
                }
            }
        }
        if (custom.events != null && custom.events.size() > 0) {
            for (Event event : custom.events) {
                if (builtIn.getEvent(event.name) == null) {
                    builtIn.events.add(event);
                }
            }
        }
    }
    
    public static Set<String> getEEUITagNames() {
        if (DirectiveLint.tagNames == null) {
            DirectiveLint.tagNames = new HashSet<>();
        }
        if (DirectiveLint.tagNames.size() == 0) {
            if (DirectiveLint.tags == null) {
                prepare();
            }
            for (EEUITag tag : DirectiveLint.tags) {
                if (!"common".equals(tag.tag)) {
                    DirectiveLint.tagNames.add(tag.tag);
                }
            }
        }
        Logger.debug(DirectiveLint.tagNames.toString());
        Logger.debug(Arrays.asList(DirectiveLint.tags).toString());
        return DirectiveLint.tagNames;
    }
    
    public static List<String> getHtmlTags() {
        return Arrays.asList(DirectiveLint.htmlTags);
    }
    
    public static boolean containsTag(String tagName) {
        if (tagName == null || "common".equals(tagName)) {
            return false;
        }
        if (DirectiveLint.tags == null) {
            prepare();
        }
        for (EEUITag tag : DirectiveLint.tags) {
            if (tagName.equals(tag.tag)) {
                return true;
            }
        }
        return false;
    }
    
    private static EEUITag getBuiltInEEUITag(String tagName) {
        if (DirectiveLint.tags == null) {
            loadBuiltInRules();
        }
        for (EEUITag tag : DirectiveLint.tags) {
            if (tag.tag.equals(tagName)) {
                return tag;
            }
        }
        return null;
    }
    
    public static EEUITag getCommonTag() {
        if (DirectiveLint.tags == null) {
            prepare();
        }
        for (EEUITag tag : DirectiveLint.tags) {
            if (tag.tag.equals("common")) {
                return tag;
            }
        }
        return null;
    }
    
    public static EEUITag getEEUITag(String tagName) {
        if (DirectiveLint.tags == null) {
            prepare();
        }
        EEUITag common = null;
        EEUITag target = null;
        for (EEUITag tag : DirectiveLint.tags) {
            if (tag.tag.equals(tagName)) {
                target = tag;
            }
            if (tag.tag.equals("common")) {
                common = tag;
            }
        }
        if (common != null) {
            if (target != null) {
                return merge(target, common);
            }
            return common;
        } else {
            return target;
        }
    }
    
    private static EEUITag merge(EEUITag target, EEUITag common) {
        if (target.attrs == null) {
            target.attrs = new CopyOnWriteArrayList<>();
        }
        if (target.events == null) {
            target.events = new CopyOnWriteArrayList<>();
        }
        target.attrs.addAll(common.attrs);
        target.events.addAll(common.events);
        return target;
    }
    
    static {
        DirectiveLint.tags = null;
        DirectiveLint.tagNames = null;
        DirectiveLint.htmlTags = new String[] { "div", "text", "video", "a" };
    }
}
