INSERT INTO users (passport_number, full_name, password_hash, email, role)
VALUES ('P654321', 'Normal User', '$2a$12$sbH9wQrcNS4wmGTB8HYTNuWXJQUsInUPEGqA.BKIJR2AljVjgnpPy', 'user1@dreamteam.com', 'USER');

INSERT INTO admins (full_name, password_hash, email, role)
VALUES ('Admin User', '$2a$12$sbH9wQrcNS4wmGTB8HYTNuWXJQUsInUPEGqA.BKIJR2AljVjgnpPy', 'admin@dreamteam.com', 'ADMIN');