package com.eeui.lang;

import com.intellij.lang.Language;

public class EEUILanguage extends Language {

    public static EEUILanguage INSTANCE = new EEUILanguage();

    private EEUILanguage() {
        super("eeui");
    }
}
