package de.regasus.participant.editor.history;

import java.util.Date;

import com.lambdalogic.messeinfo.email.EmailDispatch;
import com.lambdalogic.messeinfo.email.EmailDispatchOrder;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.util.HtmlHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.history.IHistoryEvent;

public class EmailDispatchEvent implements IHistoryEvent {

	private Date sendDate;
	private String subject;
	private String user;
	private String templateName;

	public EmailDispatchEvent(EmailTemplate emailTemplate, EmailDispatchOrder emailDispatchOrder, EmailDispatch emailDispatch) {
		sendDate = emailDispatch.getSendDate();
		subject = emailDispatch.getSubject();
		user = emailDispatchOrder.getNewDisplayUserStr();
		templateName = emailTemplate.getName();
	}


	@Override
	public String getHtmlDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("<DIV>");
		sb.append(EmailLabel.Subject.getString());
		sb.append(": ");
		sb.append(HtmlHelper.escape(subject));
		sb.append("<BR/>");
		sb.append(UtilI18N.Template);
		sb.append(": ");
		sb.append(HtmlHelper.escape(templateName));
		sb.append("</DIV>");
		return sb.toString();
	}


	@Override
	public Date getTime() {
		return sendDate;
	}


	@Override
	public String getType() {
		return I18N.Email_Dispatched;
	}


	@Override
	public String getUser() {
		return HtmlHelper.escape(user);
	}

}
