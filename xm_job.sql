/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80022
 Source Host           : localhost:3306
 Source Schema         : xm_job

 Target Server Type    : MySQL
 Target Server Version : 80022
 File Encoding         : 65001

 Date: 08/08/2025 15:57:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for advertise
-- ----------------------------
DROP TABLE IF EXISTS `advertise`;
CREATE TABLE `advertise`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '主图',
  `position_id` int(0) NULL DEFAULT NULL COMMENT '职位ID',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '位置',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '广告信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_interview_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_interview_message`;
CREATE TABLE `ai_interview_message`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '消息主键ID',
  `session_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID (关联到 ai_interview_session 表)',
  `message_order` int(0) NOT NULL COMMENT '消息在会话中的顺序 (从0开始)',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色 (system, user, assistant)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容',
  `created_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_id_order`(`session_id`, `message_order`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI模拟面试消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_interview_session
-- ----------------------------
DROP TABLE IF EXISTS `ai_interview_session`;
CREATE TABLE `ai_interview_session`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID (使用UUID)',
  `user_id` int(0) NOT NULL COMMENT '用户ID (关联到你的 user 表)',
  `job_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '面试岗位名称',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'STARTED' COMMENT '会话状态 (例如: STARTED, COMPLETED, ABANDONED)',
  `created_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI模拟面试会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for interview_session
-- ----------------------------
DROP TABLE IF EXISTS `interview_session`;
CREATE TABLE `interview_session`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` int(0) NOT NULL,
  `job_position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `interview_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `difficulty` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `duration_minutes` int(0) NOT NULL,
  `interaction_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TEXT',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CREATED',
  `current_question_no` int(0) NOT NULL DEFAULT 0,
  `version` int(0) NOT NULL DEFAULT 0,
  `started_at` datetime(0) NULL DEFAULT NULL,
  `finished_at` datetime(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_interview_session_user`(`user_id`, `created_at`) USING BTREE,
  INDEX `idx_interview_session_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '企业级AI面试会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for interview_message
-- ----------------------------
DROP TABLE IF EXISTS `interview_message`;
CREATE TABLE `interview_message`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `session_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `message_order` int(0) NOT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `modality` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TEXT',
  `question_id` int(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_interview_message_order`(`session_id`, `message_order`) USING BTREE,
  INDEX `idx_interview_message_session`(`session_id`, `created_at`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '企业级AI面试消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for interview_report
-- ----------------------------
DROP TABLE IF EXISTS `interview_report`;
CREATE TABLE `interview_report`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `session_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_score` decimal(5, 2) NOT NULL DEFAULT 0,
  `dimension_scores` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `strengths` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `weaknesses` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `suggestions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `next_training` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `raw_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'READY',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_interview_report_session`(`session_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '企业级AI面试报告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for question_bank
-- ----------------------------
DROP TABLE IF EXISTS `question_bank`;
CREATE TABLE `question_bank`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `interview_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `job_direction` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `difficulty` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_question_bank_type`(`interview_type`, `difficulty`) USING BTREE,
  INDEX `idx_question_bank_enabled`(`enabled`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI面试题库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for interview_question
-- ----------------------------
DROP TABLE IF EXISTS `interview_question`;
CREATE TABLE `interview_question`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `title` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `interview_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `difficulty` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `reference_answer` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `follow_up_points` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `scoring_dimensions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_interview_question_type`(`interview_type`, `difficulty`) USING BTREE,
  INDEX `idx_interview_question_enabled`(`enabled`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI面试题目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for question_bank_question
-- ----------------------------
DROP TABLE IF EXISTS `question_bank_question`;
CREATE TABLE `question_bank_question`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `question_bank_id` int(0) NOT NULL,
  `question_id` int(0) NOT NULL,
  `sort_order` int(0) NOT NULL DEFAULT 0,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_question_bank_question`(`question_bank_id`, `question_id`) USING BTREE,
  INDEX `idx_qbq_bank_order`(`question_bank_id`, `sort_order`) USING BTREE,
  INDEX `idx_qbq_question`(`question_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI面试题库题目关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for scoring_rule
-- ----------------------------
DROP TABLE IF EXISTS `scoring_rule`;
CREATE TABLE `scoring_rule`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `interview_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `dimension` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `criteria` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `weight` decimal(5, 2) NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_scoring_rule_type`(`interview_type`) USING BTREE,
  INDEX `idx_scoring_rule_enabled`(`enabled`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI面试评分规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for collect
-- ----------------------------
DROP TABLE IF EXISTS `collect`;
CREATE TABLE `collect`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` int(0) NULL DEFAULT NULL COMMENT '学生ID',
  `position_id` int(0) NULL DEFAULT NULL COMMENT '岗位ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for employ
-- ----------------------------
DROP TABLE IF EXISTS `employ`;
CREATE TABLE `employ`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'logo',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  `city` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `industry_id` int(0) NULL DEFAULT NULL COMMENT '行业id',
  `scale` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '规模',
  `stage` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '融资',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '企业信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for industry
-- ----------------------------
DROP TABLE IF EXISTS `industry`;
CREATE TABLE `industry`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '行业名称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '行业描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '行业信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for interview_scenario
-- ----------------------------
DROP TABLE IF EXISTS `interview_scenario`;
CREATE TABLE `interview_scenario`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `position_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '岗位名称 (如: 高级Java工程师)',
  `company_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模拟公司 (如: 阿里巴巴)',
  `industry_id` int(0) NULL DEFAULT NULL COMMENT '所属行业ID',
  `difficulty` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '难度级别 (如: 初级, 中级, 高级)',
  `job_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '职位描述 (Job Description)',
  `core_competencies` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '核心考察能力 (逗号分隔, 如: Java并发,分布式,JVM)',
  `evaluation_focus` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评估侧重点 (给AI看的)',
  `sample_questions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '示例问题建议 (JSON数组格式)',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用 (1: 启用, 0: 禁用)',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_industry_id`(`industry_id`) USING BTREE,
  INDEX `idx_difficulty`(`difficulty`) USING BTREE,
  INDEX `idx_is_active`(`is_active`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '深度面试场景表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '公告内容',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for position
-- ----------------------------
DROP TABLE IF EXISTS `position`;
CREATE TABLE `position`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '职位名称',
  `employ_id` int(0) NULL DEFAULT NULL COMMENT '企业ID',
  `industry_id` int(0) NULL DEFAULT NULL COMMENT '行业ID',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '求职类型',
  `experience` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工作经验',
  `salary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '薪资待遇',
  `education` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学历要求',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '职位标签',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '职位描述',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '职位状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '职位信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for resume
-- ----------------------------
DROP TABLE IF EXISTS `resume`;
CREATE TABLE `resume`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '简历名称',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `sex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
  `salary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '期望薪资',
  `education` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学历',
  `experience` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工作年限',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `edu_exps` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '教育经历',
  `work_exps` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '工作经历',
  `pro_exps` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '项目经验',
  `user_id` int(0) NULL DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '简历信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for submit
-- ----------------------------
DROP TABLE IF EXISTS `submit`;
CREATE TABLE `submit`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employ_id` int(0) NULL DEFAULT NULL COMMENT '企业ID',
  `position_id` int(0) NULL DEFAULT NULL COMMENT '岗位ID',
  `user_id` int(0) NULL DEFAULT NULL COMMENT '用户ID',
  `resume_id` int(0) NULL DEFAULT NULL COMMENT '简历ID',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '投递时间',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '简历状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '简历投递表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Seed data for enterprise AI interview question banks
-- ----------------------------
INSERT INTO `question_bank`
    (`id`, `title`, `description`, `interview_type`, `job_direction`, `difficulty`, `tags`, `enabled`)
VALUES
    (1001, 'Java 后端技术面试基础题库', '覆盖 Spring Boot、数据库、接口设计和工程实践的通用技术面试题。', 'TECHNICAL', 'Java 后端', 'MEDIUM', 'Java,Spring Boot,MySQL,接口设计', 1),
    (1002, '产品经理结构化面试题库', '覆盖需求分析、指标拆解、跨团队协作和业务判断。', 'TECHNICAL', '产品经理', 'MEDIUM', '产品设计,需求分析,数据指标,协作', 1),
    (1003, 'HR 通用素质面试题库', '用于考察职业动机、稳定性、团队协作与岗位匹配度。', 'HR', '通用岗位', 'EASY', '职业规划,沟通协作,稳定性', 1),
    (1004, 'STAR 行为面试题库', '围绕情境、任务、行动、结果组织追问，评估复盘能力。', 'BEHAVIORAL', '通用岗位', 'MEDIUM', 'STAR,行为面试,复盘,抗压', 1)
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `description` = VALUES(`description`),
    `interview_type` = VALUES(`interview_type`),
    `job_direction` = VALUES(`job_direction`),
    `difficulty` = VALUES(`difficulty`),
    `tags` = VALUES(`tags`),
    `enabled` = VALUES(`enabled`);

INSERT INTO `interview_question`
    (`id`, `title`, `content`, `interview_type`, `difficulty`, `tags`, `reference_answer`, `follow_up_points`, `scoring_dimensions`, `enabled`)
VALUES
    (2001, 'Spring Boot 请求链路说明', '请说明一个 HTTP 请求在 Spring Boot 后端从 Controller 到数据库返回结果的主要链路，并指出你会在哪些位置处理异常和日志。', 'TECHNICAL', 'MEDIUM', 'Spring Boot,分层架构,异常处理', '回答应覆盖 Controller、Service、Mapper、MyBatis、统一 Result、异常处理和必要的日志定位；能说明职责边界更佳。', '如果接口变慢，你会先排查哪几层？如何证明瓶颈在 SQL 还是业务逻辑？', '架构理解:35,工程实践:30,问题定位:20,表达清晰度:15', 1),
    (2002, 'MySQL 索引与分页优化', '一个岗位列表接口需要按行业、城市和发布时间分页查询，你会如何设计索引和分页方案？', 'TECHNICAL', 'MEDIUM', 'MySQL,索引,分页,性能优化', '回答应提到组合索引、选择性、排序字段、覆盖索引、深分页风险，以及必要时使用游标或延迟关联。', '如果用户可以按薪资区间筛选，索引设计会如何调整？', '数据库能力:40,性能意识:30,业务权衡:20,表达清晰度:10', 1),
    (2003, 'AI 面试报告可靠性设计', '如果 AI 生成的面试报告偶尔格式不稳定，你会如何设计后端解析、兜底和前端展示？', 'TECHNICAL', 'HARD', 'AI应用,报告生成,容错设计', '回答应覆盖结构化提示词、JSON 解析兜底、原文保存、默认评分、异常提示、重试策略和人工可追踪性。', '如何避免一次生成失败影响用户查看历史记录？', 'AI工程化:35,容错设计:30,用户体验:20,可观测性:15', 1),
    (2004, '产品指标拆解', '假设 AI 模拟面试功能上线后使用率不高，你会如何拆解指标并设计改进方案？', 'TECHNICAL', 'MEDIUM', '产品经理,指标拆解,增长分析', '回答应包含曝光、点击、创建、完成、报告查看、复访等漏斗，并结合用户访谈、A/B 实验和体验优化提出方案。', '你会优先优化创建入口、面试流程还是报告页？为什么？', '指标思维:35,用户洞察:25,方案优先级:25,表达清晰度:15', 1),
    (2005, '需求冲突处理', '业务方希望快速上线，研发认为质量风险较高，你会如何推进这个需求？', 'TECHNICAL', 'MEDIUM', '产品经理,协作,优先级', '回答应体现目标对齐、风险拆解、范围裁剪、分阶段交付、验收标准和透明沟通。', '如果只能延期或降范围二选一，你会怎么说服相关方？', '协作能力:35,风险意识:25,取舍能力:25,沟通表达:15', 1),
    (2006, '职业动机说明', '请介绍你为什么选择这个岗位，以及你认为自己和岗位最匹配的三点是什么。', 'HR', 'EASY', '职业动机,岗位匹配,自我认知', '回答应具体连接岗位职责、自身经历和长期方向，避免空泛表达。', '你最近一次主动学习与该岗位相关的内容是什么？', '岗位匹配:35,自我认知:30,稳定性:20,表达清晰度:15', 1),
    (2007, '团队协作经历', '请讲一次你和团队成员意见不一致但最终推动事情落地的经历。', 'BEHAVIORAL', 'MEDIUM', 'STAR,团队协作,冲突处理', '回答应按 STAR 描述背景、分歧、行动、结果和复盘，体现沟通与推进能力。', '如果再来一次，你会在哪个环节做得更好？', 'STAR结构:30,协作能力:30,结果意识:25,复盘能力:15', 1),
    (2008, '压力场景复盘', '请讲一次你在时间紧或资源不足的情况下完成任务的经历。', 'BEHAVIORAL', 'MEDIUM', 'STAR,抗压,项目推进', '回答应说明约束、优先级判断、关键行动、结果数据和经验沉淀。', '当时你放弃了什么？这个取舍带来了什么影响？', '压力管理:30,优先级判断:30,结果量化:25,复盘能力:15', 1)
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `content` = VALUES(`content`),
    `interview_type` = VALUES(`interview_type`),
    `difficulty` = VALUES(`difficulty`),
    `tags` = VALUES(`tags`),
    `reference_answer` = VALUES(`reference_answer`),
    `follow_up_points` = VALUES(`follow_up_points`),
    `scoring_dimensions` = VALUES(`scoring_dimensions`),
    `enabled` = VALUES(`enabled`);

INSERT INTO `question_bank_question`
    (`id`, `question_bank_id`, `question_id`, `sort_order`)
VALUES
    (4001, 1001, 2001, 10),
    (4002, 1001, 2002, 20),
    (4003, 1001, 2003, 30),
    (4004, 1002, 2004, 10),
    (4005, 1002, 2005, 20),
    (4006, 1003, 2006, 10),
    (4007, 1004, 2007, 10),
    (4008, 1004, 2008, 20)
ON DUPLICATE KEY UPDATE
    `question_bank_id` = VALUES(`question_bank_id`),
    `question_id` = VALUES(`question_id`),
    `sort_order` = VALUES(`sort_order`);

INSERT INTO `scoring_rule`
    (`id`, `interview_type`, `dimension`, `criteria`, `weight`, `enabled`)
VALUES
    (3001, 'TECHNICAL', '专业深度', '是否能解释原理、边界条件、取舍依据和落地细节。', 35.00, 1),
    (3002, 'TECHNICAL', '逻辑结构', '回答是否分层清晰，能否从问题、方案、验证三个层面展开。', 25.00, 1),
    (3003, 'TECHNICAL', '工程实践', '是否体现稳定性、可维护性、性能、数据一致性等工程意识。', 25.00, 1),
    (3004, 'TECHNICAL', '表达沟通', '是否能用简洁语言说明复杂问题，并主动补充关键上下文。', 15.00, 1),
    (3005, 'HR', '岗位匹配', '职业动机、经历积累和目标岗位要求是否一致。', 35.00, 1),
    (3006, 'HR', '稳定性与成长性', '是否具备持续学习、抗压和长期发展的倾向。', 30.00, 1),
    (3007, 'HR', '沟通表达', '表达是否真诚、具体、有结构，能否回应追问。', 35.00, 1),
    (3008, 'BEHAVIORAL', 'STAR 完整度', '是否完整覆盖情境、任务、行动和结果。', 30.00, 1),
    (3009, 'BEHAVIORAL', '行动质量', '关键行动是否体现主动性、协作和解决问题能力。', 35.00, 1),
    (3010, 'BEHAVIORAL', '复盘沉淀', '是否能总结经验、解释取舍并迁移到未来场景。', 35.00, 1)
ON DUPLICATE KEY UPDATE
    `interview_type` = VALUES(`interview_type`),
    `dimension` = VALUES(`dimension`),
    `criteria` = VALUES(`criteria`),
    `weight` = VALUES(`weight`),
    `enabled` = VALUES(`enabled`);

SET FOREIGN_KEY_CHECKS = 1;
