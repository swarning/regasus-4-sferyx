package de.regasus.onlineform.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.config.parameterset.AddressConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.CommunicationConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.FieldConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ParticipantConfigParameterSet;
import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.Communication;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.regasus.Field;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.onlineform.OnlineFormI18N;
import de.regasus.ui.Activator;

public class DataFieldsTabComposite extends Composite {

	// the entity
	private RegistrationFormConfig registrationFormConfig;

	private ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private FieldConfigComposite genderFCC;
	private FieldConfigComposite firstNameFCC;
	private FieldConfigComposite lastNameFCC;
	private FieldConfigComposite functionFCC;
	private FieldConfigComposite degreeFCC;
	private FieldConfigComposite birthdayFCC;
	private FieldConfigComposite placeOfBirthFCC;
	private FieldConfigComposite nationalityFCC;
	private FieldConfigComposite cmeFCC;
	private FieldConfigComposite mandateFCC;
	private FieldConfigComposite nobilityFCC;
	private FieldConfigComposite nobilityPrefixFCC;
	private FieldConfigComposite adminTitleFCC;
	private FieldConfigComposite customerNoFCC;
	private FieldConfigComposite taxIdFCC;

	// 8 fields for address attributes
	private FieldConfigComposite organisationFCC;
	private FieldConfigComposite departmentFCC;
	private FieldConfigComposite addresseeFCC;
	private FieldConfigComposite streetFCC;
	private FieldConfigComposite cityFCC;
	private FieldConfigComposite zipFCC;
	private FieldConfigComposite stateFCC;
	private FieldConfigComposite countryFCC;


	// Alternative Address
	private FieldConfigComposite altOrganisationFCC;
	private FieldConfigComposite altDepartmentFCC;
	private FieldConfigComposite altAddresseeFCC;
	private FieldConfigComposite altStreetFCC;
	private FieldConfigComposite altCityFCC;
	private FieldConfigComposite altZipFCC;
	private FieldConfigComposite altStateFCC;
	private FieldConfigComposite altCountryFCC;

	// 6 fields for communication attributes
	private FieldConfigComposite emailFCC;
	private FieldConfigComposite phone1FCC;
	private FieldConfigComposite phone2FCC;
	private FieldConfigComposite mobile1FCC;
	private FieldConfigComposite mobile2FCC;
	private FieldConfigComposite faxFCC;


	private FieldConfigComposite companionGenderFCC;
	private FieldConfigComposite companionFirstNameFCC;
	private FieldConfigComposite companionLastNameFCC;
	private FieldConfigComposite companionCityFCC;
	private FieldConfigComposite companionNationalityFCC;
	private FieldConfigComposite companionDegreeFCC;
	private FieldConfigComposite companionEmailFCC;
	private FieldConfigComposite companionFunctionFCC;
	private FieldConfigComposite companionMandateFCC;
	private FieldConfigComposite companionNobilityFCC;
	private FieldConfigComposite companionNobilityPrefixFCC;
	private FieldConfigComposite companionAdminTitleFCC;


	private ParticipantConfigParameterSet cps;

	private Button lastNameReadOnlyButton;

	public DataFieldsTabComposite(Composite parent, int style, Long eventPK) {
		super(parent, style);

		try {
			cps = ConfigParameterSetModel.getInstance().getConfigParameterSet(eventPK).getEvent().getParticipant();
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			cps = new ParticipantConfigParameterSet(new ConfigParameterSet());
		}

		setLayout(new GridLayout(3, false));


		// ==================================================

		Group personalGroup = new Group(this, SWT.NONE);
		personalGroup.setText(OnlineFormI18N.PersonalInformation);
		personalGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		personalGroup.setLayout(new GridLayout(2, false));

		genderFCC = createFieldConfigComposite(personalGroup, Person.GENDER, cps.getGender());
		degreeFCC = createFieldConfigComposite(personalGroup, Person.DEGREE, cps.getDegree());
		nobilityFCC = createFieldConfigComposite(personalGroup, Person.NOBILITY, cps.getNobility());
		adminTitleFCC = createFieldConfigComposite(personalGroup, Person.ADMIN_TITLE, cps.getAdminTitle());
		firstNameFCC = createFieldConfigComposite(personalGroup, Person.FIRST_NAME, cps.getFirstName());
		nobilityPrefixFCC = createFieldConfigComposite(personalGroup, Person.NOBILITY_PREFIX, cps.getNobilityPrefix());
		lastNameFCC = createFieldConfigComposite(personalGroup, Person.LAST_NAME);
		lastNameFCC.setRequiredAndNotEditable();

		new Label(personalGroup, SWT.NONE);
		lastNameReadOnlyButton = new Button(personalGroup, SWT.CHECK);
		lastNameReadOnlyButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		lastNameReadOnlyButton.setText(OnlineFormI18N.ReadOnly);

		mandateFCC = createFieldConfigComposite(personalGroup, Person.MANDATE, cps.getMandate());
		functionFCC = createFieldConfigComposite(personalGroup, Person.FUNCTION, cps.getFunction());

		birthdayFCC = createFieldConfigComposite(personalGroup, Person.DATE_OF_BIRTH, cps.getDateOfBirth());
		placeOfBirthFCC = createFieldConfigComposite(personalGroup, Person.PLACE_OF_BIRTH, cps.getPlaceOfBirth());
		nationalityFCC = createFieldConfigComposite(personalGroup, Person.NATIONALITY, cps.getNationality());
		cmeFCC = createFieldConfigComposite(personalGroup, Person.CME_NO, cps.getCMENumber());
		customerNoFCC = createFieldConfigComposite(personalGroup, Person.CUSTOMER_NO,  cps.getCustomerNumber());
		taxIdFCC = createFieldConfigComposite(personalGroup, Person.TAX_ID,  cps.getTaxId());

		//
		// **************************************************************************

		Group addressGroup = new Group(this, SWT.NONE);
		addressGroup.setLayout(new GridLayout(2, false));
		addressGroup.setText(OnlineFormI18N.Address);
		addressGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		AddressConfigParameterSet aps = cps.getAddress();
		organisationFCC = createFieldConfigComposite(addressGroup, Address.ORGANISATION, aps.getOrganisation());
		departmentFCC = createFieldConfigComposite(addressGroup, Address.DEPARTMENT, aps.getDepartment());
		addresseeFCC = createFieldConfigComposite(addressGroup, Address.ADDRESSEE, aps.getAddressee());
		streetFCC = createFieldConfigComposite(addressGroup, Address.STREET);
		zipFCC = createFieldConfigComposite(addressGroup, Address.ZIP);
		cityFCC = createFieldConfigComposite(addressGroup, Address.CITY);
		stateFCC = createFieldConfigComposite(addressGroup, Address.STATE);
		countryFCC = createFieldConfigComposite(addressGroup, Address.COUNTRY);

		// **************************************************************************

		Group communicationGroup = new Group(this, SWT.NONE);
		communicationGroup.setLayout(new GridLayout(2, false));
		communicationGroup.setText(OnlineFormI18N.Communication);
		communicationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		emailFCC = createFieldConfigComposite(communicationGroup, ContactLabel.email);

		CommunicationConfigParameterSet ccps = cps.getCommunication();
		phone1FCC = createFieldConfigComposite(communicationGroup, Communication.PHONE1, ccps.getPhone1());
		phone2FCC = createFieldConfigComposite(communicationGroup, Communication.PHONE2, ccps.getPhone2());
		mobile1FCC = createFieldConfigComposite(communicationGroup, Communication.MOBILE1, ccps.getMobile1());
		mobile2FCC = createFieldConfigComposite(communicationGroup, Communication.MOBILE2, ccps.getMobile2());
		faxFCC = createFieldConfigComposite(communicationGroup, Communication.FAX1, ccps.getFax1());

		// **************************************************************************

		Group altAddressGroup = new Group(this, SWT.NONE);
		altAddressGroup.setLayout(new GridLayout(2, false));
		altAddressGroup.setText(OnlineFormI18N.AltAddress);
		altAddressGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		altOrganisationFCC = createFieldConfigComposite(altAddressGroup, Address.ORGANISATION, aps.getOrganisation());
		altDepartmentFCC = createFieldConfigComposite(altAddressGroup, Address.DEPARTMENT, aps.getDepartment());
		altAddresseeFCC = createFieldConfigComposite(altAddressGroup, Address.ADDRESSEE, aps.getAddressee());
		altStreetFCC = createFieldConfigComposite(altAddressGroup, Address.STREET);
		altZipFCC = createFieldConfigComposite(altAddressGroup, Address.ZIP);
		altCityFCC = createFieldConfigComposite(altAddressGroup, Address.CITY);
		altStateFCC = createFieldConfigComposite(altAddressGroup, Address.STATE);
		altCountryFCC = createFieldConfigComposite(altAddressGroup, Address.COUNTRY);


		// **************************************************************************

		Group companionGroup = new Group(this, SWT.NONE);
		companionGroup.setLayout(new GridLayout(2, false));
		companionGroup.setText(OnlineFormI18N.Companion);
		companionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		companionGenderFCC = createFieldConfigComposite(companionGroup, Person.GENDER, cps.getGender());
		companionDegreeFCC = createFieldConfigComposite(companionGroup, Person.DEGREE, cps.getDegree());
		companionNobilityFCC = createFieldConfigComposite(companionGroup, Person.NOBILITY, cps.getNobility());
		companionAdminTitleFCC = createFieldConfigComposite(companionGroup, Person.ADMIN_TITLE, cps.getAdminTitle());

		companionFirstNameFCC = createFieldConfigComposite(companionGroup, Person.FIRST_NAME, cps.getFirstName());
		companionNobilityPrefixFCC = createFieldConfigComposite(companionGroup, Person.NOBILITY_PREFIX, cps.getNobilityPrefix());
		companionLastNameFCC = createFieldConfigComposite(companionGroup, Person.LAST_NAME);
		companionLastNameFCC.setRequiredAndNotEditable();
		companionMandateFCC = createFieldConfigComposite(companionGroup, Person.MANDATE, cps.getMandate());
		companionFunctionFCC = createFieldConfigComposite(companionGroup, Person.FUNCTION, cps.getFunction());

		companionCityFCC = createFieldConfigComposite(companionGroup, Address.CITY);
		companionNationalityFCC = createFieldConfigComposite(companionGroup, Person.NATIONALITY);
		companionEmailFCC = createFieldConfigComposite(companionGroup, ContactLabel.email);


		// ==================================================

		lastNameReadOnlyButton.addSelectionListener(modifySupport);
	}



	private FieldConfigComposite createFieldConfigComposite(Composite composite, I18NString i18nString, FieldConfigParameterSet fcps) {
		if (fcps.isVisible()) {
			return createFieldConfigComposite(composite, i18nString);
		}
		else {
			return null;
		}
	}

	private FieldConfigComposite createFieldConfigComposite(Composite composite, I18NString i18nString) {
		Label label = new Label(composite, SWT.RIGHT);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		label.setText(i18nString.getString());
		FieldConfigComposite fieldConfigComposite = new FieldConfigComposite(composite, SWT.NONE);
		fieldConfigComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		fieldConfigComposite.addSelectionListener(modifySupport);

		return fieldConfigComposite;
	}


	void syncWidgetsToEntity() {
		syncWidgetsToEntityInternal(registrationFormConfig, true);
	}


	private void syncWidgetsToEntityInternal(
		final RegistrationFormConfig registrationFormConfig,
		final boolean avoidEvents) {
		if (registrationFormConfig != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						if (avoidEvents) {
							modifySupport.setEnabled(false);
						}

						setField(genderFCC, registrationFormConfig.getFieldParticipantGender());
						setField(firstNameFCC, registrationFormConfig.getFieldParticipantFirstName());
						setField(lastNameFCC, registrationFormConfig.getFieldParticipantLastName());
						setField(degreeFCC, registrationFormConfig.getFieldParticipantDegree());
						setField(functionFCC, registrationFormConfig.getFieldParticipantFunction());
						setField(birthdayFCC, registrationFormConfig.getFieldParticipantBirthDay());
						setField(placeOfBirthFCC, registrationFormConfig.getFieldParticipantPlaceOfBirth());
						setField(nationalityFCC, registrationFormConfig.getFieldParticipantNationality());
						setField(cmeFCC, registrationFormConfig.getFieldParticipantCME());
						setField(mandateFCC, registrationFormConfig.getFieldParticipantMandate());
						setField(nobilityFCC, registrationFormConfig.getFieldParticipantNobility());
						setField(nobilityPrefixFCC, registrationFormConfig.getFieldParticipantNobilityPrefix());
						setField(adminTitleFCC, registrationFormConfig.getFieldParticipantAdminTitle());
						setField(customerNoFCC, registrationFormConfig.getFieldParticipantCustomerNo());
						setField(taxIdFCC, registrationFormConfig.getFieldParticipantTaxId());

						// 8 address attributes
						setField(organisationFCC, registrationFormConfig.getFieldParticipantOrganisation());
						setField(departmentFCC, registrationFormConfig.getFieldParticipantDepartment());
						setField(addresseeFCC, registrationFormConfig.getFieldParticipantAddressee());
						setField(streetFCC, registrationFormConfig.getFieldParticipantStreet());
						setField(cityFCC, registrationFormConfig.getFieldParticipantCity());
						setField(zipFCC, registrationFormConfig.getFieldParticipantZip());
						setField(stateFCC, registrationFormConfig.getFieldParticipantState());
						setField(countryFCC, registrationFormConfig.getFieldParticipantCountry());

						// 8 alternative address attributes
						setField(altOrganisationFCC, registrationFormConfig.getFieldParticipantAltOrganisation());
						setField(altDepartmentFCC, registrationFormConfig.getFieldParticipantAltDepartment());
						setField(altAddresseeFCC, registrationFormConfig.getFieldParticipantAltAddressee());
						setField(altStreetFCC, registrationFormConfig.getFieldParticipantAltStreet());
						setField(altCityFCC, registrationFormConfig.getFieldParticipantAltCity());
						setField(altZipFCC, registrationFormConfig.getFieldParticipantAltZip());
						setField(altStateFCC, registrationFormConfig.getFieldParticipantAltState());
						setField(altCountryFCC, registrationFormConfig.getFieldParticipantAltCountry());

						// 6 communication attributes
						setField(emailFCC, registrationFormConfig.getFieldParticipantEmail());
						setField(phone1FCC, registrationFormConfig.getFieldParticipantPhone1());
						setField(phone2FCC, registrationFormConfig.getFieldParticipantPhone2());
						setField(mobile1FCC, registrationFormConfig.getFieldParticipantMobile1());
						setField(mobile2FCC, registrationFormConfig.getFieldParticipantMobile2());
						setField(faxFCC, registrationFormConfig.getFieldParticipantFax());
						if (emailFCC != null) {
							if (!registrationFormConfig.isNoEmailDispatch()) {
								emailFCC.setRequiredAndNotEditable();
							}
							else {
								emailFCC.setEditable();
							}
						}


						setField(companionGenderFCC, registrationFormConfig.getFieldCompanionGender());
						setField(companionFirstNameFCC, registrationFormConfig.getFieldCompanionFirstName());
						setField(companionLastNameFCC, registrationFormConfig.getFieldCompanionLastName());
						setField(companionCityFCC, registrationFormConfig.getFieldCompanionCity());
						setField(companionNationalityFCC, registrationFormConfig.getFieldCompanionNationality());
						setField(companionDegreeFCC, registrationFormConfig.getFieldCompanionDegree());
						setField(companionEmailFCC, registrationFormConfig.getFieldCompanionEmail());

						setField(companionMandateFCC, registrationFormConfig.getFieldCompanionMandate());
						setField(companionNobilityFCC, registrationFormConfig.getFieldCompanionNobility());
						setField(companionNobilityPrefixFCC, registrationFormConfig.getFieldCompanionNobilityPrefix());
						setField(companionAdminTitleFCC, registrationFormConfig.getFieldCompanionAdminTitle());
						setField(companionFunctionFCC, registrationFormConfig.getFieldCompanionFunction());

						lastNameReadOnlyButton.setSelection(registrationFormConfig.isLastNameReadonly());

					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						if (avoidEvents) {
							modifySupport.setEnabled(true);
						}
					}
				}
			});

		}
	}

	protected void setField(FieldConfigComposite fcc, Field field) {
		if (fcc != null) {
			fcc.setField(field);
		}
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	public void syncEntityToWidgets() {
		registrationFormConfig.setFieldParticipantGender(getField(genderFCC));
		registrationFormConfig.setFieldParticipantFirstName(getField(firstNameFCC));
		registrationFormConfig.setFieldParticipantLastName(getField(lastNameFCC));
		registrationFormConfig.setFieldParticipantDegree(getField(degreeFCC));
		registrationFormConfig.setFieldParticipantFunction(getField(functionFCC));
		registrationFormConfig.setFieldParticipantBirthDay(getField(birthdayFCC));
		registrationFormConfig.setFieldParticipantPlaceOfBirth(getField(placeOfBirthFCC));
		registrationFormConfig.setFieldParticipantNationality(getField(nationalityFCC));
		registrationFormConfig.setFieldParticipantCME(getField(cmeFCC));

		registrationFormConfig.setFieldParticipantMandate(getField(mandateFCC));
		registrationFormConfig.setFieldParticipantAdminTitle(getField(adminTitleFCC));
		registrationFormConfig.setFieldParticipantNobility(getField(nobilityFCC));
		registrationFormConfig.setFieldParticipantNobilityPrefix(getField(nobilityPrefixFCC));
		registrationFormConfig.setFieldParticipantCustomerNo(getField(customerNoFCC));
		registrationFormConfig.setFieldParticipantTaxId(getField(taxIdFCC));

		registrationFormConfig.setFieldParticipantOrganisation(getField(organisationFCC));
		registrationFormConfig.setFieldParticipantDepartment(getField(departmentFCC));
		registrationFormConfig.setFieldParticipantAddressee(getField(addresseeFCC));
		registrationFormConfig.setFieldParticipantStreet(getField(streetFCC));
		registrationFormConfig.setFieldParticipantCity(getField(cityFCC));
		registrationFormConfig.setFieldParticipantZip(getField(zipFCC));
		registrationFormConfig.setFieldParticipantState(getField(stateFCC));
		registrationFormConfig.setFieldParticipantCountry(getField(countryFCC));

		registrationFormConfig.setFieldParticipantAltOrganisation(getField(altOrganisationFCC));
		registrationFormConfig.setFieldParticipantAltDepartment(getField(altDepartmentFCC));
		registrationFormConfig.setFieldParticipantAltAddressee(getField(altAddresseeFCC));
		registrationFormConfig.setFieldParticipantAltStreet(getField(altStreetFCC));
		registrationFormConfig.setFieldParticipantAltCity(getField(altCityFCC));
		registrationFormConfig.setFieldParticipantAltZip(getField(altZipFCC));
		registrationFormConfig.setFieldParticipantAltState(getField(altStateFCC));
		registrationFormConfig.setFieldParticipantAltCountry(getField(altCountryFCC));

		registrationFormConfig.setFieldParticipantEmail(getField(emailFCC));
		registrationFormConfig.setFieldParticipantPhone1(getField(phone1FCC));
		registrationFormConfig.setFieldParticipantPhone2(getField(phone2FCC));
		registrationFormConfig.setFieldParticipantMobile1(getField(mobile1FCC));
		registrationFormConfig.setFieldParticipantMobile2(getField(mobile2FCC));
		registrationFormConfig.setFieldParticipantFax(getField(faxFCC));

		registrationFormConfig.setFieldCompanionGender(getField(companionGenderFCC));
		registrationFormConfig.setFieldCompanionFirstName(getField(companionFirstNameFCC));
		registrationFormConfig.setFieldCompanionLastName(getField(companionLastNameFCC));
		registrationFormConfig.setFieldCompanionCity(getField(companionCityFCC));
		registrationFormConfig.setFieldCompanionNationality(getField(companionNationalityFCC));
		registrationFormConfig.setFieldCompanionDegree(getField(companionDegreeFCC));
		registrationFormConfig.setFieldCompanionEmail(getField(companionEmailFCC));

		registrationFormConfig.setFieldCompanionFunction(getField(companionFunctionFCC));
		registrationFormConfig.setFieldCompanionMandate(getField(companionMandateFCC));
		registrationFormConfig.setFieldCompanionAdminTitle(getField(companionAdminTitleFCC));
		registrationFormConfig.setFieldCompanionNobility(getField(companionNobilityFCC));
		registrationFormConfig.setFieldCompanionNobilityPrefix(getField(companionNobilityPrefixFCC));

		registrationFormConfig.setLastNameReadonly(lastNameReadOnlyButton.getSelection());
}


	private Field getField(FieldConfigComposite fcc) {
		if (fcc != null) {
			return fcc.getField();
		}
		else {
			return Field.INV;
		}
	}


	public void setRegistrationFormConfig(RegistrationFormConfig registrationFormConfig) {
		this.registrationFormConfig = registrationFormConfig;

		syncWidgetsToEntity();
	}

}
