import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import ApplicationTable from "../inbox/ApplicationTable";
import { Card, Header, Loader, Modal, LabelFieldPair, Dropdown, CardLabel, TextInput,  KeyNote,} from "@egovernments/digit-ui-react-components";
import InboxLinks from "../inbox/ApplicationLinks";
import SearchApplication from "./search";
import DatePicker from "react-datepicker";
// import "react-datepicker/dist/react-datepicker.css";
import { Link } from "react-router-dom";

const ArchitectDetailsDesktopInbox = ({ tableConfig, filterComponent, ...props }) => {
  const { t } = useTranslation();
  const tenantIds = Digit.SessionStorage.get("HRMS_TENANTS");
  const GetCell = (value) => <span className="cell-text">{t(value)}</span>;
  const [modalData, setModalData] = useState(false);
  const [archId, setArchId] = useState();
  const handleDateChange = (date) => {
    setSelectedDate(date);
  };
  const GetSlaCell = (value) => {
    return value == "INACTIVE" ? (
      <span className="sla-cell-error">{t(value) || ""}</span>
    ) : (
      <span className="sla-cell-success">{t(value) || ""}</span>
    );
  }; 

  const handleClick = (row) => {
    // Access row data and perform actions based on it
    console.log("Button clicked in row with data:", row.original.id);
    return(
    <Card style={{ position: "absolute" }} className={"employeeCard-override"}>
      <Modal
          hideSubmit={true}
          isDisabled={false}
          popupStyles={{ width: "800px", height: "auto", margin: "auto", padding: "auto" }}
          formId="modal-action"
        >
          <div>
            <div style={{ display: "flex", justifyContent: "space-between" }}>
              <Header styles={{ marginLeft: "0px", paddingTop: "1px", fontSize: "25px" }}>{t("EDCR Rule Entry")}</Header>
              {/* <span onClick={closeModal}>
                <CloseSvg />
              </span> */}
            </div>
            <form>
              <div>
                <KeyNote
                  noteStyle={{ color: "red", fontSize: "15px", padding: "auto" }}
                  keyValue={t("Note")}
                  note={
                    "Information will not be updated if click other than ok button of this window. Press Tab button to field navigation.* - Required Fields"
                  }
                />

               

                <LabelFieldPair>
                  <CardLabel style={{ color: "#000" }}>{`${t("From Plot Area")}`}</CardLabel>
                  <TextInput
                    isMandatory={true}
                    name="fromvalue"
                    onChange={(e) => setArchId(e.target.value)}
                    value={row.id}
                    type="number"
                  />
                </LabelFieldPair>
                

                
                <div style={{ display: "flex", justifyContent: "center" }}>
                  <button
                    onClick={''}
                    style={{
                      margin: "24px",
                      backgroundColor: "#F47738",
                      width: "20%",
                      height: "40px",
                      color: "white",
                      borderBottom: "1px solid black",
                    }}
                  >
                    {t("Update")}
                  </button>
                  <button
                    onClick={''}
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
    </Card>
    );
  
    // For example, you can access specific properties of the row data
    // and perform operations like updating state, making API calls, etc.
  };

  const data = props?.data?.user;

  const [FilterComponent, setComp] = useState(() => Digit.ComponentRegistryService?.getComponent(filterComponent));

  const columns = React.useMemo(() => {
    return [
      // {
      //   Header: t("HR_EMP_ID_LABEL"),
      //   disableSortBy: true,
      //   Cell: ({ row }) => {
      //     return (
      //       <span className="link">
      //         <Link to={`/digit-ui/employee/hrms/details/${row.original.tenantId}/${row.original.code}`}>{row.original.code}</Link>
      //       </span>
      //     );
      //   },
      // },
      {
        Header: t("Architect Name"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetCell(`${row.original?.name}`);
        },
      },
      {
        Header: t("Mobile Number"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetCell(`${row.original?.mobileNumber}`);
        },
      },
      // {
      //   Header: t("HR_ROLE_NO_LABEL"),
      //   Cell: ({ row }) => {
      //     return (
      //       <div className="tooltip">
      //         {" "}
      //         {GetCell(`${row.original?.user?.roles.length}`)}
      //         <span className="tooltiptext" style={{whiteSpace: "nowrap"}}>
      //           {row.original?.user?.roles.map((ele, index) => (
      //             <span>
      //               {`${index + 1}. ` + t(`ACCESSCONTROL_ROLES_ROLES_${ele.code}`)} <br />{" "}
      //             </span>
      //           ))}
      //         </span>
      //       </div>
      //     );
      //   },
      //   disableSortBy: true,
      // },
      // {
      //   Header: t("HR_DESG_LABEL"),
      //   disableSortBy: true,
      //   Cell: ({ row }) => {
      //     return GetCell(
      //       `${
      //         t(
      //           "COMMON_MASTERS_DESIGNATION_" + row.original?.assignments?.sort((a, b) => new Date(a.fromDate) - new Date(b.fromDate))[0]?.designation
      //         ) || ""
      //       }`
      //     );
      //   },
      // },
      {
        Header: t("Validity Date"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetCell(`${row.original?.validityDate ? ""+ (row.original?.validityDate).split(' ')[0] : null}`);
        },
      },
      {
        Header: "Actions",
        disableSortBy: true,
        Cell: ({ row }) => (
          <button onClick={() => handleClick(row)}>Click Me</button>
        ),
      },
      {
        Header: t("HR_STATUS_LABEL"),
        disableSortBy: true,
        Cell: ({ row }) => {
          return GetSlaCell(`${row.original?.active ? "ACTIVE" : "INACTIVE"}`);
        },
      },
    ];
  }, []);

  let result;
  if (props.isLoading) {
    result = <Loader />;
  } else if (data?.length === 0) {
    result = (
      <Card style={{ marginTop: 20 }}>
        {/* TODO Change localization key */}
        {t("COMMON_TABLE_NO_RECORD_FOUND")
          .split("\\n")
          .map((text, index) => (
            <p key={index} style={{ textAlign: "center" }}>
              {text}
            </p>
          ))}
      </Card>
    );
  } else if (data?.length > 0) {
    result = (
      <ApplicationTable
        t={t}
        data={data}
        columns={columns}
        getCellProps={(cellInfo) => {
          return {
            style: {
              maxWidth: cellInfo.column.Header == t("HR_EMP_ID_LABEL") ? "150px" : "",
              padding: "20px 18px",
              fontSize: "16px",
              minWidth: "150px",
            },
          };
        }}
        onPageSizeChange={props.onPageSizeChange}
        currentPage={props.currentPage}
        onNextPage={props.onNextPage}
        onPrevPage={props.onPrevPage}
        pageSizeLimit={props.pageSizeLimit}
        onSort={props.onSort}
        disableSort={props.disableSort}
        // onPageSizeChange={props.onPageSizeChange}
        sortParams={props.sortParams}
        totalRecords={props.totalRecords}
      />
    );
  }

  return (
    <div className="inbox-container">
      {!props.isSearch && (
        <div className="filters-container">
          <InboxLinks
            parentRoute={props.parentRoute}
            allLinks={[
              {
                text: "HR_COMMON_CREATE_EMPLOYEE_HEAD",
                link: "/digit-ui/employee/hrms/create",
                businessService: "hrms",
                roles: ["HRMS_ADMIN"],
              },
            ]}
            headerText={"HRMS"}
            businessService={props.businessService}
          />
          <div>
            {
              <FilterComponent
                defaultSearchParams={props.defaultSearchParams}
                onFilterChange={props.onFilterChange}
                searchParams={props.searchParams}
                type="desktop"
                tenantIds={tenantIds}
              />
            }
          </div>
        </div>
      )}
      <div style={{ flex: 1 }}>
        <SearchApplication
          defaultSearchParams={props.defaultSearchParams}
          onSearch={props.onSearch}
          type="desktop"
          tenantIds={tenantIds}
          searchFields={props.searchFields}
          isInboxPage={!props?.isSearch}
          searchParams={props.searchParams}
        />
        <div className="result" style={{ marginLeft: !props?.isSearch ? "24px" : "", flex: 1 }}>
          {result}
        </div>
      </div>
     
    </div>
  );
};

export default ArchitectDetailsDesktopInbox;
