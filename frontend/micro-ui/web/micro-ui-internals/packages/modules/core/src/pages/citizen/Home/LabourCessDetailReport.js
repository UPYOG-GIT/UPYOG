import React, { useState } from "react";
import { BackButton, TextInput, SubmitBar, Header, Card, CardHeader, CardLabel, Row, StatusTable, Dropdown } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import { useEffect } from "react";


const LabourCessDetailReport= () =>{

  const { t } = useTranslation();
  const [currentPage, setCurrentPage] = useState(1);
  const row= JSON.parse(sessionStorage.getItem("data"));
  const ulb= sessionStorage.getItem("ulb");
  const rowsPerPage = 10;

  const indexOfLastRow = currentPage * rowsPerPage;
  const indexOfFirstRow = indexOfLastRow - rowsPerPage;

  const currentRows = row.slice(indexOfFirstRow, indexOfLastRow);

  const totalPages = Math.ceil(row.length / rowsPerPage);
  const count = row.length;


    return (
     <React.Fragment>

      <BackButton>{t("CS_COMMON_BACK")}</BackButton>

      <Card style={{paddingRight:"16px"}}>
          <CardHeader>{ulb}</CardHeader>
      </Card> 

    <Card style={{ paddingRight:"16px"}}>

    {(row[0]["ULB_Name"] !== "cg.bhilaicharoda" &&
      row[0]["ULB_Name"] !== "cg.birgaon" &&
      row[0]["ULB_Name"] !== "cg.dhamtari") ? (

      
     <div style={{ overflowX: "auto" , marginTop:"20px",width:"100%" }}>
    <table  style={{ width: "100%", marginTop: "20px" }}>
      
      <thead>
        <tr>
          <th style={{border:"1px solid black",padding:"10px"}}>ULB Name</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Proposal No</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Proposal Type</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Client Name</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Zone_no</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Ward_No</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Plot Area</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Builtup Area</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Approval Date</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Payment Date</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Post Amount Paid</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Labour Cess Charges</th>
        </tr>
      </thead>

      <tbody>
        {currentRows.map((row, index) => (
          <tr key={index}>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["ULB_Name"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Proposal_No"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Proposal_Type"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Client_Name"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Zone_No"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Ward_No"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Plot_Area"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Builtup_Area"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Approval_Date"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Payment_Date"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Post_Amount_Paid"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Labour_Cess_Charges"]}</td>
          </tr>
        ))}
      </tbody>

    </table>
    </div>

    
  ):(
   


      <div>
      <table style={{ width: "100%", marginTop: "20px" }}>
      
      <thead>
        <tr>
          <th style={{border:"1px solid black",padding:"10px"}}>Occupancy Type</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Payment Date</th>
          <th style={{border:"1px solid black",padding:"10px"}}>ULB Name</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Approval Date</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Labour Cess Amount</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Consumer Code</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Builtup Area</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Labour Cess Type</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Plot Area</th>
          <th style={{border:"1px solid black",padding:"10px"}}>Ward No</th>
          
        </tr>
      </thead>

      <tbody>
        {currentRows.map((row, index) => (
          <tr key={index}>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Occupancy_Type"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Payment_Date"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["ULB_Name"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Approval_Date"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Labour_Cess_Amount"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Consumer_Code"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Builtup_Area"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Labour_Cess_Type"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Plot_Area"]}</td>
            <td style={{border:"1px solid black",padding:"10px"}}>{row["Ward_No"]}</td>
           
          </tr>
        ))}
      </tbody>

    </table>
    </div>

    
  )}
  

    <div style={{display:"flex",flexWrap:"wrap",alignItems:"center",justifyContent:"space-between"}}>
     <div style={{ marginTop: "10px" }}>
        <button style={{outline:"none",  color: currentPage === 1 ? "#999" : "blue"}}
          onClick={() => setCurrentPage(currentPage - 1)}
          disabled={currentPage === 1}
        >
          Previous
        </button>

        <span style={{ margin: "0 10px" }}>
          Page {currentPage} of {totalPages}
        </span>

        <button style={{outline:"none",color: currentPage === totalPages ? "#999" : "blue"}}
          onClick={() => setCurrentPage(currentPage + 1)}
          disabled={currentPage === totalPages}
        >
          Next
        </button>
      </div>

      <span style={{ margin: "0 10px" }}>
          Total Records : {count}
        </span>
    </div>
    </Card>
    </React.Fragment>
    ) ;

};

export default LabourCessDetailReport;