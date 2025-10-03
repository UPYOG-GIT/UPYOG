import { BackButton, SearchField, TextInput, SubmitBar, Header } from "@egovernments/digit-ui-react-components";

import React, { Fragment, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { OBPSService } from "../../../../../../libraries/src/services/elements/OBPS";

const propertyIdUpdate = ({}) => {
  const { t } = useTranslation();
  const [applicationNo, setApplicationNo] = useState("");
  const [propertyId, setPropertyId] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      const data = await OBPSService.propertyIdUpdate(applicationNo, propertyId);
      // console.log("propertyIdUpdate Response:", data);
      setErrorMessage("");
    } catch (error) {
      // console.error("Update Error:", error);
      setErrorMessage("Application number not present.");
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="cardHeaderWithOptions" style={{ marginRight: "auto", maxWidth: "960px", marginTop: 20 }}>
        <Header styles={{ fontSize: "32px" }}>{t("Update Property Id")}</Header>
      </div>

      <SearchField>
        <label>Application Number </label>
        <TextInput
          name="applicationNo"
          value={applicationNo}
          onChange={(event) => setApplicationNo(event.target.value)}
          required
          style={{ width: "400px", height: "40px", fontSize: "16px" }}
        />
      </SearchField>
      <SearchField>
        <label>Property ID </label>
        <TextInput
          name="propertyId"
          value={propertyId}
          onChange={(event) => setPropertyId(event.target.value)}
          required
          style={{ width: "400px", height: "40px", fontSize: "16px" }}
        />
      </SearchField>
      {errorMessage && (
        <div style={{ color: "red", marginBottom: "10px" }}>{errorMessage}</div>
      )}
      <SearchField className="submit">
        <SubmitBar label="Update" submit onClick={handleSubmit} disabled={!applicationNo || !propertyId} />
      </SearchField>
    </form>
  );
};

export default propertyIdUpdate;
