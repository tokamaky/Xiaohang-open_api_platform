-- Create Database
CREATE DATABASE IF NOT EXISTS xiaohangapi;

-- Switch Database
USE xiaohangapi;


SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for interface_info
-- ----------------------------
DROP TABLE IF EXISTS `interface_info`;
CREATE TABLE `interface_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接口地址',
  `host` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '主机名',
  `requestParams` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数',
  `requestParamsRemark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数说明',
  `responseParamsRemark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '响应参数说明',
  `requestHeader` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求头',
  `responseHeader` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '响应头',
  `status` int NOT NULL DEFAULT 0 COMMENT '接口状态（0-关闭，1-开启）',
  `method` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '请求类型',
  `userId` bigint NOT NULL COMMENT '创建人',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删, 1-已删)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '接口信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of interface_info
-- ----------------------------
INSERT INTO `interface_info` VALUES (1, '获取当前用户名称', '获取用户名称', '/api/name/user', 'http://localhost:8090', '{\n    \"username\": \"nero\"\n}', NULL, NULL, '{\"Content-Type\": \"application/json\"}', '{\"Content-Type\": \"application/json\"}', 1, 'POST', 1667508636207661058, '2023-06-14 21:18:10', '2023-07-10 11:24:01', 0);
INSERT INTO `interface_info` VALUES (24, '随机头像', '随机获取一个头像地址', '/api/rand.avatar', 'https://api.uomg.com', 'sort=男&format=json', '[{\"id\":1688957070977,\"name\":\"sort\",\"isRequired\":\"no\",\"type\":\"string\",\"remark\":\"选择输出分类[男|女|动漫男|动漫女]，为空随机输出\"},{\"id\":1688957075391,\"name\":\"format\",\"isRequired\":\"no\",\"type\":\"string\",\"remark\":\"选择输出格式[json|images]\"}]', '[{\"id\":1688957088125,\"name\":\"code\",\"type\":\"string\",\"remark\":\"返回的状态码\"},{\"id\":1688957090261,\"name\":\"imgurl\",\"type\":\"string\",\"remark\":\"返回图片地址\"},{\"id\":1688957090855,\"name\":\"msg\",\"type\":\"string\",\"remark\":\"返回错误提示信息！\"}]', '{\"Content-Type\": \"application/json\"}', '{\"Content-Type\": \"application/json\"}', 1, 'GET', 1667508636207661058, '2023-07-08 12:07:23', '2023-07-10 10:45:10', 0);
INSERT INTO `interface_info` VALUES (26, '网易云音乐随机歌曲', '网易云音乐，随机歌曲输出。', '/api/rand.music', 'https://api.uomg.com', 'sort=热歌榜&format=json', '[{\"id\":1,\"name\":\"sort\",\"isRequired\":\"否\",\"type\":\"string\",\"remark\":\"选择输出分类[热歌榜|新歌榜|飙升榜|抖音榜|电音榜]，为空输出热歌榜\"},{\"id\":2,\"name\":\"mid\",\"isRequired\":\"否\",\"type\":\"int\",\"remark\":\"网易云歌单ID\"},{\"id\":1688815371547,\"name\":\"format\",\"isRequired\":\"no\",\"type\":\"string\",\"remark\":\"选择输出格式[json|mp3]\"}]', '[{\"id\":1688815422793,\"name\":\"code\",\"type\":\"string\",\"remark\":\"返回的状态码\"},{\"id\":1688815424624,\"name\":\"data\",\"type\":\"string\",\"remark\":\"返回歌曲数据\"},{\"id\":1688815425060,\"name\":\"msg\",\"type\":\"string\",\"remark\":\"返回错误提示信息\"}]', '{\"Content-Type\": \"application/json\"}', '{\"Content-Type\": \"application/json\"}', 1, 'GET', 1667508636207661058, '2023-07-08 16:30:06', '2023-07-08 19:43:01', 0);
INSERT INTO `interface_info` VALUES (27, '网易云音乐热门评论', '网易云音乐热门评论随机API接口', '/api/comments.163', 'https://api.uomg.com', 'format=json', '[{\"id\":1688816624700,\"name\":\"mid\",\"isRequired\":\"no\",\"type\":\"int\",\"remark\":\"网易云歌单ID\"},{\"id\":1688816632619,\"name\":\"format\",\"isRequired\":\"no\",\"type\":\"string\",\"remark\":\"选择输出格式[json|mp3]\"}]', '[{\"id\":1688816648171,\"name\":\"code\",\"type\":\"string\",\"remark\":\"返回的状态码\"},{\"id\":1688816649732,\"name\":\"data\",\"type\":\"string\",\"remark\":\"返回评论数据\"},{\"id\":1688816650394,\"name\":\"msg\",\"type\":\"string\",\"remark\":\"返回错误提示信息\"}]', NULL, NULL, 1, 'GET', 1667508636207661058, '2023-07-08 17:05:42', '2023-07-10 11:24:32', 0);
INSERT INTO `interface_info` VALUES (29, '随机壁纸', '随机获取一个壁纸地址', '/sjbz/api.php', 'http://api.btstu.cn', 'lx=dongman&format=json', '[{\"id\":1689002138135,\"name\":\"method\",\"isRequired\":\"no\",\"type\":\"string\",\"remark\":\"输出壁纸端[mobile|pc|zsy]默认为pc\"},{\"id\":1689002153560,\"name\":\"lx\",\"isRequired\":\"no\",\"type\":\"string\",\"remark\":\"选择输出分类[meizi|dongman|fengjing|suiji]，为空随机输出\"},{\"id\":1689002153860,\"name\":\"format\",\"isRequired\":\"no\",\"type\":\"string\",\"remark\":\"输出壁纸格式[json|images]默认为images\"}]', '[{\"id\":1689002171759,\"name\":\"code\",\"type\":\"string\",\"remark\":\"返回的状态码\"},{\"id\":1689002173057,\"name\":\"imgurl\\t\",\"type\":\"string\",\"remark\":\"返回图片地址\"},{\"id\":1689002173621,\"name\":\"width\",\"type\":\"string\",\"remark\":\"返回图片宽度\"},{\"id\":1689002184505,\"name\":\"height\\t\",\"type\":\"string\",\"remark\":\"返回图片高度\"}]', NULL, NULL, 1, 'GET', 1667508636207661058, '2023-07-09 20:28:31', '2023-07-10 23:45:37', 0);
INSERT INTO `interface_info` VALUES (30, '毒鸡汤', '随机生成一句毒鸡汤语录', '/yan/api.php', 'http://localhost:8090', 'charset=utf-8&encode=json', '[{\"id\":1689002041985,\"name\":\"charset\",\"isRequired\":\"no\",\"type\":\"string\",\"remark\":\"返回编码类型[gbk|utf-8]默认utf-8\"},{\"id\":1689002080311,\"name\":\"encode\",\"isRequired\":\"no\",\"type\":\"string\",\"remark\":\"返回格式类型[text|js|json]默认text\"}]', '[{\"id\":1689002092752,\"name\":\"code\",\"type\":\"string\",\"remark\":\"code\"}]', NULL, NULL, 1, 'GET', 1667508636207661058, '2023-07-09 21:17:10', '2023-07-10 23:17:52', 0);
INSERT INTO `interface_info` VALUES (31, '抖音解析', '解析抖音链接，获取无水印链接', '/dyjx/api.php', 'http://api.btstu.cn', 'url=https://v.douyin.com/J8TVGVn/', '[{\"id\":1689003649028,\"name\":\"url\",\"isRequired\":\"yes\",\"type\":\"string\",\"remark\":\"需要解析的抖音地址\"}]', '[{\"id\":1689003654626,\"name\":\"code\",\"type\":\"string\",\"remark\":\"返回的状态码\"},{\"id\":1689003659703,\"name\":\"msg\",\"type\":\"string\",\"remark\":\"状态码说明\"},{\"id\":1689003660215,\"name\":\"data\",\"type\":\"string\",\"remark\":\"链接解析信息\"}]', NULL, NULL, 1, 'GET/POST', 1667508636207661058, '2023-07-10 23:29:28', '2023-07-11 00:07:13', 1);
INSERT INTO `interface_info` VALUES (32, 'Qrcode二维码', '生成在线二维码', '/qrcode/api.php', 'http://api.btstu.cn', 'text=https://api.btstu.cn&size=300', '[{\"id\":1689003685138,\"name\":\"text\",\"isRequired\":\"yes\",\"type\":\"string\",\"remark\":\"二维码内容\"},{\"id\":1689003723361,\"name\":\"size\",\"isRequired\":\"no\",\"type\":\"string\",\"remark\":\"二维码大小，单位为px\"}]', '[{\"id\":1689003739093,\"name\":\"code\",\"type\":\"string\",\"remark\":\"返回的状态码\"},{\"id\":1689003740418,\"name\":\"msg\",\"type\":\"string\",\"remark\":\"返回错误提示！\"}]', NULL, NULL, 1, 'GET', 1667508636207661058, '2023-07-10 23:36:22', '2023-07-11 00:05:28', 1);
INSERT INTO `interface_info` VALUES (33, '获取QQ昵称和头像', '获取QQ昵称和头像', '/qqxt/api.php', 'http://api.btstu.cn', 'qq=10001', '[{\"id\":1689004243483,\"name\":\"qq\",\"isRequired\":\"yes\",\"type\":\"string\",\"remark\":\"查询的QQ号码\"}]', '[{\"id\":1689004308636,\"name\":\"code\",\"type\":\"string\",\"remark\":\"返回的状态码\"},{\"id\":1689004319045,\"name\":\"msg\",\"type\":\"string\",\"remark\":\"返回错误提示！\"},{\"id\":1689004319618,\"name\":\"imgurl\",\"type\":\"string\",\"remark\":\"QQ头像图片地址\"},{\"id\":1689004320086,\"name\":\"name\",\"type\":\"string\",\"remark\":\"QQ昵称\"}]', NULL, NULL, 1, 'GET', 1667508636207661058, '2023-07-10 23:52:19', '2023-07-10 23:54:32', 1);
INSERT INTO `interface_info` VALUES (34, '短网址生成', '将长网址进行缩短，支持百度、新浪、腾讯短网址等等。', '/api/long2dwz', 'https://api.uomg.com', 'dwzapi=urlcn&url=http://qrpay.uomg.com', '[{\"id\":1689004480035,\"name\":\"url\",\"isRequired\":\"yes\",\"type\":\"string\",\"remark\":\"需要进行缩短的长网址\"},{\"id\":1689004514429,\"name\":\"dwzapi\",\"isRequired\":\"no\",\"type\":\"string\",\"remark\":\"选择缩短接口[tcn|dwzcn|urlcn|suoim|mrwso|]\"}]', '[{\"id\":1689004538420,\"name\":\"code\",\"type\":\"string\",\"remark\":\"返回的状态码\"},{\"id\":1689004564886,\"name\":\"ae_url\",\"type\":\"string\",\"remark\":\"返回缩短后的短网址\"},{\"id\":1689004565428,\"name\":\"msg\",\"type\":\"string\",\"remark\":\"返回错误提示信息！\"}]', NULL, NULL, 1, 'GET/POST', 1667508636207661058, '2023-07-10 23:56:35', '2023-07-10 23:57:13', 0);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userAccount` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '账号',
  `userPassword` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `unionId` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信开放平台id',
  `mpOpenId` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '公众号openId',
  `userName` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `userAvatar` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户头像',
  `userProfile` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户简介',
  `userRole` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin/ban',
  `accessKey` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'accessKey',
  `secretKey` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'secretKey',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_unionId`(`unionId` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1668967663894642689 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1667508636207661058, 'nero', '40576063cc4e128bee21f333df54d346', NULL, NULL, 'Nero', 'https://yupao-project-ichensw.oss-cn-hangzhou.aliyuncs.com/PEej0vHQ-4.jpg', NULL, 'admin', '53349ea7504b8985606a8edbda11e314', '82b9389e40dd4c589feebd12fe60786a', '2023-06-10 20:26:54', '2023-07-10 23:12:03', 0);
INSERT INTO `user` VALUES (1668967663894642689, 'niko', '40576063cc4e128bee21f333df54d346', NULL, NULL, 'Niko', 'https://image-bed-ichensw.oss-cn-hangzhou.aliyuncs.com/006VVqOWgy1h43uaynbyxj30go0go0u7.jpg', NULL, 'user', '6ab93d6aae79d390562ec73127c7446c', '95848fadb0461070537d9a519e71e22c', '2023-06-14 21:04:33', '2023-06-14 21:10:18', 0);

-- ----------------------------
-- Table structure for user_interface_info
-- ----------------------------
DROP TABLE IF EXISTS `user_interface_info`;
CREATE TABLE `user_interface_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` bigint NOT NULL COMMENT '调用用户 id',
  `interfaceInfoId` bigint NOT NULL COMMENT '接口 id',
  `totalNum` int NOT NULL DEFAULT 0 COMMENT '总调用次数',
  `leftNum` int NOT NULL DEFAULT 0 COMMENT '剩余调用次数',
  `status` int NOT NULL DEFAULT 0 COMMENT '0-正常，1-禁用',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删, 1-已删)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户调用接口关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_interface_info
-- ----------------------------
INSERT INTO `user_interface_info` VALUES (2, 1667508636207661058, 27, 33, 99999998, 0, '2023-07-09 13:11:12', '2023-07-10 11:24:32', 0);
INSERT INTO `user_interface_info` VALUES (3, 1667508636207661058, 26, 4, 99999998, 0, '2023-07-09 13:56:07', '2023-07-10 11:46:49', 0);
INSERT INTO `user_interface_info` VALUES (7, 1667508636207661058, 24, 7, 99999996, 0, '2023-07-09 17:00:15', '2023-07-10 10:43:53', 0);
INSERT INTO `user_interface_info` VALUES (8, 1667508636207661058, 29, 1, 99999998, 0, '2023-07-09 20:46:03', '2023-07-10 23:45:37', 0);
INSERT INTO `user_interface_info` VALUES (9, 1667508636207661058, 30, 25, 99999974, 0, '2023-07-09 21:17:15', '2023-07-10 23:27:42', 0);
INSERT INTO `user_interface_info` VALUES (13, 1667508636207661058, 1, 2, 99999997, 0, '2023-07-10 10:49:12', '2023-07-10 11:24:01', 0);
INSERT INTO `user_interface_info` VALUES (14, 1667508636207661058, 31, 3, 99999996, 0, '2023-07-10 23:29:44', '2023-07-11 00:06:54', 0);
INSERT INTO `user_interface_info` VALUES (15, 1667508636207661058, 32, 3, 99999996, 0, '2023-07-10 23:45:41', '2023-07-11 00:00:01', 0);
INSERT INTO `user_interface_info` VALUES (16, 1667508636207661058, 33, 2, 99999997, 0, '2023-07-10 23:52:42', '2023-07-10 23:53:44', 0);
INSERT INTO `user_interface_info` VALUES (17, 1667508636207661058, 34, 3, 99999996, 0, '2023-07-10 23:57:23', '2023-07-11 00:06:14', 0);

SET FOREIGN_KEY_CHECKS = 1;












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
    `requestParams`  TEXT                               NULL COMMENT 'Request Params',
    `requestHeader`  TEXT                               NULL COMMENT 'Request Header',
    `responseHeader` TEXT                               NULL COMMENT 'Response Header',
    `status`         INT      DEFAULT 0                 NOT NULL COMMENT 'Interface Status (0-Disabled, 1-Enabled)',
    `method`         VARCHAR(256)                       NOT NULL COMMENT 'Request Type',
    `userId`         BIGINT                             NOT NULL COMMENT 'Creator ID',
    `createTime`     DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Creation Time',
    `updateTime`     DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    `isDelete`       TINYINT  DEFAULT 0                 NOT NULL COMMENT 'Is Deleted (0-Not Deleted, 1-Deleted)'
) COMMENT 'Interface Information';

CREATE TABLE IF NOT EXISTS xiaohangapi.`user_interface_info`
(
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key'  primary key,
    `userId` BIGINT NOT NULL COMMENT 'User ID who is making the API call',
    `interfaceInfoId` BIGINT NOT NULL COMMENT 'API ID',
    `totalNum` INT DEFAULT 0 NOT NULL COMMENT 'Total number of calls',
    `leftNum` INT DEFAULT 0 NOT NULL COMMENT 'Remaining number of calls',
    `status` INT DEFAULT 0 NOT NULL COMMENT '0 - Active, 1 - Disabled',
    `createTime` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Creation time',
    `updateTime` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `isDelete` TINYINT DEFAULT 0 NOT NULL COMMENT 'Deletion status (0 - Not deleted, 1 - Deleted)'
) COMMENT 'User-API relationship';


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

