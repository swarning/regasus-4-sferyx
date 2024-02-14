package de.regasus.onlineform.provider;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.regasus.FormTexts;
import com.lambdalogic.messeinfo.regasus.FormTextsEnum;
import com.lambdalogic.util.HtmlHelper;

import de.regasus.onlineform.OnlineFormI18N;

public class FormTextsTableLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

	private FormTexts formTexts;


	private String language = "de";


	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}


	@Override
	public String getColumnText(Object element, int columnIndex) {
		FormTextsEnum textEnum = (FormTextsEnum) element;

		if (columnIndex == 0) {

			switch (textEnum) {
				case HEADER_FIRST_PAGE:
					return OnlineFormI18N.HeaderFirstPage;
				case HEADER_PASSWORD_PROTECTED_FIRST_PAGE:
					return OnlineFormI18N.HeaderPasswordProtectedFirstPage;
				case FOOTER_FIRST_PAGE:
					return OnlineFormI18N.FooterFirstPage;
				case HEADER_BOOKING_PAGE:
					return OnlineFormI18N.HeaderBookingPage;
				case FOOTER_BOOKING_PAGE:
					return OnlineFormI18N.FooterBookingPage;
				case HEADER_LAST_PAGE:
					return OnlineFormI18N.HeaderLastPage;
				case FOOTER_LAST_PAGE:
					return OnlineFormI18N.FooterLastPage;
				case ALT_BEFORE_REGISTRATION_PERIOD:
					return OnlineFormI18N.AltBeforeRegistrationPeriod;
				case ALT_AFTER_REGISTRATION_PERIOD:
					return OnlineFormI18N.AltAfterRegistrationPeriod;
				case HOTEL_SELECTION_TEXT:
					return OnlineFormI18N.HeaderHotelSelectionPage;
				case HOTEL_SELECTION_FOOTER_TEXT:
					return OnlineFormI18N.FooterHotelSelectionPage;
				case HOTEL_TEXT:
					return OnlineFormI18N.HeaderHotelPage;
				case HOTEL_FOOTER_TEXT:
					return OnlineFormI18N.FooterHotelPage;
				case TRAVEL_TEXT:
					return OnlineFormI18N.HeaderTravelPage;
				case TRAVEL_FOOTER_TEXT:
					return OnlineFormI18N.FooterTravelPage;
				case SUMMARY_TEXT:
					return OnlineFormI18N.HeaderSummaryPage;
				case SUMMARY_FOOTER_TEXT:
					return OnlineFormI18N.FooterSummaryPage;
				case PERSONAL_TEXT:
					return OnlineFormI18N.HeaderPersonalPage;
				case PERSONAL_FOOTER_TEXT:
					return OnlineFormI18N.FooterPersonalPage;
				case ABOVE_DIGITAL_EVENT_BLOCK_TEXT_PERSONAL_PAGE:
					return OnlineFormI18N.AboveDigitalEventButtonPersonalPage;
				case BELOW_DIGITAL_EVENT_BLOCK_TEXT_PERSONAL_PAGE:
					return OnlineFormI18N.BelowDigitalEventButtonPersonalPage;
				case PERSONAL_ABOVE_PERSONAL_DATA_BLOCK_TEXT:
					return OnlineFormI18N.AbovePersonalDataBlockPersonalPage;
				case PERSONAL_BELOW_PERSONAL_DATA_BLOCK_TEXT:
					return OnlineFormI18N.BelowPersonalDataBlockPersonalPage;
				case PERSONAL_ABOVE_ADDRESS_BLOCK_TEXT:
					return OnlineFormI18N.AboveAddressBlockPersonalPage;
				case PERSONAL_BELOW_ADDRESS_BLOCK_TEXT:
					return OnlineFormI18N.BelowAddressBlockPersonalPage;
				case PERSONAL_ABOVE_COMMUNICATION_BLOCK_TEXT:
					return OnlineFormI18N.AboveCommunicationBlockPersonalPage;
				case PERSONAL_BELOW_COMMUNICATION_BLOCK_TEXT:
					return OnlineFormI18N.BelowCommunicationBlockPersonalPage;
				case COMPANION_TEXT:
					return OnlineFormI18N.HeaderCompanionPage;
				case COMPANION_FOOTER_TEXT:
					return OnlineFormI18N.FooterCompanionPage;
				case COMPANION_BOOKING_TEXT:
					return OnlineFormI18N.HeaderCompanionBookingPage;
				case COMPANION_BOOKING_FOOTER_TEXT:
					return OnlineFormI18N.FooterCompanionBookingPage;
				case FOOTER_ALL_PAGES:
					return OnlineFormI18N.FooterAllPages;
				case HEADER_PAYMENT_TEXT:
					return OnlineFormI18N.HeaderPaymentPage;
				case FOOTER_PAYMENT_TEXT:
					return OnlineFormI18N.FooterPaymentPage;
				case EMAIL_RECOMMENDATION_TEXT:
					return OnlineFormI18N.EmailRecommendationTemplate;
				case SUBSCRIBE_NEWSLETTER_TEXT:
					return OnlineFormI18N.SubscribeNewsletter;
				case UNSUBSCRIBE_NEWSLETTER_TEXT:
					return OnlineFormI18N.UnsubscribeNewsletter;
				case UPLOAD_FILE_HINT_TEXT:
					return OnlineFormI18N.UploadFileHint;
				case FILE_UPLOADED_HINT_TEXT:
					return OnlineFormI18N.FileUploadedHint;
				case AGREEMENT_FOR_SHARED_DATA_TEXT:
					return OnlineFormI18N.AgreementForSharedData;
				case HEADER_TEXT_PAYMENT_FORM_FIRST_PAGE:
					return OnlineFormI18N.HeaderTextPaymentFormFirstPage;
				case FOOTER_TEXT_PAYMENT_FORM_FIRST_PAGE:
					return OnlineFormI18N.FooterTextPaymentFormFirstPage;
				case HEADER_TEXT_PAYMENT_FORM:
					return OnlineFormI18N.HeaderTextPaymentForm;
				case FOOTER_TEXT_PAYMENT_FORM:
					return OnlineFormI18N.FooterTextPaymentForm;
				case TEXT_PAYMENT_FORM_END_PAGE:
					return OnlineFormI18N.TextPaymentFormEndPage;
				case NOTHING_TO_PAY_TEXT:
					return OnlineFormI18N.NothingToPayText;
				case ABOVE_DIGITAL_EVENT_BLOCK_TEXT_END_PAGE:
					return OnlineFormI18N.AboveDigitalEventButtonEndPage;
			};
		}
		if (columnIndex == 1) {

			if (formTexts != null) {
				String text = formTexts.get(textEnum, language);
				String value = value(text);
				return value;
			}
		}

		return null;
	}


	private String value(String content) {
		if (content == null) {
			return OnlineFormI18N.NotAvailable;
		}
		else {

			String contentNoTags = content.replaceAll("\\<.*?>", " ");
			String contentNoDoubleSpaces = contentNoTags.replaceAll("\\s+", " ");
			String contentUmlauteReinserted = HtmlHelper.unescape(contentNoDoubleSpaces);

			if (contentUmlauteReinserted.length() > 100) {
				return contentUmlauteReinserted.substring(0, 100) + "...";
			}
			else {
				return contentUmlauteReinserted;
			}
		}
	}


	public FormTexts getFormTexts() {
		return formTexts;
	}


	public void setFormTexts(FormTexts formTexts) {
		this.formTexts = formTexts;
	}


	public String getLanguage() {
		return language;
	}


	public void setLanguage(String language) {
		this.language = language;
	}

}
