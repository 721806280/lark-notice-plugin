package io.jenkins.plugins.lark.notice.sdk.model.lark.builder;

import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Body;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Button;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Card;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.at.At;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.container.columns.ColumnElement;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.container.columns.ColumnSetElement;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.TextElement;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.button.ButtonBehavior;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.button.ButtonElement;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.hr.HrElement;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.icon.Icon;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.img.ImgElement;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.link.Link;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.person.PersonListElement;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.person.PersonSimpleElement;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.text.MarkdownElement;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.title.Header;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.title.TitleElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lark 风格卡片构建器，支持 Markdown、按钮、人员列表等组件。
 *
 * @author xm.z
 */
public class LarkCardBuilder {

    /**
     * 卡片对象
     */
    protected final Card card = new Card();

    /**
     * 卡片内容元素集合
     */
    protected final List<Object> elements = new ArrayList<>();

    /**
     * 设置卡片头部
     *
     * @param template 头部模板样式
     * @param title    卡片标题
     * @return 构建器自身，支持链式调用
     */
    public LarkCardBuilder withHeader(String template, String title) {
        Header header = new Header();
        header.setTemplate(template);
        // 使用 Lark Markdown 格式标题
        header.setTitle(TitleElement.buildLarkMd(title));
        card.setHeader(header);
        return this;
    }

    /**
     * 添加一组卡片元素
     *
     * @param elements 元素列表
     * @return 构建器自身
     */
    public LarkCardBuilder withElements(List<Object> elements) {
        if (elements != null && !elements.isEmpty()) {
            this.elements.addAll(elements);
        }
        return this;
    }

    /**
     * 添加单个卡片元素
     *
     * @param element 元素对象
     * @return 构建器自身
     */
    public LarkCardBuilder addElement(Object element) {
        if (element != null) {
            this.elements.add(element);
        }
        return this;
    }

    /**
     * 添加 Markdown 内容
     *
     * @param content Markdown 格式文本
     * @return 构建器自身
     */
    public LarkCardBuilder withMarkdown(String content) {
        if (content == null || content.trim().isEmpty()) {
            return this;
        }

        MarkdownElement markdown = new MarkdownElement();
        markdown.setContent(content);
        elements.add(markdown);
        return this;
    }

    /**
     * 添加分隔线
     *
     * @return 构建器自身
     */
    public LarkCardBuilder withSeparator() {
        elements.add(new HrElement());
        return this;
    }

    /**
     * 添加人员列表组件
     *
     * @return 构建器自身
     */
    public LarkCardBuilder withPersonList(At at) {
        if (at == null || at.getAtUserIds().isEmpty()) {
            return this;
        }

        List<PersonSimpleElement> persons = at.getAtUserIds().stream().map(PersonSimpleElement::of).collect(Collectors.toList());

        Icon icon = new Icon();
        icon.setTag("standard_icon");
        icon.setToken("group_outlined");
        icon.setColor("blue");

        PersonListElement personList = new PersonListElement();
        personList.setShowName(true);
        personList.setShowAvatar(true);
        personList.setSize("small");
        personList.setIcon(icon);
        personList.setPersons(persons);

        elements.add(personList);
        return this;
    }

    /**
     * 添加按钮组
     *
     * @param buttons 按钮列表
     * @return 构建器自身
     */
    public LarkCardBuilder withButtons(List<Button> buttons) {
        if (buttons == null || buttons.isEmpty()) {
            return this;
        }

        List<ColumnElement> columnElements = buttons.stream()
                .map(this::createButtonColumn)
                .collect(Collectors.toList());

        ColumnSetElement columnSet = new ColumnSetElement();
        columnSet.setColumns(columnElements);
        elements.add(columnSet);
        return this;
    }

    /**
     * 创建单个按钮列
     *
     * @param button 按钮数据
     * @return ColumnElement 按钮列对象
     */
    private ColumnElement createButtonColumn(Button button) {
        ColumnElement column = new ColumnElement();
        column.setWidth("weighted");
        column.setWeight(5);
        column.setVerticalAlign("top");
        column.setDirection("horizontal");
        column.setAction(new Link());

        ButtonElement buttonElement = new ButtonElement();
        buttonElement.setType(button.getType());
        buttonElement.setSize("medium");
        buttonElement.setWidth("fill");
        buttonElement.setText(TextElement.of(button.getText()));

        ButtonBehavior behavior = new ButtonBehavior();
        behavior.setType("open_url");
        behavior.setDefaultUrl(button.getUrl());
        buttonElement.setBehaviors(List.of(behavior));

        column.setElements(List.of(buttonElement));
        return column;
    }

    /**
     * 添加图片
     *
     * @param img 图片对象
     * @return 构建器自身
     */
    public LarkCardBuilder withImage(ImgElement img) {
        if (img != null) {
            this.withSeparator();
            elements.add(img);
        }
        return this;
    }

    /**
     * 构建最终的卡片对象
     *
     * @return 构建完成的卡片
     */
    public Card build() {
        Body body = new Body();
        // 设置卡片内容
        body.setElements(elements);
        card.setBody(body);
        return card;
    }

}
