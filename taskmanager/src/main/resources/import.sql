-- Inicjalizacja kategorii (user_id=null dla globalnych lub do przypisania)
-- Ponieważ teraz każda kategoria MUSI mieć user_id (zgodnie z moimi zmianami izolacji), 
-- lepiej stworzyć startowego użytkownika.
INSERT INTO users (id, username, password, email) VALUES (1, 'admin', '$2a$10$vYfXmY8W1TzXmY8W1TzXmY8W1TzXmY8W1TzXmY8W1TzXmY8W1TzXm', 'admin@example.com'); -- password: password
INSERT INTO user_roles (user_id, role) VALUES (1, 'ROLE_USER');

INSERT INTO categories (name, color, user_id) VALUES ('Praca', '#0d6efd', 1);
INSERT INTO categories (name, color, user_id) VALUES ('Dom', '#198754', 1);
INSERT INTO categories (name, color, user_id) VALUES ('Studia', '#6f42c1', 1);
