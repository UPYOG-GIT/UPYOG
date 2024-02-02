package org.egov.bpa.repository;

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

    public List<Map<String, Object>> getIngestData() {
    	log.info("inside getigestdata repo");

		String query1 = "SELECT \n"
		        + "  la.locality,\n"
		        + "  la.tenantid,\n"
		        + "  lm.ulb_name,\n"
		        + "  COUNT(DISTINCT CASE WHEN bp.status = 'APPROVED' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('2024-01-16', 'YYYY-MM-DD') THEN bp.applicationno END) AS ApprovedCount, \n"
		        + "  COUNT(DISTINCT CASE WHEN bp.status = 'INITIATED' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('2024-01-16', 'YYYY-MM-DD') THEN bp.applicationno END) AS InitiatedCount, \n"
		        + "  COUNT(DISTINCT CASE WHEN bp.status = 'APPROVED' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('2024-01-16', 'YYYY-MM-DD') THEN bp.applicationno END) AS todaysApprovedApplicationsWithinSLA, \n"
		        + "  COUNT(DISTINCT CASE WHEN bp.businessservice = 'BPA_LOW' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('2024-01-16', 'YYYY-MM-DD') THEN bp.applicationno END) AS LOW, \n"
		        + "  COUNT(DISTINCT CASE WHEN bp.businessservice = 'BPA' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('2024-01-16', 'YYYY-MM-DD') THEN bp.applicationno END) AS MEDHIGH, \n"
		        + "  COUNT(DISTINCT CASE WHEN la.occupancy = 'Residential' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('2024-01-16', 'YYYY-MM-DD') THEN la.landinfoid END) AS Residential, \n"
		        + "  COUNT(DISTINCT CASE WHEN la.occupancy = 'Mercantile / Commercial' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = TO_DATE('2024-01-16', 'YYYY-MM-DD') THEN la.landinfoid END) AS Institutional, \n"
		        + "  COUNT(CASE WHEN egpg.gateway_payment_mode = 'Debit Card' THEN 1 END) AS Debit_Card, \n"
		        + "  COUNT(CASE WHEN egpg.gateway_payment_mode = 'Credit Card' THEN 1 END) AS Credit_Card, \n"
		        + "  COUNT(CASE WHEN egpg.gateway_payment_mode IN ('Bharat QR', 'Unified Payments') THEN 1 END) AS UPI,\n"
		        + "  COUNT(egpg.gateway_payment_mode) AS Payment_Mode\n"
		        + "FROM \n"
		        + "  eg_land_address AS la \n"
		        + "  LEFT JOIN logo_master AS lm ON la.tenantid = lm.tenantid \n"
		        + "  LEFT JOIN eg_bpa_buildingplan AS bp ON bp.landid = la.landinfoid\n"
		        + "  LEFT JOIN (\n"
		        + "    SELECT DISTINCT consumer_code, gateway_payment_mode\n"
		        + "    FROM eg_pg_transactions\n"
		        + "    WHERE txn_status='SUCCESS' \n"
		        + "      AND TO_TIMESTAMP(created_time / 1000)::date = TO_DATE('2024-01-16', 'YYYY-MM-DD')\n"
		        + "  ) AS egpg ON bp.applicationno = egpg.consumer_code\n"
		        + "GROUP BY \n"
		        + "  la.locality, la.tenantid, lm.ulb_name\n"
		        + "ORDER BY \n"
		        + "  la.locality;\n";


		System.out.println("query1111111" + query1);


        return jdbcTemplate.queryForList(query1);
    }
}
