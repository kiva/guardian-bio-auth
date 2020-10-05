--
-- Name: fingerprint_images; Type: TABLE; Schema: public; Owner: dbuser
--

CREATE TABLE fingerprint_images (
    image_id SERIAL,
    hash_code VARCHAR(255) NOT NULL,
    time_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    count_seen INTEGER DEFAULT 1,
    PRIMARY KEY (image_id),
    CONSTRAINT unique_codes UNIQUE(hash_code)
);
