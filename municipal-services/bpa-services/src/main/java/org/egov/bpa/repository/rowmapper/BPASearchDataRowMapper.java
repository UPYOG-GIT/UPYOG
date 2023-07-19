package org.egov.bpa.repository.rowmapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.egov.bpa.web.model.AuditDetails;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.Document;
import org.egov.bpa.web.model.landInfo.LandInfo;
import org.egov.bpa.web.model.landInfo.OwnerInfo;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@Component
@Slf4j
public class BPASearchDataRowMapper implements ResultSetExtractor<List<BPA>> {

	@Autowired
	private ObjectMapper mapper;

	/**
	 * extract the data from the resultset and prepare the BPA Object
	 * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List<BPA> extractData(ResultSet rs) throws SQLException, DataAccessException {

		Map<String, BPA> buildingMap = new LinkedHashMap<String, BPA>();

		while (rs.next()) {
			String id = rs.getString("bpa_id");
			String applicationNo = rs.getString("applicationno");
			//String approvalNo = rs.getString("approvalNo");
			BPA currentbpa = buildingMap.get(id);
			//String tenantId = rs.getString("bpa_tenantId");
			//if (currentbpa == null) {
				//Long lastModifiedTime = rs.getLong("bpa_lastModifiedTime");
//				if (rs.wasNull()) {
//					lastModifiedTime = null;
//				}

//				Object additionalDetails = new Gson().fromJson(rs.getString("additionalDetails").equals("{}")
//						|| rs.getString("additionalDetails").equals("null") ? null : rs.getString("additionalDetails"),
//						Object.class);
				
//				AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("bpa_createdBy"))
//						.createdTime(rs.getLong("bpa_createdTime")).lastModifiedBy(rs.getString("bpa_lastModifiedBy"))
//						.lastModifiedTime(lastModifiedTime).build();


				currentbpa = BPA.builder()
						.applicationNo(applicationNo)
						.approvalDate(rs.getLong("approval_date"))
						.applicationDate(rs.getLong("applicationdate"))
						.build();

				buildingMap.put(id, currentbpa);
//			}
			addChildrenToProperty(rs, currentbpa);

		}

		return new ArrayList<>(buildingMap.values());

	}

	/**
	 * add child objects to the BPA fro the results set
	 * @param rs
	 * @param bpa
	 * @throws SQLException
	 */
	@SuppressWarnings("unused")
	private void addChildrenToProperty(ResultSet rs, BPA bpa) throws SQLException {

		
		OwnerInfo ownerInfo=OwnerInfo.builder().name(rs.getString("name")).mobileNumber(rs.getString("mobilenumber")).build();
		List<OwnerInfo> ownerInfoList=new ArrayList<>();
		ownerInfoList.add(ownerInfo);
		LandInfo landInfo= LandInfo.builder().owners(ownerInfoList).build();
		
		bpa.landInfo(landInfo);
		
		
	}
}
