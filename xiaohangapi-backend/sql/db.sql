-- Create Database
CREATE DATABASE IF NOT EXISTS xiaohangapi;

-- Switch Database
USE xiaohangapi;

-- User Table
CREATE TABLE IF NOT EXISTS user
(
    id           BIGINT AUTO_INCREMENT COMMENT 'ID' PRIMARY KEY,
    userName     VARCHAR(256)                           NULL COMMENT 'User Nickname',
    userAccount  VARCHAR(256)                           NOT NULL COMMENT 'Account',
    userAvatar   VARCHAR(1024)                          NULL COMMENT 'User Avatar',
    gender       TINYINT                                NULL COMMENT 'Gender',
    userRole     VARCHAR(256) DEFAULT 'user'            NOT NULL COMMENT 'User Role: user / admin',
    userPassword VARCHAR(512)                           NOT NULL COMMENT 'Password',
    `accessKey` varchar(512) not null comment 'accessKey',
    `secretKey` varchar(512) not null comment 'secretKey',
    createTime   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Creation Time',
    updateTime   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    isDelete     TINYINT      DEFAULT 0                 NOT NULL COMMENT 'Is Deleted',
    CONSTRAINT uni_userAccount UNIQUE (userAccount)
) COMMENT 'User';

-- Post Table
CREATE TABLE IF NOT EXISTS post
(
    id            BIGINT AUTO_INCREMENT COMMENT 'ID' PRIMARY KEY,
    age           INT COMMENT 'Age',
    gender        TINYINT  DEFAULT 0                 NOT NULL COMMENT 'Gender',
    education     VARCHAR(512)                       NULL COMMENT 'Education',
    place         VARCHAR(512)                       NULL COMMENT 'Place',
    job           VARCHAR(512)                       NULL COMMENT 'Job',
    contact       VARCHAR(512)                       NULL COMMENT 'Contact',
    loveExp       VARCHAR(512)                       NULL COMMENT 'Love Experience',
    content       TEXT                               NULL COMMENT 'Content',
    photo         VARCHAR(1024)                      NULL COMMENT 'Photo',
    reviewStatus  INT      DEFAULT 0                 NOT NULL COMMENT 'Review Status',
    reviewMessage VARCHAR(512)                       NULL COMMENT 'Review Message',
    viewNum       INT                                NULL COMMENT 'View Count',
    thumbNum      INT                                NULL COMMENT 'Thumbs-Up Count',
    userId        BIGINT                             NOT NULL COMMENT 'User ID',
    createTime    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Creation Time',
    updateTime    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    isDelete      TINYINT  DEFAULT 0                 NOT NULL COMMENT 'Is Deleted'
) COMMENT 'Post';


-- Interface Information Table
CREATE TABLE IF NOT EXISTS xiaohangapi.`interface_info`
(
    `id`             BIGINT                             NOT NULL AUTO_INCREMENT COMMENT 'Primary Key' PRIMARY KEY,
    `name`           VARCHAR(256)                       NOT NULL COMMENT 'Name',
    `description`    VARCHAR(256)                       NULL COMMENT 'Description',
    `url`            VARCHAR(512)                       NOT NULL COMMENT 'Interface URL',
    `requestHeader`  TEXT                               NULL COMMENT 'Request Header',
    `responseHeader` TEXT                               NULL COMMENT 'Response Header',
    `status`         INT      DEFAULT 0                 NOT NULL COMMENT 'Interface Status (0-Disabled, 1-Enabled)',
    `method`         VARCHAR(256)                       NOT NULL COMMENT 'Request Type',
    `userId`         BIGINT                             NOT NULL COMMENT 'Creator ID',
    `createTime`     DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Creation Time',
    `updateTime`     DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    `isDelete`       TINYINT  DEFAULT 0                 NOT NULL COMMENT 'Is Deleted (0-Not Deleted, 1-Deleted)'
) COMMENT 'Interface Information';


-- Insert Sample Data
INSERT INTO xiaohangapi.`interface_info` (`name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`,
                                          `method`, `userId`)
VALUES ('Xu Qingyu', 'Xue Congjian', 'www.cary-king.net', 'Pan Botao', 'Tan Congjian', 0, 'Shi Xuanming', 9500534531),
       ('Lu Hongwen', 'Bai Zhiqiang', 'www.leslee-kuhn.net', 'Pan Yixuan', 'Ma Hongtao', 0, 'Chen Junxi', 3982575846),
       ('Mao Jianhui', 'Luo Wen', 'www.rosaria-kilback.io', 'Feng Zimo', 'Peng Zhehan', 0, 'Zhao Yuanhang', 121776355),
       ('Peng Yuze', 'Cai Yuqi', 'www.norris-bergstrom.biz', 'Dong Siyuan', 'Tian Xiaobo', 0, 'Pan Qingyu', 740),
       ('Fu Zhiqiang', 'Chen Zichen', 'www.jordan-reinger.com', 'Jin Zhiqiang', 'Xiong Jincheng', 0, 'Deng Ruiyuan',
        35542559),
       ('Lu Lixin', 'Kong Yuebing', 'www.fe-okon.info', 'Wan Weichen', 'Lin Haoran', 0, 'Meng Rongxuan', 1445),
       ('Xia Xuesong', 'Xu Ziqian', 'www.lashawna-legros.co', 'Cai Haoran', 'Hu Pengtao', 0, 'Zhong Lihui', 34075514),
       ('Yan Yuxuan', 'Yan Zhize', 'www.kay-funk.biz', 'Mo Haoxuan', 'Guo Lixin', 0, 'Gong Tianyu', 70956),
       ('Xiao Jiayi', 'Cao Yitong', 'www.margarette-lindgren.biz', 'Tian Zeyang', 'Deng Ruiyuan', 0, 'Liang Zhiqiang',
        98),
       ('Du Chi', 'Feng Siyuan', 'www.vashti-auer.org', 'Li Jianbo', 'Wu Bowen', 0, 'Li Weichen', 9),
       ('Shi Jinxin', 'Cai Pengtao', 'www.diann-keebler.org', 'Xu Yelin', 'Yan Jianhui', 0, 'Li Yewei', 125),
       ('Lin Xuanming', 'Jia Xuyao', 'www.dotty-kuvalis.io', 'Liang Yuze', 'Long Weize', 0, 'Xu Zhiyuan', 79998),
       ('He Yuxuan', 'Lai Zhichen', 'www.andy-adams.net', 'Cui Simiao', 'Bai Hongxuan', 0, 'Shao Zhenjia', 7167482751),
       ('Wei Zhiqiang', 'Yu Licheng', 'www.ione-aufderhar.biz', 'Zhu Yixuan', 'Wan Zhiyuan', 0, 'Tang Haoqiang',
        741098),
       ('Yan Junhao', 'Jin Yinxiang', 'www.duane-boyle.org', 'Lei Haoyan', 'Hou Sicong', 0, 'Hao Si', 580514),
       ('Yao Haoxuan', 'Jin Peng', 'www.lyda-klein.biz', 'Du Haoqiang', 'Shao Zhize', 0, 'Feng Hongtao', 6546),
       ('Liao Chi', 'Shen Zeyang', 'www.consuelo-sipes.info', 'Peng Haoran', 'Deng Yaojie', 0, 'Zhou Bin', 7761037),
       ('Lai Zhiyuan', 'Deng Zhize', 'www.emerson-mann.co', 'Xiong Mingzhe', 'He Zhehan', 0, 'Tian Peng', 381422),
       ('Xu Tao', 'Lu Zhiyuan', 'www.vella-ankunding.name', 'Jia Zhehan', 'Mo Haoyan', 0, 'Yuan Yuebing', 4218096),
       ('Lu Junxi', 'Shen Pengfei', 'www.shari-reichel.org', 'Guo Hongxuan', 'Qin Yelin', 0, 'Xiong Lixin', 493);

