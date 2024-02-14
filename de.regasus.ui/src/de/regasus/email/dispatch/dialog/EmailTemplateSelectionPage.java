package de.regasus.email.dispatch.dialog;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateComparator;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailI18N;
import de.regasus.email.EmailTemplateModel;
import de.regasus.email.template.EmailTemplateSearchTable;
import de.regasus.ui.Activator;

/**
 * A WizardPage to select one EmailTemplate from a list of {@link EmailTemplateSearchData} which is
 * obtained via an eventPk from the {@link EmailTemplateModel}.
 */
public class EmailTemplateSelectionPage extends WizardPage {

	public static final String NAME = "EmailTemplateSelectionPage";

	// *************************************************************************
	// * Attributes
	// *


	private EmailTemplate selectedSpecificEmailTemplate;
	private EmailTemplate selectedGenericEmailTemplate;

	/**
	 * If present, the {@link EmailTemplate}s from this Event are shown; might be
	 * <code>null</null> when this wizard page was used for a profile and not for a participant.
	 */
	private Long eventPK;

	private List<EmailTemplate> emailTemplateSearchDataList;

	private EmailTemplate selectedEmailTemplateSearchData;


	// *************************************************************************
	// * Widgets
	// *

	private EmailTemplateSearchTable emailTemplateTable;

	private Button eventSpecificTemplatesRadioButton;

	private Button genericTemplatesRadioButton;

	private EmailTemplate selectedTemplate;


	// *************************************************************************
	// * Constructor
	// *

	protected EmailTemplateSelectionPage(Long eventPK) {
		super(NAME);
		setTitle(EmailI18N.TemplateSelection);

		this.eventPK = eventPK;
	}


	// *************************************************************************
	// * Overridden/implemented WizardPage-Methods
	// *

	@Override
	public void createControl(Composite parent) {
		Composite pageComposite = new Composite(parent, SWT.NONE);

		pageComposite.setLayout(new GridLayout(2, false));

		if (eventPK != null) {
			eventSpecificTemplatesRadioButton = new Button(pageComposite, SWT.RADIO);
			eventSpecificTemplatesRadioButton.setText(EmailI18N.EventSpecificTemplates);
			eventSpecificTemplatesRadioButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			eventSpecificTemplatesRadioButton.setSelection(true);
			eventSpecificTemplatesRadioButton.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (eventSpecificTemplatesRadioButton.getSelection())
						switchTemplateList();
				}
			});

			genericTemplatesRadioButton = new Button(pageComposite, SWT.RADIO);
			genericTemplatesRadioButton.setText(EmailI18N.GenericTemplates);
			genericTemplatesRadioButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			genericTemplatesRadioButton.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (genericTemplatesRadioButton.getSelection())
						switchTemplateList();
				}
			});
		}


		Composite tableComposite = new Composite(pageComposite, SWT.BORDER);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		TableColumnLayout layout = new TableColumnLayout();
		tableComposite.setLayout(layout);

		final Table table = new Table(tableComposite, SelectionMode.SINGLE_SELECTION.getSwtStyle());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(nameTableColumn, new ColumnWeightData(100));
		nameTableColumn.setText(EmailLabel.EmailTemplate.getString());

		final TableColumn languageTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(languageTableColumn, new ColumnWeightData(25));
		languageTableColumn.setText( Person.LANGUAGE_CODE.getLabel() );

		final TableColumn webidTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(webidTableColumn, new ColumnWeightData(25));
		webidTableColumn.setText(EmailI18N.WebId);

		emailTemplateTable = new EmailTemplateSearchTable(table);

		try {
			emailTemplateSearchDataList = EmailTemplateModel.getInstance().getEmailTemplateSearchDataByEvent(eventPK);

			// sort by language and name (sorting by 2 columns is not supported by SimpleTable)
			emailTemplateSearchDataList = CollectionsHelper.createArrayList(emailTemplateSearchDataList);
			Collections.sort(emailTemplateSearchDataList, EmailTemplateComparator.getInstance());
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		emailTemplateTable.setInput(emailTemplateSearchDataList);

		emailTemplateTable.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {

				ISelection selection = emailTemplateTable.getViewer().getSelection();
				if (selection.isEmpty() ) {
					selectedTemplate = null;
				}
				else {
					selectedTemplate = (EmailTemplate) SelectionHelper.getUniqueSelected(selection);
				}

				getContainer().updateButtons();
			}

		});

		setControl(pageComposite);
	}


	protected void switchTemplateList() {
		Long showEventPK;
		if (eventSpecificTemplatesRadioButton.getSelection()) {

			showEventPK = eventPK;
			selectedGenericEmailTemplate = selectedTemplate;
		}
		else {
			showEventPK = null;
			selectedSpecificEmailTemplate = selectedTemplate;
		}

		try {
			emailTemplateSearchDataList = EmailTemplateModel.getInstance().getEmailTemplateSearchDataByEvent(showEventPK);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		emailTemplateTable.setInput(emailTemplateSearchDataList);


		if (showEventPK == null) {
			if (selectedGenericEmailTemplate != null) {
				emailTemplateTable.getViewer().setSelection(new StructuredSelection(selectedGenericEmailTemplate));
			}
		}
		else {
			if (selectedSpecificEmailTemplate != null) {
				emailTemplateTable.getViewer().setSelection(new StructuredSelection(selectedSpecificEmailTemplate));
			}
		}
		emailTemplateTable.getViewer().refresh();
	}


	@Override
	public boolean isPageComplete() {
		ISelection selection = emailTemplateTable.getViewer().getSelection();
		if (selection.isEmpty()) {
			return false;
		} else {
			selectedEmailTemplateSearchData = (EmailTemplate) SelectionHelper.getUniqueSelected(selection);
			return true;
		}
	}


	// *************************************************************************
	// * Generated getters and setters
	// *

	public EmailTemplate getSelectedEmailTemplateSearchData() {
		return selectedEmailTemplateSearchData;
	}



}
