package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.egov.common.entity.edcr.SepticTank;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SepticTankExtract extends FeatureExtract {
    private static final Logger LOG = LogManager.getLogger(SepticTankExtract.class);

    @Autowired
    private LayerNames layerNames;
    
    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        return planDetail;
    }

    @Override
    public PlanDetail extract(PlanDetail pl) {

		/*
		 * DXFDocument doc = planDetail.getDoc(); List<String> septicTankLayers =
		 * Util.getLayerNamesLike(doc, "SEPTIC_TANK"); List<SepticTank> septicTanks =
		 * new ArrayList<>(); for (String septicTankLayer : septicTankLayers) {
		 * List<DXFLWPolyline> septicTankPolyLine = Util.getPolyLinesByLayer(doc,
		 * septicTankLayer); List<BigDecimal> distanceFromWaterSource =
		 * Util.getListOfDimensionByColourCode(planDetail, septicTankLayer,
		 * DxfFileConstants.INDEX_COLOR_ONE); List<BigDecimal> distanceFromBuilding =
		 * Util.getListOfDimensionByColourCode(planDetail, septicTankLayer,
		 * DxfFileConstants.INDEX_COLOR_TWO);
		 * 
		 * SepticTank septicTank = new SepticTank(); if (!septicTankPolyLine.isEmpty())
		 * { MeasurementDetail measurement = new
		 * MeasurementDetail(septicTankPolyLine.get(0), true);
		 * septicTank.setArea(measurement.getArea());
		 * septicTank.setHeight(measurement.getHeight());
		 * septicTank.setWidth(measurement.getWidth()); }
		 * septicTank.setDistanceFromBuilding(distanceFromBuilding);
		 * septicTank.setDistanceFromWaterSource(distanceFromWaterSource);
		 * septicTanks.add(septicTank); } planDetail.setSepticTanks(septicTanks);
		 * 
		 * return planDetail;
		 */
    	
    	if (pl.getDoc().containsDXFLayer(layerNames.getLayerName("LAYER_NAME_SEPTIC_TANK"))) {
            String tankCapacity = Util.getMtextByLayerName(pl.getDoc(),
                    layerNames.getLayerName("LAYER_NAME_SEPTIC_TANK"),
                    layerNames.getLayerName("LAYER_NAME_SEPTIC_TANK_CAPACITY_L"));
            if (tankCapacity != null && !tankCapacity.isEmpty())
                try {
                    if (tankCapacity.contains(";")) {
                        String[] textSplit = tankCapacity.split(";");
                        int length = textSplit.length;

                        if (length >= 1) {
                            int index = length - 1;
                            tankCapacity = textSplit[index];
                            tankCapacity = tankCapacity.replaceAll("[^\\d.]", "");
                        } else
                            tankCapacity = tankCapacity.replaceAll("[^\\d.]", "");
                    } else
                        tankCapacity = tankCapacity.replaceAll("[^\\d.]", "");

                    if (!tankCapacity.isEmpty())
                        pl.getUtility().setSepticTankCapacity(BigDecimal.valueOf(Double.parseDouble(tankCapacity)));

                } catch (NumberFormatException e) {
                    pl.addError(layerNames.getLayerName("LAYER_NAME_SEPTIC_TANK"),
                            "Septic tank capacity value contains non numeric character.");
                }
        }

        return pl;
    }

}
