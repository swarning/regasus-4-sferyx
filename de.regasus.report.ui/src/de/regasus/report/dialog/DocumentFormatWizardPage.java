package de.regasus.report.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.report.od.DocumentFormat;
import com.lambdalogic.report.oo.OpenOfficeConstants;
import com.lambdalogic.report.oo.OpenOfficeHelper;
import com.lambdalogic.report.parameter.IFormatReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.report.ReportI18N;

public class DocumentFormatWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "DocumentFormatWizardPage";

	private ListViewer listViewer;
	private IFormatReportParameter formatReportParameter;


	public DocumentFormatWizardPage() {
		super(ID);
		setTitle(ReportI18N.DocumentFormatWizardPage_Title);
		setDescription(ReportI18N.DocumentFormatWizardPage_Description);
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
				return ((DocumentFormat) element).getName();
			}
		});
		listViewer.setSorter(new ViewerSorter());

		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete( !event.getSelection().isEmpty() );
			}
		});
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof IFormatReportParameter) {
			formatReportParameter = (IFormatReportParameter) reportParameter;

			if (listViewer != null) {
				List<String> availableFormatKeys = formatReportParameter.getAvailableFormats();
				List<DocumentFormat> availableDocumentFormats = null;
				if (availableFormatKeys != null) {
					availableDocumentFormats = new ArrayList<>(availableFormatKeys.size());
					for (String formatKey : availableFormatKeys) {
						DocumentFormat documentFormat = OpenOfficeHelper.getDocumentFormatByFormatKey(formatKey);
						availableDocumentFormats.add(documentFormat);
					}
					listViewer.setInput(availableDocumentFormats);
				}
				else {
					// if there's no list of available formats, show all
					listViewer.setInput(OpenOfficeConstants.DOC_FORMATS);
				}


				String formatKey = formatReportParameter.getFormat();
				if (formatKey == null) {
					listViewer.setSelection(new StructuredSelection());
				}
				else {
					DocumentFormat documentFormat = OpenOfficeHelper.getDocumentFormatByFormatKey(formatKey);
					listViewer.setSelection(new StructuredSelection(documentFormat), true);
				}
			}
		}
	}


	@Override
	public void saveReportParameters() {
		if (formatReportParameter != null) {
    		DocumentFormat documentFormat = SelectionHelper.getUniqueSelected(listViewer);
    		String formatKey = documentFormat != null ? documentFormat.getFormatKey() : null;

			formatReportParameter.setFormat(formatKey);

			String description = buildDescription(documentFormat);
			formatReportParameter.setDescription(IFormatReportParameter.DESCRIPTION_ID, description);
		}
	}


	private String buildDescription(DocumentFormat documentFormat) {
		String description = null;
		if (documentFormat != null) {
			I18NPattern i18nPattern = new I18NPattern();
			i18nPattern.append(ReportI18N.DocumentFormatWizardPage_FileFormat);
			i18nPattern.append(": ");
			i18nPattern.append( documentFormat.getName() );

			description = i18nPattern.getString();
		}
		return description;
	}

}
