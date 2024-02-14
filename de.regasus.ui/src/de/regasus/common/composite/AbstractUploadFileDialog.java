package de.regasus.common.composite;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.FileSelectionComposite;
import com.lambdalogic.util.rcp.widget.MultiLineText;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public abstract class AbstractUploadFileDialog extends TitleAreaDialog {

	private Composite area;

	protected Text documentNameText;

	protected MultiLineText descriptionText;

	protected FileSelectionComposite fileSelectionComposite;

	private Button okButton;


	public AbstractUploadFileDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(UtilI18N.Upload);

		area = (Composite) super.createDialogArea(parent);

		Composite content = new Composite(area, SWT.NONE);
		content.setLayout(new GridLayout(2, false));
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		try {
			Label fileLabel = new Label(content, SWT.RIGHT);
			fileLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			fileLabel.setText(UtilI18N.File);

			fileSelectionComposite = new FileSelectionComposite(content, SWT.OPEN);
			fileSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fileSelectionComposite.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (fileSelectionComposite.fileForOpenOrDirForSaveExists()) {
						documentNameText.setText(fileSelectionComposite.getFile().getName());
						okButton.setEnabled(true);
						setErrorMessage(null);
					}
					else {
						setErrorMessage(null);
						okButton.setEnabled(false);
					}
				}
			});

			Label docNameLabel = new Label(content, SWT.RIGHT);
			docNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			docNameLabel.setText(UtilI18N.Name);

			documentNameText = new Text(content, SWT.BORDER);
			documentNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			Label descLabel = new Label(content, SWT.RIGHT);
			final GridData gd_organisationLabel = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			gd_organisationLabel.verticalIndent = SWTConstants.VERTICAL_INDENT;
			descLabel.setLayoutData(gd_organisationLabel);
			descLabel.setText(UtilI18N.Description);

			descriptionText = new MultiLineText(content, SWT.BORDER, false);
			descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return area;
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);

		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
}
