ALTER TABLE edcr_rule_entry
ADD COLUMN IF NOT EXISTS min_value numeric(5,2),
ADD COLUMN IF NOT EXISTS max_value numeric(5,2);


CREATE TABLE IF NOT EXISTS state.egbpa_feature_name
(
	id SERIAL PRIMARY KEY,
	name character varying(128)
);

INSERT INTO state.egbpa_feature_name (name) SELECT 'Far' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Far');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Coverage' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Coverage');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Front Setback' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Front Setback');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Rear Setback' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Rear Setback');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Side Setback1' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Side Setback1');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Side Setback2' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Side Setback2');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Parking' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Parking');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Basement' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Basement');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Building Height' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Building Height');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Floor Height' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Floor Height');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Plinth Height' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Plinth Height');
INSERT INTO state.egbpa_feature_name (name) SELECT 'General Stair Tread Width' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'General Stair Tread Width');
INSERT INTO state.egbpa_feature_name (name) SELECT 'General Stair No of Risers' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'General Stair No of Risers');
INSERT INTO state.egbpa_feature_name (name) SELECT 'General Stair Width' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'General Stair Width');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Door Height' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Door Height');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Door Width' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Door Width');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Window Height' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Window Height');
INSERT INTO state.egbpa_feature_name (name) SELECT 'Window Width' WHERE NOT EXISTS (SELECT 1 FROM state.egbpa_feature_name WHERE name = 'Window Width');


