package de.regasus.hotel.eventhotelinfo.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.hotel.data.EventHotelInfoVO;
import com.lambdalogic.messeinfo.hotel.data.EventHotelReminderStatus;
import com.lambdalogic.messeinfo.hotel.data.EventHotelReminderVO;
import com.lambdalogic.time.I18NDateMinute;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class EventHotelReminderManagementComposite extends Composite {

	private EventHotelInfoVO eventHotelInfoVO;

	private ModifySupport modifySupport = new ModifySupport(this);

	private List<EventHotelReminderComposite> eventHotelReminderCompositeList;

	private Composite contentComposite;

	private Button createNewEventHotelReminderButton;

	private Long eventPK;

	private Long hotelPK;

	/**
	 * Create the composite. It shows scroll bars when the space is not enough
	 * for all the custom fields.
	 *
	 * @param parent
	 * @param style
	 * @param hotelPK
	 * @param eventPK
	 * @throws Exception
	 */
	public EventHotelReminderManagementComposite(final Composite tabFolder, int style, Long eventPK, Long hotelPK) {
		super(tabFolder, style);

		this.eventPK = eventPK;
		this.hotelPK = hotelPK;
		this.setLayout(new GridLayout(1, false));

		eventHotelReminderCompositeList = new ArrayList<EventHotelReminderComposite>();

		createPartControl();
	}


	protected void createPartControl() {
		contentComposite = new Composite(this, SWT.NONE);
		contentComposite.setLayout(new GridLayout(1, false));
		contentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label separatorLabel = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		createNewEventHotelReminderButton = new Button(this, SWT.PUSH);
		createNewEventHotelReminderButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		createNewEventHotelReminderButton.setText(UtilI18N.Add);
		createNewEventHotelReminderButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
    			try {
    				createNewEventHotelReminderVO(
    					null,	// Date eventTime
    					null,	// Date reminderTime
    					null,	// String subject
    					null	// String text
    				);
    			}
    			catch(Exception ex) {
    				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
    			}
			}
		});

		syncWidgetsToEntity();
	}

	public void createNewEventHotelReminderVO(
		I18NDateMinute eventTime,
		I18NDateMinute reminderTime,
		String subject,
		String text
	) {
		// create new entity
		EventHotelReminderVO newEventHotelReminderVO = new EventHotelReminderVO();
		newEventHotelReminderVO.setEventPK(eventPK);
		newEventHotelReminderVO.setHotelPK(hotelPK);
		newEventHotelReminderVO.setStatus(EventHotelReminderStatus.OPEN);

		// copy values from paraemters
		newEventHotelReminderVO.setEventTime(eventTime);
		newEventHotelReminderVO.setReminderTime(reminderTime);
		newEventHotelReminderVO.setSubject(subject);
		newEventHotelReminderVO.setText(text);

		// add new entity to HotelContingentCVO
		eventHotelInfoVO.addEventHotelReminderVO(newEventHotelReminderVO);

		// create new Composite
		final EventHotelReminderComposite eventHotelReminderComposite = new EventHotelReminderComposite(
			contentComposite,
			SWT.None
		);
		eventHotelReminderComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		eventHotelReminderComposite.setEventHotelReminderVO(newEventHotelReminderVO);
		eventHotelReminderComposite.addModifyListener(modifySupport);
		eventHotelReminderComposite.addRemoveListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				remove(eventHotelReminderComposite);
			}
		});

		// add new Composite to Composite-List
		eventHotelReminderCompositeList.add(eventHotelReminderComposite);

		modifySupport.fire();

		getParent().layout(true, true);
		SWTHelper.refreshSuperiorScrollbar(this);
		eventHotelReminderComposite.setFocus();
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

	// *
	// * Modifying
	// **************************************************************************


	public void setEventHotelInfoVO(EventHotelInfoVO eventHotelInfoVO) {
		this.eventHotelInfoVO = eventHotelInfoVO;

		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (eventHotelInfoVO != null && eventHotelInfoVO.getID() != null && contentComposite != null) {

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						List<EventHotelReminderVO> eventHotelReminderVOs = eventHotelInfoVO.getEventHotelReminderVOs();
						if (eventHotelReminderVOs == null) {
							eventHotelReminderVOs = CollectionsHelper.createArrayList(0);
						}

						while (eventHotelReminderCompositeList.size() < eventHotelReminderVOs.size()) {
							// create new Composite
							final EventHotelReminderComposite eventHotelReminderComposite = new EventHotelReminderComposite(
								contentComposite,
								SWT.NONE
							);

							eventHotelReminderComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
							eventHotelReminderCompositeList.add(eventHotelReminderComposite);
							eventHotelReminderComposite.addRemoveListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									remove(eventHotelReminderComposite);
								}
							});
						}

						while (eventHotelReminderCompositeList.size() > eventHotelReminderVOs.size()) {
							// remove last Composite
							EventHotelReminderComposite composite = eventHotelReminderCompositeList.remove(
								eventHotelReminderCompositeList.size() - 1
							);
							composite.dispose();
						}

						layout(true, true);


						// set EventHotelReminderVO to Composite
						int i = 0;
						for (EventHotelReminderVO eventHotelReminderVO : eventHotelReminderVOs) {
							// get Composite
							EventHotelReminderComposite composite = eventHotelReminderCompositeList.get(i++);

							composite.removeModifyListener(modifySupport);
							composite.setEventHotelReminderVO(eventHotelReminderVO);
							composite.addModifyListener(modifySupport);
						}

						SWTHelper.refreshSuperiorScrollbar(EventHotelReminderManagementComposite.this);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});

		}
	}


	public void syncEntityToWidgets() {
		for (EventHotelReminderComposite eventHotelReminderComposite : eventHotelReminderCompositeList) {
			eventHotelReminderComposite.syncEntityToWidgets();
		}
	}


	public void remove(EventHotelReminderComposite eventHotelReminderComposite) {
		// remove eventHotelReminderVO from eventHotelInfoVO
		EventHotelReminderVO eventHotelReminderVO = eventHotelReminderComposite.getEventHotelReminderVO();
		eventHotelInfoVO.removeEventHotelReminderVO(eventHotelReminderVO);

		// remove eventHotelReminderComposite
		eventHotelReminderCompositeList.remove(eventHotelReminderComposite);
		eventHotelReminderComposite.dispose();
		modifySupport.fire();
		layout(true, true);
		SWTHelper.refreshSuperiorScrollbar(this);
	}

}
