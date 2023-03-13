package org.egov.edcr.entity.blackbox;

import org.egov.common.entity.edcr.Plot;
import org.kabeja.dxf.DXFLWPolyline;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlotDetail extends Plot {

    /**
     *
     */
    private static final long serialVersionUID = 71L;
    @JsonIgnore
    protected transient DXFLWPolyline polyLine;
    protected transient DXFLWPolyline roadAreaPolyLine;

    public DXFLWPolyline getPolyLine() {
        return polyLine;
    }

    public void setPolyLine(DXFLWPolyline polyLine) {
        this.polyLine = polyLine;
    }

	public DXFLWPolyline getRoadAreaPolyLine() {
		return roadAreaPolyLine;
	}

	public void setRoadAreaPolyLine(DXFLWPolyline roadAreaPolyLine) {
		this.roadAreaPolyLine = roadAreaPolyLine;
	}

}
