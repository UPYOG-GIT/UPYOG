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

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.Occupancy;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.springframework.stereotype.Service;

@Service
public class Ventilation extends FeatureProcess {

	private static final Logger LOG = LogManager.getLogger(Ventilation.class);
	private static final String RULE_43 = "43";
	public static final String LIGHT_VENTILATION_DESCRIPTION = "Light and Ventilation";

	@Override
	public Plan validate(Plan pl) {
		return pl;
	}

	@Override
	public Plan process(Plan pl) {
		for (Block b : pl.getBlocks()) {
			ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
//			scrutinyDetail.setKey("Common_Ventilation");
//			scrutinyDetail.addColumnHeading(1, RULE_NO);
			scrutinyDetail.addColumnHeading(1, DESCRIPTION);
			scrutinyDetail.addColumnHeading(2, PRESENTED);
//			scrutinyDetail.addColumnHeading(3, REQUIRED);
//			scrutinyDetail.addColumnHeading(4, PROVIDED);
			scrutinyDetail.addColumnHeading(3, STATUS);
			Map<String, String> details = new HashMap<>();

			if (b.getBuilding() != null && b.getBuilding().getFloors() != null
					&& !b.getBuilding().getFloors().isEmpty()) {
				scrutinyDetail.setKey("Block_" + b.getNumber() + "_" + "Ventilation");
				boolean isPresent=false;
				for (Floor f : b.getBuilding().getFloors()) {
					
//					details.put(RULE_NO, RULE_43);
					
					if (f.getLightAndVentilation() != null && f.getLightAndVentilation().getMeasurements() != null
							&& !f.getLightAndVentilation().getMeasurements().isEmpty()) {
						isPresent=true;
						/*
						 * BigDecimal totalVentilationArea =
						 * f.getLightAndVentilation().getMeasurements().stream()
						 * .map(Measurement::getArea).reduce(BigDecimal.ZERO, BigDecimal::add);
						 * BigDecimal totalCarpetArea =
						 * f.getOccupancies().stream().map(Occupancy::getCarpetArea)
						 * .reduce(BigDecimal.ZERO, BigDecimal::add);
						 * 
						 * if (totalVentilationArea.compareTo(BigDecimal.ZERO) > 0) { if
						 * (totalVentilationArea.compareTo(totalCarpetArea.divide(BigDecimal.valueOf(8))
						 * .setScale(2, BigDecimal.ROUND_HALF_UP)) >= 0) { details.put(REQUIRED,
						 * "Minimum 1/8th of the floor area "); details.put(PROVIDED,
						 * "Ventilation area " + totalVentilationArea + " of Carpet Area   " +
						 * totalCarpetArea + " at floor " + f.getNumber()); details.put(STATUS,
						 * Result.Accepted.getResultVal()); scrutinyDetail.getDetail().add(details);
						 * pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
						 * 
						 * } else { details.put(REQUIRED, "Minimum 1/8th of the floor area ");
						 * details.put(PROVIDED, "Ventilation area " + totalVentilationArea +
						 * " of Carpet Area   " + totalCarpetArea + " at floor " + f.getNumber());
						 * details.put(STATUS, Result.Not_Accepted.getResultVal());
						 * scrutinyDetail.getDetail().add(details);
						 * pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail); } }
						 */ 
						/*
							 * else { details.put(REQUIRED,
							 * "Minimum 1/8th of the floor area ");
							 * details.put(PROVIDED,
							 * "Ventilation area not defined in floor  " +
							 * f.getNumber()); details.put(STATUS,
							 * Result.Not_Accepted.getResultVal());
							 * scrutinyDetail.getDetail().add(details);
							 * pl.getReportOutput().getScrutinyDetails().add(
							 * scrutinyDetail); }
							 */
					} /*
						 * else { details.put(REQUIRED,
						 * "Minimum 1/8th of the floor area ");
						 * details.put(PROVIDED,
						 * "Ventilation area not defined in floor  " +
						 * f.getNumber()); details.put(STATUS,
						 * Result.Not_Accepted.getResultVal());
						 * scrutinyDetail.getDetail().add(details);
						 * pl.getReportOutput().getScrutinyDetails().add(
						 * scrutinyDetail); }
						 */

				}
				scrutinyDetail.getDetail().add(details);
				details.put(DESCRIPTION, LIGHT_VENTILATION_DESCRIPTION);
				details.put(PRESENTED, isPresent ? "Yes" : "No");
				details.put(STATUS, "");
				pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
			}

		}

		return pl;
	}

	@Override
	public Map<String, Date> getAmendments() {
		return new LinkedHashMap<>();
	}

}
