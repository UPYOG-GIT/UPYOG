import React, { Fragment, useCallback, useMemo, useReducer } from "react";
import { InboxComposer, CaseIcon, Header } from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import FilterFormFieldsComponent from "./FilterFormFieldsComponent";
import SearchFormFieldsComponents from "./SearchFormFieldsComponent";
import useInboxTableConfig from "./useInboxTableConfig";
import useInboxMobileCardsData from "./useInboxMobileCardsData";

const Inbox = ({ parentRoute }) => {
  window.scroll(0, 0);
  const { t } = useTranslation();

  const userInfo = Digit.UserService.getUser();
  // console.log("userInfo: " + JSON.stringify(userInfo))
  // const tenantId = Digit.ULBService.getCurrentTenantId();
  // const tenantIdCitizen = Digit.ULBService.getCitizenCurrentTenant();  // for citizen
  // const tenantIdEmployee = Digit.ULBService.getCurrentUlb()?.code;  //for employee
  const tenantId = userInfo?.info?.type === 'EMPLOYEE' ? Digit.ULBService.getCurrentUlb()?.code : Digit.ULBService.getCitizenCurrentTenant();  //for employee
  // const tenantId = userInfo?.info?.type === 'EMPLOYEE' ? tenantIdEmployee: tenantIdCitizen;  //for employee
  // const tenantId = userInfo?.info?.tenantId;  //for employee
  const userType = window.sessionStorage.getItem("userType");
  // console.log(userInfo?.info?.type);
  // console.log(userInfo?.info?.tenantId);
  // console.log("userType: "+userType);

  // 

  // console.log("tenanrId: " + tenantId)
  // console.log(Digit.ULBService.getCitizenCurrentTenant())
  // console.log(Digit.ULBService.getCurrentUlb()?.code)

  const searchFormDefaultValues = {};

  const filterFormDefaultValues = {
    moduleName: "bpa-services",
    applicationStatus: [],
    locality: [],
    assignee: "ASSIGNED_TO_ALL",
    applicationType: [],
  };
  const tableOrderFormDefaultValues = {
    sortBy: "",
    limit: Digit.Utils.browser.isMobile() ? 50 : 10,
    offset: 0,
    sortOrder: "DESC",
  };

  function formReducer(state, payload) {
    switch (payload.action) {
      case "mutateSearchForm":
        Digit.SessionStorage.set("OBPS.INBOX", { ...state, searchForm: payload.data });
        return { ...state, searchForm: payload.data };
      case "mutateFilterForm":
        Digit.SessionStorage.set("OBPS.INBOX", { ...state, filterForm: payload.data });
        return { ...state, filterForm: payload.data };
      case "mutateTableForm":
        Digit.SessionStorage.set("OBPS.INBOX", { ...state, tableForm: payload.data });
        return { ...state, tableForm: payload.data };
      default:
        break;
    }
  }
  const InboxObjectInSessionStorage = Digit.SessionStorage.get("OBPS.INBOX");

  const onSearchFormReset = (setSearchFormValue) => {
    setSearchFormValue("mobileNumber", null);
    setSearchFormValue("applicationNo", null);
    dispatch({ action: "mutateSearchForm", data: searchFormDefaultValues });
  };

  const onFilterFormReset = (setFilterFormValue) => {
    setFilterFormValue("moduleName", "bpa-services");
    setFilterFormValue("applicationStatus", "");
    setFilterFormValue("locality", []);
    setFilterFormValue("assignee", "ASSIGNED_TO_ALL");
    setFilterFormValue("applicationType", []);
    dispatch({ action: "mutateFilterForm", data: filterFormDefaultValues });
  };

  const onSortFormReset = (setSortFormValue) => {
    setSortFormValue("sortOrder", "DESC");
    dispatch({ action: "mutateTableForm", data: tableOrderFormDefaultValues });
  };

  const formInitValue = useMemo(() => {
    return (
      InboxObjectInSessionStorage || {
        filterForm: filterFormDefaultValues,
        searchForm: searchFormDefaultValues,
        tableForm: tableOrderFormDefaultValues,
      }
    );
  }, [
    Object.values(InboxObjectInSessionStorage?.filterForm || {}),
    Object.values(InboxObjectInSessionStorage?.searchForm || {}),
    Object.values(InboxObjectInSessionStorage?.tableForm || {}),
  ]);

  const [formState, dispatch] = useReducer(formReducer, formInitValue);
  const onPageSizeChange = (e) => {
    dispatch({ action: "mutateTableForm", data: { ...formState.tableForm, limit: e.target.value } });
  };
  const onSortingByData = (e) => {
    if (e.length > 0) {
      const [{ id, desc }] = e;
      const sortOrder = desc ? "DESC" : "ASC";
      const sortBy = id;
      if (!(formState.tableForm.sortBy === sortBy && formState.tableForm.sortOrder === sortOrder)) {
        dispatch({ action: "mutateTableForm", data: { ...formState.tableForm, sortBy: id, sortOrder: desc ? "DESC" : "ASC" } });
      }
    }
  };

  const onMobileSortOrderData = (data) => {
    const { sortOrder } = data;
    dispatch({ action: "mutateTableForm", data: { ...formState.tableForm, sortOrder } });
  };

  const getRedirectionLink = (bService) => {
    let redirectBS = "";
    if (bService === "BPAREG") {
      redirectBS = "search/application/stakeholder";
    } else {
      redirectBS = window.location.href.includes("/citizen") ? "bpa" : "search/application/bpa"
    }
    return redirectBS;
  };
  const { data: localitiesForEmployeesCurrentTenant, isLoading: loadingLocalitiesForEmployeesCurrentTenant } = Digit.Hooks.useBoundaryLocalities(
    tenantId,
    "revenue",
    {},
    t
  );

  const { isLoading: isInboxLoading, data: { table, statuses, totalCount } = {} } = Digit.Hooks.obps.useBPAInbox({
    tenantId,
    filters: { ...formState },
  });

  const PropsForInboxLinks = {
    logoIcon: <CaseIcon />,
    headerText: "CS_COMMON_OBPS",
    links: [
      {
        text: t("BPA_SEARCH_PAGE_TITLE"),
        link: window.location.href.includes("/citizen") ? "/digit-ui/citizen/obps/search/application" : "/digit-ui/employee/obps/search/application",
        businessService: "BPA",
        roles: ["BPAREG_EMPLOYEE", "BPAREG_APPROVER", "BPAREG_DOC_VERIFIER", "BPAREG_DOC_VERIFIER"],
      },
    ],
  };

  const SearchFormFields = useCallback(
    ({ registerRef, searchFormState, searchFieldComponents }) => (
      <SearchFormFieldsComponents {...{ registerRef, searchFormState, searchFieldComponents }} />
    ),
    []
  );

  const FilterFormFields = useCallback(
    ({ registerRef, controlFilterForm, setFilterFormValue, getFilterFormValue }) => (
      <FilterFormFieldsComponent
        {...{
          statuses,
          isInboxLoading,
          registerRef,
          controlFilterForm,
          setFilterFormValue,
          filterFormState: formState?.filterForm,
          getFilterFormValue,
          localitiesForEmployeesCurrentTenant,
          loadingLocalitiesForEmployeesCurrentTenant,
        }}
      />
    ),
    [statuses, isInboxLoading, localitiesForEmployeesCurrentTenant, loadingLocalitiesForEmployeesCurrentTenant]
  );

  const onSearchFormSubmit = (data) => {
    data.hasOwnProperty("") && delete data?.[""];
    dispatch({ action: "mutateTableForm", data: { ...tableOrderFormDefaultValues } });
    dispatch({ action: "mutateSearchForm", data });
  };

  const onFilterFormSubmit = (data) => {
    data.hasOwnProperty("") && delete data?.[""];
    dispatch({ action: "mutateTableForm", data: { ...tableOrderFormDefaultValues } });
    dispatch({ action: "mutateFilterForm", data });
  };

  const propsForSearchForm = {
    SearchFormFields,
    onSearchFormSubmit,
    searchFormDefaultValues: formState?.searchForm,
    resetSearchFormDefaultValues: searchFormDefaultValues,
    onSearchFormReset,
  };

  const propsForFilterForm = {
    FilterFormFields,
    onFilterFormSubmit,
    filterFormDefaultValues: formState?.filterForm,
    resetFilterFormDefaultValues: filterFormDefaultValues,
    onFilterFormReset,
  };

  const propsForInboxTable = useInboxTableConfig({ ...{ parentRoute, onPageSizeChange, formState, totalCount, table, dispatch, onSortingByData } });

  const propsForInboxMobileCards = useInboxMobileCardsData({ parentRoute, table, getRedirectionLink });

  const propsForMobileSortForm = { onMobileSortOrderData, sortFormDefaultValues: formState?.tableForm, onSortFormReset };

  return (
    <>
      <Header>
        {t("ES_COMMON_INBOX")}
        {totalCount ? <p className="inbox-count">{totalCount}</p> : null}
      </Header>
      <InboxComposer
        {...{
          isInboxLoading,
          PropsForInboxLinks,
          ...propsForSearchForm,
          ...propsForFilterForm,
          ...propsForMobileSortForm,
          propsForInboxTable,
          propsForInboxMobileCards,
          formState,
        }}
      ></InboxComposer>
    </>
  );
};

export default Inbox;
