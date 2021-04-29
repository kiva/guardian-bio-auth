-- Trigger to update modify time for below tables
CREATE OR REPLACE FUNCTION trigger_modify_time()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.modify_time = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Including a template_type to support multiple templating algorithms
-- Modify time is used to store with the template has being modified aka we've synced the fingerprint with the microservice
-- It worth to mention that the hash of national_id and voter_id are stored.
CREATE TABLE kiva_biometric_template (
    id SERIAL PRIMARY KEY,
    national_id VARCHAR(32) NOT NULL,
    voter_id VARCHAR(32) NOT NULL,
    did VARCHAR(32) NOT NULL,
    type_id INTEGER NOT NULL,
    version INTEGER NOT NULL,
    position INTEGER NOT NULL,
    template_type VARCHAR(200) NOT NULL,
    template TEXT,
    missing_code VARCHAR(2),
    quality_score INTEGER,
    capture_date timestamp with time zone,
    create_time TIMESTAMP NOT NULL DEFAULT now(),
    modify_time TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX national_id_template_index ON kiva_biometric_template (national_id);
CREATE INDEX voter_id_template_index ON kiva_biometric_template (voter_id);
CREATE INDEX type_id_template_index ON kiva_biometric_template (type_id);
CREATE INDEX version_template_index ON kiva_biometric_template (version);
CREATE INDEX position_template_index ON kiva_biometric_template (position);
CREATE INDEX template_type_template_index ON kiva_biometric_template (template_type);
ALTER TABLE kiva_biometric_template ADD CONSTRAINT unique_did_postion_template_constraint UNIQUE (did, "position", template_type, type_id);

CREATE TRIGGER biometric_template_modify_time
    BEFORE UPDATE ON kiva_biometric_template
    FOR EACH ROW EXECUTE PROCEDURE trigger_modify_time();