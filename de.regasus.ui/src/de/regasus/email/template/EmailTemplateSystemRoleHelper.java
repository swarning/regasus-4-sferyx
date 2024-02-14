package de.regasus.email.template;

import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.email.EmailTemplateSystemRole;

import de.regasus.IconRegistry;

public class EmailTemplateSystemRoleHelper {

	private static final String IMAGE_PATH = "icons/";


	private static Image getImage(String fileName) {
		return IconRegistry.getImage(IMAGE_PATH + fileName);
	}


	public static Image getImage(EmailTemplateSystemRole systemRole) {
		switch (systemRole) {
			// System Roles for Event specific Email Templates
			case INVITATION:
				return getImage("information.png");
			case CONFIRMATION:
				return getImage("accept.png");
			case CHANGE_CONFIRMATION:
				return getImage("edit.png");
			case CONFIRM_CANCEL:
				return getImage("exclamation.png");
			case LETTER_OF_INVITATION:
				return getImage("EmailTemplateSystemRole-LetterOfInvitation.png");
			case UPCOMING_STREAM:
				return getImage("stream16.png");
			case CERTIFICATE:
				return getImage("certificate.png");
			case PAYMENT_RECEIVED:
				return getImage("coin.png");
			case REFUND_ISSUED:
				return getImage("coin_refund.png");
			case PAYMENT_LINK:
				return getImage("checkout16.png");

			// System Roles for global (not Event specific) Email Templates
			case PROFILE_CREATED:
				return getImage("profile.png");
			case EMAIL_VERIFICATION:
				return getImage("EmailTemplateSystemRole-EmailVerification.png");
			case FORGOT_PASSWORD:
				return getImage("forgot_password.png");
			case NEWSLETTER_CONFIRM:
				return getImage("newsletter.png");

			default:
				return null;
		}
	}

}
