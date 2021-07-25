DROP TABLE IF EXISTS access_requests;

CREATE TABLE access_requests
(
    id         VARCHAR(64) PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    status     VARCHAR(32)  NOT NULL,
    key        VARCHAR(255) DEFAULT NULL,
    expiration TIMESTAMP    DEFAULT NULL,
    creation   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);