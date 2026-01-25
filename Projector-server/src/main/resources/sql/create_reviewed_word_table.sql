-- Create reviewed_word table
-- This script creates the reviewed_word table that is required by the ReviewedWord entity.
-- Run this script against your MySQL database before using the ReviewedWord functionality.
--
-- Usage:
--   mysql -u songbook -p songbook < Projector-server/src/main/resources/sql/create_reviewed_word_table.sql
--   Or execute this script using your MySQL client tool

CREATE TABLE IF NOT EXISTS `reviewed_word` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `uuid` VARCHAR(36),
    `word` VARCHAR(500) NOT NULL COLLATE utf8mb4_bin,
    `normalized_word` VARCHAR(500) NOT NULL,
    `language_id` BIGINT NOT NULL,
    `status` VARCHAR(50) NOT NULL,
    `category` VARCHAR(100),
    `context_category` VARCHAR(100),
    `context_description` VARCHAR(500),
    `reviewed_by_id` BIGINT,
    `reviewed_date` TIMESTAMP NULL,
    `notes` VARCHAR(1000),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uuid_index` (`uuid`),
    UNIQUE KEY `word_language_index` (`word`(200), `language_id`, `normalized_word`(200)),
    KEY `language_status_index` (`language_id`, `status`),
    KEY `normalized_word_index` (`normalized_word`(200)),
    CONSTRAINT `fk_reviewed_word_language` FOREIGN KEY (`language_id`) REFERENCES `language` (`id`),
    CONSTRAINT `fk_reviewed_word_user` FOREIGN KEY (`reviewed_by_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
