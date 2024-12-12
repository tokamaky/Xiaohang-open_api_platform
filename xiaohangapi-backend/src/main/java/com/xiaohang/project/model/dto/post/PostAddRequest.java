package com.xiaohang.project.model.dto.post;

import lombok.Data;

import java.io.Serializable;

/**
 * Request for add post
 *
 * @author xiaohang
 *  
 */
@Data
public class PostAddRequest implements Serializable {

    /**
     * age
     */
    private Integer age;

    /**
     * gender
     */
    private Integer gender;

    /**
     * education
     */
    private String education;

    /**
     * place
     */
    private String place;

    /**
     * 职业
     */
    private String job;

    /**
     * 联系方式
     */
    private String contact;

    /**
     * 感情经历
     */
    private String loveExp;

    /**
     * 内容（个人介绍）
     */
    private String content;

    /**
     * 照片地址
     */
    private String photo;

    // [加入编程导航](https://t.zsxq.com/0emozsIJh) 入门捷径+交流答疑+项目实战+求职指导，帮你自学编程不走弯路

    private static final long serialVersionUID = 1L;
}