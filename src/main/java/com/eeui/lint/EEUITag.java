package com.eeui.lint;

import com.intellij.psi.PsiFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class EEUITag
{
    public String tag;
    public CopyOnWriteArrayList<Attribute> attrs;
    public CopyOnWriteArrayList<Event> events;
    public List<String> parents;
    public List<String> childes;
    public float since;
    public PsiFile declare;
    public String document;

    public EEUITag() {
        this.since = 0.0f;
    }
    
    public Set<String> getAttributeNames() {
        Set<String> ret = new HashSet<>();
        if (this.attrs != null) {
            for (Attribute attr : this.attrs) {
                ret.add(attr.name);
            }
        }
        return ret;
    }
    
    public Attribute getAttribute(String name) {
        if (this.attrs != null) {
            for (Attribute attr : this.attrs) {
                if (name.equals(attr.name)) {
                    return attr;
                }
            }
        }
        return null;
    }
    
    public Event getEvent(String name) {
        if (this.events != null) {
            for (Event event : this.events) {
                if (name.equals(event.name)) {
                    return event;
                }
            }
        }
        return null;
    }
    
    public Set<String> getEventNames() {
        Set<String> ret = new HashSet<>();
        if (this.events != null) {
            for (Event event : this.events) {
                ret.add(event.name);
            }
        }
        return ret;
    }
    
    @Override
    public String toString() {
        String file = "null";
        if (this.declare != null) {
            file = this.declare.getOriginalFile().getVirtualFile().getName();
        }
        return "{" + this.tag + "} : " + file;
    }
}
