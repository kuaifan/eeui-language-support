package com.eeui;

import com.eeui.analytics.Analytics;
import com.eeui.lang.EEUIIcons;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class EEUINewFileAction extends EEUINewFileActionBase {

    public EEUINewFileAction() {
        super("New EEUI File", "Create a new EEUI file", EEUIIcons.LOGO);
    }

    @Override
    protected PsiElement[] doCreate(String name, PsiDirectory directory) {
        Analytics.newEvent().withEventName("create-file").addProperty("name", name).send();
        PsiFile file = this.createFileFromTemplate(directory, name, new String[0]);
        PsiElement child = file.getLastChild();
        return (child != null) ? new PsiElement[]{(PsiElement) file, child} : new PsiElement[]{(PsiElement) file};
    }

    @Override
    protected String getDialogPrompt() {
        return "Create a new EEUI file";
    }

    @Override
    protected String getDialogTitle() {
        return "Create EEUI File";
    }

    protected String getCommandName() {
        return "EEUI File";
    }

    protected String getActionName(PsiDirectory psiDirectory, String s) {
        return "New EEUI File";
    }
}
