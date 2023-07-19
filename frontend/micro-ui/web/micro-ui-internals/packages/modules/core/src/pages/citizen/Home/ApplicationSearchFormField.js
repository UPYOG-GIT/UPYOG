import React, { useState } from "react";
import { TextInput, SubmitBar, SearchField } from "@egovernments/digit-ui-react-components";


const ApplicationSearchFormField = ({ formState, register, reset, previousPage }) => {
  const [applicationNo, setApplicationNo] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
     
      const data = await Digit.OBPSAdminService.searchByApplicationNo( 'cg', applicationNo);

    console.log("searchByApplicationNo--" + JSON.stringify(data))

      
      console.log("Search Result:", response);
    } catch (error) {
     
      console.error("Search Error:", error);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <SearchField>
        <label>Application Number</label>
        <TextInput
          name="applicationNo"
          value={applicationNo}
          onChange={(event) => setApplicationNo(event.target.value)}
        />
      </SearchField>
      <SearchField className="submit">
        <SubmitBar label="Search" submit onClick={handleSubmit} />
      
      </SearchField>

    </form>
  );
};

export default ApplicationSearchFormField;
