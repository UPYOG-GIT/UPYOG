import React, { useEffect, useState, useRef, useMemo } from "react";
import { useTranslation } from "react-i18next";
import {
  Card, Header, Modal, Dropdown, CardLabel, TextInput, SubmitBar, Toast, CloseSvg, EditPencilIcon, DeleteIcon, LabelFieldPair, Loader, Row, Table, BackButton, KeyNote
} from "@egovernments/digit-ui-react-components";

const EdcrRuleEntry = () => {

  const { t } = useTranslation();
  const [modalData, setModalData] = useState(false);
  const [SlabPyType, setSlabPyType] = useState({ code: 'Far', value: 1 });
  const [occupancy, setOccupancy] = useState("");
  const [featureName, setFeatureName] = useState({ code: "", value: "null" });
  // const [PyBuildCat,setPyBuildCat] = useState("");
  const [PySubCat, setPySubCat] = useState({ code: "", value: "null" });
  const [fromArea, setFromArea] = useState(0);
  const [toArea, setToArea] = useState(0);
  const [PyOperation, setPyOperation] = useState("");
  const [byLaw, setByLaw] = useState(0);
  const [permissibleValue, setPermissibleValue] = useState(0);
  const [PyIndRate, setPyIndRate] = useState(0);
  const [PyMultiplyValue, setPyMultiplyValue] = useState(0);
  const [PyMaxLimit, setPyMaxLimit] = useState(0);
  const [PropCatdropdown, setPropCatdropdown] = useState([]);
  const [BuildCatdropdown, setBuildCatdropdown] = useState([]);
  const [SubCatdropdown, setSubCatdropdown] = useState([]);
  const [SlabPyTypedropval, setSlabPyTypedropval] = useState([]);
  const [Slabtblval, setSlabtblval] = useState([]);
  const [showToast, setShowToast] = useState(null);

  const Operationdropdown = [{ code: "Multiply", value: "Multiply" }, { code: "Fix", value: "Fix" }, { code: "Fix and Multiply", value: "Fix and Multiply" }, { code: "Multiply and Check Limit", value: "Multiply and Check Limit" }];

  const FeatureNameDropdown = [{ code: "Far", value: "Far" }, { code: "Coverage", value: "Coverage" }, { code: "Front Setback", value: "Front Setback" }, { code: "Rear Setback", value: "Rear Setback" }];

  const OccupancyDropdown = [{ code: "Residential", value: "Residential" }, { code: "Mercantile / Commercial", value: "Mercantile / Commercial" }, { code: "Industrial", value: "Industrial" }, { code: "Government/Semi Goverment", value: "Government/Semi Goverment" }];

  const tenantId = Digit.ULBService.getCurrentTenantId();
  let validation = {};

  const selectedRows = [];
  const [rowid, setRowid] = useState([]);

  const setSlabPaytype = (value) => setSlabPyType(value);
  const setPayPropCat = (value) => setOccupancy(value);
  const setPayBuildCat = (value) => setFeatureName(value);
  const setPaySubCat = (value) => setPySubCat(value);
  const setPayOperation = (value) => setPyOperation(value);


  let { uuid } = Digit.UserService.getUser()?.info || {};
  const GetCell = (value) => <span className="cell-text">{t(value)}</span>;

  // useEffect( async ()=>{

  //     let paytypeRule = await Digit.OBPSAdminService.getPaytype(tenantId);
  //     let PyTydrop=[];
  //     paytypeRule.map(item=>{
  //       if(item.defunt==="N"){
  //         PyTydrop.push({
  //           code: item.charges_type_name,
  //           value: item.id
  //         })
  //       }
  //     });
  //     setSlabPyTypedropval(PyTydrop);
  // },[])

  // useEffect( async ()=>{
  //   let propType = await Digit.OBPSAdminService.getProptype(tenantId);
  //   let Proptydrop=[];
  //   propType.map(item=>{
  //     if(item.defunt==="N"){
  //       Proptydrop.push({
  //         code: item.description,
  //         value: item.srno
  //       })
  //     }
  //   });
  //   setPropCatdropdown(Proptydrop);
  // },[])

  // useEffect( async ()=>{
  //   let BCatetype = await Digit.OBPSAdminService.getBCategory(tenantId);
  //   let BCatetydrop=[];
  //   BCatetype.map(item=>{
  //     if(item.defunt==="N"){
  //       BCatetydrop.push({
  //         code: item.description,
  //         value: item.id
  //       })
  //     }
  //   });
  //   setBuildCatdropdown(BCatetydrop);
  // },[])

  // useEffect( async ()=>{
  //   if(PyBuildCat.value != "null"){
  //   let cateId = PyBuildCat.value;
  //   let BStype = await Digit.OBPSAdminService.getBSCategory(tenantId, cateId);
  //   let BSCatetydrop=[];
  //   BStype.map(item=>{
  //     if(item.defunt==="N"){
  //       BSCatetydrop.push({
  //         code: item.description,
  //         value: item.id
  //       })
  //     }
  //   });
  //   setSubCatdropdown(BSCatetydrop);
  // }
  // },[PyBuildCat])


  let typevalid=SlabPyType.value;
  useEffect( async ()=>{
    let getslab = await Digit.EDCRService.getEdcrRule(tenantId);
    setSlabtblval(getslab);
  },[])



  const addEdcrRule = async (e) => {
    e.preventDefault();

    const SlabMasterRequest = {
      tenantId: tenantId,
      payTypeId: SlabPyType.value,
      fromVal: fromArea,
      toVal: toArea,
      operation: PyOperation.value,
      pCategory: occupancy.value,
      bCategory: featureName.value,
      sCategory: PySubCat.value,
      rateRes: byLaw,
      rateComm: permissibleValue,
      rateInd: PyIndRate,
      createdBy: uuid,
      multpVal: PyMultiplyValue,
      maxLimit: PyMaxLimit,
    };

    // const obj = { feature: PyBuildCat.value };
    // const prop1 = "occupancy";
    // const prop2 = "to_area";
    // const prop3 = "from_area";
    // const prop4 = "by_law";
    // const prop5 = "permissible_value";
    // obj[prop1] = PyPropCat.value;
    // obj[prop2] = PyToValue;
    // obj[prop3] = PyFromValue;
    // obj[prop4] = PyResRate;
    // obj[prop5] = PyCommRate;

    // console.log(obj);
    // {"name": "Ben", "prop name": "value 1", "prop-name": "value 2", "1prop-name": "value 3"}


    const params = {
      feature: featureName.value,
      occupancy: occupancy.value,
      to_area: toArea,
      from_area: fromArea,
      tenant_id:tenantId.split(".").length>1?tenantId.split('.')[1]:tenantId,
      by_law: byLaw,
      permissible_value: permissibleValue,

    };


    closeModal();
    setFromArea(0);
    setToArea(0);
    setPyOperation("");
    setOccupancy("");
    setFeatureName({ code: "", value: "null" });
    setPySubCat("");
    setByLaw(0);
    setPermissibleValue(0);
    setPyIndRate(0);
    setPyMultiplyValue(0);
    setPyMaxLimit(0);

    const edcrRuleResponse = await Digit.EDCRService.createEdcrRule(params);
    // const SlabMasterResp = await Digit.OBPSAdminService.createEdcrRule(params);
    console.log("edcrRuleResponse: "+JSON.stringify(edcrRuleResponse));
    console.log("edcrRuleResponse11 "+edcrRuleResponse);
    if (edcrRuleResponse > 0) {
      setShowToast({ key: false, label: "Successfully Added ", bgcolor: "#4BB543" });
      location.reload();
    }
    else {
      setShowToast({ key: true, label: "Fail To Add", bgcolor: "red" });
    }

  }



  const closeModal = () => {
    setModalData(false);
  };


  const GetCell1 = (value) => <input type="checkbox" id="checkbox2" onChange={(e) => getRowId(e)} name="checkbox2" value={value} />;
  const columns = React.useMemo(() => {
    return [
      {
        Header: t("Select"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return (
            GetCell1(`${row.original?.id}`)
          );
        },
      },
      {
        Header: t("Srno"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetCell(`${row.original?.id}`);
        },
      },
      {
        Header: t("Feature"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetCell(`${row.original?.feature}`);
        },
      },
      {
        Header: t("From Area"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetCell(`${row.original?.from_area}`);
        },
      },
      {
        Header: t("To Area"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetCell(`${row.original?.to_area}`);
        },
      },
      {
        Header: t("Occupancy"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetCell(`${row.original?.occupancy}`);
        },
      },
      {
        Header: t("Sub Occupancy"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetCell(`${row.original?.sub_occupancy}`);
        },
      },
      {
        Header: t("Permissible Value"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetCell(`${row.original?.permissible_value}`);
        },
      },
      {
        Header: t("Rule"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetCell(`${row.original?.by_law}`);
        },
      },
    ];
  }, []);

  //this is for storing final data in array for delete
  function getRowId(e) {
    const { value, checked } = e.target;
    if (checked) {
      selectedRows.push(value);
      // console.log("selectedRows value "+selectedRows);
    }
    else {
      const index = selectedRows.indexOf(value);
      if (index > -1) {
        selectedRows.splice(index, 1);
      }
    }
    if (selectedRows.length > 0) {
      setRowid(selectedRows);
    }
  }


  //this is for delete rows selected in checkBox
  const deleteItem = async () => {
    const SlabMasterRequest = {
      ids: rowid,
    }
    // console.log("rowid value "+rowid);
    if (rowid.length > 0) {
      const DeleterowResp = await Digit.OBPSAdminService.deleteSlabdata(SlabMasterRequest);

      if (DeleterowResp > 0) {
        setShowToast({ key: false, label: "Successfully Deleted ", bgcolor: "#4BB543" });
        location.reload();
      }
      else {
        setShowToast({ key: true, label: "Fail To Delete", bgcolor: "red" });
      }

    }
  }




  return (
    <Card style={{ position: "relative" }} className={"employeeCard-override"}>
      <Header styles={{ marginLeft: "0px", paddingTop: "10px", fontSize: "32px" }}>{t("EDCR Rule Detail")}</Header>

      <LabelFieldPair>
        <CardLabel style={{ color: "#000" }}>{`${t("Pay Type")}`}</CardLabel>
        <Dropdown
          style={{ width: "100%" }}
          className="form-field"
          selected={SlabPyType}
          option={SlabPyTypedropval}
          select={setSlabPaytype}
          value={SlabPyType}
          optionKey="code"
          name="SlabPyType"
        />
      </LabelFieldPair>

      <Table
        t={t}
        data={Slabtblval}
        columns={columns}
        className="customTable table-border-style"
        // manualPagination={false}
        // isPaginationRequired={false}
        getCellProps={(cellInfo) => {
          return {
            style: {
              padding: "20px 18px",
              fontSize: "16px",
              borderTop: "1px solid grey",
              textAlign: "left",
              verticalAlign: "middle",
            },
          };
        }}
      />

      <button
        onClick={() => setModalData(true)}
        style={{
          margin: "24px",
          backgroundColor: "#F47738",
          width: "20%",
          height: "40px",
          color: "white",
          borderBottom: "1px solid black",
        }}
      >
        {t("Add")}
      </button>
      <button
        onClick={deleteItem}
        style={{
          margin: "24px",
          backgroundColor: "#F47738",
          width: "20%",
          height: "40px",
          color: "white",
          borderBottom: "1px solid black",
        }}
      >
        {t("Delete")}
      </button>

      <div style={{ display: "flex", justifyContent: "center" }}>
        <button
          // onClick={updateProfile}
          style={{
            margin: "24px",
            backgroundColor: "#F47738",
            width: "20%",
            height: "40px",
            color: "white",
            borderBottom: "1px solid black",
          }}
        >
          {t("Cancel")}
        </button>
      </div>

      {modalData ? (

        <Modal
          hideSubmit={true}
          isDisabled={false}
          popupStyles={{ width: "800px", height: "auto", margin: "auto", padding: "auto" }}
          formId="modal-action"
        >
          <div>
            <div style={{ display: "flex", justifyContent: "space-between" }}>
              <Header styles={{ marginLeft: "0px", paddingTop: "1px", fontSize: "25px" }}>{t("EDCR Rule Entry")}</Header>
              <span onClick={closeModal}>
                <CloseSvg />
              </span>
            </div>
            <form >
              <div>
                <KeyNote noteStyle={{ color: "red", fontSize: "15px", padding: "auto" }} keyValue={t("Note")} note={"Information will not be updated if click other than ok button of this window. Press Tab button to field navigation.* - Required Fields"} />

                <LabelFieldPair>
                  <CardLabel style={{ color: "#000" }}>{`${t("Feature Name*")}`}</CardLabel>
                  <Dropdown
                    style={{ width: "100%", height: "2rem" }}
                    className="form-field"
                    selected={featureName}
                    option={FeatureNameDropdown}
                    select={setPayBuildCat}
                    value={featureName}
                    optionKey="code"
                    placeholder="Select Feature Name"
                    name="PyBuildCat"
                  />
                </LabelFieldPair>

                <LabelFieldPair>
                  <CardLabel style={{ color: "#000" }}>{`${t("Occupancy*")}`}</CardLabel>
                  <Dropdown
                    style={{ width: "100%", height: "2rem" }}
                    className="form-field"
                    selected={occupancy}
                    option={OccupancyDropdown}
                    select={setPayPropCat}
                    value={occupancy}
                    optionKey="code"
                    placeholder="Select Occupancy Type"
                    name="PyPropCat"
                    {...(validation = {
                      isRequired: true,
                      // pattern: "^[a-zA-Z-.`' ]*$",
                      // type: "tel",
                      title: t("CORE_COMMON_PROFILE_NAME_ERROR_MESSAGE"),
                    })}
                  />
                </LabelFieldPair>

                <LabelFieldPair>
                  <CardLabel style={{ color: "#000" }}>{`${t("From Plot Area*")}`}</CardLabel>
                  <TextInput
                    isMandatory={true}
                    name="fromvalue"
                    onChange={(e) => setFromArea(e.target.value)}
                    value={fromArea}
                    placeholder="Enter From Plot Value"
                    type="number"
                  />
                </LabelFieldPair>
                <LabelFieldPair>
                  <CardLabel style={{ color: "#000" }}>{`${t("To Plot Value*")}`}</CardLabel>
                  <TextInput
                    isMandatory={true}
                    name="tovalue"
                    placeholder="Enter To Plot Value"
                    onChange={(e) => setToArea(e.target.value)}
                    value={toArea}
                    type="number"
                  />
                </LabelFieldPair>

                <LabelFieldPair>
                  <CardLabel style={{ color: "#000" }}>{`${t("Rule Number*")}`}</CardLabel>
                  <TextInput
                    isMandatory={true}
                    name="resrate"
                    placeholder="Enter Rule Number"
                    onChange={(e) => setByLaw(e.target.value)}
                    value={byLaw}
                    type="number"
                  />
                </LabelFieldPair>
                <LabelFieldPair>
                  <CardLabel style={{ color: "#000" }}>{`${t("Value*")}`}</CardLabel>
                  <TextInput
                    isMandatory={true}
                    name="commrate"
                    placeholder="Enter Value"
                    onChange={(e) => setPermissibleValue(e.target.value)}
                    value={permissibleValue}
                    type="number"
                  />
                </LabelFieldPair>
                <div style={{ display: "flex", justifyContent: "center" }}>
                  <button
                    onClick={addEdcrRule}
                    style={{
                      margin: "24px",
                      backgroundColor: "#F47738",
                      width: "20%",
                      height: "40px",
                      color: "white",
                      borderBottom: "1px solid black",
                    }}
                  >
                    {t("OK")}
                  </button>
                  <button
                    onClick={closeModal}
                    style={{
                      margin: "24px",
                      backgroundColor: "#F47738",
                      width: "20%",
                      height: "40px",
                      color: "white",
                      borderBottom: "1px solid black",
                    }}
                  >
                    {t("Cancel")}
                  </button>
                </div>
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
}
export default EdcrRuleEntry;