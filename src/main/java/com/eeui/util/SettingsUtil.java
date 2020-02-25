package com.eeui.util;

import com.eeui.lint.EEUITag;
import com.google.gson.Gson;
import com.intellij.ide.util.PropertiesComponent;
import org.apache.http.util.TextUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SettingsUtil {

    public static EEUITag[] getRules() {
        String path = PropertiesComponent.getInstance().getValue("RULES_PATH", "");
        if (!TextUtils.isEmpty(path)) {
            try {
                return load(path);
            }
            catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static List<String> getGlobalModules() {
        List<String> result = new ArrayList<>();
        String[] paths = PropertiesComponent.getInstance().getValues("EEUI_GLOBAL_COMPONENTS");
        if (paths == null) {
            return result;
        }
        for (String s : paths) {
            String[] temp = s.split("#@#");
            if (temp.length == 2) {
                result.add(temp[1]);
            }
        }
        return result;
    }

    private static EEUITag[] load(String path) throws Exception {
        InputStream is = new FileInputStream(path);
        return new Gson().fromJson(new InputStreamReader(is), EEUITag[].class);
    }
}
