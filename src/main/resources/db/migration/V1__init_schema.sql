CREATE TABLE bob_groups
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(64)                        NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(128)                       NOT NULL,
    name       VARCHAR(64)                        NOT NULL,
    password   VARCHAR(255)                       NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE group_memberships
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id  BIGINT      NOT NULL,
    user_id   BIGINT      NOT NULL,
    role      VARCHAR(50) NOT NULL,
    joined_at DATETIME    NOT NULL,
    CONSTRAINT group_memberships__bob_groups
        FOREIGN KEY (group_id) REFERENCES bob_groups (id)
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT group_memberships__users
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT group_memberships__unique_membership
        UNIQUE (group_id, user_id)
);

CREATE TABLE bob_assignments
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT   NOT NULL,
    group_id    BIGINT   NOT NULL,
    assigned_at DATETIME NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT bob_assignments__bob_groups
        FOREIGN KEY (group_id) REFERENCES bob_groups (id)
            ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT bob_assignments__users
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE tokens
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    token      VARCHAR(255)                       NOT NULL,
    user_id    BIGINT                             NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expires_at DATETIME                           NOT NULL,
    CONSTRAINT tokens__users
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON UPDATE CASCADE ON DELETE CASCADE
);
