INSERT INTO users (id, email, name, role, profile_picture, date_of_birth, gender, created_at, telegram_chat_id)
VALUES
    ('user-1', 'alice@example.com', 'Alice Patient', 'PATIENT', NULL, '1990-01-01', 'F', NOW(), NULL),
    ('user-2', 'bob@example.com', 'Bob Caregiver', 'CAREGIVER', NULL, '1985-05-15', 'M', NOW(), NULL),
    ('user-3', 'carol@example.com', 'Carol Patient', 'PATIENT', NULL, '1975-09-20', 'F', NOW(), NULL),
    ('user-4', 'dave@example.com', 'Dave Caregiver', 'CAREGIVER', NULL, '1982-12-10', 'M', NOW(), NULL);


INSERT INTO patient_details (user_id, primary_contact)
VALUES
    ('user-1', 'user-2'),  -- Alice's primary contact is Bob
    ('user-3', 'user-4');  -- Carol's primary contact is Dave


INSERT INTO patient_caregiver (id, patient_id, caregiver_id, created_at, removed_at)
VALUES
    ('f401c189-8ec9-4ecd-8793-f8b38f470ebe', 'user-1', 'user-2', NOW(), NULL),  -- Alice <-> Bob
    ('00fe4b32-c8ad-4a82-946f-f60a95af949b', 'user-3', 'user-4', NOW(), NULL);  -- Carol <-> Dave
