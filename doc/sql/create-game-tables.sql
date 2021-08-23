-- ----------------------------
-- Table structure for tb_account
-- ----------------------------
CREATE TABLE `tb_account` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 自增 用户ID',
  `gid` tinyint NOT NULL DEFAULT 1 COMMENT 'GROUP ID 用于数据库分库',
  `type` tinyint NOT NULL COMMENT '分类：1 -> 游客账号，2 -> 第三方平台账号',
  `state` tinyint NOT NULL DEFAULT 1 COMMENT '账号状态：1 正常，2 禁用',
  `product_id` int NOT NULL COMMENT '产品ID',
  `channel` varchar(32) NOT NULL COMMENT '渠道',
  `channel_id` int NOT NULL COMMENT '渠道ID',
  `partner` tinyint NOT NULL COMMENT '合作平台：0 游客，1 微信，2 趣头条，3 苹果ID',
  `guest_id` varchar(64) COMMENT '游客ID',
  `openid` varchar(64) COMMENT 'Open ID',
  `unionid` varchar(64) COMMENT 'Union ID',
  `apple_id` varchar(64) COMMENT '苹果ID',
  `ip` varchar(64) NOT NULL COMMENT '创建账号时的IP地址',
  `client_version` varchar(64) NOT NULL COMMENT '创建账号时的客户端版本号',
  `device_id` varchar(64) NOT NULL COMMENT '创建账号时的客户端设备ID',
  `slot` tinyint NOT NULL DEFAULT 0 COMMENT '用于AB测试的编号，0-15，算法：device_id.hash & 0xF',
  `device_info` varchar(255) COMMENT '创建账号时的客户端设备信息',
  `os_info` varchar(255) COMMENT '创建账号时的客户端操作系统信息',
  `imei` varchar(64) COMMENT '创建账号时的客户端设备IMEI',
  `oaid` varchar(64) COMMENT '创建账号时的客户端设备OAID',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  `ext_i1` int NOT NULL DEFAULT 0 COMMENT '预留扩展int字段1',
  `ext_i2` int NOT NULL DEFAULT 0 COMMENT '预留扩展int字段2',
  `ext_i3` int NOT NULL DEFAULT 0 COMMENT '预留扩展int字段3',
  `ext_s1` varchar(255) COMMENT '预留扩展varchar字段1',
  `ext_s2` varchar(255) COMMENT '预留扩展varchar字段2',
  `ext_s3` varchar(255) COMMENT '预留扩展varchar字段3',
  `ext_j1` json COMMENT '预留扩展json字段1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNIQUE_guest_id` (`guest_id`),
  UNIQUE KEY `UNIQUE_openid` (`openid`),
  UNIQUE KEY `UNIQUE_apple_id` (`apple_id`)
) COMMENT = '账号表';

