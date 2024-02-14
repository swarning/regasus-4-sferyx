package de.regasus.profile.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.Person;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileModel;
import de.regasus.profile.editor.ProfileEditor;
import de.regasus.profile.editor.ProfileEditorInput;
import de.regasus.profile.search.ProfileSearchTable;
import de.regasus.ui.Activator;

public class ProfileDuplicateDialog extends Dialog {

	private final static int DEFAULT_WIDTH = 650;
	private final static int DEFAULT_HEIGTH = 400;


	/** ID for button, that shows selected duplicates */
	private final static int SHOW = 2;

	private ProfileModel profileModel;

	private Profile profile;
	private Collection<Profile> duplicates;

	// Widgets
	private String title;

	private TableViewer tableViewer;

	private List<Profile> selections;

	private Button showButton;


	/**
	 * @param parentShell
	 */
	public ProfileDuplicateDialog(
		Shell parentShell,
		String title,
		Profile profile,
		Collection<Profile> duplicates
	) {
		super(parentShell);

		this.title = title;
		this.profile = profile;
		this.duplicates = duplicates;

		profileModel = ProfileModel.getInstance();
		selections = new ArrayList<Profile>();
	}


	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}


	@Override
	protected Control createDialogArea(Composite parent){
		try {
			Composite container = (Composite) super.createDialogArea(parent);
			container.setLayout(new GridLayout());

			Label label = new Label(container, SWT.NONE);
			label.setText(I18N.DuplicateDialog_DuplicateLabel);

			Composite searchTableComposite = new Composite(container, SWT.BORDER);
			searchTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			TableColumnLayout layout = new TableColumnLayout();
			searchTableComposite.setLayout(layout);

			Table table = new Table(searchTableComposite, SelectionMode.MULTI_SELECTION.getSwtStyle());
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			TableColumn firstNameTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(firstNameTableColumn, new ColumnWeightData(140));
			firstNameTableColumn.setText( Person.FIRST_NAME.getString() );

			TableColumn lastNameTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(lastNameTableColumn, new ColumnWeightData(140));
			lastNameTableColumn.setText( Person.LAST_NAME.getString() );

			TableColumn organisationTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(organisationTableColumn, new ColumnWeightData(100));
			organisationTableColumn.setText( Address.ORGANISATION.getString() );

			TableColumn cityTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(cityTableColumn, new ColumnWeightData(100));
			cityTableColumn.setText( Address.CITY.getString() );

			ProfileSearchTable profileSearchTable = new ProfileSearchTable(table);
			tableViewer = profileSearchTable.getViewer();
			tableViewer.setInput(duplicates);
			tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					setSelectDuplicates();
					boolean enabled = selections != null && !selections.isEmpty();
					showButton.setEnabled(enabled);
				}
			});

			return container;
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			return null;
		}
	}


	@Override
	protected boolean isResizable() {
		return true;
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setLayout(new GridLayout(3, false));
		showButton = createButton(parent, SHOW, UtilI18N.Show, false);
		super.createButtonsForButtonBar(parent);
	}


	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		Button button = super.createButton(parent, id, label, defaultButton);
		GridData gridData = null;
		switch (id) {
			case OK:
				button.setText(UtilI18N.SaveAnyway);
				gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						createAnyway();
					}
				});
				break;
			case CANCEL:
				button.setText(UtilI18N.Cancel);
				gridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
				break;
			case SHOW:
				button.setEnabled(false);
				gridData = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						showDuplicates();
					}
				});
				break;
			default:
				break;
		}

		button.setLayoutData(gridData);
		return button;
	}


	private void createAnyway() {
		try {
			profile = profileModel.create(profile, true);
		}
		catch (Exception exp) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), exp);
		}
	}


	private void showDuplicates() {
		if (selections != null && !selections.isEmpty()) {
			for (Profile profile : selections) {
				ProfileEditorInput editorInput = new ProfileEditorInput(profile.getID());
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
						editorInput,
						ProfileEditor.ID
					);
				}
				catch (PartInitException pie) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), pie);
				}
			}

			close();
		}
	}


	@SuppressWarnings("unchecked")
	private void setSelectDuplicates() {
		StructuredSelection structuredSelection = (StructuredSelection) tableViewer.getSelection();
		if (structuredSelection != null) {
			selections = structuredSelection.toList();
		}
	}


	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(DEFAULT_WIDTH, DEFAULT_HEIGTH);
	}


	public Profile getProfile() {
		return profile;
	}

}
