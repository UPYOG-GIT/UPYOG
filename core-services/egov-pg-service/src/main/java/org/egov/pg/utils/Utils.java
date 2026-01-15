package org.egov.pg.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {

    private static final DecimalFormat CURRENCY_FORMATTER_RUPEE = new DecimalFormat("0.00");
    private static final DecimalFormat CURRENCY_FORMATTER_PAISE = new DecimalFormat("0");

    private Utils() {
    };

    public static String formatAmtAsRupee(String txnAmount) {
        return CURRENCY_FORMATTER_RUPEE.format(Double.valueOf(txnAmount));
    }

    public static String formatAmtAsPaise(String txnAmount) {
        return CURRENCY_FORMATTER_PAISE.format(Double.valueOf(txnAmount) * 100);
    }

    public static String convertPaiseToRupee(String paise){
        return new BigDecimal(paise).movePointLeft(2).toPlainString();
    }

	public static String convertObjectToString(ObjectMapper mapper, Map<String, Object> options) throws  Exception {
		// TODO Auto-generated method stub
//		String objct = "";
//		try {
//			 objct = mapper.writeValueAsString(options);
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return objct;
	
		 return mapper.writeValueAsString(options);
	}

}
