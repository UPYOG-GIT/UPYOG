package org.egov.bpa.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.producer.Producer;
import org.egov.bpa.repository.querybuilder.BPAQueryBuilder;
import org.egov.bpa.repository.rowmapper.BPARowMapper;
import org.egov.bpa.web.model.BCategoryRequest;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.BPASearchCriteria;
import org.egov.bpa.web.model.BSCategoryRequest;
import org.egov.bpa.web.model.PayTpRateRequest;
import org.egov.bpa.web.model.PayTypeFeeDetailRequest;
import org.egov.bpa.web.model.PayTypeRequest;
import org.egov.bpa.web.model.ProposalTypeRequest;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class BPARepository {

	@Autowired
	private BPAConfiguration config;

	@Autowired
	private Producer producer;

	@Autowired
	private BPAQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private BPARowMapper rowMapper;

	/**
	 * Pushes the request on save topic through kafka
	 *
	 * @param bpaRequest The bpa create request
	 */
	public void save(BPARequest bpaRequest) {
		producer.push(config.getSaveTopic(), bpaRequest);
	}

	/**
	 * pushes the request on update or workflow update topic through kafaka based on
	 * th isStateUpdatable
	 * 
	 * @param bpaRequest
	 * @param isStateUpdatable
	 */
	public void update(BPARequest bpaRequest, boolean isStateUpdatable) {
		RequestInfo requestInfo = bpaRequest.getRequestInfo();

		BPA bpaForStatusUpdate = null;
		BPA bpaForUpdate = null;

		BPA bpa = bpaRequest.getBPA();

		if (isStateUpdatable) {
			bpaForUpdate = bpa;
		} else {
			bpaForStatusUpdate = bpa;
		}
		if (bpaForUpdate != null)
			producer.push(config.getUpdateTopic(), new BPARequest(requestInfo, bpaForUpdate));

		if (bpaForStatusUpdate != null)
			producer.push(config.getUpdateWorkflowTopic(), new BPARequest(requestInfo, bpaForStatusUpdate));

	}

	/**
	 * BPA search in database
	 *
	 * @param criteria The BPA Search criteria
	 * @return List of BPA from search
	 */
	public List<BPA> getBPAData(BPASearchCriteria criteria, List<String> edcrNos) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getBPASearchQuery(criteria, preparedStmtList, edcrNos, false);
		List<BPA> BPAData = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
		return BPAData;
	}

	/**
	 * BPA search count in database
	 *
	 * @param criteria The BPA Search criteria
	 * @return count of BPA from search
	 */
	public int getBPACount(BPASearchCriteria criteria, List<String> edcrNos) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getBPASearchQuery(criteria, preparedStmtList, edcrNos, true);
		int count = jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
		return count;
	}

	public List<BPA> getBPADataForPlainSearch(BPASearchCriteria criteria, List<String> edcrNos) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getBPASearchQueryForPlainSearch(criteria, preparedStmtList, edcrNos, false);
		List<BPA> BPAData = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
		return BPAData;
	}

	public List<Map<String, Object>> getPayTypeByTenantId(String tenantId) {

		String query = "select id,charges_type_name,defunt from paytype_master where ulb_tenantid=? order by id";
//        	String query="select charges_type_name from paytype_master where ulb_tenantid=?";
		return jdbcTemplate.queryForList(query, new Object[] { tenantId });
//    		return jdbcTemplate.queryForObject(query, new Object[] { tenantId }, Map.class);

	}

	public void createFeeDetail(List<PayTypeFeeDetailRequest> payTypeFeeDetailRequestList) {

		LocalDateTime date = LocalDateTime.now();
		String insertQuery = "insert into pre_post_fee_details(paytype_id,ulb_tenantid,bill_id,application_no,"
				+ "unit_id,pay_id,charges_type_name,amount,status_type,propvalue,value,status,createdby,payment_type,createddate)"
				+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,'" + date + "')";

		List<Object[]> parameters = new ArrayList<Object[]>();
		for (PayTypeFeeDetailRequest payTypeFeeDetailRequest : payTypeFeeDetailRequestList) {
			parameters.add(new Object[] { payTypeFeeDetailRequest.getPayTypeId(), payTypeFeeDetailRequest.getTenantId(),
					payTypeFeeDetailRequest.getBillId(), payTypeFeeDetailRequest.getApplicationNo(),
					payTypeFeeDetailRequest.getUnitId(), payTypeFeeDetailRequest.getPayId(),
					payTypeFeeDetailRequest.getChargesTypeName(), payTypeFeeDetailRequest.getAmount(),
					payTypeFeeDetailRequest.getStatusType(), payTypeFeeDetailRequest.getPropValue(),
					payTypeFeeDetailRequest.getValue(), payTypeFeeDetailRequest.getStatus(),
					payTypeFeeDetailRequest.getCreatedBy(), payTypeFeeDetailRequest.getPaymentType() });
		}
		int[] insertResult = jdbcTemplate.batchUpdate(insertQuery, parameters);

		log.info("BPARepository.createFeeDetail: " + insertResult + " data inserted into pre_post_fee_details table");
		/*
		 * for (PayTypeFeeDetailRequest payTypeFeeDetailRequest :
		 * payTypeFeeDetailRequestList) { jdbcTemplate.update(insertQuery,
		 * payTypeFeeDetailRequest.getPayTypeId(),
		 * payTypeFeeDetailRequest.getTenantId(), payTypeFeeDetailRequest.getBillId(),
		 * payTypeFeeDetailRequest.getApplicationNo(),
		 * payTypeFeeDetailRequest.getUnitId(), payTypeFeeDetailRequest.getPayId(),
		 * payTypeFeeDetailRequest.getChargesTypeName(),
		 * payTypeFeeDetailRequest.getAmount(), payTypeFeeDetailRequest.getStatusType(),
		 * payTypeFeeDetailRequest.getPropValue(), payTypeFeeDetailRequest.getValue(),
		 * payTypeFeeDetailRequest.getStatus(), payTypeFeeDetailRequest.getCreatedBy(),
		 * payTypeFeeDetailRequest.getPaymentType()); }
		 */
	}

	public int createPayType(PayTypeRequest payTypeRequest) {

		LocalDateTime date = LocalDateTime.now();

		String insertQuery = "insert into paytype_master(ulb_tenantid,charges_type_name,payment_type,"
				+ "defunt,createdby,createddate) values (?,?,?,?,?,'" + date + "')";

//		Object parameters = new Object();
//		parameters=new Object[] { payTypeRequest.getTenantId(), payTypeRequest.getChargesTypeName(),
//				payTypeRequest.getPaymentType(), payTypeRequest.getDefunt(), payTypeRequest.getCreatedBy() };
		List<Object[]> parameters = new ArrayList<Object[]>();
		parameters.add(new Object[] { payTypeRequest.getTenantId(), payTypeRequest.getChargesTypeName(),
				payTypeRequest.getPaymentType(), payTypeRequest.getDefunt(), payTypeRequest.getCreatedBy() });

//		int[] insertResult = jdbcTemplate.batchUpdate(insertQuery, parameters);
		int insertResult = jdbcTemplate.update(insertQuery, parameters);

		log.info("BPARepository.createPayType: " + insertResult + " data inserted into paytype_master table");
		return insertResult;
	}

	public int createProposalType(ProposalTypeRequest proposalTypeRequest) {

		LocalDateTime date = LocalDateTime.now();

		String insertQuery = "insert into proposal_type_master(ULB_TENANTID,DESCRIPTION,DEFUNT,CREATEDBY,CREATEDDATE) "
				+ "values (?,?,?,?,'" + date + "')";

		Object parameters = new Object();
		parameters = new Object[] { proposalTypeRequest.getTenantId(), proposalTypeRequest.getDesc(),
				proposalTypeRequest.getDefunt(), proposalTypeRequest.getCreatedBy() };

		int insertResult = jdbcTemplate.update(insertQuery, parameters);

		log.info("BPARepository.createProposalType: " + insertResult + " data inserted into paytype_master table");
		return insertResult;
	}

	// fetch data from proposal_type_master table
	public List<Map<String, Object>> getProposalTypeByTenantId(String tenantId) {

		String query = "select ID,DESCRIPTION,DEFUNT from proposal_type_master where ULB_TENANTID=?";
		return jdbcTemplate.queryForList(query, new Object[] { tenantId });
	}

	public int createBCategory(BCategoryRequest bCategoryRequest) {

		LocalDateTime date = LocalDateTime.now();

		String insertQuery = "insert into bcategory_master(ULB_TENANTID,DESCRIPTION,DEFUNT,CREATEDBY,CREATEDDATE) "
				+ "values (?,?,?,?,'" + date + "')";

		Object parameters = new Object();
		parameters = new Object[] { bCategoryRequest.getTenantId(), bCategoryRequest.getDesc(),
				bCategoryRequest.getDefunt(), bCategoryRequest.getCreatedBy() };

		int insertResult = jdbcTemplate.update(insertQuery, parameters);

		log.info("BPARepository.createBCategory: " + insertResult + " data inserted into paytype_master table");
		return insertResult;
	}

	// fetch data from bcategory_master table
	public List<Map<String, Object>> getBCategoryByTenantId(String tenantId) {
		String query = "select ID,DESCRIPTION,DEFUNT from bcategory_master where ULB_TENANTID=?";
		return jdbcTemplate.queryForList(query, new Object[] { tenantId });
	}

	public int createBSCategory(BSCategoryRequest bsCategoryRequest) {

		LocalDateTime date = LocalDateTime.now();

		String insertQuery = "insert into bcategory_master(ULB_TENANTID,DESCRIPTION,DEFUNT,CATID,CREATEDBY,CREATEDDATE) "
				+ "values (?,?,?,?,?,'" + date + "')";

		Object parameters = new Object();
		parameters = new Object[] { bsCategoryRequest.getTenantId(), bsCategoryRequest.getDesc(),
				bsCategoryRequest.getDefunt(), bsCategoryRequest.getCatid(), bsCategoryRequest.getCreatedBy() };

		int insertResult = jdbcTemplate.update(insertQuery, parameters);

		log.info("BPARepository.createBSCategory: " + insertResult + " data inserted into paytype_master table");
		return insertResult;
	}

	// fetch data from bscategory_master table
	public List<Map<String, Object>> getBSCategoryByTenantId(String tenantId) {
		String query = "select ID,DESCRIPTION,DEFUNT from bscategory_master where ULB_TENANTID=?";
		return jdbcTemplate.queryForList(query, new Object[] { tenantId });
	}

	public int createPayTpRate(PayTpRateRequest payTpRateRequest) {

		String query = "SELECT COUNT(*) FROM pay_tp_rate_master WHERE ULB_TENANTID=? AND TYPEID=?";
		int count = jdbcTemplate.queryForObject(query,
				new Object[] { payTpRateRequest.getTenantId(), payTpRateRequest.getTypeId() }, Integer.class);
		log.info("count : " + count);
		LocalDateTime date = LocalDateTime.now();

		String insertQuery = "insert into pay_tp_rate_master(ULB_TENANTID,UNITID,TYPEID,SRNO,CALCON,"
				+ "CALCACT,P_CATEGORY,B_CATEGORY,S_CATEGORY,RATE_RES,RATE_COMM,RATE_IND,"
				+ "PERVAL,CREATEDBY,CREATEDDATE) " + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,'" + date + "')";

		Object parameters = new Object();
		parameters = new Object[] { payTpRateRequest.getTenantId(), payTpRateRequest.getUnitId(),
				payTpRateRequest.getTypeId(), count + 1, payTpRateRequest.getCalCon(), payTpRateRequest.getCalCact(),
				payTpRateRequest.getPCategory(), payTpRateRequest.getBCategory(), payTpRateRequest.getSCategory(),
				payTpRateRequest.getRateRes(), payTpRateRequest.getRateComm(), payTpRateRequest.getRateInd(),
				payTpRateRequest.getPerVal(), payTpRateRequest.getCreatedBy() };

		int insertResult = jdbcTemplate.update(insertQuery, parameters);

		log.info("BPARepository.createPayTpRate: " + insertResult + " data inserted into paytype_master table");
		return insertResult;
	}

	public List<Map<String, Object>> getPayTpRateByTenantIdAndTypeId(String tenantId, int typeId) {
		String query = "select ID,UNITID,SRNO,CALCON,CALCACT,P_CATEGORY,B_CATEGORY,S_CATEGORY,"
				+ "RATE_RES,RATE_COMM,RATE_IND,PERVAL from pay_tp_rate_master "
				+ "where ULB_TENANTID=? AND TYPEID=?";
		return jdbcTemplate.queryForList(query, new Object[] { tenantId, typeId });
	}
}
