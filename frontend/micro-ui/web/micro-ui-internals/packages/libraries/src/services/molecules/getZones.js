import { LocationService } from "../elements/Location";
import { ZoneService } from "../elements/Zone";
import { StoreService } from "./Store/service";

export const getZones = {
 
  admin: async (tenant) => {
    // console.log("HeyyyyTenant" + TenantBoundary[0])
   
    await StoreService.defaultData(tenant, tenant, Digit.StoreData.getCurrentLanguage());
    return (await ZoneService.getZones(tenant)).TenantBoundary[0];
   
  },
  revenue: async (tenant) => {
    await StoreService.defaultData(tenant, tenant, Digit.StoreData.getCurrentLanguage());
    return (await LocationService.getRevenueLocalities(tenant)).TenantBoundary[0];
  },
  
};
