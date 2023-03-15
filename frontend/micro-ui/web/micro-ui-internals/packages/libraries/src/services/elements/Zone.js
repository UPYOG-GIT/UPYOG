import Urls from "../atoms/urls";
import { ServiceRequest } from "../atoms/Utils/Request";
// console.log("heyUrl2" + ServiceRequest)
export const ZoneService = {
    getZones: (tenantId) => {
      console.log("heyUrl" + ServiceRequest)
        return ServiceRequest({ 
            serviceName: "getZones",
            url: Urls.location.zones,
            params: { tenantId: tenantId },
            useCache: true,
          }) 
  },
  getRevenueLocalities: async (tenantId) => {
    const response = await ServiceRequest({
      serviceName: "getRevenueLocalities",
      url: Urls.location.revenue_localities,
      params: { tenantId: tenantId },
      useCache: true,
    });
    return response;
  },
};
