package de.regasus.common.composite;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.widget.MultiLineText;

import de.regasus.common.FileSummary;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.ui.Activator;

public abstract class AbstractFileDetailsDialog extends TitleAreaDialog {

	private Composite area;

	protected Text documentNameText;

	protected MultiLineText descriptionText;

	protected FileSummary fileSummary;


	public AbstractFileDetailsDialog(Shell parentShell, FileSummary document) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);

		this.fileSummary = document;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(UtilI18N.Details);

		area = (Composite) super.createDialogArea(parent);

		Composite content = new Composite(area, SWT.NONE);
		content.setLayout(new GridLayout(2, false));
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		try {
			createEntry(content, UtilI18N.CreateDateTime, fileSummary.getNewTime().formatDefault());
			createEntry(content, UtilI18N.CreateUser, fileSummary.getNewDisplayUserStr());
			createEntry(content, UtilI18N.EditDateTime, fileSummary.getEditTime().formatDefault());
			createEntry(content, UtilI18N.EditUser, fileSummary.getEditDisplayUserStr());

			createEntry(content, UtilI18N.File, fileSummary.getExternalFileName());

			String fileSize = FileHelper.computeReadableFileSize(fileSummary.getSize());
			createEntry(content, UtilI18N.Size, fileSize);

			// name
			{
    			Label label = new Label(content, SWT.RIGHT);
    			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
    			label.setText(UtilI18N.Name);

    			documentNameText = new Text(content, SWT.BORDER);
    			documentNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    			// set data
    			documentNameText.setText(StringHelper.avoidNull(fileSummary.getName()));
			}

			// description
			{
    			Label label = new Label(content, SWT.RIGHT);
    			GridData gd_organisationLabel = new GridData(SWT.RIGHT, SWT.TOP, false, false);
    			gd_organisationLabel.verticalIndent = SWTConstants.VERTICAL_INDENT;
    			label.setLayoutData(gd_organisationLabel);
    			label.setText(UtilI18N.Description);

    			descriptionText = new MultiLineText(content, SWT.BORDER, false);
    			descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    			// set data
    			descriptionText.setText(StringHelper.avoidNull(fileSummary.getDescription()));
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return area;
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	private void createEntry(Composite composite, String leftText, String rightText) {
		Label label = new Label(composite, SWT.RIGHT);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		label.setText(leftText + ":");

		Label valueText = new Label(composite, SWT.LEFT);
		valueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		valueText.setText(StringHelper.avoidNull(rightText));
	}

}
