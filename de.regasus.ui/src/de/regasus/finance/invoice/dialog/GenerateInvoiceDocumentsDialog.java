package de.regasus.finance.invoice.dialog;

import static com.lambdalogic.report.oo.OpenOfficeConstants.DOC_FORMAT_PDF;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.report.oo.OpenOfficeConstants;
import com.lambdalogic.util.rcp.widget.DirectorySelectionComposite;

import de.regasus.I18N;
import de.regasus.common.combo.OpenOfficeFormatCombo;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.ui.Activator;

public class GenerateInvoiceDocumentsDialog extends TitleAreaDialog {

	private static final String DIALOG_SETTINGS = "GenerateInvoiceDocumentsDialog";


	/**
	 * The File which was chosen by the user when this dialog was used the last time (during the current application session)
	 */
	private static File previousFile = null;

	// input parameter
	private boolean showFormat = true;
	private int numberOfSelectedInvoices;



	// widgets
	private Button okButton;

	private OpenOfficeFormatCombo formatCombo;
	private Button showButton;
	private Button mergeButton;
	private Button saveButton;
	private DirectorySelectionComposite directorySelectionComposite;
	private Button printButton;


	// selected values
	private String format;
	private boolean show;
	private boolean save;
	private boolean print;
	private boolean merge;
	private File selectedFile;


	public GenerateInvoiceDocumentsDialog(Shell parentShell, boolean showFormat, int numberOfSelectedInvoices) throws Exception {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE );

		this.showFormat = showFormat;
		this.numberOfSelectedInvoices = numberOfSelectedInvoices;
	}


	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			updateButtonStatus();
		}
	};


	@Override
	protected Control createDialogArea(Composite parent) {
		try {
			setTitle(I18N.GenerateInvoiceDocumentsDialog_Title);
			setMessage(I18N.GenerateInvoiceDocumentsDialog_Message.replace("<count>", String.valueOf(numberOfSelectedInvoices)));

			Composite dialogArea = (Composite) super.createDialogArea(parent);

			Composite mainComposite = new Composite(dialogArea, SWT.NONE);
			mainComposite.setLayout(new GridLayout(3, false));
			mainComposite.setLayoutData( new GridData(GridData.FILL_BOTH) );

			if (showFormat) {
				// File Format
				Label formatLabel = new Label(mainComposite, SWT.NONE);
				formatLabel.setText(UtilI18N.Format);
				formatCombo = new OpenOfficeFormatCombo(
					mainComposite,
					SWT.READ_ONLY,
					OpenOfficeConstants.DOC_FORMAT_ODT // DocumentFormat of invoice templates (only ODT is supported)
				);
				formatCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

				formatCombo.addSelectionListener(selectionListener);
			}


			// Show
			new Label(mainComposite, SWT.NONE); // placeholder

			showButton = new Button(mainComposite, SWT.CHECK);
			showButton.setText(UtilI18N.Show);
			showButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			showButton.addSelectionListener(selectionListener);

			mergeButton = new Button(mainComposite, SWT.CHECK);
			mergeButton.setText(I18N.GenerateInvoiceDocumentsDialog_MergeDocuments);
			mergeButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			mergeButton.addSelectionListener(selectionListener);


			// Save
			new Label(mainComposite, SWT.NONE); // placeholder

			saveButton = new Button(mainComposite, SWT.CHECK);
			saveButton.setText(UtilI18N.Save);
			saveButton.addSelectionListener(selectionListener);

			directorySelectionComposite = new DirectorySelectionComposite(mainComposite);
			directorySelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));


			// Print
			new Label(mainComposite, SWT.NONE); // placeholder

			printButton = new Button(mainComposite, SWT.CHECK);
			printButton.setText(UtilI18N.Print);
			printButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			printButton.addSelectionListener(selectionListener);


			/* set initial File
			 * Do this after observing fileSelectionComposite! Otherwise setting the initial File won't be recognized
			 * and selectedFile remains empty.
			 */
			directorySelectionComposite.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent e) {
					updateButtonStatus();
				}
			});

			if (previousFile != null) {
				directorySelectionComposite.setDirectory(previousFile);
			}


			restoreDialogSettings();


			// update OK Button after initializing the table and the file widget
			updateButtonStatus();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return dialogArea;
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, UtilI18N.OK, true);

		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		updateButtonStatus();
	}


	@Override
	protected void okPressed() {
		if (showFormat) {
			format = formatCombo.getFormat().getFormatKey();
		}
		show = showButton.getSelection();
		merge = mergeButton.getSelection();
		save = saveButton.getSelection();
		selectedFile = directorySelectionComposite.getFile();
		print = printButton.getSelection();

		previousFile = selectedFile;

		saveDialogSettings();

		super.okPressed();
	}


	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_SETTINGS);
		if (section == null) {
			section = settings.addNewSection(DIALOG_SETTINGS);
		}
		return section;
	}


	private void saveDialogSettings() {
		IDialogSettings dialogSettings = getDialogSettings();
		dialogSettings.put("format", format);
		dialogSettings.put("show", show);
		dialogSettings.put("merge", merge);
		dialogSettings.put("save", save);
		dialogSettings.put("print", print);
		if (selectedFile != null) {
			dialogSettings.put("selectedFile", selectedFile.getAbsolutePath());
		}
	}


	private void restoreDialogSettings() {
		IDialogSettings dialogSettings = getDialogSettings();

		if (formatCombo != null) {
			formatCombo.setFormat( dialogSettings.get("format") );
		}
		showButton.setSelection( dialogSettings.getBoolean("show") );
		mergeButton.setSelection( dialogSettings.getBoolean("merge") );
		saveButton.setSelection( dialogSettings.getBoolean("save") );
		printButton.setSelection( dialogSettings.getBoolean("print") );

		String selectedFilePath = dialogSettings.get("selectedFile");
		if (selectedFilePath != null) {
			selectedFile = new File(selectedFilePath);
			directorySelectionComposite.setDirectory(selectedFile);
		}
	}


	private void updateButtonStatus() {
		if (okButton != null) {
			// user has to select open, save or print
			boolean isAtLeastOneSelected = showButton.getSelection() || saveButton.getSelection() || printButton.getSelection();

			// if save is selected, a directory has to be specified
			boolean isFileSelected = !saveButton.getSelection() || directorySelectionComposite.getFile() != null;

			okButton.setEnabled(isAtLeastOneSelected && isFileSelected);
		}

		mergeButton.setVisible(
			   numberOfSelectedInvoices > 1
			&& (formatCombo == null || formatCombo.getFormat() == DOC_FORMAT_PDF)
		);
		directorySelectionComposite.setVisible( saveButton.getSelection() );
	}


	public String getFormat() {
		return format;
	}


	public boolean isShow() {
		return show;
	}


	public boolean isSave() {
		return save;
	}

	public File getFile() {
		return selectedFile;
	}


	public boolean isPrint() {
		return print;
	}


	public boolean isMerge() {
		return merge;
	}

}
