package de.regasus.email.template.search.view;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;
import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.contact.data.SimplePersonSearchData;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailPropertyKey;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateComparator;
import com.lambdalogic.messeinfo.email.EmailTemplateProfileHelper;
import com.lambdalogic.messeinfo.email.EmailTemplateRegistrationFormHelper;
import com.lambdalogic.messeinfo.email.SmtpSettingsVOHelper;
import com.lambdalogic.messeinfo.email.data.SmtpSettingsVO;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.EditorHelper;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.IconRegistry;
import de.regasus.common.Language;
import de.regasus.common.Property;
import de.regasus.core.LanguageModel;
import de.regasus.core.PropertyModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailI18N;
import de.regasus.email.EmailTemplateModel;
import de.regasus.email.dispatch.dialog.SmtpSettingsWizard;
import de.regasus.email.template.EmailTemplateSearchTable;
import de.regasus.email.template.SampleRecipientModel;
import de.regasus.event.EventModel;
import de.regasus.event.combo.EventCombo;
import de.regasus.onlineform.RegistrationFormConfigModel;
import de.regasus.onlineform.editor.RegistrationFormConfigEditor;
import de.regasus.onlineform.editor.RegistrationFormConfigEditorInput;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.search.OneParticipantSelectionDialogConfig;
import de.regasus.participant.search.ParticipantSelectionDialog;
import de.regasus.profile.ProfileModel;
import de.regasus.profile.search.OneProfileSelectionDialogConfig;
import de.regasus.profile.search.ProfileSelectionDialog;
import de.regasus.ui.Activator;

/**
 * A composite that shows an {@link EventCombo} and a table for {@link EmailTemplate} for that event. When the
 * event is changed with the combo, the table shows a different list of entities.
 *
 * Also contained is a button to select a sample recipient for the email templates of that event.
 */
public class EmailTemplateSearchComposite extends Composite implements DisposeListener {

	// Models
	private EmailTemplateModel emailTemplateModel = EmailTemplateModel.getInstance();
	private EventModel eventModel = EventModel.getInstance();
	private RegistrationFormConfigModel registrationFormConfigModel = RegistrationFormConfigModel.getInstance();

	// data
	private Long eventPK;
	private List<EmailTemplate> emailTemplateSearchDataList;
	private SimplePersonSearchData personSearchData;

	private boolean ignoreEmailTemplateEvents = false;
	private boolean ignoreRegistrationFormConfigEvents = false;


	// **************************************************************************
	// * Widgets
	// *

	private EventCombo eventCombo;
	private Text sampleRecipientLastNameText;
	private Button selectSampleRecipientButton;
	private Button smtpSettingsButton;
	private Text smtpHostText;
	private Label warningsLabel;
	private Link warningsLink;
	private EmailTemplateSearchTable emailTemplateTable;

	// *
	// * Widgets
	// **************************************************************************



	public EmailTemplateSearchComposite(Composite parent, int style) {
		super(parent, style);

		addDisposeListener(this);

		try {
			setLayout(new GridLayout(3, false));

			// The Event
			final Label eventLabel = new Label(this, SWT.NONE);
			eventLabel.setText(ParticipantLabel.Event.getString());

			eventCombo = new EventCombo(this, SWT.NONE);
			eventCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			eventCombo.setKeepEntityInList(false);
			eventCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent modifyEvent) {
					try {
    					Long eventPK = eventCombo.getEventPK();
    					setEventPK(eventPK);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});

			// The SMTP Server
			final Label hostLabel = new Label(this, SWT.NONE);
			hostLabel.setText(EmailLabel.SmtpSettings.getString());

			smtpHostText = new Text(this, SWT.BORDER);
			smtpHostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			SWTHelper.disableTextWidget(smtpHostText);

			smtpSettingsButton = new Button(this, SWT.PUSH);
			smtpSettingsButton.setText(UtilI18N.Ellipsis);
			smtpSettingsButton.setToolTipText(EmailI18N.EmailAndSmtpSettings_ToolTip);

			smtpSettingsButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selectSmtpSettings();
				}
			});

			// The Sample Recipient
			final Label sampleRecipientLabel = new Label(this, SWT.NONE);
			sampleRecipientLabel.setText(EmailLabel.SampleRecipient.getString());

			sampleRecipientLastNameText = new Text(this, SWT.BORDER);
			sampleRecipientLastNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			sampleRecipientLastNameText.setEditable(false);

			selectSampleRecipientButton = new Button(this, SWT.PUSH);
			selectSampleRecipientButton.setText(UtilI18N.Ellipsis);
			selectSampleRecipientButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selectSamplePerson();
				}
			});

			// Possible warnings
			Composite warningsComposite = new Composite(this, SWT.NONE);
			warningsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
			warningsComposite.setLayout(new GridLayout(2, false));

			warningsLabel = new Label(warningsComposite, SWT.NONE);
			warningsLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

			warningsLink = new Link(warningsComposite, SWT.NONE);
			warningsLink.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			warningsLink.addListener (SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					onLinkClicked(event.text);
				}
			});

			// The Table

			// Intermediate Composite to make the TableColumnLayout work
			Composite tableComposite = new Composite(this, SWT.BORDER);
			tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			TableColumnLayout layout = new TableColumnLayout();
			tableComposite.setLayout(layout);

			final Table table = new Table(tableComposite, SelectionMode.MULTI_SELECTION.getSwtStyle());
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(nameTableColumn, new ColumnWeightData(100));
			nameTableColumn.setText(EmailLabel.EmailTemplate.getString());

			final TableColumn languageTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(languageTableColumn, new ColumnWeightData(25));
			languageTableColumn.setText( Person.LANGUAGE_CODE.getLabel() );

			final TableColumn webidTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(webidTableColumn, new ColumnWeightData(25));
			webidTableColumn.setText(EmailI18N.WebId);

			emailTemplateTable = new EmailTemplateSearchTable(table);

			emailTemplateModel.addListener(emailTemplateModelListener);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * When there is a warning shown that for some online forms there is no email, the form's webId is
	 * shown as link, and the user shall be able to open the form's editor by clicking on the link.
	 */
	protected void onLinkClicked(String webId) {
		try {
			ignoreRegistrationFormConfigEvents = true;
			Long eventPK = getEventPK();
			List<RegistrationFormConfig> configs = registrationFormConfigModel.getRegistrationFormConfigsByEventPK(eventPK);
			for (RegistrationFormConfig config : configs) {
				if ( config.getWebId().equals(webId) ) {
					RegistrationFormConfigEditorInput editorInput = new RegistrationFormConfigEditorInput(config);
					EditorHelper.openEditor(editorInput, RegistrationFormConfigEditor.ID);
					return;
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			ignoreRegistrationFormConfigEvents = false;
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (emailTemplateModel != null) {
			emailTemplateModel.removeListener(emailTemplateModelListener);
		}

		if (eventPK != null) {
			eventModel.removeListener(eventModelListener, eventPK);
			registrationFormConfigModel.removeForeignKeyListener(registrationFomConfigModelListener, eventPK);
		}
	}


	private void selectSmtpSettings() {
		try {
			SmtpSettingsVO settings = null;
			EventVO eventVO = null;
			List<Property> originalPropertyList = null;
			SmtpSettingsVOHelper smtpSettingsVOHelper = null;

			// Find which settings to show, depending on whether an event is selected
			Long eventPK = getEventPK();
			if (eventPK != null) {
				eventVO = EventModel.getInstance().getEventVO(eventPK);
				settings = new SmtpSettingsVO();
				settings.copyFrom(eventVO.getSmtpSettingsVO());
			}
			else {
				originalPropertyList = PropertyModel.getInstance().getPublicPropertyList();

				/* clone all Property elements
				 * Necessary because we must not change the original Property objects of the model.
				 * Otherwise the model cannot determine Property objects that have been changed.
				 */
				originalPropertyList = originalPropertyList.stream().map(o -> o.clone()).collect(Collectors.toList());

				smtpSettingsVOHelper = new SmtpSettingsVOHelper(originalPropertyList);
				settings = smtpSettingsVOHelper.createFromProperties();
			}

			// Open wizard to store
			SmtpSettingsWizard wizard = new SmtpSettingsWizard(settings, eventVO);
			WizardDialog dialog = new WizardDialog(getShell(), wizard);
			dialog.create();
			dialog.getShell().setSize(500, 500);
			int code = dialog.open();
			if (code == Window.OK) {
				smtpHostText.setText(settings.getHost());

				if (eventPK != null) {
					eventVO.setSmtpSettingsVO(settings);
					EventModel.getInstance().update(eventVO);

				}
				else {
					Collection<Property> editedPropertyList = smtpSettingsVOHelper.setInPropertyVOs(settings);
					PropertyModel.getInstance().update(editedPropertyList);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

	}


	private void selectSamplePerson() {
		try {
			Long eventPK = eventCombo.getEventPK();

			if (eventPK != null) {
				selectSampleParticipant(eventPK);
			}
			else {
				selectSampleProfile();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void selectSampleProfile() {
		ProfileSelectionDialog dialog = new ProfileSelectionDialog(getShell(), OneProfileSelectionDialogConfig.INSTANCE);
		int result = dialog.open();
		if (result == Window.OK) {
			Profile profile = dialog.getSelectedProfiles().get(0);

			personSearchData = new SimplePersonSearchData();
			personSearchData.setId( profile.getID() );
			personSearchData.setName( profile.getName() );

			sampleRecipientLastNameText.setText(personSearchData.getName());
			SampleRecipientModel.INSTANCE.put(null, personSearchData);
		}
	}


	private void selectSampleParticipant(Long eventPK) {
		ParticipantSelectionDialog dialog = new ParticipantSelectionDialog(
			getShell(),
			OneParticipantSelectionDialogConfig.INSTANCE,
			eventPK
		);
		int result = dialog.open();
		if (result == Window.OK) {
			ParticipantSearchData participant = dialog.getSelectedParticipants().get(0);

			personSearchData = new SimplePersonSearchData();
			personSearchData.setName(participant.getName());
			personSearchData.setId(participant.getPK());
			personSearchData.setEventID(participant.getEventId());

			sampleRecipientLastNameText.setText(personSearchData.getName());
			SampleRecipientModel.INSTANCE.put(eventPK, personSearchData);
		}
	}


	public TableViewer getTableViewer() {
		return emailTemplateTable.getViewer();
	}


	public Long getEventPK() {
		return eventCombo.getEventPK();
	}


	public void setEventPK(Long eventPK) throws Exception {
		final Long oldEventPK = this.eventPK;
		final Long newEventPK = eventPK;

		if ( ! EqualsHelper.isEqual(newEventPK, oldEventPK) ) {
    		if (oldEventPK != null) {
    			eventModel.removeListener(eventModelListener, oldEventPK);
    			registrationFormConfigModel.removeForeignKeyListener(registrationFomConfigModelListener, oldEventPK);
    		}

    		// Event
    		eventCombo.setEventPK(newEventPK);

    		refreshEmailTemplates();
    		refreshSamplePerson();
    		refreshSmtpSettings();
    		refreshWarnings();

    		// We need to know when eg configs change their webId, so that we can update warnings
    		if (newEventPK != null) {
    			eventModel.addListener(eventModelListener, newEventPK);
    			registrationFormConfigModel.addForeignKeyListener(registrationFomConfigModelListener, newEventPK);
    		}
		}

		this.eventPK = newEventPK;
	}


	public String getEventFilter() {
		return eventCombo.getFilter();
	}


	public void setEventFilter(String filter) {
		eventCombo.setFilter(filter);
	}


	private CacheModelListener<Long> emailTemplateModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (!ignoreEmailTemplateEvents) {
        		refreshEmailTemplates();
        		refreshWarnings();
			}
		}
	};


	private CacheModelListener<Long> eventModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (   event.getOperation() == CacheModelOperation.UPDATE
				|| event.getOperation() == CacheModelOperation.REFRESH
			) {
				refreshSmtpSettings();
			}
		}
	};


	private CacheModelListener<Long> registrationFomConfigModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (!ignoreRegistrationFormConfigEvents) {
    			refreshWarnings();
			}
		}
	};


	private void refreshEmailTemplates() throws Exception {
		try {
			ignoreEmailTemplateEvents = true;

    		Long eventPK = getEventPK();

    		// update Email Templates
    		emailTemplateSearchDataList = emailTemplateModel.getEmailTemplateSearchDataByEvent(eventPK);

    		// sort by language and name (sorting by 2 columns is not supported by SimpleTable)
    		emailTemplateSearchDataList = createArrayList(emailTemplateSearchDataList);
    		Collections.sort(emailTemplateSearchDataList, EmailTemplateComparator.getInstance());
		}
		finally {
			ignoreEmailTemplateEvents = false;
		}

		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					emailTemplateTable.setInput(emailTemplateSearchDataList);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});

	}


	private void refreshSamplePerson() throws Exception {
		Long eventPK = getEventPK();

		String samplePersonName = "";

		personSearchData = SampleRecipientModel.INSTANCE.getSimplePersonSearchData(eventPK);
		if (personSearchData != null) {
			if ( exists(personSearchData) ) {
				samplePersonName = personSearchData.getName();
			}
			else {
				SampleRecipientModel.INSTANCE.removeSampleRecipient(eventPK);
			}
		}


		final String finalSamplePersonName = avoidNull(samplePersonName);
		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					sampleRecipientLastNameText.setText(finalSamplePersonName);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});

	}


	private void refreshSmtpSettings() throws Exception {
		Long eventPK = getEventPK();

		String host = null;
		if (eventPK != null) {
			try {
				EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
				if (eventVO != null) {
					host = eventVO.getSmtpSettingsVO().getHost();
				}
			}
			catch (Exception e) {
				// This is presumably because the given event doesn't exist in the current DB
				System.err.println(e);
			}
		}
		else {
			host = PropertyModel.getInstance().getPropertyValue(EmailPropertyKey.SMTP_HOST);
		}


		final String finalHost = avoidNull(host);
		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					smtpHostText.setText(finalHost);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});

	}


	private void refreshWarnings() throws Exception {
		Long eventPK = getEventPK();
		List<Language> languageList = LanguageModel.getInstance().getAllUndeletedLanguages();

		StringBuilder sb = new StringBuilder();
		if (eventPK != null) {
			try {
				ignoreRegistrationFormConfigEvents = true;

    			// Validate the presence of registration confirmation mails etc per online form
    			List<RegistrationFormConfig> formConfigList = registrationFormConfigModel.getRegistrationFormConfigsByEventPK(eventPK);

    			List<String> warnings = EmailTemplateRegistrationFormHelper.validateEmailTemplateConfiguration(
    				emailTemplateSearchDataList,
    				formConfigList,
    				languageList
    			);

    			for (String warning: warnings) {
    				sb.append(warning);
    				sb.append("\n");
    			}
			}
			finally {
				ignoreRegistrationFormConfigEvents = false;
			}
		}
		else {
			// Validate the uniqueness of profile creation confirmation mails etc per event
			List<String> warnings = EmailTemplateProfileHelper.validateEmailTemplateConfiguration(
				emailTemplateSearchDataList,
				languageList
			);

			for (String warning: warnings) {
				sb.append(warning);

				/* Add new line after every warning, even if there is only one to have at least to lines.
				 * Otherwise the icon left to the text is not visible.
				 */
				sb.append("\n");
			}
		}


		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (sb.length() > 0) {
						warningsLabel.setImage(IconRegistry.getImage("icons/error.png"));
						warningsLink.setText(sb.toString());
					}
					else {
						warningsLabel.setImage(null);
						warningsLink.setText("");
					}
					warningsLabel.getParent().getParent().layout();
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});

	}


	private boolean exists(SimplePersonSearchData simplePersonSearchData)  {
		try {
			Long participantOrProfileID = simplePersonSearchData.getId();

			if (simplePersonSearchData.getEventID() == null) {
				Profile profile = ProfileModel.getInstance().getProfile(participantOrProfileID);
				return profile != null;
			}
			else {
				Participant participant = ParticipantModel.getInstance().getParticipant(participantOrProfileID);
				return participant != null;
			}
		}
		catch (Exception ignore) {
			return false;
		}
	}

}
