package de.regasus.event.dialog;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.DateHelper;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.I18N;

public class CopyEventWizardEventPage extends WizardPage {

	public static final String NAME = "CopyEventWizardEventPage";

	private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

	private CopyEventWizard wizard;
	private EventVO eventVO;

	private Label destPeriod;

	private NullableSpinner yearSpinner;

	private NullableSpinner monthSpinner;

	private NullableSpinner daySpinner;

	private Date newStartTime;
	private Date newEndTime;

	private Text mnemonicText;


	public CopyEventWizardEventPage(CopyEventWizard wizard) {
		super(NAME);
		this.wizard = wizard;

		eventVO = this.wizard.getEventVO();

		newStartTime = eventVO.getStartTime();
		newEndTime = eventVO.getEndTime();

		setTitle(I18N.CopyEventWizardEventPage_Title);
		setMessage(I18N.CopyEventWizardEventPage_Message);
	}


	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		// **************************************************************************
		// * timeShiftGroup
		// *

		// create the group
		Group timeShiftGroup = new Group(composite, SWT.NONE);
		timeShiftGroup.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 2, 1));
		timeShiftGroup.setLayout(new GridLayout(7, false));
		timeShiftGroup.setText(I18N.CopyEventWizardEventPage_timeShiftGroup);

		// 1. row: original peroid
		Label originalPeriodLabel = new Label(timeShiftGroup, SWT.NONE);
		originalPeriodLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		originalPeriodLabel.setText(I18N.CopyEventWizardEventPage_originalPeriod);

		Label originalPeriod = new Label(timeShiftGroup, SWT.NONE);
		originalPeriod.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 6, 0));
		String originalPeroidStr = getDatePeroidString(eventVO.getStartTime(), eventVO.getEndTime());
		originalPeriod.setText(originalPeroidStr);

		// 2. row destination peroid
		Label destPeriodLabel = new Label(timeShiftGroup, SWT.NONE);
		destPeriodLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		destPeriodLabel.setText(I18N.CopyEventWizardEventPage_destPeriod);

		destPeriod = new Label(timeShiftGroup, SWT.NONE);
		destPeriod.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 6, 0));
		destPeriod.setText(originalPeroidStr);

		// 3. row: time shift controls
		Label timeShiftLabel = new Label(timeShiftGroup, SWT.NONE);
		timeShiftLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		timeShiftLabel.setText(I18N.CopyEventWizardEventPage_timeShift);

		yearSpinner = new NullableSpinner(timeShiftGroup, SWT.NONE);
		yearSpinner.setMinimum(-99);
		yearSpinner.setMaximum(99);
		yearSpinner.setValue(0);
		// calculate and set the maximum width
		WidgetSizer.setWidth(yearSpinner);
		yearSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				refreshDestPeriod();
			}
		});

		Label yearLabel = new Label(timeShiftGroup, SWT.NONE);
		yearLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		yearLabel.setText(I18N.CopyEventWizardEventPage_year);

		monthSpinner = new NullableSpinner(timeShiftGroup, SWT.NONE);
		GridData monthSpinnerLayoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		monthSpinnerLayoutData.horizontalIndent = 15;
		monthSpinner.setLayoutData(monthSpinnerLayoutData);
		monthSpinner.setMinimum(-11);
		monthSpinner.setMaximum(11);
		monthSpinner.setValue(0);
		// calculate and set the maximum width
		WidgetSizer.setWidth(monthSpinner);
		monthSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				refreshDestPeriod();
			}
		});

		Label monthLabel = new Label(timeShiftGroup, SWT.NONE);
		monthLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		monthLabel.setText(I18N.CopyEventWizardEventPage_month);


		daySpinner = new NullableSpinner(timeShiftGroup, SWT.NONE);
		GridData daySpinnerLayoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		daySpinnerLayoutData.horizontalIndent = 15;
		daySpinner.setLayoutData(daySpinnerLayoutData);
		daySpinner.setMinimum(-9999);
		daySpinner.setMaximum(9999);
		daySpinner.setValue(0);
		// calculate and set the maximum width
		WidgetSizer.setWidth(daySpinner);
		daySpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				refreshDestPeriod();
			}
		});

		Label dayLabel = new Label(timeShiftGroup, SWT.NONE);
		dayLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		dayLabel.setText(I18N.CopyEventWizardEventPage_day);

		// *
		// * timeShiftGroup
		// **************************************************************************

		Label mnemonicLabel = new Label(composite, SWT.NONE);
		mnemonicLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		mnemonicLabel.setText(ParticipantLabel.Event_Mnemonic.getString());

		mnemonicText = new Text(composite, SWT.BORDER);
		mnemonicText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		mnemonicText.setTextLimit(EventVO.MAX_LENGTH_MNEMONIC);
		mnemonicText.setText(eventVO.getMnemonic());


		setControl(composite);
	}


	private void refreshDestPeriod() {
		newStartTime = eventVO.getStartTime();
		newEndTime = eventVO.getEndTime();

		int dayDiff = DateHelper.dayDiff(newStartTime, newEndTime);

		int years = yearSpinner.getValueAsInteger();
		if (years != 0) {
			newStartTime = DateHelper.addYears(newStartTime, years);
		}

		int months = monthSpinner.getValueAsInteger();
		if (months != 0) {
			newStartTime = DateHelper.addMonths(newStartTime, months);
		}

		int days = daySpinner.getValueAsInteger();
		if (days != 0) {
			newStartTime = DateHelper.addDays(newStartTime, days);
		}

		newEndTime = DateHelper.addDays(newStartTime, dayDiff);

		String peroidStr = getDatePeroidString(newStartTime, newEndTime);
		destPeriod.setText(peroidStr);
	}


	private String getDatePeroidString(Date startDate, Date endDate) {
		StringBuilder sb = new StringBuilder();
		sb.append(dateFormat.format(startDate));
		sb.append(" - ");
		sb.append(dateFormat.format(endDate));

		return sb.toString();
	}


	public int getDayShift() {
		int dayShift = DateHelper.dayDiff(eventVO.getStartTime(), newStartTime);
		return dayShift;
	}


	public String getMnemonic() {
		return mnemonicText.getText();
	}

}
