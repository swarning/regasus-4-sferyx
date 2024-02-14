package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static com.lambdalogic.util.StringHelper.*;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO_Position_Comparator;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.UnicodeHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.model.EntityNotFoundException;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.view.AbstractView;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.participant.ParticipantSearchModel;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.ui.Activator;

public class EventView
extends AbstractView
implements IPartListener2, CacheModelListener<Long>, ModelListener, EventIdProvider {

	public static final String ID = "EventView";

	public static final Color WHITE = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);

	/**
	 * The last value of visible as get from the ConfigParameterSet in isVisible().
	 * Has to be stored because the result of isVisible() should not change in the case that the
	 * getConfigParameterSet() returns null.
	 */
	private boolean visible = false;

	// *************************************************************************
	// * Models
	// *

	final private EventModel eventModel;
	final private ParticipantSearchModel participantSearchModel;
	final private ProgrammePointModel programmePointModel;

	private Long eventPK;

	// *************************************************************************
	// * Widgets
	// *

	/**
	 * The responsible for creating adapted SWT controls
	 */
	private Font boldFont;

	private Label nameLabel;

	private Label mnemonicLabel;

	private Label startLabel;

	private Label endLabel;

	private Label cityHeaderLabel;
	private GridData cityHeaderLabelGridData;
	private Label cityLabel;
	private GridData cityLabelGridData;

	private Label locationHeaderLabel;
	private GridData locationHeaderLabelGridData;
	private Label locationLabel;
	private GridData locationLabelGridData;

	private Label noteHeaderLabel;
	private GridData noteHeaderLabelGridData;
	private Label noteLabel;
	private GridData noteLabelGridData;

	private Label programmePointExceededWarnNumberHeaderLabel;
	private GridData programmePointExceededWarnNumberHeaderLabelGridData;
	private Label programmePointExceededWarnNumberLabel;
	private GridData programmePointExceededWarnNumberLabelGridData;

	private Label programmePointFullyBookedHeaderLabel;
	private GridData programmePointFullyBookedHeaderLabelGridData;
	private Label programmePointFullyBookedLabel;
	private GridData programmePointFullyBookedLabelGridData;

	private ScrolledForm form;

	private GridDataFactory labelGridDataFactory;
	private GridDataFactory valueGridDataFactory;


	public EventView() {
		GridData labelGridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		labelGridDataFactory = GridDataFactory.createFrom(labelGridData);

		GridData valueGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		valueGridDataFactory = GridDataFactory.createFrom(valueGridData);

		eventModel = EventModel.getInstance();
		eventModel.addListener(this);

		participantSearchModel = ParticipantSearchModel.getInstance();
		participantSearchModel.addListener(this);

		programmePointModel = ProgrammePointModel.getInstance();
		programmePointModel.addListener(this);
	}


	public Long getEventPK() {
		return eventPK;
	}


	@Override
	public Long getEventId() {
		return eventPK;
	}


	/* (non-Javadoc)
	 * @see de.regasus.core.ui.view.AbstractView#isVisible()
	 */
	@Override
	protected boolean isVisible() {
		/* Determine the visibility from the ConfigParameterSet.
		 * If getConfigParameterSet() returns null, its last result (the last value of visible)
		 * is returned.
		 */
		if (getConfigParameterSet() != null) {
			visible = getConfigParameterSet().getEvent().isVisible();
		}
		return visible;
	}


	@Override
	public void createWidgets(Composite parent) {
		if (isVisible()) {
    		boldFont = com.lambdalogic.util.rcp.Activator.getDefault().getFontFromRegistry(
    			com.lambdalogic.util.rcp.Activator.DEFAULT_FONT_BOLD
    		);

    		// Prepare the forms composite

    		// Prepare the forms composite
    		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
    		form = toolkit.createScrolledForm(parent);

    		Composite body = form.getBody();
    		body.setLayout(new GridLayout());

    		Composite eventInfoComposite = new Composite(body, SWT.NONE);
    		eventInfoComposite.setBackground(WHITE);
    		eventInfoComposite.setLayout( new GridLayout(2, false) );
    		eventInfoComposite.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, false) );

    		// create header for Event data
    		Label eventHeadingLabel = new Label(eventInfoComposite, SWT.WRAP);
    		eventHeadingLabel.setText(ParticipantLabel.Event.getString());
    		eventHeadingLabel.setFont(boldFont);
    		eventHeadingLabel.setLayoutData( new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 1) );
    		eventHeadingLabel.setBackground(WHITE);


    		// add Event data
    		nameLabel = createEntry(eventInfoComposite, UtilI18N.Name, "");
    		mnemonicLabel = createEntry(eventInfoComposite, ParticipantLabel.Mnemonic.getString(), "");
    		startLabel = createEntry(eventInfoComposite, KernelLabel.StartTime.getString(), "");
    		endLabel = createEntry(eventInfoComposite, KernelLabel.EndTime.getString(), "");

    		// city
    		cityHeaderLabel = new Label(eventInfoComposite, SWT.NONE);
    		cityHeaderLabel.setText(ParticipantLabel.Event_City.getString() + ":");
    		cityHeaderLabelGridData = labelGridDataFactory.create();
    		cityHeaderLabel.setLayoutData(cityHeaderLabelGridData);

    		cityLabel = new Label(eventInfoComposite, SWT.WRAP);
    		cityLabel.setFont(boldFont);
    		cityLabelGridData = valueGridDataFactory.create();
    		cityLabel.setLayoutData(cityLabelGridData = valueGridDataFactory.create());

    		// location
    		locationHeaderLabel = new Label(eventInfoComposite, SWT.NONE);
    		locationHeaderLabel.setText(ParticipantLabel.Event_Location.getString() + ":");
    		locationHeaderLabelGridData = labelGridDataFactory.create();
    		locationHeaderLabel.setLayoutData(locationHeaderLabelGridData);

    		locationLabel = new Label(eventInfoComposite, SWT.WRAP);
    		locationLabel.setFont(boldFont);
    		locationLabelGridData = valueGridDataFactory.create();
    		locationLabel.setLayoutData(locationLabelGridData);

    		// note
    		noteHeaderLabel = new Label(eventInfoComposite, SWT.NONE);
    		noteHeaderLabel.setText(UtilI18N.Note + ":");
    		noteHeaderLabelGridData = labelGridDataFactory.create();
    		noteHeaderLabel.setLayoutData(noteHeaderLabelGridData);

    		noteLabel = new Label(eventInfoComposite, SWT.WRAP);
    		noteLabel.setFont(boldFont);
    		noteLabelGridData = valueGridDataFactory.create();
    		noteLabel.setLayoutData(noteLabelGridData);



    		Composite programmePointComposite = new Composite(body, SWT.NONE);
    		programmePointComposite.setBackground(WHITE);
    		programmePointComposite.setLayout( new GridLayout(1, false) );
    		programmePointComposite.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, false) );

    		// create header for exceeded Programme Points
    		programmePointExceededWarnNumberHeaderLabel = new Label(programmePointComposite, SWT.WRAP);
    		programmePointExceededWarnNumberHeaderLabel.setText(I18N.EventView_ProgrammePointsWithExceededWarnNumber);
    		programmePointExceededWarnNumberHeaderLabel.setFont(boldFont);
    		programmePointExceededWarnNumberHeaderLabelGridData = new GridData(SWT.LEFT, SWT.TOP, true, false);
			programmePointExceededWarnNumberHeaderLabel.setLayoutData(programmePointExceededWarnNumberHeaderLabelGridData);

    		// create programmePointExceededWarnNumberLabel
    		programmePointExceededWarnNumberLabel = new Label(programmePointComposite, SWT.NONE);
    		programmePointExceededWarnNumberLabelGridData = valueGridDataFactory.create();
    		programmePointExceededWarnNumberLabel.setLayoutData(programmePointExceededWarnNumberLabelGridData);

    		// create header for booked out Programme Points
    		programmePointFullyBookedHeaderLabel = new Label(programmePointComposite, SWT.WRAP);
    		programmePointFullyBookedHeaderLabel.setText(I18N.EventView_FullyBookedProgrammePoints);
    		programmePointFullyBookedHeaderLabel.setFont(boldFont);
    		programmePointFullyBookedHeaderLabelGridData = new GridData(SWT.LEFT, SWT.TOP, true, false);
    		programmePointFullyBookedHeaderLabel.setLayoutData(programmePointFullyBookedHeaderLabelGridData);

    		// create programmePointExceededWarnNumberLabel
    		programmePointFullyBookedLabel = new Label(programmePointComposite, SWT.NONE);
    		programmePointFullyBookedLabelGridData = valueGridDataFactory.create();
    		programmePointFullyBookedLabel.setLayoutData(programmePointFullyBookedLabelGridData);


    		// On Windows the background color of a Label is not the same as its parent. So set it explicitly.
    		setBackground(body, WHITE);


    		getSite().getPage().addPartListener(this);

    		// Even if there are no Actions, there may be other contribution items like Commands
    		setContributionItemsVisible(true);

    		// init from current editor
    		setEventFromCurrentEditor();

    		if (eventPK == null) {
    			// init from ParticipantSearchModel
    			Long pk = participantSearchModel.getEventPK();
    			if (pk != null) {
    				setEvent(pk);
    			}
    		}
		}
		else {
			getSite().getPage().removePartListener(this);
			// delete current event to enable synchronisation with it again when the view is made visible
			setEvent(null);

			Label label = new Label(parent, SWT.NONE);
			label.setText(de.regasus.core.ui.CoreI18N.ViewNotAvailable);
		}
	}


	protected void updateProgrammePoints() {
		SWTHelper.syncExecDisplayThread(
			new Runnable() {
				@Override
				public void run() {
					try {
						_updateProgrammePoints();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			}
		);
	}


	private void _updateProgrammePoints() throws Exception {
		List<ProgrammePointCVO> programmePointCVOs = null;
		if (eventPK != null) {
			programmePointCVOs = programmePointModel.getProgrammePointCVOsWithExceededWarnNumber(eventPK);
		}
		else {
			programmePointCVOs = Collections.emptyList();
		}

		StringBuilder exceededWarnNumberSB = new StringBuilder();
		StringBuilder fullyBookedSB = new StringBuilder();

		if (notEmpty(programmePointCVOs)) {
			Collections.sort(programmePointCVOs, ProgrammePointCVO_Position_Comparator.getInstance());

			// build text for Programme Point Labels
			for (ProgrammePointCVO programmePointCVO : programmePointCVOs) {

				if (programmePointCVO.isFullyBooked()) {
					if (fullyBookedSB.length() > 0) {
						fullyBookedSB.append("\n");
					}

					fullyBookedSB.append(UnicodeHelper.BULLET);
					fullyBookedSB.append(" ");
					fullyBookedSB.append(programmePointCVO.getPpName().getString());
				}
				else {
					if (exceededWarnNumberSB.length() > 0) {
						exceededWarnNumberSB.append("\n");
					}

					exceededWarnNumberSB.append(UnicodeHelper.BULLET);
					exceededWarnNumberSB.append(" ");
					exceededWarnNumberSB.append(programmePointCVO.getPpName().getString());
					exceededWarnNumberSB.append(": ");
					exceededWarnNumberSB.append(programmePointCVO.getNumberOfBookings());
					exceededWarnNumberSB.append("/");
					exceededWarnNumberSB.append(programmePointCVO.getMaxNumber());
				}
			}

			// set values to Programme Point Labels
			programmePointExceededWarnNumberLabel.setText(exceededWarnNumberSB.toString());
			programmePointFullyBookedLabel.setText(fullyBookedSB.toString());
		}


		// set visibility of Programme Point Labels for Programme Points with exceeded warn number
		if (exceededWarnNumberSB.length() > 0) {
			programmePointExceededWarnNumberHeaderLabel.setVisible(true);
			programmePointExceededWarnNumberHeaderLabelGridData.heightHint = -1;

			programmePointExceededWarnNumberLabel.setVisible(true);
			programmePointExceededWarnNumberLabelGridData.heightHint = -1;
		}
		else {
			programmePointExceededWarnNumberHeaderLabel.setVisible(false);
			programmePointExceededWarnNumberHeaderLabelGridData.heightHint = 0;

			programmePointExceededWarnNumberLabel.setVisible(false);
			programmePointExceededWarnNumberLabelGridData.heightHint = 0;
		}


		// set visibility of Programme Point Labels for fully booked Programme Points
		if (fullyBookedSB.length() > 0) {
			programmePointFullyBookedHeaderLabel.setVisible(true);
			programmePointFullyBookedHeaderLabelGridData.heightHint = -1;

			programmePointFullyBookedLabel.setVisible(true);
			programmePointFullyBookedLabelGridData.heightHint = -1;
		}
		else {
			programmePointFullyBookedHeaderLabel.setVisible(false);
			programmePointFullyBookedHeaderLabelGridData.heightHint = 0;

			programmePointFullyBookedLabel.setVisible(false);
			programmePointFullyBookedLabelGridData.heightHint = 0;
		}

		form.pack();
		form.getParent().layout();
	}


	private Label createEntry(Composite sectionClient, String leftText, String rightText) {
		Label leftLabel = new Label(sectionClient, SWT.NONE);
		leftLabel.setText(leftText + ":");
		leftLabel.setLayoutData( labelGridDataFactory.create() );

		Label rightLabel = new Label(sectionClient, SWT.WRAP);
		rightLabel.setText(rightText);
		rightLabel.setFont(boldFont);
		rightLabel.setLayoutData( valueGridDataFactory.create() );

		return rightLabel;
	}


	private void setBackground(Control control, Color color) {
		Objects.requireNonNull(color);

		if (control != null) {
    		control.setBackground(color);

    		if (control instanceof Composite) {
    			Composite composite = (Composite) control;
    			Control[] children = composite.getChildren();
    			if (children != null) {
    				for (int i = 0; i < children.length; i++) {
    					setBackground(children[i], color);
    				}
    			}
    		}
		}
	}


	@Override
	public void dispose() {
		removeListener();

		// disconnect from EventModel
		try {
			if (eventModel != null) {
				eventModel.removeListener(EventView.this);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		// disconnect from ParticipantSearchModel
		try {
			if (participantSearchModel != null) {
				participantSearchModel.removeListener(this);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		try {
			if (programmePointModel != null) {
				programmePointModel.removeListener(this);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		super.dispose();
	}


	@Override
	protected void removeListener() {
		try {
			getSite().getPage().removePartListener(this);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Checks the current active editor (if any) whether it provides the Long of an event; if yes
	 * updates itself with the event data. If no event is given by the editor, the view contents
	 * are not cleared, which is a design decision.
	 */
	public void setEventFromCurrentEditor() {
		IWorkbenchPart activePart = getSite().getPage().getActivePart();

		if (activePart != null && activePart instanceof EventIdProvider) {
			EventIdProvider eventProvider = (EventIdProvider) activePart;
			Long newEventPK = eventProvider.getEventId();
			if (newEventPK != null) {
				// Update if not showing an event, or showing a different event
				if (this.eventPK == null || ! this.eventPK.equals(newEventPK)) {
					setEvent(newEventPK);
				}
			}
		}
	}


	private void refresh() {
		setEvent(eventPK);
	}


	private void setEvent(Long eventPK) {
		try {
			this.eventPK = eventPK;

			EventVO eventVO = null;
			if (eventPK != null) {
				eventVO = eventModel.getEventVO(eventPK);
			}


			final EventVO finalEventVO = eventVO;


			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					/* Only if the view is not made invisible by configuration.
					 * Don't check this before this thread is started, because summaryLabel may be
					 * removed in the meantime.
					 */
					if (nameLabel != null && ! nameLabel.isDisposed()) {
						String name = "";
						String mnemonic = "";
						String beginDate = "";
						String endDate = "";
						String city = "";
						String location = "";
						String note = "";

						if (finalEventVO != null) {
							name = finalEventVO.getName( Locale.getDefault() );
							mnemonic = finalEventVO.getMnemonic();
							beginDate = finalEventVO.getBeginDate().format();
							endDate = finalEventVO.getEndDate().format();
							city = avoidNull( finalEventVO.getCity() );
							location = avoidNull( finalEventVO.getLocation() );
							note = avoidNull( finalEventVO.getNote() );
						}

						nameLabel.setText(name);
						mnemonicLabel.setText(mnemonic);
						startLabel.setText(beginDate);
						endLabel.setText(endDate);
						cityLabel.setText(city);
						locationLabel.setText(location);
						noteLabel.setText(note);


						// set visibility of city
						if ( isEmpty( cityLabel.getText() ) ) {
							cityHeaderLabel.setVisible(false);
							cityHeaderLabelGridData.heightHint = 0;

							cityLabel.setVisible(false);
							cityLabelGridData.heightHint = 0;
						}
						else {
							cityHeaderLabel.setVisible(true);
							cityHeaderLabelGridData.heightHint = -1;

							cityLabel.setVisible(true);
							cityLabelGridData.heightHint = -1;
						}


						// set visibility of location
						if ( isEmpty( locationLabel.getText() ) ) {
							locationHeaderLabel.setVisible(false);
							locationHeaderLabelGridData.heightHint = 0;

							locationLabel.setVisible(false);
							locationLabelGridData.heightHint = 0;
						}
						else {
							locationHeaderLabel.setVisible(true);
							locationHeaderLabelGridData.heightHint = -1;

							locationLabel.setVisible(true);
							locationLabelGridData.heightHint = -1;
						}


						// set visibility of note
						if ( isEmpty( noteLabel.getText() ) ) {
							noteHeaderLabel.setVisible(false);
							noteHeaderLabelGridData.heightHint = 0;

							noteLabel.setVisible(false);
							noteLabelGridData.heightHint = 0;
						}
						else {
							noteHeaderLabel.setVisible(true);
							noteHeaderLabelGridData.heightHint = -1;

							noteLabel.setVisible(true);
							noteLabelGridData.heightHint = -1;
						}


						updateProgrammePoints();
					}
				}
			});

		}
		catch (EntityNotFoundException e) {
			// don't bother the user, just log
			RegasusErrorHandler.handleSilentError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void setFocus() {
		try {
			if (form != null && !form.isDisposed() && form.isEnabled()) {
				form.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	// **************************************************************************
	// * Implementation of IPartListener2
	// *

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		setEventFromCurrentEditor();
		getParent().redraw();
		getParent().update();
	}


	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}


	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
	}


	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
	}


	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
	}


	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}


	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
	}


	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
	}

	// *
	// * Implementation of IPartListener2
	// **************************************************************************


	/*
	 * React to EventModel
	 */
	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
    		if (eventPK != null) {

    			if (event.getSource() == eventModel) {
    				if (event.getKeyList().contains(eventPK)) {
    					if (event.getOperation() == CacheModelOperation.UPDATE ||
    						event.getOperation() == CacheModelOperation.REFRESH
    					) {
    						refresh();
    					}
    					else if (event.getOperation() == CacheModelOperation.DELETE) {
    						setEvent(null);
    					}
    				}
    			}
    			else if (event.getSource() == programmePointModel) {

					if (event.getOperation() != CacheModelOperation.CREATE) {
						// get affected Programme Points
						List<ProgrammePointVO> programmePointVOs = programmePointModel.getProgrammePointVOs( event.getKeyList() );

						// if one affected Programme Points belongs to the current Event: refresh Programme Points
						for (ProgrammePointVO programmePointVO : programmePointVOs) {
							if (programmePointVO.getEventPK().equals(eventPK)) {
								updateProgrammePoints();
								break;
							}
						}

					}
    			}

    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/*
	 * React to ParticipantSearchModel
	 */
	@Override
	public void dataChange(ModelEvent event) {
		Long eventPK = participantSearchModel.getEventPK();
		if (eventPK != null) {
			setEvent(eventPK);
		}
	}

}
