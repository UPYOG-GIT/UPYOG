insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) 
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_LOWER_GROUND_FLOOR_PARKING','LOWER_GROUND_FLOOR_PARKING',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_LOWER_GROUND_FLOOR_PARKING');

insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) 
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_SEPTIC_TANK_CAPACITY_L','SEPTIC_TANK_CAPACITY_L',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_SEPTIC_TANK_CAPACITY_L');

insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) 
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_SEPTIC_TANK','SEPTIC_TANK',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_SEPTIC_TANK');