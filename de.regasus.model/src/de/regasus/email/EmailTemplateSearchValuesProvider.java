package de.regasus.email;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateComparator;
import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;
import com.lambdalogic.util.MapHelper;

public class EmailTemplateSearchValuesProvider implements SearchValuesProvider {

	private Long eventPK;


	public EmailTemplateSearchValuesProvider(Long eventPK) {
		this.eventPK = eventPK;
	}


	@Override
	public LinkedHashMap getValues() throws Exception {
		EmailTemplateModel emailTemplateModel = EmailTemplateModel.getInstance();

		List<EmailTemplate> emailTemplateList = emailTemplateModel.getEmailTemplateSearchDataByEvent(null);

		emailTemplateList = new ArrayList<>(emailTemplateList);

		if (eventPK != null) {
			List<EmailTemplate> eventEmailTemplateList = emailTemplateModel.getEmailTemplateSearchDataByEvent(eventPK);
			emailTemplateList.addAll(eventEmailTemplateList);
		}

		Collections.sort(emailTemplateList, EmailTemplateComparator.getInstance());

		LinkedHashMap<Long, String> values = MapHelper.createLinkedHashMap( emailTemplateList.size() );

        for (EmailTemplate emailTemplateSearchData : emailTemplateList) {
        	/* The Key must be of type Long because this class is a PKSQLField.
        	 * In PKSQLField.getSQLParameter() the parameter value will be converted to a Long.
        	 * If the values have not the same type (Long) they are not equal!
        	 */
            values.put(emailTemplateSearchData.getID(), emailTemplateSearchData.getName());
        }

		return values;
	}


	public Long getEventPK() {
		return eventPK;
	}


	public void setEventPK(Long eventPK) {
		this.eventPK = eventPK;
	}

}
