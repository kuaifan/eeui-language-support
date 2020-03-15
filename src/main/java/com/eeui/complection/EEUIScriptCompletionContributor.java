package com.eeui.complection;

import com.eeui.analytics.Analytics;
import com.eeui.insight.VariableManager;
import com.eeui.insight.bean.Module;
import com.eeui.insight.ModuleManager;
import com.eeui.insight.bean.Variable;
import com.eeui.lang.EEUIIcons;
import com.eeui.util.EEUIFileUtil;
import com.eeui.util.ExtraModulesUtil;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class EEUIScriptCompletionContributor extends CompletionContributor {

    private boolean inModule = false;
    private String originalText = null;

    public EEUIScriptCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(PsiElement.class), new CompletionProvider<CompletionParameters>() {
            protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet resultSet) {
                if (!EEUIFileUtil.isOnVueFile(completionParameters.getPosition())) {
                    return;
                }
                if (!ExtraModulesUtil.isEEUIProject()) {
                    return;
                }
                inModule = false;
                originalText = EEUIFileUtil.getParentText(completionParameters.getOriginalPosition());
                //
                completionModuleName(completionParameters, resultSet);
                completionModuleFunction(completionParameters, resultSet);
                completionComponentFunction(completionParameters, resultSet);
                completionOthers(completionParameters, resultSet);
            }
        });
    }

    private void completionModuleName(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet resultSet) {
        PsiElement want = completionParameters.getOriginalPosition();
        if (want != null) want = want.getParent();
        if (want != null) want = want.getParent();
        if (want != null) want = want.getParent();
        if (want instanceof JSCallExpression) {
            JSExpression methodExpression = ((JSCallExpression)want).getMethodExpression();
            if (methodExpression instanceof JSReferenceExpression) {
                String methodName = methodExpression.getText();
                List<Module> moduleList = ModuleManager.getModules();
                for (Module module : moduleList) {
                    String completion = "";
                    if ("app.requireModule".equals(methodName)) {
                        completion = "\"" + module.getName() + "\"";
                    }
                    if (!module.isComponent() && !completion.isEmpty()) {
                        LookupElement lookupElement = LookupElementBuilder
                                .create(completion)
                                .withLookupString(module.getName())
                                .withIcon(EEUIIcons.LOGO)
                                .withBoldness(true)
                                .withTypeText(module.capitalName() + " Module")
                                .bold()
                                .withInsertHandler(new StatisticsInsertHandler("insert-module-name", module.getName(), completionParameters.getOriginalPosition()));
                        resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, Double.MAX_VALUE));
                    }
                }
            }
        }
    }

    private void completionModuleFunction(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet resultSet) {
        PsiElement element = completionParameters.getPosition().getParent();
        Editor editor = completionParameters.getEditor();
        String moduleName = resolveVarType(element, editor);
        Module module = ModuleManager.getModule(moduleName);
        if (module != null) {
            inModule = true;
            if (EEUIFileUtil.checkModuleName(originalText, module.getName())) {
                for (Module.MethodsBean s : module.getMethods()) {
                    LookupElement lookupElement = LookupElementBuilder.create(s.getName() + "()")
                            .withIcon(EEUIIcons.LOGO)
                            .withBoldness(true)
                            .withTypeText(s.getDescDef(module.capitalName() + " Module"))
                            .bold()
                            .withInsertHandler(new StatisticsInsertHandler("insert-module-function", module.getName() + "." + s.getName() + "()", completionParameters.getOriginalPosition()));
                    resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, Double.MAX_VALUE));
                }
            }
        }
    }

    private void completionComponentFunction(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet resultSet) {
        PsiElement exp = completionParameters.getPosition().getParent().getChildren()[0];
        if (exp instanceof JSCallExpression) {
            if (EEUIFileUtil.isOnVueFile(completionParameters.getPosition())) {
                return;
            }
            JSExpression method = ((JSCallExpression)exp).getMethodExpression();
            if (method != null) {
                String methodName = method.getText();
                if (methodName != null && methodName.endsWith("$el")) {
                    JSExpression[] args = ((JSCallExpression)exp).getArguments();
                    if (args.length == 1 && args[0] != null) {
                        String arg = args[0].getText().replaceAll("\"", "").replaceAll("'", "");
                        Map<String, String> ids = EEUIFileUtil.getAllTagDesc(completionParameters.getOriginalFile());
                        String type = ids.get(arg);
                        Module module = ModuleManager.getModule(type);
                        if (module != null) {
                            for (Module.MethodsBean s : module.getMethods()) {
                                LookupElement lookupElement = LookupElementBuilder
                                        .create(s.getName() + "()")
                                        .withIcon(EEUIIcons.LOGO)
                                        .withBoldness(true)
                                        .withTypeText(s.getDescDef(module.capitalName() + " Component"))
                                        .bold()
                                        .withInsertHandler(new StatisticsInsertHandler("insert-component-function", module.getName() + "." + s.getName() + "()", completionParameters.getOriginalPosition()));
                                resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, Double.MAX_VALUE));
                            }
                        }
                    }
                }
            }
        } else if (exp instanceof JSReferenceExpression) {
            String text = exp.getText();
            if (text.endsWith("$refs")) {
                Map<String, String> ids2 = EEUIFileUtil.getAllTagDesc(completionParameters.getOriginalFile());
                for (Map.Entry<String, String> entry : ids2.entrySet()) {
                    LookupElement lookupElement2 = LookupElementBuilder
                            .create(entry.getKey())
                            .withLookupString(entry.getKey())
                            .withIcon(EEUIIcons.LOGO)
                            .withBoldness(true)
                            .withTypeText("<" + entry.getValue() + "/>")
                            .bold()
                            .withInsertHandler(new StatisticsInsertHandler("insert-dom-ref", "", completionParameters.getOriginalPosition()));
                    resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement2, Double.MAX_VALUE));
                }
            } else if (text.contains("$refs")) {
                String componentRef = text.split("refs")[1];
                Map<String, String> ids3 = EEUIFileUtil.getAllTagDesc(completionParameters.getOriginalFile());
                String type2 = ids3.get(componentRef);
                Module module2 = ModuleManager.getModule(type2);
                if (module2 != null) {
                    for (Module.MethodsBean s2 : module2.getMethods()) {
                        LookupElement lookupElement3 = LookupElementBuilder
                                .create(s2.getName() + "()")
                                .withIcon(EEUIIcons.LOGO)
                                .withBoldness(true)
                                .withTypeText(module2.capitalName() + " Component")
                                .bold()
                                .withInsertHandler(new StatisticsInsertHandler("insert-component-function", module2.getName() + "." + s2.getName() + "()", completionParameters.getOriginalPosition()));
                        resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement3, Double.MAX_VALUE));
                    }
                }
            }
        }
    }

    private void completionOthers(CompletionParameters completionParameters, CompletionResultSet resultSet) {
        if (TextUtils.isEmpty(originalText) || inModule) {
            return;
        }
        List<Variable> variableList = VariableManager.getVariables();
        boolean inVariable = false;
        for (Variable variable : variableList) {
            if (originalText.startsWith(variable.getName() + ".")) {
                for (Variable.MethodsBean item : variable.getMethods()) {
                    resultSet.addElement(LookupElementBuilder.
                            create(item.getName())
                            .withIcon(EEUIIcons.LOGO)
                            .withBoldness(true)
                            .withInsertHandler(new StatisticsInsertHandler("insert-app-subapi", variable.getName() + "." + item.getName(), completionParameters.getPosition()))
                            .withTypeText(item.getDesc()));
                }
                inVariable = true;
                break;
            }
        }
        if (!inVariable) {
            for (Variable variable : variableList) {
                if (variable.isRoot() && !originalText.contains(".")) {
                    resultSet.addElement(LookupElementBuilder.
                            create(variable.getName())
                            .withIcon(EEUIIcons.LOGO)
                            .withBoldness(true)
                            .withInsertHandler(new StatisticsInsertHandler("insert-app-api", variable.getName(), completionParameters.getPosition()))
                            .withTypeText(variable.getDesc()));
                }
            }
        }
    }

    /** ********************************************************************************/
    /** ********************************************************************************/
    /** ********************************************************************************/

    private String resolveVarType(PsiElement element, Editor editor) {
        try {
            PsiElement[] elements = GotoDeclarationAction.findTargetElementsNoVS(element.getProject(), editor, element.getTextOffset(), false);
            if (elements != null) {
                PsiElement declaration = elements[0];
                if (declaration != null) {
                    PsiElement exp = declaration.getChildren()[0];
                    if (exp != null) {
                        if (exp instanceof JSCallExpression) {
                            JSExpression methodExpression = ((JSCallExpression) exp).getMethodExpression();
                            if (methodExpression instanceof JSReferenceExpression) {
                                String expString = methodExpression.getText();
                                if ("app.requireModule".equals(expString)) {
                                    JSExpression[] args = ((JSCallExpression) exp).getArguments();
                                    if (args.length == 1) {
                                        JSExpression arg = args[0];
                                        return arg.getText().replaceAll("\"", "").replaceAll("'", "");
                                    }
                                }
                            }
                            return exp.getText();
                        }
                        if (exp instanceof JSReferenceExpression) {
                            return this.resolveVarType(exp, editor);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            //
        }
        return null;
    }

    private static class StatisticsInsertHandler implements InsertHandler<LookupElement>
    {
        private String insertString;
        private String eventName;

        public StatisticsInsertHandler(String eventName, String insertString, PsiElement any) {
            this.insertString = insertString;
            this.eventName = eventName;
        }

        public void handleInsert(@NotNull InsertionContext insertionContext, @NotNull LookupElement lookupElement) {
            Analytics.newEvent().withEventName(this.eventName).addProperty("function", this.insertString).send();
        }
    }
}
