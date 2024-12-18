package com.xiaohang.project.common;

import lombok.Data;

import java.io.Serializable;

/**
 * Rrquest for Id
 *
 * @author Xiaohang
 */
@Data
public class IdRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}