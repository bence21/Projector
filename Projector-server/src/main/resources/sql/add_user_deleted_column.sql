-- Add deleted column for soft-delete support (preserves lastModifiedBy referential integrity).
-- Run manually if Hibernate does not auto-create: mysql -u songbook -p songbook < Projector-server/src/main/resources/sql/add_user_deleted_column.sql
ALTER TABLE `user` ADD COLUMN `deleted` BOOLEAN DEFAULT FALSE;
