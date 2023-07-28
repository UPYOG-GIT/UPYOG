package org.egov.regularisation.extract.service;

import org.egov.regularisation.entity.RegAmendment;
import org.springframework.stereotype.Service;

@Service
public class RegAmendmentService {
    public RegAmendment getAmendments() {
        return new RegAmendment();
    }

}
