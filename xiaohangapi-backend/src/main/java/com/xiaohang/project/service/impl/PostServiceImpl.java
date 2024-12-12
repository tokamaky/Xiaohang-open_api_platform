package com.xiaohang.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaohang.project.model.entity.Post;
import generator.mapper.PostMapper;
import generator.service.PostService;
import org.springframework.stereotype.Service;

/**
* @author 14711
* @description 针对表【post(Post)】的数据库操作Service实现
* @createDate 2024-12-12 10:01:16
*/
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
    implements PostService{

}




