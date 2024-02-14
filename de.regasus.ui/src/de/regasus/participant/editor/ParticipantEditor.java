package de.regasus.participant.editor;

import static com.lambdalogic.util.CollectionsHelper.empty;
import static com.lambdalogic.util.StringHelper.isNotEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.config.parameterset.AddressConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ParticipantConfigParameterSet;
import com.lambdalogic.messeinfo.contact.AbstractPerson;
import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.exception.DirtyWriteException;
import com.lambdalogic.messeinfo.exception.ParticipantDuplicateException;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.ClearingVO;
import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroupLocation;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup_Location_Position_Comparator;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.ParticipantMessage;
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.messeinfo.participant.data.BadgeCVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.messeinfo.participant.data.ParticipantVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.LazyScrolledTabItem;
import com.lambdalogic.util.rcp.widget.MultiLineText;

import de.regasus.I18N;
import de.regasus.common.composite.AddressGroupsComposite;
import de.regasus.common.composite.ApprovalGroup;
import de.regasus.common.composite.CommunicationGroup;
import de.regasus.common.composite.CompanionGroup;
import de.regasus.common.composite.MembershipGroup;
import de.regasus.common.composite.PersonGroup;
import de.regasus.common.composite.UserCredentialsGroup;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.finance.AccountancyModel;
import de.regasus.finance.PaymentSystem;
import de.regasus.hotel.HotelBookingModel;
import de.regasus.participant.ParticipantCorrespondenceModel;
import de.regasus.participant.ParticipantCustomFieldGroupModel;
import de.regasus.participant.ParticipantCustomFieldModel;
import de.regasus.participant.ParticipantFileModel;
import de.regasus.participant.ParticipantHistoryModel;
import de.regasus.participant.ParticipantImageHelper;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantProvider;
import de.regasus.participant.dialog.ParticipantDuplicateDialog;
import de.regasus.participant.editor.document.ParticipantFileComposite;
import de.regasus.participant.editor.finance.FinanceComposite;
import de.regasus.participant.editor.history.ParticipantHistoryComposite;
import de.regasus.participant.editor.hotelbooking.HotelBookingsTableComposite;
import de.regasus.participant.editor.lead.LeadsComposite;
import de.regasus.participant.editor.overview.ParticipantOverviewComposite;
import de.regasus.participant.editor.programmebooking.ProgrammeBookingsTableComposite;
import de.regasus.person.PersonLinkModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;
import de.regasus.programme.ProgrammeBookingModel;
import de.regasus.ui.Activator;

public class ParticipantEditor extends AbstractEditor<ParticipantEditorInput>
implements EventIdProvider, ParticipantProvider, CacheModelListener<Long>, IRefreshableEditorPart {

	public static final String ID = "ParticipantEditor";

	public static final int ADDRESS_COUNT = AbstractPerson.ADDRESS_COUNT;

	protected static List<ISaveListener> saveListenerList;

	// the entity
	private Participant participant;
	private EventVO eventVO;

	private static int lastSelectedTabIndex = 1;

	// the models
	private ParticipantModel paModel;
	private ParticipantHistoryModel paHistModel;
	private PersonLinkModel personLinkModel;
	private EventModel evModel;
	private ConfigParameterSetModel configParameterSetModel;
	private ProgrammeBookingModel pbModel;
	private HotelBookingModel hbModel;
	private AccountancyModel accModel;
	private ParticipantCorrespondenceModel paCorrespondenceModel;
	private ParticipantFileModel paFileModel;
	private ParticipantCustomFieldGroupModel paCustomFieldGroupModel;

	// ConfigParameterSet
	private ConfigParameterSet configParameterSet;
	private ParticipantConfigParameterSet participantConfigParameterSet;

	// copies of all addresses before saving to decide the update of group member's addresses in adaptGroupMembersAddresses()
	private Address[] previousAddressArray = new Address[ADDRESS_COUNT];

	private boolean wasPreviouslyCancelled;

	private boolean participantDirty = false;


	// **************************************************************************
	// * Widgets
	// *

	private ParticipantOverviewComposite participantOverviewComposite;

	private PersonGroup personGroup;

	private MembershipGroup membershipGroup;

	private ApprovalGroup approvalGroup;

	private PreferredPaymentTypeGroup preferredPaymentTypeGroup;

	private AddressGroupsComposite addressGroupsComposite;

	private ParticipantBankingComposite participantBankingComposite;

	private CompanionGroup companionGroup;

	private GridData companionGroupGridData;

	private CommunicationGroup communicationGroup;

	private UserCredentialsGroup userCredentialsGroup;

	private ParticipantGroup participantGroup;

	private MultiLineText note;

	private List<ParticipantCustomFieldComposite> participantCustomFieldComposites = new ArrayList<>();

	private ProgrammeBookingsTableComposite programmeBookingsTableComposite;

	private HotelBookingsTableComposite hotelBookingsTableComposite;

	private FinanceComposite financeComposite;

	private BadgesComposite badgesComposite;

	private LeadsComposite leadsComposite;

	private ParticipantCorrespondenceManagementComposite correspondenceManagementComposite;

	private ParticipantFileComposite fileComposite;

	private ParticipantHistoryComposite participantHistoryComposite;

	private TabFolder tabFolder;

	// TabItems
	private TabItem overviewTabItem;
	private LazyScrolledTabItem personTabItem;
	private LazyScrolledTabItem addressesTabItem;
	private LazyScrolledTabItem bankingTabItem;
	private TabItem programmeBookingsTabItem;
	private TabItem hotelBookingsTabItem;
	private TabItem financeTabItem;
	private TabItem badgeTabItem;
	private TabItem leadsTabItem;
	private TabItem correspondenceTabItem;
	private TabItem fileTabItem;
	private TabItem historyTabItem;

	/*
	 * Since there are a few tabs allow to display on ParticipantCustomFieldEditor, but there are many groups more than
	 * those. Some groups will be display under Person Tab of the Participant. So, this is initialized for the tab to be
	 * shown on ParticipantCustomFieldEditor only.
	 */
	private Set<ParticipantCustomFieldGroupLocation> customFieldTabLocations = new HashSet<>();
	{
		customFieldTabLocations.add(ParticipantCustomFieldGroupLocation.TAB_1);
		customFieldTabLocations.add(ParticipantCustomFieldGroupLocation.TAB_2);
		customFieldTabLocations.add(ParticipantCustomFieldGroupLocation.TAB_3);
	}

	// *
	// * Widgets
	// **************************************************************************

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long participantID = editorInput.getKey();

		/* The eventPK is only set if a new Participant should be created.
		 * Otherwise it is null and will be set after the Participant has been loaded.
		 */
		Long eventPK = editorInput.getEventPK();

		// get models
		evModel = EventModel.getInstance();
		personLinkModel = PersonLinkModel.getInstance();
		paModel = ParticipantModel.getInstance();
		paHistModel = ParticipantHistoryModel.getInstance();
		configParameterSetModel = ConfigParameterSetModel.getInstance();
		pbModel = ProgrammeBookingModel.getInstance();
		hbModel = HotelBookingModel.getInstance();
		accModel = AccountancyModel.getInstance();
		paCorrespondenceModel = ParticipantCorrespondenceModel.getInstance();
		paFileModel = ParticipantFileModel.getInstance();
		paCustomFieldGroupModel = ParticipantCustomFieldGroupModel.getInstance();


		if (participantID != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			participant = paModel.getExtendedParticipant(participantID);

			// register at model
			paModel.addListener(this);

			eventPK = participant.getEventId();
		}
		else {
			// create empty entity
			participant = ParticipantModel.getInitialParticipant();

			if (editorInput.isGroupManager()) {
				participant.put(ParticipantModel.GROUP_MANAGER_KEY, Boolean.TRUE);
			}

			Long groupManagerPK = editorInput.getGroupManagerPK();
			Long mainParticipantPK = editorInput.getMainParticipantPK();
			Long participantStatePK = ParticipantState.REGISTRATION.longValue();

			if (mainParticipantPK != null) {
				Participant mainParticipant = paModel.getParticipant(mainParticipantPK);
				groupManagerPK = mainParticipant.getGroupManagerPK();
				participantStatePK = mainParticipant.getParticipantStatePK();
			}

			participant.setEventId(eventPK);
			participant.setCompanionOfPK(mainParticipantPK);
			participant.setGroupManagerPK(groupManagerPK);
			participant.setParticipantStatePK(participantStatePK);
		}

		if (eventPK != null) {
			eventVO = evModel.getEventVO(eventPK);

			// get ConfigSet (eventPK must not be null)
			configParameterSet = configParameterSetModel.getConfigParameterSet(eventPK);
			participantConfigParameterSet = configParameterSet.getEvent().getParticipant();
		}
		else {
			throw new Exception("Der Teilnehmereditor konnte nicht geöffnet werden, weil keine Veranstaltung spezifiziert ist.");
		}
	}


	@Override
	public void dispose() {
		if (paModel != null && participant != null) {
			try {
				Long rootPK = participant.getRootPK();
				if (rootPK != null) {
					paModel.removeForeignKeyListener(this, rootPK);
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

			try {
				paModel.removeListener(this);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	@Override
	protected String getTypeName() {
		return ParticipantLabel.Participant.getString();
	}


	@Override
	protected String getInfoButtonToolTipText() {
		return I18N.ParticipantEditor_InfoButtonToolTip;
	}


	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			// tabFolder
			tabFolder = new TabFolder(parent, SWT.NONE);

			createPersonTab(tabFolder);
			createAddressesTab(tabFolder);
			createBankingTab(tabFolder);

			boolean showNewCustomFields = participantConfigParameterSet.getCustomField().isVisible();
			boolean showOldCustomFields = participantConfigParameterSet.getSimpleCustomField().isVisible();

			ParticipantCustomFieldGroupLocation oldCustomFieldLocation = null;

			if (showOldCustomFields) {
				boolean hasOldCustomFields = false;

				for (String customFieldName : eventVO.getCustomFieldNames()) {
					if (StringHelper.isNotEmpty(customFieldName)) {
						hasOldCustomFields = true;
						oldCustomFieldLocation = eventVO.getCustomFieldLocation();
						break;
					}
				}

				showOldCustomFields = hasOldCustomFields;
			}

			if (showNewCustomFields || showOldCustomFields) {
				List<ParticipantCustomFieldGroup> groupList =
					paCustomFieldGroupModel.getParticipantCustomFieldGroupsByEventPK(getEventId());

				Map<ParticipantCustomFieldGroupLocation, List<ParticipantCustomFieldGroup>> locationToGroupsMap =
					createLocationToGroupsMap(groupList);

				// put location for the old custom fields if there are no new custom fields belong to the location
				if (oldCustomFieldLocation != null) {
					List<ParticipantCustomFieldGroup> pcfgList = locationToGroupsMap.get(oldCustomFieldLocation);
					if ( empty(pcfgList) ) {
						locationToGroupsMap.put(oldCustomFieldLocation, Collections.emptyList());
					}
				}

				List<ParticipantCustomFieldComposite> customFieldTabs = createParticipantCustomFieldTabs(
					tabFolder,
					locationToGroupsMap
				);
				this.participantCustomFieldComposites.addAll(customFieldTabs);
			}

			if (!isNew()) {
				createTabsOfPersistedParticipants();
			}

			// sync widgets and groups to the entity
			setEntity(participant);

			/* at the first time, after the setEntity(participant) is called
			 * the setChanged(false) must be called to mark, that there is no change yet
			 */
			participantDirty = false;

			if (! isNew()) {
				tabFolder.setSelection(lastSelectedTabIndex);
			}

			tabFolder.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// Store the last selected index
					int selectionIndex = tabFolder.getSelectionIndex();
					if (selectionIndex != -1) {
						lastSelectedTabIndex = selectionIndex;
					}
				}
			});

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	private void createTabsOfPersistedParticipants() throws Exception {
		createOverviewTab(tabFolder);

		createProgrammeBookingsTab(tabFolder);
		createHotelBookingsTab(tabFolder);
		createFinanceTab(tabFolder);
		createBadgesTab(tabFolder);
		createLeadsTab(tabFolder);
		createCorrespondenceTab(tabFolder);
		createDocumentsTab(tabFolder);
		createHistoryTab(tabFolder);
	}


	protected void setEntity(Participant participant) throws Exception {
		try {
			if ( ! isNew()) {
    			// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    			participant = participant.clone();
			}
			this.participant = participant;

			personGroup.setPerson(participant);

			if (membershipGroup != null) {
				membershipGroup.setEntity( participant.getMembership() );
			}

			approvalGroup.setPerson(participant);

			if (preferredPaymentTypeGroup != null) {
				preferredPaymentTypeGroup.setParticipant(participant);
			}

			if (companionGroup != null) {
				companionGroup.setParticipant(participant);
			}

			if (userCredentialsGroup != null) {
				userCredentialsGroup.setPerson(participant);
			}

			if (participantGroup != null) {
				participantGroup.setParticipant(participant);
			}

			if (communicationGroup != null) {
				communicationGroup.setCommunication(participant.getCommunication());
			}

			if (addressGroupsComposite != null) {
				addressGroupsComposite.setAbstractPerson(participant);
			}

			if (participantBankingComposite != null) {
				participantBankingComposite.setParticipant(participant);
			}

			for (ParticipantCustomFieldComposite participantCustomFieldComposite : participantCustomFieldComposites) {
				participantCustomFieldComposite.setParticipant(participant);
			}

			if (programmeBookingsTableComposite != null) {
				programmeBookingsTableComposite.setParticipant(participant);
			}

			if (hotelBookingsTableComposite != null) {
				hotelBookingsTableComposite.setParticipant(participant);
			}

			if (financeComposite != null) {
				financeComposite.setParticipant(participant);
			}

			if (badgesComposite != null) {
				badgesComposite.setParticipant(participant);
			}

			if (leadsComposite != null) {
				leadsComposite.setParticipant(participant);
			}

			if (correspondenceManagementComposite != null) {
				correspondenceManagementComposite.setParticipantID(participant.getID());
			}

			if (fileComposite != null) {
				fileComposite.setParticipant(participant);
			}

			if (participantHistoryComposite != null) {
				participantHistoryComposite.setParticipant(participant);
			}

			syncWidgetsToEntity();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void selectInvoice(Long invoicePK) {
		if (financeComposite != null) {
			tabFolder.setSelection(financeTabItem);
			financeComposite.selectInvoice(invoicePK);
		}
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		boolean create = isNew();

		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 2);

			// the copies of the addresses are used for deciding the update of group member's addresses
			for (int i = 0; i < previousAddressArray.length; i++) {
				previousAddressArray[i] = participant.getAddress(i + 1).createCopyWithSameGroupCommonAtttributes();
			}

			wasPreviouslyCancelled = participant.isCancelled();


			/*
			 * Entity mit den Widgets synchronisieren. Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/*
				 * Save new entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				final ParticipantDuplicateException[] duplicateException = new ParticipantDuplicateException[1];
				final Participant[] participants = new Participant[1];

				BusyCursorHelper.busyCursorWhile(new Runnable(){
					@Override
					public void run() {
						try {
							participants[0] = paModel.create(participant, false);
						}
						catch (ParticipantDuplicateException e) {
							duplicateException[0] = e;
						}
						catch (Exception e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						}
					}
				});

				/* If a DuplicateException occurred, handle it
				 * (show the ParticipantDuplicateDialog).
				 */
				if (duplicateException[0] != null) {
					handleDuplicateException(duplicateException[0]);
				}
				else if (participants[0] != null) {
					participant = participants[0];
				}


				/* Open the editors of all participants.
				 * This may be the created participant or the potential duplicates.
				 */
				if (participant.getID() != null) {
					participant = paModel.getExtendedParticipant(participant.getID());

					// observe the Model
					paModel.addListener(this);

					// Set the PK of the new entity to the EditorInput
					editorInput.setKey(participant.getID());

					/* Create the tabs which are hidden for new participants.
					 * This must be called before setEntity(participant).
					 * Otherwise the new tabs have no participant.
					 */
					createTabsOfPersistedParticipants();

					// set new entity
					setEntity(participant);


					// MIRCP-959 - After having a created a new participant, refresh the
					// tooltip which shows only limited information before
					setTitleToolTip(getToolTipText());
				}
			}
			else {
				// MIRCP-17 - When a user changes the last name of an anonymous participant,
				// a Yes-No-Dialog shall appear. The dialog asks if the participant shall still be anonymous.
				if (participant.isAnonym() && personGroup.isLastNameModified()){
					String message = I18N.ParticipantEditor_ChangeAnonymousParticipantLastNameDialog_Question;
					boolean setAnonymousToFalse = MessageDialog.openQuestion(
						getSite().getShell(),
						UtilI18N.Question,
						message
					);

					// If the user doesn't want the participant to be anonymous, anonymous is set to FALSE.
					if (setAnonymousToFalse) {
						participant.setAnonym(false);
					}

					// reset last name modified flag
					personGroup.setLastNameModified(false);
				}


				/* Validate Participant and Correspondences before persisting them to avoid
				 * late InvalidValuesExceptions after other entities are already persisted.
				 */
				if (participantDirty) {
					// Participant cannot be validated directly because of some special handling.
					paModel.validate(participant);
				}
 				if (correspondenceManagementComposite != null) {
 					correspondenceManagementComposite.validate();
				}


 				// persist Correspondences
 				if (correspondenceManagementComposite != null) {
					correspondenceManagementComposite.doSave();
				}


				/*
				 * Save the entity. On success we get the updated entity, else an Exception will be thrown.
				 *
				 * Der Rückgabewert der Methode update darf nicht übernommen werden! update löst ein CacheModelEvent
				 * aus, wodurch die Methode dataChange(CacheModelEvent) aufgerufen wird, die ihrerseits die erweiterten
				 * (!) Daten vom ParticipantModel abruft. Der Rückgabewert von update und das in dataChange() abgerufene
				 * erweiterte Participant müssen nicht identisch sein! Defakto sind sie es nicht, da der Rückgabewert
				 * von update nur einen nicht erweiterten Participant liefert.
				 */
 				if (participantDirty) {
 					paModel.update(participant);
 					participantDirty = false;
				}
 				else {
 					setDirty(false);
				}

				// After having successfully updated the participant, we might have to adapt some group members's addresses
				adaptGroupMembersAddresses(monitor);

				// After having successfully updated the participant, we might have to cancel their booking
				adaptCancellationState(monitor);

				// setEntity will be called indirectly in dataChange()
			}

			// inform ISaveListeners
			fireSaved(create);

			monitor.worked(1);
		}
		catch (DirtyWriteException e) {
			monitor.setCanceled(true);
			// DirtyWriteException werden gesondert behandelt um eine aussagekräftige Fehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(
				Activator.PLUGIN_ID,
				getClass().getName(),
				e,
				I18N.UpdateParticipantDirtyWriteMessage
			);
		}
		catch (ErrorMessageException e) {
			monitor.setCanceled(true);
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			monitor.setCanceled(true);
			String msg = null;
			if (create) {
				msg = I18N.CreateParticipantErrorMessage;
			}
			else {
				msg = I18N.UpdateParticipantErrorMessage;
			}
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
		}
		finally {
			monitor.done();
		}
	}


	/**
	 * @param duplicateException This Exception is thrown, if duplicate(s) of
	 * the participant to create is found.
	 * @return Participant PK of the participant to create or die participant PKs
	 * of the selected duplicates in popped up dialog, whose data should be
	 * shown in editors.
	 */
	protected void handleDuplicateException(ParticipantDuplicateException duplicateException) {
		Collection<ParticipantSearchData> duplicates = duplicateException.getDuplicates();

		ParticipantDuplicateDialog dialog = new ParticipantDuplicateDialog(
			getSite().getShell(),
			ParticipantMessage.DuplicateDialog_Title.getString(),
			participant,
			duplicates
		);
		dialog.open();

		participant = dialog.getParticipant();
	}


	public CurrencyAmount getSelectedOpenAmount() {
		CurrencyAmount openAmount = null;
		if (financeComposite != null) {
			openAmount = financeComposite.getSelectedOpenAmount();
		}
		return openAmount;
	}


	public Collection<InvoiceVO> getSelectedInvoiceVOs() {
		Collection<InvoiceVO> selection = Collections.emptyList();
		if (financeComposite != null) {
			selection = financeComposite.getSelectedInvoiceVOs();
		}
		return selection;
	}


	public Collection<InvoicePositionVO> getSelectedInvoicePositionVOs() {
		Collection<InvoicePositionVO> selection = Collections.emptyList();
		if (financeComposite != null) {
			selection = financeComposite.getSelectedInvoicePositionVOs();
		}
		return selection;
	}


	public Collection<PaymentVO> getSelectedPaymentVOs() {
		Collection<PaymentVO> selection = Collections.emptyList();
		if (financeComposite != null) {
			selection = financeComposite.getSelectedPaymentVOs();
		}
		return selection;
	}


	public Collection<ClearingVO> getSelectedPaymentClearingVOs() {
		Collection<ClearingVO> selection = Collections.emptyList();
		if (financeComposite != null) {
			selection = financeComposite.getSelectedPaymentClearingVOs();
		}
		return selection;
	}


	public Collection<ClearingVO> getSelectedInvoicePositionClearingVOs() {
		Collection<ClearingVO> selection = Collections.emptyList();
		if (financeComposite != null) {
			selection = financeComposite.getSelectedInvoicePositionClearingVOs();
		}
		return selection;
	}


	public Collection<ClearingVO> getSelectedClearingCandidates() {
		Collection<ClearingVO> selection = Collections.emptyList();
		if (financeComposite != null) {
			selection = financeComposite.getSelectedClearingCandidates();
		}
		return selection;
	}


	public BadgeCVO getSelectedBadge() {
		if (badgesComposite != null) {
			return badgesComposite.getSelectedBadge();
		}
		return null;
	}


	public void addFinanceSelectionChangedListener(ISelectionChangedListener listener) {
		if (financeComposite != null && financeComposite.isInitialized()) {
			financeComposite.addSelectionChangedListener(listener);
		}
	}


	public void removeFinanceSelectionChangedListener(ISelectionChangedListener listener) {
		if (financeComposite != null) {
			financeComposite.removeSelectionChangedListener(listener);
		}
	}


	public void addBadgeSelectionChangedListener(ISelectionChangedListener listener) {
		if (badgesComposite != null) {
			badgesComposite.addSelectionChangedListener(listener);
		}
	}


	public void removeBadgeSelectionChangedListener(ISelectionChangedListener listener) {
		if (badgesComposite != null) {
			badgesComposite.removeSelectionChangedListener(listener);
		}
	}


	public void assignBadge() {
		if (badgesComposite != null) {
			badgesComposite.assignBadge();
		}
	}


	public boolean isWaitingForScannedId() {
		if (badgesComposite != null) {
			return badgesComposite.isWaitingForScannedId();
		}
		return false;
	}


	public void stopWaiting(final byte[] barcodeBytes) {
		if (badgesComposite != null) {
			badgesComposite.stopWaiting(barcodeBytes);
		}
	}


	public TabFolder getTabFolder() {
		return tabFolder;
	}


	private void syncWidgetsToEntity() throws Exception {
		if (participant != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						if (note != null) {
							note.setText(StringHelper.avoidNull(participant.getNote()));
						}

						setPartName(getName());

						Image image = ParticipantImageHelper.getImage(participant);
						setTitleImage(image);

						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName(getName());
						editorInput.setToolTipText(getToolTipText());

						// Signalisieren, dass Editor keinen ungespeicherten Daten mehr enthält
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
		if (participant != null) {
			personGroup.syncEntityToWidgets();

			if (membershipGroup != null) {
				membershipGroup.syncEntityToWidgets();
			}

			approvalGroup.syncEntityToWidgets();

			if (preferredPaymentTypeGroup != null) {
				preferredPaymentTypeGroup.syncEntityToWidgets();
			}

			if (companionGroup != null) {
				companionGroup.syncEntityToWidgets();
			}

			if (communicationGroup != null) {
				communicationGroup.syncEntityToWidgets();
			}

			if (note != null) {
				participant.setNote(note.getText());
			}

			if (addressGroupsComposite != null) {
				addressGroupsComposite.syncEntityToWidgets();
			}

			if (participantBankingComposite != null) {
				participantBankingComposite.syncEntityToWidgets();
			}

			if (userCredentialsGroup != null) {
				userCredentialsGroup.syncEntityToWidgets();
			}

			if (participantGroup != null) {
				participantGroup.syncEntityToWidgets();
			}

			for (ParticipantCustomFieldComposite participantCustomFieldComposite : participantCustomFieldComposites) {
				participantCustomFieldComposite.syncEntityToWidgets();
			}

			if (hotelBookingsTableComposite != null) {
				hotelBookingsTableComposite.syncEntityToWidgets();
			}

			if (correspondenceManagementComposite != null) {
				correspondenceManagementComposite.syncEntityToWidgets();
			}

			// no syncEntityToWidgets needed for finance tab
		}
	}


	/**
	 * The overview tab shows nearly all of the participant's information in one composite.
	 */
	private void createOverviewTab(TabFolder tabFolder) {
		overviewTabItem = new TabItem(tabFolder, SWT.NONE, 0);
		overviewTabItem.setText(UtilI18N.Overview);

		participantOverviewComposite = new ParticipantOverviewComposite(
			tabFolder,
			participant.getID(),
			configParameterSet
		);
		overviewTabItem.setControl(participantOverviewComposite);
	}


	private void createPersonTab(TabFolder tabFolder) throws Exception {
		personTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
		personTabItem.setText(ContactLabel.person.getString());

		Composite personTabComposite = new Composite(personTabItem.getContentComposite(), SWT.NONE);
		personTabComposite.setLayout(new GridLayout(2, true));

		// Make two composites for the left and the right side, for otherwise
		// the groups on the left and the right were forced to have the same heigth
		Composite leftComposite = new Composite(personTabComposite, SWT.NONE);
		leftComposite.setLayout(new GridLayout());
		leftComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Composite rightComposite = new Composite(personTabComposite, SWT.NONE);
		rightComposite.setLayout(new GridLayout());
		rightComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));


		// PersonGroup
		personGroup = new PersonGroup(leftComposite, SWT.NONE, participantConfigParameterSet);
		personGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		personGroup.addModifyListener(this);

		// MembershipGroup
		if (participantConfigParameterSet.getMembership().isVisible()) {
			membershipGroup = new MembershipGroup(leftComposite, SWT.NONE, participantConfigParameterSet.getMembership());
    		membershipGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    		membershipGroup.addModifyListener(this);
		}

		// ApprovalGroup
		approvalGroup = new ApprovalGroup(leftComposite, SWT.NONE, participantConfigParameterSet);
		approvalGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		approvalGroup.addModifyListener(this);

		// UserCredentialsGroup
		if (participantConfigParameterSet.getUserName().isVisible() ||
			participantConfigParameterSet.getPassword().isVisible()
		) {
			userCredentialsGroup = new UserCredentialsGroup(rightComposite, SWT.NONE, participantConfigParameterSet);
			userCredentialsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			userCredentialsGroup.addModifyListener(this);
		}



		// ParticipantGroup
		participantGroup = new ParticipantGroup(rightComposite, SWT.None, configParameterSet);
		participantGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		participantGroup.addModifyListener(this);

		// CompanionGroup
		if ( participantConfigParameterSet.getSecondPerson().isVisible() ) {
			companionGroup = new CompanionGroup(leftComposite, SWT.NONE);
			companionGroup.setText(ParticipantLabel.CompanionsAndSecondPerson.getString());
			companionGroupGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			companionGroup.setLayoutData(companionGroupGridData);

			companionGroup.addModifyListener(this);

			companionGroup.addLayoutModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (companionGroup.isDisposed()) {
						System.out.println("disposed");
					}
					companionGroupGridData.exclude = companionGroup.isEmpty();
					companionGroup.setVisible(!companionGroup.isEmpty());
					companionGroup.getParent().layout(true, true);
				}
			});

			/* connect personGroup with companionGroup and addressGroups to refresh salutation and
			 * address labels when secondPerson changes
			 */
			companionGroup.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					companionGroup.syncEntityToWidgets();
					personGroup.refreshDefaultSalutation();
					personGroup.refreshDefaultInvitationCard();
					if (addressGroupsComposite != null) {
						addressGroupsComposite.refreshDefaultAddressLabel();
					}
				}
			});
		}

		// CommunicationGroup
		if (participantConfigParameterSet.getCommunication().isVisible()) {
    		communicationGroup = new CommunicationGroup(
    			rightComposite,
    			SWT.NONE,
    			participantConfigParameterSet.getCommunication()
    		);
    		GridData gd_communicationGroup = new GridData(SWT.FILL, SWT.FILL, true, false);
    		communicationGroup.setLayoutData(gd_communicationGroup);
    		communicationGroup.addModifyListener(this);
		}


		// PreferredPaymentTypeGroup
		if (participantConfigParameterSet.getPreferredPaymentType().isVisible()) {
			preferredPaymentTypeGroup = new PreferredPaymentTypeGroup(
				rightComposite,
				SWT.NONE,
				participantConfigParameterSet.getPreferredPaymentType()
			);

			preferredPaymentTypeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			preferredPaymentTypeGroup.addModifyListener(this);
		}


		// Custom Field
		List<ParticipantCustomFieldGroup> groups = paCustomFieldGroupModel.getParticipantCustomFieldGroupsByEventPK(getEventId());
		Set<ParticipantCustomFieldGroup> leftSideGroups = new TreeSet<>( ParticipantCustomFieldGroup_Location_Position_Comparator.getInstance() );
		Set<ParticipantCustomFieldGroup> rightSideGroups = new TreeSet<>( ParticipantCustomFieldGroup_Location_Position_Comparator.getInstance() );
		for (ParticipantCustomFieldGroup group : groups) {
			switch(group.getLocation()) {
    			case PSNLT:
    				leftSideGroups.add(group);
    				break;
    			case PSNRT:
    				rightSideGroups.add(group);
    				break;
    			default:
    				break;
			}
		}

		boolean showNewCustomFields = participantConfigParameterSet.getCustomField().isVisible();
		boolean showOldCustomFields = participantConfigParameterSet.getSimpleCustomField().isVisible();

		boolean showOldCustomFieldsAtLeftSide = showOldCustomFields && (eventVO.getCustomFieldLocation() == ParticipantCustomFieldGroupLocation.PSNLT);
		Map<ParticipantCustomFieldGroup, List<ParticipantCustomField>> leftSideGroupToFieldsMap = keepOnlyGroupWithCustomFields(leftSideGroups);
		if ((!leftSideGroupToFieldsMap.isEmpty() && showNewCustomFields) || showOldCustomFieldsAtLeftSide) {
			ParticipantCustomFieldComposite customFieldWidget = createParticipantCustomFieldWidgets(
				leftComposite,
				this,
				leftSideGroupToFieldsMap,
				showNewCustomFields,
				showOldCustomFieldsAtLeftSide
			);

			// make LazyComposite visible
			customFieldWidget.setVisible(true);

			this.participantCustomFieldComposites.add(customFieldWidget);
		}

		boolean showOldCustomFieldsAtRightSide = showOldCustomFields && (eventVO.getCustomFieldLocation() == ParticipantCustomFieldGroupLocation.PSNRT);
		Map<ParticipantCustomFieldGroup, List<ParticipantCustomField>> rightSideGroupToFieldsMap = keepOnlyGroupWithCustomFields(rightSideGroups);
		if ((!rightSideGroupToFieldsMap.isEmpty() && showNewCustomFields) || showOldCustomFieldsAtRightSide) {
			ParticipantCustomFieldComposite customFieldWidget = createParticipantCustomFieldWidgets(
				rightComposite,
				this,
				rightSideGroupToFieldsMap,
				showNewCustomFields,
				showOldCustomFieldsAtRightSide
			);

			// make LazyComposite visible
			customFieldWidget.setVisible(true);

			this.participantCustomFieldComposites.add(customFieldWidget);
		}

		// note
		if (participantConfigParameterSet.getNote().isVisible()) {
			Composite noteComposite = new Composite(personTabComposite, SWT.NONE);
			noteComposite.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1) );
			noteComposite.setLayout( new GridLayout(2, false) );

			Label noteLabel = new Label(noteComposite, SWT.NONE);
			{
				GridData gridData = new GridData(SWT.LEFT, SWT.TOP, false, false);
				gridData.verticalIndent = SWTConstants.VERTICAL_INDENT;
				noteLabel.setLayoutData(gridData);
			}
			noteLabel.setText( Participant.NOTE.getLabel() );
			noteLabel.setToolTipText( Participant.NOTE.getDescription() );

			note = new MultiLineText(noteComposite, SWT.BORDER | SWT.WRAP, true);
			GridData gd_note = new GridData(SWT.FILL, SWT.FILL, true, true);
			note.setLayoutData(gd_note);
			note.setTextLimit( Participant.NOTE.getMaxLength() );

			note.addModifyListener(this);
		}

		personTabItem.refreshScrollbars();
	}


	private void createAddressesTab(TabFolder tabFolder) throws Exception {
		if (addressesTabItem == null) {
			addressesTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			addressesTabItem.setText(ContactLabel.addresses.getString());

			AddressConfigParameterSet addressConfigParameterSet = participantConfigParameterSet.getAddress();
			addressGroupsComposite = new AddressGroupsComposite(addressesTabItem.getContentComposite(), SWT.NONE, addressConfigParameterSet);
			addressGroupsComposite.setHomeCountryPK( eventVO.getOrganisationOfficeCountryPK() );

			// link with PersonGroup
			personGroup.addModifyListener(addressGroupsComposite);
			addressGroupsComposite.addModifyListener(this);

			addressesTabItem.refreshScrollbars();
		}
	}


	private void createBankingTab(TabFolder tabFolder) throws Exception {
		if (bankingTabItem == null &&
			(
				participantConfigParameterSet.getBank().isVisible() ||
				participantConfigParameterSet.getCreditCard().isVisible()
			)
		) {

			boolean withCreditCardAlias =	eventVO.getPaymentSystem() == PaymentSystem.DATATRANS ||
											eventVO.getPaymentSystem() == PaymentSystem.PAYENGINE;

			bankingTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);

			bankingTabItem.setText(ContactLabel.Banking.getString());
			participantBankingComposite = new ParticipantBankingComposite(
				bankingTabItem.getContentComposite(),
				SWT.NONE,
				withCreditCardAlias,
				participantConfigParameterSet
			);
			participantBankingComposite.addModifyListener(this);

			bankingTabItem.refreshScrollbars();
		}
	}


	/**
	 * Creates new participant custom field tabs if there are both groups and custom fields. An empty list can be returned
	 * in case there is no such group or all groups are empty (no such custom fields).
	 *
	 * @param tabFolder the target tab folder that new tabs will be inside
	 * @param locationToGroupsMap a map represent the one-to-many relationship between tab and groups
	 * @return a new participant custom field tab or empty list
	 * @throws Exception
	 */
	private List<ParticipantCustomFieldComposite> createParticipantCustomFieldTabs(
		TabFolder tabFolder,
		Map<ParticipantCustomFieldGroupLocation, List<ParticipantCustomFieldGroup>> locationToGroupsMap
	)
	throws Exception {
		List<ParticipantCustomFieldComposite> resultTabs = new ArrayList<>();

		boolean showNewCustomFields = participantConfigParameterSet.getCustomField().isVisible();
		boolean showOldCustomFields = participantConfigParameterSet.getSimpleCustomField().isVisible();

		if (showOldCustomFields) {
			boolean hasOldCustomFields = false;

			for (String customFieldName : eventVO.getCustomFieldNames()) {
				if ( isNotEmpty(customFieldName) ) {
					hasOldCustomFields = true;
					break;
				}
			}

			showOldCustomFields = hasOldCustomFields;
		}

		// if there are either old or new custom fields
		if (showNewCustomFields || showOldCustomFields) {
    		for (Entry<ParticipantCustomFieldGroupLocation, List<ParticipantCustomFieldGroup>> mapEntry : locationToGroupsMap.entrySet()) {
    			ParticipantCustomFieldGroupLocation location = mapEntry.getKey();
    			List<ParticipantCustomFieldGroup> customFieldGroups = mapEntry.getValue();

    			// determine if this location is one of the 3 custom field Tabs
    			boolean isCustomFieldTabLocation = customFieldTabLocations.contains(location);
    			if (isCustomFieldTabLocation) {
        			// determine if this is the location to show old custom fields (and if there is any old custom field at all)
        			boolean showOldCustomFieldsHere = showOldCustomFields && (location == eventVO.getCustomFieldLocation());

        			// determine if there is any new custom field to show at this location
        			Map<ParticipantCustomFieldGroup, List<ParticipantCustomField>> groupToCustomFieldsMap = keepOnlyGroupWithCustomFields(customFieldGroups);
        			boolean showNewCustomFieldsHere = !groupToCustomFieldsMap.isEmpty();

        			// if there is any (old or new) custom field to show at this location
        			if (showNewCustomFieldsHere || showOldCustomFieldsHere) {
            			LazyScrolledTabItem tabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
            			tabItem.setText( getLocationName(location) );

            			ParticipantCustomFieldComposite customFieldComposite = createParticipantCustomFieldWidgets(
            				tabItem.getContentComposite(),	// parent
            				this,							// modifyListener
            				groupToCustomFieldsMap,
            				showNewCustomFields,
            				showOldCustomFieldsHere
            			);

            			resultTabs.add(customFieldComposite);
        			}
    			}
    		}
		}

		return Collections.unmodifiableList(resultTabs);
	}


	private ParticipantCustomFieldComposite createParticipantCustomFieldWidgets(
		Composite parent,
		ModifyListener modifyListener,
		Map<ParticipantCustomFieldGroup, List<ParticipantCustomField>> mapGroupAndFields,
		boolean showNewCustomFields,
		boolean showOldCustomFields
	)
	throws Exception {
		ParticipantCustomFieldComposite customFieldComposite = new ParticipantCustomFieldComposite(
			parent,
			SWT.NONE,
			mapGroupAndFields,
			showNewCustomFields,
			showOldCustomFields
		);
		customFieldComposite.setParticipant(participant); //TODO should be removed, this line prevent eventPK == null
		customFieldComposite.addModifyListener(modifyListener);
		return customFieldComposite;
	}


	/**
	 * Creates a new map between custom field group and its fields. In case all groups are empty or no such group, the
	 * empty map will be return.
	 *
	 * @param groups a list of group
	 * @return a new map between custom field group and its fields or empty map
	 * @throws Exception
	 */
	private Map<ParticipantCustomFieldGroup, List<ParticipantCustomField>> keepOnlyGroupWithCustomFields(
		Collection<ParticipantCustomFieldGroup> groups
	)
	throws Exception {
		Map<ParticipantCustomFieldGroup, List<ParticipantCustomField>> groupToCustomFieldsMap =
			ParticipantCustomFieldModel.getInstance().getParticipantCustomFieldsByGroupMap(getEventId());

		Map<ParticipantCustomFieldGroup, List<ParticipantCustomField>> groupToNonEmptyCustomFieldsMap = new LinkedHashMap<>();
		for (ParticipantCustomFieldGroup group : groups) {
			List<ParticipantCustomField> allFieldsOfCurrentGroup = groupToCustomFieldsMap.get(group);
			if (!allFieldsOfCurrentGroup.isEmpty()) {
				groupToNonEmptyCustomFieldsMap.put(group, allFieldsOfCurrentGroup);
			}
		}
		return Collections.unmodifiableMap(groupToNonEmptyCustomFieldsMap);
	}


	/**
	 * Determine the text representation of a {@link ParticipantCustomFieldGroupLocation}.
	 *
	 * @return
	 * @throws Exception
	 */
	private String getLocationName(ParticipantCustomFieldGroupLocation location) throws Exception {
		String name = "";

		if (location == ParticipantCustomFieldGroupLocation.TAB_1) {
			LanguageString i18nName = eventVO.getCustomFieldTabName1();
			if (i18nName != null && ! i18nName.isEmpty()) {
				name = i18nName.getString();
			}
			else {
				name = ParticipantLabel.ParticipantCustomFieldGroupLocation_TAB_1_name.getString();
			}
		}
		else if (location == ParticipantCustomFieldGroupLocation.TAB_2) {
			LanguageString i18nName = eventVO.getCustomFieldTabName2();
			if (i18nName != null && ! i18nName.isEmpty()) {
				name = i18nName.getString();
			}
			else {
				name = ParticipantLabel.ParticipantCustomFieldGroupLocation_TAB_2_name.getString();
			}
		}
		else if (location == ParticipantCustomFieldGroupLocation.TAB_3) {
			LanguageString i18nName = eventVO.getCustomFieldTabName3();
			if (i18nName != null && ! i18nName.isEmpty()) {
				name = i18nName.getString();
			}
			else {
				name = ParticipantLabel.ParticipantCustomFieldGroupLocation_TAB_3_name.getString();
			}
		}

		return name;
	}


	/**
	 * Groups all participant custom field groups under the location they belong to
	 *
	 * @param groupList all groups without categorize the location where they belong
	 * @return a new map represent one-to-many relationship between location and groups
	 * @throws Exception
	 */
	private Map<ParticipantCustomFieldGroupLocation, List<ParticipantCustomFieldGroup>> createLocationToGroupsMap(
		List<ParticipantCustomFieldGroup> groupList
	)
	throws Exception {
		Map<ParticipantCustomFieldGroupLocation, List<ParticipantCustomFieldGroup>> mapTabAndGroups =
			new EnumMap<>(ParticipantCustomFieldGroupLocation.class);

		for (ParticipantCustomFieldGroup group : groupList) {
			ParticipantCustomFieldGroupLocation currentTab = group.getLocation();
			List<ParticipantCustomFieldGroup> currentTabGroupList = mapTabAndGroups.get(currentTab);
			if (currentTabGroupList == null) {
				currentTabGroupList = new ArrayList<>();
				mapTabAndGroups.put(currentTab, currentTabGroupList);
			}
			currentTabGroupList.add(group);
		}
		return mapTabAndGroups;
	}


	private void createProgrammeBookingsTab(TabFolder tabFolder) throws Exception {
		if (programmeBookingsTabItem == null &&
			configParameterSet.getEvent().getProgramme().isVisible()
		) {
			programmeBookingsTabItem = new TabItem(tabFolder, SWT.NONE);
			programmeBookingsTabItem.setText(ParticipantLabel.Programme.getString());
			programmeBookingsTableComposite = new ProgrammeBookingsTableComposite(tabFolder, SWT.NONE);
			programmeBookingsTabItem.setControl(programmeBookingsTableComposite);
		}
	}


	private void createHotelBookingsTab(TabFolder tabFolder) throws Exception {
		if (hotelBookingsTabItem == null &&
			configParameterSet.getEvent().getHotel().isVisible()
		) {
			hotelBookingsTabItem = new TabItem(tabFolder, SWT.NONE);
			hotelBookingsTabItem.setText(HotelLabel.Hotels.getString());
			hotelBookingsTableComposite = new HotelBookingsTableComposite(
				tabFolder,
				SWT.NONE,
				configParameterSet.getEvent().getHotel()
			);
			hotelBookingsTabItem.setControl(hotelBookingsTableComposite);

			hotelBookingsTableComposite.addModifyListener(this);
		}
	}


	private void createFinanceTab(TabFolder tabFolder) throws Exception {
		if (financeTabItem == null && configParameterSet.getEvent().getInvoice().isVisible()) {
			financeTabItem = new TabItem(tabFolder, SWT.NONE);
			financeTabItem.setText(InvoiceLabel.Finance.getString());
			financeComposite = new FinanceComposite (tabFolder, SWT.NONE, getSite());
			financeTabItem.setControl(financeComposite);
		}
	}


	private void createBadgesTab(TabFolder tabFolder) throws Exception {
		if (badgeTabItem == null && participantConfigParameterSet.getBadge().isVisible()) {
			badgeTabItem = new TabItem(tabFolder, SWT.NONE);
			badgeTabItem.setText(ParticipantLabel.Badges.getString());
			badgesComposite = new BadgesComposite(tabFolder, SWT.NONE);
			badgeTabItem.setControl(badgesComposite);
		}
	}


	private void createLeadsTab(final TabFolder tabFolder) throws Exception {
		if (leadsTabItem == null && participantConfigParameterSet.getLead().isVisible()) {
			leadsTabItem = new TabItem(tabFolder, SWT.NONE);
			leadsTabItem.setText(ParticipantLabel.Leads.getString());
			leadsComposite = new LeadsComposite(tabFolder, SWT.NONE);
			leadsTabItem.setControl(leadsComposite);

			tabFolder.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					int selectionIndex = tabFolder.getSelectionIndex();
					if (tabFolder.getItem(selectionIndex) == leadsTabItem) {
						leadsComposite.setParticipant(participant);
					}
				}
			});
		}
	}


	private void createCorrespondenceTab(TabFolder tabFolder) throws Exception {
		if (correspondenceTabItem == null && participantConfigParameterSet.getCorrespondence().isVisible()) {
			correspondenceTabItem = new TabItem(tabFolder, SWT.NONE );
			correspondenceTabItem.setText(ContactLabel.Correspondence.getString());
			correspondenceManagementComposite = new ParticipantCorrespondenceManagementComposite(tabFolder, SWT.NONE);
			correspondenceTabItem.setControl(correspondenceManagementComposite);

			correspondenceManagementComposite.addModifyListener(this);
		}
	}


	private void createDocumentsTab(TabFolder tabFolder) throws Exception {
		if (fileTabItem == null && participantConfigParameterSet.getDocument().isVisible()) {
			fileTabItem = new TabItem(tabFolder, SWT.NONE);
			fileTabItem.setText(ContactLabel.Files.getString());
			fileComposite = new ParticipantFileComposite(tabFolder, SWT.NONE);
			fileTabItem.setControl(fileComposite);
		}
	}


	private void createHistoryTab(final TabFolder tabFolder) {
		if (historyTabItem == null && participantConfigParameterSet.getHistory().isVisible()) {
			historyTabItem = new TabItem(tabFolder, SWT.NONE);
			historyTabItem.setText(UtilI18N.History);

			participantHistoryComposite = new ParticipantHistoryComposite(tabFolder, SWT.NONE);
			historyTabItem.setControl(participantHistoryComposite);
		}
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		FormatHelper formatHelper = new FormatHelper();

		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			ContactLabel.VigenereCode.getString(),
			ContactLabel.VigenereCodeAsHex.getString(),
    		ContactLabel.Vigenere2Code.getString(),
    		ContactLabel.Vigenere2CodeAsHex.getString(),
    		ContactLabel.Vigenere2CodeUrlSafe.getString(),
    		Participant.PERSON_LINK.getString(),
    		Participant.SYNC_ID.getString(),
    		Participant.WEB_ID.getString() ,
    		Participant.PORTAL.getString() ,
    		UtilI18N.CreateDateTime,
    		UtilI18N.CreateUser,
    		UtilI18N.EditDateTime,
    		UtilI18N.EditUser
		};

		// the values of the info dialog
		ArrayList<String> values = new ArrayList<>();
		values.add(String.valueOf(participant.getID()));
		values.add(participant.getVigenereCode());

		try {
			values.add(participant.getVigenereCodeHex());
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			values.add("");
		}

		values.add(participant.getVigenere2Code());

		try {
			values.add(participant.getVigenere2CodeHex());
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			values.add("");
		}

		try {
			values.add(participant.getVigenere2CodeUrlSafe());
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			values.add("");
		}

		values.add(StringHelper.avoidNull(participant.getPersonLink()));
		values.add(StringHelper.avoidNull(participant.getSyncId()));
		values.add(StringHelper.avoidNull(participant.getWebId()));
		values.add(getPortalName());
		values.add(formatHelper.formatDateTime(participant.getNewTime()));
		values.add(participant.getNewDisplayUserStr());
		values.add(formatHelper.formatDateTime(participant.getEditTime()));
		values.add(participant.getEditDisplayUserStr());


		// show info dialog
		EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			ParticipantLabel.Participant.getString() + ": " + UtilI18N.Info,
			labels,
			values.toArray(new String[]{})
		);
		infoDialog.open();
	}


	private String getPortalName() {
		String portalName = "";
		try {
			Long portalPK = participant.getPortalPK();
			if (portalPK != null) {
				Portal portal = PortalModel.getInstance().getPortal(portalPK);
				portalName = portal.getName();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return portalName;
	}


	@Override
	protected String getName() {
		String name = null;
		if (participant != null) {
			name = participant.getName();
		}
		if (StringHelper.isEmpty(name)) {
			name = I18N.ParticipantEditor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			Long eventPK = participant.getEventId();
			EventVO eventVO = evModel.getEventVO(eventPK);

			StringBuffer toolTipText = new StringBuffer();
			toolTipText.append(I18N.ParticipantEditor_DefaultToolTip);

			toolTipText.append('\n');
			toolTipText.append( Participant.EVENT.getString() );
			toolTipText.append(": ");
			toolTipText.append(eventVO.getMnemonic());

			// MIRCP-959 - Editor for a new participant shouldn't show
			// null as participant number (and probably no empty name)
			if (! isNew()) {
				toolTipText.append('\n');
				toolTipText.append(KernelLabel.Name.getString());
				toolTipText.append(": ");
				toolTipText.append(participant.getName());

				toolTipText.append('\n');
				toolTipText.append( Participant.NUMBER.getAbbreviation() );
				toolTipText.append(": ");
				toolTipText.append(participant.getNumber());
			}

			toolTip = toolTipText.toString();
		}
		catch (Exception e) {
			// This shouldn't happen
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			toolTip = "";
		}
		return toolTip;
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == paModel) {
				dataChangeParticipantModel(event);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void dataChangeParticipantModel(CacheModelEvent<Long> event) throws Exception {
		if (event.getKeyList().contains(participant.getID())) {
			// The entity of this editor was changed somehow.
			if (event.getOperation() == CacheModelOperation.DELETE) {
				closeBecauseDeletion();
			}
			else if (participant != null) {
				participant = paModel.getExtendedParticipant(participant.getID());

				if (participant != null) {
					setEntity(participant);
				}
				else if (ServerModel.getInstance().isLoggedIn()) {
					closeBecauseDeletion();
				}
			}
		}
		else {
			// follow name changes of current secondPerson to update address labels
			if (companionGroup != null) {
				companionGroup.syncEntityToWidgets();
			}
			Long secondPersonID = participant.getSecondPersonID();
			if (secondPersonID != null) {
				if (event.getKeyList().contains(secondPersonID)) {
					personGroup.refreshDefaultSalutation();
					personGroup.refreshDefaultInvitationCard();
					if (addressGroupsComposite != null) {
						addressGroupsComposite.refreshDefaultAddressLabel();
					}
				}
			}
		}
	}


	/**
	 * An editor refresh which reads the participant data anew from the server
	 */
	@Override
	public void refresh() throws Exception {
		if (participant != null) {
			final Long id = participant.getID();

			// refresh the old personLink
			Long oldPersonLink = participant.getPersonLink();
			if (oldPersonLink != null) {
				personLinkModel.refresh(oldPersonLink);
			}

			// refresh the participant, what will lead to a new instance of participant
			if (id != null) {
				paModel.refresh(id);
			}

			// refresh the new personLink
			Long newPersonLink = participant.getPersonLink();
			if (newPersonLink != null && !newPersonLink.equals(oldPersonLink)) {
				personLinkModel.refresh(newPersonLink);
			}

			// refresh detail entities (bookings, accountancy data, correspondence, documents)
			if (id != null) {
				pbModel.refreshForeignKey(id);
				hbModel.refreshForeignKey(id);

				accModel.refresh(id);

				paCorrespondenceModel.refreshForeignKey(id);
				paFileModel.refreshForeignKey(id);

				if (leadsComposite != null) {
					leadsComposite.loadAndShow(true);
				}

				paHistModel.refresh(id);
			}


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if ( isDirty() ) {
				participant = paModel.getExtendedParticipant(id);
				if (participant != null) {
					setEntity(participant);
				}
			}
		}
	}


	@Override
	public boolean isNew() {
		return participant.getID() == null;
	}


	public Participant getParticipant() {
		return participant;
	}


	/* (non-Javadoc)
	 * @see de.regasus.ui.EventProvider#getEventPK()
	 */
	@Override
	public Long getEventId() {
		Long eventPK = null;
		if (participant != null) {
			eventPK = participant.getEventId();
		}
		return eventPK;
	}


	/* (non-Javadoc)
	 * @see de.regasus.participant.ParticipantProvider#getParticipantPK()
	 */
	@Override
	public Long getParticipantPK() {
		Long participantPK = null;
		if (participant != null) {
			participantPK = participant.getID();
		}
		return participantPK;
	}


	/* (non-Javadoc)
	 * @see de.regasus.participant.ParticipantProvider#registerForForeignKey()
	 */
	@Override
	public void registerForForeignKey() {
		Long rootPK = participant.getRootPK();
		if (rootPK != null) {
			paModel.addForeignKeyListener(this, rootPK);
		}
	}


	@Override
	public IParticipant getIParticipant() {
		return participant;
	}


	/**
	 * Realizes "Datenübernahme bei Änderungen am Gruppenkopf", see
	 * {@link https://mi2.lambdalogic.de/jira//browse/MIRCP-15}
	 * @param monitor
	 */
	private void adaptGroupMembersAddresses(IProgressMonitor monitor) throws Exception {
		// ===================
		// Decide if actions are needed

		// If this participant is not a group manager, there is nothing to do
		if ( ! participant.isGroupManager()) {
			return;
		}

		monitor.subTask(I18N.AnalyzingAddressesOfGroupMembers);

		Address[] newAddressArray = new Address[ADDRESS_COUNT];
		for (int i = 0; i < ADDRESS_COUNT; i++) {
			newAddressArray[i] = participant.getAddress(i + 1);
		}

		// check which addresses have changed
		boolean[] isAddressChangedArray = new boolean[ADDRESS_COUNT];
		boolean anyAddressChanged = false;
		for (int i = 0; i < ADDRESS_COUNT; i++) {
			isAddressChangedArray[i] = !previousAddressArray[i].equalsInGroupCommonAttributes(newAddressArray[i]);
			anyAddressChanged = anyAddressChanged || isAddressChangedArray[i];
		}

		// if no addresses have changed, there is nothing to do
		if ( ! anyAddressChanged) {
			return;
		}

		/* Find all group members (or companions) who have still (some) same address that has
		 * changed in the participant originally had
		 */

		// get all group members
		List<Participant> groupMembers = paModel.getParticipantTreeByRootPK(participant.getRootPK());

		/* determine group members that have at least one address that is equal to the one of the
		 * participant and that has changed there.
		 */
		ArrayList<Participant> groupMemberWithSomeSameAddress = new ArrayList<>();
		for (Participant groupMember : groupMembers) {
			if (groupMember.getPrimaryKey().equals(participant.getPrimaryKey())) {
				// skip the group manager
				continue;
			}

			boolean[] hasSameAddressArray = new boolean[ADDRESS_COUNT];
			boolean addGroupMember = false;
			for (int i = 0; i < ADDRESS_COUNT; i++) {
				hasSameAddressArray[i] = previousAddressArray[i].equalsInGroupCommonAttributes(groupMember.getAddress(i + 1));
				addGroupMember = addGroupMember || (hasSameAddressArray[i] && isAddressChangedArray[i]);
			}

			if (addGroupMember) {
				groupMemberWithSomeSameAddress.add(groupMember);
			}
		}

		// If no group member (or companion) has the same address as the participant
		// originally had, there is also nothing to do
		if (groupMemberWithSomeSameAddress.isEmpty()) {
			return;
		}

		// Ask the user if they want to change also the addresses of the members...
		int count = groupMemberWithSomeSameAddress.size();
		String message = I18N.AdaptAddressesOfNGroupMembers_Question.replace("<n>", String.valueOf(count));
		boolean answer = MessageDialog.openQuestion(getSite().getShell(), UtilI18N.Question, message);

		// If the user doesn't want to change the addresses, there is - again - nothing to do
		if (! answer ) {
			return;
		}

		// keep the user informed
		monitor.beginTask(I18N.AdaptingAddressesOfGroupMembers, count);

		// copy address data and update participants
		for (Participant member	: groupMemberWithSomeSameAddress) {

			for (int i = 0; i < ADDRESS_COUNT; i++) {
				Address address = member.getAddress(i + 1);
				boolean hasSameAddress = address.equalsInGroupCommonAttributes(previousAddressArray[i]);
				if (isAddressChangedArray[i] && hasSameAddress) {
					address.copyGroupCommonAttributesFrom(newAddressArray[i]);
				}
			}

			paModel.update(member);
			monitor.worked(1);
		}

	}


	private void adaptCancellationState(IProgressMonitor monitor) throws Exception {
		// ===================
		// Decide if actions are needed

		// If the participant is not cancelled, there is nothing to do
		if (! participant.isCancelled()) {
			return;
		}

		// If the participant is cancelled, but was before as well, there is also nothing to do
		if (participant.isCancelled() && wasPreviouslyCancelled) {
			return;
		}


		// Find all programme bookings that are not yet cancelled
		List<ProgrammeBookingCVO> uncancelledProgrammeBookingCVOs = new ArrayList<>();
		List<ProgrammeBookingCVO> pbCVOs = pbModel.getProgrammeBookingCVOsByRecipient(participant.getID());
		for (ProgrammeBookingCVO programmeBookingCVO : pbCVOs) {
			if (! programmeBookingCVO.isCanceled()) {
				uncancelledProgrammeBookingCVOs.add(programmeBookingCVO);
			}
		}

		// Find all hotel bookings that are not yet cancelled
		List<HotelBookingCVO> uncancelledHotelBookingCVOs = new ArrayList<>();
		List<HotelBookingCVO> hbCVOs = hbModel.getHotelBookingCVOsByRecipient(participant.getID());
		for(HotelBookingCVO hotelBookingCVO : hbCVOs) {
			if (! hotelBookingCVO.isCanceled()) {
				uncancelledHotelBookingCVOs.add(hotelBookingCVO);
			}
		}

		int pbCount = uncancelledProgrammeBookingCVOs.size();
		int hbCount = uncancelledHotelBookingCVOs.size();

		// If no uncancelled bookings, there is nothing to do
		if (pbCount == 0 && hbCount == 0) {
			return;
		}

		// Ask the user if they want to cancel the bookings as well...
		int count = hbCount + pbCount;
		String message = I18N.AdaptCancellationOfNBookings_Question.replace("<n>", String.valueOf(count));
		boolean answer = MessageDialog.openQuestion(getSite().getShell(), UtilI18N.Question, message);

		// If the user doesn't want to change the addresses, there is - again - nothing to do
		if (! answer ) {
			return;
		}

		// ===================
		// Now doing some work and keep the user informed

		monitor.beginTask(I18N.AdaptCancellationOfBookings, 2);

		if (participant.getParticipantStatePK().equals(ParticipantVO.STATE_CANCEL_ORG)) {
			pbModel.cancelProgrammeBookingsWithoutFee(uncancelledProgrammeBookingCVOs);
			monitor.worked(1);

			HotelBookingModel.getInstance().cancelHotelBookingsWithoutFee(uncancelledHotelBookingCVOs);
			monitor.worked(1);
		}
		else if (participant.getParticipantStatePK().equals(ParticipantVO.STATE_CANCEL_PART)) {
			pbModel.cancelProgrammeBookingsWithCurrentTerms(uncancelledProgrammeBookingCVOs);
			monitor.worked(1);

			HotelBookingModel.getInstance().cancelHotelBookingsWithCurrentTerms(uncancelledHotelBookingCVOs);
			monitor.worked(1);
		}

		monitor.done();
	}


	public void openBadgeTab(byte[] barcodeBytes) {
		if (badgeTabItem != null) {
			tabFolder.setSelection(badgeTabItem);
			badgesComposite.setLastScanned(barcodeBytes);
		}
	}


	/**
	 * Correct the input of some fields automatically.
	 */
	public void autoCorrection() {
		if (personGroup != null) {
			personGroup.autoCorrection();
		}
		if (addressGroupsComposite != null) {
			addressGroupsComposite.autoCorrection();
		}
		if (participantBankingComposite != null) {
			participantBankingComposite.autoCorrection();
		}
	}


	// **************************************************************************
	// * Save Listener
	// *

	public static void addSaveListener(ISaveListener saveListener) {
		if (saveListenerList == null) {
			saveListenerList = new ArrayList<>();
		}

		saveListenerList.add(saveListener);
	}


	public static void removeSaveListener(ISaveListener saveListener) {
		if (saveListenerList != null) {
			saveListenerList.remove(saveListener);
		}
	}


	private void fireSaved(boolean create) {
		if (saveListenerList != null) {
			for (ISaveListener saveListener : saveListenerList) {
				saveListener.saved(this, create);
			}
		}
	}

	// *
	// * Save Listener
	// **************************************************************************


	public static boolean saveEditor(Long participantPK) {
		ParticipantEditorInput editorInput = ParticipantEditorInput.getEditInstance(participantPK);
		return AbstractEditor.saveEditor(editorInput);
	}


	public static boolean saveEditor(Collection<Long> participantPKs) {
		if (participantPKs != null) {
			for (Long participantPK : participantPKs) {
				if ( !saveEditor(participantPK) ) {
					return false;
				}
			}
		}
		return true;
	}


	@Override
	public void modifyText(ModifyEvent event) {
		super.modifyText(event);

		/* Set participant dirty if any data in ParticipantVO changed.
		 * This happens every time modifyText() is called, except the source of the event is
		 * correspondenceManagementComposite.
		 */
		if (event == null || event.widget != correspondenceManagementComposite) {
			participantDirty = true;
		}
	}

}
