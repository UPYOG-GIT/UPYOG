package org.egov.regularisation.feature;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.egov.common.entity.edcr.Plan;
import org.springframework.stereotype.Service;

@Service
public class RegPlanInfoFeature extends FeatureProcess {

    @Override
    public Plan validate(Plan pl) {
        // TODO Auto-generated method stub
        return pl;
    }

    @Override
    public Plan process(Plan pl) {
        return pl;
    }

    @Override
    public Map<String, Date> getAmendments() {
        return new LinkedHashMap<>();
    }

}
