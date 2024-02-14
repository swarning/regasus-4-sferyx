package de.regasus.report.dialog;

import java.util.Locale;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.contact.AbstractPerson;
import com.lambdalogic.report.parameter.ILanguageReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.ReportI18N;
import de.regasus.report.ui.Activator;

public class LanguageWizardPage extends WizardPage implements IReportWizardPage {

	private Button moreLessButton;
	private boolean filterOn = true;
	public static final String ID = "LanguageWizardPage";

	private LanguageModel languageModel;
	private ListViewer listViewer;
	private LanguageFilter languageFilter;
	private List list;
	private ILanguageReportParameter languageReportParameter;
	private String language;


	public LanguageWizardPage() {
		super(ID);
		setTitle(ReportI18N.LanguageWizardPage_Title);
		setDescription(ReportI18N.LanguageWizardPage_Description);
		languageModel = LanguageModel.getInstance();
		language = Locale.getDefault().getLanguage();
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

		listViewer = new ListViewer(container, SWT.V_SCROLL | SWT.BORDER);
		list = listViewer.getList();
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		listViewer.setContentProvider(new ArrayContentProvider());
		listViewer.setLabelProvider(new LabelProvider() {
			@Override
			@SuppressWarnings("unchecked")
			public String getText(Object element) {
				return ((Language) element).getName().getString(language);
			}
		});
		listViewer.setSorter(new ViewerSorter());

		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete( !event.getSelection().isEmpty() );
			}
		});


		languageFilter = new LanguageFilter();
		languageFilter.addLanguage("de");
		languageFilter.addLanguage("en");
		languageFilter.addLanguage("fr");
		listViewer.addFilter(languageFilter);

		moreLessButton = new Button(container, SWT.NONE);
		moreLessButton.setLayoutData(new GridData(80, SWT.DEFAULT));
		moreLessButton.setText(ReportI18N.LanguageWizardPage_More);
		moreLessButton.setToolTipText(ReportI18N.LanguageWizardPage_MoreToolTip);
		moreLessButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (filterOn) {
					listViewer.removeFilter(languageFilter);
					moreLessButton.setText(ReportI18N.LanguageWizardPage_Less);
					moreLessButton.setToolTipText(ReportI18N.LanguageWizardPage_LessToolTip);
				}
				else {
					listViewer.addFilter(languageFilter);
					moreLessButton.setText(ReportI18N.LanguageWizardPage_More);
					moreLessButton.setToolTipText(ReportI18N.LanguageWizardPage_MoreToolTip);
				}
				filterOn = !filterOn;
			}
		});


		// init models
		try {
			listViewer.setInput(languageModel.getAllUndeletedLanguages());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}



	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof ILanguageReportParameter) {
			languageReportParameter = (ILanguageReportParameter) reportParameter;

			if (listViewer != null) {
				String languageID = languageReportParameter.getLanguage();
				if (languageID == null) {
					listViewer.setSelection(new StructuredSelection());
				}
				else {
					try {
						Language language = languageModel.getLanguage(languageID);
						languageFilter.addLanguage(language.getId());
						listViewer.refresh();
						listViewer.setSelection(new StructuredSelection(language), true);
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
		Language language = SelectionHelper.getUniqueSelected(listViewer);
		String languageId = language != null ? language.getId() : null;

		languageFilter.addLanguage(languageId);

		if (languageReportParameter != null) {
			languageReportParameter.setLanguage(languageId);

			String description = buildDescription(language);
			languageReportParameter.setDescription(ILanguageReportParameter.DESCRIPTION_ID, description);
		}
	}


	private String buildDescription(Language language) {
		String description = null;
		if (language != null) {
			I18NPattern i18nPattern = new I18NPattern();
			i18nPattern.append( AbstractPerson.LANGUAGE_CODE.getLabel() );
			i18nPattern.append(": ");
			i18nPattern.append( language.getName() );

			description = i18nPattern.getString();
		}
		return description;
	}

}
