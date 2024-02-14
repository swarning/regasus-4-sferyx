package de.regasus.onlineform.editor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.regasus.LabelBean;
import com.lambdalogic.messeinfo.regasus.LabelBeanHelper;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.onlineform.OnlineFormI18N;
import de.regasus.ui.Activator;
import de.regasus.onlineform.OnlineFormI18N;

import com.lambdalogic.util.rcp.CopyAction;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

public class LabelBeansTabComposite extends Composite {

	// the entity
	private RegistrationFormConfig registrationFormConfig;

	// support ModifyListener
	private ModifySupport modifySupport = new ModifySupport(this);


	// **************************************************************************
	// * Widgets
	// *

	private Table table;

	private TableViewer tableViewer;

	private String[] PROPS = { "default", "custom" };

	private LabelBeanCellModifier labelBeanCellModifier;

	private List<LabelBean> labelBeans;

	Locale locale = Locale.GERMAN;

	private Button resetButton;

	private LabelBeanTableLabelProvider labelProvider;

	private AbstractLanguageButtonGroup languageButtonGroup;

	// *
	// * Widgets
	// **************************************************************************

	public LabelBeansTabComposite(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new GridLayout(3, false));

		// ==================================================

		languageButtonGroup = new AbstractLanguageButtonGroup(this, SWT.NONE) {

			@Override
			void onSelectLanguage(String language){
				switchLocale(new Locale(language));
			}
		};
		languageButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));


		resetButton = new Button(this, SWT.PUSH);
		resetButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					resetLabels();
				}
				catch (Exception e2) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e2);
				}
			}
		});
		resetButton.setText(OnlineFormI18N.DeleteAll);

		Composite tableComposite = new Composite(this, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		tableComposite.setLayoutData(layoutData);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);

		table = new Table(tableComposite, SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn textTableColumn = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(textTableColumn, new ColumnWeightData(200));
		textTableColumn.setText(OnlineFormI18N.Label);

		TableColumn languageTableColumn = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(languageTableColumn, new ColumnWeightData(200));
		languageTableColumn.setText(OnlineFormI18N.Text);

		tableViewer = new TableViewer(table);
		labelProvider = new LabelBeanTableLabelProvider();
		labelBeanCellModifier = new LabelBeanCellModifier(tableViewer);
		labelProvider.setLanguage(locale.getLanguage());
		labelBeanCellModifier.setLanguage(locale.getLanguage());

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setLabelProvider(labelProvider);

		CellEditor[] editors = new CellEditor[2];
		editors[0] = new TextCellEditor(table);
		editors[1] = new TextCellEditor(table);

		tableViewer.setColumnProperties(PROPS);

		tableViewer.setCellModifier(labelBeanCellModifier);
		tableViewer.setCellEditors(editors);

		// observe for ModifyEvents
		labelBeanCellModifier.addModifyListener(modifySupport);
	}


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


	protected void resetLabels() {
		boolean confirm = MessageDialog.openConfirm(getShell(), UtilI18N.Confirm, OnlineFormI18N.ReallyDeleteAllLabelNames);
		if (confirm) {
			for (LabelBean labelBean : labelBeans) {
				labelBean.clear();
			}

			modifySupport.fire();
			tableViewer.refresh();
		}
	}


	protected void switchLocale(Locale locale) {
		this.locale = locale;
		labelProvider.setLanguage(locale.getLanguage());
		labelBeanCellModifier.setLanguage(locale.getLanguage());
		sort();

		tableViewer.cancelEditing();
		tableViewer.refresh();
	}


	void syncWidgetsToEntity() {
		if (registrationFormConfig != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						List<String> languages = registrationFormConfig.getLanguageCodesList();
						languageButtonGroup.setVisibleLanguages(languages);

						labelBeans = LabelBeanHelper.readListFromJSONAndResources(registrationFormConfig);
						sort();
						tableViewer.setInput(labelBeans);
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
		LabelBeanHelper.writeListAsJSON(labelBeans, registrationFormConfig);
	}


	public void setRegistrationFormConfig(RegistrationFormConfig registrationFormConfig, EventVO eventVO) {
		this.registrationFormConfig = registrationFormConfig;

		syncWidgetsToEntity();
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	protected void sort() {
		if (labelBeans != null) {
			Comparator<LabelBean> comparator = new LabelBeanComparator(locale);
			Collections.sort(labelBeans, comparator);
		}
	}
}
