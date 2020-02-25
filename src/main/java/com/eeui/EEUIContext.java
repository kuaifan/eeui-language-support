package com.eeui;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class EEUIContext extends TemplateContextType {
    protected EEUIContext() {
        super("JAVASCRIPT", "vue");
    }

    @Override
    public boolean isInContext(@NotNull PsiFile psiFile, int offset) {
        return psiFile.getName().endsWith(".vue");
    }
}
