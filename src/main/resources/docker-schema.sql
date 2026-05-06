CREATE DATABASE IF NOT EXISTS local_db;
-- H2 Compatible Schema for ABET Assessment App

DROP TABLE IF EXISTS measure;
DROP TABLE IF EXISTS measure_result;
DROP TABLE IF EXISTS schedule_entry;
DROP TABLE IF EXISTS course_instructor;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS section;
DROP TABLE IF EXISTS section_user;
DROP TABLE IF EXISTS section_indicator;
DROP TABLE IF EXISTS performance_indicator;
DROP TABLE IF EXISTS student_outcome;
DROP TABLE IF EXISTS semester;
DROP TABLE IF EXISTS program_user;
DROP TABLE IF EXISTS program;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS course_indicator;

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
    student_count INT NULL,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- Course-specific
    is_active BOOLEAN DEFAULT TRUE NOT NULL
);

-- Section table
CREATE TABLE section (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_number VARCHAR(50) NOT NULL,
    course_id BIGINT NOT NULL,
    semester_id BIGINT NOT NULL,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- Section-specific
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    FOREIGN KEY (course_id) REFERENCES course(id),
    FOREIGN KEY (semester_id) REFERENCES semester(id)
);

-- SectionUser table
CREATE TABLE section_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    section_id BIGINT NOT NULL,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- CourseInstructor-specific
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (section_id) REFERENCES section(id)
);

-- SectionIndicator table
CREATE TABLE section_indicator (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id BIGINT NOT NULL,
    indicator_id BIGINT NOT NULL,
    complete BOOLEAN DEFAULT FALSE,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- SectionIndicator-specific
    FOREIGN KEY (section_id) REFERENCES section(id),
    FOREIGN KEY (indicator_id) REFERENCES performance_indicator(id)
);

-- SectionProgram table
CREATE TABLE section_program (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id BIGINT NOT NULL,
    program_id BIGINT NOT NULL,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- CourseInstructor-specific
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    FOREIGN KEY (section_id) REFERENCES section(id),
    FOREIGN KEY (program_id) REFERENCES program(id)
);

-- ScheduleEntry table
CREATE TABLE schedule_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    semester_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    program_id BIGINT NOT NULL,
    indicator_id BIGINT NOT NULL,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- CourseIndicator-specific
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    FOREIGN KEY (semester_id) REFERENCES semester(id),
    FOREIGN KEY (course_id) REFERENCES course(id),
    FOREIGN KEY (program_id) REFERENCES program(id),
    FOREIGN KEY (indicator_id) REFERENCES performance_indicator(id)
);

-- Measure table
CREATE TABLE measure (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_entry_id BIGINT NOT NULL,
    measure_description TEXT NOT NULL,
    recommended_action TEXT NULL,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- Measure-specific
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    FOREIGN KEY (schedule_entry_id) REFERENCES schedule_entry(id)
);

-- Measure Results Table
CREATE TABLE measure_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    measure_id BIGINT NOT NULL,
    section_program_id BIGINT NOT NULL,
    met SMALLINT NULL,
    exceeded SMALLINT NULL,
    below SMALLINT NULL,
    observation TEXT NULL,
    rejection_note TEXT NULL,
    m_status VARCHAR(10) DEFAULT 'InProgress' NOT NULL,
    -- From BaseEntity
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP NULL,
    -- Measure Results-specific
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    FOREIGN KEY (measure_id) REFERENCES measure(id),
    FOREIGN KEY (section_program_id) REFERENCES section_program(id)
);

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

-----------------------------------------
-- DATA FOR ABET ASSESSMENT APP
-- Based on 2023-24 Summary Results
-----------------------------------------

-----------------------------------------
-- USERS (Instructors and Admins)
-----------------------------------------
INSERT INTO users (id, email, password_hash, name_first, name_last, name_title)
VALUES
    (1, 'david.babcock@university.edu', '$2a$10$k3ohvw57L0aMryaJpFAet.hfSjsWiXalzsVouNQGdsfI48uCjgIEa', 'David', 'Babcock', 'Dr.'),
    (2, 'james.moscola@university.edu', '$2a$10$k3ohvw57L0aMryaJpFAet.hfSjsWiXalzsVouNQGdsfI48uCjgIEa', 'James', 'Moscola', 'Dr.'),
    (3, 'dean.zeller@university.edu', '$2a$10$k3ohvw57L0aMryaJpFAet.hfSjsWiXalzsVouNQGdsfI48uCjgIEa', 'Dean', 'Zeller', 'Dr.'),
    (4, 'admin@university.edu', '$2a$10$NyeKnPTpI8OxJmYnCHanceO/hPFw8lfk/ZQFWBqq7rB.cl9H6vwwq', 'System', 'Administrator', NULL);

------------------------------------------------------------
-- PROGRAMS
------------------------------------------------------------
INSERT INTO program (id, program_name, institution, is_active)
VALUES
    (1, 'Computer Science', 'York College of Pennsylvania', TRUE);

------------------------------------------------------------
-- PROGRAM_USER (maps users to programs)
------------------------------------------------------------
INSERT INTO program_user (id, isAdmin, program_id, user_id, is_active)
VALUES
    (1, TRUE, 1, 1, TRUE),   -- David Babcock (Admin)
    (2, FALSE, 1, 2, TRUE),   -- James Moscola (Admin)
    (3, FALSE, 1, 3, TRUE),   -- Dean Zeller (Admin)
    (4, TRUE, 1, 4, TRUE);   -- System Admin

------------------------------------------------------------
-- SEMESTERS
------------------------------------------------------------
INSERT INTO semester (id, name, code, type, status, start_date, end_date, academic_year, description, program_id, is_current)
VALUES
    (1, 'Fall 2023', 'FALL-2023', 'FALL', 'COMPLETED', '2023-08-28', '2023-12-15', 2023, 'Fall 2023 semester', 1, FALSE),
    (2, 'Spring 2024', 'SPRING-2024', 'SPRING', 'COMPLETED', '2024-01-15', '2024-05-17', 2023, 'Spring 2024 semester', 1, FALSE),
    (3, 'Fall 2024', 'FALL-2024', 'FALL', 'COMPLETED', '2024-08-26', '2024-12-15', 2024, 'Fall 2024 semester', 1, FALSE),
    (4, 'Spring 2025', 'SPRING-2025', 'SPRING', 'ACTIVE', '2025-01-15', '2025-05-16', 2024, 'Current spring semester', 1, TRUE);

------------------------------------------------------------
-- STUDENT OUTCOMES (Based on CSV - 7 total outcomes)
------------------------------------------------------------
INSERT INTO student_outcome (id, out_number, out_value, out_description, semester_id, is_active)
VALUES
    (1, 1, NULL, 'Analyze a complex computing problem and to apply principles of computing and other relevant disciplines to identify solutions.', 2, TRUE),
    (2, 2, NULL, 'Design, implement, and evaluate a computing-based solution to meet a given set of computing requirements in the context of the program''s discipline.', 2, TRUE),
    (3, 3, NULL, 'Communicate effectively in a variety of professional contexts.', 2, TRUE),
    (4, 4, NULL, 'Recognize professional responsibilities and make informed judgments in computing practice based on legal and ethical principles.', 2, TRUE),
    (5, 5, NULL, 'Function effectively as a member or leader of a team engaged in activities appropriate to the program''s discipline.', 2, TRUE),
    (6, 6, NULL, 'Apply computer science theory and software development fundamentals to produce computing-based solutions.', 2, TRUE);

------------------------------------------------------------
-- PERFORMANCE INDICATORS (Based on CSV)
------------------------------------------------------------
-- Outcome 1 Indicators (6 total)
INSERT INTO performance_indicator (id, ind_number, ind_value, ind_description, student_outcome_id, threshold_percentage, is_active)
VALUES
    (1, 1, NULL, 'Student can correctly interpret a computational problem and define its parameters', 1, 70.00, TRUE),
    (2, 2, NULL, 'Student can analyze a computational problem in order to choose mathematical and algorithmic principles that can be applied to solve the problem', 1, 70.00, TRUE),
    (3, 3, NULL, 'Student can define a solution to a computational problem', 1, 70.00, TRUE),
    (4, 4, NULL, 'Student can effectively collect and document system requirements', 1, 70.00, TRUE),
    (5, 5, NULL, 'Student can effectively analyze and model a problem domain', 1, 70.00, TRUE),
    (6, 6, NULL, 'Student can identify the relative efficiency of different algorithms using asymptotic notation', 1, 70.00, TRUE);

-- Outcome 2 Indicators (7 total)
INSERT INTO performance_indicator (id, ind_number, ind_value, ind_description, student_outcome_id, threshold_percentage, is_active)
VALUES
    (7, 1, NULL, 'Student can identify and evaluate appropriate technologies to be used in a system', 2, 70.00, TRUE),
    (8, 2, NULL, 'Student can effectively construct a design model of a system', 2, 70.00, TRUE),
    (9, 3, NULL, 'Student can effectively incorporate requirements outside the problem domain (e.g., a user interface) into the design model', 2, 70.00, TRUE),
    (10, 4, NULL, 'Student can plan and implement a testing strategy to ensure that system meets its quality goal', 2, 70.00, TRUE),
    (11, 5, NULL, 'Student can collect and analyze runtime benchmark data to characterize the efficiency of an algorithm or data structure', 2, 70.00, TRUE),
    (12, 6, NULL, 'Student can specify appropriate security concerns and requirements for a component or system', 2, 70.00, TRUE),
    (13, 7, NULL, 'Student can evaluate a component or system to identify security characteristics and identify vulnerabilities', 2, 70.00, TRUE);

-- Outcome 3 Indicators (4 total)
INSERT INTO performance_indicator (id, ind_number, ind_value, ind_description, student_outcome_id, threshold_percentage, is_active)
VALUES
    (14, 1, NULL, 'Student can write a clear and well-organized technical report', 3, 70.00, TRUE),
    (15, 2, NULL, 'Student can create and present a clear and well-organized technical presentation using appropriate visual, textual, and spoken content', 3, 70.00, TRUE),
    (16, 3, NULL, 'Student can communicate technical content to peers', 3, 70.00, TRUE),
    (17, 4, NULL, 'Student can communicate technical content to general audiences', 3, 70.00, TRUE);

-- Outcome 4 Indicators (2 total)
INSERT INTO performance_indicator (id, ind_number, ind_value, ind_description, student_outcome_id, threshold_percentage, is_active)
VALUES
    (18, 1, NULL, 'Student can analyze and explain the ethical issues surrounding a particular computing topic (for example, peer-to-peer file sharing)', 4, 70.00, TRUE),
    (19, 2, NULL, 'Student demonstrates recognition of his or her professional responsibilities as a member of the computing profession', 4, 70.00, TRUE);

-- Outcome 5 Indicators (2 total)
INSERT INTO performance_indicator (id, ind_number, ind_value, ind_description, student_outcome_id, threshold_percentage, is_active)
VALUES
    (20, 1, NULL, 'Student demonstrates an ability to participate in and implement processes for team communication and coordination', 5, 70.00, TRUE),
    (21, 2, NULL, 'Student demonstrates an ability to work closely with other students to solve technical problems', 5, 70.00, TRUE);

-- Outcome 6 Indicators (5 total)
INSERT INTO performance_indicator (id, ind_number, ind_value, ind_description, student_outcome_id, threshold_percentage, is_active)
VALUES
    (22, 1, NULL, 'Student is proficient in a current programming language', 6, 70.00, TRUE),
    (23, 2, NULL, 'Student can create user interfaces using current platforms', 6, 70.00, TRUE),
    (24, 3, NULL, 'Student can write programs that use concurrency', 6, 70.00, TRUE),
    (25, 4, NULL, 'Student can implement automated tests to satisfy the goals of a testing strategy', 6, 70.00, TRUE),
    (26, 5, NULL, 'Student can use appropriate implementation techniques and practices to meet security requirements and/or mitigate discovered vulnerabilities', 6, 70.00, TRUE);

------------------------------------------------------------
-- COURSES (Based on Summary Report)
------------------------------------------------------------
INSERT INTO course (id, course_code, course_name, course_description, student_count, is_active)
VALUES
    (1, 'CS 101', 'Fundamentals of Computer Science I', 'Introduction to programming and problem solving', 44, TRUE),
    (2, 'CS 201', 'Fundamentals of Computer Science II', 'Data structures and algorithms', 28, TRUE),
    (3, 'CS 330', 'Introduction to Networks', 'Computer networking fundamentals', 16, TRUE),
    (4, 'CS 335', 'Cybersecurity', 'Security principles and practices', 13, TRUE),
    (5, 'CS 340', 'Programming Language Design', 'Compiler design and implementation', 15, TRUE),
    (6, 'CS 360', 'Analysis of Algorithms', 'Algorithm design and analysis', 15, TRUE),
    (7, 'CS 400', 'Software Engineering', 'Software development lifecycle and team projects', 27, TRUE),
    (8, 'CS 420', 'Operating Systems', 'Operating system concepts and programming', 25, TRUE);

------------------------------------------------------------
-- SECTIONS (Based on Summary Report)
------------------------------------------------------------
INSERT INTO section (id, section_number, course_id, semester_id, is_active)
VALUES
    (1, '101', 1, 2, TRUE), -- CS 101 section 1
    (2, '201', 1, 2, TRUE), -- CS 101 section 2
    (3, '101', 2, 2, TRUE), -- CS 201 section 1
    (4, '101', 3, 2, TRUE), -- CS 330 section 1
    (5, '101', 4, 2, TRUE), -- CS 335 section 1
    (6, '101', 5, 2, TRUE), -- CS 340 section 1
    (7, '101', 6, 2, TRUE), -- CS 360 section 1
    (8, '101', 7, 2, TRUE), -- CS 400 section 1
    (9, '101', 8, 2, TRUE); -- CS 420 section 1

------------------------------------------------------------
-- SECTION_USER (Assign instructors to sections)
------------------------------------------------------------
INSERT INTO section_user (id, user_id, section_id, is_active)
VALUES
    (1, 1, 1, TRUE),  -- Babcock teaches CS 101 section 1
    (2, 3, 2, TRUE),  -- Zeller teaches CS 101 section 2
    (3, 1, 3, TRUE),  -- Babcock teaches CS 201
    (4, 2, 4, TRUE),  -- Moscola teaches CS 330
    (5, 2, 5, TRUE),  -- Moscola teaches CS 335
    (6, 3, 6, TRUE),  -- Zeller teaches CS 340
    (7, 1, 7, TRUE),  -- Babcock teaches CS 360
    (8, 3, 8, TRUE),  -- Zeller teaches CS 400
    (9, 2, 9, TRUE);  -- Moscola teaches CS 420

------------------------------------------------------------
-- SECTION_PROGRAM (Assign programs to sections)
------------------------------------------------------------
INSERT INTO section_program (id, section_id, program_id, is_active)
VALUES
    (1, 1, 1, TRUE),
    (2, 2, 1, TRUE),
    (3, 3, 1, TRUE),
    (4, 4, 1, TRUE),
    (5, 5, 1, TRUE),
    (6, 6, 1, TRUE),
    (7, 7, 1, TRUE),
    (8, 8, 1, TRUE),
    (9, 9, 1, TRUE);

------------------------------------------------------------
-- Schedule Entry (Map courses to performance indicators)
------------------------------------------------------------
-- CS 101 indicators
INSERT INTO schedule_entry (id, semester_id, course_id, program_id, indicator_id, is_active)
VALUES
    (1, 2, 1, 1, 1, TRUE),   -- 1.1: Interpret computational problem
    (2, 2, 1, 1, 3, TRUE),   -- 1.3: Define a solution
    (3, 2, 1, 1, 22, TRUE);  -- 6.1: Programming proficiency

-- CS 201 indicators
INSERT INTO schedule_entry (id, semester_id, course_id, program_id, indicator_id, is_active)
VALUES
    (4, 2, 2, 1, 1, TRUE),   -- 1.1: Interpret computational problem
    (5, 2, 2, 1, 2, TRUE),   -- 1.2: Analyze and choose principles
    (6, 2, 2, 1, 6, TRUE),   -- 1.6: Asymptotic notation
    (7, 2, 2, 1, 11, TRUE),  -- 2.5: Runtime benchmark data (used as 1.6)
    (8, 2, 2, 1, 22, TRUE),  -- 6.1: Programming proficiency
    (9, 2, 2, 1, 23, TRUE),  -- 6.2: User interfaces
    (10, 2, 2, 1, 25, TRUE); -- 6.4: Automated tests (used as 6.3)

-- CS 330 indicators
INSERT INTO schedule_entry (id, semester_id, course_id, program_id, indicator_id, is_active)
VALUES
    (11, 2, 3, 1, 12, TRUE),  -- 2.6: Security concerns
    (12, 2, 3, 1, 13, TRUE);  -- 2.7: Security characteristics

-- CS 335 indicators
INSERT INTO schedule_entry (id, semester_id, course_id, program_id, indicator_id, is_active)
VALUES
    (13, 2, 4, 1, 12, TRUE),  -- 2.6: Security concerns
    (14, 2, 4, 1, 13, TRUE),  -- 2.7: Security characteristics
    (15, 2, 4, 1, 18, TRUE),  -- 4.1: Ethical issues
    (16, 2, 4, 1, 26, TRUE);  -- 6.5: Security implementation

-- CS 340 indicators
INSERT INTO schedule_entry (id, semester_id, course_id, program_id, indicator_id, is_active)
VALUES
    (17, 2, 5, 1, 22, TRUE);  -- 6.1: Programming proficiency

-- CS 360 indicators
INSERT INTO schedule_entry (id, semester_id, course_id, program_id, indicator_id, is_active)
VALUES
    (18, 2, 6, 1, 1, TRUE),   -- 1.1: Interpret computational problem
    (19, 2, 6, 1, 2, TRUE),   -- 1.2: Analyze and choose principles
    (20, 2, 6, 1, 3, TRUE),   -- 1.3: Define a solution
    (21, 2, 6, 1, 6, TRUE),   -- 1.6: Asymptotic notation
    (22, 2, 6, 1, 11, TRUE);  -- 2.5: Runtime benchmark data

-- CS 400 indicators
INSERT INTO schedule_entry (id, semester_id, course_id, program_id, indicator_id, is_active)
VALUES
    (23, 2, 7, 1, 4, TRUE),   -- 1.4: System requirements
    (24, 2, 7, 1, 5, TRUE),   -- 1.5: Analyze and model problem domain
    (25, 2, 7, 1, 7, TRUE),   -- 2.1: Evaluate technologies
    (26, 2, 7, 1, 8, TRUE),   -- 2.2: Construct design model
    (27, 2, 7, 1, 9, TRUE),   -- 2.3: Incorporate UI requirements
    (28, 2, 7, 1, 10, TRUE),  -- 2.4: Testing strategy
    (29, 2, 7, 1, 14, TRUE),  -- 3.1: Technical report
    (30, 2, 7, 1, 15, TRUE),  -- 3.2: Technical presentation
    (31, 2, 7, 1, 16, TRUE),  -- 3.3: Communicate to peers
    (32, 2, 7, 1, 20, TRUE),  -- 5.1: Team communication
    (33, 2, 7, 1, 21, TRUE),  -- 5.2: Work with other students
    (34, 2, 7, 1, 25, TRUE);  -- 6.4: Automated tests

-- CS 420 indicators
INSERT INTO schedule_entry (id, semester_id, course_id, program_id, indicator_id, is_active)
VALUES
    (35, 2, 8, 1, 22, TRUE),  -- 6.1: Programming proficiency
    (36, 2, 8, 1, 24, TRUE),  -- 6.3: Concurrency
    (37, 2, 8, 1, 26, TRUE);  -- 6.5: Security implementation

------------------------------------------------------------
-- MEASURES (Based on Summary Report data)
------------------------------------------------------------

-- CS 101 Measures
INSERT INTO measure (id, schedule_entry_id, measure_description, recommended_action, is_active)
VALUES
    -- 1.1-cs101: Assignment 3 MS1
    (1, 1, 'Assignment 3 MS1, Dominoes Initialization', NULL, TRUE),
    -- 1.3-cs101: Assignment 3 MS2
    (2, 2, 'Assignment 3 MS2, Dominoes Simulation', NULL, TRUE),
    -- 6.1-cs101: Assignment 4
    (3, 3, 'Assignment 4, Roulette (functions)', NULL, TRUE);

-- Section 1 Measure Results
INSERT INTO measure_result (id, measure_id, section_program_id, met, exceeded, below, observation, rejection_note, m_status, is_active)
VALUES
    (1, 1, 1, 35, 3, 6, NULL, NULL, 'Complete', TRUE),
    (2, 2, 1, 32, 4, 5, NULL, NULL, 'Complete', TRUE),
    (3, 3, 1, 30, 4, 10, NULL, NULL, 'Complete', TRUE);

--Section 2 Measure Results
INSERT INTO measure_result (id, measure_id, section_program_id, met, exceeded, below, observation, rejection_note, m_status, is_active)
VALUES
    (4, 1, 2, 32, 5, 7, NULL, NULL, 'Complete', TRUE),
    (5, 2, 2, 36, 2, 3, NULL, NULL, 'Complete', TRUE),
    (6, 3, 2, 31, 2, 11, NULL, NULL, 'Complete', TRUE);

-- CS 201 Measures
INSERT INTO measure (id, schedule_entry_id, measure_description, recommended_action, is_active)
VALUES
    -- 1.1-cs201: Assignment 6
    (4, 4, 'Assignment 6, Final project', NULL, TRUE),
    -- 1.2-cs201: Exam 2
    (5, 5, 'Exam 2, Question 2, Fibonacci', NULL, TRUE),
    -- 1.6-cs201: Exam 3
    (6, 6, 'Exam 3, Question 7, Asymptotic Behavior', NULL, TRUE),
    -- 2.5-cs201: NOT ASSESSED
    -- 6.1-cs201: Final Project
    (7, 8, 'Final Project', NULL, TRUE),
    -- 6.2-cs201: Final Project UI
    (8, 9, 'Final Project, UI', NULL, TRUE),
    -- 6.3-cs201: NOT ASSESSED
    -- 6.4-cs201: Lab 6 and Assignment 6
    (9, 10, 'Lab 6 and Assignment 6, Unit tests', NULL, TRUE);

INSERT INTO measure_result (id, measure_id, section_program_id, met, exceeded, below, observation, rejection_note, m_status, is_active)
VALUES
    (7, 4, 3, 15, 3, 10, NULL, NULL, 'Complete', TRUE),
    (8, 5, 3, 15, 3, 10, NULL, NULL, 'Complete', TRUE),
    (9, 6, 3, 15, 3, 10, 'This indicator was used rather than 2.5 as the course material covered asymptotic behavior instead of empirical benchmarking.', NULL, 'Complete', TRUE),
    (10, 7, 3, 18, 4, 6, NULL, NULL, 'Complete', TRUE),
    (11, 8, 3, 20, 4, 4, NULL, NULL, 'Complete', TRUE),
    (12, 9, 3, 17, 3, 8, 'This indicator was used rather than 6.3 as the course material covered unit testing instead of concurrency.', NULL, 'Complete', TRUE);

-- CS 330 Measures
INSERT INTO measure (id, schedule_entry_id, measure_description, recommended_action, is_active)
VALUES
    -- 2.6-cs330: Lab 2
    (10, 11, 'Lab 2, HTTP', NULL, TRUE),
    -- 2.6-cs330: Quiz 3
    (11, 11, 'Quiz 3, Question 10, Host authentication', NULL, TRUE),
    -- 2.7-cs330: Quiz 3
    (12, 12, 'Quiz 3, Question 11, Symmetric Key Cryptography', NULL, TRUE);

INSERT INTO measure_result (id, measure_id, section_program_id, met, exceeded, below, observation, rejection_note, m_status, is_active)
VALUES
    (13, 10, 4, 11, 2, 3, NULL, NULL, 'Complete', TRUE),
    (14, 11, 4, 16, 0, 0, NULL, NULL, 'Complete', TRUE),
    (15, 12, 4, 16, 0, 0, NULL, NULL, 'Complete', TRUE);

-- CS 335 Measures
INSERT INTO measure (id, schedule_entry_id, measure_description, recommended_action, is_active)
VALUES
    -- 2.6-cs335: Lab 1
    (13, 13, 'Lab 1, Shellshock attack', NULL, TRUE),
    -- 2.6-cs335: Exam 1
    (14, 13, 'Exam 1, Question 6, Authorization vs. Authentication', NULL, TRUE),
    -- 2.6-cs335: Quiz 2
    (15, 13, 'Quiz 2, Question 4, XSS identification', NULL, TRUE),
    -- 2.7-cs335: Quiz 2
    (16, 14, 'Quiz 2, Question 5, XSS mitigation', NULL, TRUE),
    -- 2.7-cs335: Quiz 2
    (17, 14, 'Quiz 2, Question 8, SQL injection', NULL, TRUE),
    -- 4.1-cs335: Ethical Hacking Acknowledgement
    (18, 15, 'Ethical Hacking Acknowledgement', NULL, TRUE),
    -- 4.1-cs335: Exam 1
    (19, 15, 'Exam 1, Question 15, MAC tracking', NULL, TRUE),
    -- 6.5-cs335: Lab 6
    (20, 16, 'Lab 6, XSS vulnerabilities', 'Consider timing to avoid major capstone milestones for seniors', TRUE),
    -- 6.5-cs335: Quiz 2
    (21, 16, 'Quiz 2, Question 14, "Man-in-the-Middle" attacks', NULL, TRUE);

INSERT INTO measure_result (id, measure_id, section_program_id, met, exceeded, below, observation, rejection_note, m_status, is_active)
VALUES
    (16, 13, 5, 9, 2, 2, NULL, NULL, 'Complete', TRUE),
    (17, 14, 5, 13, 0, 0, NULL, NULL, 'Complete', TRUE),
    (18, 15, 5, 7, 2, 4, NULL, NULL, 'Complete', TRUE),
    (19, 16, 5, 8, 2, 3, NULL, NULL, 'Complete', TRUE),
    (20, 17, 5, 10, 2, 1, NULL, NULL, 'Complete', TRUE),
    (21, 18, 5, 13, 0, 0, NULL, NULL, 'Complete', TRUE),
    (22, 19, 5, 13, 0, 0, NULL, NULL, 'Complete', TRUE),
    (23, 20, 5, 13, 0, 0, NULL, NULL, 'Complete', TRUE),
    (24, 21, 5, 13, 0, 0, NULL, NULL, 'Complete', TRUE);

-- CS 340 Measures
INSERT INTO measure (id, schedule_entry_id, measure_description, recommended_action, is_active)
VALUES
    -- 6.1-cs340: Final Project
    (22, 17, 'Final Project, Compiler Design', NULL, TRUE);

INSERT INTO measure_result (id, measure_id, section_program_id, met, exceeded, below, observation, rejection_note, m_status, is_active)
VALUES
    (25, 22, 6, 10, 2, 3, NULL, NULL, 'Complete', TRUE);

-- CS 360 Measures
INSERT INTO measure (id, schedule_entry_id, measure_description, recommended_action, is_active)
VALUES
    -- 1.1-cs360: Exam 1 Takehome
    (23, 18, 'Exam 1 Takehome, Question 3a, Derive recursive equation', NULL, TRUE),
    -- 1.1-cs360: Exam 2 Inclass
    (24, 18, 'Exam 2 Inclass, Question 1, Derive recursive equation', 'Since students were solid on the concept for simpler problems (exam 1), provide some additional practice and slightly reduce the difficulty for the inclass exam problem', TRUE),
    -- 1.2-cs360: Exam 2 Inclass
    (25, 19, 'Exam 2 Inclass, Question 3, Master Theorem', 'Ensure better consistency of coverage of the material across sections and reassess', TRUE),
    -- 1.2-cs360: Exam 4 Inclass
    (26, 19, 'Exam 4 Inclass, Question 1, Dijkstra''s Algorithm', NULL, TRUE),
    -- 1.3-cs360: Exam 1 Takehome
    (27, 20, 'Exam 1 Takehome, Question 1b, More efficient algorithm', NULL, TRUE),
    -- 1.3-cs360: Exam 4 Takehome
    (28, 20, 'Exam 4 Takehome, Question 3, Maximum matching', 'Provide additional hints as problem was quite difficult', TRUE),
    -- 1.6-cs360: Empirical Comparison Report
    (29, 21, 'Empirical Comparison Report', 'Provide additional material on plotting multiple data sets on a single graph to compare results visually', TRUE),
    -- 2.5-cs360: Exam 1
    (30, 22, 'Exam 1, Question 3d, Empirical benchmark subarray sum', NULL, TRUE),
    -- 2.5-cs360: Exam 2
    (31, 22, 'Exam 2, Question 3d, Empirical benchmark median', 'A bit more instruction on constructing multiple curve graphs. Students also struggled with this on the subsequent empirical comparison report', TRUE);

INSERT INTO measure_result (id, measure_id, section_program_id, met, exceeded, below, observation, rejection_note, m_status, is_active)
VALUES
    (26, 23, 7, 11, 2, 2, NULL, NULL, 'Complete', TRUE),
    (27, 24, 7, 4, 2, 9, NULL, NULL, 'Complete', TRUE),
    (28, 25, 7, 4, 2, 9, NULL, NULL, 'Complete', TRUE),
    (29, 26, 7, 9, 2, 4, NULL, NULL, 'Complete', TRUE),
    (30, 27, 7, 12, 2, 1, NULL, NULL, 'Complete', TRUE),
    (31, 28, 7, 5, 2, 8, NULL, NULL, 'Complete', TRUE),
    (32, 29, 7, 7, 2, 6, NULL, NULL, 'Complete', TRUE),
    (33, 30, 7, 10, 2, 3, NULL, NULL, 'Complete', TRUE),
    (34, 31, 7, 8, 2, 5, NULL, NULL, 'Complete', TRUE);

-- CS 400 Measures
INSERT INTO measure (id, schedule_entry_id, measure_description, recommended_action, is_active)
VALUES
    -- 1.4-cs400: Assignment 3 Requirements
    (32, 23, 'Assignment 3, Requirements', NULL, TRUE),
    -- 1.5-cs400: Assignment 3 Use Cases
    (33, 24, 'Assignment 3, Use Cases', 'More weight and emphasis on creation of use cases as part of requirements document', TRUE),
    -- 2.1-cs400: Assignment 1
    (34, 25, 'Assignment 1, Project Proposal', NULL, TRUE),
    -- 2.2-cs400: Assignment 4 UML
    (35, 26, 'Assignment 4, Analysis and Design - UML', 'None, as students were able to demonstrate proficiency upon resubmission', TRUE),
    -- 2.3-cs400: Assignment 4 UI
    (36, 27, 'Assignment 4, Analysis and Design - UI', NULL, TRUE),
    -- 2.4-cs400: Assignment 5
    (37, 28, 'Assignment 5, Minimal Working System - Unit Tests', 'None, one group started a project from scratch and thus did not have a testing framework at this stage. By the end of the project they had a well developed testing architecture including CI/CD server deployment.', TRUE),
    -- 3.1-cs400: Final Presentation Quality
    (38, 29, 'Final Presentation, Quality', NULL, TRUE),
    -- 3.2-cs400: Final Presentation Technical
    (39, 30, 'Final Presentation, Technical', NULL, TRUE),
    -- 3.3-cs400: Final Report Draft
    (40, 31, 'Final Report, Draft', NULL, TRUE),
    -- 5.1-cs400: Issue Tracker and Peer Evaluations
    (41, 32, 'Issue Tracker, Instructor Observations, Peer Evaluations', NULL, TRUE),
    -- 5.2-cs400: Peer Evaluations
    (42, 33, 'Peer Evaluations, Instructor Observations', NULL, TRUE),
    -- 6.4-cs400: Assignment 6
    (43, 34, 'Assignment 6, 50% Working System - Testing Framework', NULL, TRUE);

INSERT INTO measure_result (id, measure_id, section_program_id, met, exceeded, below, observation, rejection_note, m_status, is_active)
VALUES
    (35, 32, 8, 18, 4, 5, NULL, NULL, 'Complete', TRUE),
    (36, 33, 8, 13, 3, 11, NULL, NULL, 'Complete', TRUE),
    (37, 34, 8, 13, 3, 11, NULL, NULL, 'Complete', TRUE),
    (38, 35, 8, 22, 4, 1, NULL, NULL, 'Complete', TRUE),
    (39, 36, 8, 18, 4, 5, NULL, NULL, 'Complete', TRUE),
    (40, 37, 8, 13, 3, 11, NULL, NULL, 'Complete', TRUE),
    (41, 38, 8, 22, 4, 1, NULL, NULL, 'Complete', TRUE),
    (42, 39, 8, 21, 4, 2, NULL, NULL, 'Complete', TRUE),
    (43, 40, 8, 18, 4, 5, NULL, NULL, 'Complete', TRUE),
    (44, 41, 8, 19, 4, 4, NULL, NULL, 'Complete', TRUE),
    (45, 42, 8, 19, 4, 4, NULL, NULL, 'Complete', TRUE),
    (46, 43, 8, 20, 4, 3, NULL, NULL, 'Complete', TRUE);

-- CS 420 Measures
INSERT INTO measure (id, schedule_entry_id, measure_description, recommended_action, is_active)
VALUES
    -- 6.1-cs420: Programming Assignment #2
    (44, 35, 'Programming Assignment #2, Shared Memory', NULL, TRUE),
    -- 6.3-cs420: Programming Assignment #3
    (45, 36, 'Programming Assignment #3, Multithreaded Application', NULL, TRUE),
    -- 6.5-cs420: Programming Assignment #1
    (46, 37, 'Programming Assignment #1, Error Checking', NULL, TRUE);

INSERT INTO measure_result (id, measure_id, section_program_id, met, exceeded, below, observation, rejection_note, m_status, is_active)
VALUES
    (47, 44, 9, 20, 3, 2, NULL, NULL, 'Complete', TRUE),
    (48, 45, 9, 19, 3, 3, NULL, NULL, 'Complete', TRUE),
    (49, 46, 9, 25, 0, 0, NULL, NULL, 'Complete', TRUE);

INSERT INTO section_indicator (id, section_id, indicator_id, complete, created_at, updated_at, version, deleted, deleted_at)
VALUES
    (1, 1, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),      -- CS 101 Sec 1 - Indicator 1.1
    (2, 1, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),      -- CS 101 Sec 1 - Indicator 1.3
    (3, 1, 22, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 101 Sec 1 - Indicator 6.1
    (4, 2, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),      -- CS 101 Sec 2 - Indicator 1.1
    (5, 2, 3, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),     -- CS 101 Sec 2 - Indicator 1.3
    (6, 2, 22, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),     -- CS 101 Sec 2 - Indicator 6.1
    (7, 3, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),      -- CS 201 Sec 1 - Indicator 1.1
    (8, 3, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),      -- CS 201 Sec 1 - Indicator 1.2
    (9, 3, 6, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),     -- CS 201 Sec 1 - Indicator 1.6
    (10, 3, 11, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 201 Sec 1 - Indicator 2.5
    (11, 3, 22, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 201 Sec 1 - Indicator 6.1
    (12, 3, 23, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 201 Sec 1 - Indicator 6.2
    (13, 3, 25, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),   -- CS 201 Sec 1 - Indicator 6.4
    (14, 4, 12, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 330 Sec 1 - Indicator 2.6
    (15, 4, 13, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 330 Sec 1 - Indicator 2.7
    (16, 5, 12, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 335 Sec 1 - Indicator 2.6
    (17, 5, 13, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 335 Sec 1 - Indicator 2.7
    (18, 5, 18, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),   -- CS 335 Sec 1 - Indicator 4.1
    (19, 5, 26, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 335 Sec 1 - Indicator 6.5
    (20, 6, 22, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 340 Sec 1 - Indicator 6.1
    (21, 7, 1, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 360 Sec 1 - Indicator 1.1
    (22, 7, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),     -- CS 360 Sec 1 - Indicator 1.2
    (23, 7, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),     -- CS 360 Sec 1 - Indicator 1.3
    (24, 7, 6, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 360 Sec 1 - Indicator 1.6
    (25, 7, 11, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 360 Sec 1 - Indicator 2.5
    (26, 8, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),     -- CS 400 Sec 1 - Indicator 1.4
    (27, 8, 5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),     -- CS 400 Sec 1 - Indicator 1.5
    (28, 8, 7, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 400 Sec 1 - Indicator 2.1
    (29, 8, 8, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),     -- CS 400 Sec 1 - Indicator 2.2
    (30, 8, 9, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),     -- CS 400 Sec 1 - Indicator 2.3
    (31, 8, 10, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),   -- CS 400 Sec 1 - Indicator 2.4
    (32, 8, 14, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 400 Sec 1 - Indicator 3.1
    (33, 8, 15, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 400 Sec 1 - Indicator 3.2
    (34, 8, 16, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 400 Sec 1 - Indicator 3.3
    (35, 8, 20, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),   -- CS 400 Sec 1 - Indicator 5.1
    (36, 8, 21, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 400 Sec 1 - Indicator 5.2
    (37, 8, 25, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 400 Sec 1 - Indicator 6.4
    (38, 9, 22, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),    -- CS 420 Sec 1 - Indicator 6.1
    (39, 9, 24, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL),   -- CS 420 Sec 1 - Indicator 6.3
    (40, 9, 26, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE, NULL);    -- CS 420 Sec 1 - Indicator 6.5
