/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.regularisation.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.PlanInformation;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.persistence.entity.AbstractAuditable;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "EDCR_REG_APPLICATION_DETAIL")
@SequenceGenerator(name = RegEdcrApplicationDetail.SEQ_EDCR_APPLICATION_DETAIL, sequenceName = RegEdcrApplicationDetail.SEQ_EDCR_APPLICATION_DETAIL, allocationSize = 1)
public class RegEdcrApplicationDetail extends AbstractAuditable {

	public static final String SEQ_EDCR_APPLICATION_DETAIL = "SEQ_EDCR_REG_APPLICATION_DETAIL";
	private static final long serialVersionUID = 62L;

	@Id
	@GeneratedValue(generator = SEQ_EDCR_APPLICATION_DETAIL, strategy = GenerationType.SEQUENCE)
	private Long id;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "dxffileid")
	private FileStoreMapper dxfFileId;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "reportoutputid")
	private FileStoreMapper reportOutputId;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "application")
	private RegEdcrApplication application;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "planDetailFileStore")
	private FileStoreMapper planDetailFileStore;

	@Length(min = 1, max = 128)
	private String dcrNumber;

	private String status;

	@Transient
	private Plan plan;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "scrutinizedDxfFileId")
	private FileStoreMapper scrutinizedDxfFileId;

	@OneToMany(mappedBy = "regEdcrApplicationDetail", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OrderBy("id DESC ")
	private List<RegEdcrPdfDetail> regEdcrPdfDetails = new ArrayList<>();

	private String khataNo;
	
	private String mauza;

	private String plotNo;

	private BigDecimal plotArea;

	@Transient
	private Long noOfErrors;

	@Transient
	private PlanInformation planInformation;

	@Length(min = 1, max = 128)
	private String comparisonDcrNumber;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	protected void setId(final Long id) {
		this.id = id;
	}

	public RegEdcrApplication getApplication() {
		return application;
	}

	public void setApplication(RegEdcrApplication application) {
		this.application = application;
	}

	public FileStoreMapper getDxfFileId() {
		return dxfFileId;
	}

	public void setDxfFileId(FileStoreMapper dxfFileId) {
		this.dxfFileId = dxfFileId;
	}

	public FileStoreMapper getReportOutputId() {
		return reportOutputId;
	}

	public void setReportOutputId(FileStoreMapper reportOutputId) {
		this.reportOutputId = reportOutputId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public FileStoreMapper getPlanDetailFileStore() {
		return planDetailFileStore;
	}

	public void setPlanDetailFileStore(FileStoreMapper planDetailFileStore) {
		this.planDetailFileStore = planDetailFileStore;
	}

	public String getDcrNumber() {
		return dcrNumber;
	}

	public void setDcrNumber(String dcrNumber) {
		this.dcrNumber = dcrNumber;
	}

	public FileStoreMapper getScrutinizedDxfFileId() {
		return scrutinizedDxfFileId;
	}

	public void setScrutinizedDxfFileId(FileStoreMapper scrutinizedDxfFileId) {
		this.scrutinizedDxfFileId = scrutinizedDxfFileId;
	}

	public List<RegEdcrPdfDetail> getEdcrPdfDetails() {
		return regEdcrPdfDetails;
	}

	public void setEdcrPdfDetails(List<RegEdcrPdfDetail> regEdcrPdfDetails) {
		this.regEdcrPdfDetails = regEdcrPdfDetails;
	}

	public Long getNoOfErrors() {
		return noOfErrors;
	}

	public void setNoOfErrors(Long noOfErrors) {
		this.noOfErrors = noOfErrors;
	}

	public PlanInformation getPlanInformation() {
		return planInformation;
	}

	public void setPlanInformation(PlanInformation planInformation) {
		this.planInformation = planInformation;
	}

	public Plan getPlan() {
		return plan;
	}

	public void setPlan(Plan plan) {
		this.plan = plan;
	}

	public String getComparisonDcrNumber() {
		return comparisonDcrNumber;
	}

	public void setComparisonDcrNumber(String comparisonDcrNumber) {
		this.comparisonDcrNumber = comparisonDcrNumber;
	}

	public String getKhataNo() {
		return khataNo;
	}

	public void setKhataNo(String khataNo) {
		this.khataNo = khataNo;
	}

	public String getMauza() {
		return mauza;
	}

	public void setMauza(String mauza) {
		this.mauza = mauza;
	}

	public String getPlotNo() {
		return plotNo;
	}

	public void setPlotNo(String plotNo) {
		this.plotNo = plotNo;
	}

	public BigDecimal getPlotArea() {
		return plotArea;
	}

	public void setPlotArea(BigDecimal plotArea) {
		this.plotArea = plotArea;
	}

	
}
