-- Inicjalizacja kategorii
INSERT INTO categories (name, color) VALUES ('Work', '#ff0000');
INSERT INTO categories (name, color) VALUES ('Home', '#00ff00');
INSERT INTO categories (name, color) VALUES ('Study', '#0000ff');

-- Przykładowe zadania (H2 nie wspiera ON DUPLICATE KEY tak samo jak MySQL, użyjemy prostych insertów)
-- Spring Boot wykona ten plik przy starcie jeśli ddl-auto nie usuwa wszystkiego
