package de.regasus.finance.invoicenumberrange.dialog;

import java.io.File;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.invoice.interfaces.InvoiceTemplateType;
import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.rcp.widget.FileSelectionComposite;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.combo.LanguageCombo;


public class UploadInvoiceTemplateDialog extends TitleAreaDialog {

	private String language;
	
	private File initFile;

	private String filePath;

	private InvoiceTemplateType invoiceTemplateType;


	// List of existing templates to be able to open a confirmation dialog to ask the user if he want to overwrite
	private List<DataStoreVO> existingInvoiceTemplateVOList;

	// widgets
	private Combo typeCombo;
	private FileSelectionComposite fileSelectionComposite;
	private Button okButton;
	private LanguageCombo languageCombo;



	public UploadInvoiceTemplateDialog(Shell shell, File initFile, InvoiceTemplateType initInvoiceTemplateType) {
		super(shell);
		this.initFile = initFile;
		this.invoiceTemplateType = initInvoiceTemplateType;
	}


	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		// Set the title
		setTitle(CoreI18N.UploadTemplateDialog_Title);
		
		return contents;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		try {
			composite.setLayout(new GridLayout(2, false));
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			composite.setFont(parent.getFont());
			
			// typeLabel
			Label typeLabel = new Label(composite, SWT.RIGHT);
			typeLabel.setText(UtilI18N.Type);
			
			// typeCombo
			typeCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
			typeCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			for (InvoiceTemplateType templateType : InvoiceTemplateType.values()) {
				typeCombo.add(templateType.getString());
			}
			// set initial selection
			if (invoiceTemplateType != null) {
				typeCombo.select(invoiceTemplateType.ordinal());
			}
			else {
				typeCombo.select(0);
			}
			
			// languageLabel
			Label languageLabel = new Label(composite, SWT.RIGHT);
			languageLabel.setText(UtilI18N.Language);

			// languageCombo
			languageCombo = new LanguageCombo(composite, SWT.NONE);
			languageCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			// fileLabel
			Label fileLabel = new Label(composite, SWT.RIGHT);
			fileLabel.setText(UtilI18N.File);
			
			// fileSelectionComposite
			fileSelectionComposite = new FileSelectionComposite(composite, SWT.OPEN);
			fileSelectionComposite.setFilterExtensions(new String[] { "*.odt" }); 
			fileSelectionComposite.setFile(initFile);
			fileSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fileSelectionComposite.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					refreshOkButtonState();
				}
			});
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return composite;
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		refreshOkButtonState();
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	private void refreshOkButtonState() {
		boolean fileOrDirExists = fileSelectionComposite.fileForOpenOrDirForSaveExists();
		okButton.setEnabled(fileOrDirExists);
	}


	@Override
	protected void okPressed() {
		/* Check if a template with same type and language already exists.
		 * If so, open confirmation dialog.
		 */
		invoiceTemplateType = InvoiceTemplateType.values()[typeCombo.getSelectionIndex()];
		language = languageCombo.getLanguageCode();
		filePath = fileSelectionComposite.getFilePath();
		
		for (DataStoreVO dataStoreVO : existingInvoiceTemplateVOList) {
			if (invoiceTemplateType.name().equals(dataStoreVO.getDocType())
				&& 
				EqualsHelper.isEqual(language, dataStoreVO.getLanguage())
			) {
				boolean ok = MessageDialog.openQuestion(
					this.getShell(), 
					UtilI18N.Question, 
					CoreI18N.TemplateExists_ConfirmOverwrite
				);
				if (! ok) {
					return;
				}
			}
		}
		
		super.okPressed();
	}

	
	public String getLanguage() {
		return language;
	}


	public String getFilePath() {
		return filePath;
	}


	public InvoiceTemplateType getInvoiceTemplateType() {
		return invoiceTemplateType;
	}


	public void setExistingTemplateVOList(List<DataStoreVO> invoiceTemplateVOList) {
		this.existingInvoiceTemplateVOList = invoiceTemplateVOList;
	}

}
