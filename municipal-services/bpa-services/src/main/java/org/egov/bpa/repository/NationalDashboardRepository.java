package org.egov.bpa.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
             String query1 = "SELECT \n" +
	                    "  la.locality,\n" +
	                    "  la.tenantid,\n" +
	           //         "  SUM(CAST(la.plotarea AS numeric)) AS TotalPlotArea,\n" +
	                    "  lm.ulb_name,\n" +
	                    "  egpg.txn_amount,\n" +
	                    "  COALESCE(SUM(CASE WHEN egpg.gateway_payment_mode = 'Debit Card' THEN egpg.txn_amount ELSE 0 END), 0) AS debit_amt,\n" +
	                    "  COALESCE(SUM(CASE WHEN egpg.gateway_payment_mode = 'Credit Card' THEN egpg.txn_amount ELSE 0 END), 0) AS credit_amt,\n" +
	                    "  COALESCE(SUM(CASE WHEN egpg.gateway_payment_mode IN ('Bharat QR', 'Unified Payments', 'Net Banking') THEN egpg.txn_amount ELSE 0 END), 0) AS upi_amt,\n" +
	                    "  COUNT(DISTINCT CASE WHEN bp.status = 'APPROVED' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('" + formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) AS ApprovedCount, \n" +
	                    "  COUNT(DISTINCT CASE WHEN bp.status = 'INITIATED' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('" + formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) AS InitiatedCount, \n" +
	                    "  COUNT(DISTINCT CASE WHEN bp.status = 'APPROVED' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('" + formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) AS todaysApprovedApplicationsWithinSLA, \n" +
	                    "  COUNT(DISTINCT CASE WHEN bp.businessservice = 'BPA_LOW' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('" + formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) AS LOW, \n" +
	                    "  COUNT(DISTINCT CASE WHEN bp.businessservice = 'BPA' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('" + formattedDate + "', 'YYYY-MM-DD') THEN bp.applicationno END) AS MEDHIGH, \n" +
	                    "  COUNT(DISTINCT CASE WHEN la.occupancy = 'Residential' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('" + formattedDate + "', 'YYYY-MM-DD') THEN la.landinfoid END) AS Residential, \n" +
	                    "  COUNT(DISTINCT CASE WHEN la.occupancy = 'Industrial' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('" + formattedDate + "', 'YYYY-MM-DD') THEN la.landinfoid END) AS Industrial, \n" +
	                    "  COUNT(DISTINCT CASE WHEN la.occupancy = 'Mercantile / Commercial' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('" + formattedDate + "', 'YYYY-MM-DD') THEN la.landinfoid END) AS Institutional, \n" +
	                    "  COUNT(egpg.gateway_payment_mode) AS Payment_Mode\n" +
	                    "FROM \n" +
	                    "  eg_land_address AS la \n" +
	                    "  LEFT JOIN logo_master AS lm ON la.tenantid = lm.tenantid \n" +
	                    "  LEFT JOIN eg_bpa_buildingplan AS bp ON bp.landid = la.landinfoid\n" +
	                    "  LEFT JOIN (\n" +
	                    "    SELECT DISTINCT txn_amount, consumer_code, gateway_payment_mode\n" +
	                    "    FROM eg_pg_transactions\n" +
	                    "    WHERE txn_status='SUCCESS' \n" +
	                    "      AND TO_TIMESTAMP(created_time / 1000)::date = TO_DATE('" + formattedDate + "', 'YYYY-MM-DD')\n" +
	                    "  ) AS egpg ON bp.applicationno = egpg.consumer_code\n" +
	                    "GROUP BY \n" +
	                    "  la.locality, la.tenantid, lm.ulb_name, egpg.txn_amount\n" +
	                    "ORDER BY \n" +
	                    "  la.locality;\n";

           System.out.println("Query for date " + formattedDate + ":\n" + query1);

            List<Map<String, Object>> result1 = jdbcTemplate.queryForList(query1);
            resultMap.put("result1", result1); // Store results for each date
        

        // Execute query2 and query3 and store their results in the resultMap
        String query2 = "SELECT AVG((approvaldate - createdtime) / 86400000.0) AS avg_days_to_issue_certificate FROM eg_bpa_buildingplan WHERE approvaldate IS NOT NULL and approvaldate != 0;";
        Double avgDaysToIssueCertificate = jdbcTemplate.queryForObject(query2, Double.class);
        
        String query3 = " SELECT SUM(CAST(plotarea AS numeric)) AS TotalPlotArea FROM eg_land_address";
        
        Double totalPlotArea = jdbcTemplate.queryForObject(query3, Double.class);
        
        resultMap.put("avg_days_to_issue_certificate", avgDaysToIssueCertificate);
        resultMap.put("totalPlotArea", totalPlotArea);
       

        // If you have more queries, execute them similarly and store their results in the resultMap

        return resultMap;
    }
}
