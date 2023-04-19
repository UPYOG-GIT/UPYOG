package org.egov.collection.service;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.egov.collection.model.FeeDetail;
import org.egov.collection.model.Payment;
import org.egov.collection.model.PaymentDetail;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.DashedBorder;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentReceiptV2 {

	public ByteArrayInputStream getPaymentReceipt(List<Payment> paymentList) throws FileNotFoundException {
		log.info("Inside PaymentReceiptV2.getPaymentReceipt().....");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document document;
		try {
			float threecol = 190f;
			float twocol = 285f;
			float twocol150 = twocol + 150f;
			float twocolumnWidth[] = { twocol, twocol };
			float threeColumnWidth[] = { 90f, twocol + 80F, threecol - 40 };
			float threeColumnWidth1[] = { 90f + twocol + 80F, threecol - 40 };
			float fullwidth[] = { threecol * 3 };

			float logo = 50f;
			float twocolumnWidth1[] = { logo, twocol150 };
			Paragraph space = new Paragraph("\n");

			PdfWriter pdfWriter = new PdfWriter(out);
			PdfDocument pdfDocument = new PdfDocument(pdfWriter);
			pdfDocument.setDefaultPageSize(PageSize.A4);
			document = new Document(pdfDocument);

//			ObjectMapper mapper = new ObjectMapper();
////	        String json = mapper.writeValueAsString(requestInfo);
//			String body = mapper.writeValueAsString(requestInfo);
//			body = "{\"RequestInfo\":" + body + "}";
//
////			String body = "{\r\n" + "    \"RequestInfo\": {\r\n"
////					+ "        \"authToken\": \"ad6bac16-a925-40fa-8a24-32d37e8d0d53\"\r\n" + "    }\r\n" + "}";
////			System.out.println(body);
//			HttpHeaders headers = new HttpHeaders();
//			headers.setContentType(MediaType.APPLICATION_JSON);
//			RestTemplate restTemplate = new RestTemplate();
//			URI uri = new URI("https://www.niwaspass.com/collection-services/payments/_search?tenantId="
//					+ paymentSearchCriteria.getTenantId() + "&consumerCodes=" + paymentSearchCriteria.getConsumerCode()
//					+ "&businessServices=" + paymentSearchCriteria.getBusinessService());
////			URI uri = new URI(
////					"https://www.niwaspass.com/collection-services/payments/_search?tenantId=cg.bhilaicharoda&consumerCodes=CG-BP-2023-04-12-000726&businessServices=BPA.NC_SAN_FEE");
////			System.out.println(uri.toString());
//			HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
//			ResponseEntity<PaymentResponse> result = restTemplate.postForEntity(uri, requestEntity,
//					PaymentResponse.class);
//
//			List<Payment> paymentList = result.getBody().getPayments();
			Payment responsePayment;
			BigDecimal totalAmount = BigDecimal.ZERO;
			BigDecimal grossAmount = BigDecimal.ZERO;
			if (paymentList.size() > 0) {
				log.info("payments.getPaidBy() : " + paymentList.get(0).getPaidBy());
				log.info("payments.getUlbName() : " + paymentList.get(0).getUlbName());
//				log.info(paymentList.get(0));
				String ulbName = paymentList.get(0).getUlbName();
				String imgPath = paymentList.get(0).getLogoUrl();// "C:\\Users\\Entit\\Downloads\\unnamed.png";
				log.info(imgPath);
				ImageData imageData = ImageDataFactory.create(imgPath);
				Image image = new Image(imageData);
				log.info(image.toString());
				image.scaleAbsolute(70, 70);

				Table table = new Table(twocolumnWidth1);
				table.addCell(new Cell().add(image).setBorder(Border.NO_BORDER));
				table.addCell(new Cell().setTextAlignment(TextAlignment.CENTER).add(ulbName.toUpperCase())
						.setFontSize(16f).setBorder(Border.NO_BORDER).setBold());
				Border gb = new SolidBorder(Color.GRAY, 2f);
				Table divider = new Table(fullwidth);
				divider.setBorder(gb);
				document.add(table);

				Table full = new Table(fullwidth);
				full.addCell(new Cell().add("Payment Receipt").setTextAlignment(TextAlignment.CENTER).setBold()
						.setFontSize(16).setBorder(Border.NO_BORDER));
				document.add(full);

				document.add(divider);
				document.add(space);
				DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
				Date date = new Date(paymentList.get(0).getTransactionDate());
				String strDate = dateFormat.format(date);
				Table table2 = new Table(twocolumnWidth);
				table2.addCell(getCell10fLeft1("Receipt No.", true));
				table2.addCell(getCell10fLeft1("Consumer ID", true));
				table2.addCell(
						getCell10fLeft1(paymentList.get(0).getPaymentDetails().get(0).getReceiptNumber(), false));
				table2.addCell(getCell10fLeft1(
						paymentList.get(0).getPaymentDetails().get(0).getBill().getConsumerCode(), false));
				table2.addCell(getCell10fLeft1("Payment Date", true));
				table2.addCell(getCell10fLeft1("Payer Name", true));
				table2.addCell(getCell10fLeft1(strDate, false));
				table2.addCell(getCell10fLeft1(
						paymentList.get(0).getPayerName() != null ? paymentList.get(0).getPayerName() : "", false));
				table2.addCell(getCell10fLeft1("Payer Contact", true));
				table2.addCell(getCell10fLeft1("Payer Status", true));
				table2.addCell(getCell10fLeft1(
						paymentList.get(0).getMobileNumber() != null ? paymentList.get(0).getMobileNumber() : "",
						false));
				table2.addCell(getCell10fLeft1(paymentList.get(0).getPaymentStatus().toString(), false));

				document.add(table2);
				document.add(space);

				grossAmount = paymentList.get(0).getTotalAmountPaid();
				totalAmount = paymentList.get(0).getTotalAmountPaid();
				double totalAmountDouble = totalAmount.doubleValue();
//				int num = (int) totalAmountDouble;
//
//				String amountInwordString = inWords(num);
//				int fractionalAmount = (int) ((totalAmountDouble - num) * 100);
//				amountInwordString += paiseInWords((int) ((totalAmountDouble - num) * 100)) + "only";
				String amountInwordString = inWords(
						Integer.parseInt(String.valueOf(totalAmountDouble).split("\\.")[0]));
				amountInwordString += paiseInWords(Integer.parseInt(String.valueOf(totalAmountDouble).split("\\.")[1]))
						+ "only";

//				System.out.println("amountInwordString:" + amountInwordString);
				Table threeColTable1 = new Table(threeColumnWidth);
				threeColTable1.setBackgroundColor(Color.BLACK, 0.7f);
				threeColTable1.addCell(new Cell().add("S.NO").setBold().setFontColor(Color.WHITE)
						.setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
				threeColTable1.addCell(new Cell().add("Particular").setBold().setFontColor(Color.WHITE)
						.setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
				threeColTable1.addCell(new Cell().add("Amount").setBold().setFontColor(Color.WHITE)
						.setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
				document.add(threeColTable1);

				Table threeColTable2 = new Table(threeColumnWidth);
				responsePayment = paymentList.get(0);
				List<PaymentDetail> paymentDetails = responsePayment.getPaymentDetails();

				List<FeeDetail> feeDetailList = paymentDetails.get(0).getFeeDetail();

				for (FeeDetail feeDetail : feeDetailList) {
					grossAmount.add(feeDetail.getAmount());
					threeColTable2.addCell(
							new Cell().add(String.valueOf(feeDetail.getSrNo())).setTextAlignment(TextAlignment.CENTER));
					threeColTable2.addCell(new Cell().add(feeDetail.getChargesTypeName()))
							.setTextAlignment(TextAlignment.CENTER);
					threeColTable2.addCell(new Cell().add(String.valueOf(feeDetail.getAmount()))
							.setTextAlignment(TextAlignment.RIGHT));
				}
				Table threeColTable31 = new Table(threeColumnWidth1);
				threeColTable31.addCell(new Cell().add("Gross Amount\r").setTextAlignment(TextAlignment.RIGHT));
				threeColTable31
						.addCell(new Cell().add(String.valueOf(grossAmount)).setTextAlignment(TextAlignment.RIGHT));
				threeColTable31.addCell(new Cell().add("Service Tax ( 0% )  ").setTextAlignment(TextAlignment.RIGHT));
				threeColTable31.addCell(new Cell().add("0").setTextAlignment(TextAlignment.RIGHT));
				threeColTable31.addCell(new Cell().add("Education Cess ( 0% )").setTextAlignment(TextAlignment.RIGHT));
				threeColTable31.addCell(new Cell().add("0").setTextAlignment(TextAlignment.RIGHT));
				threeColTable31.addCell(new Cell().add("Total  ").setTextAlignment(TextAlignment.RIGHT));
				threeColTable31
						.addCell(new Cell().add(String.valueOf(totalAmount)).setTextAlignment(TextAlignment.RIGHT));
				document.add(threeColTable2);
				document.add(threeColTable31);
				Table fullTable = new Table(fullwidth);
				fullTable.addCell(new Cell().add("Total Amount(in words) : " + amountInwordString)
						.setTextAlignment(TextAlignment.LEFT));
				fullTable.addCell(new Cell().add("For " + ulbName + "\n\n Authorised Signatory  ")
						.setTextAlignment(TextAlignment.RIGHT));
				fullTable.addCell(new Cell().add("This is a Computer Generated Printout\r\n" + "")
						.setTextAlignment(TextAlignment.CENTER));
				document.add(fullTable);
				document.close();

			} else {
				log.info("Payment Details Not Found....");
				throw new CustomException("PAYMENT_DETAIL_ERROR", "Payment Detail Not Found");
			}
		} catch (Exception e) {
//			e.printStackTrace();
			log.error("PDF Generation Error: " + e);
			throw new CustomException("PDF_GENERATION_ERROR", "Error in PDF Generation");
		}
		return new ByteArrayInputStream(out.toByteArray());
	}

	static Cell getHeaderTextCell(String textValue) {
		return new Cell().add(textValue).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
	}

	static Cell getHeaderTextCellValue(String textValue) {
		return new Cell().add(textValue).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT);
	}

	static Cell getCell10fLeft(String textValue, Boolean isBold) {
		Cell myCell = new Cell().add(textValue).setFontSize(12f).setTextAlignment(TextAlignment.LEFT);
		return isBold ? myCell.setBold() : myCell;
	}

	static Cell getCell10fLeft1(String textValue, Boolean isBold) {
		Cell myCell = new Cell().add(textValue).setFontSize(12f).setTextAlignment(TextAlignment.LEFT);
		return isBold ? myCell.setBold() : myCell;
	}

	static Table fullwidthDashedBorder(float[] fullwidth) {
		Table tableDivider2 = new Table(fullwidth);
		Border dgb = new DashedBorder(Color.GRAY, 0.5f);
		tableDivider2.setBorder(dgb);
		return tableDivider2;
	}

	public static String inWords(int num) {
		final String[] a = { "", "One ", "Two ", "Three ", "Four ", "Five ", "Six ", "Seven ", "Eight ", "Nine ",
				"Ten ", "Eleven ", "Twelve ", "Thirteen ", "Fourteen ", "Fifteen ", "Sixteen ", "Seventeen ",
				"Eighteen ", "Nineteen " };
		final String[] b = { "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety" };
		if (Integer.toString(num).length() > 9) {
			return "overflow";
		}
		String numStr = String.format("%09d", num);
		String[] n = { numStr.substring(0, 2), numStr.substring(2, 4), numStr.substring(4, 6), numStr.substring(6, 7),
				numStr.substring(7) };
		String str = "";

		if (Integer.parseInt(n[0]) != 0) {
			if (Integer.parseInt(n[0]) >= 10) {
				str += b[Integer.parseInt(n[0].substring(0, 1))] + " " + a[Integer.parseInt(n[0].substring(1))]
						+ "Crore ";
			} else {
				str += a[Integer.parseInt(n[0])] + "Crore ";
			}
		}
		if (Integer.parseInt(n[1]) != 0) {
			if (Integer.parseInt(n[1]) >= 10) {
				str += b[Integer.parseInt(n[1].substring(0, 1))] + " " + a[Integer.parseInt(n[1].substring(1))]
						+ "Lakh ";
			} else {
				str += a[Integer.parseInt(n[1])] + "Lakh ";
			}
		}
		if (Integer.parseInt(n[2]) != 0) {
			if (Integer.parseInt(n[2]) >= 10) {
				str += b[Integer.parseInt(n[2].substring(0, 1))] + " " + a[Integer.parseInt(n[2].substring(1))]
						+ "Thousand ";
			} else {
				str += a[Integer.parseInt(n[2])] + "Thousand ";
			}
		}
		if (Integer.parseInt(n[3]) != 0) {
			if (Integer.parseInt(n[3]) >= 10) {
				str += b[Integer.parseInt(n[3].substring(0, 1))] + " " + a[Integer.parseInt(n[3].substring(1))]
						+ "Hundred ";
			} else {
				str += a[Integer.parseInt(n[3])] + "Hundred ";
			}
		}
		if (Integer.parseInt(n[4]) != 0) {
//			if (str != "") {
//				str += "and ";
//			}
			if (Integer.parseInt(n[4]) >= 10) {
				str += b[Integer.parseInt(n[4].substring(0, 1))] + " " + a[Integer.parseInt(n[4].substring(1))]
						+ "Rupees ";
			} else {
				str += a[Integer.parseInt(n[4])] + "Rupees ";
			}
		}

		return str;
	}

	public static String paiseInWords(int num) {
		final String[] a = { "", "One ", "Two ", "Three ", "Four ", "Five ", "Six ", "Seven ", "Eight ", "Nine ",
				"Ten ", "Eleven ", "Twelve ", "Thirteen ", "Fourteen ", "Fifteen ", "Sixteen ", "Seventeen ",
				"Eighteen ", "Nineteen " };
		final String[] b = { "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety" };
		if (Integer.toString(num).length() > 9) {
			return "overflow";
		}
		String numStr = String.format("%02d", num);
		String[] n = { numStr.substring(0, 2) };
		String str = "";

		if (Integer.parseInt(n[0]) != 0) {
			str += "and ";
			if (Integer.parseInt(n[0]) >= 10) {
				str += b[Integer.parseInt(n[0].substring(0, 1))] + " " + a[Integer.parseInt(n[0].substring(1))]
						+ "Paise ";
			} else {
				str += a[Integer.parseInt(n[0])] + "Paise ";
			}
		}

		return str;
	}
	
	public void storeFile(ByteArrayInputStream byteArrayInputStream) {
		
	}

}
