package org.egov.bpa.repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.bpa.web.model.NdbResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class NationalDashboardRepository {
	@Autowired
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public NationalDashboardRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Map<String, Object> getIngestData(String formattedDate) {
//        LocalDate startDate = LocalDate.of(2023, 1, 1);
//        LocalDate endDate = LocalDate.of(2024, 2, 14);
//        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		System.out.println("dateeee------" + formattedDate);
//
		Map<String, Object> resultMap = new HashMap<>();
//
//        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
//            String formattedDate = date.format(dateFormatter);
		String query1 = "SELECT \n" + "  la.locality,\n" + "  la.tenantid,\n" +
		// " SUM(CAST(la.plotarea AS numeric)) AS TotalPlotArea,\n" +
				"  lm.ulb_name,\n"
//						+ "  egpg.txn_amount,\n"
				+ "  COALESCE(SUM(DISTINCT CASE WHEN egpg.gateway_payment_mode IN ('Bharat QR', 'Unified Payments', 'Net Banking', 'Debit Card', 'Credit Card') THEN egpg.txn_amount ELSE 0 END), 0) AS online_amt,\n"
				+ "  COUNT(DISTINCT CASE WHEN bp.status = 'APPROVED' AND bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) AS ApprovedCount, \n"
				+ " COUNT(DISTINCT CASE WHEN bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) AS todaysApplicationSubmitted, "
				+ "  COUNT(DISTINCT CASE WHEN bp.status = 'INITIATED' AND bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) AS InitiatedCount, \n"
				+ "  COUNT(DISTINCT CASE WHEN bp.status = 'APPROVED' AND bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate
				+ "', 'YYYY-MM-DD') THEN bp.applicationno END) AS todaysApprovedApplicationsWithinSLA, \n"
				+ "  COUNT(DISTINCT CASE WHEN bp.businessservice = 'BPA_LOW' AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) AS LOW, \n"
				+ "  COUNT(DISTINCT CASE WHEN bp.businessservice = 'BPA' AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) AS MEDHIGH, \n"
				+ "  COUNT(DISTINCT CASE WHEN la.occupancy = 'Residential' AND bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN la.landinfoid END) AS Residential, \n"
				+ "  COUNT(DISTINCT CASE WHEN la.occupancy = 'Industrial' AND bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN la.landinfoid END) AS Industrial, \n"
				+ "  COUNT(DISTINCT CASE WHEN la.occupancy = 'Mercantile / Commercial' AND bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN la.landinfoid END) AS Commercial, \n"
				+ "  COUNT(DISTINCT CASE WHEN la.occupancy like ('%,%') AND bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN la.landinfoid END) AS Mix, \n"
				+ "  COUNT(DISTINCT egpg.gateway_payment_mode) AS Payment_Mode,\n"
				+ "  COALESCE(SUM(DISTINCT CASE WHEN TO_TIMESTAMP(la.createdtime / 1000)::date = TO_DATE('"
				+ formattedDate
				+ "', 'YYYY-MM-DD') THEN CAST(la.plotarea AS numeric) ELSE 0 END), 0) AS TotalPlotArea, "
				+ "  COALESCE(AVG(DISTINCT CASE WHEN bp.businessservice IN ('BPA','BPA_LOW') AND approvaldate IS NOT NULL and approvaldate != 0 THEN ((bp.approvaldate - bp.createdtime) / 86400000.0) ELSE 0 END), 0) AS avg_days_to_issue_certificate, "
				+ "  COUNT(DISTINCT CASE \n" + "    WHEN wf.businessservicesla >= 0 \n"
				+ "      AND bp.status = 'APPROVED' \n" + " 	 AND bp.businessservice IN ('BPA','BPA_LOW') "
				+ "      AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('" + formattedDate
				+ "', 'YYYY-MM-DD') \n" + "    THEN bp.applicationno \n"
				+ "  END) AS todaysApprovedApplicationsWithinSLA1, " + "  COUNT(DISTINCT CASE \n"
				+ "    WHEN wf.businessservicesla >= 0 \n" + "      AND bp.status = 'APPROVED' \n"
				+ " 	 AND bp.businessservice IN ('BPA_OC') "
				+ "      AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('" + formattedDate
				+ "', 'YYYY-MM-DD') \n" + "    THEN bp.applicationno \n"
				+ "  END) AS todaysApprovedOCApplicationsWithinSLA, "
				+ "  COUNT(DISTINCT CASE WHEN bp.status = 'APPROVED' AND bp.businessservice IN ('BPA_OC') AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) AS OCApprovedCount, \n"
				+ " COUNT(DISTINCT CASE WHEN bp.businessservice IN ('BPA_OC') AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) AS todaysOCApplicationSubmitted, "
				+ "  COALESCE(AVG(DISTINCT CASE WHEN bp.businessservice IN ('BPA_OC') AND approvaldate IS NOT NULL and approvaldate != 0 THEN ((bp.approvaldate - bp.createdtime) / 86400000.0) ELSE 0 END), 0) AS avg_days_to_issue_oc_certificate "
				+ "  FROM \n" + "  eg_land_address AS la \n"
				+ "  LEFT JOIN logo_master AS lm ON la.tenantid = lm.tenantid \n"
				+ "  LEFT JOIN eg_bpa_buildingplan AS bp ON bp.landid = la.landinfoid\n" + "  LEFT JOIN (\n"
				+ "    SELECT DISTINCT txn_amount, consumer_code, gateway_payment_mode\n"
				+ "    FROM eg_pg_transactions\n" + "    WHERE txn_status='SUCCESS' \n"
				+ "      AND TO_TIMESTAMP(created_time / 1000)::date = TO_DATE('" + formattedDate + "', 'YYYY-MM-DD')\n"
				+ "  ) AS egpg ON bp.applicationno = egpg.consumer_code \n"
				+ " LEFT JOIN eg_wf_processinstance_v2 AS wf ON wf.businessid = bp.applicationno WHERE bp.tenantid != 'cg.citya'\n "
				+ " GROUP BY \n" + "  la.locality, la.tenantid, lm.ulb_name " + "HAVING \n"
				+ "  COALESCE(SUM(DISTINCT CASE WHEN egpg.gateway_payment_mode IN ('Bharat QR', 'Unified Payments', 'Net Banking', 'Debit Card', 'Credit Card') THEN egpg.txn_amount ELSE 0 END), 0) > 0\n"
				+ "  OR COUNT(DISTINCT CASE WHEN bp.status = 'APPROVED' AND bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) > 0\n"
				+ " OR COUNT(DISTINCT CASE WHEN bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) > 0 "
				+ "  OR COUNT(DISTINCT CASE WHEN bp.status = 'INITIATED' AND bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) > 0\n"
				+ "  OR COUNT(DISTINCT CASE WHEN bp.status = 'APPROVED' AND bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) > 0\n"
				+ "  OR COUNT(DISTINCT CASE WHEN bp.businessservice = 'BPA_LOW' AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) > 0\n"
				+ "  OR COUNT(DISTINCT CASE WHEN bp.businessservice = 'BPA' AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) > 0\n"
				+ "  OR COUNT(DISTINCT CASE WHEN la.occupancy = 'Residential' AND bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN la.landinfoid END) > 0\n"
				+ "  OR COUNT(DISTINCT CASE WHEN la.occupancy = 'Industrial' AND bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN la.landinfoid END) > 0\n"
				+ "  OR COUNT(DISTINCT CASE WHEN la.occupancy = 'Mercantile / Commercial' AND bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN la.landinfoid END) > 0\n"
				+ "  OR COUNT(DISTINCT CASE WHEN la.occupancy like('%,%') AND bp.businessservice IN ('BPA','BPA_LOW') AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN la.landinfoid END) > 0\n" + " OR COUNT(DISTINCT CASE \n"
				+ "    WHEN wf.businessservicesla >= 0 \n"
				+ "      AND bp.status = 'APPROVED' AND bp.businessservice IN ('BPA','BPA_LOW')  \n"
				+ "      AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('" + formattedDate
				+ "', 'YYYY-MM-DD') \n" + "    THEN bp.applicationno \n" + "  END) > 0 " + " OR COUNT(DISTINCT CASE \n"
				+ "    WHEN wf.businessservicesla >= 0 \n"
				+ "      AND bp.status = 'APPROVED' AND bp.businessservice IN ('BPA_OC')  \n"
				+ "      AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('" + formattedDate
				+ "', 'YYYY-MM-DD') \n" + "    THEN bp.applicationno \n" + "  END) > 0 "
				+ "  OR COUNT(egpg.gateway_payment_mode) > 0"
				+ "  OR COUNT(DISTINCT CASE WHEN bp.status = 'APPROVED' AND bp.businessservice IN ('BPA_OC') AND TO_TIMESTAMP(bp.approvaldate / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) > 0\n"
				+ " OR COUNT(DISTINCT CASE WHEN bp.businessservice IN ('BPA_OC') AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('"
				+ formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) > 0 " + " ORDER BY \n"
				+ "  la.locality;\n";

		log.info("Query for date " + formattedDate + ":\n" + query1);

		List<Map<String, Object>> result1 = jdbcTemplate.queryForList(query1);
		resultMap.put("result1", result1); // Store results for each date

		// Execute query2 and query3 and store their results in the resultMap
//		String query2 = "SELECT AVG((approvaldate - createdtime) / 86400000.0) AS avg_days_to_issue_certificate FROM eg_bpa_buildingplan WHERE approvaldate IS NOT NULL and approvaldate != 0;";
//		Double avgDaysToIssueCertificate = jdbcTemplate.queryForObject(query2, Double.class);

//		String query3 = " SELECT SUM(CAST(plotarea AS numeric)) AS TotalPlotArea FROM eg_land_address";
//
//		Double totalPlotArea = jdbcTemplate.queryForObject(query3, Double.class);

//		resultMap.put("avg_days_to_issue_certificate", avgDaysToIssueCertificate);
//		resultMap.put("totalPlotArea", totalPlotArea);

		// If you have more queries, execute them similarly and store their results in
		// the resultMap

		return resultMap;
	}

	public void saveDashboardPushedRecord(NdbResponse ndbResponse) {

//		String insertQuery = "insert into paytype_master(ulb_tenantid,charges_type_name,payment_type,"
//				+ "defunt,createdby,createddate) values (?,?,?,?,?,'" + date + "')";
		String insertQuery = "INSERT INTO eg_ndb_push_status(response_hash, no_of_reccords, error, environment, message_description, no_of_applications, approved_applications, totalPlotArea, totalCollection, data_pushed_date) values ('"
				+ ndbResponse.getResponseHash() + "','" + ndbResponse.getNoOfRecordsPushed() + "','"
				+ ndbResponse.getErrorMessage() + "','" + ndbResponse.getEnvironment() + "','"
				+ ndbResponse.getMessageDescription() + "','" + ndbResponse.getTotalNoOfApplications() + "','"
				+ ndbResponse.getTotalApprovedApplications() + "','" + ndbResponse.getTotalPlotArea() + "','" +ndbResponse.getTotalCollection() + "','" + ndbResponse.getDate() + "')";

		log.info("Dashboard insertQuery: " + insertQuery);
		int insertResult = jdbcTemplate.update(insertQuery);
		log.info("eg_ndb_push_status Result : " + insertResult + " Data inserted");
	}
}
