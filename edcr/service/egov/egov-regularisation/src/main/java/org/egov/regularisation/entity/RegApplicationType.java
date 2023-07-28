package org.egov.regularisation.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RegApplicationType {

    PERMIT("Permit"), OCCUPANCY_CERTIFICATE("Occupancy certificate"),REGULARISATION("Regularisation");

    @JsonValue
    private final String applicationTypeVal;

    RegApplicationType(String aTypeVal) {
        this.applicationTypeVal = aTypeVal;
    }

    public String getApplicationType() {
        return applicationTypeVal;
    }

    public String getApplicationTypeVal() {
        return applicationTypeVal;
    }

}
