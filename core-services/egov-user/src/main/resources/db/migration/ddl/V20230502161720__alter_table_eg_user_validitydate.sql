ALTER TABLE eg_user
ADD COLUMN IF NOT EXISTS validitydate timestamp without time zone,
ADD COLUMN IF NOT EXISTS usertenantid character varying (30);