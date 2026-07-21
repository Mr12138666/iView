-- First enterprise interview-platform schema.
-- The existing ai_interview_* tables are retained for backward compatibility.

CREATE TABLE IF NOT EXISTS interview_session (
    id varchar(36) NOT NULL,
    user_id int NOT NULL,
    job_position varchar(100) NOT NULL,
    interview_type varchar(32) NOT NULL,
    difficulty varchar(32) NOT NULL,
    duration_minutes int NOT NULL,
    interaction_mode varchar(32) NOT NULL DEFAULT 'TEXT',
    status varchar(20) NOT NULL DEFAULT 'CREATED',
    current_question_no int NOT NULL DEFAULT 0,
    version int NOT NULL DEFAULT 0,
    started_at datetime NULL,
    finished_at datetime NULL,
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_interview_session_user (user_id, created_at),
    KEY idx_interview_session_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS interview_message (
    id bigint NOT NULL AUTO_INCREMENT,
    session_id varchar(36) NOT NULL,
    message_order int NOT NULL,
    role varchar(20) NOT NULL,
    content text NOT NULL,
    modality varchar(20) NOT NULL DEFAULT 'TEXT',
    question_id int NULL,
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_interview_message_order (session_id, message_order),
    KEY idx_interview_message_session (session_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS interview_report (
    id bigint NOT NULL AUTO_INCREMENT,
    session_id varchar(36) NOT NULL,
    total_score decimal(5,2) NOT NULL DEFAULT 0,
    dimension_scores text NOT NULL,
    strengths text NOT NULL,
    weaknesses text NOT NULL,
    suggestions text NOT NULL,
    next_training text NOT NULL,
    raw_content longtext NULL,
    status varchar(20) NOT NULL DEFAULT 'READY',
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_interview_report_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
