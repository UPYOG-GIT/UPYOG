/*
 * eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) <2019>  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *      Further, all user interfaces, including but not limited to citizen facing interfaces,
 *         Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *         derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *      For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *      For any further queries on attribution, including queries on brand guidelines,
 *         please contact contact@egovernments.org
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

/* 
 * Edited by @Bhupesh Dewangan
 */
package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.A;
import static org.egov.edcr.constants.DxfFileConstants.F;
import static org.egov.edcr.constants.DxfFileConstants.G;
import static org.egov.edcr.constants.DxfFileConstants.DEVELOPMENT_ZONE;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;

import java.math.BigDecimal;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.OccupancyType;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.stereotype.Service;

@Service
public class Coverage_Dhamtari extends Coverage {
	// private static final String OCCUPANCY2 = "OCCUPANCY";

	private static final Logger LOG = LogManager.getLogger(Coverage_Dhamtari.class);

	// private static final String RULE_NAME_KEY = "coverage.rulename";
	private static final String RULE_DESCRIPTION_KEY = "coverage.description";
	private static final String RULE_EXPECTED_KEY = "coverage.expected";
	private static final String RULE_ACTUAL_KEY = "coverage.actual";
//	private static final BigDecimal Thirty = BigDecimal.valueOf(30);
//	private static final BigDecimal ThirtyFive = BigDecimal.valueOf(35);
//	private static final BigDecimal Forty = BigDecimal.valueOf(40);

	/*
	 * private static final BigDecimal FortyFive = BigDecimal.valueOf(45); private
	 * static final BigDecimal Fifty = BigDecimal.valueOf(50); private static final
	 * BigDecimal FiftyFive = BigDecimal.valueOf(55); private static final
	 * BigDecimal Sixty = BigDecimal.valueOf(60); private static final BigDecimal
	 * SixtyFive = BigDecimal.valueOf(65); private static final BigDecimal Seventy =
	 * BigDecimal.valueOf(70); private static final BigDecimal SeventyFive =
	 * BigDecimal.valueOf(75); private static final BigDecimal Eighty =
	 * BigDecimal.valueOf(80);
	 */

	public static final String RULE_38 = "38";
	public static final String RULE_7_C_1 = "Table 7-C-1";
	private static final BigDecimal ROAD_WIDTH_TWELVE_POINTTWO = BigDecimal.valueOf(12.2);
	private static final BigDecimal ROAD_WIDTH_THIRTY_POINTFIVE = BigDecimal.valueOf(30.5);

	@Override
	public Plan validate(Plan pl) {
		for (Block block : pl.getBlocks()) {
			if (block.getCoverage().isEmpty()) {
				pl.addError("coverageArea" + block.getNumber(),
						"Coverage Area for block " + block.getNumber() + " not Provided");
			}
		}
		return pl;
	}

	@Override
	public Plan process(Plan pl) {
		LOG.info("inside Coverage_BhilaiCharoda process()");
		validate(pl);
		BigDecimal totalCoverage = BigDecimal.ZERO;
		BigDecimal totalCoverageArea = BigDecimal.ZERO;
//		BigDecimal area = pl.getPlot().getArea(); // add for get total plot area
		BigDecimal plotBoundaryArea = pl.getPlot().getPlotBndryArea(); // add for get total plot area
		BigDecimal netPlotArea = pl.getPlot().getNetPlotArea();
		boolean isCenterArea = pl.getPlanInformation().isCenterArea();
		// add for getting OccupancyType
		OccupancyTypeHelper mostRestrictiveOccupancy = pl.getVirtualBuilding().getMostRestrictiveFarHelper();
//		String a=mostRestrictiveOccupancy.getType().getCode();
		// add for getting OccupancyType
//		OccupancyType mostRestrictiveOccupancy = getMostRestrictiveCoverage(pl.getVirtualBuilding().getOccupancies());
		for (Block block : pl.getBlocks()) {

			BigDecimal coverageAreaWithoutDeduction = BigDecimal.ZERO;
			BigDecimal coverageDeductionArea = BigDecimal.ZERO;

			for (Measurement coverage : block.getCoverage()) {
				coverageAreaWithoutDeduction = coverageAreaWithoutDeduction.add(coverage.getArea());
			}
			for (Measurement deduct : block.getCoverageDeductions()) {
				coverageDeductionArea = coverageDeductionArea.add(deduct.getArea());
			}
			if (block.getBuilding() != null) {
				block.getBuilding().setCoverageArea(coverageAreaWithoutDeduction.subtract(coverageDeductionArea));
				BigDecimal coverage = BigDecimal.ZERO;
				if (pl.getPlot().getPlotBndryArea().doubleValue() > 0)
					coverage = block.getBuilding().getCoverageArea().multiply(BigDecimal.valueOf(100)).divide(
							netPlotArea, DcrConstants.DECIMALDIGITS_MEASUREMENTS,
							DcrConstants.ROUNDMODE_MEASUREMENTS);

				block.getBuilding().setCoverage(coverage);

				totalCoverageArea = totalCoverageArea.add(block.getBuilding().getCoverageArea());
				// totalCoverage =
				// totalCoverage.add(block.getBuilding().getCoverage());
			}

		}

		// pl.setCoverageArea(totalCoverageArea);
		// use plotBoundaryArea
		if (pl.getPlot() != null && pl.getPlot().getPlotBndryArea().doubleValue() > 0)
			totalCoverage = totalCoverageArea.multiply(BigDecimal.valueOf(100)).divide(netPlotArea,
					DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS);
		pl.setCoverage(totalCoverage);
		if (pl.getVirtualBuilding() != null) {
			pl.getVirtualBuilding().setTotalCoverageArea(totalCoverageArea);
		}

		BigDecimal roadWidth = pl.getPlanInformation().getRoadWidth();
//		String areaCategory = pl.getAreaCategory();
		BigDecimal permissibleCoverageValue = BigDecimal.ZERO;

		// get coverage permissible value from method and store in
		// permissibleCoverageValue
		String ruleNo = "";
		if (netPlotArea.compareTo(BigDecimal.valueOf(0)) > 0 && mostRestrictiveOccupancy != null) {
//			occupancyType = mostRestrictiveOccupancy.getType().getCode();
			if (A.equals(mostRestrictiveOccupancy.getType().getCode())) { // if
				permissibleCoverageValue = getPermissibleCoverageForResidential(netPlotArea);
				ruleNo = "Table 7-C-1";
			} else if (F.equals(mostRestrictiveOccupancy.getType().getCode())) { // if
				permissibleCoverageValue = getPermissibleCoverageForCommercial(netPlotArea, isCenterArea);
				ruleNo = "Table 7-C-3";
			} else if (G.equals(mostRestrictiveOccupancy.getType().getCode())) { // if
				permissibleCoverageValue = getPermissibleCoverageForIndustrial(netPlotArea);
				ruleNo = "Table 7-C-13";
			}
		}

		if (permissibleCoverageValue.compareTo(BigDecimal.valueOf(0)) > 0) {
			processCoverage(pl, mostRestrictiveOccupancy.getType().getName(), totalCoverage, permissibleCoverageValue,
					isCenterArea, ruleNo);
		}

//		if (roadWidth != null && roadWidth.compareTo(ROAD_WIDTH_TWELVE_POINTTWO) >= 0
//				&& roadWidth.compareTo(ROAD_WIDTH_THIRTY_POINTFIVE) <= 0) {
//
//			processCoverage(pl, StringUtils.EMPTY, totalCoverage, permissibleCoverageValue);
//		}
		LOG.info("return from Coverage process()");
		return pl;
	}

//	private BigDecimal getPermissibleCoverage(OccupancyType type, BigDecimal area) {

	/*
	 * to get coverage permissible value for Residential
	 */
	private BigDecimal getPermissibleCoverageForResidential(BigDecimal area) {
		LOG.info("inside getPermissibleCoverageForResidential()");
		BigDecimal permissibleCoverage = BigDecimal.ZERO;

//		switch (developmentZone) {
//
//		case "DA-01":
		if (area.compareTo(BigDecimal.valueOf(32)) <= 0) {
			permissibleCoverage = BigDecimal.valueOf(100);
		} else if (area.compareTo(BigDecimal.valueOf(32)) > 0 && area.compareTo(BigDecimal.valueOf(48)) <= 0) {
			permissibleCoverage = BigDecimal.valueOf(70);
		} else if (area.compareTo(BigDecimal.valueOf(48)) > 0 && area.compareTo(BigDecimal.valueOf(135)) <= 0) {
			permissibleCoverage = BigDecimal.valueOf(60);
		} else if (area.compareTo(BigDecimal.valueOf(135)) > 0 && area.compareTo(BigDecimal.valueOf(200)) <= 0) {
			permissibleCoverage = BigDecimal.valueOf(55);
		} else if (area.compareTo(BigDecimal.valueOf(200)) > 0 && area.compareTo(BigDecimal.valueOf(216)) <= 0) {
			permissibleCoverage = BigDecimal.valueOf(48);
		} else if (area.compareTo(BigDecimal.valueOf(216)) > 0 && area.compareTo(BigDecimal.valueOf(270)) <= 0) {
			permissibleCoverage = BigDecimal.valueOf(45);
		} else if (area.compareTo(BigDecimal.valueOf(270)) > 0 && area.compareTo(BigDecimal.valueOf(360)) <= 0) {
			permissibleCoverage = BigDecimal.valueOf(40);
		} else if (area.compareTo(BigDecimal.valueOf(360)) > 0 && area.compareTo(BigDecimal.valueOf(600)) <= 0) {
			permissibleCoverage = BigDecimal.valueOf(33);
		} else if (area.compareTo(BigDecimal.valueOf(600)) > 0) {
			permissibleCoverage = BigDecimal.valueOf(30);
		}
//			break;
//		}
		LOG.info("return from getPermissibleCoverageForResidential()");
		return permissibleCoverage;
	}

	/*
	 * to get coverage permissible value for Commercial
	 */

	private BigDecimal getPermissibleCoverageForCommercial(BigDecimal area, boolean isCenterArea) {
		LOG.info("inside getPermissibleCoverageForCommercial()");
		BigDecimal permissibleCoverage = BigDecimal.ZERO;

//		switch (developmentZone) {
//
//		case "DA-01":
		/*
		 * if (area.compareTo(BigDecimal.valueOf(150)) <= 0) { permissibleCoverage =
		 * BigDecimal.valueOf(60); } else if (area.compareTo(BigDecimal.valueOf(150)) >
		 * 0 && area.compareTo(BigDecimal.valueOf(240)) <= 0) { permissibleCoverage =
		 * BigDecimal.valueOf(55); } else if (area.compareTo(BigDecimal.valueOf(240)) >
		 * 0 && area.compareTo(BigDecimal.valueOf(500)) <= 0) { permissibleCoverage =
		 * BigDecimal.valueOf(50); } else if (area.compareTo(BigDecimal.valueOf(500)) >
		 * 0 && area.compareTo(BigDecimal.valueOf(750)) <= 0) { permissibleCoverage =
		 * BigDecimal.valueOf(45); } else if (area.compareTo(BigDecimal.valueOf(750)) >
		 * 0 && area.compareTo(BigDecimal.valueOf(1000)) <= 0) { permissibleCoverage =
		 * BigDecimal.valueOf(40); } else if (area.compareTo(BigDecimal.valueOf(500)) >
		 * 0) { permissibleCoverage = BigDecimal.valueOf(35); }
		 */
//			break;
//		}

		if (isCenterArea) {
			permissibleCoverage = BigDecimal.valueOf(80);
		} else {
			permissibleCoverage = BigDecimal.valueOf(70);
		}
		LOG.info("return from getPermissibleCoverageForCommercial()");
		return permissibleCoverage;
	}

	private BigDecimal getPermissibleCoverageForIndustrial(BigDecimal area) {
		LOG.info("inside getPermissibleCoverageForCommercial()");
		BigDecimal permissibleCoverage = BigDecimal.ZERO;

//		switch (developmentZone) {
//
//		case "DA-01":
		if (area.compareTo(BigDecimal.valueOf(500)) <= 0) {
			permissibleCoverage = BigDecimal.valueOf(60);
		} else if (area.compareTo(BigDecimal.valueOf(500)) > 0 && area.compareTo(BigDecimal.valueOf(1000)) <= 0) {
			permissibleCoverage = BigDecimal.valueOf(55);
		} else if (area.compareTo(BigDecimal.valueOf(1000)) > 0 && area.compareTo(BigDecimal.valueOf(2000)) <= 0) {
			permissibleCoverage = BigDecimal.valueOf(50);
		} else if (area.compareTo(BigDecimal.valueOf(2000)) > 0 && area.compareTo(BigDecimal.valueOf(20000)) <= 0) {
			permissibleCoverage = BigDecimal.valueOf(45);
		} else if (area.compareTo(BigDecimal.valueOf(20000)) > 0) {
			permissibleCoverage = BigDecimal.valueOf(33);
		}
//			break;
//		}
		LOG.info("return from getPermissibleCoverageForCommercial()");
		return permissibleCoverage;
	}

	private void processCoverage(Plan pl, String occupancy, BigDecimal coverage, BigDecimal upperLimit,
			boolean isCenterArea, String ruleNo) {
		LOG.info("inside processCoverage()");
		ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
		scrutinyDetail.setKey("Common_Coverage");
		scrutinyDetail.setHeading("Coverage in Percentage");
		scrutinyDetail.addColumnHeading(1, RULE_NO);
		scrutinyDetail.addColumnHeading(2, CENTER_AREA);
		scrutinyDetail.addColumnHeading(3, DESCRIPTION);
		scrutinyDetail.addColumnHeading(4, OCCUPANCY);
		scrutinyDetail.addColumnHeading(5, PERMISSIBLE);
		scrutinyDetail.addColumnHeading(6, PROVIDED);
		scrutinyDetail.addColumnHeading(7, STATUS);

		String desc = getLocaleMessage(RULE_DESCRIPTION_KEY, upperLimit.toString());
		String actualResult = getLocaleMessage(RULE_ACTUAL_KEY, coverage.toString());
		String expectedResult = getLocaleMessage(RULE_EXPECTED_KEY, upperLimit.toString());
		if (coverage.doubleValue() <= upperLimit.doubleValue()) {
			Map<String, String> details = new HashMap<>();
			details.put(RULE_NO, ruleNo);
			details.put(CENTER_AREA, isCenterArea ? "Yes" : "No");
			details.put(DESCRIPTION, desc);
			details.put(OCCUPANCY, occupancy);
			details.put(PERMISSIBLE, expectedResult);
			details.put(PROVIDED, actualResult);
			details.put(STATUS, Result.Accepted.getResultVal());
			scrutinyDetail.getDetail().add(details);
			pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);

		} else {
			Map<String, String> details = new HashMap<>();
			details.put(RULE_NO, ruleNo);
			details.put(CENTER_AREA, isCenterArea ? "Yes" : "No");
			details.put(DESCRIPTION, desc);
			details.put(OCCUPANCY, occupancy);
			details.put(PERMISSIBLE, expectedResult);
			details.put(PROVIDED, actualResult);
			details.put(STATUS, Result.Not_Accepted.getResultVal());
			scrutinyDetail.getDetail().add(details);
			pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);

		}
		LOG.info("out from processCoverage()");

	}

	protected OccupancyType getMostRestrictiveCoverage(EnumSet<OccupancyType> distinctOccupancyTypes) {

		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_B1))
			return OccupancyType.OCCUPANCY_B1;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_B2))
			return OccupancyType.OCCUPANCY_B2;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_B3))
			return OccupancyType.OCCUPANCY_B3;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_D))
			return OccupancyType.OCCUPANCY_D;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_D1))
			return OccupancyType.OCCUPANCY_D1;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_I2))
			return OccupancyType.OCCUPANCY_I2;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_I1))
			return OccupancyType.OCCUPANCY_I1;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_C))
			return OccupancyType.OCCUPANCY_C;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_A1))
			return OccupancyType.OCCUPANCY_A1;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_A4))
			return OccupancyType.OCCUPANCY_A4;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_A2))
			return OccupancyType.OCCUPANCY_A2;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_G1))
			return OccupancyType.OCCUPANCY_G1;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_E))
			return OccupancyType.OCCUPANCY_E;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_F))
			return OccupancyType.OCCUPANCY_F;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_F4))
			return OccupancyType.OCCUPANCY_F4;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_G2))
			return OccupancyType.OCCUPANCY_G2;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_H))
			return OccupancyType.OCCUPANCY_H;

		else
			return null;
	}

	@Override
	public Map<String, Date> getAmendments() {
		return new LinkedHashMap<>();
	}
}
