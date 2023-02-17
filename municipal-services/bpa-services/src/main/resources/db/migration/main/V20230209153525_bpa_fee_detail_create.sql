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