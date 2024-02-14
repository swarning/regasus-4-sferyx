package de.regasus.hotel.offering.editor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.contact.LabelTextCombinationsVO;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelBookingPaymentCondition;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.LazyScrolledTabItem;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.common.composite.LabelTextCombinationsComposite;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.LanguageModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.HotelOfferingModel;
import de.regasus.hotel.RoomDefinitionModel;

public class HotelOfferingEditor
extends AbstractEditor<HotelOfferingEditorInput>
implements CacheModelListener<Long>, IRefreshableEditorPart, EventIdProvider {

	public static final String ID = "HotelOfferingEditor";

	// the entity
	private HotelOfferingVO hotelOfferingVO;

	// ConfigParameterSet
	private ConfigParameterSet configParameterSet;

	private Long eventPK;
	private Long hotelPK;

	private List<Language> languageList;

	// models
	private HotelOfferingModel hotelOfferingModel = HotelOfferingModel.getInstance();
	private HotelContingentModel hotelContingentModel = HotelContingentModel.getInstance();
	private ConfigParameterSetModel configParameterSetModel = ConfigParameterSetModel.getInstance();


	// **************************************************************************
	// * Widgets
	// *

	private HotelOfferingGeneralComposite generalComposite;
	private HotelOfferingInfoComposite infoComposite;
	private HotelOfferingWebComposite webComposite;
	private LabelTextCombinationsComposite labelTextCombinationsComposite;

	// *
	// * Widgets
	// **************************************************************************


	@Override
	protected void init() throws Exception {
		try {
			// handle EditorInput
			Long hotelOfferingPK = editorInput.getKey();

			HotelContingentCVO hotelContingentCVO = hotelContingentModel.getHotelContingentCVO(editorInput.getHotelContingentPK());
			eventPK = hotelContingentCVO.getVO().getEventPK();
			hotelPK = hotelContingentCVO.getVO().getHotelPK();


			if (hotelOfferingPK != null) {
				// Get the entity before registration as listener at the model.
				// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

				// get entity
				hotelOfferingVO = hotelOfferingModel.getHotelOfferingVO(hotelOfferingPK);

				// register at model
				hotelOfferingModel.addListener(this, hotelOfferingPK);
			}
			else {
				// create empty entity
				hotelOfferingVO = new HotelOfferingVO();

				hotelOfferingVO.setHotelContingentPK(getHotelContingentPK());
				hotelOfferingVO.setEventPK(getEventId());

				/* Initialize PriceVOs and set default values defined in EventVO
				 */
				EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);

				PriceVO lodgePriceVO = eventVO.getHotelLodgePriceDefaultsVO().createPriceVO();
				hotelOfferingVO.setLodgePriceVO(lodgePriceVO);

				PriceVO bfPriceVO = eventVO.getHotelBreakfastPriceDefaultsVO().createPriceVO();
				hotelOfferingVO.setBfPriceVO(bfPriceVO);


				PriceVO add1PriceVO = eventVO.getHotelAdd1PriceDefaultsVO().createPriceVO();
				hotelOfferingVO.setAdd1PriceVO(add1PriceVO);

				PriceVO add2PriceVO = eventVO.getHotelAdd2PriceDefaultsVO().createPriceVO();
				hotelOfferingVO.setAdd2PriceVO(add2PriceVO);

				// further default values
//			hotelOfferingVO.setRoomDefinitionPK(firstRoomDefinition.getID());
//			hotelOfferingVO.setDescription(firstRoomDefinition.getDescription());
//			hotelOfferingVO.setBedCount(firstRoomDefinition.getGuestQuantity());
				hotelOfferingVO.setBedCount(0);
				hotelOfferingVO.setDefaultPaymentCondition(HotelBookingPaymentCondition.BOOKING_AMOUNT);
				hotelOfferingVO.setPaymentConditionAbsorptionOfCosts(true);
				hotelOfferingVO.setPaymentConditionBookingAmount(true);
				hotelOfferingVO.setPaymentConditionDeposit(true);
				hotelOfferingVO.setPaymentConditionSelfPayPatient(true);
			}


			// get ConfigParameterSet (eventPK must not be null)
			configParameterSet = configParameterSetModel.getConfigParameterSet(eventPK);

			// determine languages
			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
			List<String> languageCodes = eventVO.getLanguages();
			languageList = LanguageModel.getInstance().getLanguages(languageCodes);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	@Override
	public void dispose() {
		if (hotelOfferingModel != null && hotelOfferingVO.getID() != null) {
			try {
				hotelOfferingModel.removeListener(this, hotelOfferingVO.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(HotelOfferingVO hotelOfferingVO) throws Exception {
		if ( ! isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		hotelOfferingVO = hotelOfferingVO.clone();
		}
		this.hotelOfferingVO = hotelOfferingVO;

		generalComposite.setEntity(hotelOfferingVO);
		infoComposite.setHotelOfferingVO(hotelOfferingVO);
		webComposite.setHotelOfferingVO(hotelOfferingVO);
		labelTextCombinationsComposite.setEntity( hotelOfferingVO.getLabelTextCombinationsVO() );

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return HotelLabel.HotelOffering.getString();
	}


	@Override
	protected String getInfoButtonToolTipText() {
		return I18N.HotelOfferingEditor_InfoButtonToolTip;
	}


	/**
	 * Create contents of the editor part
	 *
	 * @param parent
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			TabFolder tabFolder = new TabFolder(parent, SWT.NONE);

			// ====== General ======================================================
			LazyScrolledTabItem generalTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			generalTabItem.setText(UtilI18N.General);

			generalComposite = new HotelOfferingGeneralComposite(
				generalTabItem.getContentComposite(),
				SWT.NONE,
				eventPK,
				hotelPK,
				getHotelContingentPK(),
				configParameterSet.getEvent().getHotel()
			);

			generalTabItem.refreshScrollbars();


			// ====== Info ======================================================
			LazyScrolledTabItem infoTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			infoTabItem.setText(UtilI18N.Info);

			infoComposite = new HotelOfferingInfoComposite(
				infoTabItem.getContentComposite(),
				SWT.NONE,
				getEventId()
			);

			infoTabItem.refreshScrollbars();


			// ====== Web ======================================================
			TabItem webTabItem = new TabItem(tabFolder, SWT.NONE);
			webTabItem.setText(UtilI18N.Web);

			webComposite = new HotelOfferingWebComposite(tabFolder, SWT.NONE);
			webTabItem.setControl(webComposite);


			// ====== LabelTextCombinations ======================================================
			LazyScrolledTabItem labelTextCombinationsTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			labelTextCombinationsTabItem.setText(UtilI18N.Texts);

			LabelTextCombinationsVO labelTextCombinationsVO = hotelOfferingVO.getLabelTextCombinationsVO();
			labelTextCombinationsComposite = new LabelTextCombinationsComposite(
				labelTextCombinationsTabItem.getContentComposite(),
				SWT.NONE,
				labelTextCombinationsVO,
				languageList
			);

			labelTextCombinationsTabItem.refreshScrollbars();


			// Setting (and synching) the entity
			setEntity(hotelOfferingVO);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}

		// after sync add this as ModifyListener to all widgets and groups
		generalComposite.addModifyListener(this);
		infoComposite.addModifyListener(this);
		webComposite.addModifyListener(this);
		labelTextCombinationsComposite.addModifyListener(this);
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

			// Show warning if the invoice number range of additional price components differ from the main price
			if (! hotelOfferingVO.hasConsistentInvoiceNumberRangeInNonEmptyPrices()) {
				MessageDialog.openWarning(getSite().getShell(), UtilI18N.Warning, I18N.InconsistentInvoiceNumberRanges_Message);
			}

			if (create) {
				/*
				 * Save new entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				hotelOfferingVO = hotelOfferingModel.create(hotelOfferingVO);
				hotelOfferingModel.addListener(this, hotelOfferingVO.getPK());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(hotelOfferingVO.getPK());

				// set new entity
				setEntity(hotelOfferingVO);
			}
			else {
				/*
				 * Save the entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				hotelOfferingModel.update(hotelOfferingVO);
				// setEntity will be called indirectly in dataChange()
			}

			monitor.worked(1);
		}
		catch (Exception t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (hotelOfferingVO != null) {
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
		if (hotelOfferingVO != null) {
			generalComposite.syncEntityToWidgets();
			infoComposite.syncEntityToWidgets();
			webComposite.syncEntityToWidgets();
			labelTextCombinationsComposite.syncEntityToWidgets();
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == hotelOfferingModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (hotelOfferingVO != null) {
					hotelOfferingVO = hotelOfferingModel.getHotelOfferingVO(hotelOfferingVO.getPK());
					if (hotelOfferingVO != null) {
						setEntity(hotelOfferingVO);
					}
					else if (ServerModel.getInstance().isLoggedIn()) {
						closeBecauseDeletion();
					}
				}
			}

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void refresh() throws Exception {
		if (hotelOfferingVO != null && hotelOfferingVO.getPK() != null) {
			hotelOfferingModel.refresh(hotelOfferingVO.getPK());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				hotelOfferingVO = hotelOfferingModel.getHotelOfferingVO(hotelOfferingVO.getPK());
				if (hotelOfferingVO != null) {
					setEntity(hotelOfferingVO);
				}
			}
		}
	}


	@Override
	protected String getName() {
		String name = "";

		try {
			if (!isNew()) {
				StringBuilder sb = new StringBuilder();

				// if present: append description in current language
				LanguageString description = hotelOfferingVO.getDescription();
				if (description != null && ! description.isEmpty()) {
					String s = description.getString();
					if (s != null) {
						if (s.length() > 20) {
							s = s.substring(0, 20) + "...";
						}
						sb.append(s);
					}
				}
				else {
					Long roomDefPK = hotelOfferingVO.getRoomDefinitionPK();
					RoomDefinitionVO roomDefVO = RoomDefinitionModel.getInstance().getRoomDefinitionVO(roomDefPK);
					String s = roomDefVO.getName().getString();
					if (s.length() > 20) {
						s = s.substring(0, 20) + "...";
					}
					sb.append(s);
				}

				// add total price per night
				StringHelper.appendIfNeeded(sb, " - ");
				String amount = hotelOfferingVO.getCurrencyAmountGross().format(false, true);
				sb.append(amount);

				name = sb.toString();
			}
			else {
				name = I18N.HotelOfferingEditor_NewName;
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return name;
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);

			Long hotelContingentPK = hotelOfferingVO.getHotelContingentPK();
			HotelContingentVO hotelContingentVO = HotelContingentModel.getInstance().getHotelContingentVO(hotelContingentPK);

			Hotel hotel = HotelModel.getInstance().getHotel(hotelPK);

			StringBuffer toolTipText = new StringBuffer();
			toolTipText.append(I18N.HotelOfferingEditor_DefaultToolTip);

			Long roomDefinitionPK = hotelOfferingVO.getRoomDefinitionPK();
			if (roomDefinitionPK != null) {

    			RoomDefinitionVO roomDefinitionVO = RoomDefinitionModel.getInstance().getRoomDefinitionVO(roomDefinitionPK);

    			toolTipText.append('\n');
    			toolTipText.append(HotelLabel.RoomDefinition.getString());
    			toolTipText.append(": ");
    			toolTipText.append(roomDefinitionVO.getName().getString());
			}

			if (hotelOfferingVO.getDescription() != null) {
				toolTipText.append('\n');
				toolTipText.append(KernelLabel.Description.getString());
				toolTipText.append(": ");
				toolTipText.append(hotelOfferingVO.getDescription().getString());
			}

			toolTipText.append('\n');
			toolTipText.append(HotelLabel.HotelContingent.getString());
			toolTipText.append(": ");
			toolTipText.append(hotelContingentVO.getName());

			toolTipText.append('\n');
			toolTipText.append(HotelLabel.Hotel.getString());
			toolTipText.append(": ");
			toolTipText.append(hotel.getName1());

			toolTipText.append('\n');
			toolTipText.append(ParticipantLabel.Event.getString());
			toolTipText.append(": ");
			toolTipText.append(eventVO.getMnemonic());

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
	public boolean isNew() {
		return hotelOfferingVO.getPK() == null;
	}


	protected Long getHotelContingentPK() {
		return editorInput.getHotelContingentPK();
	}


	@Override
	public Long getEventId() {
		return eventPK;
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			ParticipantLabel.Event.getString(),
			ParticipantLabel.EventID.getString(),
			HotelLabel.HotelContingentID.getString(),
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};

		// get name of event
		Long eventPK = hotelOfferingVO.getEventPK();
		String eventMnemonic = null;
		try {
			EventVO eventVO =  EventModel.getInstance().getEventVO(eventPK);
			if (eventVO != null) {
				eventMnemonic = eventVO.getMnemonic();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		FormatHelper formatHelper = new FormatHelper();

		// the values of the info dialog
		final String[] values = {
			StringHelper.avoidNull(hotelOfferingVO.getID()),
			eventMnemonic,
			StringHelper.avoidNull(eventPK),
			StringHelper.avoidNull(hotelOfferingVO.getHotelContingentPK()),
			formatHelper.formatDateTime(hotelOfferingVO.getNewTime()),
			hotelOfferingVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime(hotelOfferingVO.getEditTime()),
			hotelOfferingVO.getEditDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			HotelLabel.HotelOffering.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}

}
