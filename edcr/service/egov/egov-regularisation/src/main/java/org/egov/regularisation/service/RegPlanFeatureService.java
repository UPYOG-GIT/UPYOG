package org.egov.regularisation.service;

import java.util.List;

import org.egov.common.entity.edcr.PlanFeature;
import org.egov.regularisation.repository.RegPlanFeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegPlanFeatureService {

    @Autowired
    private RegPlanFeatureRepository featureRepository;

    public List<PlanFeature> getFeatures() {
        return featureRepository.getFeatures();

    }

}
