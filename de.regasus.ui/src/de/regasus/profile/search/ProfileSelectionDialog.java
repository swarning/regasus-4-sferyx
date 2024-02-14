// REFERENCE
package de.regasus.profile.search;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.rcp.ISelectionDialogConfig;

import de.regasus.I18N;


/**
 * Dialog to select one or more Profiles.
 */
public class ProfileSelectionDialog extends TitleAreaDialog {

	private String title;
	private String message;

	private ISelectionDialogConfig config;

	/**
	 * Stores parameters to be used when this search composite is supposed to show initial
	 * search criteria (like for group membership).
	 */
	private List<SQLParameter> initialSQLParameters;

	/**
	 * If present, is used for initial search with this last name
	 */
	private String initialLastName;

	/**
	 * If present, is used for initial search with this first name
	 */
	private String initialFirstName;


	private ProfileSearchComposite profileSearchComposite;


	public ProfileSelectionDialog(Shell parentShell, ISelectionDialogConfig config) {
		super(parentShell);

		Objects.requireNonNull(config);

		this.config = config;

		/* If nothing can be selected, entering search parameters is optional.
		 * Otherwise, the "Next" button shall only be enabled if the user has something selected.
		 */
		refreshOkButtonState();
	}



	@Override
	public void setTitle(String newTitle) {
		this.title = newTitle;
		if (getShell() != null) {
			super.setTitle(newTitle);
		}
	}


	@Override
	public void setMessage(String newMessage) {
		this.message = newMessage;
		if (getShell() != null) {
			super.setMessage(newMessage);
		}
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		if (title == null) {
			title = I18N.ParticipantSelectionDialog_Title;
		}
		super.setTitle(title);
		getShell().setText(title);

		if (message == null) {
			message = config.getMessage();
		}
		setMessage(message);


		Composite dialogArea = (Composite) super.createDialogArea(parent);

		profileSearchComposite = new ProfileSearchComposite(
			dialogArea,
			config.getSelectionMode(),
			SWT.NONE,
			true // useDetachedSearchModelInstance
		);
		profileSearchComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		profileSearchComposite.setInitialFirstName(initialFirstName);
		profileSearchComposite.setInitialLastName(initialLastName);
		profileSearchComposite.setInitialSQLParameters(initialSQLParameters);

		TableViewer tableViewer = profileSearchComposite.getTableViewer();
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				refreshOkButtonState();
			}
		});

		refreshOkButtonState();

		return dialogArea;
	}


	private void refreshOkButtonState() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			List<?> selectedProfiles = getSelectedProfiles();
			boolean enabled = config.canFinish(selectedProfiles);
			okButton.setEnabled(enabled);
		}
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		refreshOkButtonState();
	}


	public boolean isCancelled() {
		return getReturnCode() == CANCEL;
	}


	public List<Long> getSelectedPKs() {
		List<Long> result = Collections.emptyList();
		if (!isCancelled()) {
			result = profileSearchComposite.getSelectedPKs();
		}
		return result;
	}


	public List<Profile> getSelectedProfiles() {
		List<Profile> result = Collections.emptyList();
		if (!isCancelled()) {
			result = profileSearchComposite.getSelectedProfiles();
		}
		return result;
	}


	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}


	@Override
	protected boolean isResizable() {
		return true;
	}


	public void doSearch() {
		if (profileSearchComposite != null) {
			profileSearchComposite.doSearch();
		}
	}


	public void setInitialSQLParameters(List<SQLParameter> initialSQLParameters) {
		this.initialSQLParameters = initialSQLParameters;
		if (profileSearchComposite != null) {
			profileSearchComposite.setInitialSQLParameters(initialSQLParameters);
		}
	}


	public void setInitialLastName(String initialLastName) {
		this.initialLastName = initialLastName;
		if (profileSearchComposite != null) {
			profileSearchComposite.setInitialLastName(initialLastName);
		}
	}


	public void setInitialFirstName(String initialFirstName) {
		this.initialFirstName = initialFirstName;
		if (profileSearchComposite != null) {
			profileSearchComposite.setInitialFirstName(initialFirstName);
		}
	}

}
