package de.regasus.participant.editor.overview;

import java.time.format.FormatStyle;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.lambdalogic.messeinfo.config.parameterset.ParticipantConfigParameterSet;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.common.Country;
import de.regasus.common.Language;
import de.regasus.core.CountryModel;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;

public class PersonSectionContainer
extends AbstractSectionContainer
implements CacheModelListener<Long>, DisposeListener {

	private Long participantID;

	private ParticipantModel participantModel;

	private ParticipantConfigParameterSet partConfigParameterSet;

	private boolean ignoreCacheModelEvents = false;


	public PersonSectionContainer(
		FormToolkit formToolkit,
		Composite body,
		Long participantID,
		ParticipantConfigParameterSet participantConfigParameterSet
	)
	throws Exception {
		super(formToolkit, body);

		this.participantID = participantID;
		this.partConfigParameterSet = participantConfigParameterSet;

		addDisposeListener(this);

		participantModel = ParticipantModel.getInstance();
		participantModel.addListener(this, participantID);

		refreshSection();
	}


	@Override
	protected String getTitle() {
		return ContactLabel.person.getString();
	}


	@Override
	protected void createSectionElements() throws Exception {
		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;

			Participant participant = participantModel.getParticipant(participantID);

			// add entries
			addIfNotEmpty(UtilI18N.Name, participant.getName());

			// function
			if (partConfigParameterSet == null || partConfigParameterSet.getFunction().isVisible()) {
				addIfNotEmpty(Person.FUNCTION.getString(), participant.getFunction());
			}

			// date of birth
			if (partConfigParameterSet == null || partConfigParameterSet.getDateOfBirth().isVisible()) {
				I18NDate dateOfBirth = participant.getDateOfBirth();
				if (dateOfBirth != null) {
					addIfNotEmpty(Person.DATE_OF_BIRTH.getString(), dateOfBirth.format(FormatStyle.SHORT));
				}
			}

			// place of birth
			if (partConfigParameterSet == null || partConfigParameterSet.getPlaceOfBirth().isVisible()) {
				String placeOfBirth = participant.getPlaceOfBirth();
				if (placeOfBirth != null) {
					addIfNotEmpty(Person.PLACE_OF_BIRTH.getString(), placeOfBirth);
				}
			}

			// language
			if (partConfigParameterSet == null || partConfigParameterSet.getLanguage().isVisible()) {
				String languageCode = participant.getLanguageCode();
				if (languageCode != null) {
					Language language = LanguageModel.getInstance().getLanguage(languageCode);
					String languageName = language.getName().getString();
					addIfNotEmpty(Person.LANGUAGE_CODE.getLabel(), languageName);
				}
			}

			// nationality
			if (partConfigParameterSet == null || partConfigParameterSet.getNationality().isVisible()) {
				String countryCode = participant.getNationalityPK();
				if (countryCode != null) {
					Country country = CountryModel.getInstance().getCountry(countryCode);
					String countryName = country.getName().getString();
					addIfNotEmpty(Person.NATIONALITY.getString(), countryName);
				}
			}

			// passportID
			if (partConfigParameterSet == null || partConfigParameterSet.getPassportID().isVisible()) {
				String passportID = participant.getPassportID();
				addIfNotEmpty(Person.PASSPORT_ID.getString(), passportID);
			}

    		// customer number
    		if (partConfigParameterSet == null || partConfigParameterSet.getCustomerNumber().isVisible()) {
    			addIfNotEmpty(Person.CUSTOMER_NO.getString(), participant.getCustomerNo());
    		}

    		// CME number
    		if (partConfigParameterSet == null || partConfigParameterSet.getCMENumber().isVisible()) {
    			addIfNotEmpty(Person.CME_NO.getString(), participant.getCmeNo());
    		}
		}
		finally {
			ignoreCacheModelEvents = false;
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if ( ! ignoreCacheModelEvents) {
				refreshSection();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent event) {
		if (participantModel != null && participantID != null) {
			try {
				participantModel.removeListener(this, participantID);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}

}
