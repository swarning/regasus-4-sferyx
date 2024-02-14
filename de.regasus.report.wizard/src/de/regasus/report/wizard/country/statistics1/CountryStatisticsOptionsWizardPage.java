package de.regasus.report.wizard.country.statistics1;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.report.countryStatistics.CountryStatisticsReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;

public class CountryStatisticsOptionsWizardPage
extends WizardPage
implements IReportWizardPage, SelectionListener {
//	private static Logger log = Logger.getLogger("ui.CountryStatisticsOptionsWizardPage"); 

	private Button withPaidRegistrationCountButton;
	private Button withRegistrationCountButton;
	private Button withParticipantCountButton;

	public static final String ID = "de.regasus.report.wizard.country.statistics1.CountryStatisticsOptionsWizardPage"; 

	private CountryStatisticsReportParameter countryStatisticsReportParameter;

	/**
	 * Create the wizard
	 */
	public CountryStatisticsOptionsWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.CountryStatisticsOptionsWizardPage_Title);
		setDescription(ReportWizardI18N.CountryStatisticsOptionsWizardPage_Description);
	}


	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout());
		//
		setControl(container);

		final Composite composite = new Composite(container, SWT.NONE);
		final GridData gd_composite = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout());

		withParticipantCountButton = new Button(composite, SWT.CHECK);
		withParticipantCountButton.setText(ReportWizardI18N.CountryStatisticsOptionsWizardPage_WithParticipantCountLabel);
		withParticipantCountButton.setToolTipText(ReportWizardI18N.CountryStatisticsOptionsWizardPage_WithParticipantCountToolTip);
		withParticipantCountButton.addSelectionListener(this);

		withRegistrationCountButton = new Button(composite, SWT.CHECK);
		withRegistrationCountButton.setText(ReportWizardI18N.CountryStatisticsOptionsWizardPage_WithRegistrationCountLabel);
		withRegistrationCountButton.setToolTipText(ReportWizardI18N.CountryStatisticsOptionsWizardPage_WithRegistrationCountToolTip);
		withRegistrationCountButton.addSelectionListener(this);

		withPaidRegistrationCountButton = new Button(composite, SWT.CHECK);
		withPaidRegistrationCountButton.setText(ReportWizardI18N.CountryStatisticsOptionsWizardPage_WithPaidRegistrationCountLabel);
		withPaidRegistrationCountButton.setToolTipText(ReportWizardI18N.CountryStatisticsOptionsWizardPage_WithPaidRegistrationCountToolTip);
		withPaidRegistrationCountButton.addSelectionListener(this);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof CountryStatisticsReportParameter) {
			countryStatisticsReportParameter = (CountryStatisticsReportParameter) reportParameter;

			withParticipantCountButton.setSelection(countryStatisticsReportParameter.isWithParticipantCount());
			withRegistrationCountButton.setSelection(countryStatisticsReportParameter.isWithRegistrationCount());
			withPaidRegistrationCountButton.setSelection(countryStatisticsReportParameter.isWithPaidRegistrationCount());
		}
	}


	private void syncReportParameter() {
		// Werte ermitteln
		boolean withParticipantCount = withParticipantCountButton.getSelection();
		boolean withRegistrationCount = withRegistrationCountButton.getSelection();
		boolean withPaidRegistrationCount = withPaidRegistrationCountButton.getSelection();

		// Werte in ReportParameter setzen
		countryStatisticsReportParameter.setWithParticipantCount(withParticipantCount);
		countryStatisticsReportParameter.setWithRegistrationCount(withRegistrationCount);
		countryStatisticsReportParameter.setWithPaidRegistrationCount(withPaidRegistrationCount);

		// description setzen
		StringBuilder desc = new StringBuilder();

		desc.append(ReportWizardI18N.CountryStatisticsOptionsWizardPage_WithParticipantCountLabel);
		desc.append(": "); 
		if (withParticipantCount) {
			desc.append(UtilI18N.Yes);
		}
		else {
			desc.append(UtilI18N.No);
		}

		desc.append("\n"); 
		desc.append(ReportWizardI18N.CountryStatisticsOptionsWizardPage_WithRegistrationCountLabel);
		desc.append(": "); 
		if (withRegistrationCount) {
			desc.append(UtilI18N.Yes);
		}
		else {
			desc.append(UtilI18N.No);
		}

		desc.append("\n"); 
		desc.append(ReportWizardI18N.CountryStatisticsOptionsWizardPage_WithPaidRegistrationCountLabel);
		desc.append(": "); 
		if (withPaidRegistrationCount) {
			desc.append(UtilI18N.Yes);
		}
		else {
			desc.append(UtilI18N.No);
		}

		countryStatisticsReportParameter.setDescription(CountryStatisticsReportParameter.DESCRIPTION_ID, desc.toString());
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
