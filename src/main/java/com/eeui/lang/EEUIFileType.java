package com.eeui.lang;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class EEUIFileType extends LanguageFileType {

    public static EEUIFileType INSTANCE;

    private EEUIFileType() {
        super(EEUILanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "eeui file";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "eeui language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "vue";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return EEUIIcons.LOGO;
    }

    static {
        INSTANCE = new EEUIFileType();
    }
}
