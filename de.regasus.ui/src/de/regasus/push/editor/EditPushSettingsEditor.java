package de.regasus.push.editor;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventModel;
import de.regasus.event.EventTable;
import de.regasus.event.dialog.EventSelectionDialog;
import de.regasus.push.PushI18N;
import de.regasus.push.PushSetting;


public class EditPushSettingsEditor extends EditorPart implements IRefreshableEditorPart {

	public static final String ID = "EditPushSettingsEditor";

	private Collection<PushSetting> pushSettings;

	private EventModel eventModel = EventModel.getInstance();

	private Table table;
	private TableViewer tableViewer;
	private EventTable eventTable;

	private Button addButton;
	private Button removeButton;
	private Button transferButton;


	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);


		try {
			// read data here to avoid creating widgets in the case of an AuthorizationException
			refreshPushSettings();

			setPartName(input.getName());
			setTitleToolTip(input.getToolTipText());
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			throw new PartInitException(e.getMessage(), e);
		}
	}


	@Override
	public boolean isDirty() {
		return false;
	}


	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}


	@Override
	public void createPartControl(Composite parent) {
		try {
			parent.setLayout(new GridLayout());

			Composite topComposite = new Composite(parent, SWT.NONE);
			topComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			topComposite.setLayout( new FillLayout() );

			Label topTextLabel = new Label(topComposite, SWT.NONE | SWT.WRAP);
			topTextLabel.setText(PushI18N.EditPushSettingsEditor_TopText);


			createTable(parent);
			createButtonWidgets(parent);

			refreshTable();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		updateButtonEnabledStates();
	}


	private void createTable(Composite parent) {
		/*
		 * For the usage of TableColumnLayout see also http://eclipsenuggets.blogspot.com/2007_11_01_archive.html
		 */
		Composite tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableComposite.setLayout(new FillLayout());

		table = new Table(tableComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		createTableColumn(400, ParticipantLabel.Event_Label.getString());
		createTableColumn(100, ParticipantLabel.Mnemonic.getString());
		createTableColumn( 80, KernelLabel.StartTime.getString());
		createTableColumn( 80, KernelLabel.EndTime.getString());

		eventTable = new EventTable(table);
		tableViewer = eventTable.getViewer();

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonEnabledStates();
			}
		});
	}


	private void createTableColumn(int width, String text) {
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setWidth(width);
		column.setText(text);
	}


	private void createButtonWidgets(Composite parent) {
		Composite composite = createButtonComposite(parent);

		createAddButton(composite);
		createRemoveButton(composite);
		createTransferButton(composite);
	}


	private Composite createButtonComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));

		RowLayout layout = new RowLayout();
		layout.pack = false;
		layout.spacing = 5;
		layout.wrap = false;
		layout.justify = true;
		composite.setLayout(layout);

		return composite;
	}


	private void createAddButton(Composite parent) {
		addButton = new Button(parent, SWT.PUSH);
		addButton.setText(UtilI18N.Add);

		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					List<Long> hideEventPKs = getPushEventPKs();
					EventSelectionDialog dialog = new EventSelectionDialog(
						getShell(),
						hideEventPKs,
						null,	// initSelectedEventPKs
						false	// multiSelection
					);


					int result = dialog.open();
					if (result == 0) {
						List<EventVO> selectedEvents = dialog.getSelectedEvents();
						if ( notEmpty(selectedEvents) ) {
							Long eventPK = selectedEvents.get(0).getID();

							PushSetting pushSetting = new PushSetting(eventPK);
							getPushSettingMgr().create(pushSetting);
							refresh();
						}
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, this.getClass().getName(), e);
				}
			}
		});
	}


	private void createRemoveButton(Composite parent) {
		removeButton = new Button(parent, SWT.PUSH);
		removeButton.setText(UtilI18N.Remove);

		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				List<EventVO> selectedEventVOs =  SelectionHelper.toList( tableViewer.getSelection() );
				String eventCount = String.valueOf(selectedEventVOs.size());

				boolean deleteOK = MessageDialog.openQuestion(
					getShell(),
					UtilI18N.Question,
					PushI18N.EditPushSettingsEditor_RemovePushSettingsQuestion.replace("<n>", eventCount)
				);

				if (deleteOK) {
    				try {
    					for (EventVO eventVO : selectedEventVOs) {
    						PushSetting pushSetting = new PushSetting( eventVO.getID() );
    						getPushSettingMgr().delete(pushSetting);
    					}
    				}
    				catch (Exception e) {
    					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, this.getClass().getName(), e);
    				}

    				try {
    					refresh();
    				}
    				catch (Exception e) {
    					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, this.getClass().getName(), e);
    				}
    			}
			}
    	});
	}


	private void createTransferButton(Composite parent) {
		transferButton = new Button(parent, SWT.PUSH);
		transferButton.setText(PushI18N.EditPushSettingsEditor_TransferButtonText);

		transferButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				List<EventVO> selectedEventVOs =  SelectionHelper.toList( tableViewer.getSelection() );
				String eventCount = String.valueOf(selectedEventVOs.size());

				boolean transferOK = MessageDialog.openQuestion(
					getShell(),
					UtilI18N.Question,
					PushI18N.EditPushSettingsEditor_TransferDataQuestion.replace("<n>", eventCount)
				);

				if (transferOK) {
					try {
						for (EventVO eventVO : selectedEventVOs) {
							getPushJobMgr().createPushJobsForEvent( eventVO.getID() );
						}
					}
					catch (RuntimeException e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, this.getClass().getName(), e);
					}
				}
			}
		});
	}


	@Override
	public void refresh() throws ErrorMessageException {
		refreshPushSettings();
		refreshTable();
		updateButtonEnabledStates();
	}


	private void refreshPushSettings() throws ErrorMessageException {
		pushSettings = getPushSettingMgr().getPushSettings();
	}


	private void refreshTable() {
		try {
			List<Long> eventPKs = getPushEventPKs();

			List<EventVO> eventVOs = eventModel.getEventVOs(eventPKs);

			tableViewer.setInput(eventVOs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private List<Long> getPushEventPKs() {
		List<Long> eventIds = new ArrayList<>( pushSettings.size() );
		for (PushSetting pushSetting : pushSettings) {
			eventIds.add( pushSetting.getEventId() );
		}

		return eventIds;
	}


	@Override
	public boolean isNew() {
		return false;
	}


	@Override
	public void setFocus() {
		table.setFocus();
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
	}


	@Override
	public void doSaveAs() {
	}


	private void updateButtonEnabledStates() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				int selectionCount = table.getSelectionCount();
				boolean oneOrMoreSelected = (selectionCount > 0);

				addButton.setEnabled(true);
				removeButton.setEnabled(oneOrMoreSelected);
				transferButton.setEnabled(oneOrMoreSelected);
			}
		});
	}


	/**
	 * Closes this editor asynchronous.
	 */
	private void close() {
		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				getSite().getPage().closeEditor(EditPushSettingsEditor.this, false /* save */);
			}
		});
	}


	private Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

}
