INSERT INTO state.egbpa_feature_name (name) SELECT 'Number of Floors' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Number of Floors');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Lift' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Lift');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Rain Water Harvesting' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Rain Water Harvesting');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Water Tank Capacity' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Water Tank Capacity');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Septic Tank Capacity' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Septic Tank Capacity');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Balcony' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Balcony');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Toilet Area' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Toilet Area');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Toilet Width' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Toilet Width');
INSERT INTO state.egbpa_feature_name (name) SELECT 'High Rise' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'High Rise');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Sanitation' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Sanitation');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Room Area' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Room Area');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Room Width' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Room Width');


INSERT INTO egbpa_occupancy(id, code, name, isactive, version, createdby, createddate, lastmodifiedby,lastmodifieddate, maxcoverage, minfar, maxfar, ordernumber, description)
VALUES (nextval('seq_egbpa_occupancy'), '', 'Common', 't', 0, 1, now(), 1,now(), 65, 3, 4, 1, 'Common');