package com.eeui.insight;

import com.eeui.lint.DirectiveLint;
import com.eeui.insight.bean.Module;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class ModuleManager
{
    private static Module[] modules;
    
    private static void loadBuiltInModules() {
        InputStream is = DirectiveLint.class.getResourceAsStream("/directives/modules.json");
        Gson gson = new Gson();
        ModuleManager.modules = gson.fromJson(new InputStreamReader(is), Module[].class);
        if (ModuleManager.modules == null) {
            ModuleManager.modules = new Module[0];
        }
    }
    
    public static List<Module> getModules() {
        if (ModuleManager.modules == null || ModuleManager.modules.length == 0) {
            loadBuiltInModules();
        }
        return Arrays.asList(ModuleManager.modules);
    }
    
    public static Module getModule(String moduleName) {
        if (moduleName == null) {
            return null;
        }
        for (Module m : getModules()) {
            if (moduleName.equals(m.getName())) {
                return m;
            }
        }
        return null;
    }
}
