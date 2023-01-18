package org.egov.pg.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pg.config.AppProperties;
import org.egov.pg.models.Bill;
import org.egov.pg.models.BillDetail;
import org.egov.pg.models.Receipt;
import org.egov.pg.models.Transaction;
import org.egov.pg.models.Transaction.TxnStatusEnum;
import org.egov.pg.producer.Producer;
import org.egov.pg.repository.TransactionRepository;
import org.egov.pg.validator.TransactionValidator;
import org.egov.pg.web.models.TransactionCriteria;
import org.egov.pg.web.models.TransactionRequest;
import org.egov.pg.web.models.User;
import org.egov.tracer.model.CustomException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.TransientDataAccessResourceException;

import lombok.extern.slf4j.Slf4j;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class TransactionServiceTest {

    private TransactionService transactionService;

    @Mock
    private Producer producer;

    @Mock
    private GatewayService gatewayService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private EnrichmentService enrichmentService;

    @Mock
    private AppProperties appProperties;

    @Mock
    private TransactionValidator validator;
    
    @Mock
    private PaymentsService paymentsService;

    private User user;
    private RequestInfo requestInfo;

    @Before
    public void setUp() {
        user = User.builder().userName("USER001").mobileNumber("9XXXXXXXXX").name("XYZ").tenantId("pb").emailId("").build();
        requestInfo = new RequestInfo("", "", 0L, "", "", "", "", "", "", null);
        lenient().when(gatewayService.getTxnId(any(Map.class))).thenReturn(Optional.of("ORDERID"));
        lenient().doNothing().when(producer).push(any(String.class), any(Object.class));
        lenient().doNothing().when(enrichmentService).enrichCreateTransaction(any(TransactionRequest.class));

        this.transactionService = new TransactionService(validator, gatewayService, producer, transactionRepository,
        		paymentsService,
                enrichmentService,
                appProperties);
    }

    /**
     * Valid test for initiating a transaction
     * @throws URISyntaxException
     */

    @Test
    public void initiateTransactionSuccessTest() throws URISyntaxException {
//        String redirectUrl = "https://paytm.com";
        String redirectUrl = "https://test.ccavenue.com/transaction/transaction.do?command=initiateTransaction&encRequest=1e85fc7dd3264d727280f2dda5b4e9736df9be6ba4e5fe5bf8630bf8ad62fb840b9cfadfcea178cf0905ae25f4cdc5c53d6e021a88b656cf4e48bea50e323ae62d23de00dc7261344334e194b28c06c108c237db32afeb656dccf685d5fc4cfebcf74512ce777ba3fd14321fcec042344e34eb305d4e8e07e75c9d6babc54bf4c1f07f7467e749c4555d51214d13ee910074766620a426cec9a85ee2920bf60f1a13f7126d4c02a8436047bfb5d138d3333c76f41198cd4496625f40d65b74cbd0a99bc418b363e9040363b0927ed60172e16a1ef2a96344477b9a5177030f1d69ea5c27cc9c10873d1908b9cfa10a6b8b36060d4ba3ce38ac00fa697ad9137770623b58c87585245497760d113e63870754920c69bf98925c9c7a5ffc71f57da204730afe0c36fd4d1010a766a384044755be1b151d74cde601969a64d229eeedbfd0b1e021f8e59ae83c843f5e00d737a9913287f5e79eab7b842f52d3958ceab782828fe1e01d5ee8d064d0bea6031b3c2e047dae374aa545bc7182275c82e12aff62b98c12a4423ef0e67b8dc1b94897d1b8900f896177bde68ed4ed7ee70c06d70b2352d04a74a9a74488763251c67b57473e2d744ae1fe5a8744e78336b7e96a43238152c24143577db1005695dd85f165682fb0d29584c8a3759dc4ea5d863306ff105ed90388c5bdae933f1b899cd4ec13ed93442e324e076a04b7c40e06f25d1339c3d1dd2101fecb3ab18c5cf3d72aa80590728f5206ae0012cbab61fe0b2d0209e7cf6becb523396e8ed96ad0230c249d34263cb198a9b444e253f1b6618d48550a3d4a8682649609eada0089189ebaa8a2eecd912fb11ea21ce811bfcda8ac81a7de26ee1f46f5e0306416f66b2915b6f4f115e4048a825a2aa6e107df5a1bcde0983deb813b2fe7f2059dbccbf4a5bd51200bf1fb4dd4b7e6c11666eec2e9b19b32cd42ca13dd62b9efc077aa592b68541455c4404ce9844eff8a5413cb9aac611fe4fc8cb328b5425729667dc613de0a04aa4bb740058b6957&access_code=AVII96KA89BB16IIBB";


        Transaction txn = Transaction.builder().txnAmount("2")
                .billId("a5dbfde8-9b6b-4bac-9966-9df67d632462")
                .productInfo("Common Payment")
                .gateway("CCAVENUE")
                .build();
//        Transaction txn = Transaction.builder().txnAmount("100")
//        		.billId("ORDER0012")
//        		.productInfo("Property Tax Payment")
//        		.gateway("PAYTM")
//        		.build();
        TransactionRequest transactionRequest = new TransactionRequest(requestInfo, txn);

        Mockito.doNothing().when(validator).validateCreateTxn(any(TransactionRequest.class));
        when(validator.skipGateway(txn)).thenReturn(false);
        when(gatewayService.initiateTxn(any(Transaction.class))).thenReturn(new URI(redirectUrl));

        Transaction resp = transactionService.initiateTransaction(transactionRequest);

        assertTrue(resp.getRedirectUrl().equalsIgnoreCase(redirectUrl));

    }

    /**
     * Test for invalid or inactive gateway
     */
    @Test(expected = CustomException.class)
    public void initiateTransactionFailTest(){
        Transaction txn = Transaction.builder().txnAmount("100")
                .billId("ORDER0012")
                .productInfo("Property Tax Payment")
                .gateway("ABCD123")
                .build();
        TransactionRequest transactionRequest = new TransactionRequest(requestInfo, txn);

        Mockito.doThrow(new CustomException("INVALID_GATEWAY", "Invalid Gateway")).when(validator).validateCreateTxn(any(TransactionRequest.class));
        lenient().when(gatewayService.initiateTxn(any(Transaction.class))).thenThrow(new CustomException());

        Transaction resp = transactionService.initiateTransaction(transactionRequest);
    }

    /**
     * Test for invalid or inactive gateway
     */
    @Test
    public void initiateTransactionSkipGatewayTest(){
        Transaction txn = Transaction.builder().txnAmount("100")
                .billId("ORDER0012")
                .productInfo("Property Tax Payment")
                .gateway("ABCD123")
                .txnAmount("0")
                .build();
        TransactionRequest transactionRequest = new TransactionRequest(requestInfo, txn);

        Mockito.doNothing().when(validator).validateCreateTxn(any(TransactionRequest.class));

        lenient().when(gatewayService.initiateTxn(any(Transaction.class))).thenThrow(new CustomException());
        lenient().when(validator.skipGateway(txn)).thenReturn(true);
        Transaction resp = transactionService.initiateTransaction(transactionRequest);
                
        assertTrue(resp.getTxnStatus().equals(TxnStatusEnum.SUCCESS));

    }


    /**
     * Test for fetching transactions based on criteria
     */
    @Test
    public void getTransactionsSuccessTest(){
        Transaction txn = Transaction.builder().txnId("PT_001")
                .txnAmount("100")
                .tenantId("pb")
                .billId("ORDER0012")
                .productInfo("Property Tax Payment")
                .gateway("ABCD123")
                .build();
        TransactionCriteria criteria = TransactionCriteria.builder().tenantId("pb").txnId("PT_001").build();

        when(transactionRepository.fetchTransactions(criteria)).thenReturn(Collections.singletonList(txn));
        assertEquals(1, transactionService.getTransactions(criteria).size());

        when(transactionRepository.fetchTransactions(criteria)).thenReturn(Collections.emptyList());
        assertEquals(0, transactionService.getTransactions(criteria).size());
    }

    /**
     * DB error occurs while running fetch
     */
    @Test(expected = CustomException.class)
    public void getTransactionsFailTest(){
        TransactionCriteria criteria = TransactionCriteria.builder().tenantId("pb").txnId("PT_001").build();
        when(transactionRepository.fetchTransactions(criteria)).thenThrow(new TransientDataAccessResourceException("test"));

        transactionService.getTransactions(criteria);
    }

    @Test
    public void updateTransactionSuccessTest() {

        Transaction txnStatus = Transaction.builder().txnId("PT_001")
                .txnAmount("100")
                .billId("ORDER0012")
                .txnStatus(Transaction.TxnStatusEnum.PENDING)
                .productInfo("Property Tax Payment")
                .gateway("PAYTM")
                .build();

        Transaction finalTxnStatus = Transaction.builder().txnId("PT_001")
                .txnAmount("100.00")
                .billId("ORDER0012")
                .txnStatus(Transaction.TxnStatusEnum.SUCCESS)
                .productInfo("Property Tax Payment")
                .gateway("PAYTM")
                .build();

        when(validator.validateUpdateTxn(any(Map.class))).thenReturn(txnStatus);
        when(validator.skipGateway(any(Transaction.class))).thenReturn(false);
        when(validator.shouldGenerateReceipt(any(Transaction.class), any(Transaction.class))).thenReturn(true);
        when(gatewayService.getLiveStatus(txnStatus, Collections.singletonMap("ORDERID", "PT_001"))).thenReturn(finalTxnStatus);


        assertEquals(transactionService.updateTransaction(requestInfo, Collections.singletonMap
                ("ORDERID", "PT_001")).get(0).getTxnStatus(), Transaction.TxnStatusEnum.SUCCESS);
    }

    /**
     * Invalid transaction id key,
     *  ex, ORDERID, specific to gateway
     */
    @Test(expected = CustomException.class)
    public void updateTransactionFailTest(){

        when(validator.validateUpdateTxn(any(Map.class))).thenThrow(new CustomException("MISSING_TXN_ID", "Cannot process request, missing transaction id"));

        transactionService.updateTransaction(requestInfo, Collections.singletonMap("abc", "PT_001"));

    }

    /**
     * No record of the Transaction exists in DB
     */
    @Test(expected = CustomException.class)
    public void updateTransactionInvalidTxnIdTest() {

        when(validator.validateUpdateTxn(any(Map.class))).thenThrow(new CustomException("TXN_NOT_FOUND", "Transaction not found"));

        transactionService.updateTransaction(requestInfo, Collections.singletonMap("abc", "PT_001"));
    }
}
