package org.egov.regularisation.repository;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.entity.edcr.PlanFeature;
import org.egov.regularisation.feature.RegParking;
import org.egov.regularisation.feature.RegCoverage;
import org.egov.regularisation.feature.RegFar;
import org.egov.regularisation.feature.RegPlanInfoFeature;
import org.springframework.stereotype.Service;

@Service
public class RegPlanFeatureRepository {

    public List<PlanFeature> getFeatures() {
        ArrayList<PlanFeature> features = new ArrayList<>();

        PlanFeature pf = new PlanFeature(RegPlanInfoFeature.class);
        features.add(pf);
        pf = new PlanFeature(RegFar.class);
        features.add(pf);

        pf = new PlanFeature(RegCoverage.class);
        features.add(pf);

//
        pf = new PlanFeature(RegParking.class);
        features.add(pf);

//        
//        pf = new PlanFeature(PlotArea.class);
//        features.add(pf);
        
//        pf = new PlanFeature(DxfToPdfConverter.class);
//        features.add(pf);
        

        return features;
    }

}
