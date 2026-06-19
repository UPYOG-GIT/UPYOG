package org.egov.bpa.web.controller;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.egov.bpa.service.BPAService;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
import org.egov.bpa.web.model.ProposalDetails;



@Slf4j
@RestController
@RequestMapping("/v1/proposalDetails")
public class ProposalController {


    @Autowired
    private BPAService bpaService;


    @GetMapping(value = "/getDetails")
    public ResponseEntity<ProposalDetails> getDetails(@RequestParam String tenantId
            , @RequestParam("startDate") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate
            , @RequestParam("endDate") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate) {

        log.info("STARTED :: genetaing Status of tenantId = " + tenantId);
        ProposalDetails details;
        if(validateTenantId(tenantId)) {
            details = bpaService.countStatuses(tenantId, startDate, endDate);
            log.info("fetched Status for tenantId = " + tenantId);
        } else {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(details, HttpStatus.OK);
    }


    public boolean validateTenantId(String tenantId) {
        List<String> ulbList = Arrays.asList("cg.dhamtari", "cg.bhilaicharoda", "cg.birgaon");
        return ulbList.contains(tenantId);
    }
}