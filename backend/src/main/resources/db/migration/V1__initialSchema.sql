CREATE TABLE chats
(
    id         UUID                        NOT NULL,
    user_id    VARCHAR(255)                NOT NULL,
    type       VARCHAR(20)                 NOT NULL,
    message    TEXT                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_chats PRIMARY KEY (id)
);

CREATE TABLE logs
(
    id          UUID                        NOT NULL,
    user_id     VARCHAR(255)                NOT NULL,
    type        VARCHAR(20)                 NOT NULL,
    description TEXT                        NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_logs PRIMARY KEY (id)
);

CREATE TABLE patient_caregiver
(
    id           UUID                        NOT NULL,
    patient_id   VARCHAR(255)                NOT NULL,
    caregiver_id VARCHAR(255)                NOT NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    removed_at   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_patient_caregiver PRIMARY KEY (id)
);

CREATE TABLE patient_details
(
    user_id         VARCHAR(255) NOT NULL,
    primary_contact VARCHAR(255) NOT NULL,
    CONSTRAINT pk_patient_details PRIMARY KEY (user_id)
);

CREATE TABLE reminders
(
    id              UUID                        NOT NULL,
    user_id         VARCHAR(255)                NOT NULL,
    type            VARCHAR(20)                 NOT NULL,
    title           VARCHAR(128)                NOT NULL,
    description     VARCHAR(512)                NOT NULL,
    cron_expression VARCHAR(255)                NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_reminders PRIMARY KEY (id)
);

CREATE TABLE subscriptions
(
    id         UUID                        NOT NULL,
    user_id    VARCHAR(255)                NOT NULL,
    type       VARCHAR(20)                 NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_subscriptions PRIMARY KEY (id)
);

CREATE TABLE users
(
    id              VARCHAR(255)                NOT NULL,
    email           VARCHAR(50)                 NOT NULL,
    name            VARCHAR(255)                NOT NULL,
    role            VARCHAR(20)                 NOT NULL,
    profile_picture VARCHAR(1000),
    date_of_birth   date                        NOT NULL,
    gender          VARCHAR(1)                  NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE chats
    ADD CONSTRAINT FK_CHATS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE logs
    ADD CONSTRAINT FK_LOGS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE patient_caregiver
    ADD CONSTRAINT FK_PATIENT_CAREGIVER_ON_CAREGIVER FOREIGN KEY (caregiver_id) REFERENCES users (id);

ALTER TABLE patient_caregiver
    ADD CONSTRAINT FK_PATIENT_CAREGIVER_ON_PATIENT FOREIGN KEY (patient_id) REFERENCES users (id);

ALTER TABLE patient_details
    ADD CONSTRAINT FK_PATIENT_DETAILS_ON_PRIMARY_CONTACT FOREIGN KEY (primary_contact) REFERENCES users (id);

ALTER TABLE patient_details
    ADD CONSTRAINT FK_PATIENT_DETAILS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE reminders
    ADD CONSTRAINT FK_REMINDERS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE subscriptions
    ADD CONSTRAINT FK_SUBSCRIPTIONS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);