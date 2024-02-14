package de.regasus.common.composite;


import static com.lambdalogic.util.StringHelper.isEmpty;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ParticipantConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.PersonConfigParameterSet;
import com.lambdalogic.messeinfo.contact.AdminTitleList;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.NobilityList;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.EntityNotFoundException;
import com.lambdalogic.util.rcp.AutoCorrectionWidgetHelper;
import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.i18n.I18NText;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetBuilder;

import de.regasus.I18N;
import de.regasus.common.Gender;
import de.regasus.common.country.combo.CountryCombo;
import de.regasus.common.salutation.InvitationCardGenerator;
import de.regasus.common.salutation.SalutationGenerator;
import de.regasus.core.PropertyModel;
import de.regasus.core.SalutationGeneratorModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.combo.LanguageCombo;
import de.regasus.core.ui.dialog.EntryOrSelectionDialog;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.event.EventModel;
import de.regasus.participant.ParticipantModel;
import de.regasus.person.FunctionListModel;
import de.regasus.profile.ProfileModel;
import de.regasus.ui.Activator;
import de.regasus.util.XmlHelper;

public class PersonGroup extends EntityGroup<Person> {
	/**
	 * Refresh the default salutation based on the values in person.
	 * Setting the parameter syncEntityToWidgets to true, copies the value from the widgets into person
	 * first, so that the defaultSalutation is based on the current values in the widgets.
	 *
	 * @param syncEntityToWidgets
	 */
	private static Profile widgetBasedProfile = new Profile();
	private static Participant widgetBasedParticipant = new Participant();

	private static NobilityList nobilityList;
	private static AdminTitleList adminTitleList;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	/**
	 * Flag to remember if last name has been modified.
	 */
	private boolean lastNameModified;


	// Models
	private FunctionListModel functionListModel;
	private EventModel eventModel;
	private PropertyModel propertyModel;

	private PersonConfigParameterSet personConfigParameterSet;

	// Widgets
	private Button maleButton;
	private Button femaleButton;
	private Button diverseButton;
	private Button organizationButton;
	private Button unknownButton;

	private Text degree;
	private Text nobility;
	private Text adminTitle;
	private Text firstName;
	private Text middleName;
	private Text nobilityPrefix;
	private Text lastName;
	private Text mandate;
	private Text functionText;
	private I18NText salutation;
	private Button defaultSalutationButton;
	private I18NText invitationCard;
	private Button defaultInvitationCardButton;
	private DateComposite dateOfBirth;
	private Text placeOfBirth;
	private LanguageCombo languageCombo;
	private CountryCombo nationalityCombo;
	private Text passportID;
	private Text customerNoText;
	private Text customerAccountNumberText;
	private Text taxIdText;
	private Text cmeNoText;

	private Button openFunctionDialogButton;
	private Button openNobilityDialogButton;
	private Button openAdminTitleDialogButton;


    public PersonGroup(
    	Composite parent,
    	int style,
    	PersonConfigParameterSet personConfigParameterSet
    )
    throws Exception {
		// super calls initialize(Object[]) and createWidgets(Composite)
    	super(parent, style, personConfigParameterSet);

    	setText( ContactLabel.person.getString() );
    }


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		try {
			this.personConfigParameterSet = (PersonConfigParameterSet) initValues[0];
			/* Initialize with any IPersonConfigParameterSet to avoid null checkings.
			 */
	    	if (personConfigParameterSet == null) {
				personConfigParameterSet = new ParticipantConfigParameterSet(new ConfigParameterSet());
			}

	    	// init Models
	    	functionListModel = FunctionListModel.getInstance();
	    	eventModel = EventModel.getInstance();
	    	propertyModel = PropertyModel.getInstance();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout(new GridLayout(3, false));


		// **************************************************************************
		// * gender
		// *

		if ( personConfigParameterSet.getGender().isVisible() ) {
			widgetBuilder.fieldMetadata(Person.GENDER);
			widgetBuilder.createLabel();

    		Composite composite = buildGenderComposite(this);
    		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.FILL).grab(true, false).span(2, 1).applyTo(composite);
		}

		// *
		// * gender
		// **************************************************************************

		widgetBuilder.getTextGridDataFactory().span(2, 1);

		// degree
		if (personConfigParameterSet.getDegree().isVisible()) {
			degree = widgetBuilder.fieldMetadata(Person.DEGREE).createTextWithLabel();
		}

		// nobility
		if (personConfigParameterSet.getNobility().isVisible()) {
			nobility = widgetBuilder.fieldMetadata(Person.NOBILITY).createTextWithLabel();
			nobility.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1) );

			openNobilityDialogButton = new Button(this, SWT.PUSH);
			openNobilityDialogButton.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1) );
			openNobilityDialogButton.setText("...");
			openNobilityDialogButton.addListener(SWT.Selection, e -> chooseNobility());
		}

		// adminTitle
		if (personConfigParameterSet.getAdminTitle().isVisible()) {
			adminTitle = widgetBuilder.fieldMetadata(Person.ADMIN_TITLE).createTextWithLabel();
			adminTitle.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1) );

    		openAdminTitleDialogButton = new Button(this, SWT.PUSH);
    		openAdminTitleDialogButton.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1) );
    		openAdminTitleDialogButton.setText("...");
    		openAdminTitleDialogButton.addListener(SWT.Selection, e -> chooseAdminTitle());
		}


		/* firstName(s)
		 * The fields firstName and middleName are shown in a single line.
		 * Therefore an additional Composite is needed.
		 */
		if (personConfigParameterSet.getFirstName().isVisible()) {
			widgetBuilder.createLabel(ContactLabel.firstNames, ContactLabel.firstNames_description);


			Composite firstNameComposite = new Composite(this, SWT.NONE);

    		GridData firstNameCompositeLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			firstNameComposite.setLayoutData(firstNameCompositeLayoutData);

			GridLayout firstNameCompositeLayout = new GridLayout(2, false);
			firstNameCompositeLayout.horizontalSpacing = 0;
			firstNameCompositeLayout.verticalSpacing = 0;
			firstNameCompositeLayout.marginHeight = 0;
			firstNameCompositeLayout.marginWidth = 0;
			firstNameComposite.setLayout(firstNameCompositeLayout);

			// firstName
    		firstName = new Text(firstNameComposite, SWT.BORDER);
    		firstName.setTextLimit( Person.FIRST_NAME.getMaxLength() );

    		firstName.addModifyListener(modifySupport);

    		GridData firstNameLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    		firstNameLayoutData.horizontalIndent = 1;
    		firstNameLayoutData.verticalIndent = 1;
    		firstName.setLayoutData(firstNameLayoutData);

    		// middleName (additional first names)
    		if (personConfigParameterSet.getMiddleName().isVisible()) {
    			middleName = new Text(firstNameComposite, SWT.BORDER);

        		GridData middleNameLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        		middleNameLayoutData.horizontalIndent = 1;
        		middleNameLayoutData.verticalIndent = 1;
        		middleName.setLayoutData(middleNameLayoutData);

        		middleName.addModifyListener(modifySupport);
    		}
    		else {
        		firstNameLayoutData.horizontalSpan = 2;
    		}
		}



		if (personConfigParameterSet.getNobilityPrefix().isVisible()) {
			nobilityPrefix = widgetBuilder.fieldMetadata(Person.NOBILITY_PREFIX).createTextWithLabel();
		}

		// lastName
		lastName = widgetBuilder.fieldMetadata(Person.LAST_NAME).createTextWithLabel();

		// mandate
		if (personConfigParameterSet.getMandate().isVisible()) {
			mandate = widgetBuilder.fieldMetadata(Person.MANDATE).createTextWithLabel();
		}


		// function
		if (personConfigParameterSet.getFunction().isVisible()) {
			functionText = widgetBuilder.fieldMetadata(Person.FUNCTION).createTextWithLabel();
    		functionText.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1) );

    		openFunctionDialogButton = new Button(this, SWT.PUSH);
    		openFunctionDialogButton.setLayoutData( new GridData(SWT.LEAD, SWT.CENTER, false, false, 1, 1) );
    		openFunctionDialogButton.setText("...");
    		openFunctionDialogButton.addListener(SWT.Selection, e -> chooseFunction());
		}

		widgetBuilder.horizontalLine();

		// salutation
		if (personConfigParameterSet.getSalutation().isVisible()) {
			Label label = widgetBuilder.fieldMetadata(Person.SALUTATION).createLabel();
			SWTHelper.top(label);


			salutation = new I18NText(this, SWT.NONE, LanguageProvider.getInstance());
			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			// Set this hint to show around 4 tabs so that the presence of 11 supported
			// salutation languages doesn't overly stretch this component when scrollbars
			// are refreshed
			layoutData.widthHint = 200;
			salutation.setLayoutData(layoutData);

			salutation.addModifyListener(modifySupport);

			defaultSalutationButton = new Button(this, SWT.CHECK);
			defaultSalutationButton.setText(KernelLabel.Standard.getString());
			defaultSalutationButton.setToolTipText(I18N.PersonGroup_StandardSalutationToolTip);
			defaultSalutationButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					modifySupport.fire();
					boolean isStandardSalutation = defaultSalutationButton.getSelection();
					salutation.setEnabled(!isStandardSalutation);
					if (isStandardSalutation) {
						refreshDefaultSalutationInternal();
					}
				}
			});
		}

		// invitation card
		if (personConfigParameterSet.getInvitationCard().isVisible()) {
			Label label = widgetBuilder.fieldMetadata(Person.INVITATION_CARD).createLabel();
			SWTHelper.top(label);


			invitationCard = new I18NText(this, SWT.NONE, LanguageProvider.getInstance());
			invitationCard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			invitationCard.addModifyListener(modifySupport);

			defaultInvitationCardButton = new Button(this, SWT.CHECK);
			defaultInvitationCardButton.setText(KernelLabel.Standard.getString());
			defaultInvitationCardButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					modifySupport.fire();
					boolean isStandardInvitationCard = defaultInvitationCardButton.getSelection();
					invitationCard.setEnabled(!isStandardInvitationCard);
					if (isStandardInvitationCard) {
						refreshDefaultInvitationCardInternal();
					}
				}
			});
		}


		// date of birth
		if (personConfigParameterSet.getDateOfBirth().isVisible()) {
			widgetBuilder.fieldMetadata(Person.DATE_OF_BIRTH).createLabel();

			dateOfBirth = new DateComposite(this, SWT.BORDER);
			dateOfBirth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			dateOfBirth.addModifyListener(modifySupport);
		}

		// place of birth
		if (personConfigParameterSet.getPlaceOfBirth().isVisible()) {
			placeOfBirth = widgetBuilder.fieldMetadata(Person.PLACE_OF_BIRTH).createTextWithLabel();
		}

		// language
		if (personConfigParameterSet.getLanguage().isVisible()) {
			widgetBuilder.fieldMetadata(Person.LANGUAGE_CODE).createLabel();

			languageCombo = new LanguageCombo(this, SWT.READ_ONLY);
			languageCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			languageCombo.addModifyListener(modifySupport);
		}


		// nationality
		if (personConfigParameterSet.getNationality().isVisible()) {
			widgetBuilder.fieldMetadata(Person.NATIONALITY).createLabel();

			nationalityCombo = new CountryCombo(this, SWT.READ_ONLY);
			nationalityCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			nationalityCombo.addModifyListener(modifySupport);
		}


		// passportID
		if (personConfigParameterSet.getPassportID().isVisible()) {
			passportID = widgetBuilder.fieldMetadata(Person.PASSPORT_ID).createTextWithLabel();
		}


		// Customer Number
		if (personConfigParameterSet.getCustomerNumber().isVisible()) {
			customerNoText = widgetBuilder.fieldMetadata(Person.CUSTOMER_NO).createTextWithLabel();
		}


		// customerAccountNumber
		if (personConfigParameterSet.getCustomerAccountNumber().isVisible()) {
			customerAccountNumberText = widgetBuilder.fieldMetadata(Person.CUSTOMER_ACCOUNT_NUMBER).createTextWithLabel();
		}

		// taxId
		if (personConfigParameterSet.getTaxId().isVisible()) {
			taxIdText = widgetBuilder.fieldMetadata(Person.TAX_ID).createTextWithLabel();
		}


		// CME Number
		if (personConfigParameterSet.getCMENumber().isVisible()) {
			cmeNoText= widgetBuilder.fieldMetadata(Person.CME_NO).createTextWithLabel();
		}


		modifySupport.addBeforeModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				// MIRCP-17 - When a user changes the last name of an anonymous participant
				String lastNameValue = StringHelper.trim(lastName.getText());
				if (!EqualsHelper.isEqual(lastNameValue, entity.getLastName())) {
					lastNameModified = true;
				}
				/* Copy values from widgets to entity.
				 * Necessary because some ModifyListeners expect the modified data in the entity.
				 */
				syncEntityToWidgets();
			}
		});


		modifySupport.addListener(e -> {
			refreshDefaultSalutation();
			refreshDefaultInvitationCard();
		});


		layout();
	}


	private Composite buildGenderComposite(Composite parent) {
		Composite composite = new Composite(this, SWT.NONE);

		// create another WidgetBuilder with different parent
		WidgetBuilder<Person> genderWidgetBuilder = widgetBuilder.copy().parent(composite);

		RowLayout layout = new RowLayout();
		layout.wrap = true;
		composite.setLayout(layout);

		maleButton = genderWidgetBuilder.createRadio( Gender.MALE.getString() );
		femaleButton = genderWidgetBuilder.createRadio( Gender.FEMALE.getString() );
		diverseButton = genderWidgetBuilder.createRadio( Gender.DIVERSE.getString() );
		organizationButton = genderWidgetBuilder.createRadio( Gender.ORGANIZATION.getString() );
		unknownButton = genderWidgetBuilder.createRadio( Gender.UNKNOWN.getString() );

		return composite;
	}


	/**
	 * Gets the currently known functions from the model and opens a dialog to select one of it
	 * or enter a new one.
	 */
	private void chooseFunction() {
		// Get the currently known functions from the model
		List<String> functionList = new ArrayList<>();
		try {
			functionList.addAll(functionListModel.getModelData());
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		// Add the currently entered function, in case it is not yet saved on the server
		String currentFunction = functionText.getText();
		if (currentFunction.trim().length() > 0 && ! functionList.contains(currentFunction)) {
			functionList.add(0, currentFunction);
		}

		// Prepare the dialog to select or enter a function
		EntryOrSelectionDialog listDialog = new EntryOrSelectionDialog(getShell());
		listDialog.setTitle( Person.FUNCTION.getString() );
		listDialog.setMessage(I18N.EnterOrSelectFunction);
		listDialog.setElements(functionList.toArray(new String[functionList.size()]));
		listDialog.setFilter(functionText.getText());
		int code = listDialog.open();
		if (code == Window.OK) {
			// Put the selected or entered function back to the text widget
			Object object = listDialog.getSelectionOrEntry();
			if (object != null && object instanceof String) {
				functionText.setText((String) object);
			}
		}
	}

	/**
	 * Gets the pre-defined nobilities depending on the gender and opens a dialog to select one of it
	 * or enter a new one.
	 */
	private void chooseNobility() {
		try {
    		if (nobilityList == null) {
    			InputStream inputStream = NobilityList.class.getResourceAsStream("nobilities.xml");
    			nobilityList = XmlHelper.createFromXML(inputStream, NobilityList.class);
    		}


    		// Prepare the dialog to select a value
    		ElementListSelectionDialog listDialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
    		listDialog.setTitle(Person.NOBILITY.getString());
    		listDialog.setMessage(I18N.SelectNobility);

    		// Fill in the gender-dependent nobility names
    		if (Gender.MALE.equals(entity.getGender())) {
    			listDialog.setElements(nobilityList.getMaleNobilityNames());
    		}
    		else if (Gender.FEMALE.equals(entity.getGender())) {
    			listDialog.setElements(nobilityList.getFemaleNobilityNames());
    		}
    		else {
    			listDialog.setElements(nobilityList.getAllNobilityNames());
    		}

    		// Get the currently selected nobility
    		String currentNobility = nobility.getText();
    		if (currentNobility != null) {
    			listDialog.setInitialElementSelections(Collections.singletonList(currentNobility));
    		}

    		int code = listDialog.open();
    		if (code == Window.OK) {
    			// put selected value back to the text widget
    			Object object = listDialog.getFirstResult();
    			if (object instanceof String) {
    				nobility.setText((String)object);
    			}
    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void chooseAdminTitle() {
		try {
    		if (adminTitleList == null) {
    			InputStream inputStream = AdminTitleList.class.getResourceAsStream("admin-titles.xml");
    			adminTitleList = XmlHelper.createFromXML(inputStream, AdminTitleList.class);
    		}


    		// Prepare the dialog to select a value
    		ElementListSelectionDialog listDialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
    		listDialog.setTitle(Person.ADMIN_TITLE.getString());
    		listDialog.setMessage(I18N.SelectAdminTitle);

    		// Fill in the gender-dependent admin titles
    		if (Gender.MALE.equals(entity.getGender())) {
    			listDialog.setElements(adminTitleList.getMaleAdminTitles());
    		}
    		else if (Gender.FEMALE.equals(entity.getGender())) {
    			listDialog.setElements(adminTitleList.getFemaleAdminTitles());
    		}
    		else {
    			listDialog.setElements(adminTitleList.getAllAdminTitles());
    		}

    		String currentAdminTitle = adminTitle.getText();

    		if (currentAdminTitle != null) {
    			listDialog.setInitialElementSelections(Collections.singletonList(currentAdminTitle));
    		}

    		int code = listDialog.open();
    		if (code == Window.OK) {
    			// put selected value back to the text widget
    			Object object = listDialog.getFirstResult();
    			if (object instanceof String) {
    				adminTitle.setText((String)object);
    			}
    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public boolean isLastNameModified() {
		return lastNameModified;
	}


	public void setLastNameModified(boolean value) {
		this.lastNameModified = value;
	}


	@Override
	protected void syncWidgetsToEntity() throws Exception {
		if (maleButton != null) {
			/* Just setting to true isn't sufficient, in case the gender gets changed and we refresh,
			 * then the previous gender doesn't get deselected!
			 */

			Gender gender = entity.getGender();
			maleButton.setSelection(gender == Gender.MALE);
			femaleButton.setSelection(gender == Gender.FEMALE);
			diverseButton.setSelection(gender == Gender.DIVERSE);
			organizationButton.setSelection(gender == Gender.ORGANIZATION);
			unknownButton.setSelection(gender == Gender.UNKNOWN);
		}

		if (degree != null) {
			degree.setText(StringHelper.avoidNull(entity.getDegree()));
		}

		if (nobility != null) {
			nobility.setText(StringHelper.avoidNull(entity.getNobility()));
		}

		if (adminTitle != null) {
			adminTitle.setText(StringHelper.avoidNull(entity.getAdminTitle()));
		}

		if (firstName != null) {
			firstName.setText(StringHelper.avoidNull(entity.getFirstName()));
		}

		if (middleName != null) {
			middleName.setText(StringHelper.avoidNull(entity.getMiddleName()));
		}

		if (nobilityPrefix != null) {
			nobilityPrefix.setText(StringHelper.avoidNull(entity.getNobilityPrefix()));
		}

		lastName.setText(StringHelper.avoidNull(entity.getLastName()));

		if (mandate != null) {
			mandate.setText(StringHelper.avoidNull(entity.getMandate()));
		}

		if (functionText != null) {
			functionText.setText(StringHelper.avoidNull(entity.getFunction()));
		}


		if (salutation != null) {
			boolean isDefaultSalutation = !entity.hasIndividualSalutation();
			defaultSalutationButton.setSelection(isDefaultSalutation);
			salutation.setEnabled(!isDefaultSalutation);
			if (isDefaultSalutation) {
				refreshDefaultSalutationInternal();
			}
			else {
				salutation.setLanguageString(entity.getIndividualSalutation());
			}
		}

		if (invitationCard != null) {
			boolean isDefaultInvitationCard = !entity.hasIndividualInvitationCard();
			defaultInvitationCardButton.setSelection(isDefaultInvitationCard);
			invitationCard.setEnabled(!isDefaultInvitationCard);
			if (isDefaultInvitationCard) {
				refreshDefaultInvitationCardInternal();
			}
			else {
				invitationCard.setLanguageString(entity.getIndividualInvitationCard());
			}
		}

		if (dateOfBirth != null) {
			I18NDate dob = entity.getDateOfBirth();
			dateOfBirth.setI18NDate(dob);
		}

		if (placeOfBirth != null) {
			placeOfBirth.setText(StringHelper.avoidNull(entity.getPlaceOfBirth()));
		}

		if (languageCombo != null) {
			languageCombo.setLanguageCode(entity.getLanguageCode());
		}

		if (nationalityCombo != null) {
			String nationalityPK = entity.getNationalityPK();
			nationalityCombo.setCountryCode(nationalityPK);
		}

		if (passportID != null) {
			passportID.setText(StringHelper.avoidNull(entity.getPassportID()));
		}

		if (customerNoText != null) {
			customerNoText.setText(StringHelper.avoidNull(entity.getCustomerNo()));
		}

		if (customerAccountNumberText != null) {
			customerAccountNumberText.setText(StringHelper.avoidNull(entity.getCustomerAccountNumber()));
		}

		if (taxIdText != null) {
			taxIdText.setText(StringHelper.avoidNull(entity.getTaxId()));
		}

		if (cmeNoText != null) {
			cmeNoText.setText(StringHelper.avoidNull(entity.getCmeNo()));
		}
	}


	public void refreshDefaultSalutation() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					refreshDefaultSalutationInternal();
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	private void refreshDefaultSalutationInternal() {
		if (salutation != null && defaultSalutationButton.getSelection()) {

			/* We have to remember if we set ignoreModify to true, because of nested usage of ignoreModify.
			 * We must not set it to false in the finally block if we did not set it to true ourselves.
			 */
			boolean ignoreModifyChanged = false;

			try {
				if (modifySupport.isEnabled()) {
					modifySupport.setEnabled(false);
					ignoreModifyChanged = true;
				}

				/* set secondPerson
				 * To get the secondPerson from the model and set it here is not elegant.
				 * The alternative would be to let the model do the work. But this seems to be very complex.
				 * Since the data of the secondPerson is cached by the models, this approach is fast enough.
				 */
				Long secondPersonID = entity.getSecondPersonID();
				SalutationGenerator salutationGenerator = SalutationGeneratorModel.getInstance().getSalutationGenerator();

				LanguageString salutations = null;
				if (entity instanceof Participant) {
					// copy all relevant values into salutationPerson
					syncEntityToWidgets(widgetBasedParticipant);

		    		// set 2nd person before generating address labels
					Participant secondPerson = null;
					if (secondPersonID != null) {
						try {
							secondPerson = ParticipantModel.getInstance().getParticipant(secondPersonID);
						}
						catch (EntityNotFoundException e) {
							// ignore
						}
					}
					widgetBasedParticipant.setSecondPerson(secondPerson);

					// determine default languages
					Long eventPK = ((Participant) entity).getEventId();
					EventVO eventVO = eventModel.getEventVO(eventPK);
					List<String> languages = eventVO.getLanguages();

					salutations = salutationGenerator.getSalutation(widgetBasedParticipant, languages);
				}
				else if (entity instanceof Profile) {
					// copy all relevant values into salutationPerson
					syncEntityToWidgets(widgetBasedProfile);

		    		// set 2nd person before generating address labels
					Profile secondPerson = null;
					if (secondPersonID != null) {
						secondPerson = ProfileModel.getInstance().getProfile(secondPersonID);
					}
					widgetBasedProfile.setSecondPerson(secondPerson);

					// determine default languages
					List<String> languages = propertyModel.getDefaultLanguageList();

					salutations = salutationGenerator.getSalutation(widgetBasedProfile, languages);
				}

				salutation.setLanguageString(salutations);
				salutation.getParent().layout();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
			finally {
				if (ignoreModifyChanged) {
					modifySupport.setEnabled(true);
				}
			}
		}
	}


	public void refreshDefaultInvitationCard() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					refreshDefaultInvitationCardInternal();
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	private void refreshDefaultInvitationCardInternal() {
		if (invitationCard != null && defaultInvitationCardButton.getSelection()) {

			/* We have to save if we set ignoreModify to true, because of nested usage of ignoreModify
			 * we must not set it to false if the finally block if we did npt set it to true ourselve.
			 */
			boolean ignoreModifyChanged = false;

			try {
				if (modifySupport.isEnabled()) {
					modifySupport.setEnabled(false);
					ignoreModifyChanged = true;
				}

				boolean isStandardInvitationCard = defaultInvitationCardButton.getSelection();
				if (isStandardInvitationCard) {

    				/* set secondPerson
    				 * To get the secondPerson from the model and set it here is not elegant.
    				 * The alternative would be to let the model do the work. But this seems to be very
    				 * complex.
    				 * Since the data of the secondPerson is cached by the models, this approach is
    				 * fast enough.
    				 */
					Long secondPersonID = entity.getSecondPersonID();

					// TODO: No InvitationCardGeneratorModel yet
					InvitationCardGenerator salutationGenerator = InvitationCardGenerator.getInstance();

    				LanguageString salutations = null;
    				if (entity instanceof Participant) {

    					Long eventPK = ((Participant) entity).getEventId();
    					EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);

    					// copy all relevant values into widgetBasedParticipant
    					syncEntityToWidgets(widgetBasedParticipant);

    		    		// set 2nd person before generating address labels
    					Participant secondPerson = null;
    					if (secondPersonID != null) {
    						try {
								secondPerson = ParticipantModel.getInstance().getParticipant(secondPersonID);
							}
							catch (EntityNotFoundException e) {
								// ignore
							}
    					}
    					widgetBasedParticipant.setSecondPerson(secondPerson);

    					salutations = salutationGenerator.getInvitationCard(widgetBasedParticipant);
    					invitationCard.setLanguageString(salutations, eventVO.getLanguages());
    				}
    				else if (entity instanceof Profile) {
    					// copy all relevant values into widgetBasedProfile
    					syncEntityToWidgets(widgetBasedProfile);

    		    		// set 2nd person before generating address labels
    					Profile secondPerson = null;
    					if (secondPersonID != null) {
    						secondPerson = ProfileModel.getInstance().getProfile(secondPersonID);
    					}
    					widgetBasedProfile.setSecondPerson(secondPerson);

    					salutations = salutationGenerator.getInvitationCard(widgetBasedProfile);
    					invitationCard.setLanguageString(salutations);
    				}


    				invitationCard.getParent().layout();

				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
			finally {
				if (ignoreModifyChanged) {
					modifySupport.setEnabled(true);
				}
			}
		}
	}


	@Override
	public void syncEntityToWidgets() {
		syncEntityToWidgets(entity);
	}


	private void syncEntityToWidgets(Person person) {
		if (person != null) {
			person.setGender( getGender() );

			if (degree != null) {
				person.setDegree(StringHelper.trim(degree.getText()));
			}

			if (nobility != null) {
				person.setNobility(StringHelper.trim(nobility.getText()));
			}

			if (adminTitle != null) {
				person.setAdminTitle(StringHelper.trim(adminTitle.getText()));
			}

			if (firstName != null) {
				person.setFirstName(StringHelper.trim(firstName.getText()));
			}

			if (middleName != null) {
				person.setMiddleName(StringHelper.trim(middleName.getText()));
			}

			if (nobilityPrefix != null) {
				person.setNobilityPrefix(StringHelper.trim(nobilityPrefix.getText()));
			}

			person.setLastName(StringHelper.trim(lastName.getText()));

			if (mandate != null) {
				person.setMandate(StringHelper.trim(mandate.getText()));
			}

			if (functionText != null) {
				person.setFunction(StringHelper.trim(functionText.getText()));
			}

			if (salutation != null) {
				person.setIndividualSalutation(getSalutation());
			}

			if (invitationCard != null) {
				person.setIndividualInvitationCard(getInvitationCard());
			}

			if (dateOfBirth != null) {
				person.setDateOfBirth( dateOfBirth.getI18NDate() );
			}

			if(placeOfBirth != null) {
				person.setPlaceOfBirth(placeOfBirth.getText());
			}

			if (languageCombo != null) {
				person.setLanguageCode(languageCombo.getLanguageCode());
			}

			if (nationalityCombo != null) {
				person.setNationalityPK(nationalityCombo.getCountryCode());
			}

			if (passportID != null) {
				person.setPassportID(passportID.getText());
			}

			if (customerNoText != null) {
				person.setCustomerNo(customerNoText.getText());
			}

			if (customerAccountNumberText != null) {
				person.setCustomerAccountNumber(StringHelper.trim(customerAccountNumberText.getText()));
			}

			if (taxIdText != null) {
				person.setTaxId(StringHelper.trim(taxIdText.getText()));
			}

			if (cmeNoText != null) {
				person.setCmeNo(cmeNoText.getText());
			}
		}
	}


	/**
	 * Corrects the user input of admin title, first name, middle name,
	 * last name and function automatically.
	 */
	public void autoCorrection() {
		AutoCorrectionWidgetHelper.correctAndSet(adminTitle);

		if (firstName != null) {
			AutoCorrectionWidgetHelper.correctAndSet(firstName);
		}

		if (middleName != null) {
			AutoCorrectionWidgetHelper.correctAndSet(middleName);
		}

		AutoCorrectionWidgetHelper.correctAndSet(lastName);

		if (functionText != null) {
			AutoCorrectionWidgetHelper.correctAndSet(functionText);
		}
	}


	public void setPerson(Person person) {
		setEntity(person);
	}


	// **************************************************************************
	// * Getter
	// *

	public Gender getGender() {
		Gender gender = Gender.UNKNOWN;

		if (maleButton != null) {
    		if (maleButton.getSelection()) {
    			gender = Gender.MALE;
    		}
    		else if (femaleButton.getSelection()) {
    			gender = Gender.FEMALE;
    		}
    		else if (diverseButton.getSelection()) {
    			gender = Gender.DIVERSE;
    		}
    		else if (organizationButton.getSelection()) {
    			gender = Gender.ORGANIZATION;
    		}
		}

		return gender;
	}


	private LanguageString getSalutation() {
		LanguageString ls = null;
		if (salutation != null && ! defaultSalutationButton.getSelection()) {
			ls = salutation.getLanguageString();
			if (salutation.getLanguageString().isEmpty()) {
				ls = null;
			}
		}
		return ls;
	}


	private LanguageString getInvitationCard() {
		LanguageString ls = null;
		if (invitationCard != null && ! defaultInvitationCardButton.getSelection()) {
			ls = invitationCard.getLanguageString();
			if (invitationCard.getLanguageString().isEmpty()) {
				ls = null;
			}
		}
		return ls;
	}


	public boolean isComplete() {
		return
			   getGender() != null
			&& ! isEmpty(lastName.getText());
	}

	// *
	// * Getter
	// **************************************************************************


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
