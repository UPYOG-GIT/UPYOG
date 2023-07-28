package org.egov.regularisation.service;

import static org.egov.infra.utils.PdfUtils.appendFiles;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.egov.common.entity.edcr.EdcrPdfDetail;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.PlanFeature;
import org.egov.common.entity.edcr.PlanInformation;
import org.egov.edcr.entity.AmendmentDetails;
import org.egov.edcr.utility.DcrConstants;
import org.egov.infra.custom.CustomImplProvider;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.regularisation.constants.DxfFileConstants;
import org.egov.regularisation.contract.RegEdcrRequest;
import org.egov.regularisation.entity.RegAmendment;
import org.egov.regularisation.entity.RegApplicationType;
import org.egov.regularisation.entity.RegEdcrApplication;
import org.egov.regularisation.entity.RegEdcrApplicationDetail;
import org.egov.regularisation.extract.service.RegAmendmentService;
import org.egov.regularisation.feature.FeatureProcess;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class RegularisationPlanService {
	private static final Logger LOG = LogManager.getLogger(RegularisationPlanService.class);
	@Autowired
	private RegPlanFeatureService featureService;
	@Autowired
	private FileStoreService fileStoreService;
	@Autowired
	private CustomImplProvider specificRuleService;
	@Autowired
	private RegEdcrApplicationDetailService edcrApplicationDetailService;
	@Autowired
	private RegEdcrPdfDetailService regEdcrPdfDetailService;
	@Autowired
	private RegularisationExtractService extractService;
	@Autowired
	private RegEdcrApplicationService edcrApplicationService;
//    @Autowired
//    private OcComparisonService ocComparisonService;
//    @Autowired
//    private OcComparisonDetailService ocComparisonDetailService;

	public Plan process(RegEdcrApplication dcrApplication, String applicationType) {
		Map<String, String> cityDetails = specificRuleService.getCityDetails();

		Date asOnDate = null;
		if (dcrApplication.getPermitApplicationDate() != null) {
			asOnDate = dcrApplication.getPermitApplicationDate();
		} else if (dcrApplication.getApplicationDate() != null) {
			asOnDate = dcrApplication.getApplicationDate();
		} else {
			asOnDate = new Date();
		}

		RegAmendmentService repo = (RegAmendmentService) specificRuleService.find("regAmendmentService");
		RegAmendment amd = repo.getAmendments();

		Plan plan = extractService.extract(dcrApplication.getSavedDxfFile(), amd, asOnDate,
				featureService.getFeatures());
		plan.setMdmsMasterData(dcrApplication.getMdmsMasterData());

//        //add areaCategory in Plan class
//        plan.setAreaCategory(dcrApplication.getAreaCategory());

		plan = applyRules(plan, amd, cityDetails);

		String comparisonDcrNumber = dcrApplication.getEdcrApplicationDetails().get(0).getComparisonDcrNumber();
		if (RegApplicationType.PERMIT.getApplicationTypeVal()
				.equalsIgnoreCase(dcrApplication.getApplicationType().getApplicationType())
				|| (RegApplicationType.OCCUPANCY_CERTIFICATE.getApplicationTypeVal()
						.equalsIgnoreCase(dcrApplication.getApplicationType().getApplicationType())
						&& StringUtils.isBlank(comparisonDcrNumber))
				|| RegApplicationType.REGULARISATION.getApplicationTypeVal()
						.equalsIgnoreCase(dcrApplication.getApplicationType().getApplicationType())) {
			InputStream reportStream = generateReport(plan, amd, dcrApplication);
			saveOutputReport(dcrApplication, reportStream, plan);
		} else if (RegApplicationType.OCCUPANCY_CERTIFICATE.getApplicationTypeVal()
				.equalsIgnoreCase(dcrApplication.getApplicationType().getApplicationType())
				&& StringUtils.isNotBlank(comparisonDcrNumber)) {
//            ComparisonRequest comparisonRequest = new ComparisonRequest();
			RegEdcrApplicationDetail regEdcrApplicationDetail = dcrApplication.getEdcrApplicationDetails().get(0);
//            comparisonRequest.setEdcrNumber(edcrApplicationDetail.getComparisonDcrNumber());
//            comparisonRequest.setTenantId(edcrApplicationDetail.getApplication().getThirdPartyUserTenant());
			regEdcrApplicationDetail.setPlan(plan);

//            OcComparisonDetail processCombinedStatus = ocComparisonService.processCombinedStatus(comparisonRequest,
//                    edcrApplicationDetail);

//            dcrApplication.setDeviationStatus(processCombinedStatus.getStatus());

			InputStream reportStream = generateReport(plan, amd, dcrApplication);
			saveOutputReport(dcrApplication, reportStream, plan);
			final List<InputStream> pdfs = new ArrayList<>();
			Path path = fileStoreService.fetchAsPath(
					dcrApplication.getEdcrApplicationDetails().get(0).getReportOutputId().getFileStoreId(),
					"Digit DCR");
			byte[] convertedDigitDcr = null;
			try {
				convertedDigitDcr = Files.readAllBytes(path);
			} catch (IOException e) {
				LOG.error("Error occurred while reading file!!!", e);
			}
			ByteArrayInputStream dcrReport = new ByteArrayInputStream(convertedDigitDcr);
			pdfs.add(dcrReport);

			if (Boolean.TRUE.equals(plan.getMainDcrPassed())) {
//                OcComparisonDetail ocComparisonE = ocComparisonService.processCombined(processCombinedStatus,
//                        edcrApplicationDetail);

				String fileName;
//                if(StringUtils.isBlank(ocComparisonE.getOcdcrNumber()))
//                    fileName = ocComparisonE.getDcrNumber() + "-comparison" + ".pdf";
//                else
//                    fileName = ocComparisonE.getOcdcrNumber() + "-" + ocComparisonE.getDcrNumber() +
//                        "-comparison" + ".pdf";
//                final FileStoreMapper fileStoreMapper = fileStoreService.store(ocComparisonE.getOutput(), fileName,
//                        "application/pdf",
//                        DcrConstants.FILESTORE_MODULECODE);
//                ocComparisonE.setOcComparisonReport(fileStoreMapper);
//                if (StringUtils.isNotBlank(dcrApplication.getEdcrApplicationDetails().get(0).getDcrNumber())) {
//                    ocComparisonE.setOcdcrNumber(dcrApplication.getEdcrApplicationDetails().get(0).getDcrNumber());
//                }
//                ocComparisonDetailService.saveAndFlush(ocComparisonE);

//                Path ocPath = fileStoreService.fetchAsPath(ocComparisonE.getOcComparisonReport().getFileStoreId(),
//                        "Digit DCR");
				byte[] convertedComparison = null;
//                try {
//                    convertedComparison = Files.readAllBytes(ocPath);
//                } catch (IOException e) {
//                    LOG.error("Error occurred while reading file!!!", e);
//                }
				ByteArrayInputStream comparisonReport = new ByteArrayInputStream(convertedComparison);
				pdfs.add(comparisonReport);
			}

			final byte[] data = appendFiles(pdfs);
			InputStream targetStream = new ByteArrayInputStream(data);
			saveOutputReport(dcrApplication, targetStream, plan);
			updateFinalReport(dcrApplication.getEdcrApplicationDetails().get(0).getReportOutputId());
		}
		return plan;
	}

	public void savePlanDetail(Plan plan, RegEdcrApplicationDetail detail) {

		if (LOG.isInfoEnabled())
			LOG.info("*************Before serialization******************");
		File f = new File("plandetail.txt");
		try (FileOutputStream fos = new FileOutputStream(f); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			mapper.writeValue(f, plan);
			detail.setPlanDetailFileStore(
					fileStoreService.store(f, f.getName(), "text/plain", DcrConstants.APPLICATION_MODULE_TYPE));
			oos.flush();
		} catch (IOException e) {
			LOG.error("Unable to serialize!!!!!!", e);
		}
		if (LOG.isInfoEnabled())
			LOG.info("*************Completed serialization******************");

	}

	private Plan applyRules(Plan plan, RegAmendment amd, Map<String, String> cityDetails) {

		// check whether valid amendments are present
		int index = -1;
		AmendmentDetails[] a = null;
		int length = amd.getDetails().size();
		if (!amd.getDetails().isEmpty()) {
			index = amd.getIndex(plan.getApplicationDate());
			a = new AmendmentDetails[amd.getDetails().size()];
			amd.getDetails().toArray(a);
		}

		for (PlanFeature ruleClass : featureService.getFeatures()) {

			FeatureProcess rule = null;
			String str = ruleClass.getRuleClass().getSimpleName();
			str = str.substring(0, 1).toLowerCase() + str.substring(1);
			LOG.info("Looking for bean " + str);
			// when amendments are not present
			if (amd.getDetails().isEmpty() || index == -1)
				rule = (FeatureProcess) specificRuleService.find(ruleClass.getRuleClass().getSimpleName());
			// when amendments are present
			else {
				if (index >= 0) {
					// find amendment specific beans
					for (int i = index; i < length; i++) {
						if (a[i].getChanges().keySet().contains(ruleClass.getRuleClass().getSimpleName())) {
							String strNew = str + "_" + a[i].getDateOfBylawString();
							rule = (FeatureProcess) specificRuleService.find(strNew);
							if (rule != null)
								break;
						}
					}
					// when amendment specific beans not found
					if (rule == null) {
						rule = (FeatureProcess) specificRuleService.find(ruleClass.getRuleClass().getSimpleName());
					}

				}

			}

			if (rule != null) {
				LOG.info("Looking for bean resulted in " + rule.getClass().getSimpleName());
				rule.process(plan);
				LOG.info("Completed Process " + rule.getClass().getSimpleName() + "  " + new Date());
			}

//            if (plan.getErrors().containsKey(DxfFileConstants.OCCUPANCY_ALLOWED_KEY)
//                    || plan.getErrors().containsKey("units not in meters")
//                    || plan.getErrors().containsKey(DxfFileConstants.OCCUPANCY_PO_NOT_ALLOWED_KEY))
//                return plan;
		}
		return plan;
	}

	private InputStream generateReport(Plan plan, RegAmendment amd, RegEdcrApplication dcrApplication) {

		String beanName = "RegPlanReportService";
		RegPlanReportService service = null;
		int index = -1;
		AmendmentDetails[] amdArray = null;
		InputStream reportStream = null;
		int length = amd.getDetails().size();
		if (!amd.getDetails().isEmpty()) {
			index = amd.getIndex(plan.getApplicationDate());
			amdArray = new AmendmentDetails[amd.getDetails().size()];
			amd.getDetails().toArray(amdArray);
		}

		try {
			beanName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);

			if (amd.getDetails().isEmpty() || index == -1)
				service = (RegPlanReportService) specificRuleService.find(beanName);
			else if (index >= 0) {
				for (int i = index; i < length; i++) {

					service = (RegPlanReportService) specificRuleService
							.find(beanName + "_" + amdArray[i].getDateOfBylawString());
					if (service != null)
						break;
				}
			}
			if (service == null) {
				service = (RegPlanReportService) specificRuleService.find(beanName);
			}

			reportStream = service.generateReport(plan, dcrApplication);

		} catch (BeansException e) {
			LOG.error("No Bean Defined for the Rule " + beanName);
		}

		return reportStream;
	}

	@Transactional
	public void saveOutputReport(RegEdcrApplication regEdcrApplication, InputStream reportOutputStream, Plan plan) {

		List<RegEdcrApplicationDetail> regEdcrApplicationDetails = edcrApplicationDetailService
				.fingByDcrApplicationId(regEdcrApplication.getId());
//        edcrApplicationDetails.get(0).setKhataNo(plan.getPlanInformation().getKhataNo());
//        edcrApplicationDetails.get(0).setMauza(plan.getPlanInformation().getMauza());
//        edcrApplicationDetails.get(0).setPlotNo(plan.getPlanInformation().getPlotNo());
//        edcrApplicationDetails.get(0).setPlotArea(plan.getPlanInformation().getPlotArea());
		final String fileName = regEdcrApplication.getApplicationNumber() + "-v" + regEdcrApplicationDetails.size()
				+ ".pdf";

		final FileStoreMapper fileStoreMapper = fileStoreService.store(reportOutputStream, fileName, "application/pdf",
				DcrConstants.FILESTORE_MODULECODE);

		buildDocuments(regEdcrApplication, null, fileStoreMapper, plan);

		PlanInformation planInformation = plan.getPlanInformation();
		regEdcrApplication.getEdcrApplicationDetails().get(0).setPlanInformation(planInformation);

		edcrApplicationDetailService.saveAll(regEdcrApplication.getEdcrApplicationDetails());
	}

	public void buildDocuments(RegEdcrApplication regEdcrApplication, FileStoreMapper dxfFile,
			FileStoreMapper reportOutput, Plan plan) {

		if (dxfFile != null) {
			RegEdcrApplicationDetail regEdcrApplicationDetail = new RegEdcrApplicationDetail();

			regEdcrApplicationDetail.setDxfFileId(dxfFile);
			regEdcrApplicationDetail.setApplication(regEdcrApplication);
			for (RegEdcrApplicationDetail edcrApplicationDetail1 : regEdcrApplication.getEdcrApplicationDetails()) {
				regEdcrApplicationDetail.setPlan(edcrApplicationDetail1.getPlan());
			}
			List<RegEdcrApplicationDetail> regEdcrApplicationDetails = new ArrayList<>();
			regEdcrApplicationDetails.add(regEdcrApplicationDetail);
			regEdcrApplication.setSavedEdcrApplicationDetail(regEdcrApplicationDetail);
			regEdcrApplication.setEdcrApplicationDetails(regEdcrApplicationDetails);
		}

		if (reportOutput != null) {
			RegEdcrApplicationDetail regEdcrApplicationDetail = regEdcrApplication.getEdcrApplicationDetails().get(0);

			if (plan.getEdcrPassed()) {
				if (plan.getEdcrPartialPassed()) {
					regEdcrApplicationDetail.setStatus("Partial Accepted");
					regEdcrApplication.setStatus("Partial Accepted");
				} else {
					regEdcrApplicationDetail.setStatus("Accepted");
					regEdcrApplication.setStatus("Accepted");
				}
			} else {
				regEdcrApplicationDetail.setStatus("Not Accepted");
				regEdcrApplication.setStatus("Not Accepted");
			}
			regEdcrApplicationDetail.setCreatedDate(new Date());
			regEdcrApplicationDetail.setReportOutputId(reportOutput);
			List<RegEdcrApplicationDetail> regEdcrApplicationDetails = new ArrayList<>();
			regEdcrApplicationDetails.add(regEdcrApplicationDetail);
			savePlanDetail(plan, regEdcrApplicationDetail);

			ArrayList<org.egov.regularisation.entity.RegEdcrPdfDetail> regEdcrPdfDetails = new ArrayList<>();

			if (plan.getEdcrPdfDetails() != null && !plan.getEdcrPdfDetails().isEmpty()) {
				for (EdcrPdfDetail edcrPdfDetail : plan.getEdcrPdfDetails()) {
					org.egov.regularisation.entity.RegEdcrPdfDetail pdfDetail = new org.egov.regularisation.entity.RegEdcrPdfDetail();
					pdfDetail.setLayer(edcrPdfDetail.getLayer());
					pdfDetail.setFailureReasons(edcrPdfDetail.getFailureReasons());
					pdfDetail.setStandardViolations(edcrPdfDetail.getStandardViolations());

					File convertedPdf = edcrPdfDetail.getConvertedPdf();
					if (convertedPdf != null && convertedPdf.length() > 0) {
						FileStoreMapper fileStoreMapper = fileStoreService.store(convertedPdf, convertedPdf.getName(),
								DcrConstants.PDF_EXT, DcrConstants.FILESTORE_MODULECODE);
						pdfDetail.setConvertedPdf(fileStoreMapper);
						pdfDetail.setEdcrApplicationDetail(regEdcrApplicationDetail);
						regEdcrPdfDetails.add(pdfDetail);
					}
				}
			}

			if (!regEdcrPdfDetails.isEmpty()) {
				regEdcrApplicationDetail.getEdcrPdfDetails().addAll(regEdcrPdfDetails);
				regEdcrPdfDetailService.saveAll(regEdcrPdfDetails);
			}

			regEdcrApplication.setEdcrApplicationDetails(regEdcrApplicationDetails);
		}
	}

	public Plan extractPlan(RegEdcrRequest regEdcrRequest, MultipartFile dxfFile) {
		File planFile = edcrApplicationService.savePlanDXF(dxfFile);

		Date asOnDate = new Date();

		RegAmendmentService repo = (RegAmendmentService) specificRuleService
				.find(RegAmendmentService.class.getSimpleName());
		RegAmendment amd = repo.getAmendments();

		Plan plan = extractService.extract(planFile, amd, asOnDate, featureService.getFeatures());
		if (StringUtils.isNotBlank(regEdcrRequest.getApplicantName()))
			plan.getPlanInformation().setApplicantName(regEdcrRequest.getApplicantName());
		else
			plan.getPlanInformation().setApplicantName(DxfFileConstants.ANONYMOUS_APPLICANT);

		return plan;
	}

	private void updateFinalReport(FileStoreMapper fileStoreMapper) {
		try {
			Path path = fileStoreService.fetchAsPath(fileStoreMapper.getFileStoreId(), "Digit DCR");

			PDDocument doc = PDDocument.load(new File(path.toString()));
			for (int i = 0; i < doc.getNumberOfPages(); i++) {
				PDPage page = doc.getPage(i);
				PDPageContentStream contentStream = new PDPageContentStream(doc, page,
						PDPageContentStream.AppendMode.APPEND, true);
				/*
				 * if (i == 0) { contentStream.setNonStrokingColor(Color.white);
				 * contentStream.addRect(275, 720, 60, 20); contentStream.fill();
				 * contentStream.setNonStrokingColor(Color.black); contentStream.beginText();
				 * contentStream.newLineAtOffset(275, 720);
				 * contentStream.setFont(PDType1Font.TIMES_BOLD, 12); if
				 * ("Not Accepted".equalsIgnoreCase(status)) {
				 * contentStream.setNonStrokingColor(Color.RED); } else {
				 * contentStream.setNonStrokingColor(0,127,0); } contentStream.showText(status);
				 * contentStream.endText(); }
				 */
				// page coordinate
				contentStream.setNonStrokingColor(Color.white);
				contentStream.addRect(230, 20, 80, 40);
				contentStream.fill();

				contentStream.setNonStrokingColor(Color.black);
				contentStream.beginText();

				contentStream.newLineAtOffset(248, 23);

				contentStream.setFont(PDType1Font.TIMES_ROMAN, 10);
				String text = (i + 1) + " of " + doc.getNumberOfPages();
				contentStream.showText(text);
				contentStream.endText();
				contentStream.close();
			}
			doc.save(new File(path.toString()));
			doc.close();
		} catch (IOException e) {
			LOG.error("error", e);
		}
	}
}
