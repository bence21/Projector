package com.bence.projector.common.model;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public enum SectionType {
    INTRO(0),
    VERSE(1),
    PRE_CHORUS(2),
    CHORUS(3),
    BRIDGE(4),
    CODA(5);
    private static Map<Integer, SectionType> sectionTypeMap;
    private final int value;

    SectionType(int value) {
        this.value = value;
    }

    public static SectionType getInstance(int value) {
        Map<Integer, SectionType> sectionTypeMap = getSectionTypeMap();
        return sectionTypeMap.get(value);
    }

    private static Map<Integer, SectionType> getSectionTypeMap() {
        if (sectionTypeMap == null) {
            SectionType[] values = SectionType.values();
            sectionTypeMap = new HashMap<>(values.length);
            for (SectionType sectionType : values) {
                sectionTypeMap.put(sectionType.getValue(), sectionType);
            }
        }
        return sectionTypeMap;
    }

    public static SectionType getValueFromString(String s) {
        String lowerCase = s.toLowerCase();
        switch (lowerCase) {
            case "[intro]":
                return INTRO;
            case "[pre_chorus]":
                return PRE_CHORUS;
            case "[chorus]":
                return CHORUS;
            case "[bridge]":
                return BRIDGE;
            case "[coda]":
                return CODA;
        }
        return VERSE;
    }

    public int getValue() {
        return value;
    }

    public String getStringValue() {
        switch (getInstance(value)) {
            case INTRO:
                return "[INTRO]";
            case PRE_CHORUS:
                return "[PRE_CHORUS]";
            case CHORUS:
                return "[CHORUS]";
            case BRIDGE:
                return "[BRIDGE]";
            case CODA:
                return "[CODA]";
        }
        return "";
    }

    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    public String getBackgroundColorHex(boolean darkTheme) {
        Color color = getBackgroundColor(darkTheme);
        return colorToHex(color);
    }

    private Color getBackgroundColor(boolean darkTheme) {
        Color color;
        if (darkTheme) {
            color = getBackgroundColorDark();
        } else {
            color = getBackgroundColorLight();
        }
        return color;
    }

    private Color getBackgroundColorDark() {
        switch (this) {
            case INTRO:
                return new Color(93, 136, 175);
            case VERSE:
                return new Color(10, 103, 0);
            case PRE_CHORUS:
                return new Color(0, 150, 155);
            case CHORUS:
                return new Color(20, 32, 129);
            case BRIDGE:
                return new Color(211, 84, 0);
            case CODA:
                return new Color(96, 0, 0);
            default:
                return VERSE.getBackgroundColorDark();
        }
    }

    private Color getBackgroundColorLight() {
        switch (this) {
            case INTRO:
                return new Color(122, 189, 255); // Light blue
            case VERSE:
                return new Color(112, 241, 112); // Light green
            case PRE_CHORUS:
                return new Color(131, 255, 255); // Light cyan
            case CHORUS:
                return new Color(86, 141, 255); // Light lavender
            case BRIDGE:
                return new Color(255, 136, 98); // Light orange
            case CODA:
                return new Color(255, 113, 113); // Light red
            default:
                return VERSE.getBackgroundColorLight();
        }
    }
}
