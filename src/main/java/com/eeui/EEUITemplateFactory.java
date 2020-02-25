package com.eeui;

import com.eeui.lang.EEUIFileType;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;

public class EEUITemplateFactory {

    public static String NEW_EEUI_TEMPLATE_NAME = "eeui_file";

    public static PsiFile createFromTemplate(PsiDirectory directory, String name, String fileName, @NonNls String... parameters) throws IncorrectOperationException {
        FileTemplate template = FileTemplateManager.getInstance(directory.getProject()).getInternalTemplate(NEW_EEUI_TEMPLATE_NAME);
        String text;
        try {
            text = template.getText();
        } catch (Exception e) {
            throw new RuntimeException("Unable to load template for " + FileTemplateManager.getDefaultInstance().internalTemplateToSubject(NEW_EEUI_TEMPLATE_NAME), e);
        }
        PsiFileFactory factory = PsiFileFactory.getInstance(directory.getProject());
        PsiFile file = factory.createFileFromText(fileName, EEUIFileType.INSTANCE, text);
        CodeStyleManager.getInstance(directory.getProject()).reformat(file);
        return (PsiFile) directory.add(file);
    }
}
