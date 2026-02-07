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