package org.entit.rga.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.entit.rga.config.RGAConfiguration;
import org.entit.rga.producer.Producer;
import org.entit.rga.repository.querybuilder.RGAQueryBuilder;
import org.entit.rga.repository.rowmapper.RGARowMapper;
import org.entit.rga.web.model.RGA;
import org.entit.rga.web.model.RGAPenaltyRequest;
import org.entit.rga.web.model.RGARequest;
import org.entit.rga.web.model.RGASearchCriteria;
import org.entit.rga.web.model.RGASlabMasterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class RGARepository {

	@Autowired
	private RGAConfiguration config;

	@Autowired
	private Producer producer;

	@Autowired
	private RGAQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private RGARowMapper rowMapper;

	/**
	 * Pushes the request on save topic through kafka
	 *
	 * @param bpaRequest The bpa create request
	 */
	public void save(RGARequest rGARequest) {
		producer.push(config.getSaveTopic(), rGARequest);
	}

	/**
	 * pushes the request on update or workflow update topic through kafaka based on
	 * th isStateUpdatable
	 * 
	 * @param rGARequest
	 * @param isStateUpdatable
	 */
	public void update(RGARequest rGARequest, boolean isStateUpdatable) {
		RequestInfo requestInfo = rGARequest.getRequestInfo();

		RGA regularisationForStatusUpdate = null;
		RGA regularisationForUpdate = null;

		RGA rGA = rGARequest.getRegularisation();

		if (isStateUpdatable) {
			regularisationForUpdate = rGA;
		} else {
			regularisationForStatusUpdate = rGA;
		}
		if (regularisationForUpdate != null)
			producer.push(config.getUpdateTopic(), new RGARequest(requestInfo, regularisationForUpdate));

		if (regularisationForStatusUpdate != null)
			producer.push(config.getUpdateWorkflowTopic(), new RGARequest(requestInfo, regularisationForStatusUpdate));

	}

	/**
	 * BPA search in database
	 *
	 * @param criteria The BPA Search criteria
	 * @return List of BPA from search
	 */
	public List<RGA> getRGAData(RGASearchCriteria criteria, List<String> edcrNos) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getRegularisationSearchQuery(criteria, preparedStmtList, edcrNos, false);
		List<RGA> BPAData = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
		return BPAData;
	}

	/**
	 * BPA search count in database
	 *
	 * @param criteria The BPA Search criteria
	 * @return count of BPA from search
	 */
	public int getBPACount(RGASearchCriteria criteria, List<String> edcrNos) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getRegularisationSearchQuery(criteria, preparedStmtList, edcrNos, true);
		int count = jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
		return count;
	}

	public List<RGA> getBPADataForPlainSearch(RGASearchCriteria criteria, List<String> edcrNos) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getBPASearchQueryForPlainSearch(criteria, preparedStmtList, edcrNos, false);
		List<RGA> BPAData = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
		return BPAData;
	}

	/*
	 * public int createRGASlabMaster(RGASlabMasterRequest rgaSlabMasterRequest) {
	 * 
	 * // String query =
	 * "SELECT COUNT(*) FROM slab_master WHERE ulb_tenantid=? AND paytype_id=?"; //
	 * int count = jdbcTemplate.queryForObject(query, // new Object[] {
	 * rgaSlabMasterRequest.getTenantId(), rgaSlabMasterRequest.getPayTypeId() }, //
	 * Integer.class); // log.info("createSlabMaster count : " + count);
	 * LocalDateTime date = LocalDateTime.now(); // count = count + 1;
	 * 
	 * String insertQuery =
	 * "insert into eg_rga_slab_master(ulb_tenantid,from_val,to_val,p_category,b_category,s_category,rate,occupancy_type,"
	 * + "createdby,createddate) " + "values (?,?,?,?,?,?,?,?,?,'" + date + "')";
	 * int insertResult = jdbcTemplate.update(insertQuery,
	 * rgaSlabMasterRequest.getTenantId(), rgaSlabMasterRequest.getFromVal(),
	 * rgaSlabMasterRequest.getToVal(), rgaSlabMasterRequest.getPCategory(),
	 * rgaSlabMasterRequest.getBCategory(), rgaSlabMasterRequest.getSCategory(),
	 * rgaSlabMasterRequest.getRate(), rgaSlabMasterRequest.getOccupancyType(),
	 * rgaSlabMasterRequest.getCreatedBy()); // int insertResult =
	 * jdbcTemplate.update(insertQuery);
	 * 
	 * log.info("BPARepository.createRGASlabMaster: " + insertResult +
	 * " data inserted into eg_rga_slab_master table"); return insertResult; }
	 * 
	 * public List<Map<String, Object>> getRGASlabMasterByTenantId(String tenantId)
	 * { String query = "SELECT id, from_val, to_val, rate," +
	 * " p_category, b_category, s_category, occupancy_type " +
	 * " FROM eg_rga_slab_master WHERE ulb_tenantid=?"; return
	 * jdbcTemplate.queryForList(query, new Object[] { tenantId }); }
	 */

	public int createRGASlabMaster(RGASlabMasterRequest rgaSlabMasterRequest) {

		LocalDateTime date = LocalDateTime.now();

		String insertQuery = "insert into eg_rga_slab_master(ulb_tenantid,from_val,to_val,p_category,b_category,s_category,rate,occupancy_type,"
				+ "createdby,createddate) " + "values (?,?,?,?,?,?,?,?,?,'" + date + "')";
		int insertResult = jdbcTemplate.update(insertQuery, rgaSlabMasterRequest.getTenantId(),
				rgaSlabMasterRequest.getFromVal(), rgaSlabMasterRequest.getToVal(), rgaSlabMasterRequest.getPCategory(),
				rgaSlabMasterRequest.getBCategory(), rgaSlabMasterRequest.getSCategory(),
				rgaSlabMasterRequest.getRate(), rgaSlabMasterRequest.getOccupancyType(),
				rgaSlabMasterRequest.getCreatedBy());

		log.info("BPARepository.createRGASlabMaster: " + insertResult + " data inserted into eg_rga_slab_master table");
		return insertResult;
	}

	public List<Map<String, Object>> getRGASlabMasterByTenantId(String tenantId) {
		String query = "SELECT id, from_val, to_val, rate, p_category, b_category, s_category, occupancy_type "
				+ " FROM eg_rga_slab_master WHERE ulb_tenantid=?";
		return jdbcTemplate.queryForList(query, new Object[] { tenantId });
	}

	public int deleteRGASlabMasterById(List<Integer> ids) {
		String id = ids.toString().replace("[", "").replace("]", "");
		String deleteQuery = "DELETE FROM eg_rga_slab_master WHERE id IN (" + id + ")";
		int deleteResult = jdbcTemplate.update(deleteQuery);
		log.info("BPARepository.deleteRGASlabMasterById: " + deleteResult
				+ " data deleted from eg_rga_slab_master table of id(s) : " + id);
		return deleteResult;
	}

	public int createRGAPenalty(RGAPenaltyRequest rgaPenaltyRequest) {

		LocalDateTime date = LocalDateTime.now();
		String insertQuery = "insert into eg_rga_penalty(ulb_tenantid,from_val,to_val,occupancy_type,multipy_penalty,rate,createdby,createddate) "
				+ "values (?,?,?,?,?,?,?,'" + date + "')";
		int insertResult = jdbcTemplate.update(insertQuery, rgaPenaltyRequest.getTenantId(),
				rgaPenaltyRequest.getFromVal(), rgaPenaltyRequest.getToVal(), rgaPenaltyRequest.getOccupancyType(),
				rgaPenaltyRequest.getMultipyPenalty(), rgaPenaltyRequest.getRate(), rgaPenaltyRequest.getCreatedBy());
		
//		int insertResult = jdbcTemplate.update(insertQuery);

		log.info("BPARepository.createRGAPenalty: " + insertResult + " data inserted into eg_rga_penalty table");
		return insertResult;
	}

	public List<Map<String, Object>> getRGAPenaltyByTenantId(String tenantId) {
		String query = "SELECT id, from_val, to_val, occupancy_type, multipy_penalty, rate "
				+ " FROM eg_rga_penalty WHERE ulb_tenantid=?";
		return jdbcTemplate.queryForList(query, new Object[] { tenantId });
	}

	public int deleteRGAPenaltyById(List<Integer> ids) {
		String id = ids.toString().replace("[", "").replace("]", "");
		String deleteQuery = "DELETE FROM eg_rga_penalty WHERE id IN (" + id + ")";
		int deleteResult = jdbcTemplate.update(deleteQuery);
		log.info("BPARepository.deleteRGAPenaltyById: " + deleteResult
				+ " data deleted from eg_rga_penalty table of id(s) : " + id);
		return deleteResult;
	}

}
