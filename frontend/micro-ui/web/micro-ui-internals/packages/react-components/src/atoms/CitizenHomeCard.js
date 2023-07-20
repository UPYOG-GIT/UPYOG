import React from "react";
import { Route, Link, useHistory, useLocation } from "react-router-dom";
import DashboardApplicationSearch from "../../../modules/core/src/pages/citizen/Home/DashboardApplicationSearch";


const CitizenHomeCard = ({ header, links, state, Icon, Info, isInfo = false, styles }) => {
  const location = useLocation();

  return (
    <div className="CitizenHomeCard" style={styles ? styles : {}}>

      <div className="header">
        <h2>{header}</h2>
        <Icon />
      </div>

      <div className="links">

        {links.map((e, i) => (
          <Link key={i} to={{ pathname: e.link, state: e.state }}>
            {e.i18nKey}
          </Link>
        ))}
      </div>

      {location.pathname === "/digit-ui/citizen/obps-home" && (
        <div className="links">
          <a href="/digit-ui/employee">Employee Login</a>
        </div>
      )}

      {location.pathname === "/digit-ui/citizen/obps-home" && (
        <div className="links">
          <a href="/digit-ui/citizen/pgr-home">Search By Application Number</a>
         

        </div>
      )}

      {location.pathname === "/digit-ui/citizen/obps-home" && (
        <div className="links" style={{ fontSize: 12, textAlign: "right", color: "#1d70b8" }}>
          <a href="/digit-ui/citizen/obps/stakeholder/apply/stakeholder-docs-required">Architect Signup</a>
        </div>
      )}

      <div>{isInfo ? <Info /> : null}</div>


    </div>
  );
};

export default CitizenHomeCard;
