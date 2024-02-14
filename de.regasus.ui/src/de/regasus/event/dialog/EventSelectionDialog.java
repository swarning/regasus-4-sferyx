package de.regasus.event.dialog;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.participant.data.EventVO;

import de.regasus.I18N;
import de.regasus.event.EventTableComposite;

public class EventSelectionDialog extends TitleAreaDialog {

	private String title = I18N.EventSelectionDialog_Title;
	private String message = I18N.EventSelectionDialog_Message;

	private boolean multiSelection;

	private Collection<Long> hideEventPKs = null;
	private Collection<Long> initSelectedEventPKs = null;

	// widgets
	private EventTableComposite eventTableComposite;


	public EventSelectionDialog(
		Shell parentShell,
		Collection<Long> hideEventPKs,
		Collection<Long> initSelectedEventPKs,
		boolean multiSelection
	) {
		super(parentShell);

		this.hideEventPKs = hideEventPKs;
		this.initSelectedEventPKs = initSelectedEventPKs;
		this.multiSelection = multiSelection;
	}


	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		super.setTitle(title);
		super.setMessage(message);

		Composite area = (Composite) super.createDialogArea(parent);
		eventTableComposite = new EventTableComposite(
			area,
			hideEventPKs,
			initSelectedEventPKs,
			multiSelection,
			SWT.NONE
		);
		eventTableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		eventTableComposite.addModifyListener(tableListener);

		return area;
	}


	private ModifyListener tableListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			refreshOkButtonState();
		}
	};


	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}


	@Override
	protected boolean isResizable() {
		return true;
	}


	public List<EventVO> getSelectedEvents() {
		return eventTableComposite.getSelectedEvents();
	}


	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(700, 500);
	}


	@Override
	public void setTitle(String title) {
		this.title = title;
	}


	@Override
	public void setMessage(String message) {
		this.message = message;
	}


	private void refreshOkButtonState() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			List<EventVO> selectedEvents = getSelectedEvents();
			okButton.setEnabled( notEmpty(selectedEvents) );
		}
	}

}
