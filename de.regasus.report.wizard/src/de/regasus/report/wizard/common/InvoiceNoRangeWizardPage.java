// REFERENCE
package de.regasus.report.wizard.common;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.invoice.report.parameter.IInvoiceNoRangeReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.InvoiceNoRangeModel;
import de.regasus.finance.invoice.InvoiceNoRangeTable;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;


public class InvoiceNoRangeWizardPage extends WizardPage implements IReportWizardPage {
	public static final String ID = "InvoiceNoRangeWizardPage";

	private InvoiceNoRangeModel invoiceNoRangeModel;
	private IInvoiceNoRangeReportParameter invoiceNoRangeReportParameter;

	private TableViewer tableViewer;


	public InvoiceNoRangeWizardPage() {
		super(ID);
		setTitle(InvoiceLabel.InvoiceNoRange.getString());
		setDescription(ReportWizardI18N.InvoiceNoRangeWizardPage_Description);
		invoiceNoRangeModel = InvoiceNoRangeModel.getInstance();
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

		Table table = new Table(container, SWT.FULL_SELECTION | SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		{	// Name
    		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
    		tableColumn.setWidth(200);
    		tableColumn.setText(InvoiceLabel.InvoiceNoRange_Name.getString());
		}
		{	// Start No
    		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
    		tableColumn.setWidth(80);
    		tableColumn.setText(InvoiceLabel.InvoiceNoRange_StartNo.getString());
		}
		{	// End No
    		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
    		tableColumn.setWidth(80);
    		tableColumn.setText(InvoiceLabel.InvoiceNoRange_EndNo.getString());
		}

		tableViewer = new InvoiceNoRangeTable(table).getViewer();

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete( !event.getSelection().isEmpty() );
			}
		});

		// init models
		try {
			tableViewer.setInput( invoiceNoRangeModel.getAllInvoiceNoRangeCVOs() );
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public void init(IReportParameter reportParameter) {
		try {
			if (reportParameter instanceof IInvoiceNoRangeReportParameter) {
				invoiceNoRangeReportParameter = (IInvoiceNoRangeReportParameter) reportParameter;

				if (tableViewer != null) {
					InvoiceNoRangeCVO invoiceNoRangeCVO = null;
					Long invoiceNoRangePK = invoiceNoRangeReportParameter.getInvoiceNoRangePK();
					if (invoiceNoRangePK != null) {
						invoiceNoRangeCVO = invoiceNoRangeModel.getInvoiceNoRangeCVO(invoiceNoRangePK);
					}

					StructuredSelection selection = null;
					if (invoiceNoRangeCVO != null) {
						selection = new StructuredSelection(invoiceNoRangeCVO);
					}
					else {
						selection = new StructuredSelection();
					}
					tableViewer.setSelection(selection, true);
				}
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	@Override
	public void saveReportParameters() {
		InvoiceNoRangeCVO invoiceNoRangeCVO = SelectionHelper.getUniqueSelected(tableViewer);
		Long invoiceNoRangePK = invoiceNoRangeCVO != null ? invoiceNoRangeCVO.getPK() : null;

		if (invoiceNoRangeReportParameter != null) {
			invoiceNoRangeReportParameter.setInvoiceNoRangePK(invoiceNoRangePK);

			String description = buildDescription(invoiceNoRangeCVO);
			invoiceNoRangeReportParameter.setDescription(IInvoiceNoRangeReportParameter.DESCRIPTION_ID, description);
		}
	}


	private String buildDescription(InvoiceNoRangeCVO invoiceNoRangeCVO) {
		String description = null;
		if (invoiceNoRangeCVO != null) {
			I18NPattern i18nPattern = new I18NPattern();
			i18nPattern.append(InvoiceLabel.InvoiceNoRange.getString());
			i18nPattern.append(": ");
			i18nPattern.append(invoiceNoRangeCVO.getVO().getName());

			description = i18nPattern.getString();
		}
		return description;
	}

}
