ALTER TABLE edcr_rule_entry
DROP COLUMN IF EXISTS no_of_floors;

ALTER TABLE edcr_rule_entry
ADD COLUMN IF NOT EXISTS multi_stories character varying(5),
ADD COLUMN IF NOT EXISTS high_rise character varying(5),
ADD COLUMN IF NOT EXISTS createdby character varying(128),
ADD COLUMN IF NOT EXISTS createddate timestamp without time zone;