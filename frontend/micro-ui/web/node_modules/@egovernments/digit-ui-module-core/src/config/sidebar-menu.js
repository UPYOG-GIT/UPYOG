import React from "react";
import { HomeIcon, LanguageIcon, LogoutIcon } from "@egovernments/digit-ui-react-components";
import ChangeLanguage from "../components/ChangeLanguage";

const SideBarMenu = (t, closeSidebar, redirectToLoginPage, isEmployee) => [
  {
    type: "link",
    element:"HOME",
    text: t("HOME"),
    link: isEmployee?"/digit-ui/employee/":"/digit-ui/citizen/",
    icon: <HomeIcon className="icon" />,
    populators: {
      onClick: closeSidebar,
    },
  },
  {
    type: "component",
    element:"LANGUAGE",
    action: <ChangeLanguage />,
    icon: <LanguageIcon className="icon" />,
  },
  { 
    id:'login-btn',
    element:"LOGIN",
    text: t("LOGIN"),
    icon: <LogoutIcon className="icon" />,
    populators: {
      onClick: redirectToLoginPage,
    },
  },
];

export default SideBarMenu;
