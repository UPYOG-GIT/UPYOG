package org.entit.rga.web.model;

import java.util.Objects;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RGARequest {

	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo = null;

	@JsonProperty("RGA")
	private RGA RGA = null;

	public RGARequest requestInfo(RequestInfo requestInfo) {
		this.requestInfo = requestInfo;
		return this;
	}

	@Valid
	public RGA getRegularisation() {
		return RGA;
	}

	public void setRegularisation(RGA RGA) {
		this.RGA = RGA;
	}
	
	@Override
	  public boolean equals(java.lang.Object o) {
	    if (this == o) {
	      return true;
	    }
	    if (o == null || getClass() != o.getClass()) {
	      return false;
	    }
	    RGARequest bpARequest = (RGARequest) o;
	    return Objects.equals(this.requestInfo, bpARequest.requestInfo) &&
	        Objects.equals(this.RGA, bpARequest.RGA);
	  }

	  @Override
	  public int hashCode() {
	    return Objects.hash(requestInfo, RGA);
	  }

	  @Override
	  public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("class RegularisationRequest {\n");
	    
	    sb.append("    requestInfo: ").append(toIndentedString(requestInfo)).append("\n");
	    sb.append("    RGA: ").append(toIndentedString(RGA)).append("\n");
	    sb.append("}");
	    return sb.toString();
	  }

	  /**
	   * Convert the given object to string with each line indented by 4 spaces
	   * (except the first line).
	   */
	  private String toIndentedString(java.lang.Object o) {
	    if (o == null) {
	      return "null";
	    }
	    return o.toString().replace("\n", "\n    ");
	  }
}
