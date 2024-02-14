package de.regasus.report.wizard.common;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.invoice.data.CurrencyVO;
import com.lambdalogic.messeinfo.invoice.report.parameter.ICurrencyReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.CurrencyModel;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;


public class CurrencyWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "CurrencyWizardPage";

	private ListViewer listViewer;
	private ICurrencyReportParameter currencyReportParameter;

	private CurrencyModel currencyModel;


	public CurrencyWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.CurrencyWizardPage_Title);
		setDescription(ReportWizardI18N.CurrencyWizardPage_Description);

		currencyModel = CurrencyModel.getInstance();
	}


	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());
		//
		setControl(container);

		listViewer = new ListViewer(container, SWT.V_SCROLL | SWT.BORDER);
		listViewer.setContentProvider(new ArrayContentProvider());
		listViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((CurrencyVO) element).getCurrency();
			}
		});
		listViewer.setSorter(new ViewerSorter());

		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete( !event.getSelection().isEmpty() );
			}
		});


		// init models
		try {
			listViewer.setInput( currencyModel.getAllCurrencyVOs() );
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof ICurrencyReportParameter) {
			currencyReportParameter = (ICurrencyReportParameter) reportParameter;

			if (listViewer != null) {
				String currency = currencyReportParameter.getCurrency();
				if (currency == null) {
					listViewer.setSelection(new StructuredSelection());
				}
				else {
					try {
						CurrencyVO currencyVO = currencyModel.getCurrencyVO(currency);
						listViewer.refresh();
						listViewer.setSelection(new StructuredSelection(currencyVO), true);
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
				}
			}
		}
	}


	@Override
	public void saveReportParameters() {
		String selectedCurrency = null;
		IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
		if (selection.size() == 1) {
			CurrencyVO selectedCurrencyVO = (CurrencyVO) selection.getFirstElement();
			selectedCurrency = selectedCurrencyVO.getCurrency();
		}

		if (currencyReportParameter != null) {
			currencyReportParameter.setCurrency(selectedCurrency);

			StringBuilder desc = new StringBuilder();
			if (selectedCurrency != null) {
    			desc.append(ReportWizardI18N.CurrencyWizardPage_Currency);
    			desc.append(": ");
    			desc.append(selectedCurrency);

			}
			currencyReportParameter.setDescription(ICurrencyReportParameter.DESCRIPTION_ID, desc.toString());
		}
	}

}
