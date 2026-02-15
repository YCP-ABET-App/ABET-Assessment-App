CREATE DATABASE IF NOT EXISTS local_db;
-- H2 Compatible Schema for ABET Assessment App

DROP TABLE IF EXISTS measure;
DROP TABLE IF EXISTS course_indicator;
DROP TABLE IF EXISTS course_instructor;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS performance_indicator;
DROP TABLE IF EXISTS student_outcome;
DROP TABLE IF EXISTS semester;
DROP TABLE IF EXISTS program_user;
DROP TABLE IF EXISTS program;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name_first VARCHAR(100) NOT NULL,
    name_last VARCHAR(100) NOT NULL,
    name_title VARCHAR(50) NULL,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- Users-specific
    is_active BOOLEAN DEFAULT TRUE NOT NULL
    );

    -- Program table
    CREATE TABLE program (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     program_name VARCHAR(255) NOT NULL,
     institution VARCHAR(255) NOT NULL,
    -- From BaseEntity
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
     version BIGINT DEFAULT 0,
     deleted BOOLEAN DEFAULT FALSE NOT NULL,
     deleted_at TIMESTAMP NULL,
    -- Program-specific
     is_active BOOLEAN DEFAULT TRUE NOT NULL
);

-- ProgramUser table
CREATE TABLE program_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    isAdmin BOOLEAN NOT NULL DEFAULT FALSE,
    program_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- ProgramUser-specific
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    FOREIGN KEY (program_id) REFERENCES program(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
    );

    -- Semester table
    CREATE TABLE semester (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    type VARCHAR(10) NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'UPCOMING',
    start_date DATE NULL,
    end_date DATE NULL,
    academic_year INT NULL,
    description VARCHAR(500) NULL,
    program_id BIGINT NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (program_id) REFERENCES program(id)
);

-- Student outcomes table
CREATE TABLE student_outcome (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    out_number TINYINT NOT NULL,
    -- Number represents order of outcomes
    out_value TINYINT NULL,
    -- Value represents number assigned during evaluation
    out_description TEXT NOT NULL,
    evaluation TEXT NULL,
    semester_id BIGINT NOT NULL,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- StudentOutcome-specific
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    FOREIGN KEY (semester_id) REFERENCES semester(id)
);

-- Performance indicators table
CREATE TABLE performance_indicator (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ind_number TINYINT NOT NULL,
    -- Number represents order of outcomes
    ind_value TINYINT NULL,
    -- Value represents number assigned during evaluation
    ind_description TEXT NOT NULL,
    evaluation TEXT NULL,
    student_outcome_id BIGINT NOT NULL,
    threshold_percentage DECIMAL(5,2) DEFAULT 70.00,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- PerformanceIndicator-specific
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    FOREIGN KEY (student_outcome_id) REFERENCES student_outcome(id)
);

-- Course table
CREATE TABLE course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL,
    course_name VARCHAR(255) NOT NULL,
    course_description TEXT NOT NULL,
    semester_id BIGINT NOT NULL,
    student_count INT NULL,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- Course-specific
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    FOREIGN KEY (semester_id) REFERENCES semester(id),
    CONSTRAINT unique_course_per_semester UNIQUE (course_code, semester_id));

-- CourseInstructor table
CREATE TABLE course_instructor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    program_user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- CourseInstructor-specific
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    FOREIGN KEY (program_user_id) REFERENCES program_user(id),
    FOREIGN KEY (course_id) REFERENCES course(id)
);

-- CourseIndicator table
CREATE TABLE course_indicator (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    indicator_id BIGINT NOT NULL,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- CourseIndicator-specific
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    FOREIGN KEY (course_id) REFERENCES course(id),
    FOREIGN KEY (indicator_id) REFERENCES performance_indicator(id)
);

-- Measure table
CREATE TABLE measure (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_indicator_id BIGINT NOT NULL,
    measure_description TEXT NOT NULL,
    observation TEXT NULL,
    recommended_action TEXT NULL,
    fcar TEXT NULL,
    met SMALLINT NULL,
    exceeded SMALLINT NULL,
    below SMALLINT NULL,
    m_status VARCHAR(10) DEFAULT 'InProgress' NOT NULL,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- Measure-specific
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    FOREIGN KEY (course_indicator_id) REFERENCES course_indicator(id)
);


INSERT INTO users (email, password_hash, name_first, name_last, name_title)
VALUES
    ('john.doe@university.edu', '$2a$10$k3ohvw57L0aMryaJpFAet.hfSjsWiXalzsVouNQGdsfI48uCjgIEa', 'John', 'Doe', 'Dr.'),
    ('jane.smith@university.edu', '$2a$10$k3ohvw57L0aMryaJpFAet.hfSjsWiXalzsVouNQGdsfI48uCjgIEa', 'Jane', 'Smith', 'Prof.'),
    ('admin@university.edu', '$2a$10$NyeKnPTpI8OxJmYnCHanceO/hPFw8lfk/ZQFWBqq7rB.cl9H6vwwq', 'System', 'Administrator', NULL),
    ('mary.johnson@university.edu', '$2a$10$k3ohvw57L0aMryaJpFAet.hfSjsWiXalzsVouNQGdsfI48uCjgIEa', 'Mary', 'Johnson', 'Dr.'),
    ('rickardo.wade@university.edu', '$2a$10$k3ohvw57L0aMryaJpFAet.hfSjsWiXalzsVouNQGdsfI48uCjgIEa', 'Rickardo', 'Wade', 'Prof.');

INSERT INTO program (id, program_name, institution, is_active)
VALUES
    (1, 'Computer Engineering', 'Example University', TRUE),
    (2, 'Electrical Engineering', 'Example University', TRUE);

INSERT INTO program_user (id, isAdmin, program_id, user_id, is_active)
VALUES
    (1, FALSE, 1, 1, TRUE),
    (2, FALSE, 1, 2, TRUE),
    (3, TRUE, 1, 3, TRUE),
    (4, FALSE, 2, 4, TRUE),
    (5, FALSE, 2, 5, TRUE);

INSERT INTO semester (id, name, code, type, status, start_date, end_date, academic_year, description, program_id, is_current)
VALUES
    (1, 'Fall 2024', 'FALL-2024', 'FALL', 'COMPLETED', '2024-08-26', '2024-12-15', 2024, 'Fall semester for Computer Engineering program', 1, FALSE),
    (2, 'Fall 2025', 'FALL-2025', 'FALL', 'ACTIVE', '2025-08-25', '2025-12-14', 2025, 'Current fall semester for Computer Engineering program', 1, TRUE),
    (3, 'Spring 2024', 'SPRING-2024', 'SPRING', 'COMPLETED', '2024-01-15', '2024-05-10', 2024, 'Spring semester for Electrical Engineering program', 2, FALSE);

INSERT INTO student_outcome (id, out_number, out_value, out_description, semester_id, is_active)
VALUES
    (1, 1, NULL, 'Ability to identify, formulate, and solve complex engineering problems', 2, TRUE),
    (2, 2, NULL, 'Ability to apply engineering design to produce solutions', 2, TRUE),
    (3, 3, NULL, 'Ability to communicate effectively with a range of audiences', 2, TRUE),
    (4, 4, NULL, 'Ability to function effectively on a team', 2, TRUE),
    (5, 5, NULL, 'Ability to recognize ethical and professional responsibilities in engineering situations', 2, TRUE),
    (6, 6, NULL, 'Ability to develop and conduct appropriate experimentation and analysis', 2, TRUE);

INSERT INTO performance_indicator (id, ind_number, ind_value, ind_description,
                                   evaluation, student_outcome_id, threshold_percentage, is_active)
VALUES
    (1, 1, NULL, 'Solves engineering problems analytically', NULL, 1, 70.00, TRUE),
    (2, 2, NULL, 'Applies mathematical principles correctly', NULL, 1, 70.00, TRUE),
    (3, 1, NULL, 'Produces design alternatives meeting constraints', NULL, 2, 70.00, TRUE),
    (4, 2, NULL, 'Uses design tools effectively', NULL, 2, 70.00, TRUE),
    (5, 1, NULL, 'Delivers clear written communication', NULL, 3, 70.00, TRUE),
    (6, 2, NULL, 'Delivers clear oral communication', NULL, 3, 70.00, TRUE),
    (7, 1, NULL, 'Works effectively within diverse teams', NULL, 4, 70.00, TRUE),
    (8, 2, NULL, 'Contributes to team decisions', NULL, 4, 70.00, TRUE),
    (9, 3, NULL, 'Leadership within team environment', NULL, 4, 70.00, TRUE),
    (10, 4, NULL, 'Conflict resolution skills', NULL, 4, 70.00, TRUE),
    (11, 1, NULL, 'Identifies ethical issues in engineering contexts', NULL, 5, 70.00, TRUE),
    (12, 2, NULL, 'Applies professional codes of conduct', NULL, 5, 70.00, TRUE),
    (13, 1, NULL, 'Designs and conducts experiments', NULL, 6, 70.00, TRUE),
    (14, 2, NULL, 'Analyzes and interprets experimental data', NULL, 6, 70.00, TRUE);

INSERT INTO course (id, course_code, course_name, course_description,
                    semester_id, student_count, is_active)
VALUES
    (1, 'CE101', 'Intro to Engineering', 'Fundamentals of engineering practice.', 1, 45, TRUE),
    (2, 'CE202', 'Circuit Analysis', 'Analysis of electric circuits.', 1, 40, TRUE),
    (3, 'CE350', 'Systems Design', 'Design and analysis of engineering systems.', 2, 30, TRUE),
    (4, 'EE210', 'Digital Logic', 'Digital systems and logic circuits.', 3, 50, TRUE);


INSERT INTO course_instructor (id, program_user_id, course_id, is_active)
VALUES
    (1, 2, 1, TRUE),
    (2, 3, 2, TRUE),
    (3, 2, 3, TRUE),
    (4, 4, 4, TRUE);


INSERT INTO course_indicator (id, course_id, indicator_id, is_active)
VALUES
    (1, 1, 1, TRUE),   -- CE101 -> Outcome 1, PI 1
    (2, 1, 5, TRUE),   -- CE101 -> Outcome 3, PI 1 (written communication)
    (3, 2, 2, TRUE),   -- CE202 -> Outcome 1, PI 2
    (4, 2, 3, TRUE),   -- CE202 -> Outcome 2, PI 1 (design)
    (5, 3, 7, TRUE),   -- CE350 -> Outcome 4, PI 1 (teamwork)
    (6, 3, 8, TRUE),   -- CE350 -> Outcome 4, PI 2
    (7, 4, 4, TRUE),   -- EE210 -> Outcome 2, PI 2
    (8, 4, 6, TRUE),   -- EE210 -> Outcome 3, PI 2 (oral communication)
    (9, 1, 11, TRUE),  -- CE101 -> Outcome 5, PI 1 (ethics)
    (10, 2, 13, TRUE), -- CE202 -> Outcome 6, PI 1 (experiments)
    (11, 3, 12, TRUE), -- CE350 -> Outcome 5, PI 2 (professional conduct)
    (12, 3, 14, TRUE); -- CE350 -> Outcome 6, PI 2 (data analysis)


INSERT INTO measure (id, course_indicator_id, measure_description,
                     observation, recommended_action, fcar, met, exceeded, below, m_status, is_active)
VALUES
    (1, 1, 'Exam 1 performance', 'Strong performance overall', 'Continue approach',
     'Detailed FCAR text', 25, 10, 10, 'InProgress', TRUE),

    (2, 2, 'Communication rubric evaluation', 'Students struggled with clarity', 'Increase practice assignments',
     'FCAR text', 15, 5, 25, 'Submitted', TRUE),

    (3, 3, 'Circuit problem exam section', 'Incorrect simplification common', 'Add supplemental workshop',
     'FCAR details', 20, 8, 12, 'InProgress', TRUE),

    (4, 4, 'Design assignment scoring', 'Good use of tools', 'Expand assignment scope',
     'More FCAR details', 30, 5, 5, 'Complete', TRUE),

    (5, 5, 'Teamwork assessment', 'Teams performed well', 'Maintain group rotations',
     'Team FCAR', 22, 8, 0, 'InReview', TRUE),

    (6, 6, 'Team decision analysis', 'Some groups lacked cohesion', 'Add leadership module',
     'More content', 10, 5, 15, 'InProgress', TRUE),

    (7, 7, 'Logic lab exam', 'Hands-on performance good', 'Increase lab difficulty',
     'Logic FCAR', 28, 12, 10, 'Submitted', TRUE),

    (8, 8, 'Presentation rubric', 'Oral skills improving', 'More group presentations',
     'Communication FCAR', 20, 10, 20, 'Complete', TRUE);