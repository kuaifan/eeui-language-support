package com.eeui.insight;

import com.eeui.insight.bean.Component;
import com.eeui.lint.DirectiveLint;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class ComponentManager
{
    private static Component[] components;
    
    private static void loadBuiltInComponents() {
        InputStream is = DirectiveLint.class.getResourceAsStream("/directives/components.json");
        Gson gson = new Gson();
        ComponentManager.components = gson.fromJson(new InputStreamReader(is), Component[].class);
        if (ComponentManager.components == null) {
            ComponentManager.components = new Component[0];
        }
    }
    
    public static List<Component> getComponents() {
        if (ComponentManager.components == null || ComponentManager.components.length == 0) {
            loadBuiltInComponents();
        }
        return Arrays.asList(ComponentManager.components);
    }
    
    public static Component getComponent(String componentName) {
        if (componentName == null) {
            return null;
        }
        for (Component m : getComponents()) {
            if (componentName.equals(m.getName())) {
                return m;
            }
        }
        return null;
    }
}
