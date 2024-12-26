package com.xiaohang.xiaohangapicommon.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Post Entity
 *
 * @author Xiaohang Ji
 */
@TableName(value = "post")
@Data
public class Post implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Age
     */
    private Integer age;

    /**
     * Gender (0 - Male, 1 - Female)
     */
    private Integer gender;

    /**
     * Education
     */
    private String education;

    /**
     * Location
     */
    private String place;

    /**
     * Job
     */
    private String job;

    /**
     * Contact Information
     */
    private String contact;

    /**
     * Relationship Experience
     */
    private String loveExp;

    /**
     * Content (Personal Introduction)
     */
    private String content;

    /**
     * Photo URL
     */
    private String photo;

    /**
     * Status (0 - Pending Review, 1 - Approved, 2 - Rejected)
     */
    private Integer reviewStatus;

    /**
     * Review Message
     */
    private String reviewMessage;

    /**
     * View Count
     */
    private Integer viewNum;

    /**
     * Like Count
     */
    private Integer thumbNum;

    /**
     * Creator User ID
     */
    private Long userId;

    /**
     * Creation Time
     */
    private Date createTime;

    /**
     * Update Time
     */
    private Date updateTime;

    /**
     * Is Deleted
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
