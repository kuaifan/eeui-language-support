package com.eeui.util;

import com.eeui.lint.Attribute;
import com.eeui.lint.DirectiveLint;
import com.eeui.lint.EEUITag;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.xml.*;
import org.apache.http.util.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EEUIFileUtil {

    private static String currentFileName;
    private static JSObjectLiteralExpression cachedExportsStatement;
    private static Map<String, String> vars;
    private static Set<String> functions;

    public static boolean isOnVueFile(PsiElement element) {
        return element.getContainingFile() != null && element.getContainingFile().getName().toLowerCase().endsWith(".vue");
    }

    public static String getParentText(PsiElement position) {
        if (position == null) {
            return "";
        }
        String text = position.getParent().getText();
        if (text.contains("\n")) text = text.substring(0, text.indexOf("\n"));
        //
        Pattern p = Pattern.compile("[a-zA-z_]");
        Matcher m = p.matcher(text.substring(0, 1));
        if (!m.matches()) {
            return "";
        }
        return position.getParent().getText();
    }

    public static boolean checkModuleName(String parenttext, String name) {
        if (TextUtils.isEmpty(parenttext)) {
            return true;
        }
        if (parenttext.contains("\n")) parenttext = parenttext.substring(0, parenttext.indexOf("\n"));
        return parenttext.equals(parenttext.replaceFirst("^" + name + "\\.[a-zA-z_].*\\(", ""));
    }

    public static String getTagName(PsiElement position) {
        int i = 0;
        String text = "";
        while (position != null && !text.startsWith("<") && i < 5) {
            text = position.getText();
            if (text.startsWith("</")) {
                text = "";
            }
            if (!text.startsWith("<")) {
                text = position.getPrevSibling().getText();
                if (text.startsWith("</")) {
                    text = "";
                }
            }
            position = position.getParent();
            i++;
        }
        if (text.startsWith("<")) {
            text = text.substring(1);
        }
        if (text.contains(" ")) {
            text = text.substring(0, text.indexOf(" "));
        }
        return TextUtils.isEmpty(text) ? "" : text.toLowerCase().trim();
    }

    public static String getAttrName(PsiElement position) {
        if (position == null) {
            return "";
        }
        position = position.getParent();
        if (position == null) {
            return "";
        }
        position = position.getParent();
        if (position == null) {
            return "";
        }
        String text = position.getText().trim();
        if (text.contains("\n")) text = text.substring(0, text.indexOf("\n"));
        //
        Pattern p = Pattern.compile("^([a-zA-z_]+)\\s*=");
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : "";
    }

    public static String currentFileHash(PsiElement element) {
        if (element.getContainingFile() != null) {
            String fullPath = element.getContainingFile().getVirtualFile().getCanonicalPath();
            return md5(fullPath);
        }
        return md5(String.valueOf(System.currentTimeMillis()));
    }

    public static String getValueType(XmlAttributeValue value) {
        if (value.getContext() != null && value.getContext() instanceof XmlAttribute) {
            String attrName = ((XmlAttribute)value.getContext()).getName();
            if (value.getContext().getContext() != null && value.getContext().getContext() instanceof XmlTag) {
                String tagName = ((XmlTag)value.getContext().getContext()).getName();
                EEUITag tag = DirectiveLint.getEEUITag(tagName);
                if (tag != null) {
                    Attribute attribute = tag.getAttribute(attrName);
                    if (attribute != null) {
                        return attribute.valueType;
                    }
                }
            }
        }
        return "var";
    }

    public static String getJSPropertyType(JSProperty jsProperty) {
        JSType t = jsProperty.getType();
        String typeString = "var";
        if (t == null) {
            if (jsProperty.getValue() instanceof JSObjectLiteralExpression) {
                typeString = "Object";
            }
        }
        else {
            typeString = t.getResolvedTypeText();
        }
        return typeString;
    }

    public static boolean hasSameType(XmlAttributeValue value, JSProperty property) {
        String valueType = getValueType(value);
        String JsType = getJSPropertyType(property);
        return hasSameType(valueType, JsType);
    }

    public static boolean hasSameType(String value, String property) {
        return value.toLowerCase().equals(property.toLowerCase()) || value.equals("var");
    }

    private static void ensureFile(PsiElement element) {
        String path = String.valueOf(System.currentTimeMillis());
        if (element != null && element.getContainingFile() != null && element.getContainingFile().getVirtualFile() != null) {
            path = element.getContainingFile().getVirtualFile().getPath();
        }
        if (!EEUIFileUtil.currentFileName.equals(path)) {
            EEUIFileUtil.cachedExportsStatement = null;
            EEUIFileUtil.vars.clear();
            EEUIFileUtil.functions.clear();
            EEUIFileUtil.currentFileName = path;
        }
    }

    public static JSObjectLiteralExpression getExportsStatement(PsiElement anyElementOnEEUIScript) {
        ensureFile(anyElementOnEEUIScript);
        if (isValid(EEUIFileUtil.cachedExportsStatement)) {
            return EEUIFileUtil.cachedExportsStatement;
        }
        PsiFile file = anyElementOnEEUIScript.getContainingFile();
        if (file instanceof XmlFile) {
            XmlDocument document = ((XmlFile)file).getDocument();
            if (document != null) {
                for (PsiElement e : document.getChildren()) {
                    if (e instanceof XmlTag && "script".equals(((XmlTag)e).getName())) {
                        for (PsiElement e2 : e.getChildren()) {
                            if (e2 instanceof JSEmbeddedContent) {
                                for (PsiElement e3 : e2.getChildren()) {
                                    if (e3 instanceof JSExpressionStatement) {
                                        for (PsiElement e4 : e3.getChildren()) {
                                            if (e4 instanceof JSAssignmentExpression) {
                                                PsiElement[] children = e4.getChildren();
                                                if (children.length == 2 && children[0].getText().equals("module.exports") && children[1] instanceof JSObjectLiteralExpression) {
                                                    EEUIFileUtil.cachedExportsStatement = (JSObjectLiteralExpression)children[1];
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return EEUIFileUtil.cachedExportsStatement;
    }

    private static boolean isValid(JSObjectLiteralExpression expression) {
        if (expression == null) {
            return false;
        }
        try {
            PsiFile file = expression.getContainingFile();
            if (file == null) {
                return false;
            }
        }
        catch (PsiInvalidElementAccessException e) {
            return false;
        }
        JSProperty data = expression.findProperty("data");
        return data != null && data.getValue() != null;
    }

    public static Map<String, String> getAllVarNames(PsiElement any) {
        ensureFile(any);
        getVarDeclaration(any, String.valueOf(System.currentTimeMillis()));
        return EEUIFileUtil.vars;
    }

    public static Set<String> getAllFunctionNames(PsiElement any) {
        ensureFile(any);
        getFunctionDeclaration(any, String.valueOf(System.currentTimeMillis()));
        return EEUIFileUtil.functions;
    }

    public static JSProperty getVarDeclaration(PsiElement anyElementOnEEUIScript, String valueName) {
        valueName = CodeUtil.getVarNameFromMustache(valueName);
        JSObjectLiteralExpression exports = getExportsStatement(anyElementOnEEUIScript);
        EEUIFileUtil.vars.clear();
        JSProperty ret = null;
        if (exports != null) {
            try {
                PsiFile file = exports.getContainingFile();
                if (file == null) {
                    return null;
                }
            }
            catch (PsiInvalidElementAccessException e) {
                return null;
            }
            JSProperty data = exports.findProperty("data");
            if (data == null || data.getValue() == null) {
                return null;
            }
            for (PsiElement pe : data.getValue().getChildren()) {
                if (pe instanceof JSProperty) {
                    String varName = ((JSProperty)pe).getName();
                    String varValue = getJSPropertyType((JSProperty)pe);
                    if (varName != null && varValue != null) {
                        EEUIFileUtil.vars.put(varName, varValue);
                    }
                    if (valueName.equals(varName)) {
                        ret = (JSProperty)pe;
                    }
                }
            }
        }
        return ret;
    }

    public static JSFunctionExpression getFunctionDeclaration(PsiElement anyElementOnEEUIScript, String valueName) {
        valueName = CodeUtil.getFunctionNameFromMustache(valueName);
        JSObjectLiteralExpression exports = getExportsStatement(anyElementOnEEUIScript);
        EEUIFileUtil.functions.clear();
        JSFunctionExpression ret = null;
        if (exports != null) {
            try {
                PsiFile file = exports.getContainingFile();
                if (file == null) {
                    return null;
                }
            }
            catch (PsiInvalidElementAccessException e3) {
                return null;
            }
            JSProperty data = exports.findProperty("methods");
            if (data != null && data.getValue() != null) {
                for (PsiElement e : data.getValue().getChildren()) {
                    if (e instanceof JSProperty) {
                        for (PsiElement e2 : e.getChildren()) {
                            if (e2 instanceof JSFunctionExpression) {
                                EEUIFileUtil.functions.add(((JSFunctionExpression)e2).getName());
                                if (valueName.equals(((JSFunctionExpression)e2).getName())) {
                                    ret = (JSFunctionExpression)e2;
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static int getExportsEndOffset(PsiElement anyElementOnEEUIScript, String name) {
        JSObjectLiteralExpression exports = getExportsStatement(anyElementOnEEUIScript);
        if (exports == null) {
            return -1;
        }
        try {
            PsiFile file = exports.getContainingFile();
            if (file == null) {
                return -1;
            }
        }
        catch (PsiInvalidElementAccessException e) {
            return -1;
        }
        JSProperty data = exports.findProperty(name);
        if (data == null || data.getValue() == null) {
            return -1;
        }
        return data.getValue().getTextRange().getEndOffset() - 1;
    }

    public static boolean isMustacheValue(String value) {
        return value != null && Pattern.compile("\\{\\{.+?\\}\\}").matcher(value).matches();
    }

    public static boolean containsMustacheValue(String value) {
        return value != null && Pattern.compile(".*\\{\\{.+?\\}\\}.*").matcher(value).matches();
    }

    public static Map<String, TextRange> getVars(String src) {
        Map<String, TextRange> results = new IdentityHashMap<String, TextRange>();
        Pattern p = Pattern.compile("\\{\\{.+?\\}\\}");
        Matcher m = p.matcher(src);
        while (m.find()) {
            String g = m.group().replaceAll("\\{+", "").replaceAll("\\}+", "").trim();
            TextRange textRange = new TextRange(m.start(), m.end());
            results.put(g, textRange);
        }
        return results;
    }

    public static int compareVersion(String version1, String version2) {
        String[] versionArray1 = version1.split("\\.");
        String[] versionArray2 = version2.split("\\.");
        int idx = 0;
        int minLength = Math.min(versionArray1.length, versionArray2.length);
        int diff = 0;
        while (idx < minLength && (diff = versionArray1[idx].length() - versionArray2[idx].length()) == 0 && (diff = versionArray1[idx].compareTo(versionArray2[idx])) == 0) {
            ++idx;
        }
        diff = ((diff != 0) ? diff : (versionArray1.length - versionArray2.length));
        return diff;
    }

    public static String md5(String string) {
        if (string == null) {
            return null;
        }
        char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        byte[] btInput = string.getBytes();
        try {
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xF];
                str[k++] = hexDigits[byte0 & 0xF];
            }
            return new String(str);
        }
        catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static Map<String, String> getAllTagDesc(PsiFile file) {
        Map<String, String> result = new HashMap<String, String>();
        if (file instanceof XmlFile) {
            PsiElement firstChild = (PsiElement)((XmlFile)file).getDocument();
            if (firstChild != null) {
                XmlTag rootTag = ((XmlDocument)firstChild).getRootTag();
                if (rootTag != null && "template".equals(rootTag.getName())) {
                    traverse(rootTag, result);
                }
            }
        }
        return result;
    }

    public static List<String> getAllAttrNames(XmlTag xmlTag) {
        List<String> result = new ArrayList<String>();
        for (XmlAttribute attribute : xmlTag.getAttributes()) {
            result.add(attribute.getName());
        }
        return result;
    }

    private static void traverse(XmlTag root, Map<String, String> ret) {
        String key = "ref";
        XmlTag[] subTags = root.getSubTags();
        for (XmlTag xmlTag : subTags) {
            String ref = xmlTag.getAttributeValue(key);
            if (ref != null && !ref.isEmpty()) {
                ret.put(ref, xmlTag.getName());
            }
            if (xmlTag.getSubTags().length > 0) {
                traverse(xmlTag, ret);
            }
        }
    }

    static {
        EEUIFileUtil.currentFileName = "";
        EEUIFileUtil.vars = new ConcurrentHashMap<String, String>();
        EEUIFileUtil.functions = new HashSet<String>();
    }
}
