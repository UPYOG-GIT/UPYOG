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
import static org.egov.edcr.constants.DxfFileConstants.A_AF;
import static org.egov.edcr.constants.DxfFileConstants.A_R;
import static org.egov.edcr.constants.DxfFileConstants.B;
import static org.egov.edcr.constants.DxfFileConstants.C_MPBLC;
import static org.egov.edcr.constants.DxfFileConstants.C_MPVT;
import static org.egov.edcr.constants.DxfFileConstants.D_MHCT;
import static org.egov.edcr.constants.DxfFileConstants.E;
import static org.egov.edcr.constants.DxfFileConstants.F;
import static org.egov.edcr.constants.DxfFileConstants.G;
import static org.egov.edcr.constants.DxfFileConstants.H;
import static org.egov.edcr.constants.DxfFileConstants.J;
import static org.egov.edcr.constants.DxfFileConstants.PARKING_SLOT;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.constants.DxfFileConstants.S_MCH;
import static org.egov.edcr.utility.DcrConstants.ROUNDMODE_MEASUREMENTS;
import static org.egov.edcr.utility.DcrConstants.SQMTRS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.Occupancy;
import org.egov.common.entity.edcr.OccupancyType;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.ParkingDetails;
import org.egov.common.entity.edcr.ParkingHelper;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.service.EdcrRestService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.jfree.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Parking_Demo extends FeatureProcess {

	private static final Logger LOGGER = LogManager.getLogger(Parking_Demo.class);

	@Autowired
	EdcrRestService edcrRestService;

	private static final String OUT_OF = "Out of ";
	private static final String SLOT_HAVING_GT_4_PTS = " number of polygon not having only 4 points.";
//	private static final String LOADING_UNLOADING_DESC = "Minimum required Loading/Unloading area";
	private static final String MINIMUM_AREA_OF_EACH_DA_PARKING = " Minimum width of Each Special parking";
	private static final String SP_PARKING_SLOT_AREA = "Special Parking Area";
	private static final String NO_VIOLATION_OF_AREA = "No violation of area in ";
	private static final String MIN_AREA_EACH_CAR_PARKING = " Minimum Area of Each ECS parking";
	private static final String PARKING_VIOLATED_MINIMUM_AREA = " parking violated minimum area.";
	private static final String PARKING = " parking ";
	private static final String NUMBERS = " Numbers ";
	private static final String MECHANICAL_PARKING = "Mechanical parking";
//	private static final String MAX_ALLOWED_MECH_PARK = "Maximum allowed mechanical parking";
	private static final String TWO_WHEELER_PARK_AREA = "Two Wheeler Parking Area";
	private static final String LOADING_UNLOADING_AREA = "Loading Unloading Area";
	private static final String SP_PARKING = "Special parking";
	private static final String SUB_RULE_34_1_DESCRIPTION = "Parking Slots Area";
	private static final String RULE_81 = "81";
	private static final String RULE_PODIUM = "72-a";
	private static final String SUB_RULE_34_2 = "34-2";
	private static final String SUB_RULE_40_8 = "40-8";
	private static final String SUB_RULE_40_11 = "40-11";
	private static final String PARKING_MIN_AREA = "5 M x 2 M";
	private static final double PARKING_SLOT_WIDTH = 2;
	private static final double PARKING_SLOT_HEIGHT = 5;
	private static final double SP_PARK_SLOT_MIN_SIDE = 3.6;
	private static final String DA_PARKING_MIN_AREA = " 3.60 M ";
	public static final String NO_OF_UNITS = "No of apartment units";
	private static final double TWO_WHEEL_PARKING_AREA_WIDTH = 1.5;
	private static final double TWO_WHEEL_PARKING_AREA_HEIGHT = 2.0;
	private static final double MECH_PARKING_WIDTH = 2.7;
	private static final double MECH_PARKING_HEIGHT = 5.5;

	private static final double OPEN_ECS = 25;
	private static final double STILT_ECS = 30;
	private static final double LGF_ECS = 30;
	private static final double COVER_ECS = 30;
	private static final double BSMNT_ECS = 35;
//	private static final double PARK_A = 0.25;
//	private static final double PARK_F = 0.30;
//	private static final double PARK_VISITOR = 0.15;
	private static final String SUB_RULE_40 = "40";
//	private static final String SUB_RULE_40_2 = "40-2";
//	private static final String SUB_RULE_40_2_DESCRIPTION = "Parking space";
	private static final String RULE_81_DESCRIPTION = "Parking space";
//	private static final String SUB_RULE_40_10 = "40-10";
//	private static final String SUB_RULE_40_10_DESCRIPTION = "Visitor’s Parking";
	public static final String OPEN_PARKING_DIM_DESC = "Open parking ECS dimension ";
	public static final String COVER_PARKING_DIM_DESC = "Cover parking ECS dimension ";
	public static final String BSMNT_PARKING_DIM_DESC = "Basement parking ECS dimension ";
	public static final String VISITOR_PARKING = "Visitor parking";
	public static final String SPECIAL_PARKING_DIM_DESC = "Special parking ECS dimension ";
	public static final String TWO_WHEELER_DIM_DESC = "Two wheeler parking dimension ";
	public static final String MECH_PARKING_DESC = "Mechanical parking dimension ";
	public static final String MECH_PARKING_DIM_DESC = "All Mechanical parking polylines should have dimension 2.7*5.5 m²";
	public static final String MECH_PARKING_DIM_DESC_NA = " mechanical parking polyines does not have dimensions 2.7*5.5 m²";

	private static final String PARKING_VIOLATED_DIM = " parking violated dimension.";
	private static final String PARKING_AREA_DIM = "1.5 M x 2 M";
	private static final String PARKING_DETAILS_DESCRIPTION = "Parking Area provided in m²";

	@Override
	public Plan validate(Plan pl) {
		// validateDimensions(pl);
		return pl;
	}

	@Override
	public Plan process(Plan pl) {
		LOGGER.info("inside process()");
		validate(pl);
		scrutinyDetail = new ScrutinyDetail();
		scrutinyDetail.setKey("Common_Parking");
		scrutinyDetail.addColumnHeading(1, RULE_NO);
		scrutinyDetail.addColumnHeading(2, DESCRIPTION);
		scrutinyDetail.addColumnHeading(3, REQUIRED);
		scrutinyDetail.addColumnHeading(4, PROVIDED);
		scrutinyDetail.addColumnHeading(5, STATUS);
		processParking(pl);
		// processMechanicalParking(pl);
		return pl;
	}

	private void validateDimensions(Plan pl) {
		ParkingDetails parkDtls = pl.getParkingDetails();
		if (!parkDtls.getCars().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getCars())
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					count++;
			if (count > 0)
				pl.addError(PARKING_SLOT, PARKING_SLOT + count + SLOT_HAVING_GT_4_PTS);
		}

		if (!parkDtls.getOpenCars().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getOpenCars())
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					count++;
			if (count > 0)
				pl.addError(OPEN_PARKING_DIM_DESC, OPEN_PARKING_DIM_DESC + count + SLOT_HAVING_GT_4_PTS);
		}

		if (!parkDtls.getCoverCars().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getCoverCars())
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					count++;
			if (count > 0)
				pl.addError(COVER_PARKING_DIM_DESC, COVER_PARKING_DIM_DESC + count + SLOT_HAVING_GT_4_PTS);
		}

		if (!parkDtls.getCoverCars().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getBasementCars())
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					count++;
			if (count > 0)
				pl.addError(BSMNT_PARKING_DIM_DESC, BSMNT_PARKING_DIM_DESC + count + SLOT_HAVING_GT_4_PTS);
		}

		if (!parkDtls.getSpecial().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getDisabledPersons())
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					count++;
			if (count > 0)
				pl.addError(SPECIAL_PARKING_DIM_DESC, SPECIAL_PARKING_DIM_DESC + count
						+ " number of DA Parking slot polygon not having only 4 points.");
		}

		if (!parkDtls.getLoadUnload().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getLoadUnload())
				if (m.getArea().compareTo(BigDecimal.valueOf(30)) < 0)
					count++;
			if (count > 0)
				pl.addError("load unload", count + " loading unloading parking spaces doesnt contain minimum of 30m2");
		}

		if (!parkDtls.getMechParking().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getMechParking())
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					count++;
			if (count > 0)
				pl.addError(MECHANICAL_PARKING,
						count + " number of Mechanical parking slot polygon not having only 4 points.");
		}

		if (!parkDtls.getTwoWheelers().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getTwoWheelers())
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					count++;
			if (count > 0)
				pl.addError(TWO_WHEELER_DIM_DESC, TWO_WHEELER_DIM_DESC + count
						+ " number of two wheeler Parking slot polygon not having only 4 points.");
		}
	}

	public void processParking(Plan pl) {
		LOGGER.info("inside processParking()");

		ScrutinyDetail scrutinyDetail1 = new ScrutinyDetail();
		scrutinyDetail1.setKey("Common_Podium Parking");
		scrutinyDetail1.addColumnHeading(1, RULE_NO);
		scrutinyDetail1.addColumnHeading(2, DESCRIPTION);
		scrutinyDetail1.addColumnHeading(3, PROVIDED);
		scrutinyDetail1.addColumnHeading(4, STATUS);

		ParkingHelper helper = new ParkingHelper();
		// checkDimensionForCarParking(pl, helper);

//		List<Occupancy> occ= pl.getOccupancies();

//		OccupancyTypeHelper mostRestrictiveOccupancy = pl.getVirtualBuilding() != null
//				? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
//				: null;
		BigDecimal totalBuiltupArea = pl.getOccupancies().stream().map(Occupancy::getBuiltUpArea)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal coverParkingArea = BigDecimal.ZERO;
		BigDecimal stiltParkingArea = BigDecimal.ZERO;
//		BigDecimal basementParkingArea = BigDecimal.ZERO;
		BigDecimal noOfBeds = BigDecimal.ZERO;
		Integer noOfSeats = 0;
		Double requiredCarParkArea = 0d;
		Double requiredVisitorParkArea = 0d;

		ArrayList<Map<String, Object>> edcrRuleList = pl.getEdcrRuleList();

		BigDecimal openParkingArea = pl.getParkingDetails().getOpenCars().stream().map(Measurement::getArea)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

//		BigDecimal stiltParkingArea = pl.getParkingDetails().getStilts().stream().map(Measurement::getArea)
//				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal lowerGroungFloorParkingArea = pl.getParkingDetails().getLowerGroundFloor().stream()
				.map(Measurement::getArea).reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal basementParkingArea = pl.getParkingDetails().getBasementCars().stream().map(Measurement::getArea)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal podiumParkingArea = pl.getParkingDetails().getPodium().stream().map(Measurement::getArea)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		noOfBeds = pl.getPlanInformation().getNoOfBeds();
		noOfSeats = pl.getPlanInformation().getNoOfSeats();

		OccupancyTypeHelper occupancyTypeHelper = null;
		for (Block block : pl.getBlocks()) {
			String blockNumber = block.getNumber();

			for (final Occupancy occupancy : block.getBuilding().getTotalArea()) {
				occupancyTypeHelper = occupancy.getTypeHelper();
				break;
			}

//			for (Floor floor : block.getBuilding().getFloors()) {
//				coverParkingArea = coverParkingArea.add(floor.getParking().getCoverCars().stream()
//						.map(Measurement::getArea).reduce(BigDecimal.ZERO, BigDecimal::add));
//			}

			/*
			 * if (occupancyTypeHelper == null || (occupancyTypeHelper != null &&
			 * (occupancyTypeHelper.getType() == null || occupancyTypeHelper.getSubtype() ==
			 * null))) { Log.error("Occupany not defined properly"); pl.addError(OCCUPANCY,
			 * getLocaleMessage(OBJECTNOTDEFINED, OCCUPANCY + " not properly defined")); }
			 * else { BigDecimal floorBuiltUpArea =
			 * block.getBuilding().getTotalBuitUpArea();
			 * 
			 * requiredCarParkArea += getRequiredCarParkArea(floorBuiltUpArea,
			 * occupancyTypeHelper, coverParkingArea, basementParkingArea, openParkingArea,
			 * stiltParkingArea, lowerGroungFloorParkingArea); }
			 */
			BigDecimal builupArea = block.getBuilding().getTotalBuitUpArea();
			if (builupArea.compareTo(BigDecimal.valueOf((50))) <= 0) {

			} else {

				for (Floor floor : block.getBuilding().getFloors()) {
					coverParkingArea = coverParkingArea.add(floor.getParking().getCoverCars().stream()
							.map(Measurement::getArea).reduce(BigDecimal.ZERO, BigDecimal::add)); //
					stiltParkingArea = stiltParkingArea.add(floor.getParking().getStilts().stream()
							.map(Measurement::getArea).reduce(BigDecimal.ZERO, BigDecimal::add));
//				basementParkingArea = basementParkingArea.add(floor.getParking().getBasementCars().stream() //
//						.map(Measurement::getArea).reduce(BigDecimal.ZERO, BigDecimal::add));

					/*
					 * occupancyTypeHelper = floor.getOccupancies().get(0).getTypeHelper(); if
					 * (occupancyTypeHelper == null || (occupancyTypeHelper != null &&
					 * (occupancyTypeHelper.getType() == null || occupancyTypeHelper.getSubtype() ==
					 * null))) { Log.error("Occupany not defined properly"); pl.addError(OCCUPANCY,
					 * getLocaleMessage(OBJECTNOTDEFINED, OCCUPANCY + " not properly defined")); }
					 * else { BigDecimal floorBuiltUpArea =
					 * floor.getOccupancies().get(0).getBuiltUpArea();
					 * 
					 * requiredCarParkArea += getRequiredCarParkArea(floorBuiltUpArea,
					 * occupancyTypeHelper, coverParkingArea, basementParkingArea, openParkingArea,
					 * stiltParkingArea, lowerGroungFloorParkingArea, edcrRuleList); }
					 */

				}
				if (occupancyTypeHelper == null || (occupancyTypeHelper != null
						&& (occupancyTypeHelper.getType() == null || occupancyTypeHelper.getSubtype() == null))) {
					Log.error("Occupany not defined properly");
					pl.addError(OCCUPANCY, getLocaleMessage(OBJECTNOTDEFINED, OCCUPANCY + " not properly defined"));
				} else {

					requiredCarParkArea += getRequiredCarParkArea(builupArea, occupancyTypeHelper,
							coverParkingArea, basementParkingArea, openParkingArea, stiltParkingArea,
							lowerGroungFloorParkingArea, edcrRuleList);
				}
			}
		}

		if (occupancyTypeHelper != null
				&& (occupancyTypeHelper.getType() != null || occupancyTypeHelper.getSubtype() != null)) {

			if (podiumParkingArea.compareTo(BigDecimal.ZERO) == 0) {
				Map<String, String> details = new HashMap<>();
				details.put(RULE_NO, RULE_PODIUM);
				details.put(DESCRIPTION, "Podium Parking");
				details.put(PROVIDED, "Podium Parking not Provided");
				details.put(STATUS, "");
				scrutinyDetail1.getDetail().add(details);
				pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail1);

			} else {
				Map<String, String> details = new HashMap<>();
				details.put(RULE_NO, RULE_PODIUM);
				details.put(DESCRIPTION, "Podium Parking");
				details.put(PROVIDED, podiumParkingArea.toString());
				details.put(STATUS, "");
				scrutinyDetail1.getDetail().add(details);
				pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail1);
			}

			/*
			 * ScrutinyDetail parkingScrutinyDetail = new ScrutinyDetail();
			 * parkingScrutinyDetail.setKey("Common_Parking Details");
			 * parkingScrutinyDetail.addColumnHeading(1, "Open Parking");
			 * parkingScrutinyDetail.addColumnHeading(2, "Stilt Parking");
			 * parkingScrutinyDetail.addColumnHeading(3, "Lower Ground Floor Parking");
			 * parkingScrutinyDetail.addColumnHeading(4, "Basement Parking");
			 * parkingScrutinyDetail.addColumnHeading(5, STATUS);
			 * 
			 * Map<String, String> parkingDetails = new HashMap<>();
			 * parkingDetails.put("Open Parking", openParkingArea.setScale(2,
			 * ROUNDMODE_MEASUREMENTS).toString()); parkingDetails.put("Stilt Parking",
			 * stiltParkingArea.setScale(2, ROUNDMODE_MEASUREMENTS).toString());
			 * parkingDetails.put("Lower Ground Floor Parking",
			 * lowerGroungFloorParkingArea.setScale(2, ROUNDMODE_MEASUREMENTS).toString());
			 * parkingDetails.put("Basement Parking", basementParkingArea.setScale(2,
			 * ROUNDMODE_MEASUREMENTS).toString()); parkingDetails.put(STATUS, "");
			 * parkingScrutinyDetail.getDetail().add(parkingDetails);
			 * pl.getReportOutput().getScrutinyDetails().add(parkingScrutinyDetail);
			 */
			
			setReportOutputParkingDetails(pl, openParkingArea, stiltParkingArea, lowerGroungFloorParkingArea,
					basementParkingArea);

			BigDecimal totalProvidedCarParkArea = openParkingArea.add(coverParkingArea).add(basementParkingArea)
					.add(stiltParkingArea).add(lowerGroungFloorParkingArea);
//		helper.totalRequiredCarParking += openParkingArea.doubleValue() / OPEN_ECS;
//		helper.totalRequiredCarParking += coverParkingArea.doubleValue() / COVER_ECS;
//		helper.totalRequiredCarParking += basementParkingArea.doubleValue() / BSMNT_ECS;

			BigDecimal providedVisitorParkArea = BigDecimal.ZERO;

//		String subOcc = mostRestrictiveOccupancy.getSubtype().getCode();
//		validateSpecialParking(pl, helper, totalBuiltupArea);

//		requiredCarParkArea = getRequiredCarParkArea(totalBuiltupArea, mostRestrictiveOccupancy, coverParkingArea,
//				basementParkingArea, openParkingArea, stiltParkingArea, lowerGroungFloorParkingArea);

			// logics deleted from here

			BigDecimal requiredCarParkingArea = Util.roundOffTwoDecimal(BigDecimal.valueOf(requiredCarParkArea));
			BigDecimal totalProvidedCarParkingArea = Util.roundOffTwoDecimal(totalProvidedCarParkArea);
			BigDecimal requiredVisitorParkingArea = Util
					.roundOffTwoDecimal(BigDecimal.valueOf(requiredVisitorParkArea));
			BigDecimal providedVisitorParkingArea = Util.roundOffTwoDecimal(providedVisitorParkArea);

			// checkDimensionForTwoWheelerParking(pl, helper);
			// checkAreaForLoadUnloadSpaces(pl);
			boolean isSingleFamilyRes = false;
			if ((occupancyTypeHelper != null && occupancyTypeHelper.getSubtype() != null
					&& A_R.equals(occupancyTypeHelper.getSubtype().getCode()))
					|| totalBuiltupArea.compareTo(BigDecimal.valueOf((50))) <= 0) {
				isSingleFamilyRes = true;
			}
			if (totalProvidedCarParkArea.doubleValue() == 0 && !isSingleFamilyRes
//				&& 
//				(mostRestrictiveOccupancy != null && 
//				!A_R.equals(mostRestrictiveOccupancy.getSubtype().getCode())
//				)
			) {
				pl.addError(RULE_81_DESCRIPTION, getLocaleMessage("msg.error.not.defined", RULE_81_DESCRIPTION));
			} else if (requiredCarParkArea > 0 && totalProvidedCarParkingArea.compareTo(requiredCarParkingArea) < 0) {
				setReportOutputDetails(pl, RULE_81, RULE_81_DESCRIPTION, requiredCarParkingArea + SQMTRS,
						totalProvidedCarParkingArea + SQMTRS, Result.Not_Accepted.getResultVal());
			} else {
				setReportOutputDetails(pl, RULE_81, RULE_81_DESCRIPTION, requiredCarParkingArea + SQMTRS,
						totalProvidedCarParkingArea + SQMTRS, Result.Accepted.getResultVal());
			}
			if (requiredVisitorParkArea > 0 && providedVisitorParkArea.compareTo(requiredVisitorParkingArea) < 0) {
				setReportOutputDetails(pl, RULE_81, RULE_81_DESCRIPTION, requiredVisitorParkingArea + SQMTRS,
						providedVisitorParkArea + SQMTRS, Result.Not_Accepted.getResultVal());
			} else if (requiredVisitorParkArea > 0) {
				setReportOutputDetails(pl, RULE_81, RULE_81_DESCRIPTION, requiredVisitorParkingArea + SQMTRS,
						providedVisitorParkingArea + SQMTRS, Result.Accepted.getResultVal());
			}
		} else {
			pl.addError(OCCUPANCY, getLocaleMessage(OBJECTNOTDEFINED, OCCUPANCY + " not properly defined"));
		}

		LOGGER.info("******************Require no of Car Parking***************" + helper.totalRequiredCarParking);
	}

	// for getting Car Park Area,(Population between 50K-200K). In future if
	// required for more population category then will create same method
	// signature and create conditions according requirement
	private Double getRequiredCarParkArea(BigDecimal totalBuiltupArea, OccupancyTypeHelper mostRestrictiveOccupancy,
			BigDecimal coverParkingArea, BigDecimal basementParkingArea, BigDecimal openParkingArea,
			BigDecimal stiltParkingArea, BigDecimal lowerGroungFloorParkingArea,
			ArrayList<Map<String, Object>> edcrRuleList) {
		LOGGER.info("inside getRequiredCarParkArea()");
		Double requiredCarParkArea = 0d;
		String occupancyName = "";
		String subOccupancyName = null;
		String featureName = "Parking";

//		String occ=mostRestrictiveOccupancy.getType().getCode();
//		String subOcc=mostRestrictiveOccupancy.getSubtype().getCode();
//		BigDecimal totalProvidedCarParkArea = openParkingArea.add(coverParkingArea).add(basementParkingArea);
		double requiredEcs = 0d;
		if (mostRestrictiveOccupancy != null && A.equals(mostRestrictiveOccupancy.getType().getCode())
				&& A_AF.equals(mostRestrictiveOccupancy.getSubtype().getCode())) {
			// multi family residential
			occupancyName = "Residential";
			subOccupancyName = "Apartment/Flat";
//			requiredEcs = totalBuiltupArea.doubleValue() / 150;
		} else if (mostRestrictiveOccupancy != null && A.equals(mostRestrictiveOccupancy.getType().getCode())
				&& A_R.equals(mostRestrictiveOccupancy.getSubtype().getCode())) {
			// multi family residential
			occupancyName = "Residential";
			subOccupancyName = "Residential Single Unit";
//			requiredEcs = totalBuiltupArea.doubleValue() / 150;
		} else if (mostRestrictiveOccupancy != null && B.equals(mostRestrictiveOccupancy.getType().getCode())) {
			// Academic
			occupancyName = "Educational";
//			requiredEcs = totalBuiltupArea.doubleValue() / 50;
		} else if (mostRestrictiveOccupancy != null && F.equals(mostRestrictiveOccupancy.getType().getCode())) {
			// Commercial (F) and Private Business Office (E)
			occupancyName = "Mercantile / Commercial";
//			requiredEcs = totalBuiltupArea.doubleValue() / 100;
		} else if (mostRestrictiveOccupancy != null && E.equals(mostRestrictiveOccupancy.getType().getCode())) {
			// Commercial (F) and Private Business Office (E)
			occupancyName = "Office/Business";
//			requiredEcs = totalBuiltupArea.doubleValue() / 100;
		} else if (mostRestrictiveOccupancy != null && G.equals(mostRestrictiveOccupancy.getType().getCode())) {
			// Industrial
			occupancyName = "Industrial";
//			requiredEcs = totalBuiltupArea.doubleValue() / 300;
		} else if (mostRestrictiveOccupancy != null && H.equals(mostRestrictiveOccupancy.getType().getCode())) {
			// Inventory (Storage)
			occupancyName = "Storage";
//			requiredEcs = totalBuiltupArea.doubleValue() / 200;
		} else if (mostRestrictiveOccupancy != null && J.equals(mostRestrictiveOccupancy.getType().getCode())) {
			// Government/Semi-Government
			occupancyName = "Government/Semi Goverment";
//			requiredEcs = totalBuiltupArea.doubleValue() / 150;
		} else if (mostRestrictiveOccupancy != null && S_MCH.equals(mostRestrictiveOccupancy.getSubtype().getCode())) {
			// Community Hall
			occupancyName = "Socio";
			subOccupancyName = "Multipurpose Community Hall";
//			requiredEcs = totalBuiltupArea.doubleValue() / 100;
		} else if (mostRestrictiveOccupancy != null && C_MPVT.equals(mostRestrictiveOccupancy.getSubtype().getCode())) {
			// Medical Private
			occupancyName = "Medical/Hospital";
			subOccupancyName = "Medical Private";
//			requiredEcs = totalBuiltupArea.doubleValue() / 10;
		} else if (mostRestrictiveOccupancy != null
				&& C_MPBLC.equals(mostRestrictiveOccupancy.getSubtype().getCode())) {
			// Medical Public
			occupancyName = "Medical/Hospital";
			subOccupancyName = "Medical Public";
//			requiredEcs = totalBuiltupArea.doubleValue() / 15;
		} else if (mostRestrictiveOccupancy != null && D_MHCT.equals(mostRestrictiveOccupancy.getSubtype().getCode())) {
			// Meeting Hall/Cinema Theater
			occupancyName = "Assembly";
			subOccupancyName = "Meeting Hall/Cinema Theater";
//			requiredEcs = totalBuiltupArea.doubleValue() / 40;
		}

		Map<String, Object> params = new HashMap<>();
		params.put("feature", featureName);
		params.put("occupancy", occupancyName);
		if (!subOccupancyName.equals("")) {
			params.put("subOccupancy", subOccupancyName);
		}

		ArrayList<String> valueFromColumn = new ArrayList<>();
		valueFromColumn.add("permissibleValue");

		List<Map<String, Object>> permissibleValue = new ArrayList<>();

		permissibleValue = edcrRestService.getPermissibleValue1(edcrRuleList, params, valueFromColumn);
		LOGGER.info("permissibleValue" + permissibleValue);

		if (!permissibleValue.isEmpty() && permissibleValue.get(0).containsKey("permissibleValue")) {
			requiredEcs = Double.valueOf(permissibleValue.get(0).get("permissibleValue").toString());
		}

		requiredEcs = BigDecimal.valueOf(requiredEcs).setScale(0, RoundingMode.UP).doubleValue();

		if (openParkingArea.doubleValue() > 0 && coverParkingArea.doubleValue() > 0)
			requiredCarParkArea += COVER_ECS * requiredEcs;
		else if (openParkingArea.doubleValue() > 0 && basementParkingArea.doubleValue() > 0)
			requiredCarParkArea += BSMNT_ECS * requiredEcs;
		else if (coverParkingArea.doubleValue() > 0 && basementParkingArea.doubleValue() > 0)
			requiredCarParkArea += BSMNT_ECS * requiredEcs;
		else if (basementParkingArea.doubleValue() > 0)
			requiredCarParkArea += BSMNT_ECS * requiredEcs;
		else if (coverParkingArea.doubleValue() > 0)
			requiredCarParkArea += COVER_ECS * requiredEcs;
		else if (stiltParkingArea.doubleValue() > 0)
			requiredCarParkArea += STILT_ECS * requiredEcs;
		else if (lowerGroungFloorParkingArea.doubleValue() > 0)
			requiredCarParkArea += LGF_ECS * requiredEcs;
		else if (openParkingArea.doubleValue() > 0)
			requiredCarParkArea += OPEN_ECS * requiredEcs;

//		if (coverParkingArea.doubleValue() > 0)
//			requiredCarParkArea += COVER_ECS * requiredEcs;

//		if (basementParkingArea.doubleValue() > 0)
//			requiredCarParkArea += BSMNT_ECS * requiredEcs;
//		if (openParkingArea.doubleValue() > 0)
//			requiredCarParkArea += OPEN_ECS * requiredEcs;
//		if (stiltParkingArea.doubleValue() > 0)
//			requiredCarParkArea += STILT_ECS * requiredEcs;
		LOGGER.info("return from getRequiredCarParkArea()");
		return requiredCarParkArea;
	}

	private void setReportOutputDetails(Plan pl, String ruleNo, String ruleDesc, String expected, String actual,
			String status) {
		Map<String, String> details = new HashMap<>();
		details.put(RULE_NO, ruleNo);
		details.put(DESCRIPTION, ruleDesc);
		details.put(REQUIRED, expected);
		details.put(PROVIDED, actual);
		details.put(STATUS, status);
		scrutinyDetail.getDetail().add(details);
		pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
	}

	private void setReportOutputParkingDetails(Plan pl, BigDecimal openParkingArea, BigDecimal stiltParkingArea,
			BigDecimal lowerGroungFloorParkingArea, BigDecimal basementParkingArea) {
		ScrutinyDetail parkingScrutinyDetail = new ScrutinyDetail();
		parkingScrutinyDetail.setKey("Common_Parking Details");
		parkingScrutinyDetail.addColumnHeading(1, PARKING_TYPE);
		parkingScrutinyDetail.addColumnHeading(2, DESCRIPTION);
		parkingScrutinyDetail.addColumnHeading(3, PROVIDED);
		parkingScrutinyDetail.addColumnHeading(4, STATUS);

		
		Map<String, String> openParkingDetails = new HashMap<>();
		openParkingDetails.put(PARKING_TYPE, "Open Parking");
		openParkingDetails.put(DESCRIPTION, PARKING_DETAILS_DESCRIPTION);
		openParkingDetails.put(PROVIDED, openParkingArea.setScale(2, ROUNDMODE_MEASUREMENTS).toString());
		openParkingDetails.put(STATUS, "");
		parkingScrutinyDetail.getDetail().add(openParkingDetails);
		
		Map<String, String> stiltParkingDetails = new HashMap<>();
		stiltParkingDetails.put(PARKING_TYPE, "Stilt Parking");
		stiltParkingDetails.put(DESCRIPTION, PARKING_DETAILS_DESCRIPTION);
		stiltParkingDetails.put(PROVIDED, stiltParkingArea.setScale(2, ROUNDMODE_MEASUREMENTS).toString());
		stiltParkingDetails.put(STATUS, "");
		parkingScrutinyDetail.getDetail().add(stiltParkingDetails);
		
		Map<String, String> lowerParkingDetails = new HashMap<>();
		lowerParkingDetails.put(PARKING_TYPE, "Lower Ground Floor Parking");
		lowerParkingDetails.put(DESCRIPTION, PARKING_DETAILS_DESCRIPTION);
		lowerParkingDetails.put(PROVIDED, lowerGroungFloorParkingArea.setScale(2, ROUNDMODE_MEASUREMENTS).toString());
		lowerParkingDetails.put(STATUS, "");
		parkingScrutinyDetail.getDetail().add(lowerParkingDetails);
		
		Map<String, String> basementParkingDetails = new HashMap<>();
		basementParkingDetails.put(PARKING_TYPE, "Basement Parking");
		basementParkingDetails.put(DESCRIPTION, PARKING_DETAILS_DESCRIPTION);
		basementParkingDetails.put(PROVIDED, basementParkingArea.setScale(2, ROUNDMODE_MEASUREMENTS).toString());
		basementParkingDetails.put(STATUS, "");
		parkingScrutinyDetail.getDetail().add(basementParkingDetails);
		
		pl.getReportOutput().getScrutinyDetails().add(parkingScrutinyDetail);
	}

	private void validateSpecialParking(Plan pl, ParkingHelper helper, BigDecimal totalBuiltupArea) {
		BigDecimal maxHeightOfBuilding = BigDecimal.ZERO;
		int failedCount = 0;
		int success = 0;
		if (!pl.getParkingDetails().getSpecial().isEmpty()) {
			for (Measurement m : pl.getParkingDetails().getSpecial()) {
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					failedCount++;
				else
					success++;
			}
			if (failedCount > 0)
				pl.addError(SPECIAL_PARKING_DIM_DESC,
						SPECIAL_PARKING_DIM_DESC + failedCount + " number not having only 4 points.");
			pl.getParkingDetails().setValidSpecialSlots(success);
		}

		for (Block block : pl.getBlocks()) {
			if (block.getBuilding().getBuildingHeight() != null
					&& block.getBuilding().getBuildingHeight().compareTo(maxHeightOfBuilding) > 0) {
				maxHeightOfBuilding = block.getBuilding().getBuildingHeight();
			}
		}
		if (maxHeightOfBuilding.compareTo(new BigDecimal(15)) >= 0
				|| (pl.getPlot() != null && pl.getPlot().getArea().compareTo(new BigDecimal(500)) > 0)) {
			if (pl.getParkingDetails().getValidSpecialSlots() == 0) {
				pl.addError(SUB_RULE_40_11, getLocaleMessage(DcrConstants.OBJECTNOTDEFINED, SP_PARKING));
			} else {
				for (Measurement m : pl.getParkingDetails().getSpecial()) {
					if (m.getMinimumSide().compareTo(new BigDecimal(0)) > 0
							&& m.getMinimumSide().compareTo(new BigDecimal(3.6)) >= 0) {
						setReportOutputDetails(pl, SUB_RULE_40_11, SP_PARKING, 1 + NUMBERS,
								pl.getParkingDetails().getValidSpecialSlots() + NUMBERS,
								Result.Accepted.getResultVal());
					} else if (m.getMinimumSide().compareTo(new BigDecimal(0)) > 0) {
						setReportOutputDetails(pl, SUB_RULE_40_11, SP_PARKING, 1 + NUMBERS,
								pl.getParkingDetails().getValidSpecialSlots() + NUMBERS,
								Result.Not_Accepted.getResultVal());
					}
				}
			}
		}

	}

	private void processTwoWheelerParking(Plan pl, ParkingHelper helper) {
		helper.twoWheelerParking = BigDecimal.valueOf(0.25 * helper.totalRequiredCarParking * 2.70 * 5.50)
				.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
		double providedArea = 0;
		for (Measurement measurement : pl.getParkingDetails().getTwoWheelers()) {
			providedArea = providedArea + measurement.getArea().doubleValue();
		}
		if (providedArea < helper.twoWheelerParking) {
			setReportOutputDetails(pl, SUB_RULE_34_2, TWO_WHEELER_PARK_AREA,
					helper.twoWheelerParking + " " + DcrConstants.SQMTRS,
					BigDecimal.valueOf(providedArea).setScale(2, BigDecimal.ROUND_HALF_UP) + " " + DcrConstants.SQMTRS,
					Result.Not_Accepted.getResultVal());
		} else {
			setReportOutputDetails(pl, SUB_RULE_34_2, TWO_WHEELER_PARK_AREA,
					helper.twoWheelerParking + " " + DcrConstants.SQMTRS,
					BigDecimal.valueOf(providedArea).setScale(2, BigDecimal.ROUND_HALF_UP) + " " + DcrConstants.SQMTRS,
					Result.Accepted.getResultVal());
		}
	}

	private void processMechanicalParking(Plan pl) {
		int count = 0;
		for (Measurement m : pl.getParkingDetails().getMechParking())
			if (m.getWidth().compareTo(BigDecimal.valueOf(MECH_PARKING_WIDTH)) < 0
					|| m.getHeight().compareTo(BigDecimal.valueOf(MECH_PARKING_HEIGHT)) < 0)
				count++;
		if (count > 0) {
			setReportOutputDetails(pl, SUB_RULE_34_2, MECH_PARKING_DESC, MECH_PARKING_DIM_DESC,
					count + MECH_PARKING_DIM_DESC_NA, Result.Not_Accepted.getResultVal());
		} else {
			setReportOutputDetails(pl, SUB_RULE_34_2, MECH_PARKING_DESC, MECH_PARKING_DIM_DESC,
					count + MECH_PARKING_DIM_DESC_NA, Result.Accepted.getResultVal());
		}
	}

	/*
	 * private double processMechanicalParking(Plan pl, ParkingHelper helper) {
	 * Integer noOfMechParkingFromPlInfo =
	 * pl.getPlanInformation().getNoOfMechanicalParking(); Integer providedSlots =
	 * pl.getParkingDetails().getMechParking().size(); double maxAllowedMechPark =
	 * BigDecimal.valueOf(helper.totalRequiredCarParking / 2).setScale(0,
	 * RoundingMode.UP) .intValue(); if (noOfMechParkingFromPlInfo > 0) { if
	 * (noOfMechParkingFromPlInfo > 0 && providedSlots == 0) {
	 * setReportOutputDetails(pl, SUB_RULE_34_2, MECHANICAL_PARKING, 1 + NUMBERS,
	 * providedSlots + NUMBERS, Result.Not_Accepted.getResultVal()); } else if
	 * (noOfMechParkingFromPlInfo > 0 && providedSlots > 0 &&
	 * noOfMechParkingFromPlInfo > maxAllowedMechPark) { setReportOutputDetails(pl,
	 * SUB_RULE_34_2, MAX_ALLOWED_MECH_PARK, maxAllowedMechPark + NUMBERS,
	 * noOfMechParkingFromPlInfo + NUMBERS, Result.Not_Accepted.getResultVal()); }
	 * else if (noOfMechParkingFromPlInfo > 0 && providedSlots > 0) {
	 * setReportOutputDetails(pl, SUB_RULE_34_2, MECHANICAL_PARKING, "",
	 * noOfMechParkingFromPlInfo + NUMBERS, Result.Accepted.getResultVal()); } }
	 * return 0; }
	 */

	/*
	 * private void buildResultForYardValidation(Plan Plan, BigDecimal
	 * parkSlotAreaInFrontYard, BigDecimal maxAllowedArea, String type) {
	 * Plan.reportOutput .add(buildRuleOutputWithSubRule(DcrConstants.RULE34,
	 * SUB_RULE_34_1,
	 * "Parking space should not exceed 50% of the area of mandatory " + type,
	 * "Parking space should not exceed 50% of the area of mandatory " + type,
	 * "Maximum allowed area for parking in " + type +" " + maxAllowedArea +
	 * DcrConstants.SQMTRS, "Parking provided in more than the allowed area " +
	 * parkSlotAreaInFrontYard + DcrConstants.SQMTRS, Result.Not_Accepted, null)); }
	 * private BigDecimal validateParkingSlotsAreWithInYard(Plan Plan, Polygon
	 * yardPolygon) { BigDecimal area = BigDecimal.ZERO; for (Measurement
	 * parkingSlot : Plan.getParkingDetails().getCars()) { Iterator parkSlotIterator
	 * = parkingSlot.getPolyLine().getVertexIterator(); while
	 * (parkSlotIterator.hasNext()) { DXFVertex dxfVertex = (DXFVertex)
	 * parkSlotIterator.next(); Point point = dxfVertex.getPoint(); if
	 * (rayCasting.contains(point, yardPolygon)) { area =
	 * area.add(parkingSlot.getArea()); } } } return area; }
	 */

	private void checkDimensionForCarParking(Plan pl, ParkingHelper helper) {

		/*
		 * for (Block block : Plan.getBlocks()) { for (SetBack setBack :
		 * block.getSetBacks()) { if (setBack.getFrontYard() != null &&
		 * setBack.getFrontYard().getPresentInDxf()) { Polygon frontYardPolygon =
		 * ProcessHelper.getPolygon(setBack.getFrontYard().getPolyLine()); BigDecimal
		 * parkSlotAreaInFrontYard = validateParkingSlotsAreWithInYard(Plan,
		 * frontYardPolygon); BigDecimal maxAllowedArea =
		 * setBack.getFrontYard().getArea().divide(BigDecimal.valueOf(2),
		 * DcrConstants.DECIMALDIGITS_MEASUREMENTS, RoundingMode.HALF_UP); if
		 * (parkSlotAreaInFrontYard.compareTo(maxAllowedArea) > 0) {
		 * buildResultForYardValidation(Plan, parkSlotAreaInFrontYard, maxAllowedArea,
		 * "front yard space"); } } if (setBack.getRearYard() != null &&
		 * setBack.getRearYard().getPresentInDxf()) { Polygon rearYardPolygon =
		 * ProcessHelper.getPolygon(setBack.getRearYard().getPolyLine()); BigDecimal
		 * parkSlotAreaInRearYard = validateParkingSlotsAreWithInYard(Plan,
		 * rearYardPolygon); BigDecimal maxAllowedArea =
		 * setBack.getRearYard().getArea().divide(BigDecimal.valueOf(2),
		 * DcrConstants.DECIMALDIGITS_MEASUREMENTS, RoundingMode.HALF_UP); if
		 * (parkSlotAreaInRearYard.compareTo(maxAllowedArea) > 0) {
		 * buildResultForYardValidation(Plan, parkSlotAreaInRearYard, maxAllowedArea,
		 * "rear yard space"); } } if (setBack.getSideYard1() != null &&
		 * setBack.getSideYard1().getPresentInDxf()) { Polygon sideYard1Polygon =
		 * ProcessHelper.getPolygon(setBack.getSideYard1().getPolyLine()); BigDecimal
		 * parkSlotAreaInSideYard1 = validateParkingSlotsAreWithInYard(Plan,
		 * sideYard1Polygon); BigDecimal maxAllowedArea =
		 * setBack.getSideYard1().getArea().divide(BigDecimal.valueOf(2),
		 * DcrConstants.DECIMALDIGITS_MEASUREMENTS, RoundingMode.HALF_UP); if
		 * (parkSlotAreaInSideYard1.compareTo(maxAllowedArea) > 0) {
		 * buildResultForYardValidation(Plan, parkSlotAreaInSideYard1, maxAllowedArea,
		 * "side yard1 space"); } } if (setBack.getSideYard2() != null &&
		 * setBack.getSideYard2().getPresentInDxf()) { Polygon sideYard2Polygon =
		 * ProcessHelper.getPolygon(setBack.getSideYard2().getPolyLine()); BigDecimal
		 * parkSlotAreaInFrontYard = validateParkingSlotsAreWithInYard(Plan,
		 * sideYard2Polygon); BigDecimal maxAllowedArea =
		 * setBack.getSideYard2().getArea().divide(BigDecimal.valueOf(2),
		 * DcrConstants.DECIMALDIGITS_MEASUREMENTS, RoundingMode.HALF_UP); if
		 * (parkSlotAreaInFrontYard.compareTo(maxAllowedArea) > 0) {
		 * buildResultForYardValidation(Plan, parkSlotAreaInFrontYard, maxAllowedArea,
		 * "side yard2 space"); } } } }
		 */

		int parkingCount = pl.getParkingDetails().getCars().size();
		int failedCount = 0;
		int success = 0;
		for (Measurement slot : pl.getParkingDetails().getCars()) {
			if (slot.getHeight().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_HEIGHT
					&& slot.getWidth().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_WIDTH)
				success++;
			else
				failedCount++;
		}
		pl.getParkingDetails().setValidCarParkingSlots(parkingCount - failedCount);
		if (parkingCount > 0)
			if (failedCount > 0) {
				if (helper.totalRequiredCarParking.intValue() == pl.getParkingDetails().getValidCarParkingSlots()) {
					setReportOutputDetails(pl, SUB_RULE_40, SUB_RULE_34_1_DESCRIPTION,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + parkingCount + PARKING + failedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Accepted.getResultVal());
				} else {
					setReportOutputDetails(pl, SUB_RULE_40, SUB_RULE_34_1_DESCRIPTION,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + parkingCount + PARKING + failedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Not_Accepted.getResultVal());
				}
			} else {
				setReportOutputDetails(pl, SUB_RULE_40, SUB_RULE_34_1_DESCRIPTION,
						PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING, NO_VIOLATION_OF_AREA + parkingCount + PARKING,
						Result.Accepted.getResultVal());
			}
		int openParkCount = pl.getParkingDetails().getOpenCars().size();
		int openFailedCount = 0;
		int openSuccess = 0;
		for (Measurement slot : pl.getParkingDetails().getOpenCars()) {
			if (slot.getHeight().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_HEIGHT
					&& slot.getWidth().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_WIDTH)
				openSuccess++;
			else
				openFailedCount++;
		}
		pl.getParkingDetails().setValidOpenCarSlots(openParkCount - openFailedCount);
		if (openParkCount > 0)
			if (openFailedCount > 0) {
				if (helper.totalRequiredCarParking.intValue() == pl.getParkingDetails().getValidOpenCarSlots()) {
					setReportOutputDetails(pl, SUB_RULE_40, OPEN_PARKING_DIM_DESC,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + openParkCount + PARKING + openFailedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Accepted.getResultVal());
				} else {
					setReportOutputDetails(pl, SUB_RULE_40, OPEN_PARKING_DIM_DESC,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + openParkCount + PARKING + openFailedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Not_Accepted.getResultVal());
				}
			} else {
				setReportOutputDetails(pl, SUB_RULE_40, OPEN_PARKING_DIM_DESC,
						PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING, NO_VIOLATION_OF_AREA + openParkCount + PARKING,
						Result.Accepted.getResultVal());
			}

		int coverParkCount = pl.getParkingDetails().getCoverCars().size();
		int coverFailedCount = 0;
		int coverSuccess = 0;
		for (Measurement slot : pl.getParkingDetails().getCoverCars()) {
			if (slot.getHeight().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_HEIGHT
					&& slot.getWidth().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_WIDTH)
				coverSuccess++;
			else
				coverFailedCount++;
		}
		pl.getParkingDetails().setValidCoverCarSlots(coverParkCount - coverFailedCount);
		if (coverParkCount > 0)
			if (coverFailedCount > 0) {
				if (helper.totalRequiredCarParking.intValue() == pl.getParkingDetails().getValidCoverCarSlots()) {
					setReportOutputDetails(pl, SUB_RULE_40, COVER_PARKING_DIM_DESC,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + coverParkCount + PARKING + coverFailedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Accepted.getResultVal());
				} else {
					setReportOutputDetails(pl, SUB_RULE_40, COVER_PARKING_DIM_DESC,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + coverParkCount + PARKING + coverFailedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Not_Accepted.getResultVal());
				}
			} else {
				setReportOutputDetails(pl, SUB_RULE_40, COVER_PARKING_DIM_DESC,
						PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING, NO_VIOLATION_OF_AREA + coverParkCount + PARKING,
						Result.Accepted.getResultVal());
			}

		// Validate dimension of basement
		int bsmntParkCount = pl.getParkingDetails().getBasementCars().size();
		int bsmntFailedCount = 0;
		int bsmntSuccess = 0;
		for (Measurement slot : pl.getParkingDetails().getBasementCars()) {
			if (slot.getHeight().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_HEIGHT
					&& slot.getWidth().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_WIDTH)
				bsmntSuccess++;
			else
				bsmntFailedCount++;
		}
		pl.getParkingDetails().setValidBasementCarSlots(bsmntParkCount - bsmntFailedCount);
		if (bsmntParkCount > 0)
			if (bsmntFailedCount > 0) {
				if (helper.totalRequiredCarParking.intValue() == pl.getParkingDetails().getValidBasementCarSlots()) {
					setReportOutputDetails(pl, SUB_RULE_40, BSMNT_PARKING_DIM_DESC,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + bsmntParkCount + PARKING + bsmntFailedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Accepted.getResultVal());
				} else {
					setReportOutputDetails(pl, SUB_RULE_40, BSMNT_PARKING_DIM_DESC,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + bsmntParkCount + PARKING + bsmntFailedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Not_Accepted.getResultVal());
				}
			} else {
				setReportOutputDetails(pl, SUB_RULE_40, BSMNT_PARKING_DIM_DESC,
						PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING, NO_VIOLATION_OF_AREA + bsmntParkCount + PARKING,
						Result.Accepted.getResultVal());
			}

	}

	private void checkDimensionForSpecialParking(Plan pl, ParkingHelper helper) {

		int success = 0;
		int specialFailedCount = 0;
		int specialParkCount = pl.getParkingDetails().getSpecial().size();
		for (Measurement spParkSlot : pl.getParkingDetails().getSpecial()) {
			if (spParkSlot.getMinimumSide().doubleValue() >= SP_PARK_SLOT_MIN_SIDE)
				success++;
			else
				specialFailedCount++;
		}
		pl.getParkingDetails().setValidSpecialSlots(specialParkCount - specialFailedCount);
		if (specialParkCount > 0)
			if (specialFailedCount > 0) {
				if (helper.daParking.intValue() == pl.getParkingDetails().getValidSpecialSlots()) {
					setReportOutputDetails(pl, SUB_RULE_40_8, SP_PARKING_SLOT_AREA,
							DA_PARKING_MIN_AREA + MINIMUM_AREA_OF_EACH_DA_PARKING,
							NO_VIOLATION_OF_AREA + pl.getParkingDetails().getValidSpecialSlots() + PARKING,
							Result.Accepted.getResultVal());
				} else {
					setReportOutputDetails(pl, SUB_RULE_40_8, SP_PARKING_SLOT_AREA,
							DA_PARKING_MIN_AREA + MINIMUM_AREA_OF_EACH_DA_PARKING,
							OUT_OF + specialParkCount + PARKING + specialFailedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Not_Accepted.getResultVal());
				}
			} else {
				setReportOutputDetails(pl, SUB_RULE_40_8, SP_PARKING_SLOT_AREA,
						DA_PARKING_MIN_AREA + MINIMUM_AREA_OF_EACH_DA_PARKING,
						NO_VIOLATION_OF_AREA + specialParkCount + PARKING, Result.Accepted.getResultVal());
			}
	}

	private void checkDimensionForTwoWheelerParking(Plan pl, ParkingHelper helper) {
		double providedArea = 0;
		int twoWheelParkingCount = pl.getParkingDetails().getTwoWheelers().size();
		int failedTwoWheelCount = 0;
		helper.twoWheelerParking = BigDecimal.valueOf(0.25 * helper.totalRequiredCarParking * 2.70 * 5.50)
				.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
		if (!pl.getParkingDetails().getTwoWheelers().isEmpty()) {
			for (Measurement m : pl.getParkingDetails().getTwoWheelers()) {
				if (m.getWidth().setScale(2, RoundingMode.UP).doubleValue() < TWO_WHEEL_PARKING_AREA_WIDTH
						|| m.getHeight().setScale(2, RoundingMode.UP).doubleValue() < TWO_WHEEL_PARKING_AREA_HEIGHT)
					failedTwoWheelCount++;

				providedArea = providedArea + m.getArea().doubleValue();
			}
		}

		if (providedArea < helper.twoWheelerParking) {
			setReportOutputDetails(pl, SUB_RULE_34_2, TWO_WHEELER_PARK_AREA,
					helper.twoWheelerParking + " " + DcrConstants.SQMTRS,
					BigDecimal.valueOf(providedArea).setScale(2, BigDecimal.ROUND_HALF_UP) + " " + DcrConstants.SQMTRS,
					Result.Not_Accepted.getResultVal());
		} else {
			setReportOutputDetails(pl, SUB_RULE_34_2, TWO_WHEELER_PARK_AREA,
					helper.twoWheelerParking + " " + DcrConstants.SQMTRS,
					BigDecimal.valueOf(providedArea).setScale(2, BigDecimal.ROUND_HALF_UP) + " " + DcrConstants.SQMTRS,
					Result.Accepted.getResultVal());
		}

		if (providedArea >= helper.twoWheelerParking && failedTwoWheelCount >= 0) {
			setReportOutputDetails(pl, SUB_RULE_40, TWO_WHEELER_DIM_DESC, PARKING_AREA_DIM,
					OUT_OF + twoWheelParkingCount + PARKING + failedTwoWheelCount + PARKING_VIOLATED_DIM,
					Result.Accepted.getResultVal());
		} else {
			setReportOutputDetails(pl, SUB_RULE_40, TWO_WHEELER_DIM_DESC, PARKING_AREA_DIM,
					OUT_OF + twoWheelParkingCount + PARKING + failedTwoWheelCount + PARKING_VIOLATED_DIM,
					Result.Not_Accepted.getResultVal());
		}
	}

	private BigDecimal getTotalCarpetAreaByOccupancy(Plan pl, OccupancyType type) {
		BigDecimal totalArea = BigDecimal.ZERO;
		for (Block b : pl.getBlocks())
			for (Occupancy occupancy : b.getBuilding().getTotalArea())
				if (occupancy.getType().equals(type))
					totalArea = totalArea.add(occupancy.getCarpetArea());
		return totalArea;
	}

	private void checkAreaForLoadUnloadSpaces(Plan pl) {
		double providedArea = 0;
		BigDecimal totalBuiltupArea = pl.getOccupancies().stream().map(Occupancy::getBuiltUpArea)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		double requiredArea = Math.abs(((totalBuiltupArea.doubleValue() - 700) / 1000) * 30);
		if (!pl.getParkingDetails().getLoadUnload().isEmpty()) {
			for (Measurement m : pl.getParkingDetails().getLoadUnload()) {
				if (m.getArea().compareTo(BigDecimal.valueOf(30)) >= 0)
					providedArea = providedArea + m.getArea().doubleValue();
			}
		}
		if (providedArea < requiredArea) {
			setReportOutputDetails(pl, SUB_RULE_40, LOADING_UNLOADING_AREA, requiredArea + " " + DcrConstants.SQMTRS,
					BigDecimal.valueOf(providedArea).setScale(2, BigDecimal.ROUND_HALF_UP) + " " + DcrConstants.SQMTRS,
					Result.Not_Accepted.getResultVal());
		} else {
			setReportOutputDetails(pl, SUB_RULE_40, LOADING_UNLOADING_AREA, requiredArea + " " + DcrConstants.SQMTRS,
					BigDecimal.valueOf(providedArea).setScale(2, BigDecimal.ROUND_HALF_UP) + " " + DcrConstants.SQMTRS,
					Result.Accepted.getResultVal());
		}
	}

	@Override
	public Map<String, Date> getAmendments() {
		return new LinkedHashMap<>();
	}
}
