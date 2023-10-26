package org.egov.pg.web.controllers;

import lombok.extern.slf4j.Slf4j;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.pg.constants.PgConstants;
import org.egov.pg.models.Transaction;
import org.egov.pg.service.GatewayService;
import org.egov.pg.service.TransactionService;
import org.egov.pg.utils.ResponseInfoFactory;
import org.egov.pg.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Endpoints to deal with all payment related operations
 */

@Slf4j
@Controller
public class TransactionsApiController {

	private final TransactionService transactionService;
	private final GatewayService gatewayService;
	private static RequestInfo requestInfo;

	@Autowired
	public TransactionsApiController(TransactionService transactionService, GatewayService gatewayService) {
		this.transactionService = transactionService;
		this.gatewayService = gatewayService;
	}

	/**
	 * Initiates a new payment transaction, on successful validation, a redirect is
	 * issued to the payment gateway.
	 *
	 * @param transactionRequest Request containing all information necessary for
	 *                           initiating payment
	 * @return Transaction that has been created
	 */
	@RequestMapping(value = "/transaction/v1/_create", method = RequestMethod.POST)
	public ResponseEntity<TransactionCreateResponse> transactionsV1CreatePost(
			@Valid @RequestBody TransactionRequest transactionRequest) {

		Transaction transaction = transactionService.initiateTransaction(transactionRequest);
		log.info("transaction : -----" + transaction);
		ResponseInfo responseInfo = ResponseInfoFactory
				.createResponseInfoFromRequestInfo(transactionRequest.getRequestInfo(), true);
		log.info("responseInfo Status : -----" + responseInfo.getStatus());

		TransactionCreateResponse response = new TransactionCreateResponse(responseInfo, transaction);
		log.info("response TxnStatusMsg: -----" + response.getTransaction().getTxnStatusMsg());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Returns the current status of a transaction in our systems; This does not
	 * guarantee live payment gateway status.
	 *
	 * @param requestInfoWrapper  Request Info
	 * @param transactionCriteria Search Conditions that should be matched
	 * @return List of transactions matching the search criteria
	 */
	@RequestMapping(value = "/transaction/v1/_search", method = RequestMethod.POST)
	public ResponseEntity<TransactionResponse> transactionsV1SearchPost(
			@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute TransactionCriteria transactionCriteria) {
		transactionCriteria.setOffset(0);
		transactionCriteria.setLimit(5);
		List<Transaction> transactions = transactionService.getTransactions(transactionCriteria);
		ResponseInfo responseInfo = ResponseInfoFactory
				.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true);
		TransactionResponse response = new TransactionResponse(responseInfo, transactions);

		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	/**
	 * Updates the status of the transaction from the gateway
	 *
	 * @param params Parameters posted by the gateway
	 * @return The current transaction status of the transaction
	 */
	@RequestMapping(value = "/transaction/v1/_update", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<TransactionResponse> transactionsV1UpdatePost(
			@RequestBody RequestInfoWrapper requestInfoWrapper, @RequestParam Map<String, String> params) {
		log.info("inside /transaction/v1/_update api......");
		String txnId = params.get("transactionId");
		String encResp = transactionService.getResponse(txnId);
		log.info("encResp: " + encResp);
		params.put("encResp", encResp);
		params.put("FromUpdateAPI", "true");
		log.info("params.toString(): " + params.toString());
		List<Transaction> transactions = transactionService.updateTransaction(requestInfoWrapper.getRequestInfo(),
				params);
		ResponseInfo responseInfo = ResponseInfoFactory
				.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true);
		TransactionResponse response = new TransactionResponse(responseInfo, transactions);
//        log.info("response :"+response.getTransactions().get(0).getTxnId());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/transaction/v1/_manualupdate", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<TransactionResponse> transactionsV1ManualUpdatePost(@RequestParam String txnId) {
		log.info("inside /transaction/v1/_manualupdate api......");
		User userInfo = User.builder().uuid("c5ef71e8-eaf4-4faa-9b10-065e76162d0e").type("SYSTEM")
				.roles(Collections.emptyList()).id(0L).build();

		requestInfo = new RequestInfo("", "", 0L, "", "", "", "", "", "", userInfo);
		List<Transaction> transactions = transactionService.updateTransaction(requestInfo,
				Collections.singletonMap(PgConstants.PG_TXN_IN_LABEL, txnId));
		ResponseInfo responseInfo = ResponseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true);
		TransactionResponse response = new TransactionResponse(responseInfo, transactions);
//        log.info("response :"+response.getTransactions().get(0).getTxnId());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Active payment gateways that can be used for payments
	 *
	 * @return list of active gateways that can be used for payments
	 */
	@RequestMapping(value = "/gateway/v1/_search", method = RequestMethod.POST)
	public ResponseEntity<Set<String>> transactionsV1AvailableGatewaysPost() {

		Set<String> gateways = gatewayService.getActiveGateways();
		log.debug("Available gateways : " + gateways);
		return new ResponseEntity<>(gateways, HttpStatus.OK);
	}

	@PostMapping(value = "/_searchtransactions")
	public ResponseEntity<List<Map<String, Object>>> getTransactions(@RequestParam String applicationNumber) {
		List<Map<String, Object>> sqlResponseList = transactionService.getTransactions(applicationNumber);
		return new ResponseEntity<>(sqlResponseList, HttpStatus.OK);
	}

	@PostMapping(value = "/_deleteTransaction")
	public ResponseEntity<Object> deleteTransaction(@RequestParam int txnId) {
		try {
			int deleteResult = transactionService.deleteTransaction(txnId);
			return new ResponseEntity<>(deleteResult, HttpStatus.OK);
		} catch (Exception ex) {
			log.error("Exception in deleteTransaction: " + ex);
			return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
		}
	}

}
