DROP INDEX IF EXISTS voter_id_template_index, national_id_template_index;

ALTER TABLE kiva_biometric_template
DROP COLUMN IF EXISTS national_id,
DROP COLUMN IF EXISTS voter_id;