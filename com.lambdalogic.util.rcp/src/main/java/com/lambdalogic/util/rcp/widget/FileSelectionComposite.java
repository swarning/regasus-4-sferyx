package com.lambdalogic.util.rcp.widget;

import static com.lambdalogic.util.StringHelper.isNotEmpty;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

public class FileSelectionComposite extends Composite {

	private int openSaveStyle;
	private Text fileText;

	private String[] filterExtensions;
//	private String filterPath = null;
//	private String fileName = null;


	public FileSelectionComposite(Composite parent, int openSaveStyle) {
		super(parent, SWT.NONE);

		this.openSaveStyle = openSaveStyle;

		if (openSaveStyle != SWT.OPEN && openSaveStyle != SWT.SAVE ) {
			throw new IllegalArgumentException("Style must be SWT.OPEN or SWT.SAVE");
		}

		GridLayout layout = new GridLayout(2, false);
		setLayout(layout);

		fileText = new Text(this, SWT.BORDER);
		fileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Button fileButton = new Button(this, SWT.PUSH);
		fileButton.setText("...");
		fileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openFileDialog();
			}
		});
	}


	protected void openFileDialog() {
		FileDialog fileDialog = new FileDialog(this.getShell(), openSaveStyle);
		if (filterExtensions != null) {
			fileDialog.setFilterExtensions(filterExtensions);
		}

		File file = getFile();

		if (file != null) {
    		String filePath = null;
    		String fileName = null;
    		if (file.isDirectory()) {
    			filePath = file.getAbsolutePath();
    		}
    		else {
    			filePath = file.getParent();
    			fileName = file.getName();
    		}

    		fileDialog.setFilterPath(filePath);
    		fileDialog.setFileName(fileName);
		}

		String selectedFilePath = fileDialog.open();
		if (selectedFilePath != null) {
			fileText.setText(selectedFilePath);
		}
	}


	public void addModifyListener(ModifyListener modifyListener) {
		fileText.addModifyListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		fileText.removeModifyListener(modifyListener);
	}


	public boolean fileForOpenOrDirForSaveExists() {
		File file = getFile();
		if (file == null) {
			return false;
		}
		else if (openSaveStyle == SWT.OPEN) {
			return file.exists();
		}
		else if (openSaveStyle == SWT.SAVE) {
			return file.getParentFile() != null && file.getParentFile().exists();
		}
		else {
			// this can actually happen when there is only a file name entered, without path,
			// in which case file.getParentFile() is null
			return false;
		}
	}


	public File getFile() {
		String fileName = fileText.getText();
		if ( isNotEmpty(fileName) ) {
			return new File(fileName);
		}
		return null;
	}


	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		if (file != null) {
			fileText.setText(file.getPath());
		}
		else {
			fileText.setText("");
		}
	}


	public String getFilePath() {
		return fileText.getText();
	}

	/**
	 * @return the filterExtensions
	 */
	public String[] getFilterExtensions() {
		return filterExtensions;
	}

	/**
	 * @param filterExtensions the filterExtensions to set
	 */
	public void setFilterExtensions(String[] filterExtensions) {
		this.filterExtensions = filterExtensions;
	}

}
