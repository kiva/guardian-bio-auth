CREATE TABLE replays (
    id SERIAL PRIMARY KEY,
    hash_code VARCHAR(255) NOT NULL UNIQUE,
    time_seen TIMESTAMPTZ DEFAULT now(),
    count_seen INTEGER DEFAULT 1
);