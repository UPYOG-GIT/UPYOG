/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) 2016  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.collection.web.controller;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.egov.collection.model.Payment;
import org.egov.collection.model.PaymentSearchCriteriaWrapper;
import org.egov.collection.service.PaymentReceipt;
import org.egov.collection.service.PaymentReceiptV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/receipts")
@Slf4j
public class ReceiptController {

	@Autowired
	private PaymentReceipt paymentReceipt;
	
	@Autowired
	private PaymentReceiptV2 paymentReceiptV2;

	@RequestMapping(value = "/_getReceipt", method = RequestMethod.POST, produces = MediaType.APPLICATION_PDF_VALUE)
	@ResponseBody
	public ResponseEntity<InputStreamResource> getReceipt(
			@RequestBody final PaymentSearchCriteriaWrapper paymentSearchCriteriaWrapper) throws FileNotFoundException {

		ByteArrayInputStream bis = paymentReceipt.getPaymentReceipt(paymentSearchCriteriaWrapper);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "inline; filename=paymentReceipt.pdf");
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));

	}
	
	@RequestMapping(value = "/v2/_getReceipt", method = RequestMethod.POST, produces = MediaType.APPLICATION_PDF_VALUE)
	@ResponseBody
	public ResponseEntity<InputStreamResource> getReceiptV2(
			@RequestBody final List<Payment> payments) throws FileNotFoundException {
		
		ByteArrayInputStream bis = paymentReceiptV2.getPaymentReceipt(payments);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "inline; filename=paymentReceipt.pdf");
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));
		
	}

}
