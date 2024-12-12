package com.xiaohang.project.model.dto.post;

import lombok.Data;

import java.io.Serializable;

/**
 * Like / Cancel like request
 *
 * @author xiaohang
 *  
 */
@Data
public class PostDoThumbRequest implements Serializable {

    /**
     *  id
     */
    private long postId;

    private static final long serialVersionUID = 1L;
}