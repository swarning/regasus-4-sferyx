package de.regasus.common.composite;

import static de.regasus.LookupService.getContactMgr;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.config.parameterset.AddressConfigParameterSet;
import com.lambdalogic.messeinfo.contact.AbstractPerson;
import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.contact.data.PostalCodeVO;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.EntityNotFoundException;
import com.lambdalogic.util.rcp.AutoCorrectionWidgetHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.AddressRole;
import de.regasus.common.AddressType;
import de.regasus.common.Country;
import de.regasus.common.country.combo.CountryCombo;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EntryOrSelectionDialog;
import de.regasus.core.ui.dnd.AddressTransfer;
import de.regasus.core.ui.dnd.CopyPasteButtonComposite;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.person.FunctionListModel;
import de.regasus.profile.ProfileModel;
import de.regasus.ui.Activator;

/**
 * Group for {@link Address}.
 * This class implements {@link ModifyListener} though it is not observing itself.
 * The interface is for accepting {@link ModifyEvent} from {@link AddressGroupsComposite}.
 * {@link AddressGroupsComposite} is observing {@link PersonGroup}. The connection between
 * {@link AddressGroupsComposite} and {@link PersonGroup} is created in editors (e.g.
 * {@link ParticipantEditor} and {@link PofileEditor}.
 */
public class AddressGroup extends Group implements ModifyListener {

	private AbstractPerson abstractPerson;

	private Address address;

	private String homeCountryPK;


	private ModifySupport modifySupport = new ModifySupport(this);


	// Models
	private FunctionListModel functionListModel = FunctionListModel.getInstance();


	// Widgets
	private Button mainAddressCheckbox;
	private Button invoiceAddressCheckbox;
	private Button businessButton;
	private Button privateButton;
	private MultiLineText organisation;
	private MultiLineText department;
	private MultiLineText addressee;
	private Text function;
	private MultiLineText street;
	private CountryCombo countryCombo;
	private Text zip;
	private Text city;
	private Text state;
	private Button standardButton;
	private MultiLineText addressLabel;
	private Button insertGroupManagerAddressButton;
	private Button openFunctionDialogButton;

	// Configuration
	private boolean showAddressRole = true;
	private boolean showOrganisation = true;
	private boolean showDepartment = true;
	private boolean showFunction = true;
	private boolean showState = true;
	private boolean showAddressee = true;

	private int addressNumber;

	private boolean isInvoiceAddress;

	private boolean isMainAddress;

	private Label countryLabel;
	private Label cityLabel;



	/**
	 * Create the composite
	 * @param groupManagerAddress2
	 */
	public AddressGroup(
		Composite parent,
		int style,
		AddressConfigParameterSet configParameterSet,
		final int addressNumber,
		final AddressGroupsComposite addressGroupsComposite,
		final Address groupManagerAddress
	)
	throws Exception {
		super(parent, style);

		if (configParameterSet != null) {
			this.showAddressRole = configParameterSet.getAddressRole().isVisible();
			this.showOrganisation = configParameterSet.getOrganisation().isVisible();
			this.showDepartment = configParameterSet.getDepartment().isVisible();
			this.showFunction = configParameterSet.getFunction().isVisible();
			this.showState = configParameterSet.getState().isVisible();
			this.showAddressee = configParameterSet.getAddressee().isVisible();
		}

		this.addressNumber = addressNumber;

		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		setLayout(gridLayout);

		// Address Role (Main and/or Invoice)

		// When used from wizard, there is one page for address role and no surrounding
		// addressGroupsComposite, in those cases the role checkbox can be disabled and we
		// don't need the callback
		if (addressGroupsComposite != null) {
			setText(ContactLabel.Address.getString() + " " + addressNumber);

			if (showAddressRole) {

				final Label addressRoleLabel = new Label(this, SWT.NONE);
				addressRoleLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				addressRoleLabel.setText(ContactLabel.AddressRole.getString());
				addressRoleLabel.setToolTipText(ContactLabel.AddressRole_description.getString());

				final Composite addressRoleComposite = new Composite(this, SWT.NONE);
				addressRoleComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 3, 1));
				final GridLayout gridLayout_0 = new GridLayout();
				gridLayout_0.numColumns = 2;
				gridLayout_0.marginWidth = 2;
				gridLayout_0.marginHeight = 2;
				addressRoleComposite.setLayout(gridLayout_0);

				mainAddressCheckbox = new Button(addressRoleComposite, SWT.CHECK);
				mainAddressCheckbox.setText(AddressRole.MAIN.getString());

				mainAddressCheckbox.addSelectionListener(modifySupport);

				mainAddressCheckbox.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						// Forbid the dis-checking
						if (! mainAddressCheckbox.getSelection()) {
							mainAddressCheckbox.setSelection(true);
						}
						else {
							addressGroupsComposite.setMainAddressGroup(addressNumber);
						}
					}
				});



				invoiceAddressCheckbox = new Button(addressRoleComposite, SWT.CHECK);
				invoiceAddressCheckbox.setText(AddressRole.INVOICE.getString());

				invoiceAddressCheckbox.addSelectionListener(modifySupport);

				invoiceAddressCheckbox.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						// Forbid the dis-checking
						if (! invoiceAddressCheckbox.getSelection()) {
							invoiceAddressCheckbox.setSelection(true);
						}
						else {
							addressGroupsComposite.setInvoiceAddressGroup(addressNumber);
						}
					}
				});
			}
		}

		// Address Type (Private/Business)
		final Label addressTypeLabel = new Label(this, SWT.NONE);
		addressTypeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		addressTypeLabel.setText( Address.ADDRESS_TYPE.getLabel() );
		addressTypeLabel.setToolTipText( Address.ADDRESS_TYPE.getDescription() );

		final Composite addressTypeComposite = new Composite(this, SWT.NONE);
		addressTypeComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 3, 1));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		gridLayout_1.marginWidth = 2;
		gridLayout_1.marginHeight = 2;
		addressTypeComposite.setLayout(gridLayout_1);

		// business
		businessButton = new Button(addressTypeComposite, SWT.RADIO);
		businessButton.setText(AddressType.BUSINESS.getString());

		businessButton.addSelectionListener(modifySupport);


		// private
		privateButton = new Button(addressTypeComposite, SWT.RADIO);
		privateButton.setText(AddressType.PRIVATE.getString());

		privateButton.addSelectionListener(modifySupport);


		// organisation
		if (showOrganisation) {
			final Label organisationLabel = new Label(this, SWT.NONE);
			final GridData gd_organisationLabel = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			gd_organisationLabel.verticalIndent = SWTConstants.VERTICAL_INDENT;
			organisationLabel.setLayoutData(gd_organisationLabel);
			organisationLabel.setText( Address.ORGANISATION.getLabel() );

			organisation = new MultiLineText(this, SWT.BORDER);
			organisation.setMinLineCount(1);
			final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			organisation.setLayoutData(gridData);
			organisation.setTextLimit( Address.ORGANISATION.getMaxLength() );

			organisation.addModifyListener(modifySupport);
		}

		if (showDepartment){
			final Label departmentLabel = new Label(this, SWT.NONE);
			final GridData gd_departmentLabel = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			gd_departmentLabel.verticalIndent = SWTConstants.VERTICAL_INDENT;
			departmentLabel.setLayoutData(gd_departmentLabel);
			departmentLabel.setText( Address.DEPARTMENT.getLabel() );

			department = new MultiLineText(this, SWT.BORDER);
			department.setMinLineCount(1);
			final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			department.setLayoutData(gridData);
			department.setTextLimit( Address.DEPARTMENT.getMaxLength() );

			department.addModifyListener(modifySupport);
		}


		if (showAddressee){
			final Label addresseeLabel = new Label(this, SWT.NONE);
			final GridData gd_addresseeLabel = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			gd_addresseeLabel.verticalIndent = SWTConstants.VERTICAL_INDENT;
			addresseeLabel.setLayoutData(gd_addresseeLabel);
			addresseeLabel.setText( Address.ADDRESSEE.getLabel() );
			addresseeLabel.setToolTipText( Address.ADDRESSEE.getDescription() );

			addressee = new MultiLineText(this, SWT.BORDER);
			addressee.setMinLineCount(1);
			final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			addressee.setLayoutData(gridData);
			addressee.setTextLimit( Address.ADDRESSEE.getMaxLength() );

			addressee.addModifyListener(modifySupport);
		}

		if (showFunction){
			final Label functionLabel = new Label(this, SWT.NONE);
			final GridData gd_functionLabel = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			gd_functionLabel.verticalIndent = SWTConstants.VERTICAL_INDENT;
			functionLabel.setLayoutData(gd_functionLabel);
			functionLabel.setText( Address.FUNCTION.getLabel() );
			functionLabel.setToolTipText( Address.FUNCTION.getDescription() );

			Composite functionComposite = new Composite(this, SWT.NONE);
       		functionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

       		GridLayout gridLayout2 = new GridLayout(2, false);
       		gridLayout2.marginWidth = 0;
       		gridLayout2.marginHeight = 0;
			functionComposite.setLayout(gridLayout2);

			function = new Text(functionComposite, SWT.BORDER);
			final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			function.setLayoutData(gridData);
			function.setTextLimit( Address.FUNCTION.getMaxLength() );

			function.addModifyListener(modifySupport);

			openFunctionDialogButton = new Button(functionComposite, SWT.PUSH);
    		openFunctionDialogButton.setText("...");
    		openFunctionDialogButton.addSelectionListener(new SelectionAdapter(){
    			@Override
    			public void widgetSelected(SelectionEvent e) {
    				chooseFunction();
    			}
    		});
    		openFunctionDialogButton.setLayoutData( new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		}

		{
			final Label streetLabel = new Label(this, SWT.NONE);
			final GridData gd_streetLabel = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			gd_streetLabel.verticalIndent = SWTConstants.VERTICAL_INDENT;
			streetLabel.setLayoutData(gd_streetLabel);
			streetLabel.setText( Address.STREET.getLabel() );
		}
		{
			street = new MultiLineText(this, SWT.BORDER);
			street.setMinLineCount(1);
			final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			street.setLayoutData(gridData);
			street.setTextLimit( Address.STREET.getMaxLength() );

			street.addModifyListener(modifySupport);
		}



		// **************************************************************************
		// * country
		// *

		countryLabel = new Label(this, SWT.NONE);
		final GridData gd_countryLabel = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		countryLabel.setLayoutData(gd_countryLabel);
		countryLabel.setText( Address.COUNTRY.getLabel() );
		countryLabel.setToolTipText( Address.COUNTRY.getDescription() );

		countryCombo = new CountryCombo(this, SWT.READ_ONLY);
		countryCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		countryCombo.addModifyListener(modifySupport);

		// *
		// * country
		// **************************************************************************

		// **************************************************************************
		// * zip
		// *

		final Label zipLabel = new Label(this, SWT.NONE);
		final GridData gd_zipLabel = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		zipLabel.setLayoutData(gd_zipLabel);
		zipLabel.setText( Address.ZIP.getLabel() );
		zipLabel.setToolTipText( Address.ZIP.getDescription() );

		zip = new Text(this, SWT.BORDER);
		final GridData gd_zip = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd_zip.widthHint = 50;
		zip.setLayoutData(gd_zip);
		zip.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent e) {
				final String countryPK = countryCombo.getCountryCode();
				final String zip = getZip();
				if (countryPK != null && zip != null) {
					List<PostalCodeVO> postalCodeVOs = getContactMgr().getPostalCodeVO(countryPK, zip);
					if (CollectionsHelper.notEmpty(postalCodeVOs)) {
						if (postalCodeVOs.size() == 1) {
							PostalCodeVO postalCodeVO = postalCodeVOs.get(0);

							String cityValue = postalCodeVO.getCity();
							city.setText(StringHelper.avoidNull(cityValue));
							city.selectAll();

							String stateValue = postalCodeVO.getState();
							state.setText(StringHelper.avoidNull(stateValue));
							state.selectAll();
						}
						else {
 							final CitySelectionDialog citySelectionDialog = new CitySelectionDialog(getShell(), postalCodeVOs);
 							final int result = citySelectionDialog.open();
 							if (result == 0) {
 								PostalCodeVO postalCodeVO = citySelectionDialog.getPostalCodeVO();
 								if (postalCodeVO != null) {
 									String cityValue = postalCodeVO.getCity();
 									city.setText(StringHelper.avoidNull(cityValue));

 									String stateValue = postalCodeVO.getState();
 									state.setText(StringHelper.avoidNull(stateValue));
 								}
 							}
							city.selectAll();
							state.selectAll();
						}
					}
				}
			}
		});
		zip.setTextLimit( Address.ZIP.getMaxLength() );

		zip.addModifyListener(modifySupport);

		// *
		// * zip
		// **************************************************************************

		// **************************************************************************
		// * city
		// *

		cityLabel = new Label(this, SWT.NONE);
		final GridData gd_cityLabel = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gd_cityLabel.horizontalIndent = 5;
		cityLabel.setLayoutData(gd_cityLabel);
		cityLabel.setText( Address.CITY.getLabel() );

		city = new Text(this, SWT.BORDER);
		final GridData gd_city = new GridData(SWT.FILL, SWT.CENTER, true, false);
		city.setLayoutData(gd_city);
		city.setTextLimit( Address.CITY.getMaxLength() );

		city.addModifyListener(modifySupport);

		// *
		// * city
		// **************************************************************************

		// **************************************************************************
		// * state
		// *

		if (showState){
			final Label stateLabel = new Label(this, SWT.NONE);
			final GridData gd_stateLabel = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			stateLabel.setLayoutData(gd_stateLabel);
			stateLabel.setText( Address.STATE.getLabel() );
			stateLabel.setToolTipText( Address.STATE.getDescription() );

			state = new Text(this, SWT.BORDER);
			final GridData gd_state = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
			state.setLayoutData(gd_state);
			state.setTextLimit( Address.STATE.getMaxLength() );

			state.addModifyListener(modifySupport);
		}

		// *
		// * state
		// **************************************************************************

		// **************************************************************************
		// * standard
		// *

		{
    		final Label addressLabelLabel = new Label(this, SWT.NONE);
    		final GridData gd_addressLabelLabel = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
    		addressLabelLabel.setLayoutData(gd_addressLabelLabel);
    		addressLabelLabel.setText( Address.LABEL.getString() );

    		standardButton = new Button(this, SWT.CHECK);
    		standardButton.setText(KernelLabel.Standard.getString());
    		standardButton.setToolTipText(I18N.AddressGroup_StandardAddressLabelToolTip);
    		standardButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
    		standardButton.addSelectionListener(new SelectionAdapter() {
    			@Override
    			public void widgetSelected(SelectionEvent e) {
    				if (modifySupport.isEnabled()) {
    					addressLabel.setEnabled( ! standardButton.getSelection());
    				}
    			}
    		});

    		standardButton.addSelectionListener(modifySupport);

    		// *
    		// * standard
    		// **************************************************************************

		}

		// **************************************************************************
		// * Label and Copy and Paste
		// *

		{
    		// Dummy label (MIRCP-2310 - Adressetikett im Layout eins nach rechts)
       		new Label(this, SWT.NONE);

       		Composite addressLabelAndCopyPasteButtonsComposite = new Composite(this, SWT.NONE);
       		addressLabelAndCopyPasteButtonsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

       		GridLayout gridLayout2 = new GridLayout(2, false);
       		gridLayout2.marginWidth = 0;
       		gridLayout2.marginHeight = 0;
			addressLabelAndCopyPasteButtonsComposite.setLayout(gridLayout2);

			addressLabel = new MultiLineText(addressLabelAndCopyPasteButtonsComposite, SWT.BORDER);
			addressLabel.setMinLineCount(5);
			final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
			addressLabel.setLayoutData(gridData);
			addressLabel.setTextLimit( Address.LABEL.getMaxLength() );

			addressLabel.addModifyListener(modifySupport);

    		CopyPasteButtonComposite cpbc = new CopyPasteButtonComposite(
    			addressLabelAndCopyPasteButtonsComposite,
    			SWT.NONE,
    			false // horizontal
    		);
    		cpbc.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
    		cpbc.getCopyButton().addSelectionListener(new SelectionAdapter() {
    			@Override
    			public void widgetSelected(SelectionEvent event) {
    				try {
    					copyToClipboad();
    				}
    				catch (Exception ex) {
    					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
    				}
    			}
    		});

    		cpbc.getPasteButton().addSelectionListener(new SelectionAdapter() {
    			@Override
    			public void widgetSelected(SelectionEvent e) {
    				try {
    					pasteFromClipboad();
    				}
    				catch (Exception e2) {
    					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e2);
    				}
    			}
    		});
		}

		// *
		// * Label and Copy and Paste
		// **************************************************************************



		if (groupManagerAddress != null) {
			insertGroupManagerAddressButton = new Button(this, SWT.PUSH);
			insertGroupManagerAddressButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));
			insertGroupManagerAddressButton.setText(I18N.AddressGroup_CopyAddressFromGroupManager);
			insertGroupManagerAddressButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					syncWidgetsToEntityInternal(groupManagerAddress, true);
				}
			});
		}


		modifySupport.addListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				refreshDefaultAddressLabel();
			}
		});
	}


	@Override
	public void modifyText(ModifyEvent e) {
		modifySupport.fire(e);
	}


	protected void pasteFromClipboad() {
		Clipboard clipboard = new Clipboard(Display.getDefault());

		Object contents = clipboard.getContents(AddressTransfer.getInstance());
		if (contents != null && contents instanceof Address) {
			Address pastedAddress = (Address) contents;
			syncWidgetsToEntityInternal(pastedAddress, true);
			modifySupport.fire();
		}
		clipboard.dispose();
	}


	protected void copyToClipboad() {
		Address addressToBeCopied = new Address();

		syncEntityToWidgetsInternal(addressToBeCopied);

		Clipboard clipboard = new Clipboard(Display.getDefault());
		String addressLabelText = addressLabel.getText();
		if (addressLabelText == null || addressLabelText.length() == 0) {
			addressLabelText = " ";
		}
		clipboard.setContents(
			new Object[] { addressToBeCopied, addressLabelText },
			new Transfer[] { AddressTransfer.getInstance(), TextTransfer.getInstance() }
		);
		clipboard.dispose();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
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
		String currentFunction = function.getText();
		if (currentFunction.trim().length() > 0 && ! functionList.contains(currentFunction)) {
			functionList.add(0, currentFunction);
		}

		// Prepare the dialog to select or enter a function
		EntryOrSelectionDialog listDialog = new EntryOrSelectionDialog(getShell());
		listDialog.setTitle( Person.FUNCTION.getString() );
		listDialog.setMessage(I18N.EnterOrSelectFunction);
		listDialog.setElements(functionList.toArray(new String[functionList.size()]));
		listDialog.setFilter(function.getText());
		int code = listDialog.open();
		if (code == Window.OK) {
			// Put the selected or entered function back to the text widget
			Object object = listDialog.getSelectionOrEntry();
			if (object != null && object instanceof String) {
				function.setText((String) object);
			}
		}
	}

	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	public void widgetDefaultSelected(SelectionEvent e) {
	}

	// *
	// * Modifying
	// **************************************************************************


	public void refreshDefaultAddressLabel() {
		SWTHelper.syncExecDisplayThread(
			new Runnable() {
				@Override
				public void run() {

            		try {
            			// refresh the addressLabel
            			if (standardButton.getSelection()) {
            				String label;
            				syncEntityToWidgets();

            				if (abstractPerson != null) {
                				/* set secondPerson
                				 * To get the secondPerson from the model and set it here is not elegant.
                				 * The alternative would be to let the model do the work. But this seems to be very
                				 * complex.
                				 * Since the data of the secondPerson is cached by the models, this approach is
                				 * fast enough.
                				 */
            					if (abstractPerson instanceof Participant) {
            						Participant participant = (Participant) abstractPerson;

            			    		// set 2nd person before generating address labels
            						Long secondPersonID = participant.getSecondPersonID();
            						Participant secondPerson = null;
            						if (secondPersonID != null) {
            							try {
            								secondPerson = ParticipantModel.getInstance().getParticipant(secondPersonID);
            							}
            							catch (EntityNotFoundException e) {
            								// ignore
            							}
            						}
            						participant.setSecondPerson(secondPerson);
            					}
            					else if (abstractPerson instanceof Profile) {
            						Profile profile = (Profile) abstractPerson;

            			    		// set 2nd person before generating address labels
            						Long secondPersonID = profile.getSecondPersonID();
            						Profile secondPerson = null;
            						if (secondPersonID != null) {
            							secondPerson = ProfileModel.getInstance().getProfile(secondPersonID);
            						}
            						profile.setSecondPerson(secondPerson);
            					}

            					label = abstractPerson.getAddressLabel(addressNumber, homeCountryPK, null);
            				}
            				else {
            					label = "";
            				}

            				modifySupport.setEnabled(false);
            				addressLabel.setText(label);
            				modifySupport.setEnabled(true);
            			}
            		}
            		catch (Exception e) {
            			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
            		}


				} // run()
			} // new Runnable()
		); // SWTHelper.syncExecDisplayThread(
	}


	private void syncWidgetsToEntity() {
		syncWidgetsToEntityInternal(address, true);
	}


	private void syncWidgetsToEntityInternal(final Address address, final boolean avoidEvents) {
		if (address != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						if (avoidEvents) {
							modifySupport.setEnabled(false);
						}


						// set main and invoice address
						if (abstractPerson != null) {
							setMainAddress(addressNumber == abstractPerson.getMainAddressNumber());
							setInvoiceAddress(addressNumber == abstractPerson.getInvoiceAddressNumber());
						}

						/*
						 * Programmatic selection of one radio button does not deselect other radio buttons, even
						 * when they are in the same group.
						 */
						AddressType addressType = address.getAddressType();
						if (addressType != null) {
							businessButton.setSelection(addressType == AddressType.BUSINESS);
							privateButton.setSelection(addressType == AddressType.PRIVATE);
						}

						if (showOrganisation) {
							organisation.setText(StringHelper.avoidNull(address.getOrganisation()));
						}
						if (showDepartment) {
							department.setText(StringHelper.avoidNull(address.getDepartment()));
						}
						if (showAddressee) {
							addressee.setText(StringHelper.avoidNull(address.getAddressee()));
						}
						if (showFunction) {
							function.setText(StringHelper.avoidNull(address.getFunction()));
						}

						street.setText(StringHelper.avoidNull(address.getStreet()));
						countryCombo.setCountryCode(address.getCountryPK());
						zip.setText(StringHelper.avoidNull(address.getZip()));
						city.setText(StringHelper.avoidNull(address.getCity()));
						if (showState) {
							state.setText(StringHelper.avoidNull(address.getState()));
						}

						final String label = address.getLabel();
						standardButton.setSelection(label == null);
						addressLabel.setEnabled(label != null);
						if (label == null) {
							refreshDefaultAddressLabel();
						}
						else {
							addressLabel.setText(StringHelper.avoidNull(label));
						}
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


	public void syncEntityToWidgets() {
		syncEntityToWidgetsInternal(address);
	}


	private void syncEntityToWidgetsInternal(Address address) {
		if (address != null) {
			if (mainAddressCheckbox != null && mainAddressCheckbox.getSelection()) {
				abstractPerson.setMainAddressNumber(addressNumber);
			}
			if (invoiceAddressCheckbox != null && invoiceAddressCheckbox.getSelection()) {
				abstractPerson.setInvoiceAddressNumber(addressNumber);
			}
			address.setAddressType(getAddressType());
			if (showOrganisation) {
				address.setOrganisation(getOrganisation());
			}
			if (showDepartment) {
				address.setDepartment(getDepartment());
			}
			if (showAddressee) {
				address.setAddressee(getAddressee());
			}
			if (showFunction) {
				address.setFunction(getFunction());
			}
			address.setStreet(getStreet());
			address.setCountryPK(countryCombo.getCountryCode());
			address.setZip(getZip());
			address.setCity(getCity());
			if (showState) {
				address.setState(getState());
			}
			address.setLabel(getLabel());
		}
	}


	public void setAddress(Address address) {
		this.address = address;

		syncWidgetsToEntity();
	}


	public String getHomeCountryPK() {
		return homeCountryPK;
	}


	public void setHomeCountryPK(String homeCountryPK) {
		this.homeCountryPK = homeCountryPK;
	}


	public void setAbstractPerson(AbstractPerson abstractPerson) {
		this.abstractPerson = abstractPerson;

		this.address = abstractPerson.getAddress(addressNumber);

		syncWidgetsToEntity();
	}


	@Override
	public void setEnabled(boolean enabled) {
		if (mainAddressCheckbox != null) {
			mainAddressCheckbox.setEnabled(enabled);
		}
		if (invoiceAddressCheckbox != null) {
			invoiceAddressCheckbox.setEnabled(enabled);
		}
		businessButton.setEnabled(enabled);
		privateButton.setEnabled(enabled);
		if (showOrganisation) {
			organisation.setEnabled(enabled);
		}
		if (showDepartment) {
			department.setEnabled(enabled);
		}
		if (showAddressee) {
			addressee.setEnabled(enabled);
		}
		if (showFunction) {
			function.setEnabled(enabled);
		}
		street.setEnabled(enabled);
		countryCombo.setEnabled(enabled);
		zip.setEnabled(enabled);
		city.setEnabled(enabled);
		if (showState) {
			state.setEnabled(enabled);
		}
		standardButton.setEnabled(enabled);
		addressLabel.setEnabled(enabled);
	}


	/**
	 * Corrects the user input of organization, address, street,
	 * city and state automatically.
	 */
	public void autoCorrection() {
		if (showOrganisation) {
			AutoCorrectionWidgetHelper.correctAndSet(organisation);
		}
		if (showDepartment) {
			AutoCorrectionWidgetHelper.correctAndSet(department);
		}
		if (showAddressee) {
			AutoCorrectionWidgetHelper.correctAndSet(addressee);
		}
		if (showFunction) {
			AutoCorrectionWidgetHelper.correctAndSet(function);
		}
		AutoCorrectionWidgetHelper.correctAndSet(street);
		AutoCorrectionWidgetHelper.correctAndSet(city);
		if (showState) {
			AutoCorrectionWidgetHelper.correctAndSet(state);
		}
	}


	// **************************************************************************
	// * Getter
	// *

	public AddressType getAddressType() {
		AddressType addressType = null;
		if (businessButton.getSelection()) {
			addressType = AddressType.BUSINESS;
		}
		else if (privateButton.getSelection()) {
			addressType = AddressType.PRIVATE;
		}
		return addressType;
	}


	public String getOrganisation() {
		return StringHelper.trim(organisation.getText());
	}

	public String getDepartment() {
		return StringHelper.trim(department.getText());
	}

	public String getAddressee() {
		return StringHelper.trim(addressee.getText());
	}

	public String getFunction() {
		return StringHelper.trim(function.getText());
	}

	public String getStreet() {
		return StringHelper.trim(street.getText());
	}


    public LanguageString getCountryName() {
    	LanguageString countryName = null;
		Country country = countryCombo.getEntity();
		if (country != null) {
			countryName = country.getName();
		}
		return countryName;
    }


    public Country getCountry() {
		Country country = countryCombo.getEntity();
		return country;
    }


    public String getZip() {
		return StringHelper.trim(zip.getText());
	}


	public String getCity() {
		return StringHelper.trim(city.getText());
	}


	public String getState() {
		return StringHelper.trim(state.getText());
	}


	public String getLabel() {
		String s = null;
		if ( ! standardButton.getSelection()) {
			s = StringHelper.trim(addressLabel.getText());
		}
		return s;
	}

	// *
	// * Getter
	// **************************************************************************

	// **************************************************************************
	// * AddressRole
	// *

	public void setMainAddress(boolean b) {
		isMainAddress = b;

		if (mainAddressCheckbox != null) {
			SWTHelper.asyncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					mainAddressCheckbox.setSelection(isMainAddress);
					adjustTitle();
				}
			});
		}
	}


	public void setInvoiceAddress(boolean b) {
		isInvoiceAddress = b;

		if (invoiceAddressCheckbox != null) {
			SWTHelper.asyncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					invoiceAddressCheckbox.setSelection(isInvoiceAddress);
					adjustTitle();
				}
			});
		}
	}


	/**
	 * Adjust the title of this group to one of the following.
	 * <ul>
	 * <li>Adresse n</li>
	 * <li>Adresse n (Hauptadresse)</li>
	 * <li>Adresse n (Rechnungsadresse)</li>
	 * <li>Adresse n (Haupt- und Rechnungsadresse)</li>
	 * </ul>
	 */
	private void adjustTitle() {
		final String groupLabel = AddressRole.getAddressRoleName(
			addressNumber,
			isMainAddress,
			isInvoiceAddress
		);

		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				setText(groupLabel);
			}
		});
	}


	public void setCountryBold(boolean bold) {
		Combo combo = countryCombo.getCombo();
		SWTHelper.setBold(countryLabel, bold);
		SWTHelper.setBold(combo, bold);
	}


	public void setCityBold(boolean bold) {
		SWTHelper.setBold(cityLabel, bold);
		SWTHelper.setBold(city, bold);
	}

}
