package me.sample.enumeration;

import java.io.Serializable;

public enum Language implements Serializable {
    RU("Русский"),
    EN("English");

    public final String text;

    Language(String t) {
        text = t;
    }
}
