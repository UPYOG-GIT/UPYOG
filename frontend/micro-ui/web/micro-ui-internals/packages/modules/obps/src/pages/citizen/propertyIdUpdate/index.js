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
  const [sucessMessage, setSucessMessage] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      const data = await OBPSService.propertyIdUpdate(applicationNo, propertyId);
      if (data === 1) {
        setSucessMessage("Property Id updated for Application No. : " + applicationNo);
        // console.log("propertyIdUpdate Response:", data);
      }
      setErrorMessage("");
    } catch (error) {
      // console.error("Update Error:", error);
      setSucessMessage("");
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
      {sucessMessage && <div style={{ color: "green", marginBottom: "10px" }}>{sucessMessage}</div>}
      {errorMessage && <div style={{ color: "red", marginBottom: "10px" }}>{errorMessage}</div>}
      <SearchField className="submit">
        <SubmitBar label="Update" submit onClick={handleSubmit} disabled={!applicationNo || !propertyId} />
      </SearchField>
    </form>
  );
};

export default propertyIdUpdate;
