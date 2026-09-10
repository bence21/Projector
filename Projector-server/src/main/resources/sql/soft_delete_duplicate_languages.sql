-- Soft-delete duplicate language rows (run after preview query below).
--
-- Step 1 — Preview duplicates:
-- SELECT
--   l.id, l.uuid, l.english_name, l.native_name, l.deleted,
--   (SELECT COUNT(*) FROM song s WHERE s.language_id = l.id AND (s.deleted IS NULL OR s.deleted = 0)) AS song_count
-- FROM `language` l
-- WHERE (l.deleted IS NULL OR l.deleted = FALSE)
--   AND LOWER(TRIM(l.english_name)) IN (
--     SELECT LOWER(TRIM(english_name)) FROM `language`
--     WHERE deleted IS NULL OR deleted = FALSE
--     GROUP BY LOWER(TRIM(english_name))
--     HAVING COUNT(*) > 1
--   )
-- ORDER BY LOWER(TRIM(l.english_name)), song_count DESC, l.id;
--
-- Step 2 — Soft-delete known zero-song duplicates (adjust UUIDs after preview on target environment):
UPDATE `language`
SET `deleted` = TRUE
WHERE `uuid` IN (
  'e010ddf2-bf79-43a5-838a-1482b15de038',
  '97b02f32-d43c-4a49-900b-da94b60cc67e',
  '098f93e6-fc02-46d6-af5e-f4754a94fce6',
  '90d1870c-91d8-41bb-845d-9f61e787ae77',
  '8c023622-1a29-406f-a149-e593ae737f10',
  '31541233-0fef-420f-a164-3bd433025ae4',
  '3b39eca8-3180-481b-b936-821b1c9ebd0e',
  '4b3b7a02-0a72-4c77-aa7c-5052e09bdf5a',
  '754c3bfb-af4e-494a-a4c9-6f48c3d742a4'
);
--
-- Step 3 — Optional generic cleanup (MySQL 8+, keeps row with most songs per english_name):
-- UPDATE `language` dup
-- JOIN (
--   SELECT l.id,
--          ROW_NUMBER() OVER (
--            PARTITION BY LOWER(TRIM(l.english_name))
--            ORDER BY (
--              SELECT COUNT(*) FROM song s
--              WHERE s.language_id = l.id AND (s.deleted IS NULL OR s.deleted = 0)
--            ) DESC, l.id ASC
--          ) AS rn
--   FROM `language` l
--   WHERE l.deleted IS NULL OR l.deleted = FALSE
-- ) ranked ON dup.id = ranked.id
-- SET dup.deleted = TRUE
-- WHERE ranked.rn > 1;
