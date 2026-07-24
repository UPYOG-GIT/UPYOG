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
import org.springframework.web.server.ResponseStatusException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import org.egov.bpa.web.model.LabourDepartmentDetails;



@Slf4j
@RestController
@RequestMapping("/v1/labourDepartment")
public class LabourDepartmentDetailsController {

        @Autowired
        private BPAService bpaService;


        @GetMapping(value = "/details")
        public ResponseEntity<List<LabourDepartmentDetails>> getDetails(@RequestParam("todayDate") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate todayDate) {
            log.info("STARTED :: Generating Details for labour department, for date = " + todayDate);
            List<LabourDepartmentDetails> details = null;
            try {
                details = bpaService.getLabourDepartmentDetails(todayDate);
                log.info("ENDED :: Successfully generated Details for labour department, for date : + " + todayDate);
                return new ResponseEntity<>(details, HttpStatus.OK);
            } catch (java.lang.Exception e) {
                log.error("Validation failed for tenantId = " + e);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Date Format provided");
            }
        }
}