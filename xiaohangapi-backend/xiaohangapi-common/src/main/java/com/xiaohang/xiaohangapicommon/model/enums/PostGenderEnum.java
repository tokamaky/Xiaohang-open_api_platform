package com.xiaohang.xiaohangapicommon.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Post Gender Enum
 *
 * @author xiaohang
 */
public enum PostGenderEnum {

    MALE("Male", 0),
    FEMALE("Female", 1);

    private final String text;
    private final int value;

    PostGenderEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * Get the list of values
     *
     * @return List of integer values for genders
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
