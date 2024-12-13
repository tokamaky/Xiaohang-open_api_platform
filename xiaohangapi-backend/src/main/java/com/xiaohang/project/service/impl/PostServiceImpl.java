package com.xiaohang.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaohang.project.common.ErrorCode;
import com.xiaohang.project.exception.BusinessException;
import com.xiaohang.project.mapper.PostMapper;
import com.xiaohang.project.model.entity.Post;
import com.xiaohang.project.model.enums.PostGenderEnum;
import com.xiaohang.project.model.enums.PostReviewStatusEnum;
import com.xiaohang.project.service.PostService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author 14711
* @description 针对表【post(Post)】的数据库操作Service实现
* @createDate 2024-12-12 10:01:16
*/
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
    implements PostService{

    @Override
    public void validPost(Post post, boolean add) {
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Integer age = post.getAge();
        Integer gender = post.getGender();
        String content = post.getContent();
        String job = post.getJob();
        String place = post.getPlace();
        String education = post.getEducation();
        String loveExp = post.getLoveExp();
        Integer reviewStatus = post.getReviewStatus();

        // During creation, all parameters must be non-null
        if (add) {
            if (StringUtils.isAnyBlank(content, job, place, education, loveExp) || ObjectUtils.anyNull(age, gender)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }

        // Validate that content length does not exceed 8192 characters
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Content is too long");
        }

        // Validate that the review status is valid
        if (reviewStatus != null && !PostReviewStatusEnum.getValues().contains(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // Validate that the age is within the acceptable range (18-100)
        if (age != null && (age < 18 || age > 100)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Age is not within the valid range");
        }

        // Validate that the gender is valid
        if (gender != null && !PostGenderEnum.getValues().contains(gender)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Gender is not valid");
        }
    }

}




