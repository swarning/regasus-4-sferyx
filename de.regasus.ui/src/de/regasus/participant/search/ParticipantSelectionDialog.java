// REFERENCE
package de.regasus.participant.search;

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
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.util.rcp.ISelectionDialogConfig;

import de.regasus.I18N;
import de.regasus.core.ui.search.SearchInterceptor;


/**
 * Dialog to select one or more Participants.
 */
public class ParticipantSelectionDialog extends TitleAreaDialog {

	private String title;
	private String message;

	private ISelectionDialogConfig config;

	private SearchInterceptor searchInterceptor;

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

	private Long eventPK;


	private ParticipantSearchComposite participantSearchComposite;


	public ParticipantSelectionDialog(Shell parentShell, ISelectionDialogConfig config, Long eventPK) {
		super(parentShell);

		Objects.requireNonNull(config);

		this.config = config;
		this.eventPK = eventPK;

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

		participantSearchComposite = new ParticipantSearchComposite(
			dialogArea,
			config.getSelectionMode(),
			SWT.NONE,
			true, // useDetachedSearchModelInstance
			eventPK
		);
		participantSearchComposite.setLayoutData( new GridData(GridData.FILL_BOTH) );

		participantSearchComposite.setSearchInterceptor(searchInterceptor);
		participantSearchComposite.setInitialFirstName(initialFirstName);
		participantSearchComposite.setInitialLastName(initialLastName);
		participantSearchComposite.setInitialSQLParameters(initialSQLParameters);

		TableViewer tableViewer = participantSearchComposite.getTableViewer();
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
			List<?> selectedParticipants = getSelectedParticipants();
			boolean enabled = config.canFinish(selectedParticipants);
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
			result = participantSearchComposite.getSelectedPKs();
		}
		return result;
	}


	public List<ParticipantSearchData> getSelectedParticipants() {
		List<ParticipantSearchData> result = Collections.emptyList();
		if (!isCancelled()) {
			result = participantSearchComposite.getSelectedParticipants();
		}
		return result;
	}


	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(1024, 768);
	}


	@Override
	protected boolean isResizable() {
		return true;
	}


	public void doSearch() {
		if (participantSearchComposite != null) {
			participantSearchComposite.doSearch();
		}
	}


	public void setSearchInterceptor(SearchInterceptor searchInterceptor) {
		this.searchInterceptor = searchInterceptor;
		if (participantSearchComposite != null) {
			participantSearchComposite.setSearchInterceptor(searchInterceptor);
		}
	}


	public void setInitialSQLParameters(List<SQLParameter> initialSQLParameters) {
		this.initialSQLParameters = initialSQLParameters;
		if (participantSearchComposite != null) {
			participantSearchComposite.setInitialSQLParameters(initialSQLParameters);
		}
	}


	public void setInitialLastName(String initialLastName) {
		this.initialLastName = initialLastName;
		if (participantSearchComposite != null) {
			participantSearchComposite.setInitialLastName(initialLastName);
		}
	}


	public void setInitialFirstName(String initialFirstName) {
		this.initialFirstName = initialFirstName;
		if (participantSearchComposite != null) {
			participantSearchComposite.setInitialFirstName(initialFirstName);
		}
	}

}
