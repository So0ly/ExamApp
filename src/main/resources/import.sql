INSERT INTO "examiners" ("mail", "password", "firstname", "lastname", "titles")
VALUES ('admin@pbs.edu.pl', '$2a$10$7b.9iLgXFVh.r1u9HEbMv.EDL3JcJgldsWHUg4etSUh4wCNGuExye', 'Admin', 'Adminowski', 'adm')
    ON CONFLICT DO NOTHING;
INSERT INTO "user_roles" ("id", "roles") VALUES (1, 'admin')
    ON CONFLICT DO NOTHING;
INSERT INTO "user_roles" ("id", "roles") VALUES (1, 'user')
    ON CONFLICT DO NOTHING;
INSERT INTO "examiners" ("mail", "password", "firstname", "lastname", "titles")
VALUES ('user@pbs.edu.pl', '$2a$10$7b.9iLgXFVh.r1u9HEbMv.EDL3JcJgldsWHUg4etSUh4wCNGuExye', 'Mikołaj', 'Barański', 'inż')
    ON CONFLICT DO NOTHING;
INSERT INTO "user_roles" ("id", "roles") VALUES (2, 'user')
    ON CONFLICT DO NOTHING;
INSERT INTO "examiners" ("mail", "password", "firstname", "lastname", "titles")
VALUES ('user1@pbs.edu.pl', '$2a$10$7b.9iLgXFVh.r1u9HEbMv.EDL3JcJgldsWHUg4etSUh4wCNGuExye', 'User', 'User', 'mgr inż.')
ON CONFLICT DO NOTHING;
INSERT INTO "user_roles" ("id", "roles") VALUES (3, 'user')
ON CONFLICT DO NOTHING;
INSERT INTO "students" ("firstname", "lastname", "index")
VALUES ('Mikołaj', 'Barański', '111222')
    ON CONFLICT DO NOTHING;
INSERT INTO "students" ("firstname", "lastname", "index")
VALUES ('Jan', 'Kowalski', '222222')
    ON CONFLICT DO NOTHING;
INSERT INTO "reports" ("student_id", "examiner_id", "classname", "examdate", "finalgrade", "examduration")
VALUES (1, 2, 'Mikroprocesory', NOW(), 4, '15s')
    ON CONFLICT DO NOTHING;
INSERT INTO "reports" ("student_id", "examiner_id", "classname", "examdate", "finalgrade", "examduration")
VALUES (2, 2, 'Przetwarzanie obrazów', NOW(), 4, '1m')
    ON CONFLICT DO NOTHING;
INSERT INTO "reports" ("student_id", "examiner_id", "classname", "examdate", "finalgrade", "examduration")
VALUES (2, 3, 'Przetwarzanie obrazów', NOW(), 4, '1m 20s')
ON CONFLICT DO NOTHING;
INSERT INTO "questions" ("reportid", "question", "questionscore")
VALUES (1, 'Co to HAL_Delay?', 3.5)
    ON CONFLICT DO NOTHING;
INSERT INTO "questions" ("reportid", "question", "questionscore")
VALUES (1, 'Omów w jaki sposób STM32 i czyste C jest lepsze od konkurencji.', 4)
    ON CONFLICT DO NOTHING;
INSERT INTO "questions" ("reportid", "question", "questionscore")
VALUES (2, 'Co to percepcja?', 3.5)
    ON CONFLICT DO NOTHING;
INSERT INTO "questions" ("reportid", "question", "questionscore")
VALUES (2, 'Co to translacja?', 4)
    ON CONFLICT DO NOTHING;
INSERT INTO "questions" ("reportid", "question", "questionscore")
VALUES (3, 'Omów macierz translacji', 3.5)
ON CONFLICT DO NOTHING;
INSERT INTO "questions" ("reportid", "question", "questionscore")
VALUES (3, 'Czym jest filtr Phonga?', 4)
ON CONFLICT DO NOTHING;