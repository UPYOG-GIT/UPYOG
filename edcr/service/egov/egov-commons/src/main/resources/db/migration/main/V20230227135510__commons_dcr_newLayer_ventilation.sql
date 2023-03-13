insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) 
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_REGULAR_ROOM_VENTILATION','BLK_%s_FLR_%s_REGULAR_ROOM_%s_VENTILATION',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_REGULAR_ROOM_VENTILATION');

insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) 
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_WATER_CLOSET_VENTILATION','WATER_CLOSET_VENTILATION',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_WATER_CLOSET_VENTILATION');