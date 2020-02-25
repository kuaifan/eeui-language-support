package com.eeui.document;

import com.eeui.insight.bean.Component;
import com.eeui.util.CodeUtil;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.eeui.insight.ComponentManager.getComponent;

public class DocumentProvider extends AbstractDocumentationProvider {
    @Override
    @Nullable
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        if (element instanceof IProperty) {
            return "\"" + renderPropertyValue((IProperty) element) + "\"" + getLocationString(element);
        }
        return null;
    }

    private static String getLocationString(PsiElement element) {
        PsiFile file = element.getContainingFile();
        return file != null ? " [" + file.getName() + "]" : "";
    }

    @NotNull
    private static String renderPropertyValue(IProperty prop) {
        String raw = prop.getValue();
        if (raw == null) {
            return "<i>empty</i>";
        }
        return StringUtil.escapeXml(raw);
    }

    @Override
    public String generateDoc(final PsiElement element, @Nullable final PsiElement originalElement) {
        String text = null;
        if (originalElement != null) {
            text = originalElement.getText();
        }

        if (null != text) {
            Component mComponent = getComponent(text);
            String docHtml = "";
            if (mComponent != null) {
                if (!TextUtils.isEmpty(mComponent.getUrl())) {
                    docHtml += "<a href='" + mComponent.getUrl() + "' target='_blank'>" + mComponent.getUrl() + "</a>";
                }
                if (mComponent.getAttrs().size() > 0) {
                    List<String[]> tbodys = new ArrayList<>();
                    for (Component.AttrsBean mAttrsBean : mComponent.getAttrs()) {
                        tbodys.add(new String[]{mAttrsBean.getName(), mAttrsBean.getType(), mAttrsBean.getDesc(), mAttrsBean.getDefval()});
                    }
                    if (tbodys.size() > 0) {
                        docHtml += CodeUtil.createTable(new String[]{"属性名", "类型", "描述", "默认值"}, tbodys);
                    }
                }
            }
            if (TextUtils.isEmpty(docHtml)) {
                return null;
            } else {
                return docHtml;
            }
        }
        return null;
    }
}
