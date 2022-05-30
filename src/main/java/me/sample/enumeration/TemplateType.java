package me.sample.enumeration;

import java.io.Serializable;

public enum TemplateType implements Serializable {
    GENERAL_PUSH("Обычный push");

    private final String text;

    TemplateType(String t) {
        text = t;
    }

    public String getText() {
        return text;
    }

    public String getName() {
        return this.name();
    }
}
