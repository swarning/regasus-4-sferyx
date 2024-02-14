package de.regasus.report.wizard.hotel.reminder.list;

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.EventHotelReminderStatus;
import com.lambdalogic.messeinfo.hotel.report.eventHotelReminderList.EventHotelReminderListReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.CollectionsHelper;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;


public class EventHotelReminderStatusWizardPage extends WizardPage implements IReportWizardPage, SelectionListener {

	public static final String ID = "EventHotelReminderStatusWizardPage";

	private EventHotelReminderListReportParameter parameter;


	// Widgets
	private Button openButton;
	private Button doneButton;
	private Button errorButton;
	private Button errorAckButton;


	public EventHotelReminderStatusWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.EventHotelReminderStatusWizardPage_Title);
		setDescription(ReportWizardI18N.EventHotelReminderStatusWizardPage_Description);
	}


	@Override
	public void createControl(Composite parent) {
		Composite controlComposite = new Composite(parent, SWT.NULL);
		controlComposite.setLayout(new GridLayout());
		setControl(controlComposite);

		Composite widgetComposite = new Composite(controlComposite, SWT.NONE);
		widgetComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		widgetComposite.setLayout(new GridLayout(1, false));

		// Booking Type
		Group statusGroup = new Group(widgetComposite, SWT.NONE);
		statusGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		statusGroup.setText(HotelLabel.EventHotelInfoReminderStatus.getString());
		statusGroup.setLayout(new GridLayout(1, false));

		openButton = new Button(statusGroup, SWT.CHECK);
		openButton.setText(EventHotelReminderStatus.OPEN.getString());
		openButton.setToolTipText(EventHotelReminderStatus.OPEN.getDescription());
		openButton.addSelectionListener(this);

		doneButton = new Button(statusGroup, SWT.CHECK);
		doneButton.setText(EventHotelReminderStatus.DONE.getString());
		doneButton.setToolTipText(EventHotelReminderStatus.DONE.getDescription());
		doneButton.addSelectionListener(this);

		errorButton = new Button(statusGroup, SWT.CHECK);
		errorButton.setText(EventHotelReminderStatus.ERROR.getString());
		errorButton.setToolTipText(EventHotelReminderStatus.ERROR.getDescription());
		errorButton.addSelectionListener(this);

		errorAckButton = new Button(statusGroup, SWT.CHECK);
		errorAckButton.setText(EventHotelReminderStatus.ERROR_ACK.getString());
		errorAckButton.setToolTipText(EventHotelReminderStatus.ERROR_ACK.getDescription());
		errorAckButton.addSelectionListener(this);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof EventHotelReminderListReportParameter) {
			parameter = (EventHotelReminderListReportParameter) reportParameter;

			openButton.setSelection(	parameter.containsStatus(EventHotelReminderStatus.OPEN));
			doneButton.setSelection(	parameter.containsStatus(EventHotelReminderStatus.DONE));
			errorButton.setSelection(	parameter.containsStatus(EventHotelReminderStatus.ERROR));
			errorAckButton.setSelection(parameter.containsStatus(EventHotelReminderStatus.ERROR_ACK));
		}
	}


	private void syncReportParameter() {
		// create statusList from scratch to have the values in a defined order
		List<EventHotelReminderStatus> statusList = CollectionsHelper.createArrayList(EventHotelReminderStatus.values().length);
		if (openButton.getSelection()) {
			statusList.add(EventHotelReminderStatus.OPEN);
		}
		if (doneButton.getSelection()) {
			statusList.add(EventHotelReminderStatus.DONE);
		}
		if (errorButton.getSelection()) {
			statusList.add(EventHotelReminderStatus.ERROR);
		}
		if (errorAckButton.getSelection()) {
			statusList.add(EventHotelReminderStatus.ERROR_ACK);
		}
		parameter.setEventHotelReminderStatusList(statusList);

		// description setzen
		StringBuilder desc = new StringBuilder();

		desc.append(HotelLabel.EventHotelInfoReminderStatus.getString());
		desc.append(": ");
		if (CollectionsHelper.empty(statusList)) {
			desc.append(UtilI18N.All);
		}
		else {
			String language = parameter.getLanguage();

			int statusCount = 0;
			for (EventHotelReminderStatus status : statusList) {
				if (statusCount++ > 0) {
					desc.append(", ");
				}
				desc.append(status.getString(language));
			}
		}

		parameter.setDescription(EventHotelReminderListReportParameter.STATUS_DESCRIPTION_ID, desc.toString());
	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}


	@Override
	public void widgetSelected(SelectionEvent e) {
		syncReportParameter();
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}
