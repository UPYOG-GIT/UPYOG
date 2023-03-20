package org.egov.bpa.calculator.repository;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class BPARepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public String[] getSanctionFeeAmount(String consumerCode) {
		String sql = "select totalamount from egbs_billdetail_v1 where businessservice ='BPA.NC_SAN_FEE' and consumercode =?";
		return jdbcTemplate.queryForObject(sql, new Object[] { consumerCode }, String[].class);
//		return null;
	}
	
	public Integer getBcategoryId(String bcat,String tenantid) {
		String sql = "select id from bcategory_master where ulb_tenantid =? and lower(description) =?";
		log.info("BPARepository.getBcategoryId Query : " + sql);
		return jdbcTemplate.queryForObject(sql, new Object[] {tenantid,bcat.toLowerCase()}, Integer.class);
	}
	
	public Integer getScategoryId(String scat,Integer bcat,String tenantid) {
		String sql = "select id from bscategory_master where ulb_tenantid =? and lower(description) =? and catid =? ";
		log.info("BPARepository.getScategoryId Query : " + sql);
		return jdbcTemplate.queryForObject(sql,new Object[] {tenantid,scat.toLowerCase(),bcat},Integer.class);
	}

	public List<Map<String, Object>> getPaytyData(String tenantid, String feetype, String occupancyType, Double plotares, String heightcat, String newrevise ) {
		
		log.info("tenantid--"+tenantid+"---feetype---"+feetype);
//		String sql = "select id, charges_type_name,zdaflg from paytype_master where ulb_tenantid=? and payment_type=? and defunt='N' and optflag='N'"; 
		String sql = "select id, charges_type_name,zdaflg from paytype_master where ulb_tenantid='" + tenantid
				+ "' and payment_type='" + feetype + "' and defunt='N' and optflag='N'";
		
		if (feetype.equals("Pre"))	
		{
			String hrnhveri ="";
			
			if(heightcat.equals("NH") && newrevise.equals("NEW"))hrnhveri="NN";
			if(heightcat.equals("NH") && newrevise.equals("REVISED"))hrnhveri="NR";
			if(heightcat.equals("HR") && newrevise.equals("NEW"))hrnhveri="HN";
			
			if((!heightcat.equals("")) && (!newrevise.equals(""))) {
				sql += "and hrnh ='"+hrnhveri+"'";
			}
			
			if((occupancyType.equals("Residential") && plotares<= 500)) {
				sql += "and id <> 1";
			}
						
		}
//		else if(feetype.equals("Post")) {
//			if((occupancyType.equals("Residential") && plotares > 500)) {
//				
//			}
//			else if((occupancyType.equals("Residential") && plotares<= 500)) {
//				
//			}
//			
//		}
	
		log.info("BPARepository.getPaytyDate Query : " + sql);
//		return jdbcTemplate.queryForList(sql, new Object[] { tenantid, feetype });
		return jdbcTemplate.queryForList(sql);

	}
	
	public Integer getCountOfPaytyrate(String tenantid,int id,Integer pCategory) {
		String sql = "select count(*)from pay_tp_rate_master "
				+ "where  ulb_tenantid=? "
				+ " and typeId=? "
				+ " and p_category=?";
		
		log.info("BPARepository.getCountOfPaytyrate: "+sql);
		return jdbcTemplate.queryForObject(sql, new Object[] { tenantid,id,pCategory }, Integer.class);
	}
	
	public Map<String,Object> getDetailOfPaytyrate(String tenantid,int id,Integer pCategory,int countPayTyrate, Integer bcategory,Integer subcate) {
		String sql = "select * from pay_tp_rate_master "
				+ "where  ulb_tenantid='"+tenantid+"'"
				+ " and typeId="+id
				+ " and p_category="+pCategory;
		
		if(countPayTyrate>1) {
			if(!bcategory.equals(null) && !bcategory.equals(0)) {
				sql +=" and b_category="+bcategory;
			}
			if(!subcate.equals(null) && !subcate.equals(0)) {
				sql +=" and s_category="+subcate;
			}
		}
		
		log.info("BPARepository.getDetailOfPaytyrate: "+sql);
//		return jdbcTemplate.queryForMap(sql, new Object[] { tenantid,id,pCategory });
		return jdbcTemplate.queryForMap(sql);
	}
	
	public List<Map<String, Object>> getDetailOfSlabMaster(Integer bcatefromrate, Integer subcatefromrate,String tenantid,int id, Integer pCategory,Double area ){
		String sql ="select * from slab_master where ulb_tenantid='"+tenantid+"'"
				+ " and paytype_id="+id+" and p_category="+pCategory
				+ " and from_val <="+area;
		if(!bcatefromrate.equals(null) && !bcatefromrate.equals(0)) {
			 sql +=" and  b_category ="+bcatefromrate;
		}
		if(!subcatefromrate.equals(null) && !subcatefromrate.equals(0)) {
			 sql +=" and  s_category ="+subcatefromrate;
		}
			sql+= " order by from_val";
			
			log.info("BPARepository.getDetailOfSlabMaster: "+sql);
//			return jdbcTemplate.queryForList(sql, new Object[] { tenantid,id,pCategory,area});
			return jdbcTemplate.queryForList(sql);
	}
	
	
}
