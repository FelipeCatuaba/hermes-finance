-- Remove color from persistence; it is now a frontend-only concern
ALTER TABLE family_members
DROP COLUMN IF EXISTS color_hex;
