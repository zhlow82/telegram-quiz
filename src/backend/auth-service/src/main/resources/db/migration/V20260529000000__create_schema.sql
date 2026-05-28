-- V1: Initial schema
-- Creates the users and user_roles tables for auth-service.
-- Uses IF NOT EXISTS so this is safe to run against an existing database.

CREATE TABLE IF NOT EXISTS users (
    id       BIGSERIAL    PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT       NOT NULL,
    role    VARCHAR(255),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id)
);
