

package com.eeui.util;

import com.eeui.lang.EEUIIcons;
import com.eeui.lint.Attribute;
import com.eeui.lint.EEUITag;
import com.intellij.codeInsight.completion.XmlAttributeInsertHandler;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.file.PsiDirectoryFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExtraModulesUtil
{
    public static List<EEUITag> getTagsFromNodeModules() {
        List<EEUITag> list = new ArrayList<>();
        Project project = ProjectUtil.guessCurrentProject(null);
        Logger.debug(project.toString());
        VirtualFile vf = project.getProjectFile();
        PsiDirectory[] localModules = new PsiDirectory[0];
        if (vf != null && vf.isDirectory()) {
            Logger.debug("Project root dir: " + vf.toString());
            PsiDirectory dir = PsiDirectoryFactory.getInstance(project).createDirectory(vf);
            PsiDirectory[] nodeModules;
            localModules = (nodeModules = getNodeModules(dir));
            for (PsiDirectory directory : nodeModules) {
                List<PsiFile> comps = getComponents(directory, getMain(directory));
                String homePage = getHomePage(directory);
                for (PsiFile comp : comps) {
                    EEUITag tag = parseToTag(comp);
                    tag.document = homePage;
                    list.add(tag);
                }
            }
        }
        else {
            Logger.debug("Project base dir is null");
        }
        for (PsiDirectory dir2 : getGlobalModules(localModules)) {
            List<PsiFile> comps2 = getComponents(dir2, getMain(dir2));
            String homePage2 = getHomePage(dir2);
            Logger.debug(comps2.toString());
            for (PsiFile comp2 : comps2) {
                EEUITag tag2 = parseToTag(comp2);
                tag2.document = homePage2;
                list.add(tag2);
            }
        }
        return list;
    }
    
    private static String getHomePage(PsiDirectory directory) {
        PsiFile pkg = directory.findFile("package.json");
        if (pkg instanceof JsonFile && ((JsonFile) pkg).getTopLevelValue() instanceof JsonObject) {
            JsonObject object = (JsonObject)((JsonFile)pkg).getTopLevelValue();
            if (object != null) {
                JsonProperty homePage = object.findProperty("homepage");
                if (homePage != null && homePage.getValue() != null && homePage.getValue() instanceof JsonStringLiteral) {
                    JsonStringLiteral propValue = (JsonStringLiteral)homePage.getValue();
                    return propValue.getValue();
                }
            }
        }
        return null;
    }
    
    private static List<PsiDirectory> getGlobalModules(PsiDirectory[] localModules) {
        if (localModules == null) {
            localModules = new PsiDirectory[0];
        }
        List<PsiDirectory> result = new ArrayList<>();
        List<String> globals = SettingsUtil.getGlobalModules();
        List<String> locals = new ArrayList<>();
        for (PsiDirectory dir : localModules) {
            if (dir != null) {
                locals.add(dir.getVirtualFile().getCanonicalPath());
            }
        }
        for (String global : globals) {
            if (!locals.contains(global)) {
                VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByPath(global);
                if (vf == null || !vf.isDirectory()) {
                    continue;
                }
                PsiDirectory dir = PsiDirectoryFactory.getInstance(ProjectUtil.guessCurrentProject(null)).createDirectory(vf);
                result.add(dir);
            }
            else {
                Logger.info("Module " + global + " already exists locally, skip it.");
            }
        }
        Logger.debug(result.toString());
        return result;
    }
    
    private static EEUITag parseToTag(PsiFile comp) {
        EEUITag eeuiTag = new EEUITag();
        eeuiTag.tag = comp.getContainingFile().getName().replace(".we", "");
        eeuiTag.attrs = new CopyOnWriteArrayList<>();
        eeuiTag.declare = comp;
        Map<String, String> vars = EEUIFileUtil.getAllVarNames(comp);
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            Attribute attribute = new Attribute();
            attribute.name = convertAttrName(entry.getKey());
            attribute.valueType = getType(entry.getValue());
            eeuiTag.attrs.add(attribute);
        }
        return eeuiTag;
    }
    
    private static String convertAttrName(String name) {
        char[] chars = name.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            if (Character.isUpperCase(c)) {
                if (sb.length() == 0) {
                    sb.append(Character.toLowerCase(c));
                }
                else {
                    sb.append('-').append(Character.toLowerCase(c));
                }
            }
            else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    private static String getType(String realType) {
        realType = realType.toLowerCase();
        if ("number".equals(realType) || "boolean".equals(realType) || "array".equals(realType)) {
            return realType.toLowerCase();
        }
        return "var";
    }
    
    private static PsiDirectory[] getNodeModules(PsiDirectory root) {
        PsiDirectory node_modules = root.findSubdirectory("node_modules");
        if (node_modules != null) {
            return node_modules.getSubdirectories();
        }
        return new PsiDirectory[0];
    }
    
    private static PsiFile getMain(PsiDirectory moduleRoot) {
        PsiFile pkg = moduleRoot.findFile("package.json");
        if (pkg instanceof JsonFile && ((JsonFile) pkg).getTopLevelValue() instanceof JsonObject) {
            JsonObject object = (JsonObject)((JsonFile)pkg).getTopLevelValue();
            if (object != null) {
                JsonProperty property = object.findProperty("main");
                if (property != null && property.getValue() != null && property.getValue() instanceof JsonStringLiteral) {
                    JsonStringLiteral propValue = (JsonStringLiteral)property.getValue();
                    String value = propValue.getValue();
                    return moduleRoot.findFile(value.replace("./", ""));
                }
            }
        }
        return null;
    }
    
    private static List<PsiFile> getComponents(PsiDirectory root, PsiFile file) {
        List<PsiFile> results = new ArrayList<>();
        if (file instanceof JSFile) {
            for (PsiElement element : file.getChildren()) {
                if (element instanceof JSExpressionStatement) {
                    JSExpression expression = ((JSExpressionStatement)element).getExpression();
                    if (expression instanceof JSCallExpression && ((JSCallExpression)expression).getArguments().length == 1 && ((JSCallExpression)expression).getArguments()[0] instanceof JSLiteralExpression) {
                        JSLiteralExpression expression2 = (JSLiteralExpression)((JSCallExpression)expression).getArguments()[0];
                        Object val = expression2.getValue();
                        if (val != null) {
                            String[] temp = val.toString().replace("./", "").split("/");
                            if (temp.length > 0) {
                                String fileName = temp[temp.length - 1];
                                if (fileName.toLowerCase().endsWith(".we")) {
                                    PsiDirectory start = root;
                                    for (int i = 0; i < temp.length - 1; ++i) {
                                        PsiDirectory dir = start.findSubdirectory(temp[i]);
                                        if (dir != null) {
                                            start = dir;
                                        }
                                    }
                                    PsiFile eeuiScript = start.findFile(temp[temp.length - 1]);
                                    if (eeuiScript != null) {
                                        results.add(eeuiScript);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    public static boolean isEEUIProject() {
        Project project = ProjectUtil.guessCurrentProject(null);
        if (project.getBasePath() != null) {
            VirtualFile vf = LocalFileSystem.getInstance().findFileByPath(project.getBasePath());
            if (vf != null && vf.isDirectory()) {
                PsiDirectory dir = PsiDirectoryFactory.getInstance(project).createDirectory(vf);
                PsiFile pkg = dir.findFile("eeui.config.js");
                return pkg instanceof JSFile;
            }
        }
        return false;
    }
    
    public static boolean isNodeModule(PsiDirectory dir) {
        PsiFile pkg = dir.findFile("package.json");
        return pkg instanceof JsonFile;
    }
    
    public static String getModuleName(PsiDirectory dir) {
        PsiFile pkg = dir.findFile("package.json");
        String name = dir.getName();
        if (pkg instanceof JsonFile && ((JsonFile) pkg).getTopLevelValue() instanceof JsonObject) {
            JsonObject object = (JsonObject)((JsonFile)pkg).getTopLevelValue();
            if (object != null) {
                JsonProperty property = object.findProperty("name");
                JsonProperty property2 = object.findProperty("version");
                if (property != null && property.getValue() != null && property.getValue() instanceof JsonStringLiteral) {
                    JsonStringLiteral propValue = (JsonStringLiteral)property.getValue();
                    name = propValue.getValue();
                    if (property2 != null && property2.getValue() != null && property2.getValue() instanceof JsonStringLiteral) {
                        JsonStringLiteral propValue2 = (JsonStringLiteral)property2.getValue();
                        name = name + ":" + propValue2.getValue();
                    }
                }
            }
        }
        return name;
    }
}
