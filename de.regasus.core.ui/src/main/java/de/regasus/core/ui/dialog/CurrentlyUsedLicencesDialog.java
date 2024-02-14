package de.regasus.core.ui.dialog;

import static de.regasus.LookupService.*;

import java.util.Collection;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.kernel.Session;
import com.lambdalogic.util.Licence;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.widget.LicenceGroup;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.action.LoginAction;

public class CurrentlyUsedLicencesDialog extends TitleAreaDialog {

	private LicenceGroup licenceGroup;
	private Button terminateSessionButton;
	private TableViewer tableViewer;


	/**
	 * Create the dialog
	 */
	public CurrentlyUsedLicencesDialog(Shell parentShell) {
		super(parentShell);

		setShellStyle(getShellStyle() | SWT.RESIZE );
	}


	/**
	 * Create contents of the dialog
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		setTitle(CoreI18N.CurrentlyUsedLicences_Title);

		Composite area = (Composite) super.createDialogArea(parent);


		// This sash form parts the dialog in a top and bottom pane
		SashForm sashForm = new SashForm(area, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));



		// **************************************************************************
		// * The Group that shows the current licence details
		// *

		licenceGroup = new LicenceGroup(sashForm, SWT.READ_ONLY);
		licenceGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// **************************************************************************
		// * The Group that shows the current sessions
		// *

		Group sessionsGroup = new Group(sashForm, SWT.NONE);
		sessionsGroup.setText(CoreI18N.CurrentSessions);
		sessionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sessionsGroup.setLayout(new GridLayout());

		Composite tableComposite = new Composite(sessionsGroup, SWT.BORDER);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableColumnLayout layout = new TableColumnLayout();
		tableComposite.setLayout(layout);
		Table table = new Table(tableComposite, SWT.SINGLE | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);



		final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(nameTableColumn, new ColumnWeightData(100));

		nameTableColumn.setText(UtilI18N.Name);

		final TableColumn hostTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(hostTableColumn, new ColumnWeightData(100));
		hostTableColumn.setText(UtilI18N.Host);

		final SessionTable sessionTable = new SessionTable(table);
		tableViewer = sessionTable.getViewer();



		loadData();

		terminateSessionButton = new Button(sessionsGroup, SWT.PUSH);
		terminateSessionButton.setText(CoreI18N.TerminateSession_Name);
		terminateSessionButton.setToolTipText(CoreI18N.TerminateSession_ToolTip);
		terminateSessionButton.setEnabled(false);
		terminateSessionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteSelectedSession();
			}
		});

		// Only when sessions are selected, you can terminate them
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				terminateSessionButton.setEnabled(! tableViewer.getSelection().isEmpty());
			}
		});
		sashForm.setWeights(new int[]{3,2});
		return area;
	}



	protected void loadData() {
		boolean tmpLogin = false;
		try {
			if (!ServerModel.getInstance().isLoggedIn()) {
				tmpLogin = true;
				ServerModel.getInstance().setSilentMode(true);
				LoginAction.login();
			}

			Licence licence = getKernelMgr().getCurrentLicence();
			licenceGroup.setLicence(licence);

			Collection<Session> sessions = getSessionMgr().findLiveSessions();
			tableViewer.setInput(sessions);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			if (tmpLogin) {
				ServerModel.getInstance().logout();
				ServerModel.getInstance().setSilentMode(false);
			}
		}
	}


	protected void deleteSelectedSession() {
		boolean tmpLogin = false;
		try {
			if (!ServerModel.getInstance().isLoggedIn()) {
				tmpLogin = true;
				ServerModel.getInstance().setSilentMode(true);
				LoginAction.login();
			}


			boolean confirmed = MessageDialog.openConfirm(getShell(), UtilI18N.Confirm, CoreI18N.TerminateSession_ToolTip);
			if (confirmed) {
				// Tell the server to delete the session
				Session session = SelectionHelper.getUniqueSelected(tableViewer.getSelection());

				getSessionMgr().deleteSession(session.getId());

				// Refresh session list
				Collection<Session> sessions = getSessionMgr().findLiveSessions();
				tableViewer.setInput(sessions);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			if (tmpLogin) {
				ServerModel.getInstance().logout();
				ServerModel.getInstance().setSilentMode(false);
			}
		}
	}


	/**
	 * Create contents of the button bar
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
}
