/*
 *    eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) 2017  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *            Further, all user interfaces, including but not limited to citizen facing interfaces,
 *            Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *            derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *            For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *            For any further queries on attribution, including queries on brand guidelines,
 *            please contact contact@egovernments.org
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 *
 */

package org.egov.infra.elasticsearch.service;

import org.egov.infra.admin.master.service.CityService;
import org.egov.infra.config.core.ApplicationThreadLocals;
import org.egov.infra.elasticsearch.entity.ApplicationIndex;
import org.egov.infra.elasticsearch.repository.ApplicationIndexRepository;
import org.egov.infra.elasticsearch.service.es.ApplicationDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.egov.infra.utils.ApplicationConstant.CITY_CODE_KEY;
import static org.egov.infra.utils.ApplicationConstant.CITY_CORP_GRADE_KEY;
import static org.egov.infra.utils.ApplicationConstant.CITY_DIST_NAME_KEY;
import static org.egov.infra.utils.ApplicationConstant.CITY_NAME_KEY;
import static org.egov.infra.utils.ApplicationConstant.CITY_REGION_NAME_KEY;

@Service
@Transactional(readOnly = true)
public class ApplicationIndexService {

    private final ApplicationIndexRepository applicationIndexRepository;
    private final ApplicationDocumentService applicationDocumentService;
    
    @Value("${elasticsearch.enable}")
	private Boolean enable;

    @Autowired
    private CityService cityService;

    @Autowired
    public ApplicationIndexService(ApplicationIndexRepository applicationIndexRepository,
                                   ApplicationDocumentService applicationDocumentService) {
        this.applicationIndexRepository = applicationIndexRepository;
        this.applicationDocumentService = applicationDocumentService;
    }

    @Transactional
    public ApplicationIndex createApplicationIndex(ApplicationIndex applicationIndex) {
    	if(enable){
        Map<String, Object> cityInfo = cityService.cityDataAsMap();
        applicationIndex.setCityCode(defaultString((String) cityInfo.get(CITY_CODE_KEY)));
        applicationIndex.setCityName(defaultString((String) cityInfo.get(CITY_NAME_KEY)));
        applicationIndex.setCityGrade(defaultString((String) cityInfo.get(CITY_CORP_GRADE_KEY)));
        applicationIndex.setDistrictName(defaultString((String) cityInfo.get(CITY_DIST_NAME_KEY)));
        applicationIndex.setRegionName(defaultString((String) cityInfo.get(CITY_REGION_NAME_KEY)));
        applicationIndexRepository.save(applicationIndex);
        applicationDocumentService.createOrUpdateApplicationDocument(applicationIndex);
        return applicationIndex;
    	}
    	else
    		return null;
    }

    @Transactional
    public ApplicationIndex updateApplicationIndex(ApplicationIndex applicationIndex) {
    	if(enable){
        applicationIndexRepository.save(applicationIndex);
        applicationDocumentService.createOrUpdateApplicationDocument(applicationIndex);
        return applicationIndex;
    	}
        else
        	return null;
    }

    public ApplicationIndex findByApplicationNumber(String applicationNumber) {
    	if(enable){
        return applicationIndexRepository.findByApplicationNumberAndCityName(applicationNumber,
                ApplicationThreadLocals.getCityName());}
    	else
    		return null;
    }

}
