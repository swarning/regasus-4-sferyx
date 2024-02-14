package de.regasus.impex.db.dialog;

import java.io.File;

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
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;
import com.lambdalogic.util.rcp.widget.FileSelectionComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.impex.ImpexI18N;
import de.regasus.impex.ui.Activator;

public class ReportExportDialog extends TitleAreaDialog {

	/**
	 * The File which was chosen by the user when this dialog was used the last time (during the current application session) 
	 */
	private static File previousFile = null;
	
	
	private Button okButton;
	private FileSelectionComposite fileSelectionComposite;
	
	
	private File selectedFile;
	private UserReportDirVO userReportDirVO;

	
	public ReportExportDialog(Shell parentShell) throws Exception {
		super(parentShell);
		setShellStyle(getShellStyle()  | SWT.RESIZE );
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(ImpexI18N.ReportExportDialog_Title);
		setMessage(ImpexI18N.ReportExportDialog_Message);

		Composite area = (Composite) super.createDialogArea(parent);
		
		try {
			area.setLayout(new GridLayout());
			
			/*
			 * file selection
			 */
			fileSelectionComposite = new FileSelectionComposite(area, SWT.SAVE);
			fileSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fileSelectionComposite.setFilterExtensions(new String[]{"*.zip", "*.*"});
			
			/* set initial File
			 * Do this after observing fileSelectionComposite! Otherwise setting the initial File won't be recognized 
			 * and selectedFile remains empty.
			 */
			fileSelectionComposite.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e) {
					selectedFile = fileSelectionComposite.getFile();
					updateButtonStatus();
				}
			});
			
			if (previousFile != null) {
				fileSelectionComposite.setFile(previousFile);
			}
			else if (userReportDirVO != null) {
				File fileProposal = new File(userReportDirVO.getName() + ".zip");
				fileSelectionComposite.setFile(fileProposal);
			}
			
			
			// update OK Button after initializing the table and the file widget
			updateButtonStatus();
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
//		shell.setText(UtilI18N.Question);
	}

	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, ImpexI18N.ExportBtn, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		
		updateButtonStatus();
	}


	@Override
	protected void okPressed() {
		previousFile = selectedFile;
		super.okPressed();
	}


	private void updateButtonStatus() {
		if (okButton != null) {
			boolean enabled = selectedFile != null;
			okButton.setEnabled(enabled);
		}
	}

	
	public File getFile() {
		return selectedFile;
	}
	

	public void setUserReportDirVO(UserReportDirVO userReportDirVO) {
		this.userReportDirVO = userReportDirVO;
	}

}
