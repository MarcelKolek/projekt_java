-- Admin user (password: password)
INSERT INTO users (id, username, password, email)
VALUES (1, 'admin', '$2a$10$TU_WKLEJ_PRAWIDLOWY_HASH_BCRYPT...', 'admin@example.com');

INSERT INTO user_roles (user_id, role) VALUES (1, 'ROLE_USER');

SELECT setval(pg_get_serial_sequence('users','id'), (SELECT MAX(id) FROM users));

INSERT INTO categories (name, color, user_id) VALUES ('Praca', '#0d6efd', 1);
INSERT INTO categories (name, color, user_id) VALUES ('Dom', '#198754', 1);
INSERT INTO categories (name, color, user_id) VALUES ('Studia', '#6f42c1', 1);
