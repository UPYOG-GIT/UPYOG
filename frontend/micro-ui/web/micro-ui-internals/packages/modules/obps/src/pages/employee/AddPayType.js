import React, { useEffect, useState,useRef} from "react";
import { useTranslation } from "react-i18next";
import { useParams, useHistory } from "react-router-dom";
import {
  Card, Header,Modal, Dropdown, CardLabel, TextInput, SubmitBar, Toast,CloseSvg, EditPencilIcon,DeleteIcon, LabelFieldPair, Loader, Row, StatusTable, BackButton
} from "@egovernments/digit-ui-react-components";


const getDatafromLS=(id)=>{
  const data = localStorage.getItem('modalList'+id);
  if(data){
    return JSON.parse(data);
  }
  else{
    return []
  }
}



const AddPayType = () => {
  
  const [showToast, setShowToast] = useState(null);
  const [modalData, setModalData] = useState(false);
  
  const [modalId,setModalId] = useState(1);
  const [modalPyType,setModalPyType] = useState("select");
  const [modalDes,setModalDes] = useState("");
  const [modalUnit,setModalUnit] = useState("per Sq. Meter");
  const [modalRate,setModalRate] = useState(0);
  const [modalValue,setModalValue] = useState(0);
  const [dropDownData,setDropDownData] = useState([]);
  const { id } = useParams();
  const [modalList,setModalList] = useState(getDatafromLS(id));
 
  const setPayRule = (value) => setModalPyType(value);

  const [postFee, setPostFee] = useState();
  let amount=0;
  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();

  const dropDownData1 = [{code:"c",value:"c"},{code:"a",value:"a"},{code:"b",value:"b"}];

 

  const handleAddAmountSubmit =(e)=>{
      e.preventDefault();
      setModalId(modalId+1);
      // console.log(modalId);
    let list={
      modalId,
      modalDes,
      modalUnit,
      modalRate,
      modalValue,
    }
    setModalList([...modalList,list]);
    
    setModalDes("");
    setModalRate(0);
    setModalValue(0);
  }
  


  useEffect(()=>{
    localStorage.setItem('modalList'+id,JSON.stringify(modalList));
  },[modalList]);

  

  let totalAmount = modalList.length>0?(modalList.map(item=>amount += parseInt(item.modalValue))).slice(-1):amount;
  //  console.log(totalAmount.slice(-1));
  

  if(modalList.length>0){
        modalList.map(item=>(
        amount +=item.modalValue));
  }


  const { data = {}, isLoading } = Digit.Hooks.obps.useBPADetailsPage(tenantId, { applicationNo: id });
  useEffect( async ()=>{
    let paytypeRule = await Digit.OBPSService.getPaytype(tenantId);
    setDropDownData(dropDownData);
    console.log("drop--gh--"+JSON.stringify(paytypeRule)); 
  },[])
  // 



  // console.log("details----"+JSON.stringify(data?.edcrDetails?.planDetail?.planInformation?.plotArea)); 
  //  console.log("edcr-----"+JSON.stringify(data.edcrDetails?.planDetail?.virtualBuilding?.totalBuitUpArea));

  if (isLoading) {
    return <Loader />;
  }

  const plotarea = data?.edcrDetails?.planDetail?.planInformation?.plotArea;
  const totalBuitUpArea = data?.edcrDetails?.planDetail?.virtualBuilding?.totalBuitUpArea;

// console.log("plotarea"+plotarea+"totalBuitUpArea"+totalBuitUpArea);

  const closeModal = () => {
    setModalData(false);
  };



  function setPostFees(e) {
    setPostFee(e.target.value);
  }

  
  let demandCreateres = {};
  let fetchBillRess = {};

  const onSubmits = async (e) => {
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


   
    const demandCreateres = await Digit.PaymentService.demandCreate({ Demands: [{ ...data }] });
    // console.log("demandCreateres "+demandCreateres+"-----"+demandCreateres?.ResponseInfo);

    if (demandCreateres?.ResponseInfo?.status === "201 CREATED") {
      fetchBillRess = await Digit.PaymentService.fetchBill(tenantId, { consumerCode: id, businessService: "BPA.NC_SAN_FEE" });

      if (fetchBillRess?.Bill?.[0]?.billDetails?.length > 0) {
        setShowToast({ key: false, label: "Successfully Added ", bgcolor: "#5CF409" });

      } else {
        setShowToast({ key: true, label: "Bill FAILL", bgcolor: "red" });
      }

    }
    else {
      setShowToast({ key: true, label: "Demand Fail", bgcolor: "red" });
    }

  };

  


  return (

    <Card style={{ position: "relative" }} className={"employeeCard-override"}>

      <Header styles={{ marginLeft: "0px", paddingTop: "10px", fontSize: "32px" }}>{t("CS_TITLE_APPLICATION_DETAILS")}</Header>
      {data?.applicationDetails?.map((detail, index) => (

        <React.Fragment key={index} >
          <div style={{ marginLeft: "20rem", }}>
            {
              detail?.title == "BPA_BASIC_DETAILS_TITLE" ? (
                <React.Fragment>
                
                  <table style={{ marginLeft: "50px", justifyContent: "space-between", fontSize: "16px", lineHeight: "19px", color: "rgb(11, 12, 12)", }}>
                  <tr><td style={{ padding: "15px" }}>{t("Application No")}</td><td style={{ paddingLeft: "5rem" }}>{t(id)}</td></tr>
                    {detail?.values?.map((value, index) => (
                      // console.log("value ----"+JSON.stringify(value))
                      <tr><td style={{ padding: "15px" }}>{t(value.title)}</td><td style={{ paddingLeft: "5rem" }}>{t(value.value)}</td></tr>

                    ))}
                    <tr><td style={{ padding: "15px" }}>{t("Plot Area")}</td><td style={{ paddingLeft: "5rem" }}>{t(plotarea)}</td></tr>
               <tr><td style={{ padding: "15px" }}>{t("Total BuitUp Area")}</td><td style={{ paddingLeft: "5rem" }}>{t(totalBuitUpArea)}</td></tr>
               
                  </table>

                </React.Fragment>
              ) : ""}
               
          </div>
        </React.Fragment>
      ))}


      <React.Fragment></React.Fragment>
      <div>
      <table style={{ border: "1px solid #494442", textAlign: "left", borderCollapse: "collapse", width: "100%" }}>
       <thead>
        <tr>
        <th style={{border: "1px solid #494442", textAlign: "left", padding: "15px", backgroundColor: "#04AA6D" }}>Sno.</th>
          <th style={{border: "1px solid #494442", textAlign: "left", padding: "15px", backgroundColor: "#04AA6D" }}>Description</th>
          <th style={{border: "1px solid #494442", textAlign: "left", padding: "15px", backgroundColor: "#04AA6D" }}>Proposed Area</th>
          <th style={{border: "1px solid #494442", textAlign: "left", padding: "15px", backgroundColor: "#04AA6D" }}>Measurement Unit</th>
          <th style={{border: "1px solid #494442", textAlign: "left", padding: "15px", backgroundColor: "#04AA6D" }}>Rate</th>
          <th style={{border: "1px solid #494442", textAlign: "left", padding: "15px", backgroundColor: "#04AA6D" }}>Value</th>
          <th style={{border: "1px solid #494442", textAlign: "left", padding: "15px", backgroundColor: "#04AA6D" }}>Action</th>
        </tr>
        </thead>
        

        {modalList.length>0 &&<React.Fragment> <tbody>
                      {modalList.map(item=>(
                        <tr>
                          <td style={{border: "1px solid #494442",textAlign: "left",padding: "15px"}} >{item.modalId}</td>
                          <td style={{border: "1px solid #494442",textAlign: "left",padding: "15px"}} >{item.modalDes}</td>
                          <td style={{border: "1px solid #494442",textAlign: "left",padding: "15px"}} >{plotarea}</td>
                          <td style={{border: "1px solid #494442",textAlign: "left",padding: "15px"}} >{item.modalUnit}</td>
                          <td style={{border: "1px solid #494442",textAlign: "left",padding: "15px"}} >{item.modalRate}</td>
                          <td style={{border: "1px solid #494442",textAlign: "left",padding: "15px"}} >{item.modalValue}</td>
                          <td style={{border: "1px solid #494442",textAlign: "left",padding: "15px"}} >
                                <EditPencilIcon className="icon" />
                                <DeleteIcon className="icon" style={{ bottom: "5px" }} />
                        </td>
                          </tr>
                          
                      ))}
                      </tbody> 
                           <tfoot>
                           <tr>
                               <td colSpan="4"></td>           
                               <td>Net Amount</td>
                               <td>{totalAmount}</td>
                           </tr>
                           </tfoot>
                    
                    </React.Fragment>
        }
        {modalList.length<1 && <div>No rows are added yet</div>}
       
      </table>
      <button type="button" className="button-sub-text" onClick={()=>setModalData(true)} style={{marginTop:"10px",backgroundColor: "#008CBA"}}>Add New Row</button>
      </div>                

      {modalData ? (
        <Modal
          hideSubmit={true}
          isDisabled={false}
          popupStyles={{ width: "700px", height: "700px", margin: "auto" }}
          formId="modal-action"
        >
          <div>
            {/* <button onClick={closeModal}>close</button> */}
            {/* <button onClick={closeModal}><CloseSvg/></button> */}
            <div style={{display: "flex",justifyContent: "space-between"}}>
              <h2></h2>
              <span onClick={closeModal}>
                <CloseSvg />
              </span>
            </div>
         <form onSubmit={handleAddAmountSubmit}>
            <div>
             <Header styles={{ marginLeft: "0px", paddingTop: "10px", fontSize: "32px" }}>{t("FORM")}</Header>
             
             <LabelFieldPair>
                <CardLabel style={{ color: "#B1B4B6" }}>{`${t("Payment Type")}`}</CardLabel>
                <Dropdown
                  style={{ width: "100%" }}
                  className="form-field"
                  selected={modalPyType}
                  // disable={gender?.length === 1 || editScreen}
                  option={dropDownData}
                  select={setPayRule}
                  value={modalPyType}
                  optionKey="code"
                  // t={t}
                  name="modalPyType"
                />
              </LabelFieldPair>

              <LabelFieldPair>
              <CardLabel style={{ color: "#B1B4B6" }}>{`${t("Payment Description")}`}</CardLabel>
               <TextInput
                        isMandatory={true}
                        name="description"
                        onChange={(e)=>setModalDes(e.target.value)}
                        value={modalDes}
                        type="text"
                      />
              </LabelFieldPair>
              <LabelFieldPair>        
              <CardLabel style={{ color: "#B1B4B6" }}>{`${t("Measurement Unit")}`}</CardLabel> 
               <TextInput
                        isMandatory={true}
                        name="unit"
                        onChange={(e)=>setModalUnit(e.target.value)}
                        value={modalUnit}
                        type="text"
                        disabled
                      /> 
               </LabelFieldPair>
               <LabelFieldPair>       
               <CardLabel style={{ color: "#B1B4B6" }}>{`${t("Rate")}`}</CardLabel>
               <TextInput
                        isMandatory={true}
                        name="rate"
                        onChange={(e)=>setModalRate(e.target.value)}
                        value={modalRate}
                        type="number"
                      />
               </LabelFieldPair>  
              <LabelFieldPair>     
              <CardLabel style={{ color: "#B1B4B6" }}>{`${t("Value")}`}</CardLabel>
                <TextInput
                        isMandatory={true}
                        name="value"
                        onChange={(e)=>setModalValue(e.target.value)}
                        value={modalValue}
                        type="number"
                      />
              </LabelFieldPair>
              <button
                // onClick={updateProfile}
                style={{
                  marginTop: "24px",
                  backgroundColor: "#F47738",
                  width: "100%",
                  height: "40px",
                  color: "white",
                  borderBottom: "1px solid black",
                }}
              >
                {t("OK")}
              </button>
            </div>
         </form>
          
          </div>
        </Modal>
      ) : null}
      
      
      {showToast && (
        <Toast
          error={showToast.key}
          label={t(showToast.label)}
          onClose={() => {
            setShowToast(null);
          }}
          style={{ backgroundColor: showToast.bgcolor }}
        />
      )}
    </Card>


  );
};

export default AddPayType;