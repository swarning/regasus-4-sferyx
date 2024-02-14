package de.regasus.view;

import static com.lambdalogic.util.StringHelper.avoidNull;
import static de.regasus.LookupService.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.google.zxing.QREncoder;
import com.lambdalogic.messeinfo.contact.data.NoteCVO;
import com.lambdalogic.messeinfo.email.AttachmentContainer;
import com.lambdalogic.messeinfo.email.EmailConstants;
import com.lambdalogic.messeinfo.email.EmailDispatchService;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingCVOSettings;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.InvoiceCVO;
import com.lambdalogic.messeinfo.invoice.interfaces.InvoiceCVOSettings;
import com.lambdalogic.messeinfo.invoice.interfaces.InvoicePositionCVOSettings;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.BadgeVO;
import com.lambdalogic.messeinfo.participant.data.ParticipantCVO;
import com.lambdalogic.messeinfo.participant.data.ParticipantCVOSettings;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammeBookingCVOSettings;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammeOfferingCVOSettings;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammePointCVOSettings;
import com.lambdalogic.report.script.ScriptContext;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.html.BrowserFactory;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.LookupService;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.ServerModelEvent;
import de.regasus.core.model.ServerModelEventType;
import de.regasus.ui.Activator;
import de.regasus.view.pref.GroovyConsoleViewPreference;

public class GroovyConsoleView extends ViewPart {

	public static final String ID = "GroovyConsoleView";

	private GroovyConsoleViewPreference preference;


	private Text idText;
	private Text groovyScriptText;
	private Text variablesText;
	private Text resultText;
	private Browser browser;
	private Combo combo;


	public GroovyConsoleView() {
		preference = GroovyConsoleViewPreference.getInstance();
	}


	@Override
	public void createPartControl(Composite parent) {

		if ("admin".equals(ServerModel.getInstance().getUser())) {

			GridLayout gridLayout = new GridLayout(4, false);
			parent.setLayout(gridLayout);

			combo = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
			combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			combo.setItems(new String[]{
				EmailLabel.Email.getString(),
				InvoiceLabel.Invoice.getString(),
				ParticipantLabel.Notes.getString(),
				ParticipantLabel.Badge.getString()
			});


			idText = SWTHelper.createLabelAndText(parent, UtilI18N.ID);

			Button button = new Button(parent, SWT.PUSH);
			button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			button.setText(I18N.Evaluate);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					onEvaluateButtonSelected();
				}
			});

			Label label = new Label(parent, SWT.NONE);
			label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1));
			label.setText(I18N.Script_PressCtrlEnterToEvaluate);

			SashForm sashForm = new SashForm(parent, SWT.VERTICAL);

			groovyScriptText = new Text(sashForm, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
			KeyAdapter evaluationKeyAdapter = new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.keyCode == '\r' && (e.stateMask & SWT.CTRL) != 0) {

						onEvaluateButtonSelected();
						e.doit = false;
					}
				}
			};
			groovyScriptText.addKeyListener(evaluationKeyAdapter);

			Group group = new Group(sashForm, SWT.NONE);
			group.setText(I18N.VariablesAndResults);
			group.setLayout(new FillLayout(SWT.HORIZONTAL));

			variablesText = new Text(group, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
			variablesText.addKeyListener(evaluationKeyAdapter);

			resultText = new Text(group, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);

			browser = BrowserFactory.createBrowser(group, SWT.BORDER);

			sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
			sashForm.setWeights(new int[] { 2, 1 });

			initFromPreferences();

			// observer ServerModel to init from preferences on login and save t preferences on logout
			ServerModel.getInstance().addListener(serverModelListener);
		}
	}


	@Override
	public void dispose() {
		try {
			ServerModel.getInstance().removeListener(serverModelListener);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		super.dispose();
	}


	protected void onEvaluateButtonSelected() {

		ScriptContext scriptContext =  new ScriptContext("DE");


		if (combo.getSelectionIndex() == 0) {
			if (! prepareForEmail(scriptContext)) {
				return;
			}
		}
		else if (combo.getSelectionIndex() == 1) {
			if (! prepareForInvoice(scriptContext)) {
				return;
			}
		}
		else if (combo.getSelectionIndex() == 2) {
			if (! prepareForNote(scriptContext)) {
				return;
			}
		}
		else if (combo.getSelectionIndex() == 3) {
			if (! prepareForBadge(scriptContext)) {
				return;
			}
		}


		try {
			scriptContext.addGroovyScript(groovyScriptText.getText());
			scriptContext.runGroovyScripts();

			String variables = variablesText.getText();
			String evaluationResult = scriptContext.evaluateString(variables.replace("${", "&{"));
			resultText.setText(evaluationResult);
			browser.setText(evaluationResult);
		}
		catch (Exception e) {
			resultText.setText(e.toString());
			browser.setText("");
		}
	}


	private boolean prepareForEmail(ScriptContext scriptContext) {
		ParticipantCVO participantCVO = null;

		if (! idText.getText().isEmpty()) {
			try {
				Long participantID = Long.valueOf(idText.getText());
				participantCVO = getParticipantMgr().getParticipantCVO(participantID, EmailDispatchService.PARTICIPANT_SETTINGS);

				scriptContext.setVariable("p", participantCVO);
				scriptContext.setVariable(EmailConstants.EMAIL_FIELD_QRENCODER, QREncoder.getInstance());

				// add LookupService to ScriptContext
				LookupService lookupService = new LookupService();
				scriptContext.setVariable(LookupService.LOOKUP_SERVICE, lookupService);
				scriptContext.setVariable(LookupService.LOAD_HELPER, lookupService);

				AttachmentContainer attachmentContainer = new AttachmentContainer();
				scriptContext.setVariable(EmailConstants.EMAIL_FIELD_ATTACHMENTS, attachmentContainer);

				return true;
			}
			catch (Exception e) {
				resultText.setText(e.toString());
				browser.setText("");
			}
		}
		return false;
	}


	private boolean prepareForNote(ScriptContext scriptContext) {
		NoteCVO noteCVO = null;

		if (! idText.getText().isEmpty()) {
			try {
				Long participantID = Long.valueOf(idText.getText());
				noteCVO = getNoteMgr().getNoteCVOs(participantID);

				scriptContext.setVariable("data", noteCVO);
				scriptContext.setVariable(EmailConstants.EMAIL_FIELD_QRENCODER, QREncoder.getInstance());

				// add LookupService to ScriptContext
				LookupService lookupService = new LookupService();
				scriptContext.setVariable(LookupService.LOOKUP_SERVICE, lookupService);
				scriptContext.setVariable(LookupService.LOAD_HELPER, lookupService);

				return true;
			}
			catch (Exception e) {
				resultText.setText(e.toString());
				browser.setText("");
			}
		}
		return false;
	}


	private boolean prepareForBadge(ScriptContext scriptContext) {
		BadgeVO badgeVO = null;

		if (! idText.getText().isEmpty()) {
			try {
				Long participantID = Long.valueOf(idText.getText());
				badgeVO = getBadgeMgr().getBadgeVOByParticipantPK(participantID);

				scriptContext.setVariable("data", badgeVO);
				scriptContext.setVariable(EmailConstants.EMAIL_FIELD_QRENCODER, QREncoder.getInstance());

				// add LookupService to ScriptContext
				LookupService lookupService = new LookupService();
				scriptContext.setVariable(LookupService.LOOKUP_SERVICE, lookupService);
				scriptContext.setVariable(LookupService.LOAD_HELPER, lookupService);

				return true;
			}
			catch (Exception e) {
				resultText.setText(e.toString());
				browser.setText("");
			}
		}
		return false;
	}


	private boolean prepareForInvoice(ScriptContext scriptContext) {
		InvoiceCVO invoiceCVO = null;

		if (! idText.getText().isEmpty()) {
			try {
				Long invoiceID = Long.valueOf(idText.getText());
				invoiceCVO = getInvoiceMgr().getInvoiceCVO(invoiceID, INVOICE_CVO_SETTINGS_FOR_DOCUMENTS);

				scriptContext.setVariable("data", invoiceCVO);
				scriptContext.setVariable(EmailConstants.EMAIL_FIELD_QRENCODER, QREncoder.getInstance());

				// add LookupService to ScriptContext
				LookupService lookupService = new LookupService();
				scriptContext.setVariable(LookupService.LOOKUP_SERVICE, lookupService);
				scriptContext.setVariable(LookupService.LOAD_HELPER, lookupService);
			}
			catch (Exception e) {
				resultText.setText(e.toString());
				browser.setText("");
				return false;
			}
		}

		return true;
	}


	@Override
	public void setFocus() {
		idText.setFocus();
	}


	public static final InvoiceCVOSettings INVOICE_CVO_SETTINGS_FOR_DOCUMENTS;
	static {
		ProgrammePointCVOSettings programmePointCVOSettings = new ProgrammePointCVOSettings();
		ProgrammeOfferingCVOSettings programmeOfferingCVOSettings = new ProgrammeOfferingCVOSettings(programmePointCVOSettings);
		ProgrammeBookingCVOSettings programmeBookingCVOSettings = new ProgrammeBookingCVOSettings(programmeOfferingCVOSettings);
		programmeBookingCVOSettings.recipientCVOSettings = new ParticipantCVOSettings();
		programmeOfferingCVOSettings.withParticipantTypeName = true;

		HotelContingentCVOSettings hotelContingentCVOSettings = new HotelContingentCVOSettings();
		hotelContingentCVOSettings.hotelCVOSettings = new HotelCVOSettings();
		HotelOfferingCVOSettings hotelOfferingCVOSettings = new HotelOfferingCVOSettings(hotelContingentCVOSettings);
		HotelBookingCVOSettings hotelBookingCVOSettings = new HotelBookingCVOSettings(hotelOfferingCVOSettings);
		hotelBookingCVOSettings.withHotelName = true;
		hotelBookingCVOSettings.recipientCVOSettings = new ParticipantCVOSettings();
		hotelBookingCVOSettings.withRoomDefinition = true;

		InvoicePositionCVOSettings invoicePositionCVOSettings = new InvoicePositionCVOSettings();
		invoicePositionCVOSettings.programmeBookingCVOSettings = programmeBookingCVOSettings;
		invoicePositionCVOSettings.hotelBookingCVOSettings = hotelBookingCVOSettings;
		invoicePositionCVOSettings.withRelatedInvoiceVO = true;

		INVOICE_CVO_SETTINGS_FOR_DOCUMENTS = new InvoiceCVOSettings(invoicePositionCVOSettings);
		INVOICE_CVO_SETTINGS_FOR_DOCUMENTS.withAllInvoices = true;
		INVOICE_CVO_SETTINGS_FOR_DOCUMENTS.withAllPayments = true;
		INVOICE_CVO_SETTINGS_FOR_DOCUMENTS.programmeBookingCVOSettings = programmeBookingCVOSettings;
		INVOICE_CVO_SETTINGS_FOR_DOCUMENTS.hotelBookingCVOSettings = hotelBookingCVOSettings;
		INVOICE_CVO_SETTINGS_FOR_DOCUMENTS.withEventInfo = true;
		INVOICE_CVO_SETTINGS_FOR_DOCUMENTS.withInvoicePositions = true;
		INVOICE_CVO_SETTINGS_FOR_DOCUMENTS.withRecipientInfo = true;
		INVOICE_CVO_SETTINGS_FOR_DOCUMENTS.withTemplateDataStoreVO = true;
	}


	// *****************************************************************************************************************
	// * Preferences
	// *

	private ModelListener serverModelListener = new ModelListener() {
		@Override
		public void dataChange(ModelEvent event) {
			ServerModelEvent serverModelEvent = (ServerModelEvent) event;
			if (serverModelEvent.getType() == ServerModelEventType.BEFORE_LOGOUT) {
				// save values to preferences before the logout will remove them
				savePreferences();
			}
			else if (serverModelEvent.getType() == ServerModelEventType.LOGIN) {
				SWTHelper.asyncExecDisplayThread(new Runnable() {
					@Override
					public void run() {
						initFromPreferences();
					}
				});
			}
		}
	};


	private void savePreferences() {
		preference.setId( idText.getText() );
		preference.setScript( groovyScriptText.getText() );
		preference.setVariables( variablesText.getText() );

		preference.save();
	}


	private void initFromPreferences() {
		try {
    		// eventFilter
			idText.setText( avoidNull(preference.getId()) );
			groovyScriptText.setText( avoidNull(preference.getScript()) );
			variablesText.setText( avoidNull(preference.getVariables()) );
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	// *
	// * Preferences
	// *****************************************************************************************************************

}
