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

package org.egov.edcr.feature;

import static org.egov.edcr.constants.DxfFileConstants.A;
import static org.egov.edcr.constants.DxfFileConstants.A_AF;
import static org.egov.edcr.constants.DxfFileConstants.A_PO;
import static org.egov.edcr.constants.DxfFileConstants.A_R;
import static org.egov.edcr.constants.DxfFileConstants.F;
import static org.egov.edcr.constants.DxfFileConstants.J;
import static org.egov.edcr.constants.DxfFileConstants.G;
import static org.egov.edcr.utility.DcrConstants.FRONT_YARD_DESC;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Building;
import org.egov.common.entity.edcr.Occupancy;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Plot;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.Road;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.common.entity.edcr.SetBack;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.infra.utils.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class FrontYardService_Citya extends FrontYardService {

	private static final Logger LOG = LogManager.getLogger(FrontYardService_Citya.class);

	private static final String RULE_35 = "35 Table-8";
	private static final String RULE_7_C_1 = "Table 7-C-1";
	private static final String RULE_7_C_13 = "Table 7-C-13";
	private static final String RULE_18_2 = "Table 18-2";
	private static final String RULE_18_18 = "Table 18-18";
//	private static final String RULE_36 = "36";
	private static final String RULE_37_TWO_A = "37-2-A";
//	private static final String RULE_37_TWO_B = "37-2-B";
//	private static final String RULE_37_TWO_C = "37-2-C";
//	private static final String RULE_37_TWO_D = "37-2-D";
//	private static final String RULE_37_TWO_G = "37-2-G";
//	private static final String RULE_37_TWO_H = "37-2-H";
//	private static final String RULE_37_TWO_I = "37-2-I";
	private static final String RULE_47 = "47";

	private static final String MINIMUMLABEL = "Minimum distance ";

	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_1 = BigDecimal.valueOf(1);
	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_1_5 = BigDecimal.valueOf(1.5);
	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_1_25 = BigDecimal.valueOf(1.25);
	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_2_25 = BigDecimal.valueOf(2.25); // New var added
	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_2_5 = BigDecimal.valueOf(2.5);
	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_3 = BigDecimal.valueOf(3);
	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_4 = BigDecimal.valueOf(4);
	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_4_5 = BigDecimal.valueOf(4.5);
	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_5 = BigDecimal.valueOf(5);
	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_6 = BigDecimal.valueOf(6);
	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_7_5 = BigDecimal.valueOf(7.5);
	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_9 = BigDecimal.valueOf(9);
	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_10 = BigDecimal.valueOf(10);
	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_12 = BigDecimal.valueOf(12);
	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_15 = BigDecimal.valueOf(15);
	private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_18 = BigDecimal.valueOf(18);
	public static final BigDecimal ROAD_WIDTH_TWELVE_POINTTWO = BigDecimal.valueOf(12.2);

	public static final String BSMT_FRONT_YARD_DESC = "Basement Front Yard";
	private static final int PLOTAREA_300 = 300;

	private class FrontYardResult {
		String rule;
		String subRule;
		String blockName;
		Integer level;
		BigDecimal actualMeanDistance = BigDecimal.ZERO;
		BigDecimal actualMinDistance = BigDecimal.ZERO;
		String occupancy;
		BigDecimal expectedminimumDistance = BigDecimal.ZERO;
		BigDecimal expectedmeanDistance = BigDecimal.ZERO;
		String additionalCondition;
		boolean status = false;
	}

	public void processFrontYard(Plan pl) {

		LOG.info("inside FrontYardService_Citya processFrontYard()");

		Plot plot = pl.getPlot();
		HashMap<String, String> errors = new HashMap<>();
		if (plot == null)
			return;
		// each blockwise, check height , floor area, buildup area. check most restricve
		// based on occupancy and front yard values
		// of occupancies.
		// If floor area less than 150 mt and occupancy type D, then consider as
		// commercial building.
		// In output show blockwise required and provided information.

		validateFrontYard(pl);

		if (plot != null && !pl.getBlocks().isEmpty()) {
			for (Block block : pl.getBlocks()) { // for each block

				ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
				scrutinyDetail.addColumnHeading(1, RULE_NO);
				scrutinyDetail.addColumnHeading(2, LEVEL);
				scrutinyDetail.addColumnHeading(3, OCCUPANCY);
				scrutinyDetail.addColumnHeading(4, FIELDVERIFIED);
				scrutinyDetail.addColumnHeading(5, PERMISSIBLE);
				scrutinyDetail.addColumnHeading(6, PROVIDED);
				scrutinyDetail.addColumnHeading(7, STATUS);
				scrutinyDetail.setHeading(FRONT_YARD_DESC);

				FrontYardResult frontYardResult = new FrontYardResult();

				for (SetBack setback : block.getSetBacks()) {
					BigDecimal min;
					BigDecimal mean;
					// consider height,floor area,buildup area, different occupancies of block
					// Get occupancies of perticular block and use the same.

					if (setback.getFrontYard() != null) {
						min = setback.getFrontYard().getMinimumDistance();
						mean = setback.getFrontYard().getMean();

						// added for front road reserve deduction from rear setback
						List<Road> roadReserve = pl.getRoadReserves();
						BigDecimal frontRoadReserve = BigDecimal.ZERO;
						boolean roadReservePresent = false;
						int frontRoadReserveIndex = -1;

						for (Road road : roadReserve) {
							frontRoadReserveIndex++;
							if (road.getName().equals("ROAD_RESERVE_FRONT")) {
								roadReservePresent = true;
								break;
							}
						}

						if (roadReservePresent) {
							frontRoadReserve = roadReserve.get(frontRoadReserveIndex).getShortestDistanceToRoad()
									.get(0);
						}

						if (frontRoadReserve.compareTo(BigDecimal.ZERO) >= 0) {
							min = min.subtract(frontRoadReserve);
						}
						// road reserve calculation end

						// if height defined at frontyard level, then use elase use buidling height.
						BigDecimal buildingHeight = setback.getFrontYard().getHeight() != null
								&& setback.getFrontYard().getHeight().compareTo(BigDecimal.ZERO) > 0
										? setback.getFrontYard().getHeight()
										: block.getBuilding().getBuildingHeight();

						if (buildingHeight != null && (min.doubleValue() > 0 || mean.doubleValue() > 0)) {
//							for (final Occupancy occupancy : block.getBuilding().getTotalArea()) {
							final Occupancy occupancy = block.getBuilding().getTotalArea().get(0);
							scrutinyDetail.setKey("Block_" + block.getName() + "_" + FRONT_YARD_DESC);

							/*
							 * if (setback.getLevel() < 0) { scrutinyDetail.setKey("Block_" +
							 * block.getName() + "_" + "Basement Front Yard"); checkFrontYardBasement(pl,
							 * block.getBuilding(), block.getName(), setback.getLevel(), plot,
							 * BSMT_FRONT_YARD_DESC, min, mean, occupancy.getTypeHelper(), frontYardResult);
							 * 
							 * }
							 */

//								if ((occupancy.getTypeHelper().getSubtype() != null
//										&& (A.equalsIgnoreCase(occupancy.getTypeHelper().getType().getCode())
//												|| A_R.equalsIgnoreCase(occupancy.getTypeHelper().getSubtype().getCode())
//												|| A_AF.equalsIgnoreCase(
//														occupancy.getTypeHelper().getSubtype().getCode())
//												|| A_PO.equalsIgnoreCase(
//														occupancy.getTypeHelper().getSubtype().getCode()))
//										|| F.equalsIgnoreCase(occupancy.getTypeHelper().getType().getCode()))) {
							if (occupancy.getTypeHelper().getType() != null
									&& (A.equalsIgnoreCase(occupancy.getTypeHelper().getType().getCode())
											|| F.equalsIgnoreCase(occupancy.getTypeHelper().getType().getCode()))) {
								checkFrontYard(pl, block.getBuilding(), block.getName(), setback.getLevel(), plot,
										FRONT_YARD_DESC, min, mean, occupancy.getTypeHelper(), frontYardResult, errors);

							} else if (G.equalsIgnoreCase(occupancy.getTypeHelper().getType().getCode())) {
								checkFrontYardForIndustrial(pl, block.getBuilding(), block.getName(),
										setback.getLevel(), plot, FRONT_YARD_DESC, min, mean, occupancy.getTypeHelper(),
										frontYardResult);
							} else if (occupancy.getTypeHelper().getType() != null
									&& J.equalsIgnoreCase(occupancy.getTypeHelper().getType().getCode())) {
								processFrontYardGovtOccupancies(pl, block.getBuilding(), block.getName(),
										setback.getLevel(), plot, FRONT_YARD_DESC, min, mean, occupancy.getTypeHelper(),
										frontYardResult);
							}

//							} // for end

							if (errors.isEmpty()) {
								Map<String, String> details = new HashMap<>();
								details.put(RULE_NO, frontYardResult.subRule);
								details.put(LEVEL,
										frontYardResult.level != null ? frontYardResult.level.toString() : "");
								details.put(OCCUPANCY, frontYardResult.occupancy);
								details.put(FIELDVERIFIED, MINIMUMLABEL);
								details.put(PERMISSIBLE, frontYardResult.expectedmeanDistance.toString());
								details.put(PROVIDED, frontYardResult.actualMeanDistance.toString());

								if (frontYardResult.status) {
									details.put(STATUS, Result.Accepted.getResultVal());
								} else {
									details.put(STATUS, Result.Not_Accepted.getResultVal());
								}
								scrutinyDetail.getDetail().add(details);
								pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
							}
						}
					}
				}
			}
		}
	}

	private void validateFrontYard(Plan pl) {

		// Front yard may not be mandatory at each level. We can check whether in any
		// level front yard defined or not ?

		for (Block block : pl.getBlocks()) {
			if (!block.getCompletelyExisting()) {
				Boolean frontYardDefined = false;
				for (SetBack setback : block.getSetBacks()) {
					if (setback.getFrontYard() != null
							&& setback.getFrontYard().getMean().compareTo(BigDecimal.valueOf(0)) > 0) {
						frontYardDefined = true;
					}
				}
				if (!frontYardDefined) {
					HashMap<String, String> errors = new HashMap<>();
					errors.put(FRONT_YARD_DESC,
							prepareMessage(OBJECTNOTDEFINED, FRONT_YARD_DESC + " for Block " + block.getName()));
					pl.addErrors(errors);
				}
			}

		}

	}

	private Boolean commercial(Integer level, String blockName, BigDecimal min, BigDecimal mean,
			OccupancyTypeHelper mostRestrictiveOccupancy, FrontYardResult frontYardResult, Boolean valid,
			String subRule, String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal depthOfPlot) {
		if (depthOfPlot.compareTo(BigDecimal.valueOf(6.10)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_1;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(6.10)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(9.15)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_1_25;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(9.15)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(12.2)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_4;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(12.2)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(18.3)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_4_5;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(18.3)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(24.38)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_5;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(24.38)) > 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_6;
		}

		/*
		 * if (-1 == level) { rule = BSMT_FRONT_YARD_DESC; subRuleDesc =
		 * SUB_RULE_24_12_DESCRIPTION; subRule = SUB_RULE_24_12; }
		 */

		valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);

		compareFrontYardResult(blockName, min, mean, mostRestrictiveOccupancy, frontYardResult, valid, subRule, rule,
				minVal, meanVal, level);
		return valid;
	}

	private Boolean checkFrontYard(Plan pl, Building building, String blockName, Integer level, Plot plot,
			String frontYardFieldName, BigDecimal min, BigDecimal mean, OccupancyTypeHelper mostRestrictiveOccupancy,
			FrontYardResult frontYardResult, HashMap<String, String> errors) {
		Boolean valid = false;
//		String subRule = RULE_7_C_1;
		String subRule = RULE_18_2;
		String rule = FRONT_YARD_DESC;
		BigDecimal minVal = BigDecimal.ZERO;
		BigDecimal meanVal = BigDecimal.ZERO;
		BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();
//		if (mostRestrictiveOccupancy.getSubtype() != null
//				&& (A.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())
//						|| A_R.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
//						|| A_AF.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
//						|| A_PO.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode()))) {
		if (A.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			if (pl.getPlanInformation() != null && pl.getPlanInformation().getRoadWidth() != null
					&& StringUtils.isNotBlank(pl.getPlanInformation().getLandUseZone())
					&& DxfFileConstants.COMMERCIAL.equalsIgnoreCase(pl.getPlanInformation().getLandUseZone())
//					&& pl.getPlanInformation().getRoadWidth().compareTo(ROAD_WIDTH_TWELVE_POINTTWO) < 0
			) {
				valid = commercial(level, blockName, min, mean, mostRestrictiveOccupancy, frontYardResult, valid,
						DxfFileConstants.RULE_28, rule, minVal, meanVal, depthOfPlot);
			} else {
				valid = residential(blockName, level, min, mean, mostRestrictiveOccupancy, frontYardResult, valid,
						subRule, rule, minVal, meanVal, depthOfPlot, errors, pl);
			}
		} else if (F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			valid = commercial(level, blockName, min, mean, mostRestrictiveOccupancy, frontYardResult, valid, subRule,
					rule, minVal, meanVal, depthOfPlot);
		}
		return valid;
	}

	private Boolean checkFrontYardBasement(Plan plan, Building building, String blockName, Integer level, Plot plot,
			String frontYardFieldName, BigDecimal min, BigDecimal mean, OccupancyTypeHelper mostRestrictiveOccupancy,
			FrontYardResult frontYardResult) {
		Boolean valid = false;
		String subRule = RULE_47;
		String rule = FRONT_YARD_DESC;
		BigDecimal minVal = BigDecimal.ZERO;
		BigDecimal meanVal = BigDecimal.ZERO;
		if ((mostRestrictiveOccupancy.getSubtype() != null
				&& A_R.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
				|| A_AF.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
				|| A_PO.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode()))
				|| F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			if (plot.getArea().compareTo(BigDecimal.valueOf(PLOTAREA_300)) <= 0) {
				minVal = FRONTYARDMINIMUM_DISTANCE_3;
				valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);
			}

			rule = BSMT_FRONT_YARD_DESC;

			compareFrontYardResult(blockName, min, mean, mostRestrictiveOccupancy, frontYardResult, valid, subRule,
					rule, minVal, meanVal, level);
		}
		return valid;
	}

	private Boolean checkFrontYardForIndustrial(Plan pl, Building building, String blockName, Integer level, Plot plot,
			String frontYardFieldName, BigDecimal min, BigDecimal mean, OccupancyTypeHelper mostRestrictiveOccupancy,
			FrontYardResult frontYardResult) {
		Boolean valid = false;
		String subRule = RULE_18_18;
		String rule = FRONT_YARD_DESC;
		BigDecimal minVal = BigDecimal.ZERO;
		BigDecimal meanVal = BigDecimal.ZERO;
		BigDecimal widthOfPlot = pl.getPlanInformation().getWidthOfPlot();
		valid = processFrontYardForIndustrial(blockName, level, min, mean, mostRestrictiveOccupancy, frontYardResult,
				valid, subRule, rule, minVal, meanVal, pl.getPlot().getArea(), widthOfPlot);
		return valid;
	}

	private Boolean processFrontYardGovtOccupancies(Plan pl, Building building, String blockName, Integer level,
			Plot plot, String frontYardFieldName, BigDecimal min, BigDecimal mean,
			OccupancyTypeHelper mostRestrictiveOccupancy, FrontYardResult frontYardResult) {
		Boolean valid = false;
		String subRule = RULE_18_2;
		String rule = FRONT_YARD_DESC;
		BigDecimal minVal = BigDecimal.ZERO;
		BigDecimal meanVal = BigDecimal.ZERO;
		BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();

		if (depthOfPlot.compareTo(BigDecimal.valueOf(9.15)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_1_5;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(9.15)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(12.2)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_2_5;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(12.2)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(18.3)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_3;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(18.3)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(24.38)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_4_5;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(24.38)) > 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_6;
		}

		valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);

		compareFrontYardResult(blockName, min, mean, mostRestrictiveOccupancy, frontYardResult, valid, subRule, rule,
				minVal, meanVal, level);
		return valid;
	}

	private void compareFrontYardResult(String blockName, BigDecimal min, BigDecimal mean,
			OccupancyTypeHelper mostRestrictiveOccupancy, FrontYardResult frontYardResult, Boolean valid,
			String subRule, String rule, BigDecimal minVal, BigDecimal meanVal, Integer level) {
		String occupancyName;
		if (mostRestrictiveOccupancy.getSubtype() != null)
			occupancyName = mostRestrictiveOccupancy.getSubtype().getName();
		else
			occupancyName = mostRestrictiveOccupancy.getType().getName();
		if (minVal.compareTo(frontYardResult.expectedminimumDistance) >= 0) {
			if (minVal.compareTo(frontYardResult.expectedminimumDistance) == 0) {
				frontYardResult.rule = frontYardResult.rule != null ? frontYardResult.rule + "," + rule : rule;
				frontYardResult.occupancy = frontYardResult.occupancy != null
						? frontYardResult.occupancy + "," + occupancyName
						: occupancyName;
			} else {
				frontYardResult.rule = rule;
				frontYardResult.occupancy = occupancyName;
			}

			frontYardResult.subRule = subRule;
			frontYardResult.blockName = blockName;
			frontYardResult.level = level;
			frontYardResult.expectedminimumDistance = minVal;
			frontYardResult.expectedmeanDistance = meanVal;
			frontYardResult.actualMinDistance = min;
			frontYardResult.actualMeanDistance = mean;
			frontYardResult.status = valid;

		}
	}

	private Boolean residential(String blockName, Integer level, BigDecimal min, BigDecimal mean,
			OccupancyTypeHelper mostRestrictiveOccupancy, FrontYardResult frontYardResult, Boolean valid,
			String subRule, String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal depthOfPlot,
			HashMap<String, String> errors, Plan pl) {
		if (depthOfPlot.compareTo(BigDecimal.valueOf(6.10)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_1;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(6.10)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(9.15)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_1_25;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(9.15)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(12.2)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_2_5;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(12.2)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(18.3)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_3;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(18.3)) > 0
				&& depthOfPlot.compareTo(BigDecimal.valueOf(24.38)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_4;
		} else if (depthOfPlot.compareTo(BigDecimal.valueOf(24.38)) > 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_5;
		}

		/*
		 * if (-1 == level) { rule = BSMT_FRONT_YARD_DESC; subRuleDesc =
		 * SUB_RULE_24_12_DESCRIPTION; subRule = SUB_RULE_24_12; }
		 */

		valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);

		compareFrontYardResult(blockName, min, mean, mostRestrictiveOccupancy, frontYardResult, valid, subRule, rule,
				minVal, meanVal, level);
		return valid;
	}

	private Boolean processFrontYardForIndustrial(String blockName, Integer level, BigDecimal min, BigDecimal mean,
			OccupancyTypeHelper mostRestrictiveOccupancy, FrontYardResult frontYardResult, Boolean valid,
			String subRule, String rule, BigDecimal minVal, BigDecimal meanVal, BigDecimal plotArea,
			BigDecimal widthOfPlot) {
		if (plotArea.compareTo(BigDecimal.valueOf(500)) < 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_3;
		} else if (plotArea.compareTo(BigDecimal.valueOf(500)) > 0
				&& plotArea.compareTo(BigDecimal.valueOf(1000)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_4_5;
		} else if (plotArea.compareTo(BigDecimal.valueOf(1000)) > 0
				&& plotArea.compareTo(BigDecimal.valueOf(2000)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_6;
		} else if (plotArea.compareTo(BigDecimal.valueOf(2000)) > 0
				&& plotArea.compareTo(BigDecimal.valueOf(5000)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_7_5;
		} else if (plotArea.compareTo(BigDecimal.valueOf(5000)) > 0
				&& plotArea.compareTo(BigDecimal.valueOf(10000)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_10;
		} else if (plotArea.compareTo(BigDecimal.valueOf(10000)) > 0
				&& plotArea.compareTo(BigDecimal.valueOf(20000)) <= 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_12;
		} else if (plotArea.compareTo(BigDecimal.valueOf(20000)) > 0) {
			meanVal = FRONTYARDMINIMUM_DISTANCE_18;

		}

		/*
		 * if (-1 == level) { rule = BSMT_FRONT_YARD_DESC; subRuleDesc =
		 * SUB_RULE_24_12_DESCRIPTION; subRule = SUB_RULE_24_12; }
		 */
		valid = validateMinimumAndMeanValue(min, mean, minVal, meanVal);

		compareFrontYardResult(blockName, min, mean, mostRestrictiveOccupancy, frontYardResult, valid, subRule, rule,
				minVal, meanVal, level);
		return valid;
	}

	private Boolean validateMinimumAndMeanValue(BigDecimal min, BigDecimal mean, BigDecimal minval,
			BigDecimal meanval) {
		Boolean valid = false;
		if (min.compareTo(minval) >= 0 && mean.compareTo(meanval) >= 0) {
			valid = true;
		}
		return valid;
	}
}
