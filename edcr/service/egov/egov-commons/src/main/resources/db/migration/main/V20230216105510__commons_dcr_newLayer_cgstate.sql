insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) 
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_PODIUM_PARKING','PODIUM_PARKING',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_PODIUM_PARKING');

insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) 
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_DOOR','BLK_%s_FLR_%s_DOOR_%s',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_DOOR');

insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) 
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_WINDOW','BLK_%s_FLR_%s_WINDOW_%s',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_WINDOW');

insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) 
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_DUCT','BLK_%s_DUCT',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_DUCT');