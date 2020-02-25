package com.eeui.insight;

import com.eeui.insight.bean.Variable;
import com.eeui.lint.DirectiveLint;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class VariableManager
{
    private static Variable[] variables;
    
    private static void loadBuiltInVariables() {
        InputStream is = DirectiveLint.class.getResourceAsStream("/directives/variables.json");
        Gson gson = new Gson();
        VariableManager.variables = gson.fromJson(new InputStreamReader(is), Variable[].class);
        if (VariableManager.variables == null) {
            VariableManager.variables = new Variable[0];
        }
    }
    
    public static List<Variable> getVariables() {
        if (VariableManager.variables == null || VariableManager.variables.length == 0) {
            loadBuiltInVariables();
        }
        return Arrays.asList(VariableManager.variables);
    }
    
    public static Variable getVariable(String variableName) {
        if (variableName == null) {
            return null;
        }
        for (Variable m : getVariables()) {
            if (variableName.equals(m.getName())) {
                return m;
            }
        }
        return null;
    }
}
