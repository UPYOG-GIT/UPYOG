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
import static org.egov.edcr.constants.DxfFileConstants.F;
import static org.egov.edcr.constants.DxfFileConstants.G;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Door;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.Room;
import org.egov.common.entity.edcr.RoomHeight;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.common.entity.edcr.Window;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.service.ProcessHelper;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.stereotype.Service;

@Service
public class HeightOfRoom extends FeatureProcess {

	private static final String SUBRULE_41_II_A = "41-ii-a";
	private static final String SUBRULE_41_II_B = "41-ii-b";

	private static final String SUBRULE_41_II_A_AC_DESC = "Minimum height of ac room";
	private static final String SUBRULE_41_II_A_REGULAR_DESC = "Minimum height of regular room";
//	private static final String SUBRULE_41_II_B_AREA_DESC = "Total area of rooms";
	private static final String SUBRULE_41_II_B_AREA_DESC = "Area of room";
	private static final String SUBRULE_BEDROOM_DESC = "Bedroom";
	private static final String SUBRULE_41_II_B_TOTAL_WIDTH = "Minimum width of room";
	private static final String SUBRULE_DOOR_DESC = "Door";
	private static final String SUBRULE_WINDOW_DESC = "Window";

	public static final BigDecimal MINIMUM_HEIGHT_3_6 = BigDecimal.valueOf(3.6);
	public static final BigDecimal MINIMUM_HEIGHT_3 = BigDecimal.valueOf(3);
	public static final BigDecimal MINIMUM_HEIGHT_2_75 = BigDecimal.valueOf(2.75);
	public static final BigDecimal MINIMUM_HEIGHT_2_4 = BigDecimal.valueOf(2.4);
	public static final BigDecimal MINIMUM_AREA_9_5 = BigDecimal.valueOf(9.5);
	public static final BigDecimal MINIMUM_WIDTH_2_4 = BigDecimal.valueOf(2.4);
	public static final BigDecimal MINIMUM_WIDTH_2_1 = BigDecimal.valueOf(2.1);
	private static final String FLOOR = "Floor";
	private static final String ROOM_HEIGHT_NOTDEFINED = "Room height is not defined in layer ";
	private static final String LAYER_ROOM_HEIGHT = "BLK_%s_FLR_%s_%s";

	@Override
	public Plan validate(Plan pl) {
		return pl;
	}

	@Override
	public Plan process(Plan pl) {
		Map<String, Integer> heightOfRoomFeaturesColor = pl.getSubFeatureColorCodesMaster().get("HeightOfRoom");
		validate(pl);
		HashMap<String, String> errors = new HashMap<>();
		if (pl != null && pl.getBlocks() != null) {
			OccupancyTypeHelper mostRestrictiveOccupancy = pl.getVirtualBuilding() != null
					? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
					: null;
			if (mostRestrictiveOccupancy != null && mostRestrictiveOccupancy.getType() != null
					&& (A.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())
							|| (G.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())
									|| F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())))) {
				for (Block block : pl.getBlocks()) {
					if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty()) {
						scrutinyDetail = new ScrutinyDetail();
						scrutinyDetail.addColumnHeading(1, RULE_NO);
						scrutinyDetail.addColumnHeading(2, DESCRIPTION);
						scrutinyDetail.addColumnHeading(3, FLOOR);
						scrutinyDetail.addColumnHeading(4, REQUIRED);
						scrutinyDetail.addColumnHeading(5, PROVIDED);
						scrutinyDetail.addColumnHeading(6, STATUS);

						scrutinyDetail.setKey("Block_" + block.getNumber() + "_" + "Room Width");

						ScrutinyDetail scrutinyDetail1 = new ScrutinyDetail();
						scrutinyDetail1.addColumnHeading(1, RULE_NO);
						scrutinyDetail1.addColumnHeading(2, DESCRIPTION);
						scrutinyDetail1.addColumnHeading(3, FLOOR);
						scrutinyDetail1.addColumnHeading(4, REQUIRED);
						scrutinyDetail1.addColumnHeading(5, PROVIDED);
						scrutinyDetail1.addColumnHeading(6, STATUS);

						scrutinyDetail1.setKey("Block_" + block.getNumber() + "_" + "Room Area");

						ScrutinyDetail scrutinyDetail2 = new ScrutinyDetail();
						scrutinyDetail2.addColumnHeading(1, RULE_NO);
						scrutinyDetail2.addColumnHeading(2, DESCRIPTION);
						scrutinyDetail2.addColumnHeading(3, FLOOR);
						scrutinyDetail2.addColumnHeading(4, REQUIRED);
						scrutinyDetail2.addColumnHeading(5, PROVIDED);
						scrutinyDetail2.addColumnHeading(6, STATUS);
						scrutinyDetail2.setKey("Block_" + block.getNumber() + "_" + "Door");

						ScrutinyDetail scrutinyDetail3 = new ScrutinyDetail();
						scrutinyDetail3.addColumnHeading(1, RULE_NO);
						scrutinyDetail3.addColumnHeading(2, DESCRIPTION);
						scrutinyDetail3.addColumnHeading(3, FLOOR);
						scrutinyDetail3.addColumnHeading(4, REQUIRED);
						scrutinyDetail3.addColumnHeading(5, PROVIDED);
						scrutinyDetail3.addColumnHeading(6, STATUS);
						scrutinyDetail3.setKey("Block_" + block.getNumber() + "_" + "Window");
						for (Floor floor : block.getBuilding().getFloors()) {
							List<BigDecimal> roomAreas = new ArrayList<>();
							List<BigDecimal> roomWidths = new ArrayList<>();
							BigDecimal minimumArea = BigDecimal.ZERO;
							BigDecimal totalArea = BigDecimal.ZERO;
							BigDecimal minWidth = BigDecimal.ZERO;
							String subRule = null;
							String subRuleDesc = null;
							String color = "";

							if (A.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode()))
								color = DxfFileConstants.COLOR_RESIDENTIAL_ROOM;
							else if (F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode()))
								color = DxfFileConstants.COLOR_COMMERCIAL_ROOM;
							else if (G.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode()))
								color = DxfFileConstants.COLOR_INDUSTRIAL_ROOM;

							if (floor.getAcRooms() != null && floor.getAcRooms().size() > 0) {
								List<BigDecimal> residentialAcRoomHeights = new ArrayList<>();

								List<RoomHeight> acHeights = new ArrayList<>();
								List<Measurement> acRooms = new ArrayList<>();

								for (Room room : floor.getAcRooms()) {
									if (room.getHeights() != null)
										acHeights.addAll(room.getHeights());
									if (room.getRooms() != null)
										acRooms.addAll(room.getRooms());
								}

								for (RoomHeight roomHeight : acHeights) {
									if (heightOfRoomFeaturesColor.get(color) == roomHeight.getColorCode()) {
										residentialAcRoomHeights.add(roomHeight.getHeight());
									}
								}

								for (Measurement acRoom : acRooms) {
									if (heightOfRoomFeaturesColor.get(color) == acRoom.getColorCode()) {
										roomAreas.add(acRoom.getArea());
										roomWidths.add(acRoom.getWidth());
									}
								}

								/*
								 * if (!residentialAcRoomHeights.isEmpty()) { BigDecimal minHeight =
								 * residentialAcRoomHeights.stream().reduce(BigDecimal::min).get();
								 * 
								 * if (!G.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode()))
								 * minimumHeight = MINIMUM_HEIGHT_2_4; else minimumHeight = MINIMUM_HEIGHT_3;
								 * 
								 * subRule = SUBRULE_41_II_A; subRuleDesc = SUBRULE_41_II_A_AC_DESC;
								 * 
								 * 
								 * boolean valid = false; boolean isTypicalRepititiveFloor = false; Map<String,
								 * Object> typicalFloorValues = ProcessHelper.getTypicalFloorValues(block,
								 * floor, isTypicalRepititiveFloor); buildResult(pl, floor, minimumHeight,
								 * subRule, subRuleDesc, minHeight, valid, typicalFloorValues); } else { String
								 * layerName = String.format(LAYER_ROOM_HEIGHT, block.getNumber(),
								 * floor.getNumber(), "AC_ROOM"); errors.put(layerName, ROOM_HEIGHT_NOTDEFINED +
								 * layerName); pl.addErrors(errors); }
								 */

							}

							if (floor.getRegularRooms() != null && floor.getRegularRooms().size() > 0) {

								List<BigDecimal> residentialRoomHeights = new ArrayList<>();

								List<RoomHeight> heights = new ArrayList<>();
								List<Measurement> rooms = new ArrayList<>();

								for (Room room : floor.getRegularRooms()) {
									if (room.getHeights() != null)
										heights.addAll(room.getHeights());
									if (room.getRooms() != null)
										rooms.addAll(room.getRooms());
								}

								for (RoomHeight roomHeight : heights) {
									if (heightOfRoomFeaturesColor.get(color) == roomHeight.getColorCode()) {
										residentialRoomHeights.add(roomHeight.getHeight());
									}
								}
								BigDecimal roomArea = BigDecimal.ZERO;
								BigDecimal roomWidth = BigDecimal.ZERO;
								for (Measurement room : rooms) {
//									if (heightOfRoomFeaturesColor.get(color) == room.getColorCode()) {
//										roomAreas.add(room.getArea());
//										roomWidths.add(room.getWidth());
									roomArea = room.getArea().setScale(2, BigDecimal.ROUND_HALF_UP);
									roomWidth = room.getWidth().setScale(2, BigDecimal.ROUND_HALF_UP);
//										if (!roomAreas.isEmpty()) {
//									totalArea = roomAreas.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
//									BigDecimal minRoomWidth = roomWidths.stream().reduce(BigDecimal::min).get()
//											.setScale(2, BigDecimal.ROUND_HALF_UP);
//			                                if (roomAreas.size() == 1) {
//			                                    minimumHeight = MINIMUM_AREA_9_5;
//			                                    minWidth = MINIMUM_WIDTH_2_4;
//			                                }

//			                                else if (roomAreas.size() == 2) {
//									minimumArea = MINIMUM_AREA_9_5;
//									minWidth = MINIMUM_WIDTH_2_1;
//			                                }
									subRule = SUBRULE_41_II_B;
									subRuleDesc = SUBRULE_BEDROOM_DESC;

									boolean valid = false;
									boolean isTypicalRepititiveFloor = false;
									Map<String, Object> typicalFloorValues = ProcessHelper.getTypicalFloorValues(block,
											floor, isTypicalRepititiveFloor);
//			                                buildResult(pl, floor, minimumHeight, subRule, subRuleDesc, totalArea, valid, typicalFloorValues);

									if (roomArea.compareTo(minimumArea) >= 0 && roomWidth.compareTo(minWidth) >= 0) {
										setReportOutputDetails(pl, subRule, subRuleDesc, floor.getNumber().toString(),
												"Area >= -, Width>= -", "Area = " + roomArea + ", Width = " + roomWidth,
												Result.Accepted.getResultVal(), scrutinyDetail1);
//										setReportOutputDetails(pl, subRule, subRuleDesc, floor.getNumber().toString(),
//												minimumArea + DcrConstants.IN_METER, roomArea + DcrConstants.IN_METER,
//												Result.Accepted.getResultVal(), scrutinyDetail1);
									} else {
										setReportOutputDetails(pl, subRule, subRuleDesc, floor.getNumber().toString(),
												"Area >= -, Width>= -", "Area = " + roomArea + ", Width = " + roomWidth,
												Result.Not_Accepted.getResultVal(), scrutinyDetail1);
//										setReportOutputDetails(pl, subRule, subRuleDesc, floor.getNumber().toString(),
//												minimumArea + DcrConstants.IN_METER, roomArea + DcrConstants.IN_METER,
//												Result.Not_Accepted.getResultVal(), scrutinyDetail1);
									}

//									subRuleDesc = SUBRULE_41_II_B_TOTAL_WIDTH;
//									if (roomWidth.compareTo(minWidth) >= 0) {
//										setReportOutputDetails(pl, subRule, subRuleDesc, floor.getNumber().toString(),
//												minWidth + DcrConstants.IN_METER, roomWidth + DcrConstants.IN_METER,
//												Result.Accepted.getResultVal(), scrutinyDetail);
//									} else {
//										setReportOutputDetails(pl, subRule, subRuleDesc, floor.getNumber().toString(),
//												minWidth + DcrConstants.IN_METER, roomWidth + DcrConstants.IN_METER,
//												Result.Not_Accepted.getResultVal(), scrutinyDetail);
//									}
//			                                buildResult(pl, floor, minWidth, subRule, subRuleDesc, minRoomWidth, valid, typicalFloorValues);
//			                            }
//									}
								}

								/*
								 * if (!residentialRoomHeights.isEmpty()) { BigDecimal minHeight =
								 * residentialRoomHeights.stream().reduce(BigDecimal::min).get();
								 * 
								 * if (!G.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode()))
								 * minimumHeight = MINIMUM_HEIGHT_2_75; else minimumHeight = MINIMUM_HEIGHT_3_6;
								 * 
								 * subRule = SUBRULE_41_II_A; subRuleDesc = SUBRULE_41_II_A_REGULAR_DESC;
								 * 
								 * 
								 * boolean valid = false; boolean isTypicalRepititiveFloor = false; Map<String,
								 * Object> typicalFloorValues = ProcessHelper.getTypicalFloorValues(block,
								 * floor, isTypicalRepititiveFloor); buildResult(pl, floor, minimumHeight,
								 * subRule, subRuleDesc, minHeight, valid, typicalFloorValues); } else { String
								 * layerName = String.format(LAYER_ROOM_HEIGHT, block.getNumber(),
								 * floor.getNumber(), "REGULAR_ROOM"); errors.put(layerName,
								 * ROOM_HEIGHT_NOTDEFINED + layerName); pl.addErrors(errors); }
								 */
							}

							// Calculation For Doors
							if (floor.getDoors() != null && floor.getDoors().size() > 0) {
								for (Door door : floor.getDoors()) {
									if (door != null) {
										BigDecimal doorHeight = door.getDoorHeight();
										BigDecimal doorWidth = door.getDoorWidth();
										BigDecimal minDoorHeight = BigDecimal.valueOf(2.0);
										BigDecimal minDoorWidth = BigDecimal.valueOf(.75);
										subRule = SUBRULE_41_II_B;
										subRuleDesc = SUBRULE_DOOR_DESC;
										if (doorHeight.compareTo(minDoorHeight) >= 0
												&& doorWidth.compareTo(minDoorWidth) >= 0) {
											setReportOutputDetails(pl, subRule, subRuleDesc,
													floor.getNumber().toString(),
													"Height >= " + minDoorHeight + ", Width >=" + minDoorWidth,
													"Height = " + doorHeight + ", Width = " + doorWidth
															+ DcrConstants.IN_METER,
													Result.Accepted.getResultVal(), scrutinyDetail2);
										} else {
											setReportOutputDetails(pl, subRule, subRuleDesc,
													floor.getNumber().toString(),
													"Height >= " + minDoorHeight + ", Width >=" + minDoorWidth,
													"Height = " + doorHeight + ", Width = " + doorWidth
															+ DcrConstants.IN_METER,
													Result.Not_Accepted.getResultVal(), scrutinyDetail2);
										}
									}
								}
							}

							// Calculation For Windows
							if (floor.getWindows() != null && floor.getWindows().size() > 0) {
								for (Window window : floor.getWindows()) {
									BigDecimal windowHeight = window.getWindowHeight();
									BigDecimal windowWidth = window.getWindowWidth();
									BigDecimal minWindowHeight = BigDecimal.valueOf(.50);
									BigDecimal minWindowWidth = BigDecimal.valueOf(.50);
									subRule = SUBRULE_41_II_B;
									subRuleDesc = SUBRULE_WINDOW_DESC;
									if (windowHeight.compareTo(minWindowHeight) >= 0
											&& windowWidth.compareTo(minWindowWidth) >= 0) {
										setReportOutputDetails(pl, subRule, subRuleDesc, floor.getNumber().toString(),
												"Height >= " + minWindowHeight + ", Width >=" + minWindowWidth,
												"Height = " + windowHeight + ", Width = " + windowWidth,
												Result.Accepted.getResultVal(), scrutinyDetail3);
									} else {
										setReportOutputDetails(pl, subRule, subRuleDesc, floor.getNumber().toString(),
												"Height >= " + minWindowHeight + ", Width >=" + minWindowWidth,
												"Height = " + windowHeight + ", Width = " + windowWidth,
												Result.Not_Accepted.getResultVal(), scrutinyDetail3);
									}

								}
							}

						}
					}
				}
			}
		}
		return pl;

	}

	/*
	 * private void buildResult(Plan pl, Floor floor, BigDecimal expected, String
	 * subRule, String subRuleDesc, BigDecimal actual, boolean valid, Map<String,
	 * Object> typicalFloorValues) { if (!(Boolean)
	 * typicalFloorValues.get("isTypicalRepititiveFloor") &&
	 * expected.compareTo(BigDecimal.valueOf(0)) > 0 && subRule != null &&
	 * subRuleDesc != null) { if (actual.compareTo(expected) >= 0) { valid = true; }
	 * String value = typicalFloorValues.get("typicalFloors") != null ? (String)
	 * typicalFloorValues.get("typicalFloors") : " floor " + floor.getNumber(); if
	 * (valid) { setReportOutputDetails(pl, subRule, subRuleDesc, value, expected +
	 * DcrConstants.IN_METER, actual + DcrConstants.IN_METER,
	 * Result.Accepted.getResultVal()); } else { setReportOutputDetails(pl, subRule,
	 * subRuleDesc, value, expected + DcrConstants.IN_METER, actual +
	 * DcrConstants.IN_METER, Result.Not_Accepted.getResultVal()); } } }
	 */

	private void setReportOutputDetails(Plan pl, String ruleNo, String ruleDesc, String floor, String expected,
			String actual, String status, ScrutinyDetail scrutinyDetail) {
		Map<String, String> details = new HashMap<>();
		details.put(RULE_NO, ruleNo);
		details.put(DESCRIPTION, ruleDesc);
		details.put(FLOOR, floor);
		details.put(REQUIRED, expected);
		details.put(PROVIDED, actual);
		details.put(STATUS, status);
		scrutinyDetail.getDetail().add(details);
		pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
	}

	@Override
	public Map<String, Date> getAmendments() {
		return new LinkedHashMap<>();
	}
}