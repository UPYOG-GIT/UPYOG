
import { StatusTable,BackButton, SearchField, TextInput, SubmitBar, Header, Card, CardHeader, Row, PDFSvg, CardSectionHeader,MultiLink, Loader } from "@egovernments/digit-ui-react-components";


import React, { Fragment, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { fromUnixTime, format } from 'date-fns';
import { OBPSService } from "../../../../libraries/src/services/elements/OBPS";
// import { MdmsService } from "../../../../../../libraries/src/services/elements/MDMS";
// import { pdfDocumentName, pdfDownloadLink, stringReplaceAll } from "../../../utils";
// import ApplicationTimeline from "../../../components/ApplicationTimeline";



const SanctionFeePaymentUpdate = ({ formState, register, reset, previousPage }) => {
  const { t } = useTranslation();
  const [applicationNo, setApplicationNo] = useState("");
  const [applicationData, setApplicationData] = useState([]);

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      const data = await Digit.OBPSAdminService.searchByApplicationNo('cg', applicationNo);
    //   console.log("searchByApplicationNo Response:", data.BPA);
      setApplicationData(data.BPA);
    } catch (error) {
      console.error("Search Error:", error);
    }
  };

  useEffect(() => {
    // Function to fetch scrutiny details when 'tableData' changes
    const updatePayment = async () => {
      try {
        if (applicationData.length > 0) {

            const updateResponse = await Digit.OBPSAdminService.updateBillAmount(applicationData[0].applicationNo, "BPA.NC_SAN_FEE", "Post");

            console.log("updateResponse: "+JSON.stringify(updateResponse));
        //   const edcrResponse = await OBPSService.scrutinyDetails(tableData[0].tenantId, { edcrNumber: tableData[0].edcrNumber });
        //   const [edcr] = edcrResponse?.edcrDetail;
        //   const mdmsRes = await MdmsService.getMultipleTypes(tableData[0].tenantId, "BPA", ["RiskTypeComputation", "CheckList"]);
        //   const riskType = Digit.Utils.obps.calculateRiskType(mdmsRes?.BPA?.RiskTypeComputation, edcr?.planDetail?.plot?.area, edcr?.planDetail?.blocks);

        //   setTableData((prevTableData) =>
        //   prevTableData.map((item) => ({
        //     ...item,
        //     riskType: riskType,
        //   }))
        // );
  
        }
      } catch (error) {
        console.error("Error fetching data:", error);
      }
    };
  
    // Call the function to fetch data whenever 'tableData' changes
    updatePayment();
  }, [applicationData]);

  const tableContainerStyle = {
    marginTop: "20px", 
  };
  


 
  return (

    
    <form onSubmit={handleSubmit}>
       <BackButton>{t("CS_COMMON_BACK")}</BackButton>
       <div className="cardHeaderWithOptions" style={{ marginRight: "auto", maxWidth: "960px" , marginTop: 20}}>
      <Header styles={{ fontSize: "32px" }}>{t("Application Search ")}</Header>
  
    </div>
 
      <SearchField>
        <label>Enter Application Number </label>
        <TextInput
          name="applicationNo"
          value={applicationNo}
          onChange={(event) => setApplicationNo(event.target.value)}
        />
      </SearchField>
      <SearchField className="submit">
        <SubmitBar label="Search" submit onClick={handleSubmit} />
      
      </SearchField>
     
      {/* {applicationData.length > 0 && (
        
      <Fragment >
         
  
    <div className="cardHeaderWithOptions" style={{ marginRight: "auto", maxWidth: "960px" , marginTop: 20}}>
      <Header styles={{ fontSize: "32px" }}>{t("Application Details")}</Header>
    </div>
 
  
    

</Fragment>
)} */}




    
  
    </form>
  );
};

export default SanctionFeePaymentUpdate;
