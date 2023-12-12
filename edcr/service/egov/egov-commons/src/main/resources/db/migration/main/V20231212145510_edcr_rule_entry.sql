CREATE TABLE IF NOT EXISTS edcr_rule_entry
(
    id SERIAL PRIMARY KEY,
    feature character varying,
    permissible_value character varying,
    by_law character varying,
    to_area numeric,
    from_area numeric,
    occupancy character varying,
    sub_occupancy character varying
  
)
