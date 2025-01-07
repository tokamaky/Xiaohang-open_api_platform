package com.xiaohang.xiaohangapicommon.model.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * File Upload Request
 */
@Data
public class UploadFileRequest implements Serializable {

    /**
     * Business identifier
     */
    private String biz;

    private static final long serialVersionUID = 1L;
}

