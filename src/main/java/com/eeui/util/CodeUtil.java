package com.eeui.util;

import java.util.List;
import java.util.regex.Pattern;

public class CodeUtil
{
    public static String getVarNameFromMustache(String raw) {
        raw = raw.replaceAll("\\{+", "").replaceAll("\\}+", "").trim();
        if (raw.contains(" in ")) {
            String[] tmp = raw.split("\\s+in\\s+");
            if (tmp.length == 2) {
                raw = tmp[1].trim();
            }
        }
        else if (raw.contains(".") || raw.contains("[")) {
            int dot = (raw.indexOf(46) == -1) ? raw.length() : raw.indexOf(46);
            int bracket = (raw.indexOf(91) == -1) ? raw.length() : raw.indexOf(91);
            int index = Math.min(dot, bracket);
            raw = raw.substring(0, index);
        }
        return raw;
    }
    
    public static String getFunctionNameFromMustache(String raw) {
        return raw.replaceAll("\\{+", "").replaceAll("\\}+", "").replaceAll("\\(.*\\)", "").trim();
    }
    
    public static String guessStringType(String valueString) {
        if ("null".equals(valueString)) {
            return "null";
        }
        if (valueString.startsWith("\"") && valueString.endsWith("\"")) {
            valueString = valueString.substring(1, valueString.length() - 1);
        }
        if (valueString.startsWith("'") && valueString.endsWith("'")) {
            valueString = valueString.substring(1, valueString.length() - 1);
        }
        if (Pattern.compile("(true|false)").matcher(valueString).matches()) {
            return "Boolean";
        }
        if (Pattern.compile("\\d*\\.?\\d+").matcher(valueString).matches()) {
            return "Number";
        }
        return "String";
    }
    
    public static boolean maybeInLineExpression(String s) {
        if (s == null) {
            return false;
        }
        String[] array;
        String[] operators = array = new String[] { "+", "-", "*", "/", "(", ")", "=", "!", "[", "]", ".", "|", "&" };
        for (String op : array) {
            if (s.contains(op)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean maybeBooleanExpression(String s) {
        if (s == null) {
            return false;
        }
        String[] array;
        String[] operators = array = new String[] { "?", "==", "!=", ":", ">", "<", "|", "&", "!" };
        for (String op : array) {
            if (s.contains(op)) {
                return true;
            }
        }
        return false;
    }

    public static String createTable(String[] theads, List<String[]> tbodys) {
        StringBuilder tmpHtml = new StringBuilder("<table border='0' cellpadding='0' cellspacing='0'>");
        tmpHtml.append("<thead>");
        tmpHtml.append("<tr style='border-left:1px solid #dfe2e5;border-bottom:1px solid #dfe2e5;'>");
        for (String name : theads) {
            tmpHtml.append("<th style='border-right:1px solid #dfe2e5;border-top:1px solid #dfe2e5;margin:0px;padding:5px 8px;'>").append(name).append("</th>");
        }
        tmpHtml.append("</tr>");
        tmpHtml.append("</thead>");
        tmpHtml.append("<tbody>");
        for (String[] bodys : tbodys) {
            tmpHtml.append("<tr style='border-left:1px solid #dfe2e5;border-bottom:1px solid #dfe2e5;'>");
            for (String text : bodys) {
                tmpHtml.append("<td style='border-right:1px solid #dfe2e5;margin:0px;padding:5px 8px;white-space:nowrap;'>")
                        .append(text)
                        .append("</td>");
            }
            tmpHtml.append("</tr>");
        }
        tmpHtml.append("</tbody>");
        tmpHtml.append("</table>");
        return tmpHtml.toString();
    }
}
