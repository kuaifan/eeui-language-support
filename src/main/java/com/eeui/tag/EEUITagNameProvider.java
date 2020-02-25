package com.eeui.tag;

import com.eeui.insight.ComponentManager;
import com.eeui.insight.bean.Component;
import com.eeui.lang.EEUIIcons;
import com.intellij.codeInsight.completion.XmlTagInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.XmlTagNameProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 标签自动完成
 */
public class EEUITagNameProvider implements XmlElementDescriptorProvider, XmlTagNameProvider {

    /**
     * 获取标签描述
     * 在任意输入后执行, 需在 plugin.xml 中配置 xml.elementDescriptorProvider
     *
     * @param xmlTag
     * @return
     */
    @Nullable
    @Override
    public XmlElementDescriptor getDescriptor(XmlTag xmlTag) {
        XmlNSDescriptor nsDescriptor = xmlTag.getNSDescriptor(xmlTag.getNamespace(), false);
        XmlElementDescriptor descriptor = nsDescriptor != null ? nsDescriptor.getElementDescriptor(xmlTag) : null;
        // 判断是否包含在特定处理标签内
        boolean special = false;
        List<Component> componentList = ComponentManager.getComponents();
        for (Component component : componentList) {
            if (component.getName().equals(xmlTag.getName())) {
                special = true;
                break;
            }
        }
        if (!special) {
            return null;
        }
        return new EEUIAnyXmlElementDescriptor(descriptor, nsDescriptor, xmlTag.getName());
    }

    /**
     * 添加属性元素标签
     * 在输入 < 后执行，需要在 plugin.xml 中配置 xml.tagNameProvider
     *
     * @param list
     * @param xmlTag
     * @param s
     */
    @Override
    public void addTagNameVariants(List<LookupElement> list, @NotNull XmlTag xmlTag, String s) {
        List<Component> componentList = ComponentManager.getComponents();
        for (Component component : componentList) {
            list.add(LookupElementBuilder
                    .create(component.getName())
                    .withInsertHandler(XmlTagInsertHandler.INSTANCE)
                    .withBoldness(true)
                    .withIcon(EEUIIcons.LOGO)
                    .withTypeText(component.getDescDef("eeui component")));
        }
    }
}
