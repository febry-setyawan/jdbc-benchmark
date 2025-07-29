CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role_id BIGINT,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Contoh data (opsional, untuk pengujian)
INSERT INTO roles (id, name) VALUES (1, 'Admin');
INSERT INTO roles (id, name) VALUES (2, 'User');

INSERT INTO users (id, name, email, role_id) VALUES (1, 'Febry', 'febry@gmail.com', 1);
INSERT INTO users (id, name, email, role_id) VALUES (2, 'Jane Doe', 'jane@example.com', 2);
