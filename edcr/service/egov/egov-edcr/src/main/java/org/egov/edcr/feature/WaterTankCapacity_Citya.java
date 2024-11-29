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

import static org.egov.edcr.utility.DcrConstants.IN_LITRE;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.stereotype.Service;

@Service
public class WaterTankCapacity_Citya extends WaterTankCapacity {
//    private static final String RULE_59_10_vii = "59-10-vii";
    private static final String RULE_76 = "76";
//    private static final String RULE_59_10_vii_DESCRIPTION = "Water Tank Capacity";
    private static final String RULE_76_DESCRIPTION = "Water Tank Capacity";
    private static final String WATER_TANK_CAPACITY = "Minimum Capacity of Water Tank";
    private static final BigDecimal TWELVE_POINTFIVE = BigDecimal.valueOf(12.5);
//    private static final BigDecimal ONEHUNDRED_THIRTYFIVE = BigDecimal.valueOf(135);
    private static final BigDecimal FORTYFIVE = BigDecimal.valueOf(45);
    private static final BigDecimal FIVE = BigDecimal.valueOf(5);
    private static final Logger LOGGER = LogManager.getLogger(WaterTankCapacity_Citya.class);

    @Override
    public Plan validate(Plan pl) {
        return pl;
    }

    @Override
    public Plan process(Plan pl) {
    	LOGGER.info("inside WaterTankCapacity_Birgaon process()");
        scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, REQUIRED);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);
        scrutinyDetail.setKey("Common_Water Tank Capacity");
        String subRule = RULE_76;
        String subRuleDesc = RULE_76_DESCRIPTION;
        BigDecimal expectedWaterTankCapacityR = BigDecimal.ZERO;
        BigDecimal expectedWaterTankCapacityC = BigDecimal.ZERO;
        BigDecimal expectedWaterTankCapacity = BigDecimal.ZERO;

        if (pl.getUtility() != null && pl.getVirtualBuilding() != null
                && pl.getUtility().getWaterTankCapacity() != null) {
            // No of persons = total builtup area / 12.5(occupant load)
            // Required Water tank capacity = 135 * no of persons

            Boolean valid = false;
            BigDecimal totalBuitUpArea = pl.getVirtualBuilding().getTotalBuitUpArea();
            
//            BigDecimal noOfPersons = totalBuitUpArea.divide(TWELVE_POINTFIVE, DcrConstants.DECIMALDIGITS_MEASUREMENTS,
//                    DcrConstants.ROUNDMODE_MEASUREMENTS);//--------COMENTED BY ME

//          --------------added by manisha for water tank logic         
            int noOfFamilyInResidential= pl.getPlanInformation().getTenementResidential();
            int noOfFamilyInCommercial= pl.getPlanInformation().getTenementCommercial();
//            String nn=pl.getPlanInfoProperties().get("TENEMENT_FOR_RESIDENTIAL");

            BigDecimal noOfPersonsR = BigDecimal.valueOf(noOfFamilyInResidential).multiply(FIVE.setScale(0, BigDecimal.ROUND_HALF_UP));
            BigDecimal noOfPersonsC = BigDecimal.valueOf(noOfFamilyInCommercial).multiply(FIVE.setScale(0, BigDecimal.ROUND_HALF_UP));
         
            expectedWaterTankCapacityR = FORTYFIVE
                  .multiply(noOfPersonsR.setScale(0, BigDecimal.ROUND_HALF_UP));
            
            expectedWaterTankCapacityC = FORTYFIVE
                    .multiply(noOfPersonsC.setScale(0, BigDecimal.ROUND_HALF_UP));
            
            expectedWaterTankCapacityR = expectedWaterTankCapacityR.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                    DcrConstants.ROUNDMODE_MEASUREMENTS);
            expectedWaterTankCapacityC = expectedWaterTankCapacityC.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                    DcrConstants.ROUNDMODE_MEASUREMENTS);
            expectedWaterTankCapacity = expectedWaterTankCapacityR.add(expectedWaterTankCapacityC);
            
            
            pl.getPlanInformation().setRequiredWaterTankCapacity(expectedWaterTankCapacity);
//          --------------added by manisha for water tank logic end          
            BigDecimal providedWaterTankCapacity = pl.getUtility().getWaterTankCapacity();
            providedWaterTankCapacity = providedWaterTankCapacity.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                    DcrConstants.ROUNDMODE_MEASUREMENTS);
            if (providedWaterTankCapacity.compareTo(expectedWaterTankCapacity) >= 0) {
                valid = true;
            }
            processWaterTankCapacity(pl, "", subRule, subRuleDesc, expectedWaterTankCapacity, valid);
        }

        return pl;
    }

    private void processWaterTankCapacity(Plan plan, String rule, String subRule, String subRuleDesc,
            BigDecimal expectedWaterTankCapacity, Boolean valid) {
        if (expectedWaterTankCapacity.compareTo(BigDecimal.valueOf(0)) > 0) {
            if (valid) {
                setReportOutputDetails(plan, subRule, WATER_TANK_CAPACITY,
                        expectedWaterTankCapacity.toString(),
                        plan.getUtility().getWaterTankCapacity().toString(),
                        Result.Accepted.getResultVal());
            } else {
                setReportOutputDetails(plan, subRule, WATER_TANK_CAPACITY,
                        expectedWaterTankCapacity.toString() + IN_LITRE,
                        plan.getUtility().getWaterTankCapacity().toString() + IN_LITRE,
                        Result.Not_Accepted.getResultVal());
            }
        }
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

    @Override
    public Map<String, Date> getAmendments() {
        return new LinkedHashMap<>();
    }
}
