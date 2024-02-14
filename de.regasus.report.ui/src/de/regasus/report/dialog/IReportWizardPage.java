package de.regasus.report.dialog;

import org.eclipse.jface.wizard.IWizardPage;

import com.lambdalogic.report.parameter.IReportParameter;

public interface IReportWizardPage extends IWizardPage {

    /**
     * Initialize the {@link IReportWizardPage}.
     * The page reads the data from the passed {@link IReportParameter} object and initializes its content accordingly.
     * 
     * @param reportParameter
     */
	void init(IReportParameter reportParameter);

	/**
	 * Save the current state of the {@link IReportWizardPage} to the {@link IReportParameter}.
     */
	void saveReportParameters();
	
}
