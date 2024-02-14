package de.regasus.onlineform.editor;

import java.util.List;
import java.util.Locale;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.regasus.FormTexts;
import com.lambdalogic.messeinfo.regasus.FormTextsEnum;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.CopyAction;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.html.LazyHtmlEditor;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.onlineform.OnlineFormI18N;
import de.regasus.onlineform.provider.FormTextsTableLabelProvider;
import de.regasus.ui.Activator;

public class TextsTabComposite extends Composite implements ISelectionChangedListener {

	// the entity
	private RegistrationFormConfig registrationFormConfig;

	private ModifySupport modifySupport = new ModifySupport(this);

	private FormTextsEnum currentTextEnum;

	// **************************************************************************
	// * Widgets
	// *

	private Table table;

	private Text textLabel;

	private LazyHtmlEditor htmlEditor;

	private TableViewer tableViewer;


	private Locale locale = Locale.GERMAN;

	private FormTexts formTexts;

	private FormTextsTableLabelProvider formTextsTableLabelProvider = new FormTextsTableLabelProvider();

	private FormTextsViewerFilter formTextsViewerFilter = new FormTextsViewerFilter();

	private AbstractLanguageButtonGroup languageButtonGroup;

	// *
	// * Widgets
	// **************************************************************************


	public TextsTabComposite(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new GridLayout(1, false));

		languageButtonGroup = new AbstractLanguageButtonGroup(this, SWT.NONE) {

			@Override
			void onSelectLanguage(String language){
				switchLocale(new Locale(language));
			}
		};
		languageButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));


		// ==================================================

		SashForm sashForm = new SashForm(this, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite tableComposite = new Composite(sashForm, SWT.NONE);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);

		table = new Table(tableComposite, SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setToolTipText(OnlineFormI18N.ToolTipTexts);

		TableColumn textTableColumn = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(textTableColumn, new ColumnWeightData(100));
		textTableColumn.setText(OnlineFormI18N.Text);

		TableColumn contentTableColumn = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(contentTableColumn, new ColumnWeightData(200));
		contentTableColumn.setText(OnlineFormI18N.Content);

		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setLabelProvider(formTextsTableLabelProvider);
		tableViewer.addFilter(formTextsViewerFilter);
		tableViewer.setInput(FormTextsEnum.values());
		tableViewer.addPostSelectionChangedListener(this);


		Composite lowerComposte = new Composite(sashForm, SWT.NONE);
		lowerComposte.setLayout(new GridLayout(1, false));

		textLabel = new Text(lowerComposte, SWT.READ_ONLY);
		textLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		textLabel.setText(OnlineFormI18N.HeaderFirstPage);

		htmlEditor = new LazyHtmlEditor(lowerComposte, SWT.NONE);
		htmlEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		htmlEditor.setEnabled(false);
		htmlEditor.addModifyListener(modifySupport);

		int[] weights = {1, 2};
		sashForm.setWeights(weights);
	}


	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		htmlEditor.setVisible(visible);
	}


	/**
	 * Makes that Ctrl+C starts the Action to copy the content of the first cell table contents to the clipboard.
	 */
	public void setCopyAction(final CopyAction copyAction) {
		copyAction.addRunnable(table, new Runnable() {
			@Override
			public void run() {
				int selectingIndex = table.getSelectionIndex();

				// get text of column 1
				String text = (selectingIndex >= 0) ? table.getItem(selectingIndex).getText(1) : "";

				// copy text to clipboard
				copyAction.copyToClipboard(text);
			}
		});
	}


	void syncWidgetsToEntity() {
		if (registrationFormConfig != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						List<String> languages = registrationFormConfig.getLanguageCodesList();
						languageButtonGroup.setVisibleLanguages(languages);

						tableViewer.refresh();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}

				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (currentTextEnum != null) {
			String html = htmlEditor.getHtml();
			String language = locale.getLanguage();
			formTexts.set(currentTextEnum, language, html);
		}
		formTexts.writeTo(registrationFormConfig);

		// Refresh the table to let it show the changed text
		tableViewer.refresh();
	}


	public void setRegistrationFormConfig(RegistrationFormConfig registrationFormConfig) {
		this.registrationFormConfig = registrationFormConfig;
		this.formTexts = new FormTexts(registrationFormConfig);
		formTextsTableLabelProvider.setFormTexts(this.formTexts);
		formTextsViewerFilter.setConfig(registrationFormConfig);

		syncWidgetsToEntity();
	}


	protected void switchLocale(Locale locale) {
		if (! locale.equals(this.locale)) {

			// Store
			storeCurrentHtmlIntoFormTexts();

			// Switch
			this.locale = locale;

			// Update
			updateTableAndHtmlFromCurrentFormText();
		}
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {

		if (! event.getSelection().isEmpty()) {
			htmlEditor.setEnabled(true);
		}

		// Store
		storeCurrentHtmlIntoFormTexts();

		// Switch - Change the currentTextEnum to match the selected row
		ISelection selection = tableViewer.getSelection();
		currentTextEnum = SelectionHelper.getUniqueSelected(selection);

		// Update
		updateTableAndHtmlFromCurrentFormText();
	}


	private void updateTableAndHtmlFromCurrentFormText() {
		// Udate table
		String language = locale.getLanguage();
		formTextsTableLabelProvider.setLanguage(language);
		tableViewer.refresh();

		if (currentTextEnum != null) {

			String firstColumnText = formTextsTableLabelProvider.getColumnText(currentTextEnum, 0);

			if (currentTextEnum == FormTextsEnum.EMAIL_RECOMMENDATION_TEXT) {
				firstColumnText += " - " + OnlineFormI18N.UseTheseVariablesForEmailRecommendation;
			}

			textLabel.setText(firstColumnText);

			// Update HTML editor

			// Fetch the Html from the map with the new currentTextEnum key and show in editor
			String storedHtml = formTexts.get(currentTextEnum, language);
			htmlEditor.setHtml(StringHelper.avoidNull(storedHtml));
		}

		else {
			textLabel.setText("");
			htmlEditor.setHtml("");
		}

	}


	private void storeCurrentHtmlIntoFormTexts() {
		if (currentTextEnum != null) {
			// Store the contents of the Html editor in the map under the currentTextEnum key
			String html = htmlEditor.getHtml();

			html = html.replace("\\", "&#92;");

			formTexts.set(currentTextEnum, locale.getLanguage(), html);

			// Refresh the table to let it show the changed text
			tableViewer.refresh();

		}
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	public Locale getLocale() {
		return locale;
	}

}
