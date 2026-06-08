package com.telegramquiz.main.entity;

/**
 * Ordered by privilege level (lower ordinal = more access).
 * OWNER=0, CO_OWNER=1, CONTRIBUTOR=2, NONE=3
 */
public enum FolderAccessLevel {
    OWNER,
    CO_OWNER,
    CONTRIBUTOR,
    NONE
}
