import { useQuery, useQueryClient } from "react-query";

export const useFetchCitizenBillsForBuissnessService = ({ businessService, ...filters }, config = {}) => {
  const queryClient = useQueryClient();
  const { mobileNumber, tenantId } = Digit.UserService.getUser()?.info || {};
  const params = { mobileNumber, businessService, ...filters };
  if (!params["mobileNumber"]) delete params["mobileNumber"];
  const { isLoading, error, isError, data, status } = useQuery(
    ["citizenBillsForBuisnessService", businessService, { ...params }],
    () => Digit.PaymentService.fetchBill(tenantId, { ...params }),
    {
      refetchOnMount: true,
      retry: false,
      ...config,
    }
  );
  return {
    isLoading,
    error,
    isError,
    data,
    status,
    revalidate: () => queryClient.invalidateQueries(["citizenBillsForBuisnessService", businessService]),
  };
};

export const useFetchBillsForBuissnessService = ({ tenantId, businessService, ...filters }, config = {}) => {
  const queryClient = useQueryClient();
  let isPTAccessDone = sessionStorage.getItem("IsPTAccessDone");
  const params = { businessService, ...filters };
  const _tenantId = tenantId || Digit.UserService.getUser()?.info?.tenantId;
  const { isLoading, error, isError, data, status } = useQuery(
    ["billsForBuisnessService", businessService, { ...filters }, config, isPTAccessDone],
    () => Digit.PaymentService.fetchBill(_tenantId, params),
    {
      retry: (count, err) => {
        return false;
      },
      ...config,
    }
  );
  return {
    isLoading,
    error,
    isError,
    data,
    status,
    revalidate: () => queryClient.invalidateQueries(["billsForBuisnessService", businessService]),
  };
};

export const useFetchPayment = ({ tenantId, consumerCode, businessService }, config) => {
  const queryClient = useQueryClient();

  const fetchBill = async () => {
    /*  Currently enabled the logic to get bill no and expiry date for PT Module  */
    if (businessService?.includes("PT")) {
      const fetchedBill = await Digit.PaymentService.fetchBill(tenantId, { consumerCode, businessService });
      const billdetail=fetchedBill?.Bill?.[0]?.billDetails?.sort((a, b) => b.fromPeriod - a.fromPeriod)?.[0]||{};
      fetchedBill.Bill[0].billDetails=fetchedBill.Bill[0].billDetails.map(ele=>({
        ...ele,
        currentBillNo: fetchedBill?.Bill?.[0]?.billNumber,
        currentExpiryDate: billdetail?.expiryDate,
      }))
      if (fetchedBill && fetchedBill?.Bill?.[0]?.billDetails?.length > 1) {
        fetchedBill?.Bill?.[0]?.billDetails?.map(async (billdet) => {
          const searchBill = await Digit.PaymentService.searchBill(tenantId, {
            consumerCode,
            fromPeriod: billdet?.fromPeriod,
            toPeriod: billdet?.toPeriod,
            service: businessService,
            retrieveOldest: true,
          });
          billdet.expiryDate = searchBill?.Bill?.[0]?.billDetails?.[0]?.expiryDate;
          billdet.billNumber = searchBill?.Bill?.[0]?.billNumber;
        });
      }
      return fetchedBill;
    } else {
      return Digit.PaymentService.fetchBill(tenantId, { consumerCode, businessService });
    }
  };

  const retry = (failureCount, error) => {
    if (error?.response?.data?.Errors?.[0]?.code === "EG_BS_BILL_NO_DEMANDS_FOUND") return false;
    else return failureCount < 3;
  };

  const queryData = useQuery(["paymentFetchDetails", tenantId, consumerCode, businessService], () => fetchBill(), { retry, ...config });

  return {
    ...queryData,
    revalidate: () => queryClient.invalidateQueries(["paymentFetchDetails", tenantId, consumerCode, businessService]),
  };
};

export const usePaymentUpdate = ({ egId }, businessService, config) => {
  // console.log("object........"+JSON.stringify(config) + "egid "+egId+"BS "+businessService);
  const getPaymentData = async (egId) => {
    // console.log("Hii")
    const transaction = await Digit.PaymentService.updateCitizenReciept(egId);
    const payments = await Digit.PaymentService.getReciept(transaction.Transaction[0].tenantId, businessService, {
      consumerCodes: transaction.Transaction[0].consumerCode,
    });
    // // console.log("transactin: "+JSON.stringify(transaction));
    // // console.log("payments: "+JSON.stringify(payments));
    return { payments, applicationNo: transaction.Transaction[0].consumerCode, txnStatus: transaction.Transaction[0].txnStatus };
  };
  // console.log("object111 exit from usePaymentUpdate");
  return useQuery(["paymentUpdate", egId], () => getPaymentData(egId), config);
};

export const useGetPaymentRulesForBusinessServices = (tenantId) => {
  return useQuery(["getPaymentRules", tenantId], () => Digit.MDMSService.getPaymentRules(tenantId));
};

export const useDemandSearch = ({ consumerCode, businessService, tenantId }, config = {}) => {
  if (!tenantId) tenantId = Digit.ULBService.getCurrentTenantId();
  const queryFn = () => Digit.PaymentService.demandSearch(tenantId, consumerCode, businessService);
  const queryData = useQuery(["demand_search", { consumerCode, businessService, tenantId }], queryFn, { refetchOnMount: "always", ...config });
  return queryData;
};

export const useRecieptSearch = ({ tenantId, businessService, ...params }, config = {}) => {
  return useQuery(
    ["reciept_search", { tenantId, businessService, params }],
    () => Digit.PaymentService.recieptSearch(tenantId, businessService, params),
    {
      refetchOnMount: false,
      ...config,
    }
  );
};
