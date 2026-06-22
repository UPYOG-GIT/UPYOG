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
import org.springframework.web.server.ResponseStatusException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;



@Slf4j
@RestController
@RequestMapping("/v1/proposalDetails")
public class ProposalController {

    private static final Set<String> VALID_TENANTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("cg.dhamtari", "cg.bhilaicharoda", "cg.birgaon")));

    @Autowired
    private BPAService bpaService;


    @GetMapping(value = "/getDetails")
    public ResponseEntity<ProposalDetails> getDetails(@RequestParam String tenantId
            , @RequestParam("startDate") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate startDate
            , @RequestParam("endDate") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate endDate) {

        log.info("STARTED :: Generating Status of tenantId = " + tenantId);
        ProposalDetails details;
        if(validateTenantId(tenantId)) {
            details = bpaService.countStatuses(tenantId, startDate, endDate);
            log.info("Success fully fetched status for tenantId = " + tenantId);
        } else {
            log.error("Validation failed for tenantId = " + tenantId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid tenantId provided: " + tenantId);
        }
        log.info("ENDED :: Successfully generated Status of tenantId = " + tenantId);
        return new ResponseEntity<>(details, HttpStatus.OK);
    }


    public boolean validateTenantId(String tenantId) {
        return tenantId != null && VALID_TENANTS.contains(tenantId);
    }
}