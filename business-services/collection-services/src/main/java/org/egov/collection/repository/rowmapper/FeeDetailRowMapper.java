package org.egov.collection.repository.rowmapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.collection.model.FeeDetail;
import org.egov.collection.model.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FeeDetailRowMapper implements ResultSetExtractor<List<FeeDetail>> {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<FeeDetail> extractData(ResultSet rs) throws SQLException, DataAccessException {

		List<FeeDetail> feeDetailList = new ArrayList<FeeDetail>();

		while (rs.next()) {
			Integer srNo = rs.getInt("srno");
			String chargesTypeName = rs.getString("charges_type_name");
			BigDecimal amount = rs.getBigDecimal("amount");
			String isFdr=rs.getString("is_fdr");
			FeeDetail feeDetail = new FeeDetail();

			feeDetail.setSrNo(srNo);
			feeDetail.setChargesTypeName(chargesTypeName);
			feeDetail.setAmount(amount);
			feeDetail.setIsFdr(isFdr);
			feeDetailList.add(feeDetail);

		}

//		return new ArrayList<>(idToPaymentMap.values());
		return feeDetailList;
	}

}
