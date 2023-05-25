package org.egov.pg.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.egov.pg.models.Transaction;
import org.egov.pg.web.models.TransactionCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class TransactionRepository {

	private final JdbcTemplate jdbcTemplate;
	private static final TransactionRowMapper rowMapper = new TransactionRowMapper();

	@Autowired
	TransactionRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Transaction> fetchTransactions(TransactionCriteria transactionCriteria) {
		List<Object> params = new ArrayList<>();
		String query = TransactionQueryBuilder.getPaymentSearchQueryByCreatedTimeRange(transactionCriteria, params);
		log.info("query :" + query);
//		log.info("rowMapper: "+rowMapper );
		log.debug(query);
		return jdbcTemplate.query(query, params.toArray(), rowMapper);
	}

	public List<Transaction> fetchTransactionsByTimeRange(TransactionCriteria transactionCriteria, Long startTime,
			Long endTime) {
		List<Object> params = new ArrayList<>();
		String query = TransactionQueryBuilder.getPaymentSearchQueryByCreatedTimeRange(transactionCriteria, startTime,
				endTime, params);
		log.info("fetchTransactionsByTimeRange (query): " + query);
		return jdbcTemplate.query(query, params.toArray(), rowMapper);
	}

	public void insertResponse(String txnId, String encResp) {
		String sql = "insert into eg_pg_resp(txn_id, txn_response) values('" + txnId + "','" + encResp + "')";
		log.info("Reponse insert sql query: " + sql);
		jdbcTemplate.execute(sql);
	}

	public String fetchResponse(String txnId) {
		String sql = "select txn_response from eg_pg_resp where txn_id=?";
		return jdbcTemplate.queryForObject(sql, new Object[] { txnId }, String.class);
	}

	public String getTenantId(String txnId) {
		String sql = "select tenant_id from eg_pg_transactions where txn_id=?";
		return jdbcTemplate.queryForObject(sql, new Object[] { txnId }, String.class);
	}

	public Map<String, Object> getCcavenueDetails(String tenantId) {
		String sqlQuery = "SELECT merchant_id,access_code,working_key FROM eg_pg_ccavenue_details WHERE tenant_id='"
				+ tenantId + "'";
		log.info("sqlQuery: "+sqlQuery);
//		return jdbcTemplate.queryForList(sql, new Object[] { tenantId });
		return jdbcTemplate.queryForMap(sqlQuery);
	}
}
