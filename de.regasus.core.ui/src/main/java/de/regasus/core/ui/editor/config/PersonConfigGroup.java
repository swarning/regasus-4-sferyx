package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.parameter.PersonConfigParameter;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

public class PersonConfigGroup extends Group {

	// the entity
	private PersonConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private PersonConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets genderWidgets;
	private FieldConfigWidgets degreeWidgets;
	private FieldConfigWidgets nobilityWidgets;
	private FieldConfigWidgets adminTitleWidgets;
	private FieldConfigWidgets firstNameWidgets;
	private FieldConfigWidgets middleNameWidgets;
	private FieldConfigWidgets nobilityPrefixWidgets;
	private FieldConfigWidgets mandateWidgets;
	private FieldConfigWidgets functionWidgets;
	private FieldConfigWidgets salutationWidgets;
	private FieldConfigWidgets invitationCardWidgets;
	private FieldConfigWidgets dateOfBirthWidgets;
	private FieldConfigWidgets placeOfBirthWidgets;
	private FieldConfigWidgets languageWidgets;
	private FieldConfigWidgets nationalityWidgets;
	private FieldConfigWidgets customerNumberWidgets;
	private FieldConfigWidgets customerAccountNumberWidgets;
	private FieldConfigWidgets taxIdWidgets;
	private FieldConfigWidgets cmeNumberWidgets;
	private FieldConfigWidgets passportIDWidgets;
	private FieldConfigWidgets noteWidgets;

	/**
	 * Array with all widgets to make some methods shorter.
	 */
	private FieldConfigWidgets[] fieldConfigWidgetsList;


	public PersonConfigGroup(Composite parent, int style) {
		super(parent, style);

		setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );
		setText(ContactLabel.person.getString());


		genderWidgets = new FieldConfigWidgets(this, Person.GENDER.getString());
		degreeWidgets = new FieldConfigWidgets(this, Person.DEGREE.getString());
		nobilityWidgets = new FieldConfigWidgets(this, Person.NOBILITY.getString());
		adminTitleWidgets = new FieldConfigWidgets(this, Person.ADMIN_TITLE.getString());
		firstNameWidgets = new FieldConfigWidgets(this, Person.FIRST_NAME.getString());
		middleNameWidgets = new FieldConfigWidgets(this, ContactLabel.additionalFirstNames.getString());
		nobilityPrefixWidgets = new FieldConfigWidgets(this, Person.NOBILITY_PREFIX.getString());
		mandateWidgets = new FieldConfigWidgets(this, Person.MANDATE.getString());
		functionWidgets = new FieldConfigWidgets(this, Person.FUNCTION.getString());
		salutationWidgets = new FieldConfigWidgets(this, Person.SALUTATION.getString());
		invitationCardWidgets = new FieldConfigWidgets(this, Person.INVITATION_CARD.getString());
		dateOfBirthWidgets = new FieldConfigWidgets(this, Person.DATE_OF_BIRTH.getString());
		placeOfBirthWidgets = new FieldConfigWidgets(this, Person.PLACE_OF_BIRTH.getString());
		nationalityWidgets = new FieldConfigWidgets(this, Person.NATIONALITY.getString());
		customerNumberWidgets = new FieldConfigWidgets(this, Person.CUSTOMER_NO.getString());
		languageWidgets = new FieldConfigWidgets(this, Person.LANGUAGE_CODE.getLabel());
		customerAccountNumberWidgets = new FieldConfigWidgets(this, Person.CUSTOMER_ACCOUNT_NUMBER.getString());
		taxIdWidgets = new FieldConfigWidgets(this, Person.TAX_ID.getString());
		cmeNumberWidgets = new FieldConfigWidgets(this, Person.CME_NO.getString());
		passportIDWidgets = new FieldConfigWidgets(this, Person.PASSPORT_ID.getString());
		noteWidgets = new FieldConfigWidgets(this, Person.NOTE.getString());

		fieldConfigWidgetsList = new FieldConfigWidgets[] {
			genderWidgets,
			degreeWidgets,
			nobilityWidgets,
			adminTitleWidgets,
			firstNameWidgets,
			middleNameWidgets,
			nobilityPrefixWidgets,
			mandateWidgets,
			functionWidgets,
			salutationWidgets,
			invitationCardWidgets,
			dateOfBirthWidgets,
			placeOfBirthWidgets,
			languageWidgets,
			nationalityWidgets,
			customerNumberWidgets,
			customerAccountNumberWidgets,
			taxIdWidgets,
			cmeNumberWidgets,
			passportIDWidgets,
			noteWidgets,
		};
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(PersonConfigParameter adminConfigParameter) {
		this.adminConfigParameter = adminConfigParameter;

		updateEnabledStatus();
	}


	private void updateEnabledStatus() {
		/* visibility of widgets depends on
		 * - enable-state of the Group
		 * - the setting of corresponding Admin-Config
		 */

		/* Use getEnabled() instead of isEnabled(), because isEnabled() returns only true if the
		 * Control and all its parent controls are enabled, whereas the result of getEnabled()
		 * relates only to the Control itself.
		 * For some reason, isEnbaled() returns false.
		 */
		boolean enabled = getEnabled();

		genderWidgets.setEnabled(				enabled && adminConfigParameter.getGenderConfigParameter().isVisible());
		degreeWidgets.setEnabled(				enabled && adminConfigParameter.getDegreeConfigParameter().isVisible());
		nobilityWidgets.setEnabled(				enabled && adminConfigParameter.getNobilityConfigParameter().isVisible());
		adminTitleWidgets.setEnabled(			enabled && adminConfigParameter.getAdminTitleConfigParameter().isVisible());
		firstNameWidgets.setEnabled(			enabled && adminConfigParameter.getFirstNameConfigParameter().isVisible());
		middleNameWidgets.setEnabled(			enabled && adminConfigParameter.getMiddleNameConfigParameter().isVisible());
		nobilityPrefixWidgets.setEnabled(		enabled && adminConfigParameter.getNobilityPrefixConfigParameter().isVisible());
		mandateWidgets.setEnabled(				enabled && adminConfigParameter.getMandateConfigParameter().isVisible());
		functionWidgets.setEnabled(				enabled && adminConfigParameter.getFunctionConfigParameter().isVisible());
		salutationWidgets.setEnabled(			enabled && adminConfigParameter.getSalutationConfigParameter().isVisible());
		invitationCardWidgets.setEnabled(		enabled && adminConfigParameter.getInvitationCardConfigParameter().isVisible());
		dateOfBirthWidgets.setEnabled(			enabled && adminConfigParameter.getDateOfBirthConfigParameter().isVisible());
		placeOfBirthWidgets.setEnabled(         enabled && adminConfigParameter.getPlaceOfBirthConfigParameter().isVisible());
		languageWidgets.setEnabled(				enabled && adminConfigParameter.getLanguageConfigParameter().isVisible());
		nationalityWidgets.setEnabled(			enabled && adminConfigParameter.getNationalityConfigParameter().isVisible());
		customerNumberWidgets.setEnabled(		enabled && adminConfigParameter.getCustomerNumberConfigParameter().isVisible());
		customerAccountNumberWidgets.setEnabled(enabled && adminConfigParameter.getCustomerAccountNumberConfigParameter().isVisible());
		taxIdWidgets.setEnabled(				enabled && adminConfigParameter.getTaxIdConfigParameter().isVisible());
		cmeNumberWidgets.setEnabled(			enabled && adminConfigParameter.getCmeNumberConfigParameter().isVisible());
		passportIDWidgets.setEnabled(			enabled && adminConfigParameter.getPassportIDConfigParameter().isVisible());
		noteWidgets.setEnabled(					enabled && adminConfigParameter.getNoteConfigParameter().isVisible());
	}


	public void addModifyListener(ModifyListener modifyListener) {
		for (FieldConfigWidgets fieldConfigWidgets : fieldConfigWidgetsList) {
			fieldConfigWidgets.addModifyListener(modifyListener);
		}
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						for (FieldConfigWidgets fieldConfigWidgets : fieldConfigWidgetsList) {
							fieldConfigWidgets.syncWidgetsToEntity();
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (configParameter != null) {
			for (FieldConfigWidgets fieldConfigWidgets : fieldConfigWidgetsList) {
				fieldConfigWidgets.syncEntityToWidgets();
			}
		}
	}


	public void setConfigParameter(PersonConfigParameter configParameter) {
		this.configParameter = configParameter;

		// set entity to other composites
		genderWidgets.setFieldConfigParameter( configParameter.getGenderConfigParameter() );
		degreeWidgets.setFieldConfigParameter( configParameter.getDegreeConfigParameter() );
		nobilityWidgets.setFieldConfigParameter( configParameter.getNobilityConfigParameter() );
		adminTitleWidgets.setFieldConfigParameter( configParameter.getAdminTitleConfigParameter() );
		firstNameWidgets.setFieldConfigParameter( configParameter.getFirstNameConfigParameter() );
		middleNameWidgets.setFieldConfigParameter( configParameter.getMiddleNameConfigParameter() );
		nobilityPrefixWidgets.setFieldConfigParameter( configParameter.getNobilityPrefixConfigParameter() );
		mandateWidgets.setFieldConfigParameter( configParameter.getMandateConfigParameter() );
		functionWidgets.setFieldConfigParameter( configParameter.getFunctionConfigParameter() );
		salutationWidgets.setFieldConfigParameter( configParameter.getSalutationConfigParameter() );
		invitationCardWidgets.setFieldConfigParameter( configParameter.getInvitationCardConfigParameter() );
		dateOfBirthWidgets.setFieldConfigParameter( configParameter.getDateOfBirthConfigParameter() );
		placeOfBirthWidgets.setFieldConfigParameter( configParameter.getPlaceOfBirthConfigParameter() );
		languageWidgets.setFieldConfigParameter( configParameter.getLanguageConfigParameter() );
		nationalityWidgets.setFieldConfigParameter( configParameter.getNationalityConfigParameter() );
		customerNumberWidgets.setFieldConfigParameter( configParameter.getCustomerNumberConfigParameter() );
		customerAccountNumberWidgets.setFieldConfigParameter( configParameter.getCustomerAccountNumberConfigParameter() );
		taxIdWidgets.setFieldConfigParameter( configParameter.getTaxIdConfigParameter() );
		cmeNumberWidgets.setFieldConfigParameter( configParameter.getCmeNumberConfigParameter() );
		passportIDWidgets.setFieldConfigParameter( configParameter.getPassportIDConfigParameter() );
		noteWidgets.setFieldConfigParameter( configParameter.getNoteConfigParameter() );

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
