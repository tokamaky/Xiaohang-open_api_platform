package com.xiaohang.xiaohangapicommon.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interface Status Enum Class
 */
public enum InterfaceInfoStatusEnum {

    /**
     * Interface Status Enum Values
     */
    OFFLINE("Closed", 0),
    ONLINE("Online", 1);

    private final String text;
    private final Integer value;

    InterfaceInfoStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * Get list of values
     *
     * @return List of values
     */
    public static List<Integer> getValues() {
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
    public static InterfaceInfoStatusEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {  // Check if the value is null or empty
            return null;
        }
        for (InterfaceInfoStatusEnum anEnum : InterfaceInfoStatusEnum.values()) {
            if (anEnum.value.equals(value)) {  // Compare the enum value
                return anEnum;  // Return the enum that matches the value
            }
        }
        return null;  // Return null if no match found
    }

    public Integer getValue() {
        return value;  // Return the value of the enum
    }

    public String getText() {
        return text;  // Return the text of the enum
    }
}
