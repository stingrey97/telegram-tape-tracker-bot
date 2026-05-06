USE tape_bot_db;

INSERT INTO users (username, pin, is_admin) VALUES ('Vincent', '0000', 1);
INSERT INTO users (username, pin, is_admin) VALUES ('Johnny', '0000', 1);

INSERT INTO tapes (title, by_username, for_username) VALUES ('Ich mags doll.', 'Vincent', 'Johnny');
INSERT INTO tapes (title, by_username, for_username) VALUES ('Richtig dreckig.', 'Johnny', 'Vincent');