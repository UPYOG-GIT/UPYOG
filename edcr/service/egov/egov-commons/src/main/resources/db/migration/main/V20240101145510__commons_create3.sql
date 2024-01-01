ALTER TABLE  edcr_rule_entry
ADD COLUMN IF NOT EXISTS development_zone character varying(64),
ADD COLUMN IF NOT EXISTS road_width numeric(5,2),
ADD COLUMN IF NOT EXISTS no_of_floors int,
ADD COLUMN IF NOT EXISTS depth_width numeric(5,2),
ADD COLUMN IF NOT EXISTS tenant_id character varying(64);

