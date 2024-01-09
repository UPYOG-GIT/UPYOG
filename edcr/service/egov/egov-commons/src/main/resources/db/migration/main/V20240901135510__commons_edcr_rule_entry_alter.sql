ALTER TABLE edcr_rule_entry
ADD COLUMN IF NOT EXISTS floor_number int,
ADD COLUMN IF NOT EXISTS building_height numeric(5,2);