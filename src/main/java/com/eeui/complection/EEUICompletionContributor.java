package com.eeui.complection;

import com.eeui.analytics.Analytics;
import com.eeui.insight.ComponentManager;
import com.eeui.insight.bean.Component;
import com.eeui.lang.EEUIIcons;
import com.eeui.util.EEUIFileUtil;
import com.eeui.util.ExtraModulesUtil;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class EEUICompletionContributor extends CompletionContributor {

    public EEUICompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(PsiElement.class), new CompletionProvider<CompletionParameters>() {
            protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet resultSet) {
                if (!EEUIFileUtil.isOnVueFile(completionParameters.getPosition())) {
                    return;
                }
                if (!ExtraModulesUtil.isEEUIProject()) {
                    return;
                }
                //
                PsiElement context = completionParameters.getPosition().getContext();
                if (context instanceof XmlAttributeValue) {
                    //值
                    String tagName = EEUIFileUtil.getTagName(completionParameters.getOriginalPosition());
                    Component mComponent = ComponentManager.getComponent(tagName);
                    if (mComponent != null) {
                        String attrName = EEUIFileUtil.getAttrName(completionParameters.getOriginalPosition());
                        Component.AttrsBean mAttrsBean = mComponent.getAttr(attrName);
                        if (mAttrsBean != null) {
                            for (Component.AttrsBean.OptionBean mOptionBean : mAttrsBean.getOption())
                            resultSet.addElement(LookupElementBuilder.
                                    create(mOptionBean.getValue())
                                    .withIcon(EEUIIcons.LOGO)
                                    .withBoldness(true)
                                    .withInsertHandler(new StatisticsInsertHandler((XmlAttributeValue) context))
                                    .withTypeText(mOptionBean.getDescDef(attrName + " value")));
                        }
                    }
                } else if (context != null && context.toString().contains("XML_ATTRIBUTE")) {
                    //键
                    String tagName = EEUIFileUtil.getTagName(completionParameters.getOriginalPosition());
                    Component mComponent = ComponentManager.getComponent(tagName);
                    if (mComponent != null) {
                        for (Component.AttrsBean mAttrsBean : mComponent.getAttrs()) {
                            resultSet.addElement(LookupElementBuilder.
                                    create(mAttrsBean.getName())
                                    .withIcon(EEUIIcons.LOGO)
                                    .withBoldness(true)
                                    .withInsertHandler(XmlAttributeInsertHandler.INSTANCE)
                                    .withTypeText(mAttrsBean.getDescDef(tagName + " attribute")));
                        }
                    }
                }
            }
        });
    }

    private static class StatisticsInsertHandler implements InsertHandler<LookupElement>
    {
        private XmlAttributeValue context;

        public StatisticsInsertHandler(XmlAttributeValue context) {
            this.context = context;
        }

        public void handleInsert(@NotNull InsertionContext insertionContext, @NotNull LookupElement lookupElement) {
            performInsert(context, insertionContext, lookupElement);
        }
    }

    private static void performInsert(XmlAttributeValue value, InsertionContext insertionContext, LookupElement lookupElement) {
        Analytics.newImmediateEvent("insert-attr-value");
        if (value.getText().startsWith("\"")) {
            insertionContext.getDocument().replaceString(value.getTextOffset(), value.getTextOffset() + getTailLength(value) + lookupElement.getLookupString().length(), lookupElement.getLookupString());
        } else {
            insertionContext.getDocument().replaceString(value.getTextOffset() - 1, value.getTextOffset() + getTailLength(value) + lookupElement.getLookupString().length() - 1, "\"" + lookupElement.getLookupString() + "\"");
        }
    }

    private static int getTailLength(XmlAttributeValue value) {
        String[] temp = value.getValue().split("IntellijIdeaRulezzz ");
        return temp.length == 2 ? temp[1].length() : 0;
    }
}
