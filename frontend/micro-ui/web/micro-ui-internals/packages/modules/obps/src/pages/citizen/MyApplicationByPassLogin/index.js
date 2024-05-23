import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Card, KeyNote, Loader, SubmitBar, Header } from "@egovernments/digit-ui-react-components";
import { Fragment } from "react";
import { Link, useHistory, useLocation } from "react-router-dom";
import { getBPAFormData } from "../../../utils/index";

const getServiceType = () => {
  return `BPA_APPLICATIONTYPE_BUILDING_PLAN_SCRUTINY`;
};

const MyApplicationByPassLogin = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);
  const location = useLocation();
  // Function to parse the query string
  const getQueryParams = (query) => {
    return new URLSearchParams(query);
  };

  const queryParams = getQueryParams(location.search);
  const token = queryParams.get("token"); // Get the 'token' parameter

  console.log("token111 " + token);
  // if (token != null) {
  useEffect(async () => {
    try {
      const requestData = {
        username: token,
        password: 123456,
        //tenantId: stateCode,
        tenantId: "cg.birgaon",
        userType: "citizen",
      };

      const { ResponseInfo, UserRequest: info, ...tokens } = await Digit.UserService.authenticate(requestData);
      console.log("info :"+JSON.stringify(info));
      console.log("tokens: "+tokens)
      const usersResponse1 = await Digit.UserService.userSearch(info?.tenantId, { uuid: [info?.uuid] }, {});

      // const usersResponse = await Digit.UserService.userSearch("cg.birgaon", { mobileNumber: token, tenantId:"cg.birgaon" }, {});
      console.log("usersResponse: " + JSON.stringify(usersResponse1));

      setUser({ info, ...tokens });
      Digit.SessionStorage.set("citizen.userRequestObject", user);
      Digit.UserService.setUser(user);

      console.log("user : "+JSON.stringify(user));
      console.log("Digit.UserService.getUser() "+JSON.stringify(Digit.UserService.getUser()));
      setLoading(false);
    } catch (err) {
      // setError(err);
    } 
    // finally {
      
    // }
  }, []);
  // }

  const history = useHistory();

  // if (!loading) {
  const [finalData, setFinalData] = useState([]);
  const [labelMessage, setLableMessage] = useState(false);
  // const tenantId = Digit.ULBService.getCurrentTenantId();
  const tenantId = Digit.ULBService.getCitizenCurrentTenant();
  const userInfo = Digit.UserService.getUser();
  const requestor = userInfo?.info?.mobileNumber;
  // console.log("requestor : " + JSON.stringify(requestor));
  // console.log("userInfo : " + JSON.stringify(userInfo));
  // const { data, isLoading, revalidate } = Digit.Hooks.obps.useBPAREGSearch(tenantId);
  // const { data: renData, isLoading: isRenSearchLoading, revalidate: renRevlidate } = Digit.Hooks.obps.useBPARENSearch(tenantId);
  const { data: bpaData, isLoading: isBpaSearchLoading, revalidate: bpaRevalidate } = Digit.Hooks.obps.useBPASearch(tenantId, {
    requestor,
    limit: -1,
    offset: 0,
  });
  const { isMdmsLoading, data: mdmsData } = Digit.Hooks.obps.useMDMS(Digit.ULBService.getStateId(), "BPA", ["RiskTypeComputation"]);

  useEffect(() => {
    return () => {
      setFinalData([]);
      revalidate?.();
      bpaRevalidate?.();
    };
  }, []);

  useEffect(() => {
    if (!isBpaSearchLoading) {
      let searchConvertedArray = [];
      let sortConvertedArray = [];

      if (bpaData?.length) {
        bpaData?.forEach((bpaDta) => {
          bpaDta.sortNumber = 0;
          bpaDta.modifiedTime = bpaDta.auditDetails.lastModifiedTime;
          bpaDta.type = "BPA";
          searchConvertedArray.push(bpaDta);
        });
      }
      sortConvertedArray = [].slice.call(searchConvertedArray).sort(function (a, b) {
        return new Date(b.modifiedTime) - new Date(a.modifiedTime) || a.sortNumber - b.sortNumber;
      });
      setFinalData(sortConvertedArray);
      let userInfos = sessionStorage.getItem("Digit.citizen.userRequestObject");
      const userInfoDetails = userInfos ? JSON.parse(userInfos) : {};
      if (userInfoDetails?.value?.info?.roles?.length == 1 && userInfoDetails?.value?.info?.roles?.[0]?.code == "CITIZEN") setLableMessage(true);
    }
  }, [isBpaSearchLoading, bpaData]);

  if (isBpaSearchLoading) {
    return <Loader />;
  }
  // } else{
  //   return <Loader />;
  // }

  if (loading) {
    return <Loader />;
  }

  return (
    <Fragment>
      <Header>{`${t("BPA_MY_APPLICATIONS")} (${bpaData?.length})`}</Header>
      {finalData?.map((application, index) => {
        return (
          <Card key={index}>
            <KeyNote keyValue={t("BPA_APPLICATION_NUMBER_LABEL")} note={application?.applicationNo} />
            <KeyNote
              keyValue={t("BPA_BASIC_DETAILS_APPLICATION_TYPE_LABEL")}
              note={application?.businessService !== "BPA_OC" ? t(`WF_BPA_BUILDING_PLAN_SCRUTINY`) : t(`WF_BPA_BUILDING_OC_PLAN_SCRUTINY`)}
            />
            <KeyNote keyValue={t("BPA_COMMON_SERVICE")} note={t(`BPA_SERVICETYPE_NEW_CONSTRUCTION`)} />
            <KeyNote
              keyValue={t("TL_COMMON_TABLE_COL_STATUS")}
              note={t(`WF_BPA_${application?.state}`)}
              noteStyle={application?.status === "APPROVED" ? { color: "#00703C" } : { color: "#D4351C" }}
            />
            <KeyNote keyValue={t("BPA_COMMON_SLA")} note={application?.sla} />
            {application.action === "SEND_TO_ARCHITECT" || application.status !== "INITIATED" ? (
              <Link to={{ pathname: `/digit-ui/citizen/obps/bpa/${application?.applicationNo}`, state: { tenantId: "" } }}>
                <SubmitBar label={t("TL_VIEW_DETAILS")} />
              </Link>
            ) : (
              <div>
                {labelMessage ? (
                  <Link to={{ pathname: `/digit-ui/citizen/obps/bpa/${application?.applicationNo}`, state: { tenantId: "" } }}>
                    <SubmitBar label={t("TL_VIEW_DETAILS")} />
                  </Link>
                ) : (
                  <SubmitBar label={t("BPA_COMP_WORKFLOW")} onSubmit={() => getBPAFormData(application, mdmsData, history, t)} />
                )}
              </div>
            )}
          </Card>
        );
      })}
    </Fragment>
  );
};

export default MyApplicationByPassLogin;
