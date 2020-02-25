

package com.eeui;

import com.eeui.lang.EEUIFileType;
import com.intellij.CommonBundle;
import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class EEUINewFileActionBase extends CreateElementActionBase {
    public EEUINewFileActionBase(String text, String description, Icon icon) {
        super(text, description, icon);
    }

    protected PsiElement[] invokeDialog(Project project, PsiDirectory psiDirectory) {
        MyInputValidator inputValidator = new MyInputValidator(project, psiDirectory);
        Messages.showInputDialog(project, this.getDialogPrompt(), this.getDialogTitle(), null, "", inputValidator);
        return inputValidator.getCreatedElements();
    }

    protected PsiElement[] create(@NotNull String s, PsiDirectory psiDirectory) {
        return this.doCreate(s, psiDirectory);
    }

    protected String getErrorTitle() {
        return CommonBundle.getErrorTitle();
    }

    protected PsiFile createFileFromTemplate(PsiDirectory directory, String className, @NonNls String... parameters) throws IncorrectOperationException {
        String ext = "." + EEUIFileType.INSTANCE.getDefaultExtension();
        String filename = className.endsWith(ext) ? className : (className + ext);
        return EEUITemplateFactory.createFromTemplate(directory, className, filename, parameters);
    }

    protected abstract PsiElement[] doCreate(String p0, PsiDirectory p1);

    protected abstract String getDialogPrompt();

    protected abstract String getDialogTitle();
}
