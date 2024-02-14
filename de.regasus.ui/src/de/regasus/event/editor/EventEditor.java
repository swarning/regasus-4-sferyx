package de.regasus.event.editor;

import static com.lambdalogic.util.CollectionsHelper.empty;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.TerminalConfigParameterSet;
import com.lambdalogic.messeinfo.contact.LabelTextCombinationsVO;
import com.lambdalogic.messeinfo.exception.DirtyWriteException;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroupLocation;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ApprovalConfig;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.ListSet;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.EntityProvider;
import com.lambdalogic.util.rcp.widget.LazyScrolledTabItem;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.common.composite.LabelTextCombinationsComposite;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.LanguageModel;
import de.regasus.core.PropertyModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.event.ParticipantType;
import de.regasus.participant.AbstractParticipantTypeProvider;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.participant.type.ChooseParticipantTypesComposite;
import de.regasus.ui.Activator;


public class EventEditor
extends AbstractEditor<EventEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long>, EventIdProvider {

	public static final String ID = "EventEditor";

	// the entity
	private EventVO eventVO;
	private ListSet<ParticipantType> participantTypes;

	// the model
	private EventModel eventModel;
	private ParticipantTypeModel participantTypeModel;
	private ConfigParameterSetModel configParameterSetModel;

	// ConfigParameterSet
	private ConfigParameterSet configParameterSet;

	// **************************************************************************
	// * Widgets
	// *

	private TabFolder tabFolder;
	private TabItem customFieldsTabItem;

	private EventGeneralComposite generalComposite;
	private ChooseParticipantTypesComposite chooseParticipantTypesComposite;
	private EventDefaultsComposite defaultsComposite;
	private EventFinanceComposite financeComposite;
	private EventTemplatesComposite templatesComposite;
	private EventCustomFieldsComposite customFieldsComposite;
	private EventSmtpComposite smtpComposite;
	private EventCertificateComposite certificateComposite;
	private EventOnsiteWorkflowComposite onsiteWorkflowComposite;
	private EventPushServiceSettingsComposite pushServiceSettingsComposite;
	private LabelTextCombinationsComposite textComposite;
	private ApprovalConfigComposite approvalConfigComposite;

	// *
	// * Widgets
	// **************************************************************************

	// ******************************************************************************************
	// * Overriden EditorPart methods

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long eventPK = editorInput.getKey();
		Long eventGroupId = editorInput.getEventGroupId();

		// get models
		eventModel = EventModel.getInstance();
		participantTypeModel = ParticipantTypeModel.getInstance();

		configParameterSetModel = ConfigParameterSetModel.getInstance();
		configParameterSetModel.addListener(this);


		refreshConfigParameterSet();

		if (eventPK != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener (look at MIRCP-1129).

			// get entity
			eventVO = eventModel.getEventVO(eventPK);
			participantTypes = new ListSet<>( participantTypeModel.getParticipantTypesByEvent(eventPK) );

			// register at model
			eventModel.addListener(this, eventPK);
			participantTypeModel.addForeignKeyListener(this, eventPK);
		}
		else {
			// create empty entity
			eventVO = new EventVO();

			eventVO.setEventGroupPK(eventGroupId);

			// default values
			eventVO.setNextBadgeNo(1);
			eventVO.setNextBookingNo(1);
			eventVO.setNextParticipantNo(1);
			eventVO.getProgPriceDefaultsVO().setGross(true);
			eventVO.getHotelLodgePriceDefaultsVO().setGross(true);
			eventVO.getHotelBreakfastPriceDefaultsVO().setGross(true);
			eventVO.getHotelAdd1PriceDefaultsVO().setGross(true);
			eventVO.getHotelAdd2PriceDefaultsVO().setGross(true);

			eventVO.setCustomFieldLocation(ParticipantCustomFieldGroupLocation.TAB_1);

			participantTypes = new ListSet<>();
		}
	}


	private void refreshConfigParameterSet() throws Exception {
		Long eventPK = editorInput.getKey();
		if (eventPK != null) {
			configParameterSet = configParameterSetModel.getConfigParameterSet(eventPK);
		}
		else {
			configParameterSet = configParameterSetModel.getConfigParameterSet();
		}

		// assure that configParameterSet is not null
		if (configParameterSet == null) {
			configParameterSet = new ConfigParameterSet();
		}
	}


	@Override
	public void dispose() {
		if (eventModel != null && eventVO.getID() != null) {
			try {
				eventModel.removeListener(this, eventVO.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		if (participantTypeModel != null && eventVO.getID() != null) {
			try {
				participantTypeModel.removeForeignKeyListener(this, eventVO.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		if (configParameterSetModel != null) {
			configParameterSetModel.removeListener(this);
		}

		super.dispose();
	}


	protected void setEntity(EventVO newEventVO) {
		if (! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			newEventVO = newEventVO.clone();
		}

		this.eventVO = newEventVO;


		// set entity to other composites
		generalComposite.setEventVO(eventVO);

		chooseParticipantTypesComposite.setChosenEntities(participantTypes);

		defaultsComposite.setEventVO(eventVO);
		financeComposite.setEventVO(eventVO);
		templatesComposite.setEvent(eventVO.getID());
		customFieldsComposite.setEventVO(eventVO);
		smtpComposite.setEventVO(eventVO);
		if (certificateComposite != null) {
			certificateComposite.setEventVO(eventVO);
		}
		if (onsiteWorkflowComposite != null) {
			onsiteWorkflowComposite.setEventVO(eventVO);
		}
		if (pushServiceSettingsComposite != null) {
			pushServiceSettingsComposite.setEventVO(eventVO);
		}
		textComposite.setEntity( eventVO.getLabelTextCombinationsVO() );
		if (approvalConfigComposite != null) {
			approvalConfigComposite.setEventVO(eventVO);
		}

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return ParticipantLabel.Event.getString();
	}

	@Override
	protected String getInfoButtonToolTipText() {
		return I18N.EventEditor_InfoButtonToolTip;
	}


	private EntityProvider<ParticipantType> participantTypeProvider = new AbstractParticipantTypeProvider() {
		@Override
		public List<ParticipantType> getEntityList() {
			try {
				return participantTypeModel.getAllUndeletedParticipantTypes();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				return Collections.emptyList();
			}
		}
	};


	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			// tabFolder
			tabFolder = new TabFolder(parent, SWT.NONE);

			// General Tab
			LazyScrolledTabItem generalTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			generalTabItem.setText(I18N.EventEditor_GeneralTabText);
			generalComposite = new EventGeneralComposite(generalTabItem.getContentComposite(), SWT.NONE, configParameterSet);
			generalTabItem.refreshScrollbars();

			// Participant Type Tab
			TabItem participantTypeTabItem = new TabItem(tabFolder, SWT.NONE);
			participantTypeTabItem.setText(I18N.EventEditor_ParticipantTypeTabText);

			chooseParticipantTypesComposite = new ChooseParticipantTypesComposite(
				tabFolder,
				participantTypeProvider,
				SWT.NONE
			);
			participantTypeTabItem.setControl(chooseParticipantTypesComposite);

			// Defaults Tab
			LazyScrolledTabItem defaultsTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			defaultsTabItem.setText(UtilI18N.DefaultValues);
			defaultsComposite = new EventDefaultsComposite(defaultsTabItem.getContentComposite(), SWT.NONE, configParameterSet);
			defaultsTabItem.refreshScrollbars();

			// Finance Tab
			LazyScrolledTabItem financeTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			financeTabItem.setText(I18N.EventEditor_FinanceTabText);
			financeComposite = new EventFinanceComposite(financeTabItem.getContentComposite(), SWT.NONE);
			financeTabItem.refreshScrollbars();

			// Templates Tab
			TabItem templatesTabItem = new TabItem(tabFolder, SWT.NONE);
			templatesTabItem.setText(I18N.EventEditor_TemplatesTabText);
			templatesComposite = new EventTemplatesComposite(tabFolder, SWT.NONE);
			templatesTabItem.setControl(templatesComposite);

			// Custom Fields Tab
			customFieldsTabItem = new TabItem(tabFolder, SWT.NONE);
			customFieldsTabItem.setText(I18N.EventEditor_CustomFieldsTabText);
			customFieldsComposite = new EventCustomFieldsComposite(tabFolder, SWT.NONE);
			customFieldsTabItem.setControl(customFieldsComposite);

			// SMTP Tab
			TabItem smtpTabItem = new TabItem(tabFolder, SWT.NONE);
			smtpTabItem.setText(I18N.EventEditor_SmtpTabText);
			smtpComposite = new EventSmtpComposite(tabFolder, SWT.NONE);
			smtpTabItem.setControl(smtpComposite);

			// Certificate Tab
			if ( configParameterSet.getEvent().getCertificate().isVisible() ) {
				TabItem certificatePolicyTabItem = new TabItem(tabFolder, SWT.NONE);
				certificatePolicyTabItem.setText(I18N.EventEditor_CertificatePolicyTabText);
				certificateComposite = new EventCertificateComposite(
					tabFolder,
					SWT.NONE,
					configParameterSet.getEvent().getCertificate()
				);
				certificatePolicyTabItem.setControl(certificateComposite);
			}

			// Onsite Workflow Tab
			if ( configParameterSet.getEvent().getOnsiteWorkflow().isVisible() ) {
				TabItem onsiteWorkflowTabItem = new TabItem(tabFolder, SWT.NONE);
				onsiteWorkflowTabItem.setText(de.regasus.core.ui.CoreI18N.Config_OnsiteWorkflow);
				onsiteWorkflowComposite = new EventOnsiteWorkflowComposite(tabFolder, SWT.NONE);
				onsiteWorkflowTabItem.setControl(onsiteWorkflowComposite);
			}

			// Push Service Settings Tab
			if ( configParameterSet.getEvent().getPushServiceSettings().isVisible() ) {
				TabItem pushServiceSettingsTabItem = new TabItem(tabFolder, SWT.NONE);
				pushServiceSettingsTabItem.setText(de.regasus.core.ui.CoreI18N.Config_PushServiceSettings);
				pushServiceSettingsComposite = new EventPushServiceSettingsComposite(tabFolder, SWT.NONE);
				pushServiceSettingsTabItem.setControl(pushServiceSettingsComposite);
			}

			// textComposite
			LazyScrolledTabItem textTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			textTabItem.setText(UtilI18N.Texts);

			LabelTextCombinationsVO labelTextCombinationsVO = eventVO.getLabelTextCombinationsVO();

			List<String> languageIds = eventVO.getLanguages();
			if ( empty(languageIds) ) {
				languageIds = LanguageProvider.getInstance().getDefaultLanguagePKList();
			}

			List<Language> languageList = LanguageModel.getInstance().getLanguages(languageIds);

			textComposite = new LabelTextCombinationsComposite(
				textTabItem.getContentComposite(),
				SWT.NONE,
				labelTextCombinationsVO,
				languageList
			);

			textTabItem.refreshScrollbars();


			// Approval Config Tab
			String approvalConfigProperty = PropertyModel.getInstance().getPropertyValue(ApprovalConfig.APPROVAL_PROPERTY_KEY);
			boolean showAprovalConfig = TypeHelper.toBoolean(approvalConfigProperty, false);
			if (showAprovalConfig) {
				TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
				tabItem.setText("Approval Config"); // TODO
				approvalConfigComposite = new ApprovalConfigComposite(tabFolder, SWT.NONE);
				tabItem.setControl(approvalConfigComposite);
			}

			// set data
			setEntity(eventVO);

			// after sync add this as ModifyListener to all widgets and groups
			generalComposite.addModifyListener(this);
			chooseParticipantTypesComposite.addModifyListener(this);
			defaultsComposite.addModifyListener(this);
			financeComposite.addModifyListener(this);
			customFieldsComposite.addModifyListener(this);
			smtpComposite.addModifyListener(this);
			if (certificateComposite != null) {
				certificateComposite.addModifyListener(this);
			}
			if (onsiteWorkflowComposite != null) {
				onsiteWorkflowComposite.addModifyListener(this);
			}
			if (pushServiceSettingsComposite != null) {
				pushServiceSettingsComposite.addModifyListener(this);
			}
			textComposite.addModifyListener(this);
			if (approvalConfigComposite != null) {
				approvalConfigComposite.addModifyListener(this);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		boolean create = isNew();

		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 2);

			/*
			 * Entity mit den Widgets synchronisieren. Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/* Save new entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				eventVO = eventModel.create(eventVO);

				List<Long> participantTypePKs = ParticipantType.getPrimaryKeyList(participantTypes);
				participantTypeModel.setEventParticipantTypes(eventVO.getID(), participantTypePKs);


				// observe the Model
				eventModel.addListener(this, eventVO.getID());
				participantTypeModel.addForeignKeyListener(this, eventVO.getID());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(eventVO.getID());

				// set new entity
				setEntity(eventVO);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				eventModel.update(eventVO);

				List<Long> participantTypePKs = ParticipantType.getPrimaryKeyList(participantTypes);
				participantTypeModel.setEventParticipantTypes(eventVO.getID(), participantTypePKs);

				// setEntity will be called indirectly in dataChange()
			}

			// save participant types


			monitor.worked(1);
		}
		catch (DirtyWriteException e) {
			monitor.setCanceled(true);
			// DirtyWriteException werden gesondert behandelt um eine aussagekräftige Fehlermeldung
			// ausgeben zu können.
			RegasusErrorHandler.handleError(
				Activator.PLUGIN_ID,
				getClass().getName(),
				e,
				I18N.UpdateEventDirtyWriteMessage
			);
		}
		catch (Exception e) {
			monitor.setCanceled(true);

			/* Beim EventEditor ist dieser Aufruf notwendig, wenn das Speichern des Events
			 * erfolgreich ist und das Speichern der Teilnehmerarten scheitert. In diesem Fall
			 * wird durch das erfolgreiche Speichern des Events syncWidgetsToEntity aufgerufen
			 * und damit dirty auf false gesetzt.
			 */
			setDirty(true);

			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			monitor.done();
		}
	}


	public void selectParticipantCustomFieldGroupLocation(ParticipantCustomFieldGroupLocation location) {
		if (customFieldsComposite != null) {
			tabFolder.setSelection(customFieldsTabItem);
			customFieldsComposite.selectParticipantCustomFieldGroupLocation(location);
		}
	}


	private void syncWidgetsToEntity() {
		if (eventVO != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						// set editor title
						setPartName(getName());
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName(getName());
						editorInput.setToolTipText(getToolTipText());

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
		if (eventVO != null) {
			generalComposite.syncEntityToWidgets();
			chooseParticipantTypesComposite.syncEntityToWidgets();
			defaultsComposite.syncEntityToWidgets();
			financeComposite.syncEntityToWidgets();
			customFieldsComposite.syncEntityToWidgets();
			smtpComposite.syncEntityToWidgets();
			if (certificateComposite != null) {
				certificateComposite.syncEntityToWidgets();
			}
			if (onsiteWorkflowComposite != null) {
				onsiteWorkflowComposite.syncEntityToWidgets();
			}
			if (pushServiceSettingsComposite != null) {
				pushServiceSettingsComposite.syncEntityToWidgets();
			}
			if (approvalConfigComposite != null) {
				approvalConfigComposite.syncEntityToWidgets();
			}
			textComposite.syncEntityToWidgets();
		}
	}


	// ******************************************************************************************
	// * Private helper methods

	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == eventModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (eventVO != null) {
					eventVO = eventModel.getEventVO(eventVO.getPK());
					if (eventVO != null) {
						setEntity(eventVO);
					}
					else if (ServerModel.getInstance().isLoggedIn()) {
						closeBecauseDeletion();
					}
				}
			}
			else if (event.getSource() == participantTypeModel) {
				/* This if-statement is necessary, because ParticipantTypeComposite requests data from
				 * ParticipantTypeModel during its initialization, which causes a refresh. But at this point
				 * of time the variable participantTypeComposite is still null.
				 */
				if (chooseParticipantTypesComposite != null && eventVO != null) {
					Long eventID = eventVO.getID();
					if (eventID != null) {
						participantTypes = new ListSet<>( participantTypeModel.getParticipantTypesByEvent(eventID) );
						chooseParticipantTypesComposite.setChosenEntities(participantTypes);
					}
				}
			}
			else if (event.getSource() == configParameterSetModel) {
				refreshConfigParameterSet();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void refresh() throws Exception {
		if (eventVO != null && eventVO.getID() != null) {
			eventModel.refresh(eventVO.getID());
			participantTypeModel.refreshForeignKey( eventVO.getID() );


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				eventVO = eventModel.getEventVO(eventVO.getID());
				if (eventVO != null) {
					setEntity(eventVO);
				}
			}
		}
	}


	@Override
	public boolean isNew() {
		return eventVO.getPK() == null;
	}


	@Override
	protected String getName() {
		String name = null;
		if (eventVO != null) {
			name = eventVO.getLabel(Locale.getDefault());
		}

		if (StringHelper.isEmpty(name)) {
			name = I18N.EventEditor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		return I18N.EventEditor_DefaultToolTip;
	}


	@Override
	public Long getEventId() {
		return eventVO.getID();
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// gather information for this Event which is otherwise not visible anywhere
		Boolean isCertificatePrintAllowed = null;
		Boolean isSelfCheckinAllowed = null;
		Boolean isGateAllowed = null;

		TerminalConfigParameterSet terminalConfigParameterSet = configParameterSet.getEvent().getTerminal();
		isCertificatePrintAllowed = terminalConfigParameterSet.getCertificatePrint().isVisible();
		isSelfCheckinAllowed = terminalConfigParameterSet.getSelfCheckIn().isVisible();
		isGateAllowed = terminalConfigParameterSet.getGate().isVisible();

		FormatHelper formatHelper = new FormatHelper();

		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			ParticipantLabel.Event_Mnemonic.getString(),
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser,
			de.regasus.core.ui.CoreI18N.Config_SelfCheckin,
			de.regasus.core.ui.CoreI18N.Config_CertificatePrint,
			de.regasus.core.ui.CoreI18N.Config_Gate,
		};

		// the values of the info dialog
		final String[] values = {
			String.valueOf(eventVO.getID()),
			eventVO.getMnemonic(),
			formatHelper.formatDateTime(eventVO.getNewTime()),
			eventVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime(eventVO.getEditTime()),
			eventVO.getEditDisplayUserStr(),
			format(isSelfCheckinAllowed),
			format(isCertificatePrintAllowed),
			format(isGateAllowed),
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			ParticipantLabel.Event.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);

		infoDialog.open();
	}


	private String format(Boolean b) {
		if (Boolean.TRUE.equals(b)) {
			return UtilI18N.Yes;
		}
		else if (Boolean.FALSE.equals(b)) {
			return UtilI18N.No;
		}
		else {
			return "?";
		}
	}

}
