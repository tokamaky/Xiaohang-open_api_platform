package com.xiaohang.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaohang.project.model.entity.Post;

/**
* @author 14711
* @description 针对表【post(Post)】的数据库操作Service
* @createDate 2024-12-12 10:01:16
*/
public interface PostService extends IService<Post> {
    /**
     * valid
     *
     * @param post
     * @param add valid it's post or not
     */
    void validPost(Post post, boolean add);

}
