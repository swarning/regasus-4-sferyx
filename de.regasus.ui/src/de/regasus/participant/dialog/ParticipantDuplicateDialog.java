/**
 *
 */
package de.regasus.participant.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
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
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantMessage;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.editor.ParticipantEditorInput;
import de.regasus.participant.search.ParticipantSearchTable;
import de.regasus.ui.Activator;

/**
 *
 * @author angel
 */
public class ParticipantDuplicateDialog extends Dialog {

	public final static int DEFAULT_WIDTH = 650;
	public final static int DEFAULT_HEIGTH = 400;

	/** ID for button, that shows selected duplicates */
	private final static int SHOW = 2;

	private ParticipantModel participantModel;

	private Participant participant;
	private Collection<ParticipantSearchData> duplicates;

	// Widgets
	private String title;

	private TableViewer tableViewer;

	/** Holds the selected participants in table. */
	private List<ParticipantSearchData> selections;

	private Button showButton;


	/**
	 * @param parentShell
	 */
	public ParticipantDuplicateDialog(
		Shell parentShell,
		String title,
		Participant participant,
		Collection<ParticipantSearchData> duplicates
	) {
		super(parentShell);

		this.title = title;
		this.participant = participant;
		this.duplicates = duplicates;

		participantModel = ParticipantModel.getInstance();
		selections = new ArrayList<ParticipantSearchData>();
	}


	/**
	 * @return The selected participant duplicate(s) to show in editor(s).
	 */
	public List<ParticipantSearchData> getSelectedDuplicates() {
		if (selections == null) {
			selections = new ArrayList<ParticipantSearchData>();
		}
		return selections;
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
			label.setText(ParticipantMessage.DuplicateDialog_DuplicateLabel.getString());

			Composite searchTableComposite = new Composite(container, SWT.BORDER);
			searchTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			searchTableComposite.setLayout(new FillLayout());

			Table table = new Table(searchTableComposite, SelectionMode.MULTI_SELECTION.getSwtStyle());
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			final TableColumn numberTableColumn = new TableColumn(table, SWT.RIGHT);
			numberTableColumn.setWidth(60);
			numberTableColumn.setText(UtilI18N.NumberAbreviation);

			final TableColumn firstNameTableColumn = new TableColumn(table, SWT.NONE);
			firstNameTableColumn.setWidth(100);
			firstNameTableColumn.setText( Participant.FIRST_NAME.getString() );

			final TableColumn lastNameTableColumn = new TableColumn(table, SWT.NONE);
			lastNameTableColumn.setWidth(120);
			lastNameTableColumn.setText( Participant.LAST_NAME.getString() );

			final TableColumn cityTableColumn = new TableColumn(table, SWT.NONE);
			cityTableColumn.setWidth(100);
			cityTableColumn.setText( Address.CITY.getString() );

			final TableColumn participantTypeTableColumn = new TableColumn(table, SWT.NONE);
			participantTypeTableColumn.setWidth(140);
			participantTypeTableColumn.setText( Participant.PARTICIPANT_TYPE.getString() );

			final TableColumn participantStateTableColumn = new TableColumn(table, SWT.NONE);
			participantStateTableColumn.setWidth(140);
			participantStateTableColumn.setText( Participant.PARTICIPANT_STATE.getString() );

			final TableColumn organisationTableColumn = new TableColumn(table, SWT.NONE);
			organisationTableColumn.setWidth(100);
			organisationTableColumn.setText( Address.ORGANISATION.getString() );

			final TableColumn customerNoTableColumn = new TableColumn(table, SWT.NONE);
			customerNoTableColumn.setWidth(80);
			customerNoTableColumn.setText(Person.CUSTOMER_NO.getString());

			final TableColumn eventTableColumn = new TableColumn(table, SWT.NONE);
			eventTableColumn.setWidth(100);
			eventTableColumn.setText( Participant.EVENT.getString() );


			ParticipantSearchTable participantSearchTable = new ParticipantSearchTable(table);
			tableViewer = participantSearchTable.getViewer();
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
			participant = participantModel.create(participant, true);
		}
		catch (Exception exp) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), exp);
		}
	}


	private void showDuplicates() {
		if (selections != null && !selections.isEmpty()) {
			for (ParticipantSearchData participantSearchData : selections) {
				Long participantID = participantSearchData.getPK();
				ParticipantEditorInput editorInput = ParticipantEditorInput.getEditInstance(participantID);
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
						editorInput,
						ParticipantEditor.ID
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


	public Participant getParticipant() {
		return participant;
	}

}
