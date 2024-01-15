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
        String query1 = "SELECT " +
                "la.locality, " +
                "la.tenantid, " +
                "lm.ulb_name, " +  
                "COUNT(CASE WHEN bp.status = 'APPROVED' THEN 1 END) AS ApprovedCount, " +
                "COUNT(CASE WHEN bp.status = 'INITIATED' THEN 1 END) AS InitiatedCount, " +
                "SUM(CASE WHEN bp.status = 'APPROVED' AND TO_TIMESTAMP(bp.createdtime / 1000)::date = CURRENT_DATE THEN 1 ELSE 0 END) AS todaysApprovedApplicationsWithinSLA, " +
                "COUNT(CASE WHEN bp.businessservice = 'BPA_LOW' THEN 1 END) AS LOW, " +
                "COUNT(CASE WHEN bp.businessservice = 'BPA' THEN 1 END) AS MEDHIGH, " +
                "COUNT(CASE WHEN la.occupancy = 'Residential' THEN 1 END) AS Residential, " +
                "COUNT(CASE WHEN la.occupancy = 'Mercantile / Commercial' THEN 1 END) AS Institutional, " +
                "COUNT(CASE WHEN egpg.gateway_payment_mode = 'Debit Card' THEN 1 END) AS Debit_Card, " +
                "COUNT(CASE WHEN egpg.gateway_payment_mode = 'Credit Card' THEN 1 END) AS Credit_Card, " +
                "COUNT(CASE WHEN egpg.gateway_payment_mode IN ('Bharat QR', 'Unified Payments') THEN 1 END) AS UPI " +
                "FROM " +
                "eg_land_address AS la " +
                "LEFT JOIN " + 
                "logo_master AS lm ON la.tenantid = lm.tenantid " + 
                "LEFT JOIN " +
                "eg_bpa_buildingplan AS bp ON bp.landid = la.landinfoid " +
                "LEFT JOIN " +
                "eg_pg_transactions AS egpg ON bp.applicationno = egpg.consumer_code " +
                "GROUP BY " +
                "la.locality, la.tenantid, lm.ulb_name";  
        log.info("query1111111" + query1);

        return jdbcTemplate.queryForList(query1);
    }
}
