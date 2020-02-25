package com.eeui.util;

import com.intellij.notification.*;

public class Logger
{
    public static String NAME;
    private static int LEVEL;
    public static int DEBUG = 3;
    public static int INFO = 2;
    public static int WARN = 1;
    public static int ERROR = 0;
    
    public static void init(String name, int level) {
        Logger.NAME = name;
        Logger.LEVEL = level;
        NotificationsConfiguration.getNotificationsConfiguration().register(Logger.NAME, NotificationDisplayType.NONE);
    }
    
    public static void debug(String text) {
        if (Logger.LEVEL >= 3) {
            Notifications.Bus.notify(new Notification(Logger.NAME, Logger.NAME + " [DEBUG]", redirect(text), NotificationType.INFORMATION));
        }
    }
    
    public static void info(String text) {
        if (Logger.LEVEL >= 2) {
            Notifications.Bus.notify(new Notification(Logger.NAME, Logger.NAME + " [INFO]", redirect(text), NotificationType.INFORMATION));
        }
    }
    
    public static void warn(String text) {
        if (Logger.LEVEL >= 1) {
            Notifications.Bus.notify(new Notification(Logger.NAME, Logger.NAME + " [WARN]", redirect(text), NotificationType.WARNING));
        }
    }
    
    public static void error(String text) {
        if (Logger.LEVEL >= 0) {
            Notifications.Bus.notify(new Notification(Logger.NAME, Logger.NAME + " [ERROR]", redirect(text), NotificationType.ERROR));
        }
    }
    
    private static String redirect(String text) {
        if (Logger.LEVEL == 3) {
            StackTraceElement ste = new Throwable().getStackTrace()[2];
            String prefix = ste.getFileName();
            int lineNum = ste.getLineNumber();
            return "D/EEUI language support: (" + prefix + ":" + lineNum + ") " + text;
        }
        return text;
    }
    
    static {
        Logger.LEVEL = 0;
        init("eeui language support", 2);
    }
}
