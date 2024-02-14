package de.regasus.event.editor;

import static com.lambdalogic.util.StringHelper.*;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.config.parameterset.EventConfigParameterSet;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.combo.DigitalEventProviderCombo;
import de.regasus.programme.programmepoint.combo.ProgrammePointCombo;
import de.regasus.ui.Activator;

public class DigitalEventGroup extends Group {

	// the entity
	private EventVO eventVO;


	protected ModifySupport modifySupport = new ModifySupport(this);

	protected EventConfigParameterSet eventConfigParameterSet;

	// **************************************************************************
	// * Widgets
	// *

	private DigitalEventProviderCombo providerCombo;
	private Text urlText;
	private DateTimeComposite startDateTimeComposite;
	private DateTimeComposite endDateTimeComposite;
	private ProgrammePointCombo programmePointCombo;
	private Text participantWebTokenClaimsText;

	// *
	// * Widgets
	// **************************************************************************


	public DigitalEventGroup(Composite parent, int style, EventConfigParameterSet eventConfigParameterSet)
	throws Exception {
		super(parent, style);

		this.eventConfigParameterSet = eventConfigParameterSet;

		setText( ParticipantLabel.DigitalEvent.getString() );

		createWidgets();
	}


	private void createWidgets() throws Exception {
		/* layout with 4 columns
		 */
		final int COL_COUNT = 4;
		setLayout(new GridLayout(COL_COUNT, false));

		GridDataFactory widgetGridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER);

		/*
		 * Row 1
		 */

		/*** Digital Event Provider  ***/
		Label providerLabel = SWTHelper.createLabel(this, EventVO.DIGITAL_EVENT_PROVIDER.getLabel(), false);
		providerLabel.setToolTipText( EventVO.DIGITAL_EVENT_PROVIDER.getDescription() );

		providerCombo = new DigitalEventProviderCombo(this, SWT.BORDER);
		widgetGridDataFactory.applyTo(providerCombo);
		providerCombo.addModifyListener(modifySupport);


		/*** URL ***/
		Label urlLabel = SWTHelper.createLabel(this, EventVO.DIGITAL_EVENT_URL.getLabel(), false);
		urlLabel.setToolTipText( EventVO.DIGITAL_EVENT_URL.getDescription() );

		urlText = new Text(this, SWT.BORDER);
		widgetGridDataFactory.copy().grab(true, false).applyTo(urlText);
		urlText.addModifyListener(modifySupport);


		/*
		 * Row 2
		 */

		/*** start time ***/
		Label startTimeLabel = SWTHelper.createLabel(this, EventVO.DIGITAL_EVENT_START_TIME.getLabel());
		startTimeLabel.setToolTipText( EventVO.DIGITAL_EVENT_START_TIME.getDescription() );

		startDateTimeComposite = new DateTimeComposite(this, SWT.BORDER);
		widgetGridDataFactory.applyTo(startDateTimeComposite);
		startDateTimeComposite.addModifyListener(modifySupport);


		/*** end time ***/
		Label endTimeLabel = SWTHelper.createLabel(this, EventVO.DIGITAL_EVENT_END_TIME.getLabel());
		endTimeLabel.setToolTipText( EventVO.DIGITAL_EVENT_END_TIME.getDescription() );

		endDateTimeComposite = new DateTimeComposite(this, SWT.BORDER);
		widgetGridDataFactory.applyTo(endDateTimeComposite);
		endDateTimeComposite.addModifyListener(modifySupport);


		/*
		 * Row 3
		 */
		Label programmePointLabel = SWTHelper.createLabel(this, EventVO.DIGITAL_EVENT_LEAD_PROGRAMME_POINT.getLabel());
		programmePointLabel.setToolTipText( EventVO.DIGITAL_EVENT_LEAD_PROGRAMME_POINT.getDescription() );

		programmePointCombo = new ProgrammePointCombo(this, SWT.NONE);
		widgetGridDataFactory
			.copy()
			.span(COL_COUNT - 1, 1)
			.grab(true, false)
			.applyTo(programmePointCombo);

		programmePointCombo.addModifyListener(modifySupport);


		/*
		 * Row 4
		 */

		if ( eventConfigParameterSet.getParticipantWebTokenClaims().isVisible() ) {
    		/*** Participant Web Token Claims ***/
    		Label participantWebTokenClaimsLabel = SWTHelper.createLabel(this, EventVO.PARTICIPANT_WEB_TOKEN_CLAIMS.getLabel(), false);
    		participantWebTokenClaimsLabel.setToolTipText( EventVO.PARTICIPANT_WEB_TOKEN_CLAIMS.getDescription() );

    		participantWebTokenClaimsText = new Text(this, SWT.BORDER);
    		widgetGridDataFactory
    			.copy()
    			.span(COL_COUNT - 1, 1)
    			.grab(true, false)
    			.applyTo(participantWebTokenClaimsText);
    		participantWebTokenClaimsText.addModifyListener(modifySupport);
		}
	}


	public void setEvent(EventVO eventVO) {
		this.eventVO = eventVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (eventVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						providerCombo.setDigitalEventProvider( eventVO.getDigitalEventProvider() );
						urlText.setText( avoidNull(eventVO.getDigitalEventUrl()) );
						startDateTimeComposite.setI18NDateMinute( eventVO.getDigitalEventStart() );
						endDateTimeComposite.setI18NDateMinute( eventVO.getDigitalEventEnd() );

						programmePointCombo.setEventPK( eventVO.getID() );
						programmePointCombo.setProgrammePointPK( eventVO.getDigitalEventLeadProgrammePointId() );

						if (participantWebTokenClaimsText != null) {
							participantWebTokenClaimsText.setText( avoidNull(eventVO.getParticipantWebTokenClaims()) );
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (eventVO != null) {
			eventVO.setDigitalEventProvider( providerCombo.getDigitalEventProvider() );
			eventVO.setDigitalEventUrl( trim(urlText.getText()) );
			eventVO.setDigitalEventStart( startDateTimeComposite.getI18NDateMinute() );
			eventVO.setDigitalEventEnd( endDateTimeComposite.getI18NDateMinute() );
			eventVO.setDigitalEventLeadProgrammePointId( programmePointCombo.getProgrammePointPK() );

			if (participantWebTokenClaimsText != null) {
				eventVO.setParticipantWebTokenClaims( trim(participantWebTokenClaimsText.getText()) );
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

	// *
	// * Modifying
	// **************************************************************************


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
