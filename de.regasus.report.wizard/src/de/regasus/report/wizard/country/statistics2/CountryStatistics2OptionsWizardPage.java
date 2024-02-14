package de.regasus.report.wizard.country.statistics2;

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

import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.participant.report.countryStatistics.CountryStatistics2ReportParameter;
import com.lambdalogic.messeinfo.participant.report.countryStatistics.GroupingMode;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;

public class CountryStatistics2OptionsWizardPage extends WizardPage implements IReportWizardPage {
	public static final String ID = "de.regasus.report.wizard.country.statistics2.CountryStatistics2OptionsWizardPage";

	private boolean ignoreSelectionEvents = false;

	private Group groupingModeGroup;
	private Button groupByCountryButton;
	private Button groupByRegion1Button;
	private Button groupByRegion2Button;
	private Button groupByRegion3Button;
	private Button groupByRegion4Button;
	private Button groupByRegion5Button;

	private Button separateGroupManagersButton;

	private CountryStatistics2ReportParameter countryStatistics2ReportParameter;

	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			try {
				if (!ignoreSelectionEvents && !ModifySupport.isDeselectedRadioButton(event)) {
					syncReportParameter();
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	/**
	 * Create the wizard
	 */
	public CountryStatistics2OptionsWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.CountryStatistics2OptionsWizardPage_Title);
		setDescription(ReportWizardI18N.CountryStatistics2OptionsWizardPage_Description);
	}


	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout());
		setControl(container);


		Composite mainComposite = new Composite(container, SWT.NONE);
		mainComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		mainComposite.setLayout(new GridLayout());


		groupingModeGroup = new Group(mainComposite, SWT.NONE);
		groupingModeGroup.setText(ReportWizardI18N.CountryStatistics2OptionsWizardPage_GroupByLabel);
		groupingModeGroup.setToolTipText(ReportWizardI18N.CountryStatistics2OptionsWizardPage_GroupByToolTip);
		groupingModeGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		groupingModeGroup.setLayout(new GridLayout());

		groupByCountryButton = new Button(groupingModeGroup, SWT.RADIO);
		groupByCountryButton.setText( ContactLabel.Country.getString() );
		groupByCountryButton.addSelectionListener(selectionListener);

		groupByRegion1Button = new Button(groupingModeGroup, SWT.RADIO);
		groupByRegion1Button.setText( ContactLabel.region1.getString() );
		groupByRegion1Button.addSelectionListener(selectionListener);

		groupByRegion2Button = new Button(groupingModeGroup, SWT.RADIO);
		groupByRegion2Button.setText( ContactLabel.region2.getString() );
		groupByRegion2Button.addSelectionListener(selectionListener);

		groupByRegion3Button = new Button(groupingModeGroup, SWT.RADIO);
		groupByRegion3Button.setText( ContactLabel.region3.getString() );
		groupByRegion3Button.addSelectionListener(selectionListener);

		groupByRegion4Button = new Button(groupingModeGroup, SWT.RADIO);
		groupByRegion4Button.setText( ContactLabel.region4.getString() );
		groupByRegion4Button.addSelectionListener(selectionListener);

		groupByRegion5Button = new Button(groupingModeGroup, SWT.RADIO);
		groupByRegion5Button.setText( ContactLabel.region5.getString() );
		groupByRegion5Button.addSelectionListener(selectionListener);


		new Label(mainComposite, SWT.NONE);

		separateGroupManagersButton = new Button(mainComposite, SWT.CHECK);
		separateGroupManagersButton.setText(ReportWizardI18N.CountryStatistics2OptionsWizardPage_SeparateGroupManagersLabel);
		separateGroupManagersButton.setToolTipText(ReportWizardI18N.CountryStatistics2OptionsWizardPage_SeparateGroupManagersToolTip);
		separateGroupManagersButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		separateGroupManagersButton.addSelectionListener(selectionListener);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof CountryStatistics2ReportParameter) {

			ignoreSelectionEvents = true;
			try {
    			countryStatistics2ReportParameter = (CountryStatistics2ReportParameter) reportParameter;

    			GroupingMode groupingMode = countryStatistics2ReportParameter.getGroupingMode();
    			groupByCountryButton.setSelection(groupingMode == GroupingMode.COUNTRY);
    			groupByRegion1Button.setSelection(groupingMode == GroupingMode.REGION_1);
    			groupByRegion2Button.setSelection(groupingMode == GroupingMode.REGION_2);
    			groupByRegion3Button.setSelection(groupingMode == GroupingMode.REGION_3);
    			groupByRegion4Button.setSelection(groupingMode == GroupingMode.REGION_4);
    			groupByRegion5Button.setSelection(groupingMode == GroupingMode.REGION_5);

    			separateGroupManagersButton.setSelection(countryStatistics2ReportParameter.isSeparateGroupManagers());
			}
			finally {
				ignoreSelectionEvents = false;
			}
		}
	}


	private void syncReportParameter() {
		// get values
		GroupingMode groupingMode = null;
		if (groupByCountryButton.getSelection()) {
			groupingMode = GroupingMode.COUNTRY;
		}
		else if (groupByRegion1Button.getSelection()) {
			groupingMode = GroupingMode.REGION_1;
		}
		else if (groupByRegion2Button.getSelection()) {
			groupingMode = GroupingMode.REGION_2;
		}
		else if (groupByRegion3Button.getSelection()) {
			groupingMode = GroupingMode.REGION_3;
		}
		else if (groupByRegion4Button.getSelection()) {
			groupingMode = GroupingMode.REGION_4;
		}
		else if (groupByRegion5Button.getSelection()) {
			groupingMode = GroupingMode.REGION_5;
		}

		boolean separateGroupManagers = separateGroupManagersButton.getSelection();

		// Werte in ReportParameter setzen
		if (groupingMode != null) {
			countryStatistics2ReportParameter.setGroupByValue(groupingMode);
		}
		countryStatistics2ReportParameter.setSeparateGroupManagers(separateGroupManagers);


		// description setzen
		StringBuilder desc = new StringBuilder();

		desc.append(ReportWizardI18N.CountryStatistics2OptionsWizardPage_GroupByLabel);
		desc.append(": ");
		desc.append(groupingMode.toString());

		desc.append("\n");

		desc.append(ReportWizardI18N.CountryStatistics2OptionsWizardPage_SeparateGroupManagersLabel);
		desc.append(": ");
		if (separateGroupManagers) {
			desc.append(UtilI18N.Yes);
		}
		else {
			desc.append(UtilI18N.No);
		}

		countryStatistics2ReportParameter.setDescription(CountryStatistics2ReportParameter.DESCRIPTION_ID, desc.toString());


		setPageComplete(isPageComplete());
	}


	@Override
	public boolean isPageComplete() {
		GroupingMode groupingMode = countryStatistics2ReportParameter.getGroupingMode();
		return groupingMode != null;
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}
