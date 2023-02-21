CREATE TABLE IF NOT EXISTS paytype_master
(
    id SERIAL PRIMARY KEY,
    ulb_tenantid character varying(56),
    charges_type_name character varying(256),
    payment_type character varying(10),
    defunt character varying(2),
    createdby character varying(128),
	createddate timestamp without time zone,
	updatedby character varying(128),
	updateddate timestamp without time zone,
	UNIQUE (ulb_tenantid, charges_type_name)
);


CREATE TABLE IF NOT EXISTS pre_post_fee_details
(
    id SERIAL PRIMARY KEY,
	paytype_id bigint,
    ulb_tenantid character varying(56),
	bill_id character varying(64),
	application_no character varying(64),
	unit_id character varying(56),
	pay_id character varying(56),
    charges_type_name character varying(256),
	amount numeric(15,2),
	status_type character varying(15),
	propvalue character varying(56),
	value character varying(56),
	status character varying(20),
	createdby character varying(128),
	createddate timestamp without time zone,
	payment_type character varying(10)
);

CREATE TABLE IF NOT EXISTS proposal_type_master
(
    ID SERIAL PRIMARY KEY,
	ULB_TENANTID character varying(56),
	DESCRIPTION character varying(100),
	DEFUNT character varying(2),
	CREATEDBY character varying(128),
	CREATEDDATE timestamp without time zone,
	UPDATEDBY character varying(128),
	UPDATEDDATE timestamp without time zone
);


CREATE TABLE IF NOT EXISTS bcategory_master
(
    ID SERIAL PRIMARY KEY,
	ULB_TENANTID character varying(56),
	DESCRIPTION character varying(100),
	DEFUNT character varying(2),
	CREATEDBY character varying(128),
	CREATEDDATE timestamp without time zone,
	UPDATEDBY character varying(128),
	UPDATEDDATE timestamp without time zone
);


CREATE TABLE IF NOT EXISTS bscategory_master
(
    ID SERIAL PRIMARY KEY,
	CATID bigint,
	ULB_TENANTID character varying(56),
	DESCRIPTION character varying(100),
	DEFUNT character varying(2),
	CREATEDBY character varying(128),
	CREATEDDATE timestamp without time zone,
	UPDATEDBY character varying(128),
	UPDATEDDATE timestamp without time zone,
	CONSTRAINT fk_bscategory_master_id FOREIGN KEY (CATID)
        REFERENCES bcategory_master(ID) MATCH SIMPLE
);



CREATE TABLE IF NOT EXISTS pay_tp_rate_master
(
    ID SERIAL PRIMARY KEY,
    ULB_TENANTID character varying(56),
	UNITID character varying(40),
	TYPEID bigint,
	SRNO bigint,
	CALCON character varying(56),
    CALCACT character varying(56),
	P_CATEGORY bigint,
	B_CATEGORY bigint,
	S_CATEGORY bigint,
	RATE_RES numeric(10,2),
	RATE_COMM numeric(10,2),
	RATE_IND numeric(10,2),
	PERVAL numeric(10),
	CREATEDBY character varying(128),
	CREATEDDATE timestamp without time zone,
	CONSTRAINT fk_pcategory_master_id FOREIGN KEY (P_CATEGORY)
        REFERENCES proposal_type_master(ID) MATCH SIMPLE,
	CONSTRAINT fk_bcategory_master_id FOREIGN KEY (B_CATEGORY)
        REFERENCES bcategory_master(ID) MATCH SIMPLE,
	CONSTRAINT fk_bscategory_master_id FOREIGN KEY (S_CATEGORY)
        REFERENCES bscategory_master(ID) MATCH SIMPLE
	
);



ALTER TABLE paytype_master 
ADD COLUMN optflag character varying(2),
ADD COLUMN hrnh character varying(2);
