ALTER TABLE eg_land_Address
ADD COLUMN IF NOT EXISTS khataNo character varying(20),
ADD COLUMN IF NOT EXISTS mauza character varying(20),
ADD COLUMN IF NOT EXISTS occupancy character varying(40),
ADD COLUMN IF NOT EXISTS plotArea character varying(8);


ALTER TABLE eg_land_address_auditdetails
ADD COLUMN IF NOT EXISTS khataNo character varying(20),
ADD COLUMN IF NOT EXISTS mauza character varying(20),
ADD COLUMN IF NOT EXISTS occupancy character varying(40),
ADD COLUMN IF NOT EXISTS plotArea character varying(8);