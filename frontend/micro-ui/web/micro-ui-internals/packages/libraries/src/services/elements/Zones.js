import { LocalizationService } from "./Localization/service";

const ADMIN_CODE = ({ tenantId, hierarchyType }) => {
  return tenantId.replace(".", "_").toUpperCase() + "_" + hierarchyType.code;
};

const getI18nKeys = (ZonesWithLocalizationKeys) => {
  return ZonesWithLocalizationKeys.map((zone) => ({
    code: zone.code,
    message: zone.name,
  }));
};

const getZones = (tenantBoundry) => {
  console.log("tenantBoundry===99999==="+JSON.stringify(tenantBoundry));
  const adminCode = ADMIN_CODE(tenantBoundry);
  const zonesWithLocalizationKeys = tenantBoundry.boundary.map((boundaryObj) => ({
    ...boundaryObj,
    i18nkey: adminCode + "_" + boundaryObj.code,
  }));
  // console.log("HYyyyy" + JSON.stringify(zonesWithLocalizationKeys))

  
  return zonesWithLocalizationKeys;
};
// console.log("heyyy" + zonesWithLocalizationKeys)

export const ZoneService = {
  get: (tenantBoundry) => getZones(tenantBoundry),
 
  
};
