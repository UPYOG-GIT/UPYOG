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

package org.egov.common.entity.edcr;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author vinoth
 *
 */
public class ParkingDetails implements Serializable {
	private static final long serialVersionUID = 8L;
	private List<Measurement> cars = new ArrayList<>();
	private List<Measurement> openCars = new ArrayList<>();
	private List<Measurement> coverCars = new ArrayList<>();
	private List<Measurement> basementCars = new ArrayList<>();
	private List<Measurement> visitors = new ArrayList<>();
	private Integer validCarParkingSlots = 0;
	private Integer validOpenCarSlots = 0;
	private Integer validCoverCarSlots = 0;
	private Integer validBasementCarSlots = 0;
	private Integer diningSeats = 0;
	private List<Measurement> loadUnload = new ArrayList<>();
	private List<Measurement> mechParking = new ArrayList<>();
	private List<Measurement> twoWheelers = new ArrayList<>();
	private List<Measurement> disabledPersons = new ArrayList<>();
	private Integer validDAParkingSlots = 0;
	private BigDecimal distFromDAToMainEntrance = BigDecimal.ZERO;
	private List<Measurement> special = new ArrayList<>();
	private Integer validSpecialSlots = 0;
	private List<Measurement> stilts = new ArrayList<>();
	private List<Measurement> mechanicalLifts = new ArrayList<>();
	private List<Measurement> lowerGroundFloor = new ArrayList<>(); //add for Lower Ground Floor Parking
	private List<Measurement> podium = new ArrayList<>(); //add for Lower Ground Floor Parking

	public List<Measurement> getCars() {
		return cars;
	}

	public void setCars(List<Measurement> cars) {
		this.cars = cars;
	}

	public List<Measurement> getOpenCars() {
		return openCars;
	}

	public void setOpenCars(List<Measurement> openCars) {
		this.openCars = openCars;
	}

	public List<Measurement> getCoverCars() {
		return coverCars;
	}

	public void setCoverCars(List<Measurement> coverCars) {
		this.coverCars = coverCars;
	}

	public List<Measurement> getBasementCars() {
		return basementCars;
	}

	public void setBasementCars(List<Measurement> basementCars) {
		this.basementCars = basementCars;
	}

	public List<Measurement> getVisitors() {
		return visitors;
	}

	public void setVisitors(List<Measurement> visitors) {
		this.visitors = visitors;
	}

	public Integer getValidCarParkingSlots() {
		return validCarParkingSlots;
	}

	public void setValidCarParkingSlots(Integer validCarParkingSlots) {
		this.validCarParkingSlots = validCarParkingSlots;
	}

	public Integer getDiningSeats() {
		return diningSeats;
	}

	public void setDiningSeats(Integer diningSeats) {
		this.diningSeats = diningSeats;
	}

	public List<Measurement> getLoadUnload() {
		return loadUnload;
	}

	public void setLoadUnload(List<Measurement> loadUnload) {
		this.loadUnload = loadUnload;
	}

	public List<Measurement> getMechParking() {
		return mechParking;
	}

	public void setMechParking(List<Measurement> mechParking) {
		this.mechParking = mechParking;
	}

	public List<Measurement> getTwoWheelers() {
		return twoWheelers;
	}

	public void setTwoWheelers(List<Measurement> twoWheelers) {
		this.twoWheelers = twoWheelers;
	}

	public List<Measurement> getDisabledPersons() {
		return disabledPersons;
	}

	public void setDisabledPersons(List<Measurement> disabledPersons) {
		this.disabledPersons = disabledPersons;
	}

	public Integer getValidDAParkingSlots() {
		return validDAParkingSlots;
	}

	public void setValidDAParkingSlots(Integer validDAParkingSlots) {
		this.validDAParkingSlots = validDAParkingSlots;
	}

	public BigDecimal getDistFromDAToMainEntrance() {
		return distFromDAToMainEntrance;
	}

	public void setDistFromDAToMainEntrance(BigDecimal distFromDAToMainEntrance) {
		this.distFromDAToMainEntrance = distFromDAToMainEntrance;
	}

	public List<Measurement> getSpecial() {
		return special;
	}

	public void setSpecial(List<Measurement> special) {
		this.special = special;
	}

	public Integer getValidOpenCarSlots() {
		return validOpenCarSlots;
	}

	public void setValidOpenCarSlots(Integer validOpenCarSlots) {
		this.validOpenCarSlots = validOpenCarSlots;
	}

	public Integer getValidCoverCarSlots() {
		return validCoverCarSlots;
	}

	public void setValidCoverCarSlots(Integer validCoverCarSlots) {
		this.validCoverCarSlots = validCoverCarSlots;
	}

	public Integer getValidBasementCarSlots() {
		return validBasementCarSlots;
	}

	public void setValidBasementCarSlots(Integer validBasementCarSlots) {
		this.validBasementCarSlots = validBasementCarSlots;
	}

	public Integer getValidSpecialSlots() {
		return validSpecialSlots;
	}

	public void setValidSpecialSlots(Integer validSpecialSlots) {
		this.validSpecialSlots = validSpecialSlots;
	}

	public List<Measurement> getStilts() {
		return stilts;
	}

	public void setStilts(List<Measurement> stilts) {
		this.stilts = stilts;
	}

	public List<Measurement> getMechanicalLifts() {
		return mechanicalLifts;
	}

	public void setMechanicalLifts(List<Measurement> mechanicalLifts) {
		this.mechanicalLifts = mechanicalLifts;
	}

	public List<Measurement> getLowerGroundFloor() {
		return lowerGroundFloor;
	}

	public void setLowerGroundFloor(List<Measurement> lowerGroundFloor) {
		this.lowerGroundFloor = lowerGroundFloor;
	}

	public List<Measurement> getPodium() {
		return podium;
	}

	public void setPodium(List<Measurement> podium) {
		this.podium = podium;
	}

	
	
}