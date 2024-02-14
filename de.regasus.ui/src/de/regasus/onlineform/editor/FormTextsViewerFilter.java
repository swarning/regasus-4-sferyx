package de.regasus.onlineform.editor;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.lambdalogic.messeinfo.regasus.FormTextsEnum;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;

/**
 * This filter controls for which possible form pages, the according custom
 * texts can be edited.
 */
public class FormTextsViewerFilter extends ViewerFilter {

	private RegistrationFormConfig config;

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (config != null && element != null) {
			FormTextsEnum fte = (FormTextsEnum) element;
			switch (fte) {
				case EMAIL_RECOMMENDATION_TEXT:
					return config.isEmailRecommendationEnabled();

				case HOTEL_SELECTION_TEXT:
				case HOTEL_SELECTION_FOOTER_TEXT:
				case HOTEL_TEXT:
				case HOTEL_FOOTER_TEXT:
					return config.isHotelEnabled();

				case TRAVEL_TEXT:
				case TRAVEL_FOOTER_TEXT:
					return config.isTravelEnabled();

				case COMPANION_TEXT:
				case COMPANION_FOOTER_TEXT:
					return config.isCompanionEnabled();

				case COMPANION_BOOKING_TEXT:
				case COMPANION_BOOKING_FOOTER_TEXT:
					return config.isCompanionEnabled() && config.isCompanionBookingsEnabled();

				case HEADER_BOOKING_PAGE:
				case FOOTER_BOOKING_PAGE:
					return config.isBookingEnabled();

				case HEADER_PAYMENT_TEXT:
				case FOOTER_PAYMENT_TEXT:
					return config.isPaymentTypePageEnabled();

				case HEADER_PASSWORD_PROTECTED_FIRST_PAGE:
					return config.isPasswordProtectedFirstPageEnable();

				case SUBSCRIBE_NEWSLETTER_TEXT:
				case UNSUBSCRIBE_NEWSLETTER_TEXT:
					return config.isNewsletterEnabled();
					
				case UPLOAD_FILE_HINT_TEXT:
				case FILE_UPLOADED_HINT_TEXT:
					return config.isUploadFunctionEnabled();

				case AGREEMENT_FOR_SHARED_DATA_TEXT:
					return config.isAgreementForSharedDataEnabled();

				default:
					// All other text blocks can always be shown, they don't depend
					// on a certain configuration flag.
					return true;
			}
		}

		return true;
	}

	public void setConfig(RegistrationFormConfig config) {
		this.config = config;
	}


}
