

INSERT INTO egbpa_occupancy(id, code, name, isactive, version, createdby, createddate, lastmodifiedby,lastmodifieddate, maxcoverage, minfar, maxfar, ordernumber, description, colorcode)
VALUES (nextval('seq_egbpa_occupancy'), 'J', 'Government/Semi Goverment', 't', 0, 1, now(), 1,now(), 65, 3, 4, 1, 'Government/Semi Goverment',35);





INSERT INTO egbpa_sub_occupancy(id, code, name, ordernumber, isactive, createdby, createddate, lastmodifieddate, lastmodifiedby, version, maxcoverage, minfar, maxfar, occupancy, description, colorcode)  VALUES (nextval('seq_egbpa_sub_occupancy'), 'C-MPVT', 'Medical Private', 60, 't', 1, now(), now(), 1, 0,10, 15, 0, (select id from egbpa_occupancy where code='C'), 'Medical Private', 36);

INSERT INTO egbpa_sub_occupancy(id, code, name, ordernumber, isactive, createdby, createddate, lastmodifieddate, lastmodifiedby, version, maxcoverage, minfar, maxfar, occupancy, description, colorcode)  VALUES (nextval('seq_egbpa_sub_occupancy'), 'C-MPBLC', 'Medical Public', 61, 't', 1, now(), now(), 1, 0,10, 15, 0, (select id from egbpa_occupancy where code='C'), 'Medical Public', 37);

INSERT INTO egbpa_sub_occupancy(id, code, name, ordernumber, isactive, createdby, createddate, lastmodifieddate, lastmodifiedby, version, maxcoverage, minfar, maxfar, occupancy, description, colorcode)  VALUES (nextval('seq_egbpa_sub_occupancy'), 'D-MHCT', 'Meeting Hall/Cinema Theater', 62, 't', 1, now(), now(), 1, 0,10, 15, 0, (select id from egbpa_occupancy where code='D'), 'Meeting Hall/Cinema Theater', 38);