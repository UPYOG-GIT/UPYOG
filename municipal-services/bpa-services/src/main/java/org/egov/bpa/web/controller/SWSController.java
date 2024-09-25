package org.egov.bpa.web.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.service.BPAService;
import org.egov.bpa.service.SwsService;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAErrorConstants;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.util.ResponseInfoFactory;
import org.egov.bpa.web.model.BCategoryRequest;
import org.egov.bpa.web.model.BCategoryRequestWrapper;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.BPAResponse;
import org.egov.bpa.web.model.BPASearchCriteria;
import org.egov.bpa.web.model.BSCategoryRequest;
import org.egov.bpa.web.model.BSCategoryRequestWrapper;
import org.egov.bpa.web.model.IngestRequest;
import org.egov.bpa.web.model.PayTpRateRequest;
import org.egov.bpa.web.model.PayTpRateRequestWrapper;
import org.egov.bpa.web.model.PayTypeFeeDetailRequest;
import org.egov.bpa.web.model.PayTypeFeeDetailRequestWrapper;
import org.egov.bpa.web.model.PayTypeRequest;
import org.egov.bpa.web.model.PayTypeRequestWrapper;
import org.egov.bpa.web.model.ProposalTypeRequest;
import org.egov.bpa.web.model.ProposalTypeRequestWrapper;
import org.egov.bpa.web.model.RequestInfoWrapper;
import org.egov.bpa.web.model.SlabMasterRequest;
import org.egov.bpa.web.model.SlabMasterRequestWrapper;
import org.egov.ndb.service.NationalDashboardService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/sws")
public class SWSController {

	
	
	@Autowired
	SwsService swsService;
	
	
	@GetMapping(value = "/_getstatuscodelist")
	public ResponseEntity<String> pushDatatoSws(@RequestBody String dataRequest){
		return swsService.getStatusCodeList();
//		return null;
	}
	
}
