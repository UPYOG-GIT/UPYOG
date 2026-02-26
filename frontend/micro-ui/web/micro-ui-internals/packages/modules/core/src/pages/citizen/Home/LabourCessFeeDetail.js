import React, { useState } from "react";
import { BackButton, TextInput, SubmitBar, Header, Card, CardHeader, CardLabel, Row, StatusTable, Dropdown } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import LabourCessDetailReport from "./LabourCessDetailReport";
import LabourCessUlbDetailReport from "./LabourCessUlbDetailReport";
import { Loader } from "@upyog/digit-ui-react-components";


const LabourCessFeeDetail = () => {
  const { t } = useTranslation();
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [selectedUlb, setSelectedUlb] = useState(null);
  const [reportData, setReportData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [totalCount,setTotalCount] = useState(0);


  // Get current tenant info
  const tenantId = Digit.ULBService.getCitizenCurrentTenant();
  const tenantName = Digit.SessionStorage.get("CITIZEN.COMMON.HOME.CITY")?.name || "Chhattisgarh";

  // Fetch ULB list using the tenant hook
  const { data: ulbs, isLoading: isUlbLoading } = Digit.Hooks.useTenants();

  
  
   const ulbOptions = [
    { code: "Raipur", name: "Municipal Corporation Raipur", value:"cg.raipur", i18nKey: "Municipal Corporation Raipur" },
    { code: "Durg", name: "Municipal Corporation Durg", value:"cg.durg", i18nKey: "Municipal Corporation Durg" },
    { code: "Bhilai", name: "Municipal Corporation Bhilai", value:"cg.bhilai", i18nKey: "Municipal Corporation Bhilai" },
    { code: "Bilaspur", name: "Municipal Corporation Bilaspur", value:"cg.bilaspur", i18nKey: "Municipal Corporation Bilaspur" },
    { code: "Raigarh", name: "Municipal Corporation Raigarh", value:"cg.raigarh", i18nKey: "Municipal Corporation Raigarh" },
    { code: "Rajnandgaon", name: "Municipal Corporation Rajnandgaon",value:"cg.rajnandgaon", i18nKey: "Municipal Corporation Rajnandgaon" },
    { code: "Korba", name: "Municipal Corporation Korba",value:"cg.korba", i18nKey: "Municipal Corporation Korba" },
    { code: "Ambikapur", name: "Municipal Corporation Ambikapur",value:"cg.ambikapur", i18nKey: "Municipal Corporation Ambikapur" },
    { code: "Jagdalpur", name: "Municipal Corporation Jagdalpur",value:"cg.jagdalpur", i18nKey: "Municipal Corporation Jagdalpur" },
    { code: "Risali", name: "Municipal Corporation Risali",value:"cg.risali", i18nKey: "Municipal Corporation Risali" },
    { code: "Birgaon", name: "Municipal Corporation Birgaon",value:"cg.birgaon", i18nKey: "Municipal Corporation Birgaon" },
    { code: "Dhamtari", name: "Municipal Corporation Dhamtari",value:"cg.dhamtari", i18nKey: "Municipal Corporation Dhamtari" },
    { code: "Bhilai Charoda", name: "Municipal Corporation Bhilai Charoda",value:"cg.bhilaicharoda", i18nKey: "Municipal Corporation Bhilai Charoda" }
  
  ];
  // Handle ULB selection
  const handleUlbSelect = (selectedOption) => {
    setSelectedUlb(selectedOption);
    console.log("ulb");
  };

  
  const handleSubmit = async (event) => {
    event.preventDefault();

         setIsLoading(true);

          if (!selectedUlb) {
          alert("Please select ULB Name");
          return;
          }

          if (!fromDate) {
          alert("Please select Date Range");
          return;
          }

          if (!toDate) {
          alert("Please select Date Range");
          return;
          }
     
    try {

          const response = await Digit.OBPSAdminService.labourCessDetailsUlb(selectedUlb.value,fromDate,toDate)
          console.log("Labour Cess Fee Details response", response.labour_cess_details);
          setReportData(response.labour_cess_details);
          sessionStorage.setItem("data",JSON.stringify(response.labour_cess_details));
          sessionStorage.setItem("ulb",selectedUlb.name);
          console.log("data",sessionStorage.getItem("data"));
          location.href="/digit-ui/citizen/labour-cess-reportdetails";
      
    } catch (error) {
      console.error("Search Error:", error);
    } finally {
    setIsLoading(false);
  }
 
  }

    if (isLoading) {
    return <Loader />;
  }

  return (
    <React.Fragment>
      
      

  
    <div>
      <BackButton>{t("CS_COMMON_BACK")}</BackButton>
      <div className="cardHeaderWithOptions" style={{ marginRight: "auto", maxWidth: "960px", marginTop: 20 }}>
        <Header styles={{ fontSize: "32px" }}>{t("Labour Cess Fee Detail")}</Header>
      </div>
      <Card style={{paddingRight:"16px"}}>
        <div style={{ display: "flex", flexDirection: "row", justifyContent: "space-around",flexWrap:"wrap"}}>
          <div>
        <CardHeader>{t("Select ULB")}</CardHeader>
        <div style={{ marginBottom: "16px" }}>
          <CardLabel>{t("ULB Name")} *</CardLabel>
          <Dropdown style={{width:"400px"}}
            isMandatory={true}
            option={ulbOptions}
            selected={selectedUlb}
            optionKey="name"
            select={handleUlbSelect}
            placeholder={t("Select ULB")}
            disable={isUlbLoading}
              optionCardStyles={{
              maxHeight: "200px",
              overflowY: "auto"
            }}
          />
        
        </div>
         </div>
         <div>
        <CardHeader>{t("Select Date Range")} </CardHeader>
        <div style={{ display: "flex", gap: "20px", alignItems: "top-right" ,flexWrap:"wrap"}}>
          <div>
            <label>{t("From Date")} *</label>
            <TextInput style={{marginTop:"13px"}} 
              type="date"
              name="fromDate"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
            />
          </div>
          <div>
            <label>{t("To Date")} *</label>
            <TextInput style={{marginTop:"13px"}} 
              type="date"
              name="toDate"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
            />
          </div>
        </div>
        </div>
        </div>
        <div style={{ textAlign: "center" ,margin:"20px"}}>
        <SubmitBar label={t("Submit")} submit  onSubmit={handleSubmit} />
        </div>
      </Card>
      </div>
     
     

    </React.Fragment>
  );
};

export  default LabourCessFeeDetail;
