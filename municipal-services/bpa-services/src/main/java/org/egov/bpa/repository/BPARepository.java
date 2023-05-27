package org.egov.bpa.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.egov.bpa.web.model.SlabMasterRequest;
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

	public int createFeeDetail(PayTypeFeeDetailRequest payTypeFeeDetailRequest) {

		String query = "SELECT COUNT(*) FROM fee_details WHERE feetype=? and ulb_tenantid=?";
		int count = jdbcTemplate.queryForObject(query,
				new Object[] { payTypeFeeDetailRequest.getFeeType(), payTypeFeeDetailRequest.getTenantId() },
				Integer.class);
		log.info("createPayTpRate count : " + count);
		count = count + 1;

		LocalDateTime date = LocalDateTime.now();
		String insertQuery = "insert into fee_details(paytype_id,feetype,srno,ulb_tenantid,bill_id,application_no,"
				+ "unit,charges_type_name,prop_plot_area,amount,rate,type,createdby,createddate)"
				+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,'" + date + "')";
//		String insertQuery = "insert into pre_post_fee_details(paytype_id,ulb_tenantid,bill_id,application_no,"
//				+ "unit_id,pay_id,charges_type_name,amount,status_type,propvalue,value,status,createdby,payment_type,tip_rate,createddate)"
//				+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'" + date + "')";

		List<Object[]> parameters = new ArrayList<Object[]>();
//		for (PayTypeFeeDetailRequestWrapper payTypeFeeDetailRequestWrapper : payTypeFeeDetailRequestWrapperList) {
//			PayTypeFeeDetailRequest payTypeFeeDetailRequest = payTypeFeeDetailRequestWrapper
//					.getPayTypeFeeDetailRequest();
		parameters.add(new Object[] { payTypeFeeDetailRequest.getPayTypeId(), payTypeFeeDetailRequest.getFeeType(),
				count, payTypeFeeDetailRequest.getTenantId(), payTypeFeeDetailRequest.getBillId(),
				payTypeFeeDetailRequest.getApplicationNo(), payTypeFeeDetailRequest.getUnit(),
				payTypeFeeDetailRequest.getChargesTypeName(), payTypeFeeDetailRequest.getPropPlotArea(),
				payTypeFeeDetailRequest.getAmount(), payTypeFeeDetailRequest.getRate(),
				payTypeFeeDetailRequest.getType(), payTypeFeeDetailRequest.getCreatedBy() });
//		}
//		int[] insertResult = jdbcTemplate.batchUpdate(insertQuery, parameters);
		int insertResult = jdbcTemplate.update(insertQuery, payTypeFeeDetailRequest.getPayTypeId(),
				payTypeFeeDetailRequest.getFeeType(), count, payTypeFeeDetailRequest.getTenantId(),
				payTypeFeeDetailRequest.getBillId(), payTypeFeeDetailRequest.getApplicationNo(),
				payTypeFeeDetailRequest.getUnit(), payTypeFeeDetailRequest.getChargesTypeName(),
				payTypeFeeDetailRequest.getPropPlotArea(), payTypeFeeDetailRequest.getAmount(),
				payTypeFeeDetailRequest.getRate(), payTypeFeeDetailRequest.getType(),
				payTypeFeeDetailRequest.getCreatedBy());

		log.info("BPARepository.createFeeDetail: " + insertResult + " data inserted into pre_post_fee_details table");
		String totalAmountQuery = "SELECT SUM(amount) as amount from fee_details WHERE application_no='"
				+ payTypeFeeDetailRequest.getApplicationNo() + "' and feetype='" + payTypeFeeDetailRequest.getFeeType()
				+ "'";
		Map<String, Object> resultMap = jdbcTemplate.queryForMap(totalAmountQuery);
		updateBillDetailAmount(payTypeFeeDetailRequest.getApplicationNo(),
				Double.valueOf(resultMap.get("amount").toString()));
		return insertResult;
	}

	public int updateFeeDetails(PayTypeFeeDetailRequest payTypeFeeDetailRequest) {
		LocalDateTime date = LocalDateTime.now();

		String updateQuery = "update fee_details set amount='" + payTypeFeeDetailRequest.getAmount() + "',updatedby='"
				+ payTypeFeeDetailRequest.getUpdatedBy() + "',updateddate='" + date + "' where application_no ='"
				+ payTypeFeeDetailRequest.getApplicationNo() + "' and ulb_tenantid ='"
				+ payTypeFeeDetailRequest.getTenantId() + "'" + " and id=" + payTypeFeeDetailRequest.getId();
		int updateResult = jdbcTemplate.update(updateQuery);
		log.info("BPARepository.updateFeeDetails: " + updateResult + " data updated into paytype_master table");
		return updateResult;
	}

	public List<Map<String, Object>> getFeeDetails(String applicationNo) {
		String query = "select id,ulb_tenantid,paytype_id,feetype,srno,bill_id,unit,charges_type_name,prop_plot_area,amount,rate,type,verify from fee_details where application_no=? and feetype='Post'";
		return jdbcTemplate.queryForList(query, new Object[] { applicationNo });

	}

	public int deleteFeeDetailsById(List<Integer> ids, String applicationNo, String feeType) {
//		String deleteQuery = "DELETE FROM fee_details WHERE id IN (:msgNos)";
		String id = ids.toString().replace("[", "").replace("]", "");
		String deleteQuery = "DELETE FROM fee_details WHERE id IN (" + id + ")";
		log.info("deleteQuery: " + deleteQuery);
		int deleteResult = jdbcTemplate.update(deleteQuery);
		log.info("BPARepository.deletePayTpRateById: " + deleteResult
				+ " data deleted from pay_tp_rate_master table of id(s) : " + ids.toString());
		String totalAmountQuery = "SELECT SUM(amount) as amount from fee_details WHERE application_no='" + applicationNo
				+ "' and feetype='" + feeType + "'";
		Map<String, Object> resultMap = jdbcTemplate.queryForMap(totalAmountQuery);
		updateBillDetailAmount(applicationNo, Double.valueOf(resultMap.get("amount").toString()));
		return deleteResult;
	}

	public int verifyFeeDetailsByApplicationNo(String applicationNo, String isVerified, String verifiedBy,
			String feeType) {
		LocalDateTime date = LocalDateTime.now();
		String updateQuery = "UPDATE fee_details SET verify='" + isVerified + "',verifiedby='" + verifiedBy
				+ "',verifieddate='" + date + "' WHERE application_no =" + "'" + applicationNo + "' and feetype='"
				+ feeType + "'";
		int updateResult = jdbcTemplate.update(updateQuery);
		log.info("BPARepository.verifyFeeDetailsByApplicationNo: " + updateResult
				+ " data updated into paytype_master table");
		return updateResult;
	}

	public void updateBillDetailAmount(String applicationNo, double totalAmount) {
		String updateQuery = "UPDATE egbs_billdetail_v1 SET totalamount='" + totalAmount + "' WHERE consumercode ='"
				+ applicationNo + "' AND businessservice='BPA.NC_SAN_FEE'";
		int updateResult = jdbcTemplate.update(updateQuery);
		log.info("BPARepository.updateFeeDetails: " + updateResult + " data updated into paytype_master table");
//		return updateResult;
	}

	public int createPayType(PayTypeRequest payTypeRequest) {

		LocalDateTime date = LocalDateTime.now();

//		String insertQuery = "insert into paytype_master(ulb_tenantid,charges_type_name,payment_type,"
//				+ "defunt,createdby,createddate) values (?,?,?,?,?,'" + date + "')";
		String insertQuery = "insert into paytype_master(ulb_tenantid,charges_type_name,payment_type,"
				+ "defunt,optflag,hrnh,depflag,fdrflg,zdaflg,createdby,createddate) values ('"
				+ payTypeRequest.getTenantId() + "','" + payTypeRequest.getChargesTypeName() + "','"
				+ payTypeRequest.getPaymentType() + "','" + payTypeRequest.getDefunt() + "','"
				+ payTypeRequest.getOptFlag() + "','" + payTypeRequest.getHrnh() + "','" + payTypeRequest.getDepFlag()
				+ "','" + payTypeRequest.getFdrFlg() + "','" + payTypeRequest.getZdaFlg() + "','"
				+ payTypeRequest.getCreatedBy() + "','" + date + "')";

//		Object parameters = new Object();
//		parameters=new Object[] { payTypeRequest.getTenantId(), payTypeRequest.getChargesTypeName(),
//				payTypeRequest.getPaymentType(), payTypeRequest.getDefunt(), payTypeRequest.getCreatedBy() };
//		List<Object[]> parameters = new ArrayList<Object[]>();
//		parameters.add(new Object[] { payTypeRequest.getTenantId(), payTypeRequest.getChargesTypeName(),
//				payTypeRequest.getPaymentType(), payTypeRequest.getDefunt(), payTypeRequest.getCreatedBy() });

//		int[] insertResult = jdbcTemplate.batchUpdate(insertQuery, parameters);
//		int insertResult = jdbcTemplate.update(insertQuery, parameters);
//		int insertResult = jdbcTemplate.update(insertQuery, payTypeRequest.getTenantId(),payTypeRequest.getChargesTypeName(),
//				payTypeRequest.getPaymentType(), payTypeRequest.getDefunt(), payTypeRequest.getCreatedBy());
		int insertResult = jdbcTemplate.update(insertQuery);
		log.info("BPARepository.createPayType: " + insertResult + " data inserted into paytype_master table");
//		return Array.getInt(insertResult, 0);
		return insertResult;
	}

	public List<Map<String, Object>> getPayTypeByTenantId(String tenantId) {

		String query = "select id,charges_type_name,payment_type,defunt,optflag,hrnh,depflag,fdrflg,zdaflg from paytype_master where ulb_tenantid=? order by id";
//        	String query="select charges_type_name from paytype_master where ulb_tenantid=?";
		return jdbcTemplate.queryForList(query, new Object[] { tenantId });
//    		return jdbcTemplate.queryForObject(query, new Object[] { tenantId }, Map.class);

	}

	public int updatePayType(PayTypeRequest payTypeRequest) {
		LocalDateTime date = LocalDateTime.now();

		String updateQuery = "update paytype_master set charges_type_name='" + payTypeRequest.getChargesTypeName()
				+ "',payment_type='" + payTypeRequest.getPaymentType() + "',defunt='" + payTypeRequest.getDefunt()
				+ "',optflag='" + payTypeRequest.getOptFlag() + "',hrnh='" + payTypeRequest.getHrnh() + "',depflag='"
				+ payTypeRequest.getDepFlag() + "',fdrflg='" + payTypeRequest.getFdrFlg() + "',zdaflg='"
				+ payTypeRequest.getZdaFlg() + "',updatedby='" + payTypeRequest.getUpdatedBy() + "',updateddate='"
				+ date + "' where id=" + payTypeRequest.getId();
		int updateResult = jdbcTemplate.update(updateQuery);
		log.info("BPARepository.updatePayType: " + updateResult + " data updated into paytype_master table");
		return updateResult;
	}

	public int createProposalType(ProposalTypeRequest proposalTypeRequest) {

		String query = "SELECT COUNT(*) FROM proposal_type_master WHERE ulb_tenantid=?";
		int count = jdbcTemplate.queryForObject(query, new Object[] { proposalTypeRequest.getTenantId() },
				Integer.class);
		log.info("createPayTpRate count : " + count);
		count = count + 1;

		LocalDateTime date = LocalDateTime.now();

		String insertQuery = "insert into proposal_type_master(srno,ulb_tenantid,description,defunt,createdby,createddate) "
				+ "values (" + count + ",'" + proposalTypeRequest.getTenantId() + "','" + proposalTypeRequest.getDesc()
				+ "','" + proposalTypeRequest.getDefunt() + "','" + proposalTypeRequest.getCreatedBy() + "','" + date
				+ "')";

//		Object parameters = new Object();
//		parameters = new Object[] { proposalTypeRequest.getTenantId(), proposalTypeRequest.getDesc(),
//				proposalTypeRequest.getDefunt(), proposalTypeRequest.getCreatedBy() };
//
//		int insertResult = jdbcTemplate.update(insertQuery, parameters);
		int insertResult = jdbcTemplate.update(insertQuery);

		log.info(
				"BPARepository.createProposalType: " + insertResult + " data inserted into proposal_type_master table");
		return insertResult;
	}

	// fetch data from proposal_type_master table
	public List<Map<String, Object>> getProposalTypeByTenantId(String tenantId) {

		String query = "select id,srno,description,defunt from proposal_type_master where ulb_tenantid=?";
		return jdbcTemplate.queryForList(query, new Object[] { tenantId });
	}

	public int updateProposalType(ProposalTypeRequest proposalTypeRequest) {
		LocalDateTime date = LocalDateTime.now();

		String updateQuery = "update proposal_type_master set description='" + proposalTypeRequest.getDesc()
				+ "',defunt='" + proposalTypeRequest.getDefunt() + "',updatedby='" + proposalTypeRequest.getUpdatedBy()
				+ "',updateddate='" + date + "' where id=" + proposalTypeRequest.getId();
		int updateResult = jdbcTemplate.update(updateQuery);
		log.info("BPARepository.updateBSCategory: " + updateResult + " data updated into paytype_master table");
		return updateResult;
	}

	public int createBCategory(BCategoryRequest bCategoryRequest) {

		LocalDateTime date = LocalDateTime.now();

		String insertQuery = "insert into bcategory_master(ulb_tenantid,description,defunt,createdby,createddate) "
				+ "values ('" + bCategoryRequest.getTenantId() + "','" + bCategoryRequest.getDesc() + "','"
				+ bCategoryRequest.getDefunt() + "','" + bCategoryRequest.getCreatedBy() + "','" + date + "')";

//		Object parameters = new Object();
//		parameters = new Object[] { bCategoryRequest.getTenantId(), bCategoryRequest.getDesc(),
//				bCategoryRequest.getDefunt(), bCategoryRequest.getCreatedBy() };
//
//		int insertResult = jdbcTemplate.update(insertQuery, parameters);
		int insertResult = jdbcTemplate.update(insertQuery);

		log.info("BPARepository.createBCategory: " + insertResult + " data inserted into bcategory_master table");
		return insertResult;
	}

	// fetch data from bcategory_master table
	public List<Map<String, Object>> getBCategoryByTenantId(String tenantId) {
		String query = "select id,description,defunt from bcategory_master where ulb_tenantid=?";
		return jdbcTemplate.queryForList(query, new Object[] { tenantId });
	}

	public int updateBCategory(BCategoryRequest bCategoryRequest) {
		LocalDateTime date = LocalDateTime.now();

		String updateQuery = "update bcategory_master set description='" + bCategoryRequest.getDesc() + "',defunt='"
				+ bCategoryRequest.getDefunt() + "',updatedby='" + bCategoryRequest.getUpdatedBy() + "',updateddate='"
				+ date + "' where id=" + bCategoryRequest.getId();
		int updateResult = jdbcTemplate.update(updateQuery);
		log.info("BPARepository.updateBCategory: " + updateResult + " data updated into paytype_master table");
		return updateResult;
	}

	public int createBSCategory(BSCategoryRequest bsCategoryRequest) {

		LocalDateTime date = LocalDateTime.now();

		String insertQuery = "insert into bscategory_master(ulb_tenantid,description,defunt,catid,createdby,createddate) "
				+ "values ('" + bsCategoryRequest.getTenantId() + "','" + bsCategoryRequest.getDesc() + "','"
				+ bsCategoryRequest.getDefunt() + "','" + bsCategoryRequest.getCatid() + "','"
				+ bsCategoryRequest.getCreatedBy() + "','" + date + "')";
//
//		Object parameters = new Object();
//		parameters = new Object[] { bsCategoryRequest.getTenantId(), bsCategoryRequest.getDesc(),
//				bsCategoryRequest.getDefunt(), bsCategoryRequest.getCatid(), bsCategoryRequest.getCreatedBy() };
//
//		int insertResult = jdbcTemplate.update(insertQuery, parameters);
		int insertResult = jdbcTemplate.update(insertQuery);

		log.info("BPARepository.createBSCategory: " + insertResult + " data inserted into bscategory_master table");
		return insertResult;
	}

	// fetch data from bscategory_master table
	public List<Map<String, Object>> getBSCategoryByTenantId(String tenantId, int catId) {
		String query = "select id,description,defunt,catid from bscategory_master where ulb_tenantid=? AND catid=?";
		return jdbcTemplate.queryForList(query, new Object[] { tenantId, catId });
	}

	public int updateBSCategory(BSCategoryRequest bsCategoryRequest) {
		LocalDateTime date = LocalDateTime.now();

		String updateQuery = "update bscategory_master set description='" + bsCategoryRequest.getDesc() + "',defunt='"
				+ bsCategoryRequest.getDefunt() + "',,catid='" + bsCategoryRequest.getCatid() + "',updatedby='"
				+ bsCategoryRequest.getUpdatedBy() + "',updateddate='" + date + "' where id="
				+ bsCategoryRequest.getId();
		int updateResult = jdbcTemplate.update(updateQuery);
		log.info("BPARepository.updateBSCategory: " + updateResult + " data updated into paytype_master table");
		return updateResult;
	}

	public int createPayTpRate(PayTpRateRequest payTpRateRequest) {

		String query = "SELECT COUNT(*) FROM pay_tp_rate_master WHERE ulb_tenantid=? AND typeid=?";
		int count = jdbcTemplate.queryForObject(query,
				new Object[] { payTpRateRequest.getTenantId(), payTpRateRequest.getTypeId() }, Integer.class);
		log.info("createPayTpRate count : " + count);
		LocalDateTime date = LocalDateTime.now();
		count = count + 1;

		String insertQuery = "insert into pay_tp_rate_master(ulb_tenantid,unitid,typeid,srno,calcon,"
				+ "calcact,p_category,b_category,s_category,rate_res,rate_comm,rate_ind,"
				+ "perval,createdby,createddate) " + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,'" + date + "')";
//		String insertQuery = "insert into pay_tp_rate_master(ulb_tenantid,unitid,typeid,srno,calcon,"
//				+ "calcact,p_category,b_category,s_category,rate_res,rate_comm,rate_ind,"
//				+ "perval,createdby,createddate) " + "values ('" + payTpRateRequest.getTenantId() + "','"
//				+ payTpRateRequest.getUnitId() + "'," + payTpRateRequest.getTypeId() + "," + count + ",'"
//				+ payTpRateRequest.getCalCon() + "','" + payTpRateRequest.getCalCact() + "','"
//				+ payTpRateRequest.getPCategory() + "','" + payTpRateRequest.getBCategory() + "','"
//				+ payTpRateRequest.getSCategory() + "','" + payTpRateRequest.getRateRes() + "','"
//				+ payTpRateRequest.getRateComm() + "','" + payTpRateRequest.getRateInd() + "','"
//				+ payTpRateRequest.getPerVal() + "','" + payTpRateRequest.getCreatedBy() + "','" + date + "')";

//		Object parameters = new Object();
//		parameters = new Object[] { payTpRateRequest.getTenantId(), payTpRateRequest.getUnitId(),
//				payTpRateRequest.getTypeId(), count + 1, payTpRateRequest.getCalCon(), payTpRateRequest.getCalCact(),
//				payTpRateRequest.getPCategory(), payTpRateRequest.getBCategory(), payTpRateRequest.getSCategory(),
//				payTpRateRequest.getRateRes(), payTpRateRequest.getRateComm(), payTpRateRequest.getRateInd(),
//				payTpRateRequest.getPerVal(), payTpRateRequest.getCreatedBy() };
//
		int insertResult = jdbcTemplate.update(insertQuery, payTpRateRequest.getTenantId(),
				payTpRateRequest.getUnitId(), payTpRateRequest.getTypeId(), count, payTpRateRequest.getCalCon(),
				payTpRateRequest.getCalCact(), payTpRateRequest.getPCategory(), payTpRateRequest.getBCategory(),
				payTpRateRequest.getSCategory(), payTpRateRequest.getRateRes(), payTpRateRequest.getRateComm(),
				payTpRateRequest.getRateInd(), payTpRateRequest.getPerVal(), payTpRateRequest.getCreatedBy());
//		int insertResult = jdbcTemplate.update(insertQuery);

		log.info("BPARepository.createPayTpRate: " + insertResult + " data inserted into pay_tp_rate_master table");
		return insertResult;
	}

	public List<Map<String, Object>> getPayTpRateByTenantIdAndTypeId(String tenantId, int typeId) {
		String query = "select trm.id,trm.unitid,trm.srno,trm.calcon,trm.calcact,"
				+ "ptm.description as p_category, bm.description as b_category, bsm.description as s_category, "
				+ "trm.rate_res,trm.rate_comm,trm.rate_ind,trm.perval from pay_tp_rate_master trm "
				+ "LEFT JOIN proposal_type_master as ptm on ptm.id = trm.p_category "
				+ "LEFT JOIN bcategory_master as bm on bm.id = trm.b_category "
				+ "LEFT JOIN bscategory_master as bsm on bsm.id = trm.s_category "
				+ "where trm.ulb_tenantid=? AND trm.typeid=?";
//		String query = "select id,unitid,srno,calcon,calcact,p_category,b_category,s_category,"
//				+ "rate_res,rate_comm,rate_ind,perval from pay_tp_rate_master " + "where ulb_tenantid=? AND typeid=?";
		return jdbcTemplate.queryForList(query, new Object[] { tenantId, typeId });
	}

	// delete from pay_tp_rate_master table
	public int deletePayTpRateById(List<Integer> ids) {
		String id = ids.toString().replace("[", "").replace("]", "");
		String deleteQuery = "DELETE FROM pay_tp_rate_master WHERE id IN (" + id + ")";
		int deleteResult = jdbcTemplate.update(deleteQuery);
		log.info("BPARepository.deletePayTpRateById: " + deleteResult
				+ " data deleted from pay_tp_rate_master table of id(s) : " + id);
		return deleteResult;
	}

	// insert data into slab_master table
	public int createSlabMaster(SlabMasterRequest slabMasterRequest) {

		String query = "SELECT COUNT(*) FROM slab_master WHERE ulb_tenantid=? AND paytype_id=?";
		int count = jdbcTemplate.queryForObject(query,
				new Object[] { slabMasterRequest.getTenantId(), slabMasterRequest.getPayTypeId() }, Integer.class);
		log.info("createSlabMaster count : " + count);
		LocalDateTime date = LocalDateTime.now();
		count = count + 1;

		String insertQuery = "insert into slab_master(ulb_tenantid,paytype_id,srno,from_val,"
				+ "to_val,p_category,b_category,s_category,rate_res,rate_comm,rate_ind,"
				+ "operation,multp_val,max_limit,createdby,createddate) " + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'"
				+ date + "')";
//		String insertQuery = "insert into slab_master(ulb_tenantid,paytype_id,srno,from_val,"
//				+ "to_val,p_category,b_category,s_category,rate_res,rate_comm,rate_ind,"
//				+ "operation,multp_val,max_limit,createdby,createddate) " + "values ('"
//				+ slabMasterRequest.getTenantId() + "'," + slabMasterRequest.getPayTypeId() + "," + count + ",'"
//				+ slabMasterRequest.getFromVal() + "','" + slabMasterRequest.getToVal() + "','"
//				+ slabMasterRequest.getPCategory() + "','" + slabMasterRequest.getBCategory() + "','"
//				+ slabMasterRequest.getSCategory() + "','" + slabMasterRequest.getRateRes() + "','"
//				+ slabMasterRequest.getRateComm() + "','" + slabMasterRequest.getRateInd() + "','"
//				+ slabMasterRequest.getOperation() + "','" + slabMasterRequest.getMultpVal() + "','"
//				+ slabMasterRequest.getMaxLimit() + "','" + slabMasterRequest.getCreatedBy() + "','" + date + "')";
		int insertResult = jdbcTemplate.update(insertQuery, slabMasterRequest.getTenantId(),
				slabMasterRequest.getPayTypeId(), count, slabMasterRequest.getFromVal(), slabMasterRequest.getToVal(),
				slabMasterRequest.getPCategory(), slabMasterRequest.getBCategory(), slabMasterRequest.getSCategory(),
				slabMasterRequest.getRateRes(), slabMasterRequest.getRateComm(), slabMasterRequest.getRateInd(),
				slabMasterRequest.getOperation(), slabMasterRequest.getMultpVal(), slabMasterRequest.getMaxLimit(),
				slabMasterRequest.getCreatedBy());
//		int insertResult = jdbcTemplate.update(insertQuery);

		log.info("BPARepository.createPayTpRate: " + insertResult + " data inserted into slab_master table");
		return insertResult;
	}

	public List<Map<String, Object>> getSlabMasterByTenantIdAndTypeId(String tenantId, int payTypeId) {
		String query = "select sm.id,sm.srno,sm.from_val,sm.to_val,ptm.description as p_category, "
				+ "bm.description as b_category, bsm.description as s_category, sm.rate_res,"
				+ "sm.rate_comm,sm.rate_ind,sm.operation,sm.multp_val,sm.max_limit " + "from slab_master sm "
				+ "LEFT JOIN proposal_type_master as ptm on ptm.id = sm.p_category "
				+ "LEFT JOIN bcategory_master as bm on bm.id = sm.b_category "
				+ "LEFT JOIN bscategory_master as bsm on bsm.id = sm.s_category "
				+ "Where sm.ulb_tenantid=? AND sm.paytype_id=?";
//		String query = "select id,srno,from_val,to_val,p_category,b_category,s_category,"
//				+ "rate_res,rate_comm,rate_ind,operation,multp_val,max_limit from slab_master "
//				+ "where ulb_tenantid=? AND paytype_id=?";
		return jdbcTemplate.queryForList(query, new Object[] { tenantId, payTypeId });
	}

	public int deleteSlabMasterById(List<Integer> ids) {
		String id = ids.toString().replace("[", "").replace("]", "");
		String deleteQuery = "DELETE FROM slab_master WHERE id IN (" + id + ")";
		int deleteResult = jdbcTemplate.update(deleteQuery);
		log.info("BPARepository.deleteSlabMasterById: " + deleteResult + " data deleted from slab_master table of id(s) : "
				+ id);
		return deleteResult;
	}
	
	public List<Map<String, Object>> getDataCountsForDashboard() {
	    String query = "SELECT " +
	            "COUNT(CASE WHEN bp.status = 'APPROVED' THEN 1 END) AS Approved, " +
	            "COUNT(CASE WHEN bp.status = 'REJECTED' THEN 1 END) AS Rejected, " +
	            "COUNT(CASE " + "WHEN bp.status IN ('DOC_VERIFICATION_INPROGRESS_BY_ENGINEER',        'DOC_VERIFICATION_INPROGRESS_BY_BUILDER', 'APPROVAL_INPROGRESS', 'POST_FEE_APPROVAL_INPROGRESS') " +
	            "    THEN 1 " +
	            "END) AS department_inprocess " +
	            "FROM eg_bpa_buildingplan bp";

	    return jdbcTemplate.queryForList(query, new Object[]{});
	}

}
