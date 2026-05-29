-- interview_simulator database initialization
-- Run this script to create all required tables
-- 创建库
create database if not exists ai_interview;

-- 切换库
use ai_interview;

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `interview` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `direction` VARCHAR(20) NOT NULL COMMENT '面试方向：java_backend/ai_dev/fullstack',
  `difficulty` VARCHAR(10) NOT NULL COMMENT '难度：junior/mid/senior',
  `interview_type` VARCHAR(20) NOT NULL COMMENT '类型：knowledge/project/comprehensive',
  `status` VARCHAR(20) NOT NULL COMMENT '状态：in_progress/completed',
  `total_score` DECIMAL(3,1) COMMENT '总评分',
  `duration_seconds` INT DEFAULT 0 COMMENT '面试时长(秒)',
  `question_count` INT DEFAULT 0 COMMENT '提问数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) COMMENT='面试记录表';

CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `interview_id` BIGINT NOT NULL,
  `role` VARCHAR(20) NOT NULL COMMENT '角色：interviewer/candidate',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `topic` VARCHAR(50) COMMENT '当前考察的知识点',
  `score` DECIMAL(3,1) COMMENT '该回答评分(1-10)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_interview_id` (`interview_id`)
) COMMENT='面试消息表';

CREATE TABLE IF NOT EXISTS `interview_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `interview_id` BIGINT NOT NULL UNIQUE,
  `overall_score` DECIMAL(3,1) COMMENT '总评分',
  `summary` TEXT COMMENT '面试总结',
  `improvement` TEXT COMMENT '改进建议',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) COMMENT='面试报告表';

CREATE TABLE IF NOT EXISTS `topic_score` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `report_id` BIGINT NOT NULL,
  `topic` VARCHAR(50) NOT NULL COMMENT '知识点',
  `score` DECIMAL(3,1) NOT NULL COMMENT '评分',
  `comment` TEXT COMMENT '评语',
  `is_weak` TINYINT DEFAULT 0 COMMENT '是否薄弱项',
  PRIMARY KEY (`id`),
  KEY `idx_report_id` (`report_id`)
) COMMENT='知识点评分表';
