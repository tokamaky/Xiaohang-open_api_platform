# 第一次执行
create database my_db;

use my_db;

# 用户表
create table user
(
    id           bigint auto_increment comment 'id' primary key,
    username     varchar(256)       null comment 'UserName',
    userAccount  varchar(256)       null comment 'Account',
    avatarUrl    varchar(1024)      null comment 'UserAvatar',
    gender       tinyint            null comment 'Gender',
    userPassword varchar(512)       not null comment 'Password',
    phone        varchar(128)       null comment 'Phone',
    email        varchar(512)       null comment 'Email',
    userStatus   int      default 0 not null comment 'Status 0 - Normal',
    createTime   datetime default CURRENT_TIMESTAMP comment 'CreateTime',
    updateTime   datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0 not null comment 'IsDelete',
    userRole     int      default 0 not null comment 'User Role 0 - Normal User 1 - Administrator'
) comment 'User';


