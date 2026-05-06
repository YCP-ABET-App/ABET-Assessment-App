-----------------------------------------
-- DATA FOR ABET ASSESSMENT APP
-- Based on 2023-24 Summary Results
-----------------------------------------

-----------------------------------------
-- USERS (Instructors and Admins)
-----------------------------------------
INSERT INTO users (email, password_hash, name_first, name_last, name_title)
VALUES
    ('david.babcock@university.edu', '$2a$10$k3ohvw57L0aMryaJpFAet.hfSjsWiXalzsVouNQGdsfI48uCjgIEa', 'David', 'Babcock', 'Dr.'),
    ('james.moscola@university.edu', '$2a$10$k3ohvw57L0aMryaJpFAet.hfSjsWiXalzsVouNQGdsfI48uCjgIEa', 'James', 'Moscola', 'Dr.'),
    ('dean.zeller@university.edu', '$2a$10$k3ohvw57L0aMryaJpFAet.hfSjsWiXalzsVouNQGdsfI48uCjgIEa', 'Dean', 'Zeller', 'Dr.'),
    ('admin@university.edu', '$2a$10$NyeKnPTpI8OxJmYnCHanceO/hPFw8lfk/ZQFWBqq7rB.cl9H6vwwq', 'System', 'Administrator', NULL);

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
INSERT INTO course (id, course_code, course_name, course_description, semester_id, student_count, is_active)
VALUES
    (1, 'CS 101', 'Fundamentals of Computer Science I', 'Introduction to programming and problem solving', 2, 44, TRUE),
    (2, 'CS 201', 'Fundamentals of Computer Science II', 'Data structures and algorithms', 2, 28, TRUE),
    (3, 'CS 330', 'Introduction to Networks', 'Computer networking fundamentals', 2, 16, TRUE),
    (4, 'CS 335', 'Cybersecurity', 'Security principles and practices', 2, 13, TRUE),
    (5, 'CS 340', 'Programming Language Design', 'Compiler design and implementation', 2, 15, TRUE),
    (6, 'CS 360', 'Analysis of Algorithms', 'Algorithm design and analysis', 2, 15, TRUE),
    (7, 'CS 400', 'Software Engineering', 'Software development lifecycle and team projects', 2, 27, TRUE),
    (8, 'CS 420', 'Operating Systems', 'Operating system concepts and programming', 2, 25, TRUE);

------------------------------------------------------------
-- COURSE_INSTRUCTOR (Assign instructors to courses)
------------------------------------------------------------
INSERT INTO course_instructor (id, program_user_id, course_id, is_active)
VALUES
    (1, 1, 1, TRUE),  -- Babcock teaches CS 101
    (2, 1, 2, TRUE),  -- Babcock teaches CS 201
    (3, 2, 3, TRUE),  -- Moscola teaches CS 330
    (4, 2, 4, TRUE),  -- Moscola teaches CS 335
    (5, 3, 5, TRUE),  -- Zeller teaches CS 340
    (6, 1, 6, TRUE),  -- Babcock teaches CS 360
    (7, 3, 7, TRUE),  -- Zeller teaches CS 400
    (8, 2, 8, TRUE);  -- Moscola teaches CS 420

------------------------------------------------------------
-- COURSE_INDICATOR (Map courses to performance indicators)
------------------------------------------------------------
-- CS 101 indicators
INSERT INTO course_indicator (id, course_id, indicator_id, is_active)
VALUES
    (1, 1, 1, TRUE),   -- 1.1: Interpret computational problem
    (2, 1, 3, TRUE),   -- 1.3: Define a solution
    (3, 1, 22, TRUE);  -- 6.1: Programming proficiency

-- CS 201 indicators
INSERT INTO course_indicator (id, course_id, indicator_id, is_active)
VALUES
    (4, 2, 1, TRUE),   -- 1.1: Interpret computational problem
    (5, 2, 2, TRUE),   -- 1.2: Analyze and choose principles
    (6, 2, 6, TRUE),   -- 1.6: Asymptotic notation
    (7, 2, 11, TRUE),  -- 2.5: Runtime benchmark data (used as 1.6)
    (8, 2, 22, TRUE),  -- 6.1: Programming proficiency
    (9, 2, 23, TRUE),  -- 6.2: User interfaces
    (10, 2, 25, TRUE); -- 6.4: Automated tests (used as 6.3)

-- CS 330 indicators
INSERT INTO course_indicator (id, course_id, indicator_id, is_active)
VALUES
    (11, 3, 12, TRUE),  -- 2.6: Security concerns
    (12, 3, 13, TRUE);  -- 2.7: Security characteristics

-- CS 335 indicators
INSERT INTO course_indicator (id, course_id, indicator_id, is_active)
VALUES
    (13, 4, 12, TRUE),  -- 2.6: Security concerns
    (14, 4, 13, TRUE),  -- 2.7: Security characteristics
    (15, 4, 18, TRUE),  -- 4.1: Ethical issues
    (16, 4, 26, TRUE);  -- 6.5: Security implementation

-- CS 340 indicators
INSERT INTO course_indicator (id, course_id, indicator_id, is_active)
VALUES
    (17, 5, 22, TRUE);  -- 6.1: Programming proficiency

-- CS 360 indicators
INSERT INTO course_indicator (id, course_id, indicator_id, is_active)
VALUES
    (18, 6, 1, TRUE),   -- 1.1: Interpret computational problem
    (19, 6, 2, TRUE),   -- 1.2: Analyze and choose principles
    (20, 6, 3, TRUE),   -- 1.3: Define a solution
    (21, 6, 6, TRUE),   -- 1.6: Asymptotic notation
    (22, 6, 11, TRUE);  -- 2.5: Runtime benchmark data

-- CS 400 indicators
INSERT INTO course_indicator (id, course_id, indicator_id, is_active)
VALUES
    (23, 7, 4, TRUE),   -- 1.4: System requirements
    (24, 7, 5, TRUE),   -- 1.5: Analyze and model problem domain
    (25, 7, 7, TRUE),   -- 2.1: Evaluate technologies
    (26, 7, 8, TRUE),   -- 2.2: Construct design model
    (27, 7, 9, TRUE),   -- 2.3: Incorporate UI requirements
    (28, 7, 10, TRUE),  -- 2.4: Testing strategy
    (29, 7, 14, TRUE),  -- 3.1: Technical report
    (30, 7, 15, TRUE),  -- 3.2: Technical presentation
    (31, 7, 16, TRUE),  -- 3.3: Communicate to peers
    (32, 7, 20, TRUE),  -- 5.1: Team communication
    (33, 7, 21, TRUE),  -- 5.2: Work with other students
    (34, 7, 25, TRUE);  -- 6.4: Automated tests

-- CS 420 indicators
INSERT INTO course_indicator (id, course_id, indicator_id, is_active)
VALUES
    (35, 8, 22, TRUE),  -- 6.1: Programming proficiency
    (36, 8, 24, TRUE),  -- 6.3: Concurrency
    (37, 8, 26, TRUE);  -- 6.5: Security implementation

------------------------------------------------------------
-- MEASURES (Based on Summary Report data)
------------------------------------------------------------

-- CS 101 Measures
INSERT INTO measure (id, course_indicator_id, measure_description, observation, recommended_action, met, exceeded, below, m_status, is_active)
VALUES
    -- 1.1-cs101: Assignment 3 MS1
    (1, 1, 'Assignment 3 MS1, Dominoes Initialization', NULL, NULL, 35, 3, 6, 'Complete', TRUE),
    -- 1.3-cs101: Assignment 3 MS2
    (2, 2, 'Assignment 3 MS2, Dominoes Simulation', NULL, NULL, 32, 4, 8, 'Complete', TRUE),
    -- 6.1-cs101: Assignment 4
    (3, 3, 'Assignment 4, Roulette (functions)', NULL, NULL, 30, 4, 10, 'Complete', TRUE);

-- CS 201 Measures
INSERT INTO measure (id, course_indicator_id, measure_description, observation, recommended_action, met, exceeded, below, m_status, is_active)
VALUES
    -- 1.1-cs201: Assignment 6
    (4, 4, 'Assignment 6, Final project', NULL, NULL, 15, 3, 10, 'Complete', TRUE),
    -- 1.2-cs201: Exam 2
    (5, 5, 'Exam 2, Question 2, Fibonacci', NULL, NULL, 15, 3, 10, 'Complete', TRUE),
    -- 1.6-cs201: Exam 3
    (6, 6, 'Exam 3, Question 7, Asymptotic Behavior', 'This indicator was used rather than 2.5 as the course material covered asymptotic behavior instead of empirical benchmarking.', NULL, 15, 3, 10, 'Complete', TRUE),
    -- 2.5-cs201: NOT ASSESSED
    -- 6.1-cs201: Final Project
    (7, 8, 'Final Project', NULL, NULL, 18, 4, 6, 'Complete', TRUE),
    -- 6.2-cs201: Final Project UI
    (8, 9, 'Final Project, UI', NULL, NULL, 20, 4, 4, 'Complete', TRUE),
    -- 6.3-cs201: NOT ASSESSED
    -- 6.4-cs201: Lab 6 and Assignment 6
    (9, 10, 'Lab 6 and Assignment 6, Unit tests', 'This indicator was used rather than 6.3 as the course material covered unit testing instead of concurrency.', NULL, 17, 3, 8, 'Complete', TRUE);

-- CS 330 Measures
INSERT INTO measure (id, course_indicator_id, measure_description, observation, recommended_action, met, exceeded, below, m_status, is_active)
VALUES
    -- 2.6-cs330: Lab 2
    (10, 11, 'Lab 2, HTTP', NULL, NULL, 11, 2, 3, 'Complete', TRUE),
    -- 2.6-cs330: Quiz 3
    (11, 11, 'Quiz 3, Question 10, Host authentication', NULL, NULL, 16, 0, 0, 'Complete', TRUE),
    -- 2.7-cs330: Quiz 3
    (12, 12, 'Quiz 3, Question 11, Symmetric Key Cryptography', NULL, NULL, 16, 0, 0, 'Complete', TRUE);

-- CS 335 Measures
INSERT INTO measure (id, course_indicator_id, measure_description, observation, recommended_action, met, exceeded, below, m_status, is_active)
VALUES
    -- 2.6-cs335: Lab 1
    (13, 13, 'Lab 1, Shellshock attack', NULL, NULL, 9, 2, 2, 'Complete', TRUE),
    -- 2.6-cs335: Exam 1
    (14, 13, 'Exam 1, Question 6, Authorization vs. Authentication', NULL, NULL, 13, 0, 0, 'Complete', TRUE),
    -- 2.6-cs335: Quiz 2
    (15, 13, 'Quiz 2, Question 4, XSS identification', NULL, NULL, 7, 2, 4, 'Complete', TRUE),
    -- 2.7-cs335: Quiz 2
    (16, 14, 'Quiz 2, Question 5, XSS mitigation', NULL, NULL, 8, 2, 3, 'Complete', TRUE),
    -- 2.7-cs335: Quiz 2
    (17, 14, 'Quiz 2, Question 8, SQL injection', NULL, NULL, 10, 2, 1, 'Complete', TRUE),
    -- 4.1-cs335: Ethical Hacking Acknowledgement
    (18, 15, 'Ethical Hacking Acknowledgement', NULL, NULL, 13, 0, 0, 'Complete', TRUE),
    -- 4.1-cs335: Exam 1
    (19, 15, 'Exam 1, Question 15, MAC tracking', NULL, NULL, 13, 0, 0, 'Complete', TRUE),
    -- 6.5-cs335: Lab 6
    (20, 16, 'Lab 6, XSS vulnerabilities', NULL, 'Consider timing to avoid major capstone milestones for seniors', 4, 2, 7, 'Complete', TRUE),
    -- 6.5-cs335: Quiz 2
    (21, 16, 'Quiz 2, Question 14, "Man-in-the-Middle" attacks', NULL, NULL, 13, 0, 0, 'Complete', TRUE);

-- CS 340 Measures
INSERT INTO measure (id, course_indicator_id, measure_description, observation, recommended_action, met, exceeded, below, m_status, is_active)
VALUES
    -- 6.1-cs340: Final Project
    (22, 17, 'Final Project, Compiler Design', NULL, NULL, 10, 2, 3, 'Complete', TRUE);

-- CS 360 Measures
INSERT INTO measure (id, course_indicator_id, measure_description, observation, recommended_action, met, exceeded, below, m_status, is_active)
VALUES
    -- 1.1-cs360: Exam 1 Takehome
    (23, 18, 'Exam 1 Takehome, Question 3a, Derive recursive equation', NULL, NULL, 11, 2, 2, 'Complete', TRUE),
    -- 1.1-cs360: Exam 2 Inclass
    (24, 18, 'Exam 2 Inclass, Question 1, Derive recursive equation', NULL, 'Since students were solid on the concept for simpler problems (exam 1), provide some additional practice and slightly reduce the difficulty for the inclass exam problem', 4, 2, 9, 'Complete', TRUE),
    -- 1.2-cs360: Exam 2 Inclass
    (25, 19, 'Exam 2 Inclass, Question 3, Master Theorem', NULL, 'Ensure better consistency of coverage of the material across sections and reassess', 4, 2, 9, 'Complete', TRUE),
    -- 1.2-cs360: Exam 4 Inclass
    (26, 19, 'Exam 4 Inclass, Question 1, Dijkstra''s Algorithm', NULL, NULL, 9, 2, 4, 'Complete', TRUE),
    -- 1.3-cs360: Exam 1 Takehome
    (27, 20, 'Exam 1 Takehome, Question 1b, More efficient algorithm', NULL, NULL, 12, 2, 1, 'Complete', TRUE),
    -- 1.3-cs360: Exam 4 Takehome
    (28, 20, 'Exam 4 Takehome, Question 3, Maximum matching', NULL, 'Provide additional hints as problem was quite difficult', 5, 2, 8, 'Complete', TRUE),
    -- 1.6-cs360: Empirical Comparison Report
    (29, 21, 'Empirical Comparison Report', NULL, 'Provide additional material on plotting multiple data sets on a single graph to compare results visually', 7, 2, 6, 'Complete', TRUE),
    -- 2.5-cs360: Exam 1
    (30, 22, 'Exam 1, Question 3d, Empirical benchmark subarray sum', NULL, NULL, 10, 2, 3, 'Complete', TRUE),
    -- 2.5-cs360: Exam 2
    (31, 22, 'Exam 2, Question 3d, Empirical benchmark median', NULL, 'A bit more instruction on constructing multiple curve graphs. Students also struggled with this on the subsequent empirical comparison report', 8, 2, 5, 'Complete', TRUE);

-- CS 400 Measures
INSERT INTO measure (id, course_indicator_id, measure_description, observation, recommended_action, met, exceeded, below, m_status, is_active)
VALUES
    -- 1.4-cs400: Assignment 3 Requirements
    (32, 23, 'Assignment 3, Requirements', NULL, NULL, 18, 4, 5, 'Complete', TRUE),
    -- 1.5-cs400: Assignment 3 Use Cases
    (33, 24, 'Assignment 3, Use Cases', NULL, 'More weight and emphasis on creation of use cases as part of requirements document', 13, 3, 11, 'Complete', TRUE),
    -- 2.1-cs400: Assignment 1
    (34, 25, 'Assignment 1, Project Proposal', NULL, NULL, 22, 4, 1, 'Complete', TRUE),
    -- 2.2-cs400: Assignment 4 UML
    (35, 26, 'Assignment 4, Analysis and Design - UML', NULL, 'None, as students were able to demonstrate proficiency upon resubmission', 15, 3, 9, 'Complete', TRUE),
    -- 2.3-cs400: Assignment 4 UI
    (36, 27, 'Assignment 4, Analysis and Design - UI', NULL, NULL, 18, 4, 5, 'Complete', TRUE),
    -- 2.4-cs400: Assignment 5
    (37, 28, 'Assignment 5, Minimal Working System - Unit Tests', NULL, 'None, one group started a project from scratch and thus did not have a testing framework at this stage. By the end of the project they had a well developed testing architecture including CI/CD server deployment.', 13, 3, 11, 'Complete', TRUE),
    -- 3.1-cs400: Final Presentation Quality
    (38, 29, 'Final Presentation, Quality', NULL, NULL, 22, 4, 1, 'Complete', TRUE),
    -- 3.2-cs400: Final Presentation Technical
    (39, 30, 'Final Presentation, Technical', NULL, NULL, 21, 4, 2, 'Complete', TRUE),
    -- 3.3-cs400: Final Report Draft
    (40, 31, 'Final Report, Draft', NULL, NULL, 18, 4, 5, 'Complete', TRUE),
    -- 5.1-cs400: Issue Tracker and Peer Evaluations
    (41, 32, 'Issue Tracker, Instructor Observations, Peer Evaluations', NULL, NULL, 19, 4, 4, 'Complete', TRUE),
    -- 5.2-cs400: Peer Evaluations
    (42, 33, 'Peer Evaluations, Instructor Observations', NULL, NULL, 19, 4, 4, 'Complete', TRUE),
    -- 6.4-cs400: Assignment 6
    (43, 34, 'Assignment 6, 50% Working System - Testing Framework', NULL, NULL, 20, 4, 3, 'Complete', TRUE);

-- CS 420 Measures
INSERT INTO measure (id, course_indicator_id, measure_description, observation, recommended_action, met, exceeded, below, m_status, is_active)
VALUES
    -- 6.1-cs420: Programming Assignment #2
    (44, 35, 'Programming Assignment #2, Shared Memory', NULL, NULL, 20, 3, 2, 'Complete', TRUE),
    -- 6.3-cs420: Programming Assignment #3
    (45, 36, 'Programming Assignment #3, Multithreaded Application', NULL, NULL, 19, 3, 3, 'Complete', TRUE),
    -- 6.5-cs420: Programming Assignment #1
    (46, 37, 'Programming Assignment #1, Error Checking', NULL, NULL, 25, 0, 0, 'Complete', TRUE);
