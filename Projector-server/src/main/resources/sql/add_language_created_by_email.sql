-- Add created_by_id to language for tracking who created each language.
-- Run manually if Hibernate does not auto-create:
--   mysql -u songbook -p songbook < Projector-server/src/main/resources/sql/add_language_created_by_email.sql
ALTER TABLE `language` ADD COLUMN `created_by_id` BIGINT NULL;
ALTER TABLE `language`
    ADD CONSTRAINT `fk_language_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `user` (`id`);
