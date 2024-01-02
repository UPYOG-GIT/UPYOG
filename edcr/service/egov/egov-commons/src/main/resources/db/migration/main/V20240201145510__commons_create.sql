ALTER TABLE edcr_rule_entry
DROP COLUMN IF EXISTS depth_width;

ALTER TABLE edcr_rule_entry
ADD COLUMN IF NOT EXISTS from_depth numeric(5,2),
ADD COLUMN IF NOT EXISTS to_depth numeric(5,2),
ADD COLUMN IF NOT EXISTS from_width numeric(5,2),
ADD COLUMN IF NOT EXISTS to_width numeric(5,2);