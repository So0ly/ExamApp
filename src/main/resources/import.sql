INSERT INTO "examiners" ("id", "mail", "password", "firstname", "lastname", "titles")
VALUES (0, 'admin@pbs.edu.pl', '$2a$10$7b.9iLgXFVh.r1u9HEbMv.EDL3JcJgldsWHUg4etSUh4wCNGuExye', 'Admin', 'Adminowski', 'adm')
    ON CONFLICT DO NOTHING;
INSERT INTO "user_roles" ("id", "roles") VALUES (0, 'admin')
    ON CONFLICT DO NOTHING;
INSERT INTO "user_roles" ("id", "roles") VALUES (0, 'user')
    ON CONFLICT DO NOTHING;
INSERT INTO "examiners" ("id", "mail", "password", "firstname", "lastname", "titles")
VALUES (1, 'user@pbs.edu.pl', '$2a$10$7b.9iLgXFVh.r1u9HEbMv.EDL3JcJgldsWHUg4etSUh4wCNGuExye', 'Mikołaj', 'Barański', 'debil')
    ON CONFLICT DO NOTHING;
INSERT INTO "user_roles" ("id", "roles") VALUES (1, 'user')
    ON CONFLICT DO NOTHING;
INSERT INTO "students" ("id", "firstname", "lastname", "index")
VALUES (0, 'Mikołaj', 'Barański', '111222')
    ON CONFLICT DO NOTHING;
INSERT INTO "students" ("id", "firstname", "lastname", "index")
VALUES (1, 'Jan', 'Kowalski', '222222')
    ON CONFLICT DO NOTHING;
INSERT INTO "reports" ("id", "student_id", "examiner_id", "examdate", "finalgrade")
VALUES (0, 0, 1, NOW(), 4)
    ON CONFLICT DO NOTHING;
INSERT INTO "reports" ("id", "student_id", "examiner_id", "examdate", "finalgrade")
VALUES (1, 1, 1, NOW(), 4)
    ON CONFLICT DO NOTHING;
INSERT INTO "questions" ("reportid", "question", "questionscore")
VALUES (0, 'Pytanie', 3.5)
    ON CONFLICT DO NOTHING;
INSERT INTO "questions" ("reportid", "question", "questionscore")
VALUES (0, 'Drugie Pytanie?', 4)
    ON CONFLICT DO NOTHING;
INSERT INTO "questions" ("reportid", "question", "questionscore")
VALUES (1, 'Pytanie', 3.5)
    ON CONFLICT DO NOTHING;
INSERT INTO "questions" ("reportid", "question", "questionscore")
VALUES (1, 'Drugie Pytanie?', 4)
    ON CONFLICT DO NOTHING;