CREATE TABLE IF NOT EXISTS edcr_reg_application (
    id bigint NOT NULL,
    applicationnumber character varying(128) NOT NULL,
    applicationdate date NOT NULL,
    planinfoid bigint,
    createdby bigint NOT NULL,
    createddate timestamp without time zone NOT NULL,
    lastmodifieddate timestamp without time zone,
    lastmodifiedby bigint,
    version numeric DEFAULT 0,
    status character varying(30),
    occupancy character varying(256),
    applicantname character varying(256),
    architectinformation character varying(256),
    servicetype character varying(50),
    amenities character varying(200),
    projecttype character varying(512),
    regapplicationtype character varying(48),
    permitapplicationdate date,
    planpermitnumber character varying(30),
    buildinglicensee bigint,
    transactionnumber character varying(128),
    thirdpartyusercode character varying(48),
    thirdpartyusertenant character varying(48)
);


CREATE TABLE IF NOT EXISTS edcr_reg_application_detail (
    id bigint NOT NULL,
    application bigint NOT NULL,
    dxffileid bigint,
    createdby bigint NOT NULL,
    createddate timestamp without time zone NOT NULL,
    lastmodifieddate timestamp without time zone,
    lastmodifiedby bigint,
    version numeric NOT NULL,
    reportoutputid bigint,
    status character varying(128),
    dcrnumber character varying(128),
    plandetailfilestore bigint,
    scrutinizeddxffileid bigint,
    planinfoid bigint,
    comparisondcrnumber character varying(128),
    khatano character varying(15),
    mauza character varying(25),
    plotno character varying(15),
    plotarea character varying(10)
);


CREATE TABLE IF NOT EXISTS edcr_reg_pdf_detail (
    id bigint NOT NULL,
    applicationdetail bigint NOT NULL,
    layer character varying(512),
    convertedpdf bigint,
    failurereasons character varying(512),
    version numeric DEFAULT 0,
    createdby bigint,
    createddate timestamp without time zone,
    lastmodifiedby bigint,
    lastmodifieddate timestamp without time zone,
    standardviolations character varying(5000)
);

CREATE SEQUENCE IF NOT EXISTS SEQ_EDCR_REG_APPLICATION;

CREATE SEQUENCE IF NOT EXISTS SEQ_EDCR_REG_APPLICATION_DETAIL;

CREATE SEQUENCE IF NOT EXISTS SEQ_EDCR_REG_PDF_DETAIL;
