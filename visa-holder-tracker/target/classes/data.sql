INSERT INTO users (passport_number, full_name, password_hash, email, role)
VALUES ('P654321', 'Normal User', '$2a$10$7QJ8Z1V1Z1V1Z1V1Z1V1ZeK1Z1V1Z1V1Z1V1Z1V1Z1V1Z1V1Z1V1Z', 'user1@tracker.com', 'USER');

INSERT INTO admins (full_name, password_hash, email, role)
VALUES ('Admin User', '$2a$10$7QJ8Z1V1Z1V1Z1V1Z1V1ZeK1Z1V1Z1V1Z1V1Z1V1Z1V1Z1V1Z1V1Z', 'admin@tracker.com', 'ADMIN');