/*
 * @author Bhupesh
 */

package org.entit.rga.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.entit.rga.service.RGAService;
import org.entit.rga.util.RGAUtil;
import org.entit.rga.util.ResponseInfoFactory;
import org.entit.rga.web.model.RGA;
import org.entit.rga.web.model.RGAPenaltyRequest;
import org.entit.rga.web.model.RGAPenaltyRequestWrapper;
import org.entit.rga.web.model.RGARequest;
import org.entit.rga.web.model.RGAResponse;
import org.entit.rga.web.model.RGASearchCriteria;
import org.entit.rga.web.model.RGASlabMasterRequest;
import org.entit.rga.web.model.RGASlabMasterRequestWrapper;
import org.entit.rga.web.model.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rga")
public class RGAController {

	@Autowired
	private RGAService rgaService;

	@Autowired
	private RGAUtil rgaUtil;

	@Autowired
	private ResponseInfoFactory responseInfoFactory;

	@PostMapping(value = "/_create")
	public ResponseEntity<RGAResponse> create(@Valid @RequestBody RGARequest rGARequest) {

//		RegularisationResponse response = new RegularisationResponse();
//		return new ResponseEntity<>(response, HttpStatus.OK);

		rgaUtil.defaultJsonPathConfig();
		RGA rga = rgaService.create(rGARequest);
		List<RGA> rgas = new ArrayList<RGA>();
		rgas.add(rga);
		RGAResponse response = RGAResponse.builder().RGA(rgas)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(rGARequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping(value = "/_update")
	public ResponseEntity<RGAResponse> update(@Valid @RequestBody RGARequest rGARequest) {
		RGA rga = rgaService.update(rGARequest);
//		log.info("bpa---" + regularisation.getApprovalNo());
		List<RGA> rgas = new ArrayList<RGA>();
		rgas.add(rga);
		RGAResponse response = RGAResponse.builder().RGA(rgas)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(rGARequest.getRequestInfo(), true))
				.build();
//		log.info("Response Approval number----" + response.getRegularisation().get(0).getApprovalNo());
		return new ResponseEntity<>(response, HttpStatus.OK);

	}
	
	@PostMapping(value = "/_search")
	public ResponseEntity<RGAResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute RGASearchCriteria criteria) {

		List<RGA> bpas = rgaService.search(criteria, requestInfoWrapper.getRequestInfo());
		int count = rgaService.getBPACount(criteria, requestInfoWrapper.getRequestInfo());
		RGAResponse response = RGAResponse
				.builder().RGA(bpas).responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
				.count(count).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/*
	
	*/
	
	@PostMapping(value = "/_creatergaslab")
	public ResponseEntity<Object> createRGASlabMaster(
			@RequestBody RGASlabMasterRequestWrapper rgaSlabMasterRequestWrapper) {
		try {
			RGASlabMasterRequest rgaSlabMasterRequest = rgaSlabMasterRequestWrapper.getRgaSlabMasterRequest();
			int insertResult = rgaService.createRGASlabMaster(rgaSlabMasterRequest);
			if (insertResult > 0) {
				return new ResponseEntity<>(insertResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(insertResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in createRGASlabMaster: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/_searchrgaslab")
	public ResponseEntity<List<Map<String, Object>>> getSlabMasterByTenantIdAndTypeId(@RequestParam String tenantId) {
		List<Map<String, Object>> sqlResponseList = rgaService.getRGASlabMasterByTenantId(tenantId);
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);
	}
	
	@PostMapping(value = "/_deletergaslab")
	public ResponseEntity<Object> deleteRGASlabMasterById(@RequestBody RGASlabMasterRequestWrapper rgaSlabMasterRequestWrapper) {
		try {
			RGASlabMasterRequest rgaSlabMasterRequest = rgaSlabMasterRequestWrapper.getRgaSlabMasterRequest();
			int deleteResult = rgaService.deleteRGASlabMasterById(rgaSlabMasterRequest.getIds());
			if (deleteResult > 0) {
				return new ResponseEntity<>(deleteResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(deleteResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in deleteRGASlabMasterById: " + ex); 
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping(value = "/_creatergapenalty")
	public ResponseEntity<Object> createRGAPenalty(
			@RequestBody RGAPenaltyRequestWrapper rgaPenaltyRequestRequestWrapper) {
		try {
			RGAPenaltyRequest rgaPenaltyRequest = rgaPenaltyRequestRequestWrapper.getRgaPenaltyRequest();
			int insertResult = rgaService.createRGAPenalty(rgaPenaltyRequest);
			if (insertResult > 0) {
				return new ResponseEntity<>(insertResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(insertResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in createRGAPenalty: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping(value = "/_searchrgapenalty")
	public ResponseEntity<List<Map<String, Object>>> getRGAPenaltyByTenantId(@RequestParam String tenantId) {
		List<Map<String, Object>> sqlResponseList = rgaService.getRGAPenaltyByTenantId(tenantId);
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);
	}
	
	@PostMapping(value = "/_deletergapenalty")
	public ResponseEntity<Object> deleteRGAPenaltyById(@RequestBody RGAPenaltyRequestWrapper rgaPenaltyRequestRequestWrapper) {
		try {
			RGAPenaltyRequest rgaPenaltyRequest = rgaPenaltyRequestRequestWrapper.getRgaPenaltyRequest();
			int deleteResult = rgaService.deleteRGAPenaltyById(rgaPenaltyRequest.getIds());
			if (deleteResult > 0) {
				return new ResponseEntity<>(deleteResult, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(deleteResult, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			log.error("Exception in deleteRGAPenaltyById: " + ex); 
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
	}
	
}
