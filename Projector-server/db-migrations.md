# Database migrations (manual – run on production when needed)

## Add `has_unsolved_words` to `song` table

**If the column already exists** (Error 1060: Duplicate column name), drop it first:

```sql
ALTER TABLE song
DROP COLUMN has_unsolved_words;
```

Then add the column:

```sql
ALTER TABLE song
ADD COLUMN has_unsolved_words bit(1) DEFAULT NULL;
```

## Add `source_language_id` and `foreign_language_type` to `reviewed_word` table

Run the migration script:

```bash
mysql -u songbook -p songbook < Projector-server/src/main/resources/sql/add_reviewed_word_source_language.sql
```

Or execute manually:

```sql
ALTER TABLE `reviewed_word`
ADD COLUMN `source_language_id` BIGINT NULL,
ADD COLUMN `foreign_language_type` INT NULL,
ADD CONSTRAINT `fk_reviewed_word_source_language` FOREIGN KEY (`source_language_id`) REFERENCES `language` (`id`);
```