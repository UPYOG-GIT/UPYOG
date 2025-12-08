DROP TABLE IF EXISTS  eg_pg_resp;

CREATE TABLE "eg_pg_resp" (
	"txn_id" VARCHAR(128) NOT NULL,
	"txn_response" VARCHAR NULL,
	PRIMARY KEY ("txn_id")
);


CREATE TABLE IF NOT EXISTS eg_pg_ccavenue_details
(
    id SERIAL PRIMARY KEY,
    tenant_id character varying(20),
    merchant_id character varying(20),
    access_code character varying(32),
    working_key character varying(40),
    environment character varying(24),
    gateway_url character varying(64),
    gateway_name character varying(20)
);
