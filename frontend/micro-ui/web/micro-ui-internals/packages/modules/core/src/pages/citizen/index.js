import { BackButton } from "@egovernments/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useRouteMatch } from "react-router-dom";
import ErrorBoundary from "../../components/ErrorBoundaries";
import { AppHome } from "../../components/Home";
import TopBarSideBar from "../../components/TopBarSideBar";
import CitizenHome from "./Home";
import LanguageSelection from "./Home/LanguageSelection";
import LocationSelection from "./Home/LocationSelection";
import Login from "./Login";
import UserProfile from "./Home/UserProfile";
import { color, style } from "@material-ui/system";
import { Typography, Box, Chip } from "@material-ui/core";
import { List, ListItem, ListItemText, Alert } from '@mui/material';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';

const getTenants = (codes, tenants) => {
  return tenants.filter((tenant) => codes.map((item) => item.code).includes(tenant.code));
};

const Home = ({
  stateInfo,
  userDetails,
  CITIZEN,
  cityDetails,
  mobileView,
  handleUserDropdownSelection,
  logoUrl,
  DSO,
  stateCode,
  modules,
  appTenants,
  sourceUrl,
  pathname,
}) => {
  const classname = Digit.Hooks.fsm.useRouteSubscription(pathname);
  const { t } = useTranslation();
  const { path } = useRouteMatch();
  sourceUrl = "https://try-digit-eks-yourname.s3.ap-south-1.amazonaws.com";
  const pdfUrl = "https://www.entitcs.com/";

  const appRoutes = modules.map(({ code, tenants }, index) => {
    const Module = Digit.ComponentRegistryService.getComponent(`${code}Module`);
    return (
      <Route key={index} path={`${path}/${code.toLowerCase()}`}>
        <Module stateCode={stateCode} moduleCode={code} userType="citizen" tenants={getTenants(tenants, appTenants)} />
      </Route>
    );
  });

  const ModuleLevelLinkHomePages = modules.map(({ code, bannerImage }, index) => {
    let Links = Digit.ComponentRegistryService.getComponent(`${code}Links`) || (() => <React.Fragment />);

    return (
      <Route key={index} path={`${path}/${code.toLowerCase()}-home`}>
        <div className="moduleLinkHomePage">
          <img src={bannerImage || stateInfo?.bannerUrl} alt="noimagefound" />
          {/* <BackButton className="moduleLinkHomePageBackButton" /> */}
          {/* <h1>{t("MODULE_" + code.toUpperCase())}</h1> */}
        </div>
        <Box display="flex" flexDirection={{ xs: 'column', sm: 'row' }} >
          <Box flex="3" className="leftColumn">
            <Chip label="Welcome to Online Building Permission System!" style={{ width: '100%', maxWidth: 500, color: 'white', backgroundColor: '#f47738', marginTop: 20, fontSize: 19 }} />
            <Typography variant="body1" style={{ fontFamily: 'Sans-serif', color: '#444444', padding: 10, marginTop: 10, marginRight: 40, fontSize: 20, textAlign: 'justify' }}>
              Niwaspass system enables citizens of urban areas of Chhattisgarh to upload their requisite documents as per the set procedure and generate the building permission after various checks of the system. In this system, an unique Chhattisgarh model based initiative has been introduced where citizens having plot size upto 500 Sq. Mtr can get Direct Building Permission by paying a 1/- application fees.
            </Typography>

            {/* if not responsive remove marginnRight of above*/}
            <Typography variant="body1" style={{ fontFamily: 'Sans-serif', color: '#2e2e2e', marginBottom: '1rem', marginLeft: 10, fontSize: 18 }}>
              Currently the following ULB's are in this system :
            </Typography>

            <List sx={{ display: 'flex', flexDirection: 'row', flexWrap: 'wrap', marginTop: -2 }}>
              <ListItem>

                <ListItemText primary="Birgaon Municipal Corporation"  style={{color: "#444444"}} />
              </ListItem>
              <ListItem>
                <ListItemText primary="Dhamtari Municipal Corporation" style={{color: "#444444"}}/>
              </ListItem>
              <ListItem>
                <ListItemText primary="Bhilai-Charoda Municipal Corporation" style={{color: "#444444"}} />
              </ListItem>
            </List>



          </Box>
          <Box flex="1" className="rightColumn">
            <div className="moduleLinkHomePageModuleLinks">
              <Links key={index} matchPath={`/digit-ui/citizen/${code.toLowerCase()}`} userType={"citizen"} />
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', width: '1000%' }}>


            </div>

          </Box>
        </Box>

        <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between' }}>
          <Card sx={{
            width: '30%', marginBottom: '1rem', backgroundColor: 'white', boxShadow: '0px 0px 20px 5px rgba(0, 0, 0, 0.1)',
            borderRadius: '10px',
          }}>
            <CardContent>
              <Typography style={{ fontSize: 30, justifyContent: 'center', display: 'flex', color: '#EA7738' }}
                gutterBottom>
                05
              </Typography>
              <Typography style={{ color: '#EA7738', justifyContent: 'center', display: 'flex' }}>
                Uploaded Proposals
              </Typography>

            </CardContent>
          </Card>

          <Card sx={{
            width: '30%', marginBottom: '1rem', backgroundColor: 'white', boxShadow: '0px 0px 20px 5px rgba(0, 0, 0, 0.1)',
            borderRadius: '10px',
          }}>
            <CardContent>
              <Typography style={{ fontSize: 30, justifyContent: 'center', display: 'flex', color: '#EA7738' }}
                gutterBottom>
                03
              </Typography>
              <Typography style={{ color: '#EA7738', justifyContent: 'center', display: 'flex' }}>
                Approved Proposals
              </Typography>

            </CardContent>
          </Card>

          <Card sx={{
            width: '30%', marginBottom: '1rem', backgroundColor: 'white', boxShadow: '0px 0px 20px 5px rgba(0, 0, 0, 0.1)',
            borderRadius: '10px',
          }}>
            <CardContent>
              <Typography style={{ fontSize: 30, justifyContent: 'center', display: 'flex', color: '#EA7738' }}
                gutterBottom>
                02
              </Typography>
              <Typography style={{ color: '#EA7738', justifyContent: 'center', display: 'flex' }}>
                Rejected Proposals
              </Typography>

            </CardContent>
          </Card>
          

        </div>

        <Box flex="6" marginBottom={2}>
          <Alert severity="info" sx={{ maxWidth: 1300, padding: '1rem', justifyContent: 'center', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <Typography variant="h6" align="center">Notice</Typography>
            <Typography variant="body1" align="center">*Grievance Redressal Number - 1100 (Toll Free)</Typography>
            <Typography variant="body1" align="center">Install WhatsApp application on your mobile and click on <a href="#">CLICK HERE</a></Typography>
            <Typography variant="body1" align="center">Inside that, you will get links to join WhatsApp group of BPMS Support of all municipal corporation, add yourself in your respective municipal corporation by clicking on it</Typography>
          </Alert>

        </Box>

      </Route>
    );

  });


  return (
    <div className={classname}>
      <TopBarSideBar
        t={t}
        stateInfo={stateInfo}
        userDetails={userDetails}
        CITIZEN={CITIZEN}
        cityDetails={cityDetails}
        mobileView={mobileView}
        handleUserDropdownSelection={handleUserDropdownSelection}
        logoUrl={logoUrl}
        showSidebar={true}
      />
      <div
        className={"main center-container mb-25"}
        style={
          location?.pathname === "/digit-ui/citizen/select-location" || location?.pathname === "/digit-ui/citizen/register/name" || location?.pathname === "/digit-ui/citizen/register/otp" || location?.pathname === "/digit-ui/citizen/select-language" || location?.pathname === "/digit-ui/citizen/login/otp" || location?.pathname === "/digit-ui/citizen/login"
            ? {
              backgroundImage: `url("https://try-digit-eks-yourname.s3.ap-south-1.amazonaws.com/background_pic1.png")`, width: '100%', backgroundRepeat: 'no-repeat',height:'auto'
            }
            : {}
        }
      >

        <Switch>
          <Route exact path={path}>
            <CitizenHome />
          </Route>

          <Route exact path={`${path}/select-language`}>
            <LanguageSelection />
          </Route>

          <Route exact path={`${path}/select-location`}>
            <LocationSelection />
          </Route>

          <Route path={`${path}/all-services`}>
            <AppHome userType="citizen" modules={modules} />
          </Route>

          <Route path={`${path}/login`}>
            <Login stateCode={stateCode} />
          </Route>

          <Route path={`${path}/register`}>
            <Login stateCode={stateCode} isUserRegistered={false} />
          </Route>

          <Route path={`${path}/user/profile`}>
            <UserProfile stateCode={stateCode} userType={"citizen"} cityDetails={cityDetails} />
          </Route>

          <ErrorBoundary>
            {appRoutes}
            {ModuleLevelLinkHomePages}
          </ErrorBoundary>
        </Switch>
      </div>
     <div style={{ width: '100%', bottom: 0}}>
  <div style={{ display: 'flex', justifyContent: 'center', color: "#22394d", height: 'auto' }}>
    <img style={{ cursor: "pointer", display: "inline-flex", height: '1.4em' }} alt={"Powered by UPYOG"} src={`${sourceUrl}/digit-footer+copy.png`} onError={"this.src='./../digit-footer+copy.png'"} onClick={() => {
      window.open('https://www.digit.org/', '_blank').focus();
    }}></img>
    <span style={{ margin: "0 10px" }}>|</span>
    <span style={{ cursor: "pointer", fontSize: "16px", fontWeight: "400" }} onClick={() => { window.open('https://niua.in/', '_blank').focus(); }} >Copyright © 2022 National Institute of Urban Affairs</span>
    <span style={{ margin: "0 10px" }}>|</span>
    <a style={{ cursor: "pointer", fontSize: "16px", fontWeight: "400" }} href={pdfUrl} target='_blank'>Developed by </a>
    <img style={{ cursor: "pointer", display: "inline-flex", height: '1.4em' }} alt={"Developed By Entit Consulatncy Services"} src={`${sourceUrl}/entit-logo.png`} onError={"this.src='./../entit-logo.png'"} onClick={() => {
      window.open('https://www.entitcs.com/', '_blank').focus();
    }}></img>
  </div>
</div>

    </div>

  );
};

export default Home;