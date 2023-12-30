ALTER TABLE IF NOT EXISTS demo.edcr_rule_entry
ADD COLUMN development_zone character varying(64),
ADD COLUMN road_width numeric(5,2),
ADD COLUMN no_of_floors int,
ADD COLUMN depth_width numeric(5,2);

