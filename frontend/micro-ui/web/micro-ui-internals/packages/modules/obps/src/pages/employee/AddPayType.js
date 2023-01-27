import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";
import Axios from "axios";
import { useParams, useHistory } from "react-router-dom";
import { Card,Header,CardLabel,TextInput, CardSectionHeader, CardSubHeader, CheckPoint, ConnectingCheckPoints, Loader, Row, StatusTable
} from "@egovernments/digit-ui-react-components";


const AddPayType = () => {
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
<td><TextInput
isMandatory={true}
 optionKey="i18nKey"
// t={t}
name="postFee"
onChange={setPostFees}
// uploadMessage={uploadMessage}
// value={name}

/></td>
 </tr>
</table></React.Fragment>
 ):""}
  </div>
</React.Fragment>

// console.log(index == 1?detail.title:"nhi")
 ))}
{postFee}

<React.Fragment></React.Fragment>
 </Card>
);
};

export default AddPayType;


