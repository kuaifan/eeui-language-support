package com.eeui.analytics;

import com.eeui.util.EEUIFileUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.psi.PsiElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Analytics
{
    public static String COMMON_SESSION_ID;
    private static ExecutorService executor;
    private static List<JsonObject> paddingEvents;
    private static JsonObject client;
    private static String currentSession;
    
    public static void putEvent(PsiElement any, String eventName, String attrName, String attrValue) {
        String sessionId = EEUIFileUtil.currentFileHash(any);
        putEvent(sessionId, eventName, attrName, attrValue);
    }
    
    public static void putEvent(String sessionId, String eventName, String attrName, String attrValue) {
        Map<String, String> attrs = new HashMap<String, String>(1);
        attrs.put(attrName, attrValue);
        putEvent(sessionId, eventName, attrs);
    }
    
    public static void putEvent(String eventName, String attrName, String attrValue) {
        putEvent(Analytics.COMMON_SESSION_ID, eventName, attrName, attrValue);
    }
    
    public static void putEvent(String eventName, Map<String, String> attrs) {
        putEvent(Analytics.COMMON_SESSION_ID, eventName, attrs);
    }
    
    public static void putEvent(String eventName) {
        putEvent(Analytics.COMMON_SESSION_ID, eventName, new HashMap<String, String>(0));
    }
    
    public static void putEvent(PsiElement any, String eventName) {
        String sessionId = EEUIFileUtil.currentFileHash(any);
        putEvent(sessionId, eventName, new HashMap<String, String>(0));
    }
    
    public static void putEvent(String sessionId, String eventName, Map<String, String> attributes) {
        if (Analytics.currentSession == null || sessionId.equals(Analytics.currentSession)) {
            Analytics.currentSession = sessionId;
            JsonObject event = new JsonObject();
            event.addProperty("event", eventName);
            JsonObject attrs = new JsonObject();
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                attrs.addProperty((String)entry.getKey(), (String)entry.getValue());
            }
            event.add("attributes", (JsonElement)attrs);
            Analytics.paddingEvents.add(event);
        }
        else {
            sendEvent(new Runnable() {
                @Override
                public void run() {
                    Analytics.currentSession = null;
                    Analytics.paddingEvents.clear();
                    Analytics.putEvent(sessionId, eventName, attributes);
                }
            });
        }
    }
    
    public static void submitSync() {
        sendEvent(generateEvent(), new Runnable() {
            @Override
            public void run() {
                Analytics.currentSession = null;
                Analytics.paddingEvents.clear();
            }
        }, true);
    }
    
    private static void sendEvent(Runnable callback) {
        sendEvent(generateEvent(), callback, false);
    }
    
    private static void sendEvent(String content, Runnable callback, boolean sync) {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                CloseableHttpClient client = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost("https://api.leancloud.cn/1.1/stats/open/collect");
                httpPost.addHeader("Content-Type", "application/json");
                httpPost.addHeader("X-LC-Id", "ynVqz1tCDmyXd6ufTWxuOsEr-gzGzoHsz");
                httpPost.addHeader("X-LC-Key", "79JYs37OwxAa55Vs93HzygJE");
                httpPost.setEntity((HttpEntity)new StringEntity(content, Charset.forName("UTF-8")));
                try {
                    HttpResponse response = (HttpResponse)client.execute((HttpUriRequest)httpPost);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                callback.run();
            }
        };
        if (sync) {
            action.run();
        }
        else {
            Analytics.executor.submit(action);
        }
    }
    
    private static String generateEvent() {
        return generateEvent(Analytics.currentSession);
    }
    
    private static String generateEvent(String sessionId) {
        JsonObject object = new JsonObject();
        JsonObject session = new JsonObject();
        session.addProperty("id", sessionId);
        JsonArray events = new JsonArray();
        for (JsonObject event : Analytics.paddingEvents) {
            events.add((JsonElement)event);
        }
        object.add("client", (JsonElement)Analytics.client);
        object.add("session", (JsonElement)session);
        object.add("events", (JsonElement)events);
        return object.toString();
    }
    
    private static String getMachineId() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; ++i) {
                sb.append(String.format("%02X%s", mac[i], ""));
            }
            return EEUIFileUtil.md5(sb.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
            return "unknown machine";
        }
    }

    private static float getTargetVersion() {
        String v = PropertiesComponent.getInstance().getValue("EEUI_TARGET_VERSION", String.valueOf(Float.MAX_VALUE));
        try {
            return Float.parseFloat(v);
        }
        catch (Throwable t) {
            return Float.MAX_VALUE;
        }
    }
    
    private static JsonObject fillClient() {
        JsonObject object = new JsonObject();
        object.addProperty("id", getMachineId());
        object.addProperty("platform", System.getProperty("os.name", "unknown OS"));
        object.addProperty("app_version", String.valueOf(getTargetVersion()));
        object.addProperty("app_channel", "plugin");
        return object;
    }
    
    public static Event newEvent() {
        return new Event();
    }
    
    public static void newImmediateEvent(String name) {
        new Event().withEventName(name).send();
    }
    
    static {
        COMMON_SESSION_ID = EEUIFileUtil.md5("excited");
        Analytics.executor = Executors.newSingleThreadExecutor();
        Analytics.paddingEvents = new CopyOnWriteArrayList<JsonObject>();
        Analytics.client = fillClient();
    }
    
    public static class Event
    {
        private JsonObject session;
        private JsonObject event;
        
        public Event() {
            this.session = new JsonObject();
            this.event = new JsonObject();
            this.session.addProperty("id", EEUIFileUtil.md5(String.valueOf(System.currentTimeMillis())));
        }
        
        public Event withSessionId(String sessionId) {
            this.session.addProperty("id", sessionId);
            return this;
        }
        
        public Event withEventName(String name) {
            this.event.addProperty("event", name);
            return this;
        }
        
        public Event addProperty(String key, String value) {
            JsonElement attrs = this.event.get("attributes");
            if (attrs == null) {
                attrs = (JsonElement)new JsonObject();
                this.event.add("attributes", attrs);
            }
            if (attrs instanceof JsonObject) {
                ((JsonObject)attrs).addProperty(key, value);
            }
            return this;
        }
        
        public void send() {
            JsonObject object = new JsonObject();
            object.add("client", (JsonElement)Analytics.client);
            object.add("session", (JsonElement)this.session);
            JsonArray array = new JsonArray();
            array.add((JsonElement)this.event);
            object.add("events", (JsonElement)array);
            sendEvent(object.toString(), new Runnable() {
                @Override
                public void run() {
                }
            }, false);
        }
    }
}
