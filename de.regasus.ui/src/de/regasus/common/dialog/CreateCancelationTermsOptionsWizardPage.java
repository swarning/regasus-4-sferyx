package de.regasus.common.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.regasus.I18N;
import de.regasus.core.ui.groups.OptionsGroup;


public class CreateCancelationTermsOptionsWizardPage extends WizardPage {

	public static final String NAME = "CreateCancelationTermsOptionsWizardPage";
	private OptionsGroup intervalCheckOptionsGroup;
	

	public CreateCancelationTermsOptionsWizardPage() {
		super(NAME);

		setTitle(I18N.CreateCancelationTermsOptionsWizardPage_title);
		setMessage(I18N.CreateCancelationTermsOptionsWizardPage_message);
	}


	/**
	 * Shows three radio buttons to determine who should be the participant
	 */
	public void createControl(Composite parent) {
		Composite controlComposite = new Composite(parent, SWT.NONE);
		controlComposite.setLayout(new GridLayout(1, false));

		String title = I18N.CheckOverlappingIntervals;
		String option0 = I18N.CheckOverlappingIntervals_DontCreateWhenOverlappingTakesPlace;
		String option1 = I18N.CheckOverlappingIntervals_CreateAnyway;
		
		intervalCheckOptionsGroup = new OptionsGroup(controlComposite, SWT.NONE, title, option0, option1);
		intervalCheckOptionsGroup.setOptionNo(0);
		intervalCheckOptionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		setControl(controlComposite);
		setPageComplete(true);
	}

	public boolean isForceInterval() {
		return intervalCheckOptionsGroup.getOptionNo() == 1;
	}

}
