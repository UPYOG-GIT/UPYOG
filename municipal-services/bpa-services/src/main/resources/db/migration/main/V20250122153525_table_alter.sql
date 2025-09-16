ALTER TABLE eg_ndb_push_status
ADD COLUMN totalPlotArea DOUBLE PRECISION,
ADD COLUMN totalCollection DOUBLE PRECISION;


ALTER TABLE eg_wf_processinstance_v2
ALTER COLUMN comment type character varying(2048);


ALTER TABLE eg_bpa_buildingplan
ADD COLUMN propertyid character varying(64);

ALTER TABLE eg_bpa_auditdetails
ADD COLUMN propertyid character varying(64);