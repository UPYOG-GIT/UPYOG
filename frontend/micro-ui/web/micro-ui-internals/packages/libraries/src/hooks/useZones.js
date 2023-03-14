import { useQuery } from "react-query";
import { getLocalities } from "../services/molecules/getLocalities";
import { LocalityService } from "../services/elements/Localities";
import { ZoneService } from "../services/elements/Zones";
import { getZones } from "../services/molecules/getZones";

const useZones = (tenant, boundaryType = "admin", config, t) => {


   console.log("localities" + boundaryType);


  return useQuery(["BOUNDARY_DATA", tenant, boundaryType], () => getZones[boundaryType.toLowerCase()](tenant), {
    select: (data) => {
      return ZoneService.get(data).map((key) => {
        return { ...key, i18nkey: t(key.i18nkey) };
      });
    },
    staleTime: Infinity,
    ...config,
  });
  
};

export default useZones;
