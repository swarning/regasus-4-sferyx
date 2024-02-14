package de.regasus.report.wizard.common;

import static com.lambdalogic.messeinfo.hotel.report.parameter.EventOrTimePeriodReportParameter.*;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.hotel.report.parameter.EventOrTimePeriodReportParameter;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventTableComposite;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;


public class EventOrTimePeriodWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "EventOrTimePeriodWizardPage";

	private EventOrTimePeriodReportParameter parameter;

	/**
	 * To format the description
	 */
	private DateFormat dateFormat;

	// Widgets
	private DateComposite startDate;
	private DateComposite endDate;
	private EventTableComposite eventTableComposite;

	private ModifySupport modifySupport = new ModifySupport();

	private Button eventPeriodRadioButton;

	private Button timePeriodRadioButton;


	public EventOrTimePeriodWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.TimePeriodOrEvent);
		setDescription(ReportWizardI18N.TimePeriodOrEventDescription);

		dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
	}


	@Override
	public void createControl(Composite parent) {
		try {
			Composite timePeriodAndEventTableContainer = new Composite(parent, SWT.NONE);
			timePeriodAndEventTableContainer.setLayout(new GridLayout(2, false));

			Group timeOrEventContainer = new Group(timePeriodAndEventTableContainer, SWT.NONE);
			timeOrEventContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			timeOrEventContainer.setText(UtilI18N.Selection);

			timeOrEventContainer.setLayout(new GridLayout());

			timePeriodRadioButton = new Button(timeOrEventContainer, SWT.RADIO);
			timePeriodRadioButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			timePeriodRadioButton.setText(ReportWizardI18N.TimePeriod);

			eventPeriodRadioButton = new Button(timeOrEventContainer, SWT.RADIO);
			eventPeriodRadioButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			eventPeriodRadioButton.setText(ParticipantLabel.Event.getString());

			Group timePeriodContainer = new Group(timePeriodAndEventTableContainer, SWT.NONE);
			timePeriodContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			GridLayout gridLayout = new GridLayout(3, false);
			gridLayout.numColumns = 3;
			timePeriodContainer.setLayout(gridLayout);
			{
				Label lblBeginDate = new Label(timePeriodContainer, SWT.NONE);
				lblBeginDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				lblBeginDate.setText(UtilI18N.BeginTime);
			}
			GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			layoutData.widthHint = 100;
			{
				startDate = new DateComposite(timePeriodContainer, SWT.NONE);
				startDate.setLayoutData(layoutData);
				startDate.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						checkPageComplete();
					}
				});
			}
			{
				Label lblEndDate = new Label(timePeriodContainer, SWT.NONE);
				lblEndDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				lblEndDate.setText(UtilI18N.EndTime);
			}
			{
				endDate = new DateComposite(timePeriodContainer, SWT.NONE);
				endDate.setLayoutData(layoutData);
				endDate.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						checkPageComplete();
					}
				});
			}

			Composite eventTableContainer = new Composite(timePeriodAndEventTableContainer, SWT.NONE);
			eventTableContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			eventTableContainer.setLayout(new FillLayout());

			eventTableComposite = new EventTableComposite(
				eventTableContainer,
				null,	// hideEventPKs
				null,	// initSelectedEventPKs
				false,	// multiSelection,
				SWT.NONE
			);
			eventTableComposite.addModifyListener(tableListener);


			setControl(timePeriodAndEventTableContainer);


			// handle selection of radio buttons

			timePeriodRadioButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					try {
						if (!ModifySupport.isDeselectedRadioButton(event)) {
							eventTableComposite.setSelectedEvent(null);
							eventTableComposite.setEnabled(false);
							startDate.setEnabled(true);
							endDate.setEnabled(true);
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});

			eventPeriodRadioButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					try {
						if (!ModifySupport.isDeselectedRadioButton(event)) {
							eventTableComposite.setEnabled(true);
							startDate.setLocalDate(null);
							startDate.setEnabled(false);
							endDate.setLocalDate(null);
							endDate.setEnabled(false);
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private ModifyListener tableListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			checkPageComplete();
		}
	};


	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}


	@Override
	public boolean isPageComplete() {
		boolean complete = false;

		if ( eventPeriodRadioButton.getSelection() ) {
			complete = !eventTableComposite.getSelectedEvents().isEmpty();
		}
		else {
			complete = startDate.getLocalDate() != null && endDate.getLocalDate() != null;
		}

		return complete;
	}


	private void checkPageComplete() {
		setPageComplete( isPageComplete() );
	}


	public void setEndDate(Date endValue) {
		if (parameter != null) {
			// set parameter
			parameter.setEndDate(endValue);

			// set description
			String desc = null;
			if (endValue != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(UtilI18N.EndTime);
				sb.append(": ");
				sb.append(dateFormat.format(endValue));
				desc = sb.toString();
			}
			parameter.setDescription("endDate", desc);
		}

		checkPageComplete();
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof EventOrTimePeriodReportParameter) {
			parameter = (EventOrTimePeriodReportParameter) reportParameter;

			startDate.setLocalDate( TypeHelper.toLocalDate(parameter.getStartDate()) );
			endDate.setLocalDate( TypeHelper.toLocalDate(parameter.getEndDate()) );

			timePeriodRadioButton.setSelection( parameter.isSelectionByTimePeriod() );
			eventPeriodRadioButton.setSelection( parameter.isSelectionByEvent() );

			startDate.setEnabled( !parameter.isSelectionByEvent() );
			endDate.setEnabled( !parameter.isSelectionByEvent() );

			eventTableComposite.setEnabled( parameter.isSelectionByEvent() );
			eventTableComposite.setSelectedEventId( parameter.getEventPK() );
		}
	}


	@Override
	public void saveReportParameters() {
		if (parameter != null) {
			if ( eventPeriodRadioButton.getSelection() ) {
				parameter.setDescription(ATTRIBUTE_SELECTION, UtilI18N.Selection + ": " + ParticipantLabel.Event);
				parameter.setSelection(VALUE_EVENT);
			}
			else if ( timePeriodRadioButton.getSelection() ) {
				parameter.setDescription(ATTRIBUTE_SELECTION, UtilI18N.Selection + ": " + ReportWizardI18N.TimePeriod);
				parameter.setSelection(VALUE_TIME_PERIOD);
			}

			setStartDateParameter( startDate.getDate() );
			setEndDateParameter( endDate.getDate() );
			setEventParameter( eventTableComposite.getSelectedEvent() );
		}
	}


	private void setStartDateParameter(Date startDate) {
		parameter.setStartDate(startDate);

		// set description
		String desc = null;
		if (startDate != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(UtilI18N.BeginTime);
			sb.append(": ");
			sb.append(dateFormat.format(startDate));
			desc = sb.toString();
		}
		parameter.setDescription("startDate", desc);
	}


	private void setEndDateParameter(Date endDate) {
		parameter.setEndDate(endDate);

		// set description
		String desc = null;
		if (endDate != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(UtilI18N.EndTime);
			sb.append(": ");
			sb.append(dateFormat.format(endDate));
			desc = sb.toString();
		}
		parameter.setDescription("endDate", desc);
	}


	public void setEventParameter(EventVO eventVO) {
		if (eventVO != null) {
			parameter.setEventPK(eventVO.getID());
			String desc = ParticipantLabel.Event.getString() + ": " + eventVO.getName().getString();
			parameter.setDescription("event", desc);
		}
		else {
			parameter.setEventPK(null);
			parameter.setDescription("event", null);
		}
	}

}
