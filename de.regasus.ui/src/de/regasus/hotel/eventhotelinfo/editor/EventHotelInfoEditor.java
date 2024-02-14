package de.regasus.hotel.eventhotelinfo.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.HotelConfigParameterSet;
import com.lambdalogic.messeinfo.contact.LabelTextCombinationsVO;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.EventHotelInfoVO;
import com.lambdalogic.messeinfo.hotel.data.EventHotelKey;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelEventStatisticCVO;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.messeinfo.hotel.data.VolumeVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventCVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.html.BrowserFactory;
import com.lambdalogic.util.rcp.widget.LazyScrolledTabItem;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.common.composite.LabelTextCombinationsComposite;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.LanguageModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.hotel.EventHotelInfoModel;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.RoomDefinitionModel;
import de.regasus.ui.Activator;

/**
 * An editor that shows on one tab a read-only presentation of the usage of volumes in one hotel for
 * one event, grouping together hotel contingents which have identical sets of room definitions.
 * <p>
 * On other tabs the editor shows the widgets for a EventHotelInfoVO for the same hotel and event,
 * which may or may not exist. If such a VO exists, it gets loaded and the widgets get filled, if
 * such a thing was loaded, but the widgets are emptied when the editor is saved, the VO gets deleted.
 */
public class EventHotelInfoEditor
extends AbstractEditor<EventHotelInfoEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long>, EventIdProvider {

	public static final String ID = "EventHotelInfoEditor";

	// **************************************************************************
	// * Models
	// *

	private EventModel eventModel = EventModel.getInstance();

	private HotelModel hotelModel = HotelModel.getInstance();

	private HotelContingentModel hotelContingentModel = HotelContingentModel.getInstance();

	private RoomDefinitionModel roomDefinitionModel = RoomDefinitionModel.getInstance();

	private EventHotelInfoModel eventHotelInfoModel = EventHotelInfoModel.getInstance();

	private ConfigParameterSetModel configParameterSetModel = ConfigParameterSetModel.getInstance();

	// **************************************************************************
	// * Entities
	// *

	private EventHotelInfoVO eventHotelInfoVO;

	private EventCVO eventCVO;

	private Hotel hotel;

	private List<HotelContingentCVO> hotelContingentCVOs;

	private Map<Long, RoomDefinitionVO> roomDefinitionMap;

	private List<Language> languageList;


	// **************************************************************************
	// * Widgets
	// *

	private TabFolder tabFolder;
	private Browser statisticsBrowser;
	private EventHotelInfoCancelationTermsComposite eventHotelInfoCancelationTermsComposite;
	private EventHotelInfoRoomCreditComposite eventHotelInfoRoomCreditComposite;
	private LabelTextCombinationsComposite labelTextCombinationsComposite;

	// **************************************************************************
	// * Other Attributes
	// *

	private Long hotelPK;

	private Long eventPK;

	private List<StatisticData> statisticDataList;

	private HotelConfigParameterSet hotelConfigParameterSet;
	private boolean reminderVisible = true;


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		EventHotelKey eventHotelKey = editorInput.getKey();

		eventPK = eventHotelKey.getEventPK();
		hotelPK = eventHotelKey.getHotelPK();

		eventCVO = eventModel.getExtendedEventCVO(eventPK);
		hotel = hotelModel.getHotel(hotelPK);
		hotelContingentCVOs = hotelContingentModel.getHotelContingentCVOsByEventAndHotel(eventPK, hotelPK);
		List<RoomDefinitionVO> roomDefinitionVOs = roomDefinitionModel.getRoomDefinitionVOsByHotelPK(hotelPK);
		roomDefinitionMap = RoomDefinitionVO.abstractVOs2Map(roomDefinitionVOs);

		// Special entity EventHotelInfoVO, we neither know whether it exists, nor its ID if it exists
		EventHotelInfoVO modelEventHotelInfoVO = eventHotelInfoModel.getEventHotelInfoByEventPKAndHotelPK(
			eventPK,
			hotelPK
		);

		if (modelEventHotelInfoVO == null) {
			// If the model gives no VO, create one to be used in EventHotelInfoComposite
			eventHotelInfoVO = new EventHotelInfoVO();
			eventHotelInfoVO.setEventPK(eventPK);
			eventHotelInfoVO.setHotelPK(hotelPK);
		}
		else {
			// If the model gives a VO, clone it so that the model doesn't get corrupted by changes in the GUI that ain't officially saved
			eventHotelInfoVO = modelEventHotelInfoVO.clone();
			eventHotelInfoModel.addListener(this, eventHotelInfoVO.getPK());
		}

		// get ConfigParameterSet (eventID must not be null)
		ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet(eventPK);
		hotelConfigParameterSet = configParameterSet.getEvent().getHotel();
		reminderVisible = hotelConfigParameterSet.getReminder().isVisible();

		// determine languages
		EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
		List<String> languageCodes = eventVO.getLanguages();
		languageList = LanguageModel.getInstance().getLanguages(languageCodes);

		// Register as listener
		eventModel.addListener(this, eventPK);
		hotelModel.addListener(this, hotelPK);
		hotelContingentModel.addForeignKeyListener(this, eventPK);
		roomDefinitionModel.addForeignKeyListener(this, hotelPK);
	}


	@Override
	public void dispose() {
		// Deregister as listener
		try {
			eventHotelInfoModel.removeListener(this, eventHotelInfoVO.getPK());
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		try {
			eventModel.removeListener(this, eventPK);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		try {
			hotelModel.removeListener(this, hotelPK);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		try {
			hotelContingentModel.removeForeignKeyListener(this, eventPK);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		try {
			roomDefinitionModel.removeForeignKeyListener(this, hotelPK);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		super.dispose();
	}


	@Override
	protected String getTypeName() {
		return HotelLabel.EventHotelInfo.getString();
	}


	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			// tabFolder
			tabFolder = new TabFolder(parent, SWT.NONE);

			// Statistics Tab
			TabItem statisticsTabItem = new TabItem(tabFolder, SWT.NONE);
			statisticsTabItem.setText(I18N.EventHotel_Statistics);
			Composite statisticsComposite = new Composite(tabFolder, SWT.NONE);
			statisticsTabItem.setControl(statisticsComposite);

			statisticsComposite.setLayout(new GridLayout());

			statisticsBrowser = BrowserFactory.createBrowser(statisticsComposite, SWT.BORDER);
			statisticsBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			// Composite for copy and print button
			Composite buttonComposite = new Composite(statisticsComposite, SWT.NONE);
			buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			buttonComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

			// Copy
			Button copyButton = new Button(buttonComposite, SWT.PUSH);
			copyButton.setText(I18N.CopyData);
			copyButton.setToolTipText(I18N.CopyData_ToolTip);
			copyButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						copyToClipboard();
					}
					catch (Exception ex) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
					}
				}
			});

			// Print
			Button printButton = new Button(buttonComposite, SWT.PUSH);
			printButton.setText(UtilI18N.Print);
			printButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						statisticsBrowser.execute("print()");
					}
					catch (Exception ex) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
					}

				}
			});


			// Tab - Event Hotel Info Cancelation Terms
			LazyScrolledTabItem eventHotelInfoCancelationTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			eventHotelInfoCancelationTabItem.setText(HotelLabel.EventHotelInfoCancelationTerms.getString());

			eventHotelInfoCancelationTermsComposite = new EventHotelInfoCancelationTermsComposite(
				eventHotelInfoCancelationTabItem.getContentComposite(),
				SWT.NONE,
				reminderVisible,
				eventPK,
				hotelPK
			);

			eventHotelInfoCancelationTabItem.refreshScrollbars();


			// Tab - RoomCredit
			LazyScrolledTabItem eventHotelInfoRoomCreditTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			eventHotelInfoRoomCreditTabItem.setText(HotelLabel.EventHotelInfoRoomCredit.getString());

			eventHotelInfoRoomCreditComposite = new EventHotelInfoRoomCreditComposite(
				eventHotelInfoRoomCreditTabItem.getContentComposite(),
				SWT.NONE
			);

			eventHotelInfoRoomCreditTabItem.refreshScrollbars();


			// Tab - LabelTextCombinations
			LazyScrolledTabItem labelTextCombinationsTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			labelTextCombinationsTabItem.setText(I18N.EventHotelInfoEditor_TextTab);

			LabelTextCombinationsVO labelTextCombinationsVO = eventHotelInfoVO.getLabelTextCombinationsVO();
			labelTextCombinationsComposite = new LabelTextCombinationsComposite(
				labelTextCombinationsTabItem.getContentComposite(),
				SWT.NONE,
				labelTextCombinationsVO,
				languageList
			);

			labelTextCombinationsTabItem.refreshScrollbars();


			// set data
			setEntity(eventHotelInfoVO);


			// add ModifyListeners
			eventHotelInfoCancelationTermsComposite.addModifyListener(this);
			eventHotelInfoRoomCreditComposite.addModifyListener(this);
			labelTextCombinationsComposite.addModifyListener(this);


			// Open cancelation/reminder tab if this editor is opened to show failed reminders
			if (reminderVisible) {
    			EventHotelInfoEditorInput input = (EventHotelInfoEditorInput) getEditorInput();
    			if (input.isShowReminderTab()) {
    				tabFolder.setSelection(eventHotelInfoCancelationTabItem);
    			}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	@Override
	protected void afterCreatePartControl() {
		/* The EventHotelInfoEditor is dirty if no corresponding entity exists. This is the default
		 * behaviour of all editors.
		 * However, from the user's point of view the EventHotelInfoEditor should not be dirty,
		 * because he clicked an existing node to open it.
		 */
		setDirty(false);
	}


	/**
	 * This save method is trickier than usual, because it might be that the info VO is to be
	 * deleted upon save when it contains no data.
	 */
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
				eventHotelInfoVO = eventHotelInfoModel.create(eventHotelInfoVO);

				// observe the Model
				eventHotelInfoModel.addListener(this, eventHotelInfoVO.getPK());

				// set new entity
				setEntity(eventHotelInfoVO);
			}
			else {
				/* Update the entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				eventHotelInfoModel.update(eventHotelInfoVO);

				// setEntity will be called indirectly in dataChange()
			}

			monitor.worked(1);
		}
		catch (Exception e) {
			monitor.setCanceled(true);
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			monitor.done();
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (!ServerModel.getInstance().isLoggedIn()) {
				close();
			}
			else if (event.getOperation() == CacheModelOperation.DELETE &&
				(
					event.getSource() == eventModel ||
					event.getSource() == hotelModel
				)
			) {
				closeBecauseDeletion();
			}
			else if (eventPK != null && hotelPK != null) {
				if (event.getSource() == eventModel) {
					eventCVO = eventModel.getExtendedEventCVO(eventPK);
					if (eventCVO == null) {
						closeBecauseDeletion();
					}
					else {
						syncNameToEntity();
						syncStatisticsDataToEntity();
					}
				}
				else if (event.getSource() == hotelModel) {
					hotel = hotelModel.getHotel(hotelPK);
					if (hotel == null) {
						closeBecauseDeletion();
					}
					else {
						syncNameToEntity();
						syncStatisticsDataToEntity();
					}
				}
				else if (event.getSource() == hotelContingentModel) {
					boolean refresh = false;

					HotelContingentPKsLoop:
					for (Long hotelContingentPK : event.getKeyList()) {
						for (HotelContingentCVO hotelContingentCVO : hotelContingentCVOs) {
							if (hotelContingentPK.equals(hotelContingentCVO.getPK())) {
								refresh = true;
								break HotelContingentPKsLoop;
							}
						}
					}

					if (refresh) {
						hotelContingentCVOs = hotelContingentModel.getHotelContingentCVOsByEventAndHotel(
							eventPK,
							hotelPK
						);
						syncStatisticsDataToEntity();
					}
				}
				else if (event.getSource() == eventHotelInfoModel) {
					if (event.getOperation() == CacheModelOperation.UPDATE
						||
						event.getOperation() == CacheModelOperation.REFRESH
					) {
						eventHotelInfoVO = eventHotelInfoModel.getEventHotelInfoByEventPKAndHotelPK(eventPK, hotelPK);

						// TODO: probably not necessary
						eventHotelInfoCancelationTermsComposite.setEventHotelInfoVO(eventHotelInfoVO);
						eventHotelInfoRoomCreditComposite.setEntity(eventHotelInfoVO);
						labelTextCombinationsComposite.setEntity( eventHotelInfoVO.getLabelTextCombinationsVO() );

						syncWidgetsToEntity();
					}
					else if (event.getOperation() == CacheModelOperation.DELETE) {
						// Although deleted on server, keep the Java object, it remains as
						// the buffer/carrier for possible new values to be stored
						eventHotelInfoVO.setID(null);
						syncWidgetsToEntity();
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
		if (eventCVO != null && eventCVO.getPK() != null) {
			eventModel.refresh(eventCVO.getPK());

			// Maybe another user created an info VO, so we must ask the model to
			eventHotelInfoModel.refreshForeignKey(new EventHotelKey(eventPK, hotelPK));


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				eventCVO = eventModel.getExtendedEventCVO(eventCVO.getPK());
				if (eventCVO != null) {
					setEntity(eventHotelInfoVO);
				}
			}
		}
	}


	protected void setEntity(EventHotelInfoVO eventHotelInfoVO) throws Exception {
		if ( ! isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			eventHotelInfoVO = eventHotelInfoVO.clone();
		}
		this.eventHotelInfoVO = eventHotelInfoVO;

		syncWidgetsToEntity();
	}


	@Override
	public boolean isNew() {
		return eventHotelInfoVO.getPK() == null;
	}


	@Override
	protected String getName() {
		return hotel.getName() + " (" + eventCVO.getLabel() + ")";
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			StringBuffer toolTipText = new StringBuffer();
			toolTipText.append(I18N.EventHotelInfoEditor_DefaultToolTip);

			toolTipText.append('\n');
			toolTipText.append(ParticipantLabel.Event.getString());
			toolTipText.append(": ");
			toolTipText.append(eventCVO.getMnemonic());

			toolTipText.append('\n');
			toolTipText.append(HotelLabel.Hotel.getString());
			toolTipText.append(": ");
			toolTipText.append(hotel.getName1());

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
	public Long getEventId() {
		return editorInput.getEventId();
	}


	// ******************************************************************************************
	// * Private helper methods

	private void syncWidgetsToEntity() {
		if (eventCVO != null) {
			syncExecInParentDisplay(new Runnable() {

				@Override
				public void run() {
					try {
						syncStatisticsDataToEntity();

						// set editor title (the name might have changed)
						// refresh the EditorInput
						syncNameToEntity();

						eventHotelInfoCancelationTermsComposite.setEventHotelInfoVO(eventHotelInfoVO);
						eventHotelInfoRoomCreditComposite.setEntity(eventHotelInfoVO);
						labelTextCombinationsComposite.setEntity( eventHotelInfoVO.getLabelTextCombinationsVO() );

						setDirty(false);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}

			});
		}
	}


	private void syncNameToEntity() {
		if (eventCVO != null) {
			syncExecInParentDisplay(new Runnable() {

				@Override
				public void run() {
					try {
						// set editor title (the name might have changed)
						setPartName(getName());
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName(getName());
						editorInput.setToolTipText(getToolTipText());
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}

			});
		}
	}


	private void syncStatisticsDataToEntity() {
		if (eventCVO != null) {
			syncExecInParentDisplay(new Runnable() {

				@Override
				public void run() {
					try {
						statisticDataList = createStatistics();

						String html = StatisticDataPresentationHelper.convertToHtml(
							statisticDataList,
							getName(),
							hotelConfigParameterSet
						);
						statisticsBrowser.setText(html);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}

			});
		}
	}


	private void syncEntityToWidgets() {
		eventHotelInfoCancelationTermsComposite.syncEntityToWidgets();
		eventHotelInfoRoomCreditComposite.syncEntityToWidgets();
		labelTextCombinationsComposite.syncEntityToWidgets();
	}


	/**
	 * Build statistical data for all combinations of Room Definitions used in all Hotel Contingents
	 * of the Hotel in this Event. The result contains one {@link StatisticData} for each combinations
	 * of Room Definitions that is used in any Hotel Contingent of the Event.
	 */
	private List<StatisticData> createStatistics() {
		List<StatisticData> statisticDataList = new ArrayList<>();

		HotelEventStatisticCVO hotelEventStatisticCVO = new HotelEventStatisticCVO(hotel.getHotelVO());
		hotelEventStatisticCVO.setHotelContingentCVOs(hotelContingentCVOs);

		// Map from a key that identifies the combination of Room Definitions to their data
		// The key contains the IDs of the Room Definitions in their natural order.
		// The value is a List of RoomDefinitionVO.
		HashMap<String, List<RoomDefinitionVO>> key2RoomDefinitionVOListMap =
			MapHelper.createHashMap( hotelContingentCVOs.size() );

		// Map from a key that identifies the combination of Room Definitions to VolumeVOs
		// The key contains the IDs of the Room Definitions in their natural order.
		// The value is a Map from a day to the List of VolumeVOs.
		HashMap<String, TreeMap<I18NDate, List<VolumeVO>>> key2Date2VolumeVOListMapMap =
			MapHelper.createHashMap(hotelContingentCVOs.size());


		for (HotelContingentCVO hotelContingentCVO : hotelContingentCVOs) {
			String key = getRoomDefinitionPKCombinationKey( hotelContingentCVO.getRoomDefinitionPKs() );


			/* Create a List of the RoomDefinitionVOs of the HotelContingent by the roomDefinitionPKs
			 * from the HotelContingentCVO and the roomDefinitionMap.
			 */
			List<RoomDefinitionVO> roomDefinitionVOs = new ArrayList<>( roomDefinitionMap.size() );
			for (Long roomDefinitionPK : hotelContingentCVO.getRoomDefinitionPKs()) {
				RoomDefinitionVO roomDefinitionVO = roomDefinitionMap.get(roomDefinitionPK);

				// just in case the Room Definition is not in the Map, try to load it directly
				if (roomDefinitionVO == null) {
					try {
						roomDefinitionVO = roomDefinitionModel.getRoomDefinitionVO(roomDefinitionPK);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
				if (roomDefinitionVO != null) {
					roomDefinitionVOs.add(roomDefinitionVO);
				}
			}
			key2RoomDefinitionVOListMap.put(key, roomDefinitionVOs);


			// The volumes are to be assembled on a per-day-basis (using TreeMap spares later sorting)
			TreeMap<I18NDate, List<VolumeVO>> date2VolumeVOListMap = key2Date2VolumeVOListMapMap.get(key);
			if (date2VolumeVOListMap == null) {
				date2VolumeVOListMap = new TreeMap<>();
				key2Date2VolumeVOListMapMap.put(key, date2VolumeVOListMap);
			}


			for (VolumeVO volumeVO : hotelContingentCVO.getVolumes()) {
				if (!date2VolumeVOListMap.containsKey(volumeVO.getDay())) {
					date2VolumeVOListMap.put(volumeVO.getDay(), new ArrayList<VolumeVO>());
				}
				List<VolumeVO> volumeVOList = date2VolumeVOListMap.get(volumeVO.getDay());
				volumeVOList.add(volumeVO);
			}
		}

		// We iterate over all different combinations of Room Definitions which have Volumes

		for (String key : key2Date2VolumeVOListMapMap.keySet()) {
			StatisticData statisticData = new StatisticData();
			TreeMap<I18NDate, List<VolumeVO>> date2VolumeListMap = key2Date2VolumeVOListMapMap.get(key);

			// We print the room definition names for the current set
			List<RoomDefinitionVO> roomDefinitions = key2RoomDefinitionVOListMap.get(key);

			for (RoomDefinitionVO roomDefinitionVO : roomDefinitions) {
				statisticData.addRoomDefinitionName(roomDefinitionVO.getName().getString());
			}
			// We get the map of (sorted) dates and the corresponding volume list

			// For each date there are volumes for...
			for (I18NDate volumeDate : date2VolumeListMap.keySet()) {
				int trueSize = 0;
				int bookSize = 0;
				int booked = 0;

				// ...we add up all counters for bookings and capacity
				for (VolumeVO volumeVO : date2VolumeListMap.get(volumeDate)) {
					trueSize += volumeVO.getTrueSize().intValue();
					bookSize += volumeVO.getBookSize().intValue();
					booked += volumeVO.getUsed().intValue();
				}

				StatisticDatum statisticDatum = new StatisticDatum();
				statisticDatum.date = volumeDate;
				statisticDatum.booked = booked;
				statisticDatum.trueSize = trueSize;
				statisticDatum.bookSize = bookSize - trueSize;
				statisticDatum.free = bookSize - booked;

				statisticData.addStatisticDatum(statisticDatum);
			}
			statisticDataList.add(statisticData);
		}

		return statisticDataList;
	}


	/**
	 * Copy CSV data to system clipboard
	 */
	protected void copyToClipboard() {
		String content = StatisticDataPresentationHelper.convertToTabSeparatedValues(
			statisticDataList,
			getName(),
			hotelConfigParameterSet
		);

		ClipboardHelper.copyToClipboard(content);
	}


	/**
	 * Makes from a collection of Longs a String that combines all of them in their natural order, to be used as key.
	 */
	private String getRoomDefinitionPKCombinationKey(Collection<Long> roomDefinitionPKCollection) {
		ArrayList<Long> tmpList = new ArrayList<>(roomDefinitionPKCollection);
		Collections.sort(tmpList);
		return tmpList.toString();
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
			HotelLabel.Hotel.getString(),
			HotelLabel.HotelID.getString(),
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};

		FormatHelper formatHelper = new FormatHelper();

		// the values of the info dialog
		final String[] values = {
			StringHelper.avoidNull(eventHotelInfoVO.getID()),
			eventCVO.getMnemonic(),
			StringHelper.avoidNull(eventPK),
			hotel.getName1(),
			StringHelper.avoidNull(hotel.getID()),
			formatHelper.formatDateTime(eventHotelInfoVO.getNewTime()),
			eventHotelInfoVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime(eventHotelInfoVO.getEditTime()),
			eventHotelInfoVO.getEditDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			HotelLabel.EventHotelInfo.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}

}
