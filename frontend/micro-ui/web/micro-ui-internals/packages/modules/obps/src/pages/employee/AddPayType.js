import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams, useHistory } from "react-router-dom";
import {
  Card,Header,CardLabel,TextInput,SubmitBar,Toast, CardSectionHeader, CardSubHeader, CheckPoint, ConnectingCheckPoints, Loader, Row, StatusTable
} from "@egovernments/digit-ui-react-components";


const AddPayType = () => {

const [showToast, setShowToast] = useState(null);
const [postFee,setPostFee] = useState();
const { id } = useParams();
  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  

  const { data = {}, isLoading } = Digit.Hooks.obps.useBPADetailsPage(tenantId, { applicationNo: id });
  
  
  if (isLoading) {
    return <Loader />;
  }

  function setPostFees(e) {
    setPostFee(e.target.value);
  }

let demandCreateres={};
let fetchBillRess={};

const onSubmits= async (e)=>{
  e.preventDefault();

const data = {
    tenantId: tenantId,
    consumerCode: id,
    consumerType: "",
    businessService: "BPA.NC_SAN_FEE",
    taxPeriodFrom: "1554076799000",
    taxPeriodTo: "1585679399000",
    demandDetails: [
        {
            taxHeadMasterCode: "BPA_SANC_FEES",
            taxAmount: postFee,
            collectionAmount: 0
        }
    ],
    minimumAmountPayable: 1,
    additionalDetails: {
        HI: "hii"
    }

};

const demandCreateres = await Digit.PaymentService.demandCreate({Demands:[{...data}]});

if(demandCreateres.ResponseInfo.status === "201 CREATED"){
  fetchBillRess = await Digit.PaymentService.fetchBill(tenantId, { consumerCode: id, businessService: "BPA.NC_SAN_FEE"});
  
  if(fetchBillRess?.Bill?.[0]?.billDetails?.length > 0){
    setShowToast({ key: true, label: "SUCCESS" });

  }else{
    setShowToast({ key: true, label: "FAILL" });
  }
 
}
else{
  setShowToast({ key: true, label: "Demand Fail" });
  
}

};

  return (
    

    <Card style={{ position: "relative" }} className={"employeeCard-override"}>
       
       <Header styles={{marginLeft:"0px", paddingTop: "10px", fontSize: "32px"}}>{t("CS_TITLE_APPLICATION_DETAILS")}</Header>
       {data?.applicationDetails?.map((detail, index) => (

        <React.Fragment key={index}>
          <div>
            { 
            detail?.title == "BPA_BASIC_DETAILS_TITLE"?(
             <React.Fragment>
               {/* let validation = { } */}
              <table style={{marginLeft:"50px",justifyContent:"space-between",fontSize: "16px", lineHeight: "19px", color:"rgb(11, 12, 12)",}}>
               {detail?.values?.map((value, index)=>(
                  // console.log("value ----"+JSON.stringify(value))
                  <tr><td style={{padding: "15px"}}>{t(value.title)}</td><td style={{paddingLeft: "5rem"}}>{t(value.value)}</td></tr>
                  
                ))}
                <tr>
      
                  <td><CardLabel>Add charges</CardLabel> </td>
                  <td><form><td><TextInput
                       isMandatory={true}
                        optionKey="i18nKey"
                      // t={t}
                       name="postFee"
                       onChange={setPostFees}
                  
                    
                  /></td>
                  {/* <button type="submit" className="btn btn-success">ok</button> */}
                  <td><SubmitBar
                        label={t("SUBMIT")}
                        onSubmit={onSubmits}
                        style={{ margin: "10px 0px 0px 0px" }}
                      /></td>
                  </form></td>
                
                </tr>
           </table></React.Fragment>
            ):""}
          </div>
        </React.Fragment>
         

         
        ))}
 
     
      <React.Fragment></React.Fragment>
      {showToast && (
        <Toast
          error={showToast.key}
          label={t(showToast.label)}
          onClose={() => {
            setShowToast(null);
          }}
        />
      )}
    </Card>
    
 
  );
};

export default AddPayType;