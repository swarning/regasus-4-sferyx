/**
 * PersonSectionContainer.java
 * created on 05.08.2013 12:42:29
 */
package de.regasus.profile.editor.overview;

import java.time.format.FormatStyle;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.lambdalogic.messeinfo.config.parameterset.ProfileConfigParameterSet;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.profile.Profile;
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
import de.regasus.profile.ProfileModel;
import de.regasus.ui.Activator;

public class PersonSectionContainer
extends AbstractSectionContainer
implements CacheModelListener<Long>, DisposeListener {

	private Long profileID;

	private ProfileModel profileModel;

	private ProfileConfigParameterSet profileConfigParameterSet;

	private boolean ignoreCacheModelEvents = false;


	public PersonSectionContainer(
		FormToolkit formToolkit,
		Composite body,
		Long profileID,
		ProfileConfigParameterSet profileConfigParameterSet
	)
	throws Exception {
		super(formToolkit, body);

		this.profileID = profileID;
		this.profileConfigParameterSet = profileConfigParameterSet;

		addDisposeListener(this);

		profileModel = ProfileModel.getInstance();
		profileModel.addListener(this, profileID);

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


			Profile profile = profileModel.getProfile(profileID);

			// add entries
			addIfNotEmpty(UtilI18N.Name, profile.getName());

			// date of birth
			if (profileConfigParameterSet == null || profileConfigParameterSet.getDateOfBirth().isVisible()) {
				I18NDate dateOfBirth = profile.getDateOfBirth();
				if (dateOfBirth != null) {
					addIfNotEmpty(Person.DATE_OF_BIRTH.getString(), dateOfBirth.format(FormatStyle.SHORT));
				}
			}

			// place of birth
			if (profileConfigParameterSet == null || profileConfigParameterSet.getPlaceOfBirth().isVisible()) {
				String placeOfBirth = profile.getPlaceOfBirth();
				if (placeOfBirth != null) {
					addIfNotEmpty(Person.PLACE_OF_BIRTH.getString(), placeOfBirth);
				}
			}

			// function
			if (profileConfigParameterSet == null || profileConfigParameterSet.getFunction().isVisible()) {
				addIfNotEmpty(Person.FUNCTION.getString(), profile.getFunction());
			}

			// language
			if (profileConfigParameterSet == null || profileConfigParameterSet.getLanguage().isVisible()) {
				String languageId = profile.getLanguageCode();
				if (languageId != null) {
					Language language = LanguageModel.getInstance().getLanguage(languageId);
					String languageName = language.getName().getString();
					addIfNotEmpty(Person.LANGUAGE_CODE.getLabel(), languageName);
				}
			}

			// nationality
			if (profileConfigParameterSet == null || profileConfigParameterSet.getNationality().isVisible()) {
				String countryCode = profile.getNationalityPK();
				if (countryCode != null) {
					Country country = CountryModel.getInstance().getCountry(countryCode);
					String countryName = country.getName().getString();
					addIfNotEmpty(Person.NATIONALITY.getString(), countryName);
				}
			}

			// passportID
			if (profileConfigParameterSet == null || profileConfigParameterSet.getPassportID().isVisible()) {
				String passportID = profile.getPassportID();
				addIfNotEmpty(Person.PASSPORT_ID.getString(), passportID);
			}

    		// customer number
    		if (profileConfigParameterSet == null || profileConfigParameterSet.getCustomerNumber().isVisible()) {
    			addIfNotEmpty(Person.CUSTOMER_NO.getString(), profile.getCustomerNo());
    		}

    		// CME number
    		if (profileConfigParameterSet == null || profileConfigParameterSet.getCMENumber().isVisible()) {
    			addIfNotEmpty(Person.CME_NO.getString(), profile.getCmeNo());
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
		if (profileModel != null && profileID != null) {
			try {
				profileModel.removeListener(this, profileID);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}

}
