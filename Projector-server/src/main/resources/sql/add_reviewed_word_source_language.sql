-- Add source language and foreign language type to reviewed_word table
-- Run this script when deploying the Foreign Language Word Review feature.
--
-- Usage:
--   mysql -u songbook -p songbook < Projector-server/src/main/resources/sql/add_reviewed_word_source_language.sql

ALTER TABLE `reviewed_word`
ADD COLUMN `source_language_id` BIGINT NULL,
ADD COLUMN `foreign_language_type` INT NULL,
ADD CONSTRAINT `fk_reviewed_word_source_language` FOREIGN KEY (`source_language_id`) REFERENCES `language` (`id`);
