package de.regasus.event.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.ui.Activator;


public class CloseEventDialog extends TitleAreaDialog {

	private boolean deleteAcl;
	private boolean deleteHistory;
	private boolean deleteLeads;
	private boolean deleteCreditCard;
	private boolean deletePortalPhotos;

	private Button aclCheckbox;
	private Button historyCheckBox;
	private Button leadsCheckBox;
	private Button creditCardCheckBox;
	private Button portalPhotosCheckBox;


	public CloseEventDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle()  | SWT.RESIZE );
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(UtilI18N.Question);
		setMessage(I18N.CloseEventDescriptionLabel);

		Composite area = (Composite) super.createDialogArea(parent);

		try {
			Composite innerArea = new Composite(area, SWT.NONE);
			innerArea.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
			innerArea.setLayout(new GridLayout());

			aclCheckbox = new Button(innerArea, SWT.CHECK);
			aclCheckbox.setText(I18N.DeleteAcl);
			aclCheckbox.setToolTipText(I18N.DeleteAclTooltip);

			historyCheckBox = new Button(innerArea, SWT.CHECK);
			historyCheckBox.setText(I18N.DeleteHistory);
			historyCheckBox.setToolTipText(I18N.DeleteHistoryTooltip);

			leadsCheckBox = new Button(innerArea, SWT.CHECK);
			leadsCheckBox.setText(I18N.DeleteLeads);
			leadsCheckBox.setToolTipText(I18N.DeleteLeadsTooltip);

			creditCardCheckBox = new Button(innerArea, SWT.CHECK);
			creditCardCheckBox.setText(I18N.DeleteCreditCards);
			creditCardCheckBox.setToolTipText(I18N.DeleteCreditCardsTooltip);

			portalPhotosCheckBox = new Button(innerArea, SWT.CHECK);
			portalPhotosCheckBox.setText(I18N.DeletePortalPhotos);
			portalPhotosCheckBox.setToolTipText(I18N.DeletePortalPhotosTooltip);

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return area;
	}

	/**
	 * The Window Icon and Title are made identical to the icon and the tooltip of the button that opens this dialog.
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(UtilI18N.Question);
	}


	@Override
	protected void okPressed() {
		deleteAcl = aclCheckbox.getSelection();
		deleteLeads = leadsCheckBox.getSelection();
		deleteHistory = historyCheckBox.getSelection();
		deleteCreditCard = creditCardCheckBox.getSelection();
		deletePortalPhotos = portalPhotosCheckBox.getSelection();

		super.okPressed();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	public boolean isDeleteAcl() {
		return deleteAcl;
	}


	public boolean isDeleteHistory() {
		return deleteHistory;
	}


	public boolean isDeleteLeads() {
		return deleteLeads;
	}


	public boolean isDeleteCreditCard() {
		return deleteCreditCard;
	}


	public boolean isDeletePortalPhotos() {
		return deletePortalPhotos;
	}

}
