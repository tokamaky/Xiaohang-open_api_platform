package com.xiaohang.xiaohangapicommon.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Post Review Status Enum
 *
 * Represents the different review statuses for posts.
 *
 * Author: Xiaohang
 */
public enum PostReviewStatusEnum {

    REVIEWING("Pending Review", 0),
    PASS("Approved", 1),
    REJECT("Rejected", 2);

    private final String text;
    private final int value;

    PostReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * Get the list of values
     *
     * @return List of integer values for review statuses
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
