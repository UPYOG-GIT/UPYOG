package org.egov.regularisation.service;

import static org.egov.edcr.utility.DcrConstants.FILESTORE_MODULECODE;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.egov.common.entity.edcr.Plan;
import org.egov.infra.config.persistence.datasource.routing.annotation.ReadOnly;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.utils.ApplicationNumberGenerator;
import org.egov.regularisation.entity.RegApplicationType;
import org.egov.regularisation.entity.RegEdcrApplication;
import org.egov.regularisation.entity.RegEdcrApplicationDetail;
import org.egov.regularisation.repository.RegEdcrApplicationDetailRepository;
import org.egov.regularisation.repository.RegEdcrApplicationRepository;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cad.Image;
import com.aspose.cad.imageoptions.PdfOptions;

@Service
@Transactional(readOnly = true)
public class RegEdcrApplicationService {
	private static final String RESUBMIT_SCRTNY = "Resubmit Plan Scrutiny";
	private static final String NEW_SCRTNY = "New Plan Scrutiny";
	private static final String REGULARISATION = "Regularisation";
	public static final String ULB_NAME = "ulbName";
	public static final String ABORTED = "Aborted";
	private static Logger LOG = LogManager.getLogger(RegEdcrApplicationService.class);
	@Autowired
	protected SecurityUtils securityUtils;

	@Autowired
	private RegEdcrApplicationRepository edcrApplicationRepository;

	@Autowired
	private RegEdcrApplicationDetailRepository edcrApplicationDetailRepository;

	@Autowired
	private RegularisationPlanService planService;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private FileStoreService fileStoreService;

	@Autowired
	private ApplicationNumberGenerator applicationNumberGenerator;

//	@Autowired
//	private EdcrIndexService edcrIndexService;

	@Autowired
	private RegEdcrApplicationDetailService edcrApplicationDetailService;

	public Session getCurrentSession() {
		return entityManager.unwrap(Session.class);
	}

	@Transactional
	public RegEdcrApplication create(final RegEdcrApplication regEdcrApplication) {

		// edcrApplication.setApplicationDate(new Date("01/01/2020"));
		regEdcrApplication.setApplicationDate(new Date());
		regEdcrApplication.setApplicationNumber(applicationNumberGenerator.generate());
		regEdcrApplication.setSavedDxfFile(saveDXF(regEdcrApplication));
		regEdcrApplication.setStatus(ABORTED);

		edcrApplicationRepository.save(regEdcrApplication);

//		edcrIndexService.updateIndexes(edcrApplication, NEW_SCRTNY);

		callDcrProcess(regEdcrApplication, NEW_SCRTNY);
//		edcrIndexService.updateIndexes(edcrApplication, NEW_SCRTNY);

		return regEdcrApplication;
	}

	@Transactional
	public RegEdcrApplication update(final RegEdcrApplication regEdcrApplication) {
		regEdcrApplication.setSavedDxfFile(saveDXF(regEdcrApplication));
		regEdcrApplication.setStatus(ABORTED);
		Plan unsavedPlanDetail = regEdcrApplication.getEdcrApplicationDetails().get(0).getPlan();
		RegEdcrApplication applicationRes = edcrApplicationRepository.save(regEdcrApplication);
		regEdcrApplication.getEdcrApplicationDetails().get(0).setPlan(unsavedPlanDetail);

//		edcrIndexService.updateIndexes(edcrApplication, RESUBMIT_SCRTNY);

		callDcrProcess(regEdcrApplication, RESUBMIT_SCRTNY);

		return applicationRes;
	}

	private Plan callDcrProcess(RegEdcrApplication regEdcrApplication, String applicationType) {
		Plan planDetail = new Plan();
		planDetail = planService.process(regEdcrApplication, applicationType);
		updateFile(planDetail, regEdcrApplication);
		edcrApplicationDetailService.saveAll(regEdcrApplication.getEdcrApplicationDetails());

		return planDetail;
	}

	private Plan callRegularisationDcrProcess(RegEdcrApplication regEdcrApplication, String applicationType) {
		Plan planDetail = new Plan();
		planDetail = planService.process(regEdcrApplication, applicationType);
		updateFile(planDetail, regEdcrApplication);
		edcrApplicationDetailService.saveAll(regEdcrApplication.getEdcrApplicationDetails());

		return planDetail;
	}

	private File saveDXF(RegEdcrApplication regEdcrApplication) {
		FileStoreMapper fileStoreMapper = addToFileStore(regEdcrApplication.getDxfFile());
		File dxfFile = fileStoreService.fetch(fileStoreMapper.getFileStoreId(), FILESTORE_MODULECODE);
		planService.buildDocuments(regEdcrApplication, fileStoreMapper, null, null);
		List<RegEdcrApplicationDetail> regEdcrApplicationDetails = regEdcrApplication.getEdcrApplicationDetails();
		regEdcrApplicationDetails.get(0).setStatus(ABORTED);
		regEdcrApplication.setEdcrApplicationDetails(regEdcrApplicationDetails);
		return dxfFile;

	}

	public File savePlanDXF(final MultipartFile file) {
		FileStoreMapper fileStoreMapper = addToFileStore(file);
		return fileStoreService.fetch(fileStoreMapper.getFileStoreId(), FILESTORE_MODULECODE);
	}

	private FileStoreMapper addToFileStore(final MultipartFile file) {
		FileStoreMapper fileStoreMapper = null;
		try {
			fileStoreMapper = fileStoreService.store(file.getInputStream(), file.getOriginalFilename(),
					file.getContentType(), FILESTORE_MODULECODE);
		} catch (final IOException e) {
			LOG.error("Error occurred, while getting input stream!!!!!", e);
		}
		return fileStoreMapper;
	}

	public List<RegEdcrApplication> findAll() {
		return edcrApplicationRepository.findAll(new Sort(Sort.Direction.ASC, "name"));
	}

	public RegEdcrApplication findOne(Long id) {
		return edcrApplicationRepository.findOne(id);
	}

	public RegEdcrApplication findByApplicationNo(String appNo) {
		return edcrApplicationRepository.findByApplicationNumber(appNo);
	}

//	public RegEdcrApplication findByApplicationNoAndType(String applnNo, RegApplicationType type) {
//		return edcrApplicationRepository.findByApplicationNumberAndApplicationType(applnNo, type);
//	}

	public RegEdcrApplication findByPlanPermitNumber(String permitNo) {
		return edcrApplicationRepository.findByPlanPermitNumber(permitNo);
	}

	public RegEdcrApplication findByTransactionNumber(String transactionNo) {
		return edcrApplicationRepository.findByTransactionNumber(transactionNo);
	}

	public RegEdcrApplication findByTransactionNumberAndTPUserCode(String transactionNo, String userCode) {
		return edcrApplicationRepository.findByTransactionNumberAndThirdPartyUserCode(transactionNo, userCode);
	}

	public List<RegEdcrApplication> search(RegEdcrApplication regEdcrApplication) {
		return edcrApplicationRepository.findAll();
	}

	public List<RegEdcrApplication> findByThirdPartyUserCode(String userCode) {
		return edcrApplicationRepository.findByThirdPartyUserCode(userCode);
	}

	public List<RegEdcrApplication> getEdcrApplications() {
		Pageable pageable = new PageRequest(0, 25, Sort.Direction.DESC, "id");
		Page<RegEdcrApplication> regEdcrApplications = edcrApplicationRepository.findAll(pageable);
		return regEdcrApplications.getContent();
	}

//	@ReadOnly
//	public Page<SearchBuildingPlanScrutinyForm> planScrutinyPagedSearch(SearchBuildingPlanScrutinyForm searchRequest) {
//		final Pageable pageable = new PageRequest(searchRequest.pageNumber(), searchRequest.pageSize(),
//				searchRequest.orderDir(), searchRequest.orderBy());
//		List<SearchBuildingPlanScrutinyForm> searchResults = new ArrayList<>();
//		Page<EdcrApplicationDetail> dcrApplications = edcrApplicationDetailRepository
//				.findAll(DcrReportSearchSpec.searchReportsSpecification(searchRequest), pageable);
//		for (EdcrApplicationDetail applicationDetail : dcrApplications)
//			searchResults.add(buildResponseAsPerForm(applicationDetail));
//		return new PageImpl<>(searchResults, pageable, dcrApplications.getTotalElements());
//	}

//	private SearchBuildingPlanScrutinyForm buildResponseAsPerForm(EdcrApplicationDetail applicationDetail) {
//		SearchBuildingPlanScrutinyForm planScrtnyFrm = new SearchBuildingPlanScrutinyForm();
//		EdcrApplication application = applicationDetail.getApplication();
//		planScrtnyFrm.setApplicationNumber(application.getApplicationNumber());
//		planScrtnyFrm.setApplicationDate(application.getApplicationDate());
//		planScrtnyFrm.setApplicantName(application.getApplicantName());
//		planScrtnyFrm.setBuildingPlanScrutinyNumber(applicationDetail.getDcrNumber());
//		planScrtnyFrm.setUploadedDateAndTime(applicationDetail.getCreatedDate());
//		if (applicationDetail.getDxfFileId() != null)
//			planScrtnyFrm.setDxfFileStoreId(applicationDetail.getDxfFileId().getFileStoreId());
//		if (applicationDetail.getDxfFileId() != null)
//			planScrtnyFrm.setDxfFileName(applicationDetail.getDxfFileId().getFileName());
//		if (applicationDetail.getReportOutputId() != null)
//			planScrtnyFrm.setReportOutputFileStoreId(applicationDetail.getReportOutputId().getFileStoreId());
//		if (applicationDetail.getReportOutputId() != null)
//			planScrtnyFrm.setReportOutputFileName(applicationDetail.getReportOutputId().getFileName());
//		planScrtnyFrm.setStakeHolderId(application.getCreatedBy().getId());
//		planScrtnyFrm.setStatus(applicationDetail.getStatus());
//		planScrtnyFrm.setBuildingLicenceeName(application.getCreatedBy().getName());
//		return planScrtnyFrm;
//	}

	private static String readFile(File srcFile) {
		String fileAsString = null;
		try {
			String canonicalPath = srcFile.getCanonicalPath();
			if (!canonicalPath.equals(srcFile.getPath()))
				throw new FileNotFoundException("Invalid file path, please try again.");
		} catch (IOException e) {
			LOG.error("Invalid file path, please try again.", e);
		}
		try (InputStream is = new FileInputStream(srcFile);
				BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			String line = br.readLine();
			StringBuilder sb = new StringBuilder();
			while (line != null) {
				sb.append(line).append("\n");
				line = br.readLine();
			}
			fileAsString = sb.toString();
		} catch (IOException e) {
			LOG.error("Error occurred when reading file!!!!!", e);
		}
		return fileAsString;
	}

	private void updateFile(Plan pl, RegEdcrApplication regEdcrApplication) {
//		String readFile = readFile(edcrApplication.getSavedDxfFile());
		String filePath = regEdcrApplication.getSavedDxfFile().getAbsolutePath();
//		String replace = readFile.replace("ENTITIES", "ENTITIES\n0\n" + pl.getAdditionsToDxf());
//        String newFile = edcrApplication.getDxfFile().getOriginalFilename().replace(".dxf", "_system_scrutinized.dxf");
		String newFile = regEdcrApplication.getDxfFile().getOriginalFilename().replace(".dxf",
				"_system_scrutinized.pdf");
		// Load the source CAD file
		Image objImage = Image.load(filePath);

		// Create an instance of PdfOptions
		PdfOptions pdfOptions = new PdfOptions();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		// Export CAD to PDF
//     		objImage.save("dwg-to-pdf.pdf", pdfOptions);
		objImage.save(outputStream, pdfOptions);

		byte[] pdfBytes = outputStream.toByteArray();

		try (PDDocument document = PDDocument.load(pdfBytes)) {
			byte[] modifiedPdfBytes;
			if (!regEdcrApplication.getStatus().equalsIgnoreCase("Not Accepted")) {
				// Get the first page of the PDF (assuming there's only one page)
				PDPage page = document.getPage(0);

				// Create a new content stream to add the watermark
				PDPageContentStream contentStream = new PDPageContentStream(document, page,
						PDPageContentStream.AppendMode.APPEND, true, true);

				PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
				graphicsState.setNonStrokingAlphaConstant(0.5f);
				graphicsState.setAlphaSourceFlag(true);
				// Set the opacity (0.5f for semi-transparent)
				contentStream.setGraphicsStateParameters(graphicsState);
				InputStream imageStream = RegEdcrApplication.class.getResourceAsStream("/watermark.png");
				java.awt.image.BufferedImage image1 = ImageIO.read(imageStream);
				// Load the watermark image (replace "watermark.png" with the path to your
				// watermark image)
				PDImageXObject image = LosslessFactory.createFromImage(document, image1);
				float xPos = 0f;
				float yPos = 0f;
				// Draw the watermark image on the page
				contentStream.drawImage(image, xPos, yPos, page.getMediaBox().getWidth(),
						page.getMediaBox().getHeight());

				// Close the content stream
				contentStream.close();

				// Save the modified PDF
				ByteArrayOutputStream modifiedPdfStream = new ByteArrayOutputStream();
				document.save(modifiedPdfStream);
				document.close();

				// Convert the modified PDF to a byte array
				modifiedPdfBytes = modifiedPdfStream.toByteArray();
			} else {
				modifiedPdfBytes = outputStream.toByteArray();
			}
			File f = new File(newFile);
			try (FileOutputStream fos = new FileOutputStream(f)) {
				if (!f.exists())
					f.createNewFile();
//            fos.write(replace.getBytes());
				fos.write(modifiedPdfBytes);
				fos.flush();
				FileStoreMapper fileStoreMapper = fileStoreService.store(f, f.getName(),
						regEdcrApplication.getDxfFile().getContentType(), FILESTORE_MODULECODE);
				regEdcrApplication.getEdcrApplicationDetails().get(0).setScrutinizedDxfFileId(fileStoreMapper);
			} catch (IOException e) {
				LOG.error("Error occurred when reading file!!!!!", e);
			}
		} catch (IOException e) {
			LOG.error("Error occurred when processing PDF!!!!!", e);
		}
	}

	@Transactional
	public RegEdcrApplication createRestEdcr(final RegEdcrApplication regEdcrApplication) {
		String comparisonDcrNo = regEdcrApplication.getEdcrApplicationDetails().get(0).getComparisonDcrNumber();
		if (regEdcrApplication.getApplicationDate() == null)
			regEdcrApplication.setApplicationDate(new Date());
		regEdcrApplication.setApplicationNumber(applicationNumberGenerator.generate());
		regEdcrApplication.setSavedDxfFile(saveDXF(regEdcrApplication));
		regEdcrApplication.setStatus(ABORTED);
		edcrApplicationRepository.save(regEdcrApplication);
		regEdcrApplication.getEdcrApplicationDetails().get(0).setComparisonDcrNumber(comparisonDcrNo);
		if (regEdcrApplication.getApplicationType().toString()
				.equalsIgnoreCase(RegApplicationType.REGULARISATION.toString()))
			callRegularisationDcrProcess(regEdcrApplication, REGULARISATION);
		else
			callDcrProcess(regEdcrApplication, NEW_SCRTNY);
//		edcrIndexService.updateEdcrRestIndexes(edcrApplication, NEW_SCRTNY);

		return regEdcrApplication;
	}
}