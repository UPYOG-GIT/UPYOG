
INSERT INTO egbpa_sub_occupancy(id, code, name, ordernumber, isactive, createdby, createddate, lastmodifieddate, lastmodifiedby, version, maxcoverage, minfar, maxfar, occupancy, description, colorcode)  VALUES (nextval('seq_egbpa_sub_occupancy'), 'G', 'Industrial', 65, 't', 1, now(), now(), 1, 0,10, 15, 0, (select id from egbpa_occupancy where code='G'), 'Industrial', 14);
INSERT INTO egbpa_sub_occupancy(id, code, name, ordernumber, isactive, createdby, createddate, lastmodifieddate, lastmodifiedby, version, maxcoverage, minfar, maxfar, occupancy, description, colorcode)  VALUES (nextval('seq_egbpa_sub_occupancy'), 'J', 'Government/Semi Goverment', 66, 't', 1, now(), now(), 1, 0,10, 15, 0, (select id from egbpa_occupancy where code='G'), 'Government/Semi Goverment', 35);