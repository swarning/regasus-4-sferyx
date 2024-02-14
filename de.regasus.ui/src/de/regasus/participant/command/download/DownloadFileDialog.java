package de.regasus.participant.command.download;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.util.rcp.ModifySelectionAdapter;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.DirectorySelectionComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;

public class DownloadFileDialog extends TitleAreaDialog {

	private DirectorySelectionComposite directorySelectionComposite;
	private File directory;
	private Button printButton;
	private Button okButton;
	private boolean shouldPrint;
	private String message;

	public DownloadFileDialog(Shell parentShell, String message) {
		super(parentShell);

		this.message = message;
	}


	@Override
	public void create() {
		setShellStyle(SWT.RESIZE | SWT.SHELL_TRIM);
		super.create();
	}


	@Override
	protected Control createDialogArea(Composite parent) {

		setTitle(I18N.DownloadPrintFiles);
		setMessage(I18N.DownloadPrintParticipantFiles);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label sizeLabel = new Label(composite, SWT.WRAP);
		sizeLabel.setText(message);
		sizeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		SWTHelper.createLabel(composite, UtilI18N.Directory);

		directorySelectionComposite = new DirectorySelectionComposite(composite);
		directorySelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		directorySelectionComposite.addModifyListener(new ModifySelectionAdapter() {
			@Override
			public void handleEvent(TypedEvent event) {
				updateButtonStates();
			}
		});

		printButton = new Button(composite, SWT.CHECK | SWT.WRAP);
		printButton.setText(I18N.PrintToStandardPrinter);
		printButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		printButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonStates();
			}
		});

		Label hintsLabel = new Label(composite, SWT.WRAP);
		hintsLabel.setText(I18N.DownloadPrintParticipantFilesHints);
		hintsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		return composite;
	}


	@Override
	protected Point getInitialSize() {
		return new Point(500, 300);
	}


	/**
	 * The okButton is only to be enabled when a directory has been selected
	 */
	protected void updateButtonStates() {
		directory = directorySelectionComposite.getFile();
		shouldPrint = printButton.getSelection();

		if (directory != null && directory.exists() && directory.isDirectory()) {
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		okButton = createButton(parent, Dialog.OK, IDialogConstants.OK_LABEL, true);
		createButton(parent, Dialog.CANCEL, IDialogConstants.CANCEL_LABEL, false);

		updateButtonStates();
	}


	public File getDirectory() {
		return directory;
	}

	public boolean isShouldPrint() {
		return shouldPrint;
	}

}
