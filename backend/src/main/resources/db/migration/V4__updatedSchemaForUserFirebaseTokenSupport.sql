CREATE TABLE user_devices
(
    id          VARCHAR(255)                NOT NULL,
    user_id     VARCHAR(255)                NOT NULL,
    token       VARCHAR(255)                NOT NULL,
    device_name   VARCHAR(255)              NOT NULL,
    last_update TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_user_devices PRIMARY KEY (id)
);

ALTER TABLE user_devices
    ADD CONSTRAINT FK_USER_DEVICES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);