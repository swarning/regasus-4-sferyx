package de.regasus.onlineform.editor;

import static com.lambdalogic.util.StringHelper.isEmpty;

import java.util.Date;
import java.util.List;

import org.apache.commons.jexl.ScriptFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.actions.ActionFactory;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.messeinfo.regasus.Rule;
import com.lambdalogic.messeinfo.regasus.RuleListStringConverter;
import com.lambdalogic.util.DateHelper;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.Vigenere;
import com.lambdalogic.util.Vigenere2;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.CopyAction;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.LazyScrolledTabItem;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.LanguageModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventModel;
import de.regasus.event.ParticipantType;
import de.regasus.onlineform.OnlineFormI18N;
import de.regasus.onlineform.RegistrationFormConfigModel;
import de.regasus.onlineform.util.RightsHelper;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.ui.Activator;

public class RegistrationFormConfigEditor extends AbstractEditor<RegistrationFormConfigEditorInput> implements
	IRefreshableEditorPart, CacheModelListener<Long> {

	public static final String ID = "RegistrationFormConfigEditor";

	private static final String WEB_ID_REGEX = "[A-Za-z0-9\\_\\-]+";

	// the entity
	private RegistrationFormConfig rfConfig;

	// **************************************************************************
	// * Widgets
	// *

	private TabFolder tabFolder;

	private ConfigurationTabComposite configurationTabComposite;

	private DesignTabComposite designTabComposite;

	private DataFieldsTabComposite dataFieldsTabComposite;

	private TextsTabComposite textsTabComposite;

	private FilesTabComposite filesTabComposite;

	private TabItem textsTabItem;

	private TabItem labelsTabItem;

	private TabItem rulesTabItem;

	private TabItem filesTabItem;

	private EventVO eventVO;

	private LazyScrolledTabItem previewTabItem;

	private PreviewTabComposite previewTabComposite;

	private RegistrationFormConfigModel  registrationFormConfigModel;

	private ParticipantTypeModel participantTypeModel;

	private LabelBeansTabComposite labelsTabComposite;

	private RulesTabComposite rulesTabComposite;

	private boolean isCreateAllowed = false;

	private EventModel eventModel;

	// *
	// * Widgets
	// **************************************************************************


	@Override
	public void dispose() {
		if (registrationFormConfigModel != null && rfConfig.getId() != null) {
			try {
				registrationFormConfigModel.removeListener(this, rfConfig.getId());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		if (eventModel != null) {
			eventModel.removeListener(this, eventVO.getID());
		}
		super.dispose();
	}


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		try {
			Long key = editorInput.getKey();

			registrationFormConfigModel = RegistrationFormConfigModel.getInstance();

			Long eventPK = editorInput.getEventPK();

			eventModel = EventModel.getInstance();
			eventModel.addListener(this, eventPK);
			eventVO = eventModel.getEventVO(eventPK);

			participantTypeModel = ParticipantTypeModel.getInstance();

			if (key != null) {
				// Get the entity before registration as listener at the model.
				// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

				// get entity
				rfConfig = registrationFormConfigModel.getRegistrationFormConfig(key);

				// register at model
				registrationFormConfigModel.addListener(this, key);

			}
			else {
				rfConfig = editorInput.getConfig();

				// rfConfig might exist in case we made a copy. If not:
				if (rfConfig == null) {
					// create empty entity
					rfConfig = new RegistrationFormConfig(eventVO);
					rfConfig.setBackgroundColor(0xFFFFFF); // white
					rfConfig.setForegroundColor(0x000000); // black
					String language = eventVO.getLanguage();
					if ("en".equals(language)) {
						rfConfig.setEnglishAvailable(true);
					}
					else {
						rfConfig.setGermanAvailable(true);
					}
				}

				// even if rfConfig was a copy, we set these data for the first time
				rfConfig.setEventPK(eventVO.getID());
				rfConfig.setWebId(getValidWebIdFrom(eventVO.getMnemonic()));

				// today
				Date start = new Date();
				// the day before the event starts
				Date end = DateHelper.addDays(eventVO.getStartTime(), -1);
				// set values for start- and endRegistrationPeriod only if start is before end
				if (start.before(end)) {
    				rfConfig.setStartRegistrationPeriod(start);
    				rfConfig.setEndRegistrationPeriod(end);
				}
			}
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			throw new Exception(e);
		}
	}


	@Override
	public void setDirty(boolean dirty) {
		super.setDirty(dirty);

		SWTHelper.asyncExecDisplayThread( new Runnable() {
			@Override
			public void run() {
				if (configurationTabComposite != null) {
					configurationTabComposite.setEditorIsDirty(dirty);
				}
				designTabComposite.setEditorIsDirty(dirty);
			}
		} );
	}


	protected void setEntity(RegistrationFormConfig config) {
		try {
			if ( ! isNew()) {
    			// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    			config = config.clone();
			}
			this.rfConfig = config;

			if (configurationTabComposite != null) {
				configurationTabComposite.setRegistrationFormConfig(rfConfig);
			}
			designTabComposite.setRegistrationFormConfig(rfConfig, eventVO);
			dataFieldsTabComposite.setRegistrationFormConfig(rfConfig);
			textsTabComposite.setRegistrationFormConfig(rfConfig);
			filesTabComposite.setRegistrationFormConfig(rfConfig);
			labelsTabComposite.setRegistrationFormConfig(rfConfig, eventVO);
			previewTabComposite.setRegistrationFormConfig(rfConfig);
			if (rulesTabComposite != null) {
				rulesTabComposite.setRegistrationFormConfig(rfConfig);
			}

			syncWidgetsToEntity();
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			throw new RuntimeException(e);
		}
	}


	@Override
	protected String getTypeName() {
		return OnlineFormI18N.WebsiteConfiguration;
	}


	/**
	 * Create contents of the editor part
	 *
	 * @param mainComposite
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			CopyAction copyAction = new CopyAction();
			getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);


			isCreateAllowed = RightsHelper.isCreateAllowed();


			this.parent = parent;

			parent.setLayout(new FillLayout());

			tabFolder = new TabFolder(parent, SWT.NONE);
			tabFolder.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					tabItemChanged();
				}
			});

			if (isCreateAllowed) {
				// Configuration Tab
				LazyScrolledTabItem configurationTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
				configurationTabComposite = new ConfigurationTabComposite(configurationTabItem.getContentComposite(), SWT.NONE);
				configurationTabItem.setText(OnlineFormI18N.Configuration);

				configurationTabItem.refreshScrollbars();
			}

			// Design Tab
			LazyScrolledTabItem designTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			designTabComposite = new DesignTabComposite(designTabItem.getContentComposite(), SWT.NONE);
			designTabItem.setText(OnlineFormI18N.Design);

			designTabItem.refreshScrollbars();


			// Fields Tab
			LazyScrolledTabItem fieldsTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			dataFieldsTabComposite = new DataFieldsTabComposite(fieldsTabItem.getContentComposite(), SWT.NONE, eventVO.getID());
			fieldsTabItem.setText(OnlineFormI18N.DataFields);

			fieldsTabItem.refreshScrollbars();


			// Text Tab
			textsTabItem = new TabItem(tabFolder, SWT.NONE);
			textsTabComposite = new TextsTabComposite(tabFolder, SWT.NONE);
			textsTabItem.setText(OnlineFormI18N.Texts);
			textsTabItem.setControl(textsTabComposite);
			textsTabComposite.setCopyAction(copyAction);

			// Labels Tab
			labelsTabItem = new TabItem(tabFolder, SWT.NONE);
			labelsTabComposite = new LabelBeansTabComposite(tabFolder, SWT.NONE);
			labelsTabItem.setText(OnlineFormI18N.Labels);
			labelsTabItem.setControl(labelsTabComposite);
			labelsTabComposite.setCopyAction(copyAction);

			// Files Tab
			filesTabItem = new TabItem(tabFolder, SWT.NONE);
			filesTabComposite = new FilesTabComposite(tabFolder, SWT.NONE);
			filesTabItem.setText(OnlineFormI18N.FileUpload);
			filesTabItem.setControl(filesTabComposite);

			if (isCreateAllowed) {

				// Rules Tab
				rulesTabItem = new TabItem(tabFolder, SWT.NONE);
				rulesTabComposite = new RulesTabComposite(tabFolder, SWT.NONE);
				rulesTabItem.setText(OnlineFormI18N.BookingRules);
				rulesTabItem.setControl(rulesTabComposite);
			}

			// Preview Tab
			previewTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			previewTabComposite = new PreviewTabComposite(previewTabItem.getContentComposite(), SWT.NONE);
			previewTabItem.setText(OnlineFormI18N.Preview);

			previewTabItem.refreshScrollbars();


			// sync widgets and groups to the entity
			setEntity(rfConfig);

			if (configurationTabComposite != null) {
				// after sync add this as ModifyListener to all widgets and groups
				configurationTabComposite.addModifyListener(this);
			}
			dataFieldsTabComposite.addModifyListener(this);
			textsTabComposite.addModifyListener(this);
			labelsTabComposite.addModifyListener(this);
			if (designTabComposite != null) {
				designTabComposite.addModifyListener(this);
			}

			if (rulesTabComposite != null) {
				rulesTabComposite.addModifyListener(this);
			}

		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	@Override
	public void doSave(IProgressMonitor monitor) {

		syncEntityToWidgets();

		// MIRCP-2945 - is no email dispatch is done, no email address is required
		if (isEmpty(rfConfig.getDefaultFromEmailAddress()) && ! rfConfig.isNoEmailDispatch()) {
			MessageDialog.openError(getSite().getShell(), UtilI18N.Error, OnlineFormI18N.DefaultFromAddressNeeded);
			return;
		}

		if (rfConfig.isTravelEnabled()) {

			String customField11 =  eventVO.getCustomFieldName(11);
			String customField21 =  eventVO.getCustomFieldName(21);

			String arrivalString = HotelLabel.HotelBooking_Arrival.getString();
			String departureString = HotelLabel.HotelBooking_Departure.getString();

			if (isEmpty(customField11) && isEmpty(customField21)) {
				createCustomFieldsForTravelData(arrivalString, departureString);
			}
			else if (! arrivalString.equals(customField11) || ! departureString.equals(customField21)) {
				boolean confirm = MessageDialog.openConfirm(getSite().getShell(), UtilI18N.Warning, OnlineFormI18N.CustomFields11And21NotSuitedForTravelData);
				if (confirm) {
					createCustomFieldsForTravelData(arrivalString, departureString);
				}
			}
		}

		if (rfConfig.isPaymentTypePageEnabled()) {

			if (! rfConfig.isDebitPaymentEnabled() &&
				! rfConfig.isCashPaymentEnabled() &&
				! rfConfig.isCcPaymentEngineEnabled() &&
				! rfConfig.isEasyCheckoutEnabled() &&
				! rfConfig.isTransferPaymentEnabled()) {
				MessageDialog.openError(getSite().getShell(), UtilI18N.Error, OnlineFormI18N.OnePaymentMustBeSelected);
				return;
			}
		}

		if (rfConfig.isCompanionEnabled()) {

			try {
				List<ParticipantType> typesByEventPK = participantTypeModel.getParticipantTypesByEvent(rfConfig.getEventPK());
				boolean companionParticipantTypeBelongsToEvent = false;
				for (ParticipantType participantType : typesByEventPK) {
					if (participantType.getId() == 4) {
						companionParticipantTypeBelongsToEvent = true;
						break;
					}
				}

				if (! companionParticipantTypeBelongsToEvent) {
					MessageDialog.openError(getSite().getShell(), UtilI18N.Error, OnlineFormI18N.CompanionParticipantTypeIsNotPresentInEvent);
					return;
				}

			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		if (rfConfig.isOnlineFormEnabled()) {
			if (rfConfig.getLanguageCount() == 0) {
				MessageDialog.openError(getSite().getShell(), UtilI18N.Error, OnlineFormI18N.OneLanguageMustBeSelected);
				return;
			}

			if (!
					(
					rfConfig.isRegistrationWithoutLoginEnabled() ||
					rfConfig.isLoginPersonalizedLinkEnabled() ||
					rfConfig.isLoginUsernamePasswordEnabled() ||
					rfConfig.isProfilePortalEnabled() ||
					rfConfig.isPasswordProtectedFirstPageEnable()
					)
				) {

				MessageDialog.openError(
					getSite().getShell(),
					UtilI18N.Error,
					OnlineFormI18N.IfRegistrationNeedsLoginOneLoginWayMustBeEnabled);
				return;
			}

			if (rfConfig.getStartRegistrationPeriod() != null &&
				rfConfig.getEndRegistrationPeriod() != null &&
				!rfConfig.getStartRegistrationPeriod().before(rfConfig.getEndRegistrationPeriod())
			) {
				MessageDialog.openError(
					getSite().getShell(),
					UtilI18N.Error,
					OnlineFormI18N.StartRegistrationPeriodNotBeforeEndRegistrationPeriod);
				return;
			}

			if (isEmpty(rfConfig.getWebId())) {
				MessageDialog.openError(getSite().getShell(), UtilI18N.Error, OnlineFormI18N.WebIdNeededForOnline);
				return;
			}


			if (!rfConfig.getWebId().matches(WEB_ID_REGEX)) {
				MessageDialog.openError(getSite().getShell(), UtilI18N.Error, OnlineFormI18N.WebIdOnlyCharactersDigitsUnderscoreAndHyphen);
				return;
			}


			if (!rfConfig.isPaymentForm() && !rfConfig.isParticipantTypeSelectable() &&
				rfConfig.getDefaultParticipantTypePK() == null &&
				!rfConfig.isDefineParticipantTypeFromRegistrationPP()
			) {
				MessageDialog.openError(
					getSite().getShell(),
					UtilI18N.Error,
					OnlineFormI18N.DefaultParticipantTypeRequiredIfParticipantTypeNotSelectable);
				return;
			}


			// MIRCP-2333 - Validate syntax of booking rules
			String rulesString = rfConfig.getBookingRules();
			List<Rule> rules = RuleListStringConverter.fromString(rulesString);
			for (Rule rule : rules) {
				try {
					ScriptFactory.createScript(rule.getCondition());
				}
				catch (Exception e) {
					String message = OnlineFormI18N.InvalidBookingRule  + ":\n\n" + rule.getCondition();
					MessageDialog.openError(getSite().getShell(), UtilI18N.Error, message);
					return;
				}
			}
		}

		try {

			if (rfConfig.isEmailRecommendationEnabled()) {
				LanguageString emailRecommendationText = new LanguageString(rfConfig.getEmailRecommendationTextI18n());
				String languageWithMessage = null;
				if (rfConfig.isGermanAvailable()) {
					String string = emailRecommendationText.getString("de", false);
					if (string == null || ! string.contains("${message}")) {
						languageWithMessage = LanguageModel.getInstance().getLanguage("de").getName().getString();
					}
				}
				if (rfConfig.isEnglishAvailable()) {
					String string = emailRecommendationText.getString("en", false);
					if (string == null || ! string.contains("${message}")) {
						languageWithMessage = LanguageModel.getInstance().getLanguage("en").getName().getString();
					}
				}
				if (languageWithMessage != null) {
					String message = OnlineFormI18N.UseTheseVariablesForEmailRecommendation + " (" + languageWithMessage + ")";
					MessageDialog.openError(getSite().getShell(), UtilI18N.Error, message);
					return;
				}
			}

			if (isNew()) {
				rfConfig = registrationFormConfigModel.create(rfConfig);
				editorInput.setConfig(rfConfig);

				// observe the Model
				registrationFormConfigModel.addListener(this, rfConfig.getId());

				// set new entity
				setEntity(rfConfig);

			}
			else {
				/* Save the entity.
				 * On success setEntity() will be called indirectly in dataChange(), else an
				 * Exception will be thrown.
				 * The result of update() must not be assigned (to the entity), because this will
				 * happen in setEntity() and there it may be cloned!
				 * Assigning the entity here would overwrite the cloned value with the one from
				 * the model. Therefore we would have inconsistent data!
				 */
				registrationFormConfigModel.update(rfConfig);
			}
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		firePropertyChange(PROP_TITLE);
	}


	private void createCustomFieldsForTravelData(String arrivalString, String departureString) {
		eventVO.setCustomFieldName(11, arrivalString);
		eventVO.setCustomFieldName(21, departureString);
		try {
			/* Save the entity.
			 * On success setEntity() will be called indirectly in dataChange(), else an
			 * Exception will be thrown.
			 * The result of update() must not be assigned (to the entity), because this will
			 * happen in setEntity() and there it may be cloned!
			 * Assigning the entity here would overwrite the cloned value with the one from
			 * the model. Therefore we would have inconsistent data!
			 */
			eventModel.update(eventVO);
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void syncWidgetsToEntity() {
		if (rfConfig != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						if (configurationTabComposite != null) {
							configurationTabComposite.syncWidgetsToEntity();
						}

						designTabComposite.syncEntityToWidgets();
						dataFieldsTabComposite.syncWidgetsToEntity();
						textsTabComposite.syncWidgetsToEntity();
						labelsTabComposite.syncWidgetsToEntity();

						if (rulesTabComposite != null) {
							rulesTabComposite.syncWidgetsToEntity();
						}

						// refresh the EditorInput
						String name = getName();

						setPartName(name);
						editorInput.setName(name);
						editorInput.setToolTipText(getToolTipText());

						firePropertyChange(PROP_TITLE);

						// signal that editor has no unsaved data anymore
						setDirty(false);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private void syncEntityToWidgets() {
		try {
			if (rfConfig != null) {
				if (configurationTabComposite != null) {
					configurationTabComposite.syncEntityToWidgets();
				}

				designTabComposite.syncEntityToWidgets();
				dataFieldsTabComposite.syncEntityToWidgets();
				textsTabComposite.syncEntityToWidgets();
				labelsTabComposite.syncEntityToWidgets();

				if (rulesTabComposite != null) {
					rulesTabComposite.syncEntityToWidgets();
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void refresh() throws Exception {
		if (rfConfig != null && rfConfig.getId() != null) {


			registrationFormConfigModel.refresh(rfConfig.getId());

			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				rfConfig = registrationFormConfigModel.getRegistrationFormConfig(rfConfig.getId());
				if (rfConfig != null) {
					setEntity(rfConfig);
				}
			}
		}
	}


	@Override
	protected String getName() {
		String name = null;
		if (!isNew()) {
			name = rfConfig.getWebId();
		}
		if (isEmpty(name)) {
			name = OnlineFormI18N.Regasus_Editor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		return OnlineFormI18N.Regasus_Editor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return rfConfig == null || rfConfig.getId() == null;
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		FormatHelper formatHelper = new FormatHelper();

		// the labels of the info dialog
		final String[] labels =	{
			UtilI18N.ID,
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser,
			ContactLabel.VigenereCode.getString(),
			ContactLabel.Vigenere2Code.getString(),
		};

		// the values of the info dialog
		String vigenere = "";
		String vigenere2 = "";

		if (! isNew()) {
			vigenere = Vigenere.toVigenereString(rfConfig.getId());
			vigenere2 = Vigenere2.toVigenereString(rfConfig.getId());
		}

		final String[] values =	{
			String.valueOf(rfConfig.getPrimaryKey()),
			formatHelper.formatDateTime(rfConfig.getNewTime()),
			rfConfig.getNewDisplayUserStr(),
			formatHelper.formatDateTime(rfConfig.getEditTime()),
			rfConfig.getEditDisplayUserStr(),
			vigenere,
			vigenere2
		};

		// show info dialog
		final EditorInfoDialog infoDialog =
			new EditorInfoDialog(
				getSite().getShell(),
				OnlineFormI18N.WebsiteConfiguration + ": " + UtilI18N.Info,
				labels,
				values);

		infoDialog.open();
	}


	protected void tabItemChanged() {
		 TabItem selectedTabItem = tabFolder.getSelection()[0];

		 if (selectedTabItem == previewTabItem && ! isDirty()) {
			 previewTabComposite.isMadeVisible();
		 }
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == registrationFormConfigModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (rfConfig != null) {
					rfConfig = registrationFormConfigModel.getRegistrationFormConfig(rfConfig.getId());
					if (rfConfig != null) {
						setEntity(rfConfig);
					}
					else if (ServerModel.getInstance().isLoggedIn()) {
						closeBecauseDeletion();
					}
				}
			}
			else if (event.getSource() == eventModel) {
				if (event.getOperation() == CacheModelOperation.UPDATE && eventVO != null) {
					eventVO = eventModel.getEventVO(eventVO.getID());
					designTabComposite.setEventVO(eventVO);
				}
			}

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private String getValidWebIdFrom(String value) {
		if(!value.matches(WEB_ID_REGEX)) {
			for (int i = 0; i < value.length(); i++) {
				String currentCharacter = String.valueOf(value.charAt(i));
				if (!currentCharacter.matches(WEB_ID_REGEX)) {
					value = value.replaceAll(currentCharacter, "_");
				}
			}
		}

		return value;
	}
}
