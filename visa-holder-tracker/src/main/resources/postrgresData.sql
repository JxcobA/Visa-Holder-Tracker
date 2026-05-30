-- Users:
INSERT INTO users (passport_number, full_name, password_hash, email, role)
VALUES ('P654321', 'Normal User', '$2a$12$sbH9wQrcNS4wmGTB8HYTNuWXJQUsInUPEGqA.BKIJR2AljVjgnpPy', 'user1@dreamteam.com', 'USER');

INSERT INTO users (passport_number, full_name, password_hash, email, role)
VALUES ('P111222', 'Another User', '$2a$12$sbH9wQrcNS4wmGTB8HYTNuWXJQUsInUPEGqA.BKIJR2AljVjgnpPy', 'user2@dreamteam.com', 'USER');

INSERT INTO users (passport_number, full_name, password_hash, email, role)
VALUES ('P333444', 'Tertiary User', '$2a$12$sbH9wQrcNS4wmGTB8HYTNuWXJQUsInUPEGqA.BKIJR2AljVjgnpPy', 'user3@dreamteam.com', 'USER');

-- Admins:
INSERT INTO admins (full_name, password_hash, email, role)
VALUES ('Admin User', '$2a$12$sbH9wQrcNS4wmGTB8HYTNuWXJQUsInUPEGqA.BKIJR2AljVjgnpPy', 'admin1@dreamteam.com', 'ADMIN');

INSERT INTO admins (full_name, password_hash, email, role)
VALUES ('Another Admin', '$2a$12$sbH9wQrcNS4wmGTB8HYTNuWXJQUsInUPEGqA.BKIJR2AljVjgnpPy', 'admin2@dreamteam.com', 'ADMIN');

INSERT INTO admins (full_name, password_hash, email, role)
VALUES ('Tertiary Admin', '$2a$12$sbH9wQrcNS4wmGTB8HYTNuWXJQUsInUPEGqA.BKIJR2AljVjgnpPy', 'admin3@dreamteam.com', 'ADMIN');


-- Visa Holders:

-- Active holder
INSERT INTO visa_holders (passport_number, full_name, nationality, visa_type, expiry_date, entry_date, status)
VALUES ('VH001', 'Aiko Tanaka', 'Japanese', 'Student', '2027-01-15 00:00:00', '2025-09-01 00:00:00', 'ACTIVE');

-- Expiring within 30 days (triggers expiring-soon alert)
INSERT INTO visa_holders (passport_number, full_name, nationality, visa_type, expiry_date, entry_date, status)
VALUES ('VH002', 'Luca Rossi', 'Italian', 'Work',  NOW() + INTERVAL '20 days', '2024-06-01 00:00:00', 'ACTIVE');

-- Already expired
INSERT INTO visa_holders (passport_number, full_name, nationality, visa_type, expiry_date, entry_date, status)
VALUES ('VH003', 'Fatima Al-Hassan', 'Moroccan', 'Tourist', '2024-11-30 00:00:00', '2024-08-01 00:00:00', 'EXPIRED');

-- Overstay: expired date but status still ACTIVE
INSERT INTO visa_holders (passport_number, full_name, nationality, visa_type, expiry_date, entry_date, status)
VALUES ('VH004', 'Chen Wei', 'Chinese', 'Business', '2025-03-01 00:00:00', '2024-12-01 00:00:00', 'ACTIVE');

-- Active, long-term holder with rich movement history
INSERT INTO visa_holders (passport_number, full_name, nationality, visa_type, expiry_date, entry_date, status)
VALUES ('VH005', 'Priya Sharma', 'Indian', 'Student', '2026-08-31 00:00:00', '2024-01-10 00:00:00', 'ACTIVE');


-- Movements:

-- Single entry, still in country
INSERT INTO movement (passport_number, entry_date, exit_date)
VALUES ('VH001', '2025-09-01 10:30:00', NULL);

-- Entered, left, re-entered
INSERT INTO movement (passport_number, entry_date, exit_date)
VALUES ('VH002', '2024-06-01 08:00:00', '2024-08-15 14:00:00');

INSERT INTO movement (passport_number, entry_date, exit_date)
VALUES ('VH002', '2024-09-10 09:00:00', NULL);

-- Entered and exited (expired, gone)
INSERT INTO movement (passport_number, entry_date, exit_date)
VALUES ('VH003', '2024-08-01 11:00:00', '2024-11-28 16:00:00');

-- Entered, never exited (overstay scenario)
INSERT INTO movement (passport_number, entry_date, exit_date)
VALUES ('VH004', '2024-12-01 07:45:00', NULL);

-- Multiple trips over a long stay
INSERT INTO movement (passport_number, entry_date, exit_date)
VALUES ('VH005', '2024-01-10 13:00:00', '2024-04-20 10:00:00');

INSERT INTO movement (passport_number, entry_date, exit_date)
VALUES ('VH005', '2024-05-05 09:30:00', '2024-07-01 18:00:00');

INSERT INTO movement (passport_number, entry_date, exit_date)
VALUES ('VH005', '2024-09-15 11:00:00', NULL);