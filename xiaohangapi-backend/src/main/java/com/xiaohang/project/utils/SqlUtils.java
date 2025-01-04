package com.xiaohang.project.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * SQL Utility
 *
 */
public class SqlUtils {

    /**
     * Validates whether the sorting field is valid (to prevent SQL injection)
     *
     * @param sortField
     * @return
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }
}
