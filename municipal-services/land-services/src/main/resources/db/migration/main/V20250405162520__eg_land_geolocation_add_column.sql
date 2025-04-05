ALTER TABLE eg_land_geolocation
ADD COLUMN gisplacename character varying(192);

ALTER TABLE eg_land_geolocation_auditdetails
ADD COLUMN gisplacename character varying(192);
