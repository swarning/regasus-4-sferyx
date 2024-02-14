package de.regasus.report.wizard.hotel.guestlist;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.report.hotelGuestList.HotelGuestListReportParameter;
import com.lambdalogic.messeinfo.hotel.report.hotelGuestList.HotelGuestListSort;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionListenerAdapter;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;

public class HotelGuestListOptionsWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "HotelGuestListOptionsWizardPage";

	private HotelGuestListReportParameter parameter;

	/**
	 * Zeigt an, wir uns in der Methode init() befinden.
	 * Die Methode syncReportParameter() bricht sofort ab, wenn dies der Fall ist.
	 */
	private boolean initializing = false;

	/**
	 * Zur Formatierung der Description
	 */
	private DateFormat dateFormat;

	// Widgets
	private Button currentStateButton;
	private Button changesSinceButton;

	private Label separatorLabel;

	private Label referenceTimeLabel;
	private DateTimeComposite referenceTime;

	private Label lastReportTimeLabel;
	private DateTimeComposite lastReportTime;
	private Button changesSinceLastReportButton;

	private Button sortByArrivalButton;
	private Button sortByNameButton;

	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			try {
				if (!ModifySupport.isDeselectedRadioButton(event)) {
					syncReportParameter();
					refreshWidgets();
				}
			}
			catch (Throwable e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};

	SelectionListenerAdapter modifyListener = new SelectionListenerAdapter(selectionListener);


	public HotelGuestListOptionsWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.HotelGuestListOptionsWizardPage_Title);
		setDescription(ReportWizardI18N.HotelGuestListOptionsWizardPage_Description);
		dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
	}


	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite controlComposite = new Composite(parent, SWT.NULL);
		controlComposite.setLayout(new GridLayout());
		setControl(controlComposite);

		Composite mainComposite = new Composite(controlComposite, SWT.NONE);
		mainComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		mainComposite.setLayout(new GridLayout(1, false));


		Group dataGroup = new Group(mainComposite, SWT.NONE);
		dataGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		dataGroup.setLayout(new GridLayout(3, false));
		dataGroup.setText(ReportWizardI18N.HotelGuestListOptionsWizardPage_ReportData);

		// Row 1 - "Aktueller Buchungsstand"
		currentStateButton = new Button(dataGroup, SWT.RADIO);
		currentStateButton.setText(ReportWizardI18N.HotelGuestListOptionsWizardPage_CurrentState);
		currentStateButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

		// Row 2 - "Änderungen seit"
		changesSinceButton = new Button(dataGroup, SWT.RADIO);
		changesSinceButton.setText(ReportWizardI18N.HotelGuestListOptionsWizardPage_ChangesSinceReferenceTime);
		changesSinceButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));

		// Separator
		separatorLabel = new Label(dataGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		// Row 3
		referenceTimeLabel = new Label(dataGroup, SWT.LEFT);
		referenceTimeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		referenceTimeLabel.setText(ReportWizardI18N.HotelGuestListOptionsWizardPage_ReferenceTime);

		referenceTime = new DateTimeComposite(dataGroup, SWT.BORDER);
		WidgetSizer.setWidth(referenceTime);

		// placeholder
		new Label(dataGroup, SWT.NONE);



		// Row 4 - "Letzter Bericht"
		lastReportTimeLabel = new Label(dataGroup, SWT.LEFT);
		lastReportTimeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		lastReportTimeLabel.setText(ReportWizardI18N.HotelGuestListOptionsWizardPage_LastReportGeneration);

		lastReportTime = new DateTimeComposite(dataGroup, SWT.BORDER);
		WidgetSizer.setWidth(lastReportTime);

		// "Schaltfläche kopiert diesen Wert in das Feld "Änderungen seit" und setzt "Letzter Bericht" auf den aktuellen Zeitpunkt"
		changesSinceLastReportButton = new Button(dataGroup, SWT.PUSH);
		changesSinceLastReportButton.setText(ReportWizardI18N.HotelGuestListOptionsWizardPage_UseLastReportAsReferenceTime);
		changesSinceLastReportButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		changesSinceLastReportButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyLastReportTimeToReferenceTime();
			}
		});



		Group sortGroup = new Group(mainComposite, SWT.NONE);
		sortGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		sortGroup.setLayout(new GridLayout(1, false));
		sortGroup.setText(ReportWizardI18N.HotelGuestListOptionsWizardPage_Sorting);

		sortByArrivalButton = new Button(sortGroup, SWT.RADIO);
		sortByArrivalButton.setText(HotelLabel.HotelBooking_Arrival.getString());

		sortByNameButton = new Button(sortGroup, SWT.RADIO);
		sortByNameButton.setText(Person.LAST_NAME.getString() + ", " + Person.FIRST_NAME.getString());


		// observe widgets

		currentStateButton.addSelectionListener(selectionListener);
		changesSinceButton.addSelectionListener(selectionListener);

		referenceTime.addModifyListener(modifyListener);
		lastReportTime.addModifyListener(modifyListener);

		sortByArrivalButton.addSelectionListener(selectionListener);
		sortByNameButton.addSelectionListener(selectionListener);
	}


	protected void copyLastReportTimeToReferenceTime() {
		Date lastReportDate = lastReportTime.getDate();
		if (lastReportDate != null) {
			referenceTime.setDate(lastReportDate);
		}

		lastReportTime.setDate(new Date());

		syncReportParameter();
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof HotelGuestListReportParameter) {
			initializing = true;
			try {
    			parameter = (HotelGuestListReportParameter) reportParameter;
    			Date referenceTimeValue = parameter.getReferenceTime();
    			if (referenceTimeValue == null) {
    				Calendar cal = Calendar.getInstance();
    				cal.set(Calendar.HOUR_OF_DAY, 0);
    				cal.set(Calendar.MINUTE, 0);
    				cal.set(Calendar.SECOND, 0);
    				cal.set(Calendar.MILLISECOND, 0);
    				referenceTime.setDate(cal.getTime());
    			}
    			else {
    				referenceTime.setDate(referenceTimeValue);
    			}


    			Date lastReportTimeValue = parameter.getLastReportTime();
    			if (lastReportTimeValue == null) {
    				lastReportTimeValue = referenceTimeValue;
    			}
    			lastReportTime.setDate(lastReportTimeValue);


    			currentStateButton.setSelection(referenceTimeValue == null);
    			changesSinceButton.setSelection(referenceTimeValue != null);


    			// sorting
    			HotelGuestListSort sort = parameter.getSort();
    			sortByArrivalButton.setSelection(sort == HotelGuestListSort.ARRIVAL);
    			sortByNameButton.setSelection(sort == HotelGuestListSort.LAST_NAME);



    			refreshWidgets();
			}
			finally {
				initializing = false;
			}
		}
	}


	private void syncReportParameter() {
		if (!initializing) {
    		// determine values
    		Date referenceTimeValue = null;
    		Date lastReportTimeValue = null;
    		if (changesSinceButton.getSelection()) {
    			referenceTimeValue = referenceTime.getDate();
    			lastReportTimeValue = lastReportTime.getDate();
    		}

    		HotelGuestListSort sort = null;
    		if (sortByArrivalButton.getSelection()) {
    			sort = HotelGuestListSort.ARRIVAL;
    		}
    		else if (sortByNameButton.getSelection()) {
    			sort = HotelGuestListSort.LAST_NAME;
    		}


    		// set values to ReportParameter
    		parameter.setReferenceTime(referenceTimeValue);
    		parameter.setLastReportTime(lastReportTimeValue);
    		parameter.setSort(sort);


    		// set description for report data (current state or changes since referenceTIme
    		I18NPattern desc = new I18NPattern();
    		desc.append(ReportWizardI18N.HotelGuestListOptionsWizardPage_ReportData);
    		desc.append(": ");
    		if (referenceTimeValue == null) {
    			desc.append(ReportWizardI18N.HotelGuestListOptionsWizardPage_CurrentState);
    		}
    		else {
    			desc.append(ReportWizardI18N.HotelGuestListOptionsWizardPage_ChangesSinceReferenceTime);
    			desc.append(" ");
    			desc.append(dateFormat.format(referenceTimeValue));
    		}

    		parameter.setDescription(HotelGuestListReportParameter.DESCRIPTION_ID_REFERENCE_TIME, desc.toString());


    		// set description for last report generation
    		I18NPattern lastReportGenerationDesc = new I18NPattern();
    		if (lastReportTimeValue != null) {
        		lastReportGenerationDesc.append(ReportWizardI18N.HotelGuestListOptionsWizardPage_LastReportGeneration);
        		lastReportGenerationDesc.append(": ");
        		lastReportGenerationDesc.append(dateFormat.format(parameter.getLastReportTime()));
    		}
    		parameter.setDescription(HotelGuestListReportParameter.DESCRIPTION_ID_LAST_REPORT_TIME, lastReportGenerationDesc.toString());


    		// set description for sorting
    		I18NPattern sortDesc = new I18NPattern();
    		sortDesc.append(ReportWizardI18N.HotelGuestListOptionsWizardPage_Sorting);
    		sortDesc.append(": ");
    		sortDesc.append(sort.getLabel());

    		parameter.setDescription(HotelGuestListReportParameter.DESCRIPTION_ID_SORT, sortDesc.toString());
		}
	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	private void refreshWidgets() {
		// enable/disable widgets
		boolean changes = changesSinceButton.getSelection();

		separatorLabel.setVisible(changes);
		referenceTimeLabel.setVisible(changes);
		referenceTime.setVisible(changes);
		lastReportTime.setVisible(changes);
		lastReportTimeLabel.setVisible(changes);
		changesSinceLastReportButton.setVisible(changes);
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}
