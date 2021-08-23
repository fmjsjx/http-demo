-- ----------------------------
-- Table structure for tb_device_activation_log
-- ----------------------------
CREATE TABLE `tb_device_activation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
  `product_id` int NOT NULL COMMENT '产品ID',
  `channel` varchar(32) NOT NULL COMMENT '渠道',
  `channel_id` int NOT NULL COMMENT '渠道ID',
  `ip` varchar(64) NOT NULL COMMENT '最初的IP地址',
  `client_version` varchar(64) NOT NULL COMMENT '生成账号时的客户端版本号',
  `device_id` varchar(64) NOT NULL COMMENT '生成账号时的客户端设备ID',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `INDEX_create_time` (`create_time` DESC),
  UNIQUE `UNIQUE_device_id` (`device_id`)
) COMMENT = '设备激活日志表';


-- ----------------------------
-- Table structure for tb_item_log
-- ----------------------------
CREATE TABLE `tb_item_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
  `uid` int NOT NULL COMMENT '账号ID',
  `product_id` int NOT NULL COMMENT '产品ID',
  `channel` varchar(32) NOT NULL COMMENT '登录渠道',
  `channel_id` int NOT NULL COMMENT '登录渠道ID',
  `client_version` varchar(64) NOT NULL COMMENT '登录时的客户端版本号',
  `device_id` varchar(64) NOT NULL COMMENT '登录时的客户端设备ID',
  `slot` tinyint NOT NULL COMMENT '用于AB测试的编号，0-15',
  `item_id` int NOT NULL COMMENT '道具id',
  `original` bigint NOt NULL COMMENT '更新前原数量',
  `number` bigint NOT NULL COMMENT '更新数量',
  `source_id` int NOT NULL COMMENT '来源id',
  `remark` varchar(255) NULL COMMENT '备注',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `INDEX_create_time` (`create_time` DESC)
) COMMENT = '道具更新日志表';


-- ----------------------------
-- Table structure for tb_event_log
-- ----------------------------
CREATE TABLE `tb_event_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
  `uid` int NOT NULL COMMENT '账号ID',
  `product_id` int NOT NULL COMMENT '产品ID',
  `channel` varchar(32) NOT NULL COMMENT '登录渠道',
  `channel_id` int NOT NULL COMMENT '登录渠道ID',
  `client_version` varchar(64) NOT NULL COMMENT '登录时的客户端版本号',
  `device_id` varchar(64) NOT NULL COMMENT '登录时的客户端设备ID',
  `slot` tinyint NOT NULL COMMENT '用于AB测试的编号，0-15',
  `event` varchar(64) NOT NULL COMMENT '玩家事件',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '事件时间',
  `data` json NOT NULL COMMENT '事件具体数据',
  PRIMARY KEY (`id`),
  KEY `INDEX_create_time_event` (`create_time` DESC, `event`)
) COMMENT = '玩家操作日志总事件表';


-- ----------------------------
-- Table structure for tb_auth_account_log
-- ----------------------------
CREATE TABLE `tb_auth_account_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
  `uid` int NOT NULL COMMENT '账号ID',
  `product_id` int NOT NULL COMMENT '产品ID',
  `channel` varchar(32) NOT NULL COMMENT '登录渠道',
  `channel_id` int NOT NULL COMMENT '登录渠道ID',
  `client_version` varchar(64) NOT NULL COMMENT '登录时的客户端版本号',
  `device_id` varchar(64) NOT NULL COMMENT '登录时的客户端设备ID',
  `slot` tinyint NOT NULL COMMENT '用于AB测试的编号，0-15',
  `event` varchar(64) NOT NULL COMMENT '玩家事件',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '事件时间',
  `data` json NOT NULL COMMENT '事件具体数据',
  `type` tinyint GENERATED ALWAYS AS (`data`->>'$.type') VIRTUAL COMMENT '分类：1 => 游客账号，2 => 合作平台账号',
  `partner` tinyint GENERATED ALWAYS AS (`data`->>'$.partner') VIRTUAL COMMENT '合作平台：0 => 无，1 => 微信',
  `guest_id` varchar(255) GENERATED ALWAYS AS (`data`->>'$.type') VIRTUAL COMMENT '游客ID',
  `openid` varchar(255) GENERATED ALWAYS AS (`data`->>'$.openid') VIRTUAL COMMENT 'open ID',
  `unionid` varchar(255) GENERATED ALWAYS AS (`data`->>'$.unionid') VIRTUAL COMMENT 'union ID',
  `apple_id` varchar(255) GENERATED ALWAYS AS (`data`->>'$.apple_id') VIRTUAL COMMENT 'Apple ID',
  `ip` varchar(64) GENERATED ALWAYS AS (`data`->>'$.ip') VIRTUAL COMMENT 'IP',
  `imei` varchar(255) GENERATED ALWAYS AS (`data`->>'$.imei') VIRTUAL COMMENT 'IMEI',
  `oaid` varchar(255) GENERATED ALWAYS AS (`data`->>'$.oaid') VIRTUAL COMMENT 'OAID',
  `device` varchar(255) GENERATED ALWAYS AS (`data`->>'$.device') VIRTUAL COMMENT '设备信息',
  `os` varchar(255) GENERATED ALWAYS AS (`data`->>'$.os') VIRTUAL COMMENT '操作系统信息',
  `wlx_platform` varchar(255) GENERATED ALWAYS AS (`data`->>'$.wlx_platform') VIRTUAL COMMENT '外拉新平台',
  PRIMARY KEY (`id`),
  KEY `INDEX_create_time` (`create_time` DESC)
) COMMENT = '玩家创建账号事件表';


-- ----------------------------
-- Table structure for tb_auth_login_log
-- ----------------------------
CREATE TABLE `tb_auth_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
  `uid` int NOT NULL COMMENT '账号ID',
  `product_id` int NOT NULL COMMENT '产品ID',
  `channel` varchar(32) NOT NULL COMMENT '登录渠道',
  `channel_id` int NOT NULL COMMENT '登录渠道ID',
  `client_version` varchar(64) NOT NULL COMMENT '登录时的客户端版本号',
  `device_id` varchar(64) NOT NULL COMMENT '登录时的客户端设备ID',
  `slot` tinyint NOT NULL COMMENT '用于AB测试的编号，0-15',
  `event` varchar(64) NOT NULL COMMENT '玩家事件',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '事件时间',
  `data` json NOT NULL COMMENT '事件具体数据',
  `type` tinyint GENERATED ALWAYS AS (`data`->>'$.type') VIRTUAL COMMENT '分类：1 => 游客账号，2 => 合作平台账号',
  `partner` tinyint GENERATED ALWAYS AS (`data`->>'$.partner') VIRTUAL COMMENT '合作平台：0 => 无，1 => 微信',
  `guest_id` varchar(255) GENERATED ALWAYS AS (`data`->>'$.type') VIRTUAL COMMENT '游客ID',
  `openid` varchar(255) GENERATED ALWAYS AS (`data`->>'$.openid') VIRTUAL COMMENT 'open ID',
  `unionid` varchar(255) GENERATED ALWAYS AS (`data`->>'$.unionid') VIRTUAL COMMENT 'union ID',
  `apple_id` varchar(255) GENERATED ALWAYS AS (`data`->>'$.apple_id') VIRTUAL COMMENT 'Apple ID',
  `ip` varchar(64) GENERATED ALWAYS AS (`data`->>'$.ip') VIRTUAL COMMENT 'IP',
  `imei` varchar(255) GENERATED ALWAYS AS (`data`->>'$.imei') VIRTUAL COMMENT 'IMEI',
  `oaid` varchar(255) GENERATED ALWAYS AS (`data`->>'$.oaid') VIRTUAL COMMENT 'OAID',
  `device` varchar(255) GENERATED ALWAYS AS (`data`->>'$.device') VIRTUAL COMMENT '设备信息',
  `os` varchar(255) GENERATED ALWAYS AS (`data`->>'$.os') VIRTUAL COMMENT '操作系统信息',
  PRIMARY KEY (`id`),
  KEY `INDEX_create_time` (`create_time` DESC)
) COMMENT = '玩家登录事件表';
