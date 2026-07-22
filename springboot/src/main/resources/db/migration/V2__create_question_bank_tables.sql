CREATE TABLE IF NOT EXISTS question_bank (
    id int NOT NULL AUTO_INCREMENT,
    title varchar(100) NOT NULL,
    description text NULL,
    interview_type varchar(32) NOT NULL,
    job_direction varchar(100) NULL,
    difficulty varchar(32) NULL,
    tags varchar(255) NULL,
    enabled tinyint NOT NULL DEFAULT 1,
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_question_bank_type (interview_type, difficulty),
    KEY idx_question_bank_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS interview_question (
    id int NOT NULL AUTO_INCREMENT,
    title varchar(150) NOT NULL,
    content text NOT NULL,
    interview_type varchar(32) NOT NULL,
    difficulty varchar(32) NOT NULL,
    tags varchar(255) NULL,
    reference_answer longtext NULL,
    follow_up_points text NULL,
    scoring_dimensions text NULL,
    enabled tinyint NOT NULL DEFAULT 1,
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_interview_question_type (interview_type, difficulty),
    KEY idx_interview_question_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS question_bank_question (
    id int NOT NULL AUTO_INCREMENT,
    question_bank_id int NOT NULL,
    question_id int NOT NULL,
    sort_order int NOT NULL DEFAULT 0,
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_question_bank_question (question_bank_id, question_id),
    KEY idx_qbq_bank_order (question_bank_id, sort_order),
    KEY idx_qbq_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS scoring_rule (
    id int NOT NULL AUTO_INCREMENT,
    interview_type varchar(32) NOT NULL,
    dimension varchar(80) NOT NULL,
    criteria text NULL,
    weight decimal(5,2) NOT NULL,
    enabled tinyint NOT NULL DEFAULT 1,
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_scoring_rule_type (interview_type),
    KEY idx_scoring_rule_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
