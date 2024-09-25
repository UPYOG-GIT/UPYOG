CREATE TABLE IF NOT EXISTS  eg_ndb_push_status(
    id SERIAL PRIMARY KEY,
    response_hash TEXT,
    error TEXT,
    createddate timestamp without time zone
);