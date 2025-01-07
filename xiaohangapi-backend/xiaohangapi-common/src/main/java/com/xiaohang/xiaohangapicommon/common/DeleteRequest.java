package com.xiaohang.xiaohangapicommon.common;

import lombok.Data;

import java.io.Serializable;

/**
 * delete request
 *
 * @author xiaohang
 */
@Data
public class DeleteRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}