package org.egov.bpa.calculator.repository;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BPARepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public String[] getSanctionFeeAmount(String consumerCode) {
		String sql="select totalamount from egbs_billdetail_v1 where businessservice ='BPA.NC_SAN_FEE' and consumercode =?";
		return jdbcTemplate.queryForObject(sql, new Object[] { consumerCode }, String[].class);
//		return null;
	}
	
	public List<Map<String,Object>> getPaytyDate(String feety, String tenantid) {
		String sql = "select id, charges_type_name,zdaflg from paytype_master where ulb_tenantid=? and payment_type=? and defunt='N' and optflag='N'"; 
		return jdbcTemplate.queryForList(sql, new Object[] { tenantid, feety} );
		
	}
}
