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

	public List<Map<String, Object>> getPaytyDate(String tenantid, String feetype ) {
		
		log.info("tenantid--"+tenantid+"---feetype---"+feetype);
//		String sql = "select id, charges_type_name,zdaflg from paytype_master where ulb_tenantid=? and payment_type=? and defunt='N' and optflag='N'"; 
		String sql = "select id, charges_type_name,zdaflg from paytype_master where ulb_tenantid='" + tenantid
				+ "' and payment_type='" + feetype + "' and defunt='N' and optflag='N'";
		log.info("BPARepository.getPaytyDate Query : " + sql);
//		return jdbcTemplate.queryForList(sql, new Object[] { tenantid, feetype });
		return jdbcTemplate.queryForList(sql);

	}
}
