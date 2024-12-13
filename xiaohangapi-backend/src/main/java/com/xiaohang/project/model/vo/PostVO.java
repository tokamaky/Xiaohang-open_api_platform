package com.xiaohang.project.model.vo;

import com.xiaohang.project.model.entity.Post;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Post View
 *
 * @author
 * <a href="https://github.com/lixiaohang">Programmer Yupi</a>
 * @from
 * <a href="https://xiaohang.icu">Programming Navigation Knowledge Planet</a>
 * @TableName product
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostVO extends Post {

    /**
     * Whether Liked
     */
    private Boolean hasThumb;

    private static final long serialVersionUID = 1L;
}