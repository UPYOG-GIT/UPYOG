
import { StatusTable, SearchField, TextInput, SubmitBar, Header, Card, CardHeader, Row, PDFSvg, CardSectionHeader,MultiLink, Loader } from "@egovernments/digit-ui-react-components";


import React, { Fragment, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { fromUnixTime, format } from 'date-fns';
// import { pdfDocumentName, pdfDownloadLink, stringReplaceAll } from "../../../utils";
// import ApplicationTimeline from "../../../components/ApplicationTimeline";



const ApplicationSearchFormField = ({ formState, register, reset, previousPage }) => {
  const { t } = useTranslation();
 
  const [applicationNo, setApplicationNo] = useState("");
  const [tableData, setTableData] = useState([]);

  
  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      const data = await Digit.OBPSAdminService.searchByApplicationNo('cg', applicationNo);
      console.log("searchByApplicationNo Response:", data.BPA); // Log the API response directly
      setTableData(data.BPA);
    } catch (error) {
      console.error("Search Error:", error);
    }
  };
  const tableContainerStyle = {
    marginTop: "20px", 
  };

  return (

    
    <form onSubmit={handleSubmit}>
      <SearchField>
        <label>Enter Application Number To Search</label>
        <TextInput
          name="applicationNo"
          value={applicationNo}
          onChange={(event) => setApplicationNo(event.target.value)}
        />
      </SearchField>
      <SearchField className="submit">
        <SubmitBar label="Search" submit onClick={handleSubmit} />
      
      </SearchField>
      
      {tableData.length > 0 && (
      <Fragment >
  
    <div className="cardHeaderWithOptions" style={{ marginRight: "auto", maxWidth: "960px" , marginTop: 20}}>
      <Header styles={{ fontSize: "32px" }}>{t("BPA_TASK_DETAILS_HEADER")}</Header>
    </div>
 
  
    <Card>
      <StatusTable>
        <Row
          className="border-none"
          label={t("BPA_APPLICATION_NUMBER_LABEL")}
          text={tableData[0]?.applicationNo || t("NA")}
        />
      </StatusTable>
    </Card>
    <Card>
    <CardHeader>{t(`BPA_BASIC_DETAILS_TITLE`)}</CardHeader>
        <StatusTable>
          <Row className="border-none" label={t(`BPA_BASIC_DETAILS_APP_DATE_LABEL`)} text={tableData[0]?.auditDetails.createdTime ? format(new Date(tableData[0]?.auditDetails.createdTime), 'dd/MM/yyyy') : tableData[0]?.auditDetails.createdTime} />
          <Row className="border-none" label={t(`BPA_BASIC_DETAILS_APPLICATION_TYPE_LABEL`)} text={t(`WF_BPA_${tableData[0]?.additionalDetails.applicationType}`)}/>
          <Row className="border-none" label={t(`BPA_BASIC_DETAILS_SERVICE_TYPE_LABEL`)} text={t(tableData[0]?.additionalDetails.serviceType)} />
          <Row className="border-none" label={t(`BPA_BASIC_DETAILS_OCCUPANCY_LABEL`)} text={tableData[0]?.landInfo?.address?.occupancy}/>
          <Row className="border-none" label={t(`BPA_BASIC_DETAILS_RISK_TYPE_LABEL`)} text={t(`WF_BPA_${tableData[0]?.riskType}`)} />
          <Row className="border-none" label={t(`BPA_BASIC_DETAILS_APPLICATION_NAME_LABEL`)} text={tableData[0]?.landInfo?.owners[0]?.name} />
          <Row className="border-none" label={t(`Application Status`)} text={t(`WF_BPA_${tableData[0]?.status}`)} />
        </StatusTable>
        </Card>

        <Card>
    <CardHeader>{t("BPA_PLOT_DETAILS_TITLE")}</CardHeader>
    
    <StatusTable>
          <Row className="border-none" label={t(`BPA_BOUNDARY_PLOT_AREA_LABEL`)} text={tableData[0]?.landInfo?.address?.plotArea ? `${tableData[0]?.landInfo?.address?.plotArea} ${t(`BPA_SQ_FT_LABEL`)}` : t("CS_NA")} />
          <Row className="border-none" label={t(`BPA_PLOT_NUMBER_LABEL`)} text={tableData[0]?.landInfo?.address?.plotNo || t("CS_NA")} />
          <Row className="border-none" label={t(`Khasra Number`)} text={tableData[0]?.landInfo?.address?.khataNo || t("CS_NA")}/>
          <Row className="border-none" label={t(`BPA_PATWARI_HALKA_NUMBER_LABEL`)} text={tableData[0]?.landInfo?.address?.patwariHN  || t("CS_NA")} />
          <Row className="border-none" label={t(`BPA_HOLDING_NUMBER_LABEL`)} text={t("CS_NA")} />
          <Row className="border-none" label={t(`BPA_BOUNDARY_LAND_REG_DETAIL_LABEL`)} text={t("CS_NA")} />
          
    </StatusTable>
    </Card>   
    <Card>
      <CardHeader>{t("BPA_NEW_TRADE_DETAILS_HEADER_DETAILS")}</CardHeader>
        
      <StatusTable>
          <Row className="border-none"  label={t(`BPA_DETAILS_PIN_LABEL`)} text={tableData[0]?.landInfo?.address?.pincode || t("CS_NA")} />
          <Row className="border-none" label={t(`BPA_CITY_LABEL`)} text={tableData[0]?.landInfo?.address?.city || t("CS_NA")} />
          <Row className="border-none" label={t(`BPA_LOC_MOHALLA_LABEL`)} text={tableData[0]?.landInfo?.address?.locality?.name || t("CS_NA")} />
          <Row className="border-none" label={t(`BPA_DETAILS_SRT_NAME_LABEL`)} text={tableData[0]?.landInfo?.address?.street || t("CS_NA")} />
          <Row className="border-none" label={t(`ES_NEW_APPLICATION_LOCATION_LANDMARK`)} text={tableData[0]?.landInfo?.address?.landmark || t("CS_NA")} />
          <Row className="border-none" label={t(`Proposed Site Address`)} text={tableData[0]?.landInfo?.address?.address || t("CS_NA")} />
      </StatusTable>
      </Card>

      <Card >
          <CardHeader>{t("BPA_APPLICANT_DETAILS_HEADER")}</CardHeader>
        
              <StatusTable>
                <Row className="border-none" label={t(`CORE_COMMON_NAME`)} text={tableData[0]?.landInfo?.owners[0]?.name} />
                <Row className="border-none" label={t(`BPA_APPLICANT_GENDER_LABEL`)} text={t(tableData[0]?.landInfo?.owners[0]?.gender)} />
                <Row className="border-none" label={t(`CORE_COMMON_MOBILE_NUMBER`)} text={tableData[0]?.landInfo?.owners[0]?.mobileNumber} />
                <Row className="border-none" label={t(`BPA_IS_PRIMARY_OWNER_LABEL`)} text={`${tableData[0]?.landInfo?.owners[0]?.isPrimaryOwner}`} />
              </StatusTable>
            
        </Card>

</Fragment>
)}




    
  
    </form>
  );
};

export default ApplicationSearchFormField;
