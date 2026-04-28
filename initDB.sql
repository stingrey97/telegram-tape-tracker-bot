CREATE DATABASE IF NOT EXISTS tapebot;
USE tapebot;

CREATE TABLE users (
    id INT AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    pin VARCHAR(4) NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE,
    wants_abonnement BOOLEAN DEFAULT TRUE,
    CONSTRAINT pk_users_id PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE TABLE tapes (
    id INT AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    by_username VARCHAR(255) NOT NULL,
    for_username VARCHAR(255) NOT NULL,
    date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_tapes_id PRIMARY KEY (id),
    CONSTRAINT fk_tapes_by_username FOREIGN KEY (by_username) REFERENCES users(username),
    CONSTRAINT fk_tapes_for_username FOREIGN KEY (for_username) REFERENCES users(username)
);

CREATE TABLE user_states (
    chat_id BIGINT,
    user_state VARCHAR(255) NOT NULL,
    username VARCHAR(255),
    CONSTRAINT pk_user_states_chat_id PRIMARY KEY (chat_id),
    CONSTRAINT fk_user_states_username FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

DELIMITER $$
CREATE TRIGGER before_user_delete
    BEFORE DELETE ON users
    FOR EACH ROW
BEGIN
    -- Setze den Titel und den for_username auf "deleted", wenn der Benutzer for_username ist
    UPDATE tapes
    SET title = 'deleted', for_username = 'deleted'
    WHERE for_username = OLD.username;

    -- Setze den by_username auf "deleted", wenn der Benutzer by_username ist
    UPDATE tapes
    SET by_username = 'deleted'
    WHERE by_username = OLD.username;
END$$
DELIMITER ;

INSERT INTO users (username, pin, is_admin, wants_abonnement)
VALUES ('deleted', 'xxxx', FALSE, FALSE);