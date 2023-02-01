package org.egov.bpa.calculator.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BPARepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public String[] getSanctionFeeAmount(String consumerCode) {
		String sql="select totalamount from billdetail_v1 where businessservice ='BPA.NC_SAN_FEE' and consumercode =?";
		return jdbcTemplate.queryForObject(sql, new Object[] { consumerCode }, String[].class);
//		return null;
	}
}
