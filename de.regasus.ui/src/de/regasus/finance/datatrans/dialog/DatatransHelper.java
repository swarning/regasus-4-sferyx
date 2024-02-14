package de.regasus.finance.datatrans.dialog;

import static de.regasus.LookupService.getPropertyMgr;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.contact.data.CreditCardTypeVO;
import com.lambdalogic.util.NumberHelper;

import de.regasus.common.Property;
import de.regasus.core.CreditCardTypeModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


public class DatatransHelper {

//	public static final String refno = "13";

//	private static final String AUTHORIZATION_URL = "https://pilot.datatrans.biz/upp/jsp/upStart.jsp";
//	private static final String STATUS_REQUEST_URL = "https://pilot.datatrans.biz/upp/jsp/XML_status.jsp";

	/* The request type specifies whether the transaction has
	 * to be immediately settled or authorized only. There
	 * are two request types available:
	 * NOA: authorization only
	 * CAA: authorization with immediate settlement in case
	 * of successful authorization.
	 */
//	private static final String REQUEST_TYPE = "CAA";

	private static final String SUCCESS_KEY = "datatrans.successUrl";
	private static final String ERROR_KEY = "datatrans.errorUrl";
	private static final String CANCEL_KEY = "datatrans.cancelUrl";

	public static final String DEFAULT_SUCCESS_URL = "https://mi2.lambdalogic.de/datatrans/datatrans-success.html";
	public static final String DEFAULT_ERROR_URL = "https://mi2.lambdalogic.de/datatrans/datatrans-error.html";
	public static final String DEFAULT_CANCEL_URL = "https://mi2.lambdalogic.de/datatrans/datatrans-cancel.html";


	private static String successUrl;
	private static String errorUrl;
	private static String cancelUrl;


	static {
		try {
			// get Datatrans-URLs from DB table PROPERTY
			Collection<String> keyList = new ArrayList<>(3);
			keyList.add(SUCCESS_KEY);
			keyList.add(ERROR_KEY);
			keyList.add(CANCEL_KEY);

			List<Property> propertyList = getPropertyMgr().read(keyList);

			for (Property property : propertyList) {
				if (property.getKey().equals(SUCCESS_KEY)) {
					successUrl = property.getValue();
				}
				else if (property.getKey().equals(ERROR_KEY)) {
					errorUrl = property.getValue();
				}
				else if (property.getKey().equals(CANCEL_KEY)) {
					cancelUrl = property.getValue();
				}
			}

			// assure initialization of by default values
			if (successUrl == null) {
				successUrl = DEFAULT_SUCCESS_URL;
			}
			if (errorUrl == null) {
				errorUrl = DEFAULT_ERROR_URL;
			}
			if (cancelUrl == null) {
				cancelUrl = DEFAULT_CANCEL_URL;
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, DatatransHelper.class.getName(), e);
		}
	}


	public static String getSuccessUrl() {
		return successUrl;
	}
	public static String getErrorUrl() {
		return errorUrl;
	}
	public static String getCancelUrl() {
		return cancelUrl;
	}


	public static String amountToString(BigDecimal amount) {
		amount = amount.multiply(NumberHelper.BD_100);
		amount = amount.setScale(0, RoundingMode.HALF_UP);
		return amount.toString();
	}


	public static String creditCardTypePK2paymentMethod(Long creditCardTypePK) throws Exception {
		String datatransPaymentMethod = null;
		if (creditCardTypePK != null) {
			CreditCardTypeVO creditCardTypeVO = CreditCardTypeModel.getInstance().getCreditCardTypeVO(
				creditCardTypePK
			);
			datatransPaymentMethod = creditCardTypeVO.getDatatransPaymentMethod();
		}

		return datatransPaymentMethod;
	}

}
