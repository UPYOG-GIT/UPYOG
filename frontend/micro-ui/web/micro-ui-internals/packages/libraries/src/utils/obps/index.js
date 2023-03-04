export const calculateRiskType = (riskTypes, plotArea, blocks) => {
  const buildingHeight = blocks?.reduce((acc, block) => {
    return Math.max(acc, block.building.buildingHeight)
  }, Number.NEGATIVE_INFINITY);

  const risk = riskTypes?.find(riskType => {
   if(buildingHeight >= riskType?.fromBuildingHeight){
        if (riskType.riskType === "HIGH" && plotArea > riskType?.fromPlotArea) {
            return true;
        }
    }else{
        if (riskType.riskType === "MEDIUM" && (plotArea >= riskType?.fromPlotArea && plotArea <= riskType?.toPlotArea)) {
            return true;
        } else if (riskType?.riskType === "LOW" && (plotArea >= riskType?.fromPlotArea && plotArea <= riskType?.toPlotArea)) {
            return true;
        } else if (riskType?.riskType === "VLOW" && plotArea < riskType.toPlotArea) {
            return true;
        }
    }

    return false;
  })
  return risk?.riskType;
}
