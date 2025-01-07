package com.xiaohang.xiaohangapicommon.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * File Upload Business Type Enum
 */
public enum FileUploadBizEnum {

    USER_AVATAR("User Avatar", "user_avatar");

    private final String text;
    private final String value;

    FileUploadBizEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * Get a list of values
     *
     * @return List of values
     */
    public static List<String> getValues() {
        return Arrays.stream(values())  // Stream through all enum values
                .map(item -> item.value)  // Extract the 'value' from each enum
                .collect(Collectors.toList());  // Collect into a list
    }

    /**
     * Get enum by value
     *
     * @param value The value of the enum
     * @return The corresponding enum, or null if not found
     */
    public static FileUploadBizEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {  // Check if the value is null or empty
            return null;
        }
        for (FileUploadBizEnum anEnum : FileUploadBizEnum.values()) {
            if (anEnum.value.equals(value)) {  // Compare the enum value
                return anEnum;  // Return the enum that matches the value
            }
        }
        return null;  // Return null if no match found
    }

    public String getValue() {
        return value;  // Return the value of the enum
    }

    public String getText() {
        return text;  // Return the text of the enum
    }
}
