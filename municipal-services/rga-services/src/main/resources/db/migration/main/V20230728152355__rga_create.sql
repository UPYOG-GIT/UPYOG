CREATE TABLE IF NOT EXISTS eg_rga_buildingplan
(
    id character varying(256) NOT NULL,
    applicationno character varying(64),
    tenantid character varying(256),
    edcrnumber character varying(64),
    status character varying(64),
    landid character varying(256),
	risktype character varying(32),
    additionaldetails jsonb,
    createdby character varying(64),
    lastmodifiedby character varying(64),
    createdtime bigint,
    lastmodifiedtime bigint,
    approvalno character varying(64)DEFAULT NULL::character varying,
    approvaldate bigint,
    applicationdate bigint,
    businessservice character varying(64)DEFAULT NULL::character varying,
    accountid character varying(256) DEFAULT NULL::character varying,
    CONSTRAINT pk_eg_rga_buildingplan PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS eg_rga_document
(
    id character varying(64) NOT NULL,
    documenttype character varying(64),
    filestoreid character varying(64),
    documentuid character varying(64),
    buildingplanid character varying(64),
    additionaldetails jsonb,
    createdby character varying(64),
    lastmodifiedby character varying(64),
    createdtime bigint,
    lastmodifiedtime bigint,
    CONSTRAINT uk_eg_rga_document PRIMARY KEY (id),
    CONSTRAINT fk_eg_rga_document FOREIGN KEY (buildingplanid)
        REFERENCES eg_rga_buildingplan (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);


CREATE TABLE IF NOT EXISTS eg_rga_auditdetails
(
    id character varying(256) NOT NULL,
    applicationno character varying(64),
    tenantid character varying(256),
    edcrnumber character varying(64),
    status character varying(64),
    landid character varying(256),
	risktype character varying(32),
    additionaldetails jsonb,
    createdby character varying(64),
    lastmodifiedby character varying(64),
    createdtime bigint,
    lastmodifiedtime bigint,
    approvalno character varying(64) DEFAULT NULL::character varying,
    approvaldate bigint,
    applicationdate bigint,
    businessservice character varying(64) DEFAULT NULL::character varying,
    accountid character varying(256) DEFAULT NULL::character varying
);


CREATE SEQUENCE IF NOT EXISTS seq_eg_rg_apn
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;
    
    
CREATE SEQUENCE IF NOT EXISTS seq_eg_rg_pn
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;