package de.regasus.report.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.report.DocumentContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.IImageKeys;
import de.regasus.report.IconRegistry;
import de.regasus.report.ReportI18N;
import de.regasus.report.ui.Activator;
import de.regasus.report.view.GenerateReportAction;

public class ReportWizardDialog extends WizardDialog {
	
	protected IReportWizard reportWizard;
	private Button generateReportButton;
	private DocumentContainer template;
	
	
	public ReportWizardDialog(Shell parentShell, IReportWizard newWizard, DocumentContainer template) {
		super(parentShell, newWizard);
		setPageSize(newWizard.getPreferredSize());
		this.reportWizard = newWizard;
		this.template = template;
	}

	@Override
	protected void backPressed() {
		// Wizard informieren
		try {
			IWizardPage currentPage = getCurrentPage();
			reportWizard.backPressed((IReportWizardPage) currentPage);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		super.backPressed();
	}

	@Override
	protected void nextPressed() {
		// Wizard informieren
		try {
			IWizardPage currentPage = getCurrentPage();
			reportWizard.nextPressed((IReportWizardPage) currentPage);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		super.nextPressed();
	}
	
	@Override
	protected void finishPressed() {
		// Wizard informieren
		try {
			IWizardPage currentPage = getCurrentPage();
			reportWizard.finishPressed((IReportWizardPage) currentPage);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		super.finishPressed();
	}
	
	@Override
	protected void cancelPressed() {
		// Wizard informieren
		try {
			IWizardPage currentPage = getCurrentPage();
			reportWizard.cancelPressed((IReportWizardPage) currentPage);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		super.cancelPressed();
		
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		generateReportButton = createGenerateReportButton(parent);
		super.createButtonsForButtonBar(parent);
	}

	
	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		if (id == IDialogConstants.FINISH_ID) {
			label = ReportI18N.ReportWizardDialog_FinishButton;
		}
		return super.createButton(parent, id, label, defaultButton);
	}

	@Override
	public void updateButtons() {
		super.updateButtons();
		
		boolean canFinish = getWizard().canFinish();
		generateReportButton.setEnabled(canFinish);
	}

	private Button createGenerateReportButton(Composite parent) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.PUSH);
		button.setText(ReportI18N.ReportWizardDialog_GenerateButton);
		button.setImage(IconRegistry.getImage(IImageKeys.GENERATE_REPORT));
		setButtonLayoutData(button);
		button.setFont(parent.getFont());

		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					GenerateReportAction.generateReport(reportWizard.getReportParameter().getXMLContainer(), template);
				}
				catch (Throwable t) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}
			}
		});
		
		return button;
	}

}
