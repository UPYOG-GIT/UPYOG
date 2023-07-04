ALTER TABLE eg_land_address
ADD COLUMN IF NOT EXISTS khataNo character varying(20),
ADD COLUMN IF NOT EXISTS mauza character varying(20),
ADD COLUMN IF NOT EXISTS occupancy character varying(40),
ADD COLUMN IF NOT EXISTS plotArea character varying(8),
ADD COLUMN IF NOT EXISTS patwariHN character varying(15),
ADD COLUMN IF NOT EXISTS wardNo character varying(5),
ADD COLUMN IF NOT EXISTS address character varying(256);


ALTER TABLE eg_land_address_auditdetails
ADD COLUMN IF NOT EXISTS khataNo character varying(20),
ADD COLUMN IF NOT EXISTS mauza character varying(20),
ADD COLUMN IF NOT EXISTS occupancy character varying(40),
ADD COLUMN IF NOT EXISTS plotArea character varying(8),
ADD COLUMN IF NOT EXISTS patwariHN character varying(15),
ADD COLUMN IF NOT EXISTS wardNo character varying(5),
ADD COLUMN IF NOT EXISTS address character varying(256);