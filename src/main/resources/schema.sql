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

    -- Menambahkan Indeks untuk Kolom yang Sering Dicari/Digabungkan
    -- Indeks pada role_id di tabel users (Foreign Key)
    CREATE INDEX idx_users_role_id ON users (role_id);

    -- Indeks pada kolom name di tabel users (sering dicari)
    CREATE INDEX idx_users_name ON users (name);

    -- Indeks pada kolom email di tabel users (sering dicari)
    CREATE INDEX idx_users_email ON users (email);

    -- Contoh data (opsional, untuk pengujian)
    INSERT INTO roles (id, name) VALUES (1, 'Admin');
    INSERT INTO roles (id, name) VALUES (2, 'User');

    INSERT INTO users (id, name, email, role_id) VALUES (1, 'Febry', 'febry@gmail.com', 1);
    INSERT INTO users (id, name, email, role_id) VALUES (2, 'Jane Doe', 'jane@example.com', 2);
    